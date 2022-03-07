package com.example.remidication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RemidicationAdapter extends RecyclerView.Adapter<RemidicationAdapter.MyViewHolder> {
    Context context;
    ArrayList<RemidicationApp> remidication;




    public RemidicationAdapter(Context c, ArrayList<RemidicationApp> p)
    {
        context = c;
        remidication = p;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.title.setText(remidication.get(position).title);
        holder.time.setText(remidication.get(position).time);
        holder.recycler_view_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(context, NewReminderActivity.class);
                a.putExtra("FROM_ACTIVITY", "B");
                a.putExtra("Time", remidication.get(position).time);
                context.startActivity(a);
            }
        });
    }

    @Override
    public int getItemCount() {
        return remidication.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView time;
        LinearLayout recycler_view_item;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.medication_title);
            time = (TextView) itemView.findViewById(R.id.time);
            recycler_view_item = itemView.findViewById(R.id.recycler_view_item);
        }
    }

}
