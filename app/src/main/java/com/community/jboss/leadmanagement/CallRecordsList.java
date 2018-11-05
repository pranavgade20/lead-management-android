package com.community.jboss.leadmanagement;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class CallRecordsList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_records_list);

        Intent intent = getIntent();
        if(intent.hasExtra("stopRecord") && intent.getBooleanExtra("stopRecord", false) && CallRecordActivity.mRecorder!=null) {
            CallRecordActivity.mRecorder.stop();
            CallRecordActivity.mRecorder.release();
            CallRecordActivity.mRecorder = null;
        }

        Toast myToast = Toast.makeText(this, "Stopped Recording", Toast.LENGTH_LONG);
        myToast.show();
        // TODO: implement a listView to show all files here, and play option
    }
}
