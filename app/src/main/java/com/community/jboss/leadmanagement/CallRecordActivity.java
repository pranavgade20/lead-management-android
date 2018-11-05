package com.community.jboss.leadmanagement;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.community.jboss.leadmanagement.main.contacts.editcontact.EditContactActivity;

import java.io.File;
import java.io.IOException;

public class CallRecordActivity extends AppCompatActivity {
    @TargetApi(26)

    public static MediaRecorder mRecorder;
    private Intent intent;
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_record);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Added Note!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        intent = getIntent();

        //Remove notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (intent.hasExtra("notifId")) {
            notificationManager.cancel(intent.getIntExtra("notifId", 0));
        } else {
            // A rather harsh measure
            notificationManager.cancelAll();
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

        } else {
            startRecording();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecording();
                } else {
                    final NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_call_black_24dp)
                            .setContentTitle("Call in Progress")
                            .setTicker("Lead Management")
                            .setContentText("Recording call")
                            .setChannelId("lead-management-ch");

                    final NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify(intent.getIntExtra("notifId", 0), notification.build());

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void showRecordNotification() {
        Intent stopRecordIntent = new Intent(getApplicationContext(), CallRecordsList.class);
        stopRecordIntent.putExtra("stopRecord", true);
        PendingIntent stopRecordPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, stopRecordIntent, 0);
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_call_black_24dp)
                .setContentTitle("Call in Progress")
                .setTicker("Lead Management")
                .setContentText("Recording call")
                .setChannelId("lead-management-ch")
                .addAction(R.drawable.ic_close_black_24dp, "Stop Recording", stopRecordPendingIntent);

        final NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(intent.getIntExtra("notifId", 0), notification.build());
    }

    public void startRecording() {
        mRecorder = new MediaRecorder();

        String outputFileName = "";
        if(intent.hasExtra("fileName")) {
            // TODO: add a new file for each recording.
            outputFileName = intent.getStringExtra("fileName");
        } else {
            outputFileName = "record.3gp";
        }
        File outputPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        outputPath.mkdirs();
        File outputFile = new File(outputPath, outputFileName);

        mRecorder = new MediaRecorder();
        try{
            mRecorder.reset();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            mRecorder.setAudioSamplingRate(8000);
            mRecorder.setAudioEncodingBitRate(12200);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(outputFile.getPath());
            Log.e("FilePath:", outputFile.getPath());

            MediaRecorder.OnErrorListener errorListener = (arg0, arg1, arg2) -> Log.e("Err", "OnErrorListener " + arg1 + "," + arg2);
            mRecorder.setOnErrorListener(errorListener);

            MediaRecorder.OnInfoListener infoListener = (arg0, arg1, arg2) -> Log.e("Err", "OnInfoListener " + arg1 + "," + arg2);
            mRecorder.setOnInfoListener(infoListener);

            mRecorder.prepare();

            mRecorder.start();
        }catch (Exception e){
            Log.e("Err", "Error ", e);
        }

        showRecordNotification();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        final NotificationManager manager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(intent.getIntExtra("notifId", 0));
        }
    }
}
