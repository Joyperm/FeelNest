package com.example.FeelNest.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.FeelNest.AddNewItem;
import com.example.FeelNest.Adapter.CustomMoodAdapter;
import com.example.FeelNest.Adapter.MainAdapter;
import com.example.FeelNest.Model.MoodModel;
import com.example.FeelNest.R;
import com.example.FeelNest.databinding.FragmentHomeBinding;
import com.example.FeelNest.databinding.UpdatePopupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    RecyclerView recyclerView;
    MainAdapter mainAdapter;
    DatabaseReference databaseRef;

    private CustomMoodAdapter customAdapter;
    private List<MoodModelWithKey> moodList = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up RecyclerView
        recyclerView = binding.rv;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Initialize adapter with empty list
        customAdapter = new CustomMoodAdapter(new ArrayList<>(), getContext());
        recyclerView.setAdapter(customAdapter);



        // Set up Firebase reference
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("FeelNest")
                .child(mAuth.getCurrentUser().getUid());

        // Load data and sort by createAt (newest first)
        loadData();


        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //databaseRef = FirebaseDatabase.getInstance().getReference("FeelNest").child(uid);


        // Set the query to order by `createAt`
        //Query query = databaseRef.orderByChild("createAt");


//        FirebaseRecyclerOptions<MoodModel> options =
//                new FirebaseRecyclerOptions.Builder<MoodModel>()
//                        .setQuery(query, MoodModel.class)
//                        .build();



        //mainAdapter = new MainAdapter(options);
        //recyclerView.setAdapter(mainAdapter);


        //floating btn
        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open activity_add
                startActivity(new Intent(getContext(), AddNewItem.class));
            }
        });
        return root;
    }

    private void loadData() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                moodList.clear();

                // Collect all mood models with their keys
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MoodModel model = snapshot.getValue(MoodModel.class);
                    String key = snapshot.getKey();
                    if (model != null) {
                        moodList.add(new MoodModelWithKey(model, key));
                    }
                }

                // Sort by createAt in descending order (newest first)
                Collections.sort(moodList, (item1, item2) ->
                        Long.compare(item2.getModel().getCreateAt(), item1.getModel().getCreateAt()));

                // Extract just the models for the adapter
                List<MoodModel> sortedModels = new ArrayList<>();
                for (MoodModelWithKey item : moodList) {
                    sortedModels.add(item.getModel());
                }

                // Update the adapter with sorted data
                customAdapter.updateData(sortedModels);

                // Set click listener for dialog
                customAdapter.setOnItemClickListener((model, position) ->
                        openDialog(getContext(), model, position));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "Database error: " + error.getMessage());
            }
        });
    }

    // Helper class to store model with its Firebase key
    private static class MoodModelWithKey {
        private MoodModel model;
        private String key;

        public MoodModelWithKey(MoodModel model, String key) {
            this.model = model;
            this.key = key;
        }

        public MoodModel getModel() {
            return model;
        }

        public String getKey() {
            return key;
        }
    }

    // Get Firebase key for a position
    private String getKeyAtPosition(int position) {
        if (position >= 0 && position < moodList.size()) {
            return moodList.get(position).getKey();
        }
        return null;
    }

    private void openDialog(Context context, MoodModel model, int position) {
        DialogPlus dialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(new ViewHolder(R.layout.update_popup))
                .setExpanded(true, 1200)
                .create();

        // Use ViewBinding for DialogPlus
        UpdatePopupBinding dialogBinding = UpdatePopupBinding.bind(dialogPlus.getHolderView());

        // Set values in the dialog fields
        dialogBinding.txtDate.setText(model.getDate());
        dialogBinding.txtNote.setText(model.getNote());

        // Update button click handler
        dialogBinding.btnUpdate.setOnClickListener(v -> {
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

            // Update in database
            map.put("Turl", imgUrl);
            map.put("createAt", model.getCreateAt());
            map.put("date", dialogBinding.txtDate.getText().toString());
            map.put("mood", dialogBinding.moodSpinner.getSelectedItem().toString().toLowerCase());
            map.put("note", dialogBinding.txtNote.getText().toString());

            // Get the key for this position
            String key = getKeyAtPosition(position);
            if (key != null) {
                databaseRef.child(key).updateChildren(map)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context,
                                    "Update Successfully",
                                    Toast.LENGTH_SHORT).show();
                            dialogPlus.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context,
                                    "Error While Updating..",
                                    Toast.LENGTH_SHORT).show();
                            dialogPlus.dismiss();
                        });
            }
        });

        // Delete button click handler
        dialogBinding.btnDel.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Are you sure to delete?");
            builder.setMessage("Delete can't be undo!");

            builder.setPositiveButton("Delete", (dialog, which) -> {
                String key = getKeyAtPosition(position);
                if (key != null) {
                    databaseRef.child(key).removeValue()
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(context, "Data Deleted!", Toast.LENGTH_SHORT).show());
                }
                dialogPlus.dismiss();
            });

            builder.setNegativeButton("Cancel", (dialog, which) ->
                    Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show());

            builder.show();
        });

        dialogPlus.show();
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        mainAdapter.startListening();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        //mainAdapter.stopListening();
//
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}