package com.example.FeelNest.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.FeelNest.Model.MoodModel;
import com.example.FeelNest.R;
import com.example.FeelNest.databinding.MoodItemBinding;
import com.example.FeelNest.databinding.UpdatePopupBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainAdapter extends FirebaseRecyclerAdapter<MoodModel, MainAdapter.myViewHolder> {


    Context context;
    MoodModel model;
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("FeelNest")
            .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // ist to store all mood models
    private List<MoodModel> moodModelList = new ArrayList<>();

    public MainAdapter(@NonNull FirebaseRecyclerOptions<MoodModel> options, List<MoodModel> moodModelList) {
        super(options);
        this.moodModelList = moodModelList;
    }

    public MainAdapter(@NonNull FirebaseRecyclerOptions<MoodModel> options) {
        super(options);
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_item,parent,false);
        MoodItemBinding binding = MoodItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        myViewHolder myViewHolder = new myViewHolder(binding.getRoot(), binding);
        return myViewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull MoodModel model) {
        this.model = model;

        // Add the model to our list if it's not already there
        if (!moodModelList.contains(model)) {
            moodModelList.add(model);
        }

        // Convert stored string date to readable format
        String formattedDate = formatDate(model.getDate());
        holder.binding.dateTextView.setText(formattedDate);

        Glide.with(holder.binding.moodImageView.getContext()).load(model.getTurl())
                .placeholder(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_light)
                .circleCrop()
                .error(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark_normal)
                .into(holder.binding.moodImageView);

        holder.bindData(model, position);


    }

    // Method to get the list of MoodModel objects
    public List<MoodModel> getMoodModelList() {
        return new ArrayList<>(moodModelList);
    }

    // Method to clear the list
    public void clearMoodModelList() {
        moodModelList.clear();
    }

    // Override cleanup to clear the list when adapter stops listening
    @Override
    public void onDataChanged() {
        super.onDataChanged();
        // This method is called each time the data changes
        // You can add additional logic here if needed
    }

    @Override
    public void stopListening() {
        super.stopListening();
        // Clear the list when we stop listening to avoid memory leaks
        clearMoodModelList();
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


    class myViewHolder extends RecyclerView.ViewHolder {

        MoodItemBinding binding;
        //MoodModel model;

        public myViewHolder(@NonNull View itemView, MoodItemBinding binding) {
            super(itemView);
            this.binding = binding;

        }

        // Method to bind data inside ViewHolder
        public void bindData(MoodModel model, int position) {
            String mood = model.getMood();
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(v.getContext(), "Mood: " + mood, Toast.LENGTH_SHORT).show();
                    openDialog(v.getContext(), model, position);
                }
            });
        }

        private void openDialog(Context context, MoodModel model, int position) {
            DialogPlus dialogPlus = DialogPlus.newDialog(context)
                    .setContentHolder(new ViewHolder(R.layout.update_popup)) // Ensure this layout exists
                    .setExpanded(true, 1200)
                    .create();

            // Use ViewBinding for DialogPlus
            UpdatePopupBinding dialogBinding = UpdatePopupBinding.bind(dialogPlus.getHolderView());

            // Set values in the dialog fields

            dialogBinding.txtDate.setText(model.getDate());
            dialogBinding.txtNote.setText(model.getNote());

            // Update Firebase or Database logic
            //create action for update button
            dialogBinding.btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Map<String, Object> map = new HashMap<>();
                    String moodChoice = dialogBinding.moodSpinner.getSelectedItem().toString().toLowerCase();
                    String imgUrl;
                    if (moodChoice.equals("neutral")) {
                        imgUrl = "https://icons.iconarchive.com/icons/microsoft/fluentui-emoji-flat/256/Neutral-Face-Flat-icon.png";
                    } else if (moodChoice.equals("sad")) {
                        imgUrl = "https://cdn3.iconfinder.com/data/icons/emoticon-emoji-1/50/Blue-512.png";
                    } else {
                        imgUrl = "https://cdn1.iconfinder.com/data/icons/smileys-emoticons-green-filled-with-medical-mask-i/96/SMILEY_SMILING_filled_green-512.png";
                    }

                    //same in database
                    map.put("Turl", imgUrl);
                    map.put("createAt", System.currentTimeMillis());
                    map.put("date", dialogBinding.txtDate.getText().toString());
                    map.put("mood", dialogBinding.moodSpinner.getSelectedItem().toString().toLowerCase());
                    map.put("note", dialogBinding.txtNote.getText().toString());

                    databaseRef
                            .child(getRef(position).getKey()).updateChildren(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(context,
                                            "Update Successfully",
                                            Toast.LENGTH_SHORT).show();
                                    //close dialog
                                    dialogPlus.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context,
                                            "Error While Updating..",
                                            Toast.LENGTH_SHORT).show();
                                    //close dialog
                                    dialogPlus.dismiss();
                                }
                            });
                    dialogPlus.dismiss();
                }

            });

            dialogBinding.btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Are you sure to delete?");
                    builder.setMessage("Delete can't be undo!");

                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            databaseRef
                                    .child(getRef(position).getKey()).removeValue();
//                            FirebaseDatabase.getInstance().getReference().child("FeelNest")
//                                    .child(getRef(position).getKey()).removeValue();

                            Toast.makeText(context, "Data Deleted!", Toast.LENGTH_SHORT).show();
                            dialogPlus.dismiss();

                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.show();

                }
            });

            dialogPlus.show();

        }
    }
}
