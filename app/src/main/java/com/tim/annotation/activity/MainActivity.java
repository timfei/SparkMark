package com.tim.annotation.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.tim.annotation.R;
import com.tim.annotation.adapter.FolderAdapter;
import com.tim.annotation.adapter.ImageGridViewAdapter;
import com.tim.annotation.constants.Constant;
import com.tim.annotation.entity.ImageFolder;
import com.tim.annotation.entity.ImageItem;
import com.tim.annotation.source.ImageSource;
import com.tim.annotation.util.ImageUtil;
import com.tim.annotation.util.Util;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ImageSource.OnImageLoadFinished, ImageGridViewAdapter.OnImageItemClickListener, FolderAdapter.OnFolderItemClickListener {

    private GridView mGridView;
    private ImageView mIV_Ghost;
    private List<ImageFolder> mImageFolders;
    private ImageGridViewAdapter mImageGridViewAdapter;
    private FolderAdapter mFolderAdapter;
    private Point size;
    private ImageFolder mAllImageFolder;
    private boolean isFolderMode = false;
    private boolean isAllImageFolder = true;

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private File photoFile;

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
            mIV_Ghost.setVisibility(View.GONE);
            new ImageSource(this, this);
            mImageGridViewAdapter = new ImageGridViewAdapter(this, null, size);
            mFolderAdapter = new FolderAdapter(this, null, size);
        } else {
            mIV_Ghost.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new ImageSource(this, this);
                mImageGridViewAdapter = new ImageGridViewAdapter(this, null, size);
                mFolderAdapter = new FolderAdapter(this, null, size);
            }

        } else if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            }
        }

    }

    private void initView() {
        mGridView = (GridView) findViewById(R.id.main_gridview);
        mGridView.setNumColumns(3);
        mIV_Ghost = (ImageView) findViewById(R.id.main_ghost_iv);
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
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                }
                boolean hasTakePhotoPermission = Util.checkSelfPermission(this, Manifest.permission.CAMERA);
                if (hasTakePhotoPermission) {
                    takePhoto();
                }
                break;

            case R.id.main_convert:
                boolean hasStoragePermission = Util.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (hasStoragePermission) {
                    if (isFolderMode) {
                        switchToAllImage();
                    } else {
                        switchToFolder();
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                photoFile = new File(Environment.getExternalStorageDirectory(), "/DCIM/camera");
            } else {
                photoFile = Environment.getDataDirectory();
            }
            photoFile = ImageUtil.createFile(photoFile, "IMG_", ".jpg");
            if (photoFile != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            }
        }
        startActivityForResult(intent, Constant.EXTRA_TAKE_PHOTO_CODE);
    }


    @Override
    public void imageLoadFinish(List<ImageFolder> imageFolders) {
        //when new image saved  gridView will reload and show all image
        mGridView.setNumColumns(3);
        this.mImageFolders = imageFolders;
        if (mImageFolders.size() == 0) {
            mImageGridViewAdapter.refreshData(null);
            mIV_Ghost.setVisibility(View.VISIBLE);
            Util.showToast(this, getResources().getString(R.string.no_image));
        } else {
            mIV_Ghost.setVisibility(View.GONE);
            mAllImageFolder = mImageFolders.get(0);
            mImageGridViewAdapter.refreshData(mAllImageFolder.images);
        }
        mImageGridViewAdapter.setOnImageItemClickListener(this);
        mGridView.setAdapter(mImageGridViewAdapter);
    }

    @Override
    public void onImageItemClick(View view, ImageItem imageItem, int position) {
        Intent intent = new Intent(this, WorkSpaceActivity.class);
        intent.putExtra(Constant.EXTRA_PICK_IMAGE_ITEM, imageItem);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    @Override
    public void onFolderItemClick(View view, ImageFolder imageFolder, int position) {
        mGridView.setNumColumns(3);
        mGridView.setAdapter(mImageGridViewAdapter);
        mImageGridViewAdapter.refreshData(imageFolder.images);
        isFolderMode = false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.EXTRA_TAKE_PHOTO_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    ImageUtil.scanPhoto(this, photoFile);
                    ImageItem imageItem = new ImageItem();
                    imageItem.path = photoFile.getAbsolutePath();
                    Intent intent = new Intent(MainActivity.this, WorkSpaceActivity.class);
                    intent.putExtra(Constant.EXTRA_PICK_IMAGE_ITEM, imageItem);
                    startActivity(intent);
                }
        }
    }

    @Override
    public void onBackPressed() {
        if (isFolderMode) {
            switchToAllImage();
        } else if (isAllImageFolder) {
            super.onBackPressed();
        } else {
            switchToFolder();
        }
    }

    /**
     * switch to folder mode
     */
    private void switchToFolder() {
        mGridView.setNumColumns(2);
        mGridView.setAdapter(mFolderAdapter);
        if (mImageFolders != null && mImageFolders.size() > 0) {
            mFolderAdapter.refreshData(mImageFolders);
        }
        mFolderAdapter.setOnFolderItemClickListener(this);
        isFolderMode = true;
        isAllImageFolder = false;
    }

    /**
     * switch to all image
     */
    private void switchToAllImage() {
        mGridView.setNumColumns(3);
        mGridView.setAdapter(mImageGridViewAdapter);
        if (mAllImageFolder != null) {
            mImageGridViewAdapter.refreshData(mAllImageFolder.images);
        }
        isFolderMode = false;
        isAllImageFolder = true;
    }
}
