package com.example.remidication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class NewReminderActivity extends AppCompatActivity {

    //define needed variables
    TextView title;
    TextView desc;
    TextView time;
    EditText medication_title;
    EditText reminder_time;
    Button create_reminder;
    Button cancel_reminder;
    EditText test;
    DatabaseReference reference;
    RecyclerView medication_list;
    NewReminderListAdapter reminderListAdapter;
    ArrayList<NewReminderList> listofmedications;
    String id;
    private static Context context;
    String previousActivity;
    ImageButton add_medication;
    Button save_reminder;
    Button delete_reminder;
    String chosenRingtone;
    ArrayList<NewReminderList> edit_medications_list;
    long start_time = 0;
    // this is used for the unique id for the alarm since every alarm has its wn request code and
    // by using this request code we can cancel this alarm from another activity
    AtomicInteger c = new AtomicInteger(0);
    String time_value;
    Integer reminderNum = new Random().nextInt();
    ArrayList<NewReminderList> copy_of_medications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NewReminderActivity.context = getApplicationContext();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reminder);
        title = findViewById(R.id.titlepage);

        Typeface typeface = ResourcesCompat.getFont(
                this,
                R.font.merriweathersansregular);
        title.setTypeface(typeface);
        // again gettin references to all buttons etc that we defined in the layout file of this activity
        reminder_time = findViewById(R.id.reminder_time);
        medication_title = findViewById(R.id.edit_medication_field);
        desc = findViewById(R.id.description_text);
        time = findViewById(R.id.time_title);
        create_reminder = findViewById(R.id.create_reminder);
        cancel_reminder = findViewById(R.id.cancel_reminder);
        delete_reminder  = findViewById(R.id.delete_reminder);

        test = findViewById(R.id.test);
        add_medication = findViewById(R.id.add_medication);
        medication_list = (RecyclerView) findViewById(R.id.new_reminder_medication_list);
        medication_list.setLayoutManager(new LinearLayoutManager(this));
        listofmedications = new ArrayList<NewReminderList>();
        NewReminderList new_item = new NewReminderList();
        new_item.setMedication_name("No medications yet");
        listofmedications.add(new_item);
        // we again set adapter for our list
        reminderListAdapter = new NewReminderListAdapter(this, listofmedications);
        medication_list.setAdapter(reminderListAdapter);
        listofmedications.clear();

// this sis used to store the variable value to the storage of the device so eeven if app is closed we can get the value
        /// of the variable next time
        SharedPreferences sharedPrefs = getSharedPreferences("uniqueId", MODE_PRIVATE);
        SharedPreferences.Editor ed;


// if not defined in the storage we put into the storage the value
        if(!sharedPrefs.contains("requestcode")){
            ed = sharedPrefs.edit();

            //Indicate that the default shared prefs have been set
            ed.putInt("requestcode", c.intValue());

            //Set some default shared pref

            ed.apply();
        }
        else
        {
            int c_copy = sharedPrefs.getInt("requestcode", 0);
            c = new AtomicInteger(c_copy);
        }

