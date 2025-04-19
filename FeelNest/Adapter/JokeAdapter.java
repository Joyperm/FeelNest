package com.example.FeelNest.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.FeelNest.Model.Jokes;
import com.example.FeelNest.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

public class JokeAdapter extends RecyclerView.Adapter<JokeAdapter.ViewHolder> {

    // Constants
    private static final String GIPHY_API_KEY = "xxxx";
    private static final String GIPHY_ENDPOINT = "xxxx";

    // Instance variables
    private List<Jokes> allJokes;
    private Context context;
    private RequestQueue requestQueue;
    private String category; // Current joke category

    public JokeAdapter(List<Jokes> allJokes) {
        this.allJokes = allJokes;
        this.category = "Any"; // Default category
    }

    // Constructor with category parameter
    public JokeAdapter(List<Jokes> allJokes, String category) {
        this.allJokes = allJokes;
        this.category = category;
    }

    // Setter for updating the category
    public void setCategory(String category) {
        this.category = category;
        notifyDataSetChanged(); // Refresh the adapter to update GIFs
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        // Initialize RequestQueue if not already done
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.jokes_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Load joke text
        if (allJokes.get(position).getType().equals("single")) {
            holder.firstLine.setText(allJokes.get(position).getJoke());
            holder.secondLine.setText("");
        } else {
            holder.firstLine.setText(allJokes.get(position).getSetUp());
            holder.secondLine.setText(allJokes.get(position).getDelivery());
        }

        // Load GIF based on the current category
        loadCategoryGif(holder.ivJokesBackground);
    }

    @Override
    public int getItemCount() {
        return allJokes.size();
    }

    /**
     * Loads a GIF from Giphy based on the current joke category
     * @param imageView The ImageView to load the GIF into
     */
    private void loadCategoryGif(final ImageView imageView) {
        // Prepare search term based on category
        String searchTerm;

        // Determine appropriate search term based on category
        switch (category) {
            case "Programming":
                searchTerm = "coding joke";
                break;
            case "Dark":
                searchTerm = "dark humor";
                break;
            case "Pun":
                searchTerm = "pun";
                break;
            case "Spooky":
                searchTerm = "halloween joke";
                break;
            case "Christmas":
                searchTerm = "christmas funny";
                break;
            case "Miscellaneous":
                searchTerm = "random funny";
                break;
            case "Any":
            default:
                // For "Any" category, use a random search term from these options
                String[] anyTerms = {"funny", "laugh", "joke", "comedy", "lol"};
                searchTerm = anyTerms[new Random().nextInt(anyTerms.length)];
                break;
        }

        // Build the Giphy API URL
        String url = "xxxx";

        // Make the API request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Parse the response to get a random GIF URL
                            JSONArray dataArray = response.getJSONArray("data");
                            if (dataArray.length() > 0) {
                                // Get a random GIF from the results
                                int randomIndex = new Random().nextInt(dataArray.length());
                                JSONObject gifObject = dataArray.getJSONObject(randomIndex);
                                JSONObject images = gifObject.getJSONObject("images");

                                // Use fixed_height for consistent sizing
                                JSONObject fixedHeight = images.getJSONObject("fixed_height");
                                String gifUrl = fixedHeight.getString("url");

                                // Load the GIF into the ImageView using Glide
                                Glide.with(context)
                                        .asGif()
                                        .load(gifUrl)
                                        .centerCrop()
                                        .into(imageView);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Keep the default image if there's an error
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        // Keep the default image if there's an error
                    }
                }
        );

        // Add the request to the queue
        requestQueue.add(request);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView firstLine, secondLine;
        ImageView ivJokesBackground;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            firstLine = itemView.findViewById(R.id.firstLine);
            secondLine = itemView.findViewById(R.id.secondLine);
            ivJokesBackground = itemView.findViewById(R.id.ivJokesBackground);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(v.getContext(), firstLine.getText().toString() + " Clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}