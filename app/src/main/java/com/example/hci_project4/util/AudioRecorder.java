package com.example.hci_project3.util;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioRecorder {
    //microphone
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    //sample rate
    //44100
    private final static int AUDIO_SAMPLE_RATE = 44100;
//    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    //private int bufferSizeInBytes = AudioRecord.getMinBufferSize(0);
    private int bufferSizeInBytes = 0;

    private AudioRecord audioRecord;

    private Status status = Status.STATUS_NO_READY;

    private String fileName;

    private List<String> filesName = new ArrayList<>();

    private static class AudioRecorderHolder {
        /**
         * initialization
         */
        private static AudioRecorder instance = new AudioRecorder();
    }

    private AudioRecorder() {
    }

    public static AudioRecorder getInstance() {
        return AudioRecorderHolder.instance;
    }

    /**
     * create 
     */
    public void createAudio(String fileName, int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        // 
        bufferSizeInBytes = 7 * AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, channelConfig);
        audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        this.fileName = fileName;
    }

    /**
     *
     */
    public void createDefaultAudio(String fileName) {
        // 
        bufferSizeInBytes = 7 * AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
                AUDIO_CHANNEL, AUDIO_ENCODING);
        audioRecord = new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING, bufferSizeInBytes);
        this.fileName = fileName;
        status = Status.STATUS_READY;
    }


    /**
     */
    public void startRecord(final RecordStreamListener listener) {
        if (status == Status.STATUS_NO_READY || TextUtils.isEmpty(fileName)) {
            throw new IllegalStateException("Not recording, please start recording first or check the authorization.");
        }
        if (status == Status.STATUS_START) {
//            throw new IllegalStateException("recording ");
            throw new IllegalStateException("Recording");
        }
        Log.d("AudioRecorder", "===startRecord===" + audioRecord.getState());

        audioRecord.startRecording();

        new Thread(new Runnable() {
            @Override
            public void run() {
                writeDataTOFile(listener);
            }
        }).start();
    }

    /**
     * pause
     */
    public void pauseRecord() {
        Log.d("AudioRecorder", "===pauseRecord===");
        if (status != Status.STATUS_START) {
//            throw new IllegalStateException(" no recording ");
            throw new IllegalStateException("Not recording");
        } else {
            status = Status.STATUS_PAUSE;
            audioRecord.stop();
//            status = Status.STATUS_PAUSE;
        }
    }

    /**
     * stop
     */
    public void stopRecord() {
        Log.d("AudioRecorder", "===stopRecord===");
        if (status == Status.STATUS_NO_READY || status == Status.STATUS_READY) {
//            throw new IllegalStateException(" not starting ");
            throw new IllegalStateException("Recording not start yet");
        } else {
            status = Status.STATUS_STOP;
            audioRecord.stop();
            release();
        }
    }

    /**
     * release
     */
    public void release() {
        Log.d("AudioRecorder", "===release===");
        //
        try {
            if (filesName.size() > 0) {
                List<String> filePaths = new ArrayList<>();
                for (String fileName : filesName) {
                    filePaths.add(FileUtil.getPcmFileAbsolutePath(fileName));
                }
                filesName.clear();
                //change pcm to wav file
                mergePCMFilesToWAVFile(filePaths);

            } else {
                //Log.d("AudioRecorder", "=====makePCMFileToWAVFile======");
                //makePCMFileToWAVFile();
            }
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e.getMessage());
        }

        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

        status = Status.STATUS_NO_READY;
    }

    /**
     * cancel recording
     */
    public void cancel() {
        filesName.clear();
        fileName = null;
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

        status = Status.STATUS_NO_READY;
    }


    /**
     * 
     *
     * @param listener 
     */
    private void writeDataTOFile(RecordStreamListener listener) {
        // new
        byte[] audiodata = new byte[bufferSizeInBytes];

        FileOutputStream fos = null;
        int readsize = 0;
        try {
            String currentFileName = fileName;
            if (status == Status.STATUS_PAUSE) {
                //pause current file for recording
                currentFileName += filesName.size();

            }
            filesName.add(currentFileName);
            File file = new File(FileUtil.getPcmFileAbsolutePath(currentFileName));
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);// 
        } catch (IllegalStateException e) {
            Log.e("AudioRecorder", e.getMessage());
            throw new IllegalStateException(e.getMessage());
        } catch (FileNotFoundException e) {
            Log.e("AudioRecorder", e.getMessage());

        }
        //
        status = Status.STATUS_START;
        while (status == Status.STATUS_START) {
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize && fos != null) {
                try {
                    fos.write(audiodata);
                    if (listener != null) {
                        listener.recordOfByte(audiodata, 0, audiodata.length);
                    }
                } catch (IOException e) {
                    Log.e("AudioRecorder", e.getMessage());
                }
            }
        }
        try {
            if (fos != null) {
                fos.close();// 
            }
        } catch (IOException e) {
            Log.e("AudioRecorder", e.getMessage());
        }
    }

    /**
     *
     * @param filePaths
     */
    private void mergePCMFilesToWAVFile(final List<String> filePaths) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (PcmToWav.mergePCMFilesToWAVFile(filePaths, FileUtil.getWavFileAbsolutePath(fileName))) {
                } else {
                    Log.e("AudioRecorder", "mergePCMFilesToWAVFile fail");
                    throw new IllegalStateException("mergePCMFilesToWAVFile fail");
                }
                fileName = null;
            }
        }).start();
    }

    private void makePCMFileToWAVFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (PcmToWav.makePCMFileToWAVFile(FileUtil.getPcmFileAbsolutePath(fileName), FileUtil.getWavFileAbsolutePath(fileName), true)) {
                } else {
                    Log.e("AudioRecorder", "makePCMFileToWAVFile fail");
                    throw new IllegalStateException("makePCMFileToWAVFile fail");
                }
                fileName = null;
            }
        }).start();
    }

    /**
     * @return
     */
    public Status getStatus() {
        return status;
    }

    /**
     *
     * @return
     */
    public int getPcmFilesCount() {
        return filesName.size();
    }

    /**
     */
    public enum Status {
        STATUS_NO_READY,
        STATUS_READY,
        STATUS_START,
        STATUS_PAUSE,
        STATUS_STOP
    }

}
