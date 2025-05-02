package com.example.hci_project3;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.hci_project3.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HXL on 16/8/11.
 */
public class ListActivity extends Activity {
    ListView listView;
    List<File> list = new ArrayList<>();
    FileListAdapter adapter;

    MediaPlayer player = new MediaPlayer();
    private TextView mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        listView = (ListView) findViewById(R.id.listView);
        if ("csv".equals(getIntent().getStringExtra("type"))) {
            list = FileUtil.getCSVFiles();
        } else if("pcm".equals(getIntent().getStringExtra("type"))) {
            list = FileUtil.getPcmFiles();
        } else if("wav".equals(getIntent().getStringExtra("type"))) {
            list = FileUtil.getWavFiles();
        } else {
            list = FileUtil.getVideoFiles();
        }

        adapter = new FileListAdapter(this, list);
        listView.setAdapter(adapter);

        initEvent();
    }

    private void initEvent() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String file = String.valueOf(list.get(position));
                int index = file.lastIndexOf("/");
                String fileName = file.substring(index + 1);
                Toast.makeText(getApplicationContext(), "Clicked " + fileName, Toast.LENGTH_SHORT).show();
                if(mState == null){
                    mState = findViewById(R.id.playStatus);
                    mState.setText("Playing: " + fileName);
                    try {
                        player.setDataSource(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        player.prepare();
                        player.start();
//                        player.setLooping(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    player.stop();
                    player.reset();
                    mState = findViewById(R.id.playStatus);
                    mState.setText("Playing: " + fileName);
                    try {
                        player.setDataSource(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        player.prepare();
                        player.start();
//                        player.setLooping(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}

