package com.example.final3o.Teacher.Students;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final3o.R;
import java.util.List;

public class ChangeRequestAdapter extends RecyclerView.Adapter<ChangeRequestAdapter.ChangeRequestViewHolder> {

    private Context context;
    private List<ChangeRequest> requestList;

    public ChangeRequestAdapter(Context context, List<ChangeRequest> requestList) {
        this.context = context;
        this.requestList = requestList;
    }

    @Override
    public ChangeRequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new ChangeRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChangeRequestViewHolder holder, int position) {
        ChangeRequest request = requestList.get(position);

        holder.studentName.setText("Full Name: " + request.getFullName());
        holder.address.setText("Address: " + request.getAddress());
        holder.courseName.setText("Course: " + request.getCourseName());
        holder.dob.setText("DOB: " + request.getDob());
        holder.gender.setText("Gender: " + request.getGender());
        holder.parentName.setText("Parent Name: " + request.getParentName());
        holder.phoneNumber.setText("Phone: " + request.getPhoneNumber());
        holder.semester.setText("Semester: " + request.getSemester());
        holder.status.setText("Status: " + request.getStatus());

        holder.approveButton.setOnClickListener(v -> {
            if (context instanceof ApprovalActivity) {
                ((ApprovalActivity) context).approveRequest(request);
            }
        });

        holder.rejectButton.setOnClickListener(v -> {
            if (context instanceof ApprovalActivity) {
                ((ApprovalActivity) context).rejectRequest(request);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ChangeRequestViewHolder extends RecyclerView.ViewHolder {
        TextView studentName, address, courseName, dob, gender, parentName, phoneNumber, semester, status;
        Button approveButton, rejectButton;

        public ChangeRequestViewHolder(View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.studentName);
            address = itemView.findViewById(R.id.address);
            courseName = itemView.findViewById(R.id.courseName);
            dob = itemView.findViewById(R.id.dob);
            gender = itemView.findViewById(R.id.gender);
            parentName = itemView.findViewById(R.id.parentName);
            phoneNumber = itemView.findViewById(R.id.phoneNumber);
            semester = itemView.findViewById(R.id.semester);
            status = itemView.findViewById(R.id.status);
            approveButton = itemView.findViewById(R.id.approveButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }
}
