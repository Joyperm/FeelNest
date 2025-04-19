package com.example.FeelNest.ui.dashboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.FeelNest.Model.MoodModel;
import com.example.FeelNest.R;
import com.example.FeelNest.databinding.FragmentDashboardBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private List<String> moodList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private PieChart pieChart;
    private BarChart barChart;
    private Spinner monthSpinner;
    private TextView barChartTitle;
    private Map<String, Map<String, Integer>> monthlyMoodCounts = new HashMap<>();
    private List<String> availableMonths = new ArrayList<>();
    private String selectedMonth = "All"; // Default selection

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        pieChart = binding.pieChart;
        barChart = binding.barChart;
        monthSpinner = binding.monthSpinner;
        barChartTitle = binding.barChartTitle;

        // Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("FeelNest")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Initialize spinner with a loading placeholder
        List<String> initialList = new ArrayList<>();
        initialList.add("Loading months...");
        ArrayAdapter<String> initialAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, initialList);
        initialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(initialAdapter);

        fetchMoodData();
        fetchMoodByMonth();

        return root;
    }

    private void setupMonthSpinner() {
        // Sort months chronologically
        Collections.sort(availableMonths, new Comparator<String>() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM", Locale.US);

            @Override
            public int compare(String month1, String month2) {
                if (month1.equals("All")) return -1;
                if (month2.equals("All")) return 1;

                try {
                    Date date1 = format.parse(month1);
                    Date date2 = format.parse(month2);
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    return month1.compareTo(month2);
                }
            }
        });

        // Add "All" option at the beginning
        availableMonths.add(0, "All");

        // Format month names for display (e.g., "2023-05" to "May 2023")
        List<String> displayMonths = new ArrayList<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM", Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);

        for (String month : availableMonths) {
            if (month.equals("All")) {
                displayMonths.add("All Months");
            } else {
                try {
                    Date date = inputFormat.parse(month);
                    displayMonths.add(outputFormat.format(date));
                } catch (ParseException e) {
                    displayMonths.add(month); // Fallback to original format
                }
            }
        }

        // Create adapter with formatted month names
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, displayMonths) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                // Apply custom font to spinner items
                Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.jua);
                textView.setTypeface(typeface);
                textView.setTextColor(Color.parseColor("#4F3422"));

                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;

                // Apply custom font to dropdown items
                Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.jua);
                textView.setTypeface(typeface);
                textView.setTextColor(Color.parseColor("#4F3422"));
                textView.setPadding(20, 15, 20, 15);

                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);

        // Set listener for month selection
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Convert display name back to data format
                String displayMonth = displayMonths.get(position);

                if (displayMonth.equals("All Months")) {
                    selectedMonth = "All";
                    Log.d("MonthSpinner", "Selected: All Months");
                } else {
                    try {
                        Date date = outputFormat.parse(displayMonth);
                        selectedMonth = inputFormat.format(date)+"-";
                        Log.d("MonthSpinner", "Selected month: " + selectedMonth);
                    } catch (ParseException e) {
                        selectedMonth = availableMonths.get(position);
                        Log.d("MonthSpinner", "Selected month (fallback): " + selectedMonth);
                    }
                }

                // Update only the bar chart based on selection
                updateBarChartForSelectedMonth();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void updateBarChartForSelectedMonth() {
        Log.d("BarChart", "Updating bar chart for month: " + selectedMonth);

        if (selectedMonth.equals("All")) {
            // Show all data for bar chart
            Log.d("BarChart", "Showing all months. Total months: " + monthlyMoodCounts.size());
            showBarChart(monthlyMoodCounts);
            barChartTitle.setText("Monthly Mood Analysis - All Months");
        } else {
            // Filter data for selected month - only for bar chart
            Log.d("BarChart", "Filtering for month: " + selectedMonth);
            Log.d("BarChart", "Available months: " + monthlyMoodCounts.keySet());

            // Make sure the selected month is in the normalized format
            String normalizedSelectedMonth = selectedMonth;
            if (selectedMonth.length() >= 7) {
                String[] parts = selectedMonth.split("-");
                if (parts.length >= 2) {
                    String year = parts[0];
                    String month = parts[1];
                    normalizedSelectedMonth = year + "-" + (month.length() == 1 ? "0" + month : month);
                }
            }

            Log.d("BarChart", "Normalized selected month: " + normalizedSelectedMonth);

            if (monthlyMoodCounts.containsKey(normalizedSelectedMonth)) {
                Map<String, Map<String, Integer>> filteredMonthlyMoodCounts = new HashMap<>();
                Map<String, Integer> monthData = monthlyMoodCounts.get(normalizedSelectedMonth);

                Log.d("BarChart", "Found data for month: " + normalizedSelectedMonth + ", moods: " + monthData);
                filteredMonthlyMoodCounts.put(normalizedSelectedMonth, monthData);

                // Update bar chart with filtered data
                showBarChart(filteredMonthlyMoodCounts);
                barChartTitle.setText("Monthly Mood Analysis - " + formatMonthForDisplay(normalizedSelectedMonth));
            } else {
                // Handle case where no data exists for the selected month
                Log.d("BarChart", "No data found for month: " + normalizedSelectedMonth);
                barChart.clear();
                barChart.setNoDataText("No data for " + formatMonthForDisplay(normalizedSelectedMonth));
                barChart.setNoDataTextColor(Color.parseColor("#4F3422"));
                barChart.setNoDataTextTypeface(ResourcesCompat.getFont(getContext(), R.font.jua));
                barChart.invalidate();
                barChartTitle.setText("No data for " + formatMonthForDisplay(normalizedSelectedMonth));
            }
        }
    }

    private void fetchMoodData() {
        // Listen for real-time changes in Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                moodList.clear();
                // Fetch the mood data from Firebase and store it in the moodList
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MoodModel moodModel = dataSnapshot.getValue(MoodModel.class);
                    if (moodModel != null) {
                        moodList.add(moodModel.getMood()); // Store moods in the list
                    }
                }
                // After fetching new mood data, update the pie chart with ALL data
                showPieChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching data", error.toException());
            }
        });
    }

    private void showPieChart() {
        List<PieEntry> entries = new ArrayList<>();

        // Count occurrences of each mood
        Map<String, Integer> moodCount = new HashMap<>();
        for (String mood : moodList) {
            moodCount.put(mood, moodCount.getOrDefault(mood, 0) + 1);
        }

        // Convert the counted data to PieEntries
        for (Map.Entry<String, Integer> entry : moodCount.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Load custom font
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.jua);
        dataSet.setValueTypeface(typeface); // Set font for slice value labels

        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTypeface(typeface);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setData(pieData); // Set new data for the pie chart

        // Customize chart appearance
        // Enable Entry Labels
        pieChart.setDrawEntryLabels(true);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTypeface(typeface);

        // Set center text - always "Your Mood" since pie chart shows all data
        pieChart.setCenterText("Your Overall Mood");
        pieChart.setCenterTextSize(15f);
        pieChart.setUsePercentValues(true);
        pieChart.setCenterTextColor(Color.parseColor("#4F3422"));
        pieChart.setCenterTextTypeface(typeface); // Set custom font for center text
        pieChart.invalidate(); // Redraw the chart with updated data
        pieChart.getDescription().setEnabled(false);

        // Customize Legend
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);  // Enable legend
        legend.setTextSize(18f);  // Set legend text size
        legend.setTextColor(Color.parseColor("#4F3422"));
        legend.setTypeface(typeface); // Set custom font for legend

        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);

        // Set space between legend items
        legend.setXEntrySpace(100f);
    }

    private String formatMonthForDisplay(String monthKey) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
            Date date = inputFormat.parse(monthKey);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return monthKey;
        }
    }

    private void fetchMoodByMonth() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                monthlyMoodCounts.clear();
                availableMonths.clear();

                // Debug: Log the total number of entries
                Log.d("MoodData", "Total entries: " + snapshot.getChildrenCount());

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MoodModel moodModel = dataSnapshot.getValue(MoodModel.class);
                    if (moodModel != null) {
                        String date = moodModel.getDate();  // Get the date string
                        String mood = moodModel.getMood();

                        if (date != null && mood != null) {
                            // Debug: Log each mood entry
                            Log.d("MoodData", "Original date: " + date + ", Mood: " + mood);

                            // Extract the month in a format-agnostic way
                            String[] dateParts = date.split("-");
                            if (dateParts.length >= 2) {
                                // Normalize the month format to "yyyy-MM"
                                String year = dateParts[0];
                                String month = dateParts[1];

                                // Create a normalized month key (yyyy-MM)
                                String monthKey = year + "-" + (month.length() == 1 ? "0" + month : month);
                                Log.d("MoodData", "Normalized month key: " + monthKey);

                                // Add to available months list if not already there
                                if (!availableMonths.contains(monthKey)) {
                                    availableMonths.add(monthKey);
                                    Log.d("MoodData", "Added new month: " + monthKey);
                                }

                                // Initialize the month entry if not present
                                monthlyMoodCounts.putIfAbsent(monthKey, new HashMap<>());
                                Map<String, Integer> moodMap = monthlyMoodCounts.get(monthKey);

                                // Count occurrences of each mood
                                moodMap.put(mood, moodMap.getOrDefault(mood, 0) + 1);
                                Log.d("MoodData", "Updated count for " + mood + " in " + monthKey + ": " + moodMap.get(mood));
                            } else {
                                Log.e("MoodData", "Invalid date format: " + date);
                            }
                        }
                    }
                }

                // Debug: Log all available months and their mood counts
                Log.d("MoodData", "Available months: " + availableMonths.size());
                for (String month : availableMonths) {
                    Log.d("MoodData", "Month: " + month + " has moods: " + monthlyMoodCounts.get(month));
                }

                // Setup spinner after data is loaded
                setupMonthSpinner();

                // Show bar chart with all data initially
                showBarChart(monthlyMoodCounts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching data", error.toException());
            }
        });
    }

    private void showBarChart(Map<String, Map<String, Integer>> dataMap) {
        // Load custom font
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.jua);

        // Check if we have data to display
        if (dataMap.isEmpty()) {
            Log.d("BarChart", "No data to display");
            barChart.clear();
            barChart.setNoDataText("No mood data available");
            barChart.setNoDataTextColor(Color.parseColor("#4F3422"));
            barChart.setNoDataTextTypeface(typeface);
            barChart.invalidate();
            return;
        }

        List<BarEntry> happyEntries = new ArrayList<>();
        List<BarEntry> neutralEntries = new ArrayList<>();
        List<BarEntry> sadEntries = new ArrayList<>();
        List<String> months = new ArrayList<>();

        int index = 0;

        // Sort months chronologically for display
        List<String> sortedMonths = new ArrayList<>(dataMap.keySet());
        Log.d("BarChart", "Months to display: " + sortedMonths);

        Collections.sort(sortedMonths, new Comparator<String>() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM", Locale.US);

            @Override
            public int compare(String month1, String month2) {
                try {
                    Date date1 = format.parse(month1);
                    Date date2 = format.parse(month2);
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    return month1.compareTo(month2);
                }
            }
        });

        for (String month : sortedMonths) {
            Map<String, Integer> moods = dataMap.get(month);
            Log.d("BarChart", "Processing month: " + month + " with moods: " + moods);

            // Format month for display
            String displayMonth = formatMonthForDisplay(month);
            months.add(displayMonth);

            // Use the same x position for all three bars in a group
            float xPosition = index;

            // Get mood counts, defaulting to 0 if not present
            float happyCount = moods.getOrDefault("happy", 0);
            float neutralCount = moods.getOrDefault("neutral", 0);
            float sadCount = moods.getOrDefault("sad", 0);

            Log.d("BarChart", "Adding entries for " + month + ": happy=" + happyCount +
                    ", neutral=" + neutralCount + ", sad=" + sadCount);

            happyEntries.add(new BarEntry(xPosition, happyCount));
            neutralEntries.add(new BarEntry(xPosition, neutralCount));
            sadEntries.add(new BarEntry(xPosition, sadCount));

            index++;
        }

        // Create datasets only if we have entries
        if (!happyEntries.isEmpty()) {
            BarDataSet happyDataSet = new BarDataSet(happyEntries, "Happy");
            happyDataSet.setColor(Color.rgb(217, 80, 138));
            happyDataSet.setValueTypeface(typeface);
            happyDataSet.setValueTextSize(9f);

            happyDataSet.setValueTextColor(Color.BLACK);

            BarDataSet neutralDataSet = new BarDataSet(neutralEntries, "Neutral");
            neutralDataSet.setColor(Color.rgb(254, 247, 120));
            neutralDataSet.setValueTypeface(typeface);
            neutralDataSet.setValueTextSize(9f);
            neutralDataSet.setValueTextColor(Color.BLACK);

            BarDataSet sadDataSet = new BarDataSet(sadEntries, "Sad");
            sadDataSet.setColor(Color.rgb(254, 149, 7));
            sadDataSet.setValueTypeface(typeface);
            sadDataSet.setValueTextSize(9f);
            sadDataSet.setValueTextColor(Color.BLACK);

            BarData barData = new BarData(happyDataSet, neutralDataSet, sadDataSet);

            // Adjust these values to control bar width and spacing
            float groupSpace = 0.6f;  // Space between groups
            float barSpace = 0.03f;   // Space between bars in a group
            float barWidth = 0.1f;    // Width of each bar

            barData.setBarWidth(barWidth);
            barChart.setData(barData);

            // Important: Set the visible range to start from 0
            barChart.getXAxis().setAxisMinimum(0);

            // Calculate the maximum x value based on your data
            float groupCount = months.size();
            // This calculation ensures the chart extends to show all groups properly
            barChart.getXAxis().setAxisMaximum(0 + barChart.getBarData().getGroupWidth(groupSpace, barSpace) * groupCount);

            // Customize X-Axis
            XAxis xAxis = barChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setCenterAxisLabels(true);
            xAxis.setTypeface(typeface);
            xAxis.setTextSize(12f);
            xAxis.setTextColor(Color.parseColor("#4F3422"));

            // Customize Y-Axis
            barChart.getAxisLeft().setTypeface(typeface);
            barChart.getAxisLeft().setTextSize(12f);
            barChart.getAxisLeft().setTextColor(Color.parseColor("#4F3422"));

            // This is critical - it positions the first group at the left edge
            barChart.groupBars(0, groupSpace, barSpace);

            barChart.getAxisRight().setEnabled(false);
            barChart.getDescription().setEnabled(false);


            // Enable and customize legend for bar chart
            Legend legend = barChart.getLegend();
            legend.setEnabled(false);
            legend.setTypeface(typeface);
            legend.setTextSize(14f);
            legend.setTextColor(Color.parseColor("#4F3422"));
            legend.setForm(Legend.LegendForm.CIRCLE);
            legend.setFormSize(8f);
            legend.setXEntrySpace(20f);

            barChart.getAxisLeft().setDrawGridLines(false); // Disable left axis grid lines
            barChart.getAxisRight().setDrawGridLines(false); // Disable right axis grid lines
            barChart.getXAxis().setDrawGridLines(false); // Disable horizontal grid lines
            barChart.setFitBars(true);

            Log.d("BarChart", "Chart updated with data");
        } else {
            // No data to display
            Log.d("BarChart", "No entries to display");
            barChart.clear();
            barChart.setNoDataText("No mood data available");
            barChart.setNoDataTextColor(Color.parseColor("#4F3422"));
            barChart.setNoDataTextTypeface(typeface);
        }

        // Always invalidate to refresh
        barChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}