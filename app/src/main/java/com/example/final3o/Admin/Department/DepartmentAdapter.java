package com.example.final3o.Admin.Department;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final3o.R;

import java.util.List;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.DepartmentViewHolder> {

    private List<DepartmentModel> departmentList;
    private Context context;
    private OnDepartmentActionListener listener;

    public DepartmentAdapter(List<DepartmentModel> departmentList, Context context, OnDepartmentActionListener listener) {
        this.departmentList = departmentList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DepartmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_department, parent, false);
        return new DepartmentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DepartmentViewHolder holder, int position) {
        DepartmentModel department = departmentList.get(position);
        holder.departmentName.setText(department.getDepartmentName());
        holder.depId.setText("Dep ID: " + department.getDepid());
        holder.teacherName.setText("Assigned Teacher: " + department.getAssignedTeacher());

        holder.itemView.setOnClickListener(v -> {
            // Open the popup when a department is clicked
            listener.onDepartmentClick(department);
        });
    }

    @Override
    public int getItemCount() {
        return departmentList.size();
    }

    public static class DepartmentViewHolder extends RecyclerView.ViewHolder {
        TextView departmentName, depId, teacherName;

        public DepartmentViewHolder(View itemView) {
            super(itemView);
            departmentName = itemView.findViewById(R.id.departmentName);
            depId = itemView.findViewById(R.id.depId);
            teacherName = itemView.findViewById(R.id.teacherName);
        }
    }

    public interface OnDepartmentActionListener {
        void onDepartmentClick(DepartmentModel department);
    }
}
