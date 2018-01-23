package com.tim.annotation.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tim.annotation.R;
import com.tim.annotation.entity.ImageItem;
import com.tim.annotation.loader.GlideImageLoader;

import java.util.ArrayList;

/**
 * Created by TimFei on 2018/1/22.
 */

public class ImageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private Context context;
    private ArrayList<ImageItem> mImages;
    private GlideImageLoader imageLoader;
    private OnImageClickListener listener;

    public ImageListAdapter(Context context, ArrayList<ImageItem> imageItems) {
        this.context = context;
        this.mImages = imageItems;
        imageLoader = new GlideImageLoader();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_image_gridview, parent, false);
        return new ImageViewHolder(itemView);
    }

    public void setOnImageItemClickListener(OnImageClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof ImageViewHolder) {
            final ImageViewHolder holder = (ImageViewHolder) viewHolder;
            final ImageItem imageItem = mImages.get(position);
            imageLoader.displayImage(context, imageItem.path, holder.mImage);
            holder.mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onImageClick(holder.mImage, imageItem, position);
                }
            });

        }

    }

    public void refreshData(ArrayList<ImageItem> list) {
        if (list == null || list.size() == 0) {
            this.mImages = new ArrayList<>();
        } else {
            this.mImages = list;
        }
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        if (mImages != null) {
            return mImages.size();
        } else {
            return 0;
        }
    }

    @Override
    public void onClick(View v) {

    }

    private class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImage;

        public ImageViewHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.item_main_gridview_iv);
        }
    }

    public interface OnImageClickListener {
        void onImageClick(View view, ImageItem imageItem, int position);
    }
}
