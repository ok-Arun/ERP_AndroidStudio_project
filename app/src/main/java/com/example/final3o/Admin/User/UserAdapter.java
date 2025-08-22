package com.example.final3o.Admin.User;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final3o.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private ManageUsersActivity context;

    public UserAdapter(List<User> userList, ManageUsersActivity context) {
        this.userList = userList;
        this.context = context;
    }

    public void updateList(List<User> updatedList) {
        this.userList = updatedList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        // Determine user name based on role
        String userName = "";
        if ("student".equalsIgnoreCase(user.getRole())) {
            userName = user.getFullName(); // students use fullName
        } else if ("teacher".equalsIgnoreCase(user.getRole())) {
            userName = user.getName(); // teachers use fullName (or getName if you use that)
        } else if ("admin".equalsIgnoreCase(user.getRole())) {
            userName = user.getName(); // admins use fullName (or getName if you use that)
        } else {
            userName = user.getFullName(); // fallback
        }

        holder.nameText.setText(userName);
        holder.emailText.setText(user.getEmail());
        holder.roleText.setText("Role: " + user.getRole());

        String idDisplay = "";
        if ("student".equalsIgnoreCase(user.getRole())) {
            idDisplay = "Student ID: " + user.getStudentId();
        } else if ("teacher".equalsIgnoreCase(user.getRole())) {
            idDisplay = "Teacher ID: " + user.getTeacherId();
        } else if ("admin".equalsIgnoreCase(user.getRole())) {
            idDisplay = "Admin ID: " + user.getAdminId();
        }
        holder.idText.setText(idDisplay);

        // Show course only for students
        if ("student".equalsIgnoreCase(user.getRole()) && user.getCourse() != null && !user.getCourse().isEmpty()) {
            holder.courseText.setText("Course: " + user.getCourse());
            holder.courseText.setVisibility(View.VISIBLE);
        } else {
            holder.courseText.setVisibility(View.GONE);
        }

        // Show user status
        String status = user.getUserStatus();  // adjust if your User class uses getStatus()
        if (status == null || status.isEmpty()) {
            status = "Unknown";
        }
        holder.statusText.setText("userStatus: " + status);

        // Handle item click to show options dialog
        holder.itemView.setOnClickListener(v -> context.showOptionsDialog(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText, roleText, idText, courseText, statusText;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            emailText = itemView.findViewById(R.id.emailText);
            roleText = itemView.findViewById(R.id.roleText);
            idText = itemView.findViewById(R.id.idText);
            courseText = itemView.findViewById(R.id.courseText);
            statusText = itemView.findViewById(R.id.statusText);
        }
    }
}
