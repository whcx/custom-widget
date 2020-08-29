package com.example.detailmodule.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DetailRecycleAdapter extends RecyclerView.Adapter<DetailRecycleAdapter.DetailHolder>{
    private final Context mContext;
    private List<Bitmap> mList;

    public DetailRecycleAdapter(List<Bitmap>  list, Context context) {
        mList = list;
        mContext = context;
    }

    @NonNull
    @Override
    public DetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(layoutParams);
        DetailHolder detailHolder = new DetailHolder(imageView);
        return detailHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DetailHolder holder, int position) {
        holder.imageView.setImageBitmap(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class DetailHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public DetailHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView)itemView;
        }
    }

}