// ringtone picker
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                startActivity(intent);


            }
        });


        // create reminder button on clikc listener defines what should happen if user clicks on this button

        create_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // we get the reference to our databasse so that we can save new entries in the right place
                reference = FirebaseDatabase.getInstance().getReference().child("Remidication").child(id).
                        child("Remidication" + reminderNum);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // check if user entered any data if not show an error
                        if(medication_title.getText().toString().length() > 0 &&
                                reminder_time.getText().toString().length() > 0 && listofmedications.size() > 0)
                        {
                            // save values to the database
                            snapshot.getRef().child("medications").setValue(listofmedications);
                            snapshot.getRef().child("time").setValue(reminder_time.getText().toString());
                            // get new request code for the alarm
                            int new_request_code = getID();
                            // create new intent for the alarm and put values that you want acces from ankother activity
                            Intent intent = new Intent(NewReminderActivity.this, ReminderBroadcast.class);
                            Bundle extra = new Bundle();
                            extra.putSerializable("objects", listofmedications);
                            intent.putExtra("extra", extra);
                            intent.putExtra("requestcode", new_request_code);

                            //save current unique id (request code) value to the storage
                            SharedPreferences sharedPrefs = getSharedPreferences("uniqueId", MODE_PRIVATE);
                            SharedPreferences.Editor ed;
                            ed = sharedPrefs.edit();

                            //Indicate that the default shared prefs have been set
                            ed.putInt("requestcode", c.intValue());

                            //Set some default shared pref

                            ed.apply();


                            // create pending intent which is needed to set an alarm
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(NewReminderActivity.this, 0, intent, 0);
                            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(NewReminderActivity.this, new_request_code, intent, 0);
                            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            long time = System.currentTimeMillis();
                            Calendar mcurrentTime = Calendar.getInstance();

                            // get time value from the edit text field which user entered and split this time on : sign so that we have hours
                            // and minutes separated
                            String[] arrOfStr = reminder_time.getText().toString().split(":");
                           // we use calendar to set the time to the time when user wants his reminder to be and to get this time in the miliseconds
                            mcurrentTime.set(mcurrentTime.get(Calendar.YEAR), mcurrentTime.get(Calendar.MONTH), mcurrentTime.get(Calendar.DAY_OF_MONTH));

                            mcurrentTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arrOfStr[0]));
                            mcurrentTime.set(Calendar.MINUTE,Integer.parseInt(arrOfStr[1]));
                            mcurrentTime.set(Calendar.SECOND, 0);
                            //////////////////////////////////////////////////
                            long startTime = mcurrentTime.getTimeInMillis();

                            // we set the alarm to the time when user want it and also we set that it should be repeated every day
                            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime, AlarmManager.INTERVAL_DAY,  pendingIntent);
                            // we set another alarm 10 minutes later if user does not respond to the first alarm
                            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime + 600000, AlarmManager.INTERVAL_DAY,  pendingIntent2);

                            // wwe show the mai sccreen to te user after he clicked to add a new reminder
                            Intent a = new Intent(NewReminderActivity.this, MainActivity.class);
                            startActivity(a);
                        }
                        else
                        {
                            Toast errorToast = Toast.makeText(NewReminderActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT);
                            errorToast.show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
        desc.setTypeface(typeface);
        time.setTypeface(typeface);

        // this is a listener for the edit field where user can enter the time at which he wants to get the reminder
        // here we just get the value that user entered
        reminder_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                // we create new time picker and show it to the user so he can choose the time
                mTimePicker = new TimePickerDialog(NewReminderActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        //when user chose his time we save this value so that we can set up an alarm
                        String new_selectedHour ="";
                        String new_selectedMinute = "";
                        new_selectedHour = String.valueOf(selectedHour);
                        new_selectedMinute = String.valueOf(selectedMinute);
                        // this if statments just add 0 because if we select for xample time 09:00
                        // we will see it as 9:00 so we add a zero before so that it looks more beautiful
                        if(selectedHour < 10)
                        {
                            new_selectedHour  = "0" + new_selectedHour;
                        }
                        if(selectedMinute < 10)
                        {
                            new_selectedMinute  = "0" + new_selectedMinute;
                        }
                        // and we set the edit field to this value so that user can see which time he selected
                        reminder_time.setText( new_selectedHour + ":" + new_selectedMinute);

                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        /// when user clicks on the blue plus button when he wants to add another medication to the list of the medications
        add_medication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // we check if the medication already exists so that user acnt add the same medicatin two times
                for(NewReminderList medication : listofmedications)
                {
                    if(medication.getMedication_name().toLowerCase().equals(medication_title.getText().toString().toLowerCase()))
                    {
                        Toast errorToast = Toast.makeText(NewReminderActivity.this, "Medication already exists", Toast.LENGTH_SHORT);
                        errorToast.show();
                        return;
                    }
                }
                // we allow max six medications pro reminder (pro time)
                if(listofmedications.size() == 6)
                {
                    Toast errorToast = Toast.makeText(NewReminderActivity.this, "Max 6 medications is allowed", Toast.LENGTH_SHORT);
                    errorToast.show();
                }
                // if user did not entered eanything we show an error
                else if(medication_title.getText().toString().isEmpty())
                {
                    Toast errorToast = Toast.makeText(NewReminderActivity.this, "Please enter the name of the medication", Toast.LENGTH_SHORT);
                    errorToast.show();
                }
                else
                {
                    // if everything is fine we get the name of the meidcation that user enetered and add it to the list and display it
                    String name = medication_title.getText().toString().toUpperCase();
                    NewReminderList new_item = new NewReminderList();
                    new_item.setMedication_name(name);
                    listofmedications.add(new_item);
                    reminderListAdapter.setMedications(listofmedications);
                    reminderListAdapter.notifyDataSetChanged();
                }

            }
        });

        // if user clicks on the cancel button we just return him to the main screen
        cancel_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent a = new Intent(NewReminderActivity.this, MainActivity.class);
                startActivity(a);

            }
        });



        /// we get the id of the current device so that we can access the database
        // we also check if user came to edit some reminder
        FirebaseInstallations.getInstance().getId()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            Log.d("Installations", "Installation ID: " + task.getResult());
                            id = task.getResult();
                            Intent mIntent = getIntent();
                            previousActivity = mIntent.getStringExtra("FROM_ACTIVITY");
                            // this checks if user clicked on some item in the list to edit it
                            if(previousActivity.equals("B"))
                            {
                                time_value = mIntent.getStringExtra("Time");
                                myfunc(time_value);

                            }
                        } else {
                            Log.e("Installations", "Unable to get Installation ID");
                        }
                    }
                });

        // this deletes the reminder if user wants to delete some reminder on the edit screen
        delete_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GenericTypeIndicator<ArrayList<NewReminderList>> mapType = new GenericTypeIndicator<ArrayList<NewReminderList>>() {};

                reference = FirebaseDatabase.getInstance().getReference().child("Remidication").child(id);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                          // iterate through the database and find the list whihc user wants to delete and remove it from the database
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if(dataSnapshot.child("time").getValue() != null && dataSnapshot.child("time").getValue().equals(time_value)
                            && createListofStrings(copy_of_medications).equals(createListofStrings(dataSnapshot.child("medications").getValue(mapType))))
                            {
                                dataSnapshot.getRef().removeValue();
                            }


                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                        // redirect user to the main screen
                        Intent a = new Intent(NewReminderActivity.this, MainActivity.class);
                        startActivity(a);

            }
        });
    }

    // this function is used if user came from te main scrreen to edit some remnder (he clicked on some item in the list on the main screen)
    public void myfunc(String time)
    {
        GenericTypeIndicator<ArrayList<NewReminderList>> mapType = new GenericTypeIndicator<ArrayList<NewReminderList>>() {};
      // we nneed save remider button since user wants to edit the reminder and not to create one new..
        save_reminder = findViewById(R.id.save_reminder);
        save_reminder.setVisibility(View.VISIBLE);
        create_reminder.setVisibility(View.GONE);
        // when user clicks on the save reminder we save it to the databse the new edited
        // reminder we just replace the old one with the new values
        save_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference = FirebaseDatabase.getInstance().getReference().child("Remidication").child(id);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {



                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if(dataSnapshot.child("time").getValue() != null && dataSnapshot.child("time").getValue().equals(time_value)
                            && createListofStrings(copy_of_medications).equals(createListofStrings(dataSnapshot.child("medications").getValue(mapType))))
                            {
                                dataSnapshot.child("medications").getRef().setValue(listofmedications);
                                dataSnapshot.child("time").getRef().setValue(reminder_time.getText().toString());
                            }


                        }

                        int new_request_code = getID();
                        // create new intent for the alarm and put values that you want acces from ankother activity
                        Intent intent = new Intent(NewReminderActivity.this, ReminderBroadcast.class);
                        Bundle extra = new Bundle();
                        extra.putSerializable("objects", listofmedications);
                        intent.putExtra("extra", extra);
                        intent.putExtra("requestcode", new_request_code);

                        //save current unique id (request code) value to the storage
                        SharedPreferences sharedPrefs = getSharedPreferences("uniqueId", MODE_PRIVATE);
                        SharedPreferences.Editor ed;
                        ed = sharedPrefs.edit();

                        //Indicate that the default shared prefs have been set
                        ed.putInt("requestcode", c.intValue());

                        //Set some default shared pref

                        ed.apply();


                        // create pending intent which is needed to set an alarm
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(NewReminderActivity.this, 0, intent, 0);
                        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(NewReminderActivity.this, new_request_code, intent, 0);
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        long time = System.currentTimeMillis();
                        Calendar mcurrentTime = Calendar.getInstance();

                        // get time value from the edit text field which user entered and split this time on : sign so that we have hours
                        // and minutes separated
                        String[] arrOfStr = reminder_time.getText().toString().split(":");
                        // we use calendar to set the time to the time when user wants his reminder to be and to get this time in the miliseconds
                        mcurrentTime.set(mcurrentTime.get(Calendar.YEAR), mcurrentTime.get(Calendar.MONTH), mcurrentTime.get(Calendar.DAY_OF_MONTH));

                        mcurrentTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arrOfStr[0]));
                        mcurrentTime.set(Calendar.MINUTE,Integer.parseInt(arrOfStr[1]));
                        mcurrentTime.set(Calendar.SECOND, 0);
                        //////////////////////////////////////////////////
                        long startTime = mcurrentTime.getTimeInMillis();

                        // we set the alarm to the time when user want it and also we set that it should be repeated every day
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime, AlarmManager.INTERVAL_DAY,  pendingIntent);
                        // we set another alarm 10 minutes later if user does not respond to the first alarm
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime + 600000, AlarmManager.INTERVAL_DAY,  pendingIntent2);

                        Intent a = new Intent(NewReminderActivity.this, MainActivity.class);
                        startActivity(a);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });
        reference = FirebaseDatabase.getInstance().getReference().child("Remidication").child(id);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {

            // when user clicks on the item he wants to edit we get the list from zhe database so taht we can displayt the list of the item he seleceeted
            // so that he can edit it
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if(dataSnapshot.child("time").getValue() != null && dataSnapshot.child("time").getValue(String.class).equals(time))
                    {



                        ArrayList<NewReminderList> listofmedications_saver = dataSnapshot.child("medications").getValue(mapType);
                        if(listofmedications_saver != null)
                        {
                            listofmedications = new ArrayList<NewReminderList>(listofmedications_saver);

                        }

                    }

                }
                // we again fill in the list wit htese values
                reminderListAdapter.setMedications(listofmedications);
                copy_of_medications = new ArrayList<NewReminderList>(listofmedications);
                reminderListAdapter.notifyDataSetChanged();
                reminder_time.setText(time);
                delete_reminder.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getBaseContext(), "No data", Toast.LENGTH_SHORT).show();

            }
        });
    }

    // function that creates list of strings from the list of mediactions so that we can easier compare two lists
    public ArrayList<String> createListofStrings(ArrayList<NewReminderList> list_one)
    {
        ArrayList<String> new_list = new ArrayList<String>();
        for(NewReminderList item: list_one)
        {

            new_list.add(item.getMedication_name());
        }
        return  new_list;
    }

    public int getID() {
        return c.incrementAndGet();
    }

    public static Context getAppContext(){
        return NewReminderActivity.context;
    }
}