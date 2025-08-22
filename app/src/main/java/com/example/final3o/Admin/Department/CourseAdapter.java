package com.example.final3o.Admin.Department;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final3o.R;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<CourseModel> courseList;

    public CourseAdapter(List<CourseModel> courseList) {
        this.courseList = courseList;
    }

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseViewHolder holder, int position) {
        CourseModel course = courseList.get(position);
        holder.courseName.setText(course.getCourseName());
        holder.courseId.setText(course.getCourseId());
        holder.semester.setText("Semester: " + course.getSemester());
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public void updateCourseList(List<CourseModel> newCourseList) {
        this.courseList = newCourseList;
        notifyDataSetChanged();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView courseName, courseId, semester;

        public CourseViewHolder(View itemView) {
            super(itemView);
            courseName = itemView.findViewById(R.id.courseName);
            courseId = itemView.findViewById(R.id.courseId);
            semester = itemView.findViewById(R.id.semester);
        }
    }
}
