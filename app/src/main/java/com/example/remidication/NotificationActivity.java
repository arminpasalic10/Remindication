package com.example.remidication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationActivity extends AppCompatActivity {
    //create variavles
    ArrayList<NewReminderList> medications_list;
    RecyclerView confirm_recycler_view;
    NewReminderListAdapter adapter;
    ImageButton delete_button;
    Button confirm_medications;
    Button remind_me;
    Button show_nearby;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get all references to this layout buttons text views etc
        setContentView(R.layout.notification_layout);
        Intent mIntent = getIntent();
        Bundle data = mIntent.getExtras();
        Bundle extra = mIntent.getBundleExtra("extra");
        ArrayList<NewReminderList> objects = (ArrayList<NewReminderList>) extra.getSerializable("objects");
        confirm_recycler_view = (RecyclerView) findViewById(R.id.confirm_recycler_view);
        confirm_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        confirm_medications = findViewById(R.id.confirm_medications);
        show_nearby = findViewById(R.id.show_nearby);


        adapter = new NewReminderListAdapter(this, objects);
        confirm_recycler_view.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if(objects.size() > 0)
        {
            delete_button = findViewById(R.id.delete_medication);
            //delete_button.setVisibility(View.GONE);
        }
        int request_code = getIntent().getIntExtra("requestcode", 0);

        // create spinner so hat user can choose option to remind him again to take medications after some time if he wants

        Spinner dropdown = findViewById(R.id.minutesspinner);
//create a list of items for the spinner.
        String[] items = new String[23];
        for (int a = 0; a < items.length; a++) {
            items[a] = String.valueOf(a+5) + " Minutes";
        }
//create an adapter to describe how the items are displayed, adapters are used in several places in android.
//There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
//set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        dropdown.setSelection(0);
        remind_me = findViewById(R.id.cancel_reminder);
        //if user clicks on the confrm button we just cancel the next alaram reminder so that we do not sisturb user
        // since he confirmed that he took the medictions
        confirm_medications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(NewReminderActivity.getAppContext(), ReminderBroadcast.class);


                PendingIntent pendingIntent = PendingIntent.getBroadcast(NewReminderActivity.getAppContext(), request_code, intent, PendingIntent.FLAG_NO_CREATE);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                if(pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                }

                finishAndRemoveTask();

            }
        });


        // if user clicks on the remind me button we just create a new alarm which wil be triggered when user selected after
        // specific number of minutes which user selecetd in the dropdown
        remind_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String minutes =  dropdown.getSelectedItem().toString();
                Intent intent = new Intent(NotificationActivity.this, ReminderBroadcast.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(NotificationActivity.this, 0, intent, 0);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Calendar mcurrentTime = Calendar.getInstance();

                long minutes_ms = TimeUnit.MINUTES.toMillis(Integer.parseInt(minutes));  //mcurrentTime.getTimeInMillis();
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mcurrentTime.getTimeInMillis() +  minutes_ms, AlarmManager.INTERVAL_DAY,  pendingIntent);


            }
        });

        // if user is not at home and clicks on the button to show neraby pharmcies we gake him to the maps
        show_nearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent a = new Intent(NotificationActivity.this, MapsActivity.class);
                startActivity(a);

            }
        });

    }
}