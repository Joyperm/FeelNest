package com.example.FeelNest.ui.jokes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.FeelNest.Adapter.CatagoryAdapter;
import com.example.FeelNest.R;
import com.example.FeelNest.databinding.FragmentJokesBinding;
import com.example.FeelNest.frangment.Main;

import java.util.ArrayList;
import java.util.List;


public class JokesFragment extends Fragment {
    private FragmentJokesBinding binding;
    RecyclerView catList;
    public static List<String> categories;
    CatagoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        binding = FragmentJokesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        categories = new ArrayList<>();
        //add data to list
        categories.add("Any");
        categories.add("Programming");
        categories.add("Miscellaneous");
        categories.add("Dark");
        categories.add("Pun");
        categories.add("Spooky");
        categories.add("Christmas");



        catList = binding.catRecyclerviewList;
        adapter = new CatagoryAdapter(categories);
        //add layout manager, set to horizontal scroll with original order
        catList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false));
        catList.setAdapter(adapter);

        //put fragment inside mainActivity (replace frame layout in activity_main.xml)
        AppCompatActivity activity = (AppCompatActivity) getContext();
        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();

        // Create a new instance of the Main fragment with default parameters
        String defaultCategoryUrl = getResources().getString(R.string.baseUrl) + "Any?amount=10";
        Main mainFragment = new Main(defaultCategoryUrl);

        fragmentTransaction.replace(R.id.fragment_container_view_tag, mainFragment);
        fragmentTransaction.commit();


        //final TextView textView = binding.textJokes;
        //jokesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
