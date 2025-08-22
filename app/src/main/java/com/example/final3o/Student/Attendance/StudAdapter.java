package com.example.final3o.Student.Attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.final3o.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StudAdapter extends RecyclerView.Adapter<StudAdapter.StudViewHolder> {

    private List<StudModel> attendanceList;

    // Constructor
    public StudAdapter(List<StudModel> attendanceList) {
        this.attendanceList = attendanceList;
        // Sort the list by date (assuming date is stored in "yyyy-MM-dd" format)
        Collections.sort(this.attendanceList, new Comparator<StudModel>() {
            @Override
            public int compare(StudModel o1, StudModel o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
    }

    @Override
    public StudViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_item, parent, false);
        return new StudViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StudViewHolder holder, int position) {
        StudModel attendance = attendanceList.get(position);
        holder.paperNameTextView.setText("Paper: " + attendance.getPaperName());
        holder.dateTextView.setText("Date: " + attendance.getDate());
        holder.statusTextView.setText("Status: " + attendance.getStatus());
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class StudViewHolder extends RecyclerView.ViewHolder {

        TextView paperNameTextView, dateTextView, statusTextView;

        public StudViewHolder(View itemView) {
            super(itemView);
            paperNameTextView = itemView.findViewById(R.id.paperNameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
        }
    }
}
