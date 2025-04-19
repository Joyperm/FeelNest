package com.example.FeelNest.Adapter;


import static com.example.FeelNest.ui.jokes.JokesFragment.categories;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;


import com.example.FeelNest.R;
import com.example.FeelNest.frangment.Main;

import java.util.List;

public class CatagoryAdapter extends RecyclerView.Adapter<CatagoryAdapter.ViewHolder>{

    //4.crate list
    List<String> cats;
    //keep track of item selected
    int selectedItem = -1;

    public CatagoryAdapter(List<String> cats) {
        this.cats = cats;
    }

    //3. create override methods
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //5. inflate custom view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_view,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.catName.setText(cats.get(position));
        //set color when item clicked
        if(position == selectedItem){
            //set bg for selcted Item
            holder.catName.setTextColor(ColorStateList.valueOf(Color.parseColor("#ffffff")));
            holder.cardView.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#4F3422")));

        }
        else{
            //set to none
            holder.catName.setTextColor(ColorStateList.valueOf(Color.parseColor("#4F3422")));
            holder.cardView.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#ffffff")));

        }


    }

    @Override
    public int getItemCount() {
        return cats.size();
    }

    //1.create view holder (inner class)
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView catName;
        CardView cardView;

        //2. create constructor matching super
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            catName = itemView.findViewById(R.id.catName);
            cardView = itemView.findViewById(R.id.cardItem);
            //set onclicklistener
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            notifyItemChanged(selectedItem);
            selectedItem = getAdapterPosition();
            notifyItemChanged(selectedItem);

            // Check if it's the first time the fragment is being loaded
            if (selectedItem == -1 || categories != null || categories.isEmpty()) {
                // Load the fragment with "any" as the default category
                loadFragment(v, new Main(v.getContext().getResources().getString(R.string.baseUrl) + "Any?amount=10"));
            } else {
                // Otherwise, load the selected category URL
                for (int i = 0; i < categories.size(); i++) {
                    if (cats.get(selectedItem).equals(categories.get(i))) {
                        loadFragment(v, new Main(v.getContext().getResources().getString(R.string.baseUrl) + categories.get(i) + "?amount=10"));
                    }
                }
            }

        }
    }

    public void loadFragment(View v, Fragment fragment){
        AppCompatActivity activity = (AppCompatActivity) v.getContext();
        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction()
                .replace(R.id.fragment_container_view_tag,fragment);
        fragmentTransaction.commit();
    }
}
