package com.chrischen.androidwaveview;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.chrischen.waveview.WaveView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    WaveView waveView;
    MyHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        waveView = (WaveView) findViewById(R.id.waveView);

        handler = new MyHandler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!waveView.hasOver()){
                    Message msg =handler.obtainMessage();
                    handler.sendMessage(msg);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }


    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Random random = new Random();
            waveView.putValue(random.nextInt(100));
        }
    }

}
