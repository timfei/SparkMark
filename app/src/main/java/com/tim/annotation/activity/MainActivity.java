package com.tim.annotation.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import com.tim.annotation.R;
import com.tim.annotation.adapter.FolderModeAdapter;
import com.tim.annotation.adapter.ImageListAdapter;
import com.tim.annotation.constants.Constant;
import com.tim.annotation.entity.ImageFolder;
import com.tim.annotation.entity.ImageItem;
import com.tim.annotation.source.ImageSource;
import com.tim.annotation.util.ImageUtil;
import com.tim.annotation.util.Util;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ImageSource.OnImageLoadFinished, FolderModeAdapter.OnFolderItemClickListener, ImageListAdapter.OnImageClickListener {

    private ImageView mIV_Ghost;
    private ArrayList<ImageFolder> mImageFolders;
    private ImageListAdapter mImageListAdapter;
    private FolderModeAdapter mFolderModeAdapter;
    private ImageFolder mAllImageFolder;
    private boolean isFolderMode = false;
    private boolean isAllImageFolder = true;
    private File photoFile;
    private MenuItem viewModeItem;
    private RecyclerView mList;


    private GridLayoutManager imageLayoutManager;
    private GridLayoutManager folderLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.CODE_WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
        initImageListAdapter();
        boolean hasPermission = Util.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasPermission) {
            mIV_Ghost.setVisibility(View.GONE);
            new ImageSource(this, this);
        } else {
            mIV_Ghost.setVisibility(View.VISIBLE);
        }
    }

    private void initImageListAdapter() {
        mImageListAdapter = new ImageListAdapter(this, null);
        imageLayoutManager = new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        mList.setLayoutManager(imageLayoutManager);
        mList.setAdapter(mImageListAdapter);
        mImageListAdapter.setOnImageItemClickListener(this);

        mFolderModeAdapter = new FolderModeAdapter(this, null);
        folderLayoutManager = new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false);
        mFolderModeAdapter.setOnFolderItemClickListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constant.CODE_WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new ImageSource(this, this);
            }

        } else if (requestCode == Constant.CODE_CAMERA_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            }
        }

    }

    private void initView() {
        mIV_Ghost = findViewById(R.id.main_ghost_iv);
        mList = findViewById(R.id.main_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        viewModeItem = menu.findItem(R.id.main_convert);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.main_take_photo:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constant.CODE_CAMERA_REQUEST);
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
                        viewModeItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_view_list));
                    } else {
                        switchToFolder();
                        viewModeItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_view_module));
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
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(this, getResources().getString(R.string.file_provider_name), photoFile));
            }
        }
        startActivityForResult(intent, Constant.EXTRA_TAKE_PHOTO_CODE);
    }


    @Override
    public void imageLoadFinish(ArrayList<ImageFolder> imageFolders) {
        //when new image saved  gridView will reload and show all image
        this.mImageFolders = imageFolders;
        if (mImageFolders.size() == 0) {
            mIV_Ghost.setVisibility(View.VISIBLE);
            mImageListAdapter.refreshData(null);
            Util.showToast(this, getResources().getString(R.string.no_image));
        } else {
            mIV_Ghost.setVisibility(View.GONE);
            mAllImageFolder = mImageFolders.get(0);
            mImageListAdapter.refreshData(mAllImageFolder.images);
        }
    }

    @Override
    public void onFolderItemClick(View view, ImageFolder imageFolder, int position) {
        mList.setLayoutManager(imageLayoutManager);
        mList.setAdapter(mImageListAdapter);
        mImageListAdapter.refreshData(imageFolder.images);
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
        mList.setLayoutManager(folderLayoutManager);
        mList.setAdapter(mFolderModeAdapter);
        if (mImageFolders != null && mImageFolders.size() > 0) {
            mFolderModeAdapter.refreshData(mImageFolders);
        }
        isFolderMode = true;
        isAllImageFolder = false;
    }

    /**
     * switch to all image
     */
    private void switchToAllImage() {
        mList.setLayoutManager(imageLayoutManager);
        mList.setAdapter(mImageListAdapter);
        if (mAllImageFolder != null) {
            mImageListAdapter.refreshData(mAllImageFolder.images);
        }
        isFolderMode = false;
        isAllImageFolder = true;
    }

    @Override
    public void onImageClick(View view, ImageItem imageItem, int position) {
        Intent intent = new Intent(this, WorkSpaceActivity.class);
        intent.putExtra(Constant.EXTRA_PICK_IMAGE_ITEM, imageItem);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);

    }
}
