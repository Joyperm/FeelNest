package com.example.FeelNest.frangment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.FeelNest.Adapter.JokeAdapter;
import com.example.FeelNest.Model.Jokes;
import com.example.FeelNest.databinding.FragmentMainBinding;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Main extends Fragment {
    private FragmentMainBinding binding;
    String jokeApiUrl;
    public static final String TAG = "JokeApi";
    RecyclerView jokeList;
    JokeAdapter jokeAdapter;
    List<Jokes> alljokes;
    private String category;

    public Main(String jokeApiUrl) {
        this.jokeApiUrl = jokeApiUrl;
        alljokes = new ArrayList<>();
        category = "Any";
        Log.d(TAG, "Main: "+jokeApiUrl); //testing
    }

    public Main(String category, String jokeApiUrl) {
        this.category = category;
        this.jokeApiUrl = jokeApiUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false);
        View v = binding.getRoot();

        jokeList = binding.jokeRecyclerList;
        jokeList.setLayoutManager(new LinearLayoutManager(getContext()));

        //jokeAdapter = new JokeAdapter(alljokes);
        jokeAdapter = new JokeAdapter(alljokes, category);
        jokeList.setAdapter(jokeAdapter);

        // Fetch jokes
        getJokes(jokeApiUrl);

        return v;
    }

    public void getJokes(String jokeApiUrl){
        //extract json data using volley
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, jokeApiUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Log.d(TAG,"onResponse: "+response.toString());
                try {
                    int amount = Integer.parseInt(response.getString("amount"));
                    JSONArray jokesArray = response.getJSONArray("jokes");
                    for(int i = 0;i < amount;i++){
                        JSONObject jokes = jokesArray.getJSONObject(i);
                        //Log.d(TAG,"onResponse: "+jokes.getString("type"));

                        Jokes j = new Jokes();
                        j.setType(jokes.getString("type"));
                        if(jokes.getString("type").equals("single")){
                            j.setJoke(jokes.getString("joke"));
                        }
                        else{
                            j.setSetUp(jokes.getString("setup"));
                            j.setDelivery(jokes.getString("delivery"));
                        }

                        //add to list
                        alljokes.add(j);
                        jokeAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(jsonObjectRequest);
    }
}