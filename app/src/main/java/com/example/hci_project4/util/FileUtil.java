package com.example.hci_project3.util;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    private static String rootPath = "AudioAppStorage";
    private static String basePath = Environment.getExternalStorageDirectory().getAbsolutePath();

    private final static String AUDIO_PCM_BASEPATH = "/" + rootPath + "/pcm/";
    private final static String AUDIO_WAV_BASEPATH = "/" + rootPath + "/wav/";

    private final static String CSV_BASEPATH = "/" + rootPath + "/csv/";
    private final static String VIDEO_BASEPATH = "/" + rootPath + "/video/";

    private static void setRootPath(String rootPath) {
        FileUtil.rootPath = rootPath;
    }

    public static void setBasePath(String basePath)
    {
        FileUtil.basePath = basePath;
    }

    public static String getPcmFileAbsolutePath(String fileName) {
        return getFileAbsolutePath("pcm", fileName);
    }

    public static String getCSVFileAbsolutePath(String fileName)
    {
        return getFileAbsolutePath("csv", fileName);
    }

    private static String getFileAbsolutePath(String type, String fileName)
    {
        if (fileName == null) {
            throw new NullPointerException("fileName can't be null");
        }
        if (!isSdcardExit()) {
            throw new IllegalStateException("sd card no found");
        }

        String mAudioWavPath = "";
        if (isSdcardExit()) {
            File videoFolder = new File(basePath + VIDEO_BASEPATH);
            if (!videoFolder.exists()) {
                videoFolder.mkdirs();
            }
            if (!fileName.endsWith("." + type)) {
                fileName = fileName + '.' + type;
            }
            String fileBasePath = basePath + getBasePathByType(type);
            File file = new File(fileBasePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            mAudioWavPath = fileBasePath + fileName;
        }
        return mAudioWavPath;
    }

    private static String getBasePathByType(String type)
    {
        if (type.equals("wav")) {
            return AUDIO_WAV_BASEPATH;
        } else if (type.equals("pcm")) {
            return AUDIO_PCM_BASEPATH;
        } else {
            return CSV_BASEPATH;
        }
    }

    public static String getWavFileAbsolutePath(String fileName) {
        return getFileAbsolutePath("wav", fileName);
    }

    /**
     *
     * @return true | false
     */
    public static boolean isSdcardExit() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    /**
     *
     * @return
     */
    public static List<File> getPcmFiles() {
        List<File> list = new ArrayList<>();
        String fileBasePath = basePath + AUDIO_PCM_BASEPATH;

        File rootFile = new File(fileBasePath);
        if (!rootFile.exists()) {
        } else {

            File[] files = rootFile.listFiles();
            for (File file : files) {
                list.add(file);
            }

        }
        return list;
    }

    /**
     *
     * @return
     */
    public static List<File> getWavFiles() {
        List<File> list = new ArrayList<>();
        String fileBasePath = basePath + AUDIO_WAV_BASEPATH;

        File rootFile = new File(fileBasePath);
        if (!rootFile.exists()) {
        } else {
            File[] files = rootFile.listFiles();
            for (File file : files) {
                list.add(file);
            }

        }
        return list;
    }

    /**
     *
     * @return
     */
    public static String[] getWavFilesStrings() {
        List<File> list = new ArrayList<>();
        String fileBasePath = basePath + AUDIO_WAV_BASEPATH;

        File rootFile = new File(fileBasePath);
        if (!rootFile.exists()) {
        } else {
            File[] files = rootFile.listFiles();
            for (File file : files) {
                list.add(file);
            }

        }
        String [] filestrings = new String[list.size()];
        for(int i = 0; i < list.size(); i++){
            filestrings[i] = ""+list.get(i);
        }
        for(String s : filestrings){
            Log.d("wavtostring", "getWavFiles: " + s);
        }
        return filestrings;
    }

    /**
     *
     * @return
     */
    public static List<File> getVideoFiles() {
        List<File> list = new ArrayList<>();
        String fileBasePath = basePath + VIDEO_BASEPATH;
        Log.d("getvideo", "getVideoFiles: ");
        File rootFile = new File(fileBasePath);
        if (!rootFile.exists()) {
        } else {
            File[] files = rootFile.listFiles();
            for (File file : files) {
                list.add(file);
            }

        }
        return list;
    }

    /**
     * @return
     */
    public static String[] getVideoFilesStrings() {
        List<File> list = new ArrayList<>();
        String fileBasePath = basePath + VIDEO_BASEPATH;

        File rootFile = new File(fileBasePath);
        if (!rootFile.exists()) {
        } else {
            File[] files = rootFile.listFiles();
            for (File file : files) {
                list.add(file);
            }

        }
        String [] filestrings = new String[list.size()];
        for(int i = 0; i < list.size(); i++){
            filestrings[i] = ""+list.get(i);
        }
        for(String s : filestrings){
            Log.d("videotostring", "getVideoFiles: " + s);
        }
        return filestrings;
    }

    public static List<File> getCSVFiles() {
        List<File> list = new ArrayList<>();
        String fileBasePath = basePath + CSV_BASEPATH;

        File rootFile = new File(fileBasePath);
        if (!rootFile.exists()) {
        } else {
            File[] files = rootFile.listFiles();
            for (File file : files) {
                list.add(file);
            }

        }
        return list;
    }
}
