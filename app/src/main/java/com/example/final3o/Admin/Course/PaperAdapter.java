package com.example.final3o.Admin.Course;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.final3o.R;

import java.util.List;

public class PaperAdapter extends RecyclerView.Adapter<PaperAdapter.PaperViewHolder> {

    private List<Paper> paperList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public PaperAdapter(List<Paper> paperList) {
        this.paperList = paperList;
    }

    // Interface for handling item clicks
    public interface OnItemClickListener {
        void onItemClick(Paper paper);
    }

    // Set the listener to handle clicks
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public PaperViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_paper, parent, false);
        return new PaperViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PaperViewHolder holder, int position) {
        Paper paper = paperList.get(position);

        holder.paperNameTextView.setText(paper.getPaperName());
        holder.paperCodeTextView.setText("Paper Code: " +paper.getPaperCode());
        holder.semesterTextView.setText("Semester: "+paper.getSemester());
        holder.teacherTextView.setText("Teacher: "+paper.getTeacherId());

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(paper); // Pass the selected paper to the activity
            }
        });
    }

    @Override
    public int getItemCount() {
        return paperList.size();
    }

    // ViewHolder for the RecyclerView items
    public static class PaperViewHolder extends RecyclerView.ViewHolder {

        TextView paperNameTextView, paperCodeTextView, semesterTextView, teacherTextView;

        public PaperViewHolder(View itemView) {
            super(itemView);

            paperNameTextView = itemView.findViewById(R.id.textViewPaperName);
            paperCodeTextView = itemView.findViewById(R.id.textViewPaperCode);
            semesterTextView = itemView.findViewById(R.id.textViewSemester);
            teacherTextView = itemView.findViewById(R.id.textViewTeacher);
        }
    }
}



