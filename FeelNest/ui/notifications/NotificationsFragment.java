package com.example.FeelNest.ui.notifications;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.FeelNest.R;
import com.example.FeelNest.databinding.FragmentNotificationsBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    // Sleep cycle duration in minutes (90 minutes per cycle)
    private static final int SLEEP_CYCLE_DURATION = 90;

    // Time to fall asleep in minutes
    private static final int FALL_ASLEEP_TIME = 15;

    // Number of sleep cycles to calculate (4-6 cycles = 6-9 hours)
    private static final int[] SLEEP_CYCLES = {6, 5, 4, 3, 2, 1};
    private int wakeUpHour,wakeUpMin;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.timePicker.setIs24HourView(false);
        binding.calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateSleepTimes();
                binding.inputLayout.setVisibility(View.GONE);
                binding.resultLayout.setVisibility(View.VISIBLE);
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.resultLayout.getVisibility() == View.VISIBLE) {
                    binding.inputLayout.setVisibility(View.VISIBLE);
                    binding.resultLayout.setVisibility(View.GONE);
                    //Toast.makeText(getContext(), "back click", Toast.LENGTH_SHORT).show();
                }


            }
        });

        binding.learnMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://my.clevelandclinic.org/health/body/12148-sleep-basics"));
                startActivity(intent);
            }
        });

        binding.goToAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the optimal bedtime (first one in the list - bedtime1)
                String optimalBedtime = binding.bedtime1.getText().toString();

                // Launch alarm app with this value
                launchAlarmWithBedtime(optimalBedtime);
            }
        });
        return root;
    }

    //Lunch Alarm clock
    private void launchAlarmWithBedtime(String bedtime) {
        try {
            // Parse the bedtime string to get hours and minutes
            SimpleDateFormat inputFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(inputFormat.parse(bedtime));

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Create intent to open the clock app at the alarm tab
            Intent intent = new Intent(android.provider.AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_HOUR, wakeUpHour);
            intent.putExtra(AlarmClock.EXTRA_MINUTES, wakeUpMin);
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, "Optimal Bedtime");

            if (wakeUpHour <= 24 && wakeUpMin <= 60) {
                 startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error setting alarm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



    private void calculateSleepTimes() {
        // Get wake up time from time picker
        int hour, minute;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hour = binding.timePicker.getHour();
            minute = binding.timePicker.getMinute();
        } else {
            hour = binding.timePicker.getCurrentHour();
            minute = binding.timePicker.getCurrentMinute();
        }

        // Set wake up time in result view
        String formattedWakeTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        binding.wakeUpTimeText.setText(formattedWakeTime);

        //set value to pass to intent
        wakeUpHour = hour;
        wakeUpMin = minute;

        // Calculate and display bedtimes
        List<String> bedtimes = calculateBedtimes(hour, minute);
        displayBedtimes(bedtimes);

    }

    private void displayBedtimes(List<String> bedtimes) {
        TextView[] bedtimeViews = new TextView[6];
        bedtimeViews[0] = binding.bedtime1;
        bedtimeViews[1] = binding.bedtime2;
        bedtimeViews[2] = binding.bedtime3;
        bedtimeViews[3] = binding.bedtime4;
        bedtimeViews[4] = binding.bedtime5;
        bedtimeViews[5] = binding.bedtime6;
        // Display calculated bedtimes
        for (int i = 0; i < bedtimes.size() && i < bedtimeViews.length; i++) {
            bedtimeViews[i].setText(bedtimes.get(i));

            // Highlight the optimal sleep time (5-6 cycles)
            if (i <= 1) {
                bedtimeViews[i].setTextColor(getResources().getColor(R.color.colorOptimal));
            } else {
                bedtimeViews[i].setTextColor(getResources().getColor(R.color.mainFont));
            }
        }
    }

    private List<String> calculateBedtimes(int wakeHour, int wakeMinute) {

        List<String> bedtimes = new ArrayList<>();

        // Create calendar instance for wake up time
        Calendar wakeUpTime = Calendar.getInstance();
        wakeUpTime.set(Calendar.HOUR_OF_DAY, wakeHour);
        wakeUpTime.set(Calendar.MINUTE, wakeMinute);
        wakeUpTime.set(Calendar.SECOND, 0);

        // If wake time is in the past, set it to tomorrow
        Calendar now = Calendar.getInstance();
        if (wakeUpTime.before(now)) {
            wakeUpTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Calculate bedtimes for different sleep cycles
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());

        for (int cycles : SLEEP_CYCLES) {
            Calendar bedtime = (Calendar) wakeUpTime.clone();

            // Subtract sleep cycles plus time to fall asleep
            int totalMinutes = (cycles * SLEEP_CYCLE_DURATION) + FALL_ASLEEP_TIME;
            bedtime.add(Calendar.MINUTE, -totalMinutes);

            bedtimes.add(sdf.format(bedtime.getTime()));
        }

        return bedtimes;
    }
}