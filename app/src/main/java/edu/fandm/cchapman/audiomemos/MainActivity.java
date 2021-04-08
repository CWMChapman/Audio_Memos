package edu.fandm.cchapman.audiomemos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    // USING MediaRecorder API
    // https://developer.android.com/reference/android/media/MediaRecorder?authuser=1
    // OVERVIEW: https://developer.android.com/guide/topics/media/mediarecorder

    private static final String LOG_TAG = "AudioRecordTest";
    private static final String DEFAULT_TAG = "Default Tag";
    public static boolean isRecording = false;


    MediaRecorder mr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button b = findViewById(R.id.record_button);
        b.setBackgroundColor(Color.GRAY);
        b.setTextColor(Color.WHITE);
    }

    public void record(View v) {
        // callback function when tapping the record/recording... button
        vibrate();
        Button b = findViewById(R.id.record_button);
        if (isRecording == false) {
            startRecording(b);
        } else {
            stopRecording(b);
        }
        isRecording = !isRecording; // toggle recording status
    }

    public void startRecording(Button b) {
        Log.d(LOG_TAG, "Switching to recording mode.");
        b.setBackgroundColor(Color.RED);
        b.setText("Recording...");

        File storageDirectory = Environment.getExternalStorageDirectory();
        File recordingFile;
        try {
            recordingFile = File.createTempFile("sound", ".3gp", storageDirectory);
        } catch (IOException e) {
            Log.e(DEFAULT_TAG, "external storage access error");
            return;
        }

        mr = new MediaRecorder();
        mr.setAudioSource(MediaRecorder.AudioSource.MIC);
        mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mr.setOutputFile(recordingFile.getAbsolutePath());
    }

    public void stopRecording(Button b) {
        Log.d(LOG_TAG, "Finishing recording.");
        b.setBackgroundColor(Color.GRAY);
        b.setText("Record");

        mr.stop();
        mr.release();
        // now we need to list the file on the screen
    }


    public void vibrate() {
        Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect ve = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE);
        v.vibrate(ve);
    }
}
