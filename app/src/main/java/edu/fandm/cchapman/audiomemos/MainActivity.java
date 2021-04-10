package edu.fandm.cchapman.audiomemos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity {

    // USING MediaRecorder API
    // https://developer.android.com/reference/android/media/MediaRecorder?authuser=1
    // OVERVIEW: https://developer.android.com/guide/topics/media/mediarecorder

    private static final String TAG = "AudioRecordTest";
    public static boolean isRecording = false;

    MediaRecorder mr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button b = findViewById(R.id.record_button);
        b.setBackgroundColor(Color.GRAY);
        b.setTextColor(Color.WHITE);
        UpdateListView(); // refreshes the list view

        boolean hasPermissions = CheckPermissions();

        if(!hasPermissions) {
            RequestPermissions();
        }
    }

    public void Record(View v) {
        // callback function when tapping the record/recording... button
        Vibrate();
        Button b = findViewById(R.id.record_button);
        if (isRecording == false) {
            startRecording(b);
        } else {
            stopRecording(b);
        }
        isRecording = !isRecording; // toggle recording status
    }

    public void startRecording(Button b) {
        Log.d(TAG, "Switching to recording mode.");
        b.setBackgroundColor(Color.RED);
        b.setText("Recording...");

        // state = mounted or unmounted
        String state = Environment.getExternalStorageState();
        Log.d(TAG, state);

        Context ctx = this.getApplicationContext();
        File audioDir = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_PODCASTS), "AudioMemos");
        audioDir.mkdirs();
        Log.d(TAG, "Recording file location: " + audioDir.getAbsolutePath());

        Date currentTime = Calendar.getInstance().getTime(); // current time
        String curTimeStr = currentTime.toString().replace(" ", "_");

        File recordingFile;
        try {
            recordingFile = File.createTempFile(curTimeStr, ".m4a", audioDir);
            Log.d(TAG, "Created file: " + recordingFile.getName());
        } catch (IOException e) {
            Log.e(TAG, "external storage access error");
            return;
        }

        mr = new MediaRecorder();
        mr.setAudioSource(MediaRecorder.AudioSource.MIC);
        mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mr.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mr.setOutputFile(recordingFile.getAbsolutePath());

        try {
            mr.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
        mr.start();
    }

    public void stopRecording(Button b) {
        Log.d(TAG, "Finishing recording.");
        b.setBackgroundColor(Color.GRAY);
        b.setText("Record");

        // https://stackoverflow.com/questions/18430090/app-crashes-when-recorder-is-supposed-to-stop/18430237
        try{
            mr.stop();
        }catch(RuntimeException ex) {
            //Ignore
        }
        mr.release();
        UpdateListView(); // refreshes the list view

        // TODO: now we need to list the file on the screen
    }


    public void UpdateListView() {
        // this function is called at specific moments which would like finishing recoreding or deleting a recording that would need an updated list of files
        // List view: https://stackoverflow.com/questions/20750118/displaying-list-of-strings-in-android/20750202

        File audioDir = new File(this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PODCASTS), "AudioMemos");
        audioDir.mkdirs(); // makes this directory if one does not already exist (i.e. when a user first downloads this application)
        File[] audioDirContents = audioDir.listFiles();
        ArrayList<File> listViewContents = new ArrayList<File>(Arrays.asList(audioDirContents));
        ArrayList<String> audioDirContentsNames = new ArrayList<String>();
        String s = "";
        for (File f : audioDirContents) {
            audioDirContentsNames.add(f.getName());
            s += f.getName() + ", ";
        }
        Log.d(TAG, s);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, audioDirContentsNames);
        ListView lv = (ListView) this.findViewById(R.id.lv_AudioFiles);
//        String AudioMemosDirectoryPath = audioDir.getPath();



    }


    public boolean CheckPermissions() {
        boolean hasRecordAudio = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean hasWriteExternalStorage = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean hasReadExternalStorage = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;


        // TODO: add other permissions later (like manage external storage, etc.)

        return hasRecordAudio && hasWriteExternalStorage && hasReadExternalStorage;
    }
    private void RequestPermissions() {
        ActivityCompat.requestPermissions(this, new String[] {RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 1 );
    }


    public void Vibrate() {
        Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect ve = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE);
        v.vibrate(ve);
    }
}
