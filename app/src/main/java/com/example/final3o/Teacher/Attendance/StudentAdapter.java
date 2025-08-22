package com.example.final3o.Teacher.Attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final3o.R;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<Student> studentList;

    public StudentAdapter(List<Student> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacherstudatten, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);

        holder.txtName.setText(student.getName());
        holder.txtId.setText(student.getStudentId());

        // Set radio button states based on attendance status
        if ("Present".equals(student.getAttendanceStatus())) {
            holder.radioButtonPresent.setChecked(true);
            holder.radioButtonAbsent.setChecked(false);
        } else {
            holder.radioButtonPresent.setChecked(false);
            holder.radioButtonAbsent.setChecked(true);
        }

        // Handle radio button selection
        holder.radioButtonPresent.setOnClickListener(v -> {
            student.setAttendanceStatus("Present");
        });

        holder.radioButtonAbsent.setOnClickListener(v -> {
            student.setAttendanceStatus("Absent");
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    // ViewHolder
    public static class StudentViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtId;
        RadioButton radioButtonPresent, radioButtonAbsent;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.student_name);  // fixed ID
            txtId = itemView.findViewById(R.id.student_id);      // added student ID
            radioButtonPresent = itemView.findViewById(R.id.radioButtonPresent);
            radioButtonAbsent = itemView.findViewById(R.id.radioButtonAbsent);
        }
    }
}
