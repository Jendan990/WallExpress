package com.jdmdsoftware.wallexpress;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class WallAdapter extends RecyclerView.Adapter<WallAdapter.AdaptingViews> {

    private final RecyclerObjectItem recyclerObjectItem;
    Context context;
    ArrayList<WallData> dbArray;

    public WallAdapter(Context context,ArrayList<WallData> dbArray,RecyclerObjectItem recyclerObjectItem) {
        this.context = context;
        this.dbArray = dbArray;
        this.recyclerObjectItem =recyclerObjectItem;
    }

    @NonNull
    @Override
    public AdaptingViews onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.adapter_recycler,parent,false);

        return new AdaptingViews(view,recyclerObjectItem);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptingViews holder, int position) {
        Glide.with(context).load(new File(dbArray.get(position).getPath())).into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return dbArray.size();
    }

    public static class AdaptingViews extends RecyclerView.ViewHolder{
        ImageView imageView;

        public AdaptingViews(View items,RecyclerObjectItem recyclerObjectItem){
            super(items);
            imageView = items.findViewById(R.id.image_viewer);

            items.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(recyclerObjectItem != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            recyclerObjectItem.onClicked(position);
                        }
                    }
                }
            });

        }

    }
}
