package com.community.jboss.leadmanagement;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.community.jboss.leadmanagement.main.contacts.editcontact.EditContactActivity;

public class CallRecordActivity extends AppCompatActivity {

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

        Intent intent = getIntent();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (intent.hasExtra("notifId")) {
            notificationManager.cancel(intent.getIntExtra("notifId", 0));
        } else {
            notificationManager.cancelAll();
        }

        final Intent notificationIntent = new Intent(getApplicationContext(), EditContactActivity.class);
        String CHANNEL_ID = "lead-management-ch";


        final PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_call_black_24dp)
                .setContentTitle("Call in Progress")
                .setTicker("Lead Management")
                .setContentIntent(contentIntent)
                .setContentText("Recording call")
                .setChannelId(CHANNEL_ID);

        
    }

}
