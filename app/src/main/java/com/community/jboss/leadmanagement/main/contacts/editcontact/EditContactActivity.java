package com.community.jboss.leadmanagement.main.contacts.editcontact;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.community.jboss.leadmanagement.R;
import com.community.jboss.leadmanagement.StopCallRecord;
import com.community.jboss.leadmanagement.data.entities.ContactNumber;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.community.jboss.leadmanagement.SettingsActivity.PREF_DARK_THEME;

public class EditContactActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_CONTACT_NUM = "INTENT_EXTRA_CONTACT_NUM";

    public static MediaRecorder mRecorder;
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    Intent intent;

    @BindView(R.id.add_contact_toolbar)
    android.support.v7.widget.Toolbar toolbar;
    @BindView(R.id.contact_name_field)
    EditText contactNameField;
    @BindView(R.id.contact_number_field)
    EditText contactNumberField;

    private EditContactActivityViewModel mViewModel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useDarkTheme = preferences.getBoolean(PREF_DARK_THEME, false);

        if(useDarkTheme) {
            setTheme(R.style.AppTheme_BG);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_contact);

        ButterKnife.bind(this);

        mViewModel = ViewModelProviders.of(this).get(EditContactActivityViewModel.class);
        mViewModel.getContact().observe(this, contact -> {
            if (contact == null || mViewModel.isNewContact()) {
                setTitle(R.string.title_add_contact);
            } else {
                setTitle(R.string.title_edit_contact);
                contactNameField.setText(contact.getName());
            }
        });
        mViewModel.getContactNumbers().observe(this, contactNumbers -> {
            if (contactNumbers == null || contactNumbers.isEmpty()) {
                return;
            }
            // Get only the first one for now
            final ContactNumber contactNumber = contactNumbers.get(0);
            contactNumberField.setText(contactNumber.getNumber());
        });

        intent = getIntent();
        final String number = intent.getStringExtra(INTENT_EXTRA_CONTACT_NUM);
        if(mViewModel.getContactNumberByNumber(number)!=null){
            mViewModel.setContact(mViewModel.getContactNumberByNumber(number).getContactId());
        }else{
            mViewModel.setContact(null);
            contactNumberField.setText(number);
        }

        if (intent.hasExtra("record") && intent.getBooleanExtra("record", false)) {
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

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_close_black_24dp));
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
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
                            .setContentText("Failed to get permissions")
                            .setChannelId("lead-management-ch");

                    final NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify(intent.getIntExtra("notifId", 0), notification.build());

                }
                return;
            }
        }
    }

    public void showRecordNotification() {
        Intent stopRecordIntent = new Intent(getApplicationContext(), StopCallRecord.class);
        stopRecordIntent.putExtra("record", false);
        PendingIntent stopRecordPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, stopRecordIntent, 0);
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_call_black_24dp)
                .setContentTitle("Call in Progress")
                .setTicker("Lead Management")
                .setContentText("Recording call")
                .setChannelId("lead-management-ch")
                .setContentIntent(stopRecordPendingIntent)
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_save:
                saveContact();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_contact, menu);
        return true;
    }



    //TODO Add multiple numbers
    private void saveContact() {
        // Check is Name or Password is empty
        if (!checkEditText(contactNameField, "Please enter name")||!checkNo(contactNumberField,"Enter Correct no.")
                || !checkEditText(contactNumberField, "Please enter number")) {
            return;
        }


        final String name = contactNameField.getText().toString();
        mViewModel.saveContact(name);

        final String number = contactNumberField.getText().toString();
        mViewModel.saveContactNumber(number);

        finish();
    }

    private boolean checkEditText(EditText editText, String errorStr) {
        if (editText.getText().toString().isEmpty()) {
            editText.setError(errorStr);
            return false;
        }

        return true;
    }
    private boolean checkNo(EditText editText, String errorStr) {
        if (editText.getText().toString().length() < 4) {
            editText.setError(errorStr);
            return false;
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (intent.hasExtra("record")) {
            if (mRecorder != null) {
                mRecorder.release();
                mRecorder = null;

                Toast myToast = Toast.makeText(this, "Stopped Recording", Toast.LENGTH_LONG);
                myToast.show();
            }

            final NotificationManager manager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.cancel(intent.getIntExtra("notifId", 0));
            }
        }
    }
}
