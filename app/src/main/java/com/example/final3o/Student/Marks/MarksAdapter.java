package com.example.final3o.Student.Marks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final3o.R;

import java.util.ArrayList;

public class MarksAdapter extends RecyclerView.Adapter<MarksAdapter.MarksViewHolder> {

    ArrayList<MarksModel> marksList;

    public MarksAdapter(ArrayList<MarksModel> marksList) {
        this.marksList = marksList;
    }

    @NonNull
    @Override
    public MarksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_marks, parent, false);
        return new MarksViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MarksViewHolder holder, int position) {
        MarksModel model = marksList.get(position);
        holder.tvPaperName.setText(model.getPaper());
        holder.tvMarks.setText("Marks: " + model.getMarks());
    }

    @Override
    public int getItemCount() {
        return marksList.size();
    }

    public static class MarksViewHolder extends RecyclerView.ViewHolder {
        TextView tvPaperName, tvMarks, tvTime;

        public MarksViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPaperName = itemView.findViewById(R.id.tv_paper_name);
            tvMarks = itemView.findViewById(R.id.tv_marks);
        }
    }
}
