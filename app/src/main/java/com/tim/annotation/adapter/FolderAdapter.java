package com.tim.annotation.adapter;

import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tim.annotation.R;
import com.tim.annotation.entity.ImageFolder;
import com.tim.annotation.loader.GlideImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TimFei on 16/9/18.
 */
public class FolderAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater mInflater;
    private List<ImageFolder> mImageFolders;
    private Point size;
    private OnFolderItemClickListener listener;


    public FolderAdapter(Context context, List<ImageFolder> folders, Point size) {
        this.context = context;
        this.size = size;
        if (folders != null && folders.size() > 0) {
            folders.remove(0);
            this.mImageFolders = folders;
        } else {
            mImageFolders = new ArrayList<>();
        }
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setOnFolderItemClickListener(OnFolderItemClickListener listener) {
        this.listener = listener;
    }

    public void refreshData(List<ImageFolder> folders) {
        if (folders != null && folders.size() > 0) {
            if (folders.get(0).path.equals("/")) {
                folders.remove(0);
            }
            this.mImageFolders = folders;
        } else {
            mImageFolders.clear();
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mImageFolders.size();
    }

    @Override
    public Object getItem(int position) {
        return mImageFolders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_folder_gridview, parent, false);
            viewHolder = new ViewHolder(convertView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ImageFolder folder = (ImageFolder) getItem(position);
        int coverWidth = size.x / 2;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(coverWidth, coverWidth);
        viewHolder.mCover.setLayoutParams(layoutParams);
        viewHolder.mFolderName.setText(folder.name);
        viewHolder.mFolderImageCount.setText(folder.images.size() + "");
        GlideImageLoader loader = new GlideImageLoader();
        loader.displayImage(context, folder.cover.path, viewHolder.mCover);

        viewHolder.mCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFolderItemClick(viewHolder.rootView, folder, position);
            }
        });

        return convertView;
    }

    private class ViewHolder {
        ImageView mCover;
        TextView mFolderName;
        TextView mFolderImageCount;
        View rootView;

        public ViewHolder(View view) {
            rootView = view;
            mCover = (ImageView) view.findViewById(R.id.item_folder_cover_iv);
            mFolderName = (TextView) view.findViewById(R.id.item_folder_name_tv);
            mFolderImageCount = (TextView) view.findViewById(R.id.item_folder_count_tv);
            view.setTag(this);
        }
    }

    public interface OnFolderItemClickListener {
        void onFolderItemClick(View view, ImageFolder imageFolder, int position);
    }

}
