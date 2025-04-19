package com.example.FeelNest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.FeelNest.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_profile,
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_sleep,
                R.id.navigation_jokes)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController navController, @NonNull NavDestination navDestination, @Nullable Bundle bundle) {
                int id = navDestination.getId();

//                if(id == R.id.navigation_dashboard){
//                    Toast.makeText(MainActivity.this,"Dashboard Clicked",Toast.LENGTH_LONG).show();
//                }
            }
        });

        //allows the bottom navigation to work with the Navigation Component
        navView.setOnItemSelectedListener(item -> {
            navController.navigate(item.getItemId());
            return true;
        });


        //handle the navigation to the JokesFragment snack bar
        String navTarget = getIntent().getStringExtra("navigateTo");
        if ("jokes".equals(navTarget)) {
            Navigation.findNavController(this, R.id.nav_host_fragment_activity_main)
                    .navigate(R.id.navigation_jokes);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionmenu, menu);
        return true;

    }



    public void logout(MenuItem item) {

        FirebaseAuth.getInstance().signOut();
        // Redirect to LoginActivity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Finish MainActivity so the user cannot go back

    }

    public void GoSetting(MenuItem item) {
        // Redirect to SettingActivity
        Intent intent = new Intent(MainActivity.this, UserProfile.class);
        startActivity(intent);
        finish(); // Finish MainActivity so the user cannot go back
    }
}