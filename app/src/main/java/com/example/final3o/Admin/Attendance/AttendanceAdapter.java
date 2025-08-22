package com.example.final3o.Admin.Attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final3o.R;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    List<AttendanceModel> attendanceList;

    public AttendanceAdapter(List<AttendanceModel> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceModel model = attendanceList.get(position);
        holder.textName.setText(model.getName());
        holder.textUid.setText(model.getStudentId());
        holder.textStatus.setText("Status: " + model.getAttendanceStatus());

        // Safe null-check before color coding
        String status = model.getAttendanceStatus();
        if (status != null && status.equalsIgnoreCase("present")) {
            holder.textStatus.setTextColor(holder.textStatus.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.textStatus.setTextColor(holder.textStatus.getResources().getColor(android.R.color.holo_red_dark));
        }
    }


    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textUid, textStatus;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textUid = itemView.findViewById(R.id.textUid);
            textStatus = itemView.findViewById(R.id.textStatus);
        }
    }
}
