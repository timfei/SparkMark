package com.tim.annotation.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import com.tim.annotation.R;
import com.tim.annotation.adapter.GridViewAdapter;
import com.tim.annotation.entity.ImageFolder;
import com.tim.annotation.source.ImageSource;
import com.tim.annotation.util.Util;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ImageSource.OnImageLoadFinished {

    private GridView mGridView;
    private List<ImageFolder> mImageFolders;
    private GridViewAdapter mGridViewAdapter;

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
        boolean hasPermission = Util.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasPermission) {
            new ImageSource(this, this);
            mGridViewAdapter = new GridViewAdapter(this, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new ImageSource(this, this);
                mGridViewAdapter = new GridViewAdapter(this, null);
            } else {
                Snackbar.make(mGridView, getString(R.string.permission_tip), Snackbar.LENGTH_LONG).show();
            }
        }

    }

    private void initView() {
        mGridView = (GridView) findViewById(R.id.main_gridview);
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

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void imageLoadFinish(List<ImageFolder> imageFolders) {
        this.mImageFolders = imageFolders;
        if (mImageFolders.size() == 0) {
            mGridViewAdapter.refreshData(null);
        } else {
            mGridViewAdapter.refreshData(mImageFolders.get(0).images);
        }
        mGridView.setAdapter(mGridViewAdapter);

    }
}
