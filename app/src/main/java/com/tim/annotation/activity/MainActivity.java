package com.tim.annotation.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import com.tim.annotation.R;
import com.tim.annotation.adapter.GridViewAdapter;
import com.tim.annotation.entity.ImageFolder;
import com.tim.annotation.source.ImageSource;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ImageSource.OnImageLoadFinished {

    private GridView mGridView;
    private List<ImageFolder> mImageFolders;
    private GridViewAdapter mGridViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        new ImageSource(this, this);
        mGridViewAdapter = new GridViewAdapter(this, null);
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
