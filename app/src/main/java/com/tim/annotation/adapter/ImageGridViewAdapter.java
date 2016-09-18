package com.tim.annotation.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tim.annotation.R;
import com.tim.annotation.entity.ImageItem;
import com.tim.annotation.loader.GlideImageLoader;
import com.tim.annotation.util.ImageUtil;

import java.util.ArrayList;

/**
 * Created by TimFei on 16/9/7.
 */
public class ImageGridViewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ImageItem> mImages;
    private int mImageSize;
    private Point size;
    private OnImageItemClickListener listener;

    public ImageGridViewAdapter(Context context, ArrayList<ImageItem> imageItems, Point size) {
        this.context = context;
        this.mImages = imageItems;
        this.size = size;

        mImageSize = ImageUtil.getImageItemWidth(context);
    }

    public void setOnImageItemClickListener(OnImageItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    @Override
    public Object getItem(int position) {
        return mImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void refreshData(ArrayList<ImageItem> images) {
        if (images == null || images.size() == 0) this.mImages = new ArrayList<>();
        else this.mImages = images;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_image_gridview, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ImageItem imageItem = (ImageItem) getItem(position);


        GlideImageLoader loader = new GlideImageLoader();
        int thumbWidth = size.x / 3;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(thumbWidth, thumbWidth);
        viewHolder.mImageThumb.setLayoutParams(layoutParams);
        loader.displayImage(context, imageItem.path, viewHolder.mImageThumb);

        viewHolder.mImageThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onImageItemClick(viewHolder.rootView, imageItem, position);
                }
            }
        });


        return convertView;
    }

    private class ViewHolder {
        private View rootView;
        private ImageView mImageThumb;

        public ViewHolder(View view) {
            rootView = view;
            mImageThumb = (ImageView) view.findViewById(R.id.item_main_gridview_iv);
        }
    }

    public interface OnImageItemClickListener {
        void onImageItemClick(View view, ImageItem imageItem, int position);
    }
}
