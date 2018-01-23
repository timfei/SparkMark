package com.tim.annotation.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tim.annotation.R;
import com.tim.annotation.entity.ImageFolder;
import com.tim.annotation.loader.GlideImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TimFei on 2018/1/22.
 */

public class FolderModeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context context;
    private ArrayList<ImageFolder> mFolders;
    private GlideImageLoader imageLoader;
    private OnFolderItemClickListener listener;

    public FolderModeAdapter(Context context, ArrayList<ImageFolder> folders) {
        this.context = context;
        this.mFolders = folders;

        imageLoader = new GlideImageLoader();
    }

    public void setOnFolderItemClickListener(OnFolderItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_folder_gridview, parent, false);
        return new FolderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof FolderViewHolder) {
            FolderViewHolder holder = (FolderViewHolder) viewHolder;
            holder.itemView.setTag(position);
            final ImageFolder imageFolder = mFolders.get(position);
            holder.mTv_Name.setText(imageFolder.name);
            holder.mTv_ImageCount.setText(String.valueOf(imageFolder.images.size()));
            imageLoader.displayImage(context, imageFolder.cover.path, holder.mIv_Cover);
            holder.mIv_Cover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onFolderItemClick(viewHolder.itemView, imageFolder, position);
                }
            });
        }

    }

    public void refreshData(ArrayList<ImageFolder> list) {
        if (list == null || list.size() == 0) {
            this.mFolders = new ArrayList<>();
        } else {
            this.mFolders = list;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mFolders != null) {
            return mFolders.size();
        } else {
            return 0;
        }
    }


    private class FolderViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIv_Cover;
        private TextView mTv_Name;
        private TextView mTv_ImageCount;

        public FolderViewHolder(View itemView) {
            super(itemView);
            mIv_Cover = itemView.findViewById(R.id.item_folder_cover_iv);
            mTv_Name = itemView.findViewById(R.id.item_folder_name_tv);
            mTv_ImageCount = itemView.findViewById(R.id.item_folder_count_tv);

        }
    }

    public interface OnFolderItemClickListener {
        void onFolderItemClick(View view, ImageFolder imageFolder, int position);
    }

}
