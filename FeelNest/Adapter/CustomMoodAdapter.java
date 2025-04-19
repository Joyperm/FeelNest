package com.example.FeelNest.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.FeelNest.Model.MoodModel;
import com.example.FeelNest.databinding.MoodItemBinding;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomMoodAdapter extends RecyclerView.Adapter<CustomMoodAdapter.MoodViewHolder> {

    private List<MoodModel> moodList;
    private Context context;
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(MoodModel model, int position);
    }

    public CustomMoodAdapter(List<MoodModel> moodList, Context context) {
        this.moodList = moodList;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MoodItemBinding binding = MoodItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MoodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        MoodModel model = moodList.get(position);

        // Convert stored string date to readable format
        String formattedDate = formatDate(model.getDate());
        holder.binding.dateTextView.setText(formattedDate);

        Glide.with(holder.binding.moodImageView.getContext()).load(model.getTurl())
                .placeholder(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_light)
                .circleCrop()
                .error(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark_normal)
                .into(holder.binding.moodImageView);

        // Set click listener
        final int pos = position;
        holder.binding.getRoot().setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(model, pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return moodList != null ? moodList.size() : 0;
    }

    // Update data and notify adapter
    public void updateData(List<MoodModel> newList) {
        this.moodList = newList;
        notifyDataSetChanged();
    }

    private String formatDate(String dateString) {
        if (dateString == null) return "Unknown Date";

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        try {
            Date date = inputFormat.parse(String.valueOf(dateString));
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Invalid Date";
        }
    }

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        MoodItemBinding binding;

        public MoodViewHolder(MoodItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}