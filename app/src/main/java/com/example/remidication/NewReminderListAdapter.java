package com.example.remidication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NewReminderListAdapter extends  RecyclerView.Adapter<NewReminderListAdapter.MyViewHolder>
{

    ///////////////////////
    //////// THIS IS ADAPTER FOR THE RECYCLER VIEW WHICH NEEDDS TO BE CREATED IN ORDER TO FILL
    // THE LISTS THAT WE HAVE ON OUR SCREEN JUST GOOGLE RECYCLER VIEW ADAPTER

        Context context;

    public void setMedications(ArrayList<NewReminderList> medications) {
        this.medications = medications;
    }

    ArrayList<NewReminderList> medications;
        public NewReminderListAdapter(Context c, ArrayList<NewReminderList> test)
        {
            context = c;
            medications = test;
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.medication_item_new_reminder, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.name.setText(medications.get(position).medication_name);
            holder.deleteMedication.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(medications.size() > 0)
                    {
                        medications.remove(position);
                        notifyItemRemoved(position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return medications.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            ImageButton deleteMedication;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.medication_name);
                deleteMedication = itemView.findViewById(R.id.delete_medication);
            }
        }

    }


