package com.tim.annotation.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.tim.annotation.R;
import com.tim.annotation.entity.ImageItem;
import com.tim.annotation.loader.GlideImageLoader;
import com.tim.annotation.util.ImageUtil;

import java.util.ArrayList;

/**
 * Created by TimFei on 16/9/7.
 */
public class GridViewAdapter extends BaseAdapter {

    private Activity mActivity;
    private ArrayList<ImageItem> mImages;
    private int mImageSize;

    public GridViewAdapter(Activity activity, ArrayList<ImageItem> imageItems) {
        this.mActivity = activity;
        this.mImages = imageItems;

        mImageSize = ImageUtil.getImageItemWidth(mActivity);
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_main_gridview, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ImageItem imageItem = (ImageItem) getItem(position);

        GlideImageLoader loader = new GlideImageLoader();
        loader.displayImage(mActivity, imageItem.path, viewHolder.mImageThumb, mImageSize, mImageSize);


        return convertView;
    }

    private class ViewHolder {

        private ImageView mImageThumb;

        public ViewHolder(View view) {
            mImageThumb = (ImageView) view.findViewById(R.id.item_main_gridview_iv);
        }
    }
}
