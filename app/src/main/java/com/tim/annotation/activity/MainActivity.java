package com.tim.annotation.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.tim.annotation.R;
import com.tim.annotation.adapter.FolderAdapter;
import com.tim.annotation.adapter.ImageGridViewAdapter;
import com.tim.annotation.constants.Constant;
import com.tim.annotation.entity.ImageFolder;
import com.tim.annotation.entity.ImageItem;
import com.tim.annotation.source.ImageSource;
import com.tim.annotation.util.Util;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ImageSource.OnImageLoadFinished, ImageGridViewAdapter.OnImageItemClickListener {

    private GridView mGridView;
    private List<ImageFolder> mImageFolders;
    private ImageGridViewAdapter mImageGridViewAdapter;
    private ProgressBar mProgressBar;
    private FolderAdapter mFolderAdapter;
    private Point size;
    private ImageFolder mAllImageFolder;
    private boolean isFolderMode = false;

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        Display display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
        boolean hasPermission = Util.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasPermission) {
            new ImageSource(this, this);
            mImageGridViewAdapter = new ImageGridViewAdapter(this, null, size);
            mFolderAdapter = new FolderAdapter(this, null, size);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new ImageSource(this, this);
                mImageGridViewAdapter = new ImageGridViewAdapter(this, null, size);
                mFolderAdapter = new FolderAdapter(this, null, size);
            } else {
                Snackbar.make(mGridView, getString(R.string.permission_tip), Snackbar.LENGTH_LONG).show();
            }
        }

    }

    private void initView() {
        mGridView = (GridView) findViewById(R.id.main_gridview);
        mGridView.setNumColumns(3);
        mProgressBar = (ProgressBar) findViewById(R.id.main_progressbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.main_take_photo:

                break;

            case R.id.main_convert:
                if (isFolderMode) {
                    mGridView.setNumColumns(3);
                    mGridView.setAdapter(mImageGridViewAdapter);
                    mImageGridViewAdapter.refreshData(mAllImageFolder.images);
                    isFolderMode = false;
                } else {
                    mGridView.setNumColumns(2);
                    mGridView.setAdapter(mFolderAdapter);
                    if (mImageFolders.size() > 0) {
                        mFolderAdapter.refreshData(mImageFolders);
                    }
                    isFolderMode = true;
                }


                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void imageLoadFinish(List<ImageFolder> imageFolders) {
        this.mImageFolders = imageFolders;
        if (mImageFolders.size() == 0) {
            mImageGridViewAdapter.refreshData(null);
        } else {
            mAllImageFolder = mImageFolders.get(0);
            mImageGridViewAdapter.refreshData(mAllImageFolder.images);
        }
        mImageGridViewAdapter.setOnImageItemClickListener(this);
        mGridView.setAdapter(mImageGridViewAdapter);
        mProgressBar.setVisibility(View.GONE);

    }

    @Override
    public void onImageItemClick(View view, ImageItem imageItem, int position) {
        Intent intent = new Intent(this, WorkSpaceActivity.class);
        intent.putExtra(Constant.EXTRA_PICK_IMAGE_ITEM, imageItem);
        startActivity(intent);
    }
}
