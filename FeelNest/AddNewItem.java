package com.example.FeelNest;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.FeelNest.databinding.ActivityAddNewItemBinding;
import com.example.FeelNest.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddNewItem extends AppCompatActivity {
    private ActivityAddNewItemBinding binding;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    // New variables for mood selection
    private String currentMood = "happy", moodId; // Default mood
    private Button btnHappy, btnNeutral, btnSad;
    private RelativeLayout mainLayout;
    private TextView currentMoodLabel;
    private ImageView moodFaceImage;
    DatabaseReference databaseRef;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddNewItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Make app bar transparent
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        }

        // Initialize new UI elements
        btnHappy = binding.btnHappy;
        btnNeutral = binding.btnNeutral;
        btnSad = binding.btnSad;
        mainLayout = binding.main;
        currentMoodLabel = binding.currentMoodLabel;
        moodFaceImage = binding.moodFaceImage;
        //Intent intent = getIntent();
        //uid = intent.getStringExtra("mAuth");//
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("FeelNest").child(uid);
        // Create a new unique mood ID
        moodId = databaseRef.push().getKey();

        // Set up mood button click listeners
        btnHappy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMood("happy", Color.parseColor("#FFEB3B"), R.drawable.happy_face);
            }
        });

        btnNeutral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMood("neutral", Color.parseColor("#80DEEA"), R.drawable.neutral_face);
            }
        });

        btnSad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMood("sad", Color.parseColor("#FF9966"), R.drawable.sad_face);
            }
        });

        // Set default mood
        setMood("happy", Color.parseColor("#FFEB3B"), R.drawable.happy_face);

        binding.textViewDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(AddNewItem.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = year + "-" + month + "-" + dayOfMonth;
                binding.textViewDate.setText(date);
            }
        };

        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IsSad();

            }
        });
    }

    private void setMood(String mood, int backgroundColor, int faceDrawable) {
        // Update current mood
        currentMood = mood;

        // Update background color
        mainLayout.setBackgroundColor(backgroundColor);

        // Update mood text - capitalize first letter for display
        String displayMood = mood.substring(0, 1).toUpperCase() + mood.substring(1);
        currentMoodLabel.setText(displayMood);

        // Update face image
        moodFaceImage.setImageResource(faceDrawable);

        // Update button styles
        btnHappy.setBackground(getResources().getDrawable(
                mood.equals("happy") ? R.drawable.rounded_button_selected : R.drawable.rounded_button));

        btnNeutral.setBackground(getResources().getDrawable(
                mood.equals("neutral") ? R.drawable.rounded_button_selected : R.drawable.rounded_button));

        btnSad.setBackground(getResources().getDrawable(
                mood.equals("sad") ? R.drawable.rounded_button_selected : R.drawable.rounded_button));
    }

    private void clearData() {
        binding.txtNote.setText("");
        binding.textViewDate.setText("");
        // Keep the current mood selection
    }

    private void IsSad() {
        if (currentMood.equals("sad")) {
            // Create an AlertDialog...
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Feeling Sad?");
            builder.setMessage("Would you like to check out some jokes to boost your mood?");

            // Add the buttons
            builder.setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface dialog, int id) {
                    //Toast.makeText(AddNewItem.this, "Go to jokes", Toast.LENGTH_SHORT).show();
                    // Navigate to jokes screen
                    Intent intent = new Intent(AddNewItem.this, MainActivity.class);
                    intent.putExtra("navigateTo", "jokes");
                    startActivity(intent);
                    finish();
                }
            });

            builder.setNegativeButton("No", new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface dialog, int id) {
                    // User clicked No, just save the data
                    insertData();
                    clearData();
                    dialog.dismiss();
                    //open activity_add
                    finish();
                }
            });

            // Create and show the AlertDialog
            androidx.appcompat.app.AlertDialog dialog = builder.create();
            dialog.show();

            // Optional: Customize dialog button colors
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FF9966"));
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
        } else {
            // For happy and neutral moods, just save directly
            insertData();
            clearData();
            //open activity_add
            finish();
        }
    }

    private void insertData() {
        Map<String, Object> map = new HashMap<>();
        String imgUrl;

        if(currentMood.equals("neutral")){
            imgUrl = "https://icons.iconarchive.com/icons/microsoft/fluentui-emoji-flat/256/Neutral-Face-Flat-icon.png";
        } else if (currentMood.equals("sad")) {
            imgUrl = "https://cdn3.iconfinder.com/data/icons/emoticon-emoji-1/50/Blue-512.png";
        } else {
            imgUrl = "https://cdn1.iconfinder.com/data/icons/smileys-emoticons-green-filled-with-medical-mask-i/96/SMILEY_SMILING_filled_green-512.png";
        }

        // Same in database
        map.put("Turl", imgUrl);
        map.put("createAt", System.currentTimeMillis());
        map.put("date", binding.textViewDate.getText().toString());
        map.put("mood", currentMood);
        map.put("note", binding.txtNote.getText().toString());


        databaseRef.push()
                .setValue(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(AddNewItem.this, "Data Insert Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddNewItem.this, "Error While Inserting..", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void GoBack(View view) {
        finish();
    }
}