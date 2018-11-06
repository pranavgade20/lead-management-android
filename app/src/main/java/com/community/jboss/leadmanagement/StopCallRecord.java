package com.community.jboss.leadmanagement;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.community.jboss.leadmanagement.main.contacts.editcontact.EditContactActivity;

public class StopCallRecord extends IntentService {
    public StopCallRecord() {
        super("stop call record");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (EditContactActivity.mRecorder != null) {
            EditContactActivity.mRecorder.stop();
            EditContactActivity.mRecorder.release();
            EditContactActivity.mRecorder = null;
        }

        final NotificationManager manager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(intent.getIntExtra("notifId", 0));
        }
    }

}
