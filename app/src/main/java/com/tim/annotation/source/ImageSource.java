package com.tim.annotation.source;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.tim.annotation.R;
import com.tim.annotation.entity.ImageFolder;
import com.tim.annotation.entity.ImageItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TimFei on 16/9/7.
 */
public class ImageSource implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_ADDED};


//    private String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
//            + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
//
//    private Uri queryUri = MediaStore.Files.getContentUri("external");

    private OnImageLoadFinished listener;

    private FragmentActivity activity;

    private ArrayList<ImageFolder> imageFolders = new ArrayList<>();


    public ImageSource(FragmentActivity activity, OnImageLoadFinished listener) {
        this.listener = listener;
        this.activity = activity;
        LoaderManager loaderManager = activity.getSupportLoaderManager();
        loaderManager.initLoader(0, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader;
        cursorLoader = new CursorLoader(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null,
                IMAGE_PROJECTION[6] + " DESC");

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        imageFolders.clear();
        if (data != null) {
            ArrayList<ImageItem> allImages = new ArrayList<>();
            while (data.moveToNext()) {
                String imageName = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                String imagePath = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                long imageSize = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                int imageWidth = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
                int imageHeight = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
                String imageMimeType = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[5]));
                long imageAddTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[6]));

                ImageItem imageItem = new ImageItem();
                imageItem.name = imageName;
                imageItem.path = imagePath;
                imageItem.size = imageSize;
                imageItem.width = imageWidth;
                imageItem.height = imageHeight;
                imageItem.mimeType = imageMimeType;
                imageItem.addTime = imageAddTime;
                allImages.add(imageItem);

                File imageFile = new File(imagePath);
                File imageParentFile = imageFile.getParentFile();
                ImageFolder imageFolder = new ImageFolder();
                imageFolder.name = imageParentFile.getName();
                imageFolder.path = imageParentFile.getAbsolutePath();

                if (!imageFolders.contains(imageFolder)) {
                    ArrayList<ImageItem> images = new ArrayList<>();
                    images.add(imageItem);
                    imageFolder.cover = imageItem;
                    imageFolder.images = images;
                    imageFolders.add(imageFolder);
                } else {
                    imageFolders.get(imageFolders.indexOf(imageFolder)).images.add(imageItem);
                }
            }
            if (data.getCount() > 0) {
                ImageFolder allImagesFolder = new ImageFolder();
                allImagesFolder.name = activity.getResources().getString(R.string.all_images);
                allImagesFolder.path = "/";
                if (allImages.size() != 0) {
                    allImagesFolder.cover = allImages.get(0);
                }
                allImagesFolder.images = allImages;
                imageFolders.add(0, allImagesFolder);
            }
        }
        listener.imageLoadFinish(imageFolders);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public interface OnImageLoadFinished {
        void imageLoadFinish(List<ImageFolder> imageFolders);
    }
}
