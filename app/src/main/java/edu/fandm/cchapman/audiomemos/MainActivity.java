package edu.fandm.cchapman.audiomemos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    // USING MediaRecorder API
    // https://developer.android.com/reference/android/media/MediaRecorder?authuser=1
    // OVERVIEW: https://developer.android.com/guide/topics/media/mediarecorder

    // App Icon Credit (Free Use): https://pixabay.com/vectors/microphone-icon-logo-design-mic-3404243/

    private static final String TAG = "AudioRecordTest";
    public static boolean isRecording = false;
    MediaRecorder mr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configureView(); // sets layout file and configures button colors, etc.

        boolean hasPermissions = CheckPermissions();
        if(!hasPermissions) {
            RequestPermissions();
        }
    }

    public void configureView() {
        // https://stackoverflow.com/questions/3663665/how-can-i-get-the-current-screen-orientation
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main);
        } else {
            setContentView(R.layout.activity_main_horizontal);
        }

        Button b = findViewById(R.id.record_button);
        b.setBackgroundColor(Color.GRAY);
        b.setTextColor(Color.WHITE);
        UpdateListView(); // refreshes the list view
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "config changed");
        super.onConfigurationChanged(newConfig);

        // https://developer.android.com/guide/topics/resources/runtime-changes.html
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_horizontal);
            Log.d(TAG, "setting horizontal content view");
            configureView();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView((R.layout.activity_main));
            Log.d(TAG, "setting vertical content view");
            configureView();
        }
    }

    public void Record(View v) {
        // callback function when tapping the record/recording... button
        Vibrate();
        Button b = findViewById(R.id.record_button);
        boolean hasPermission = CheckPermissions();
        Log.d(TAG, "hasPermission: " + hasPermission);
        if(hasPermission) {
            if (isRecording == false) {
                Log.d(TAG, "Switching to recording mode.");
                b.setBackgroundColor(Color.RED);
                b.setText("Recording...");
                startRecording(b);
            } else {
                Log.d(TAG, "Finishing recording.");
                b.setBackgroundColor(Color.GRAY);
                b.setText("Record");
                stopRecording(b);
            }
            isRecording = !isRecording; // toggle recording status
        }
        else {
            RequestPermissions();
        }

    }

    public void startRecording(Button b) {
        // state = mounted or unmounted
        String state = Environment.getExternalStorageState();
        Log.d(TAG, state);

        Context ctx = this.getApplicationContext();
        File audioDir = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AudioMemos");
        audioDir.mkdirs();
        String audioDirPath = audioDir.getAbsolutePath();
        Log.d(TAG, "Recording file location: " + audioDirPath);

        Date currentTime = Calendar.getInstance().getTime(); // current time
        String curTimeStr = currentTime.toString().replace(" ", "_");

        File recordingFile = new File(audioDirPath + "/" + curTimeStr + ".m4a");
        Log.d(TAG, "Created file: " + recordingFile.getName());

//        try {
//        } catch (IOException e) {
//            Log.e(TAG, "external storage access error");
//            return;
//        }

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
        // https://stackoverflow.com/questions/18430090/app-crashes-when-recorder-is-supposed-to-stop/18430237
        try{
            mr.stop();
        }catch(RuntimeException ex) {
            //Ignore
        }
        mr.release();
        UpdateListView(); // refreshes the list view

    }


    public void UpdateListView() {
        // this function is called at specific moments which would like finishing recoreding or deleting a recording that would need an updated list of files
        // List view: https://stackoverflow.com/questions/20750118/displaying-list-of-strings-in-android/20750202

        File audioDir = new File(this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AudioMemos");
        audioDir.mkdirs(); // makes this directory if one does not already exist (i.e. when a user first downloads this application)
        File[] audioDirContents = audioDir.listFiles();
        ArrayList<File> listViewContents = new ArrayList<File>(Arrays.asList(audioDirContents));
        ArrayList<String> audioDirContentsNames = new ArrayList<String>();
        for (File f : audioDirContents) {
            audioDirContentsNames.add(f.getName());
        }
        audioDirContentsNames.sort(Comparator.comparing(String::toString)); // sort the list alphabetically
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.textview_for_listview, audioDirContentsNames);
        ListView lv = (ListView) this.findViewById(R.id.lv_AudioFiles);
        lv.setAdapter(adapter); // populate the list view


        lv.setOnItemClickListener(this);
        lv.setOnItemLongClickListener(this);

    }


    public boolean CheckPermissions() {
        boolean hasRecordAudio = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean hasWriteExternalStorage = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean hasReadExternalStorage = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        Log.d(TAG, "hasRecordAudio: " + hasRecordAudio);
        Log.d(TAG, "hasWriteExternalStorage: " + hasWriteExternalStorage);
        Log.d(TAG, "hasReadExternalStorage: " + hasReadExternalStorage);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File audioDir = new File(this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AudioMemos");
        String audioFileName = parent.getItemAtPosition(position).toString();
        String audioFilePath = audioDir + "/" + audioFileName;
        File audioFile = new File(audioFilePath);
        Log.d(TAG, audioFileName + " clicked!");
//        ;
//        int[] attrs = new int[]{R.attr.selectableItemBackground};
//        TypedArray typedArray = this.obtainStyledAttributes(attrs);
//        int backgroundResource = typedArray.getResourceId(0, 0);
//        view.setBackgroundResource(backgroundResource);

        // https://developer.android.com/reference/android/media/MediaPlayer
        MediaPlayer mp = new MediaPlayer();
        try {
            // I had problems setting the data source as a string and it turns out that thats normal:
            // https://stackoverflow.com/questions/9625680/mediaplayer-setdatasource-better-to-use-path-or-filedescriptor
            // https://stackoverflow.com/questions/3773262/mediaplayer-cant-play-audio-files-from-program-data-folder
            mp.setDataSource((new FileInputStream(audioFilePath)).getFD());
            mp.prepare();
        } catch (IOException ioe) {
            Log.d(TAG, "IOE! Cannot set Datasource");
        }
        mp.setLooping(false);
        mp.start();

        Toast t = Toast.makeText(this.getApplicationContext(), "Playing Recording", Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, -1000);
        t.show();

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        File audioDir = new File(this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AudioMemos");
        String audioFileName = parent.getItemAtPosition(position).toString();
        String audioFilePath = audioDir + "/" + audioFileName;
        File audioFile = new File(audioFilePath);
        Log.d(TAG, audioFileName + " LONG clicked!");

        // Alert Dialogue
        // https://developer.android.com/guide/topics/ui/dialogs
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setMessage("Do you want to delete this recording?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                Log.d(TAG, "User clicked Delete Button");
                audioFile.delete();

                Toast t = Toast.makeText(MainActivity.super.getApplicationContext(), "Recording Deleted", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, -1000);
                t.show();

                UpdateListView(); // because we want to remove that entry from the listview
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                Log.d(TAG, "User clicked Cancel Button");

                Toast t = Toast.makeText(MainActivity.super.getApplicationContext(), "Recording Not Deleted", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, -1000);
                t.show();
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();


        return true;
    }
}
