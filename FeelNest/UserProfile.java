package com.example.FeelNest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.FeelNest.databinding.ActivityUserProfileBinding;

public class UserProfile extends AppCompatActivity {
    private ActivityUserProfileBinding binding;
    private String name;
    private int image,
            selectedAvatarId = -1,
            selectedAvatarResourceId = R.drawable.profile; // Default avatar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load avatar images (in a real app, you would load these from resources or a server)
        loadAvatarImages();

        // Set up click listeners for avatar selection
        setupAvatarClickListeners();

        // Set up save changes button
        binding.btnSaveChanges.setOnClickListener(v -> saveChanges());


    }

    private void saveChanges() {
        String name = binding.etName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedAvatarId == -1) {
            // No avatar selected, use the current one
            Toast.makeText(this, "Profile updated with current avatar", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Profile updated with avatar " + (selectedAvatarId + 1), Toast.LENGTH_SHORT).show();
        }

        // Call the method to save settings and go to main
        SaveSettingAndGoToMain(null);
    }

    private void setupAvatarClickListeners() {
        // Create an array of all avatar ImageViews for easier handling
        ImageView[] avatarViews = {
                binding.ivAvatar1,
                binding.ivAvatar2,
                binding.ivAvatar3,
                binding.ivAvatar4,
                binding.ivAvatar5,
                binding.ivAvatar6
        };

        // Set click listeners for each avatar
        for (int i = 0; i < avatarViews.length; i++) {
            final int avatarIndex = i;
            avatarViews[i].setOnClickListener(v -> selectAvatar(avatarIndex, avatarViews));
        }
    }

    private void selectAvatar(int index, ImageView[] avatarViews) {
        // Update the selected avatar
        selectedAvatarId = index;

        // Store the resource ID based on the selected index
        switch(index) {
            case 0:
                selectedAvatarResourceId = R.drawable.profile;
                break;
            case 1:
                selectedAvatarResourceId = R.drawable.profile2;
                break;
            case 2:
                selectedAvatarResourceId = R.drawable.logo_br;
                break;
            case 3:
                selectedAvatarResourceId = R.drawable.landscape1;
                break;
            case 4:
                selectedAvatarResourceId = R.drawable.landscape_background;
                break;
            case 5:
                selectedAvatarResourceId = R.drawable.logo;
                break;
        }

        // Update the current avatar preview
        binding.ivCurrentAvatar.setImageDrawable(avatarViews[index].getDrawable());

        // Highlight the selected avatar (you could add a border or other visual indicator)
        // For simplicity, we'll just show a toast
        //Toast.makeText(this, "Avatar " + (index + 1) + " selected", Toast.LENGTH_SHORT).show();
    }

    private void loadAvatarImages() {
        // Set the current avatar
        binding.ivCurrentAvatar.setImageResource(R.drawable.profile);

        // Set the avatar options
        binding.ivAvatar1.setImageResource(R.drawable.profile);
        binding.ivAvatar2.setImageResource(R.drawable.profile2);
        binding.ivAvatar3.setImageResource(R.drawable.logo_br);
        binding.ivAvatar4.setImageResource(R.drawable.landscape1);
        binding.ivAvatar5.setImageResource(R.drawable.landscape_background);
        binding.ivAvatar6.setImageResource(R.drawable.logo);

    }

    public void GoBack(View view) {
        Intent intent = new Intent(UserProfile.this, MainActivity.class);
        startActivity(intent);
    }

    public void SaveSettingAndGoToMain(View view) {
        // Get the name from the EditText
        name = binding.etName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SharedPreferences
        SharedPreferences settings = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("username", name);
        editor.putInt("profileImg", selectedAvatarResourceId);

        // Apply changes
        editor.apply();

        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();

        // Navigate back to MainActivity
        Intent intent = new Intent(UserProfile.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close this activity

    }
}