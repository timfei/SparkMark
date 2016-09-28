package com.tim.annotation.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.SuperToast;
import com.tim.annotation.R;
import com.tim.annotation.constants.Constant;
import com.tim.annotation.entity.ImageItem;
import com.tim.annotation.util.ImageUtil;
import com.tim.annotation.util.Util;
import com.tim.annotation.view.AnnotatedView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by TimFei on 16/9/18.
 */
public class WorkSpaceActivity extends AppCompatActivity {

    @BindView(R.id.annotatedview)
    AnnotatedView mAnnotatedView;

    private ImageItem mImageItem;
    private int screenWidth;
    private int screenHeight;
    private static final String SAVE_PATH = "/storage/emulated/0/Annotation/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace);
        ButterKnife.bind(this);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        mImageItem = (ImageItem) intent.getSerializableExtra(Constant.EXTRA_PICK_IMAGE_ITEM);
        mAnnotatedView.setBitmap(scaleBitmap(mImageItem.path), screenWidth, screenHeight);
    }

    private Bitmap scaleBitmap(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        int h = bitmap.getHeight();
        int w = bitmap.getWidth();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int ww = dm.widthPixels;
        int newHeight = ww * h / w;
        float scaleWidth = ((float) ww) / w;
        float scaleHeight = ((float) newHeight) / h;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newBitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.workspace_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.workspace_save:
                saveBitmap();
                break;

            case R.id.workspace_share:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveBitmap() {
        mAnnotatedView.setDrawingCacheEnabled(true);
        Bitmap drawingCache = mAnnotatedView.getDrawingCache();
        File dir = new File(SAVE_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        File file = new File(dir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            if (drawingCache != null) {
                drawingCache.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }
            Util.showToast(this, SAVE_PATH + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Util.showToast(this, getResources().getString(R.string.save_failed));
        } catch (IOException e) {
            e.printStackTrace();
            Util.showToast(this, getResources().getString(R.string.save_failed));
        }
        ImageUtil.scanPhoto(this, file);
    }

}
