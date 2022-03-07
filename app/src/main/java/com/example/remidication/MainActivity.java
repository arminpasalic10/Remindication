package com.example.remidication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //we define needed varibles
    TextView title;
    TextView subtitle;
    TextView endpage;
    DatabaseReference reference;
    RecyclerView medication_recycler_view;
    ArrayList<RemidicationApp> medication_list;
    RemidicationAdapter remidicationAdapter;
    FloatingActionButton add_reminder_button;
    String id;
    ////////////////////////////////////////
    /////////////////

    /////////////////////////
    // On create method gets called when the activity is first created(started)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        //WE GET THE reference to the buttons and text views and other things we defined in the layout file for this
        // activity in this case we can find the variables in activity_main.xml file in layout folder
        title = findViewById(R.id.titlepage);
        medication_recycler_view = findViewById(R.id.medication_recycler_view);
        medication_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(getApplicationContext(), R.dimen.small_flag_height);
        medication_recycler_view.addItemDecoration(itemDecoration);
        medication_list = new ArrayList<RemidicationApp>();
        add_reminder_button = findViewById(R.id.add_reminder_button);
        /// every notification has its channel so we create the channel for the notifications
        createNotificationChannel();


        //we set thee font type for the title
        Typeface typeface = ResourcesCompat.getFont(
                this,
                R.font.merriweathersansregular);
        title.setTypeface(typeface);



        /// we define pn click listener for the add_reminder button where we define what shoud happen if
        // user clicks on this button
        add_reminder_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // we start a new activity which means we switch to another screen
                // So we switch to new reminder acitivity in this case
                Intent a = new Intent(MainActivity.this, NewReminderActivity.class);
                a.putExtra("FROM_ACTIVITY", "A");
                startActivity(a);
            }
        });



        //// commmunication with the database
//////////////// we get the instance of the databasse
        FirebaseInstallations.getInstance().getId()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            // heere we get the id whoch is different for every device that installs our app we use this
                            // id so we can distinguish in the databse betwwen different devices. so that every device has its own list of mediactions
                            // and other users cant see lists or reminders from other users
                            Log.d("Installations", "Installation ID: " + task.getResult());
                            id = task.getResult();
                            // we get the reference to the databse of the currrent device using the app by using unique id
                            reference = FirebaseDatabase.getInstance().getReference().child("Remidication").child(id);
                            reference.addValueEventListener(new ValueEventListener() {
                                // this needs to be done in order to acces array list from the databse since we ssave array list to the databs
                                /// and this array list contains objects of type NewReminderList
                                GenericTypeIndicator<ArrayList<NewReminderList>> mapType = new GenericTypeIndicator<ArrayList<NewReminderList>>() {
                                };


                                ArrayList<NewReminderList> listofmedications = new ArrayList<NewReminderList>();
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    // we iterate through entries i  the database
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        // we build the string for the main screen where every item
                                        // on the screen has mediactioons list title and contains every medication that user added to that list
                                        String builder = "Medications list: \n";
                                        String separator = ", ";
                                        listofmedications = dataSnapshot.child("medications").getValue(mapType);
                                        if(listofmedications != null) {


                                            for (NewReminderList medication : listofmedications) {
                                                builder = builder.concat(medication.getMedication_name());
                                                if (medication.getMedication_name() != listofmedications.get(listofmedications.size() - 1).getMedication_name()) {
                                                    builder = builder.concat(separator);
                                                }

                                            }
                                            // when we get the list of the mediactions and the time when the user needs to take it
                                            // we add this to the list since user can have multiple reminders
                                            RemidicationApp p = new RemidicationApp();
                                            p.setTitle(builder);
                                            p.setTime(dataSnapshot.child("time").getValue(String.class));
                                            medication_list.add(p);
                                            builder = "";
                                        }

                                    }
                                    // we pass all information that we gathered from the database to the adapter of our recycler view
                                    // recyler view is basically a list and it has its own adapter which fills this list with the data that we pass to it
                                    remidicationAdapter = new RemidicationAdapter(MainActivity.this, medication_list);
                                    medication_recycler_view.setAdapter(remidicationAdapter);
                                    remidicationAdapter.notifyDataSetChanged();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getBaseContext(), "No data", Toast.LENGTH_SHORT).show();

                                }
                            });

                        } else {
                            Log.e("Installations", "Unable to get Installation ID");
                        }
                    }
                });
    }

    private void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = "Remidicationreinderchannel";
            String description = "Channel for remidication";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel("remidicationchannel", name, importance);
            notificationChannel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

}

