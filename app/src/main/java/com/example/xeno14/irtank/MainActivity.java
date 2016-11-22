package com.example.xeno14.irtank;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import static android.media.AudioManager.STREAM_MUSIC;


public class MainActivity extends Activity {
    private static final int STREAM_TYPE = STREAM_MUSIC;
    private static final int SAMPLE_RATE = IRSound.SAMPLE_RATE;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_STEREO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    private static final int MODE = AudioTrack.MODE_STREAM;
    private AudioTrack track;
    private Thread backgroundThread;
    private boolean running;
    private int count = 0;

    private IRSound irSound = new IRSound();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

    }

    @Override
    protected void onResume() {
        super.onResume();
        track = new AudioTrack(STREAM_TYPE, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE, MODE);
        startBackgroundThread();
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        track.release();
        track = null;
        super.onPause();
    }

    private void startBackgroundThread() {
        running = true;
        backgroundThread = new Thread() {
            @Override
            public void run() {
                while (running) {
                    irSound.setValue(count);
                    byte[] buf = irSound.getByteArray();
                    Log.v("track", "play " + count);

                    track.play();
                    track.write(buf, 0, buf.length);
                    track.stop();
                    track.flush();

                    try {
                        Thread.sleep(1000, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        //Log.e("track", e.printStackTrace());
                    }
                    count += 1;
                }
            }
        };
        backgroundThread.start();
    }


    private void stopBackgroundThread() {
        running = false;
        track.stop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }
}
