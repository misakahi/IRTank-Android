package com.example.xeno14.irtank;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;

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
    private LeverSwitchView leverLeft, leverRight;

    private static final int PLAY_INTERVAL = 200;  // ms

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        leverLeft = (LeverSwitchView)findViewById(R.id.lever_left);
        leverRight = (LeverSwitchView)findViewById(R.id.lever_right);
    }

    @Override
    protected void onResume() {
        super.onResume();

        leverLeft.invalidate();
        leverRight.invalidate();

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

    private int leverToMessage(float leverval) {
        int direction = leverval >= 0 ? (byte)0 : (byte)1;
        int value = (int)(7 * Math.abs(leverval));
        return ((direction << 3) & 0x8) + (value & 0x7);
    }

    private void startBackgroundThread() {
        running = true;
        backgroundThread = new Thread() {
            @Override
            public void run() {
                while (running && track != null) {
                    int left = leverToMessage(leverLeft.getValue());
                    int right = leverToMessage(leverRight.getValue());
                    int msg = ((left << 12) & 0xf000) + ((right << 8) & 0x0f00);

                    if (msg != 0) {
                        Log.v("play", String.format("%04x", msg));
                        irSound.setValue16bit((short)msg);

                        byte[] buf = irSound.getByteArray();

                        track.play();
                        track.write(buf, 0, buf.length);
                        track.stop();
                        track.flush();
                    }

                    try {
                        Thread.sleep(PLAY_INTERVAL, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        backgroundThread.start();
    }

    private void stopBackgroundThread() {
        track.stop();
    }
}
