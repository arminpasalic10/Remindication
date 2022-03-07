package com.example.remidication;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.widget.Adapter;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.Serializable;
import java.util.ArrayList;

public class ReminderBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "remidicationchannel")
                .setSmallIcon(R.drawable.ic_baseline_healing_24)
                .setContentTitle("Its time to take your medications")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        Bundle data = intent.getExtras();
        Bundle extra = intent.getBundleExtra("extra");
        int request_code = intent.getIntExtra("requestcode", 0);
        ArrayList<NewReminderList> objects = (ArrayList<NewReminderList>) extra.getSerializable("objects");

        Bundle extras = new Bundle();
        extras.putSerializable("objects", objects);
        Intent new_intent = new Intent(context, NotificationActivity.class);
        new_intent.putExtra("extra", extras);
        new_intent.putExtra("requestcode", request_code);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new_intent, PendingIntent.FLAG_UPDATE_CURRENT);


        builder.setContentIntent(contentIntent);


        NotificationManagerCompat notificationManager =  NotificationManagerCompat.from(context);
        notificationManager.notify(200, builder.build());

    }
}
