package com.tim.annotation.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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

import at.markushi.ui.CircleButton;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by TimFei on 16/9/18.
 */
public class WorkSpaceActivity extends AppCompatActivity {

    @BindView(R.id.annotatedview)
    AnnotatedView mAnnotatedView;

    @BindView(R.id.workspace_toolbox_btn)
    CircleButton mToolbox;

    private ImageItem mImageItem;
    private int screenWidth;
    private int screenHeight;
    private ProgressDialog mSaveImageProgressDialog;
    private String fileName;
    private static final String SAVE_PATH = "/storage/emulated/0/Annotation/";
    private LayoutInflater mInflater = null;
    private PopupWindow mToolBoxPW;
    private View toolBoxContentView;

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
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setToolBoxPopupWindow();

    }

    private void initData() {
        Intent intent = getIntent();
        mImageItem = (ImageItem) intent.getSerializableExtra(Constant.EXTRA_PICK_IMAGE_ITEM);
        mAnnotatedView.setBitmap(scaleBitmap(mImageItem.path), screenWidth, screenHeight);
    }

    /**
     * scale the image to fit screen width
     *
     * @param path
     * @return
     */
    private Bitmap scaleBitmap(String path) {
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        Bitmap bitmap = null;
        try {
            bitmap = ImageUtil.getBitmap(this, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Bitmap bitmap = BitmapFactory.decodeFile(path);
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
                new SaveBitmapTask().execute();

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
        fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        File file = new File(dir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            if (drawingCache != null) {
                drawingCache.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
        ImageUtil.scanPhoto(this, file);
    }

    private void setToolBoxPopupWindow() {
        toolBoxContentView = mInflater.inflate(R.layout.popuwindow_toolbox, null);
        mToolBoxPW = new PopupWindow(toolBoxContentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mToolBoxPW.setFocusable(true);
        mToolBoxPW.setOutsideTouchable(true);
        mToolBoxPW.update();
        ColorDrawable backgroundColor = new ColorDrawable(0000000000);
        mToolBoxPW.setBackgroundDrawable(backgroundColor);
    }

    private void showToolBoxPopuWindow(View parent) {
        if (mToolBoxPW.isShowing()) {
            mToolBoxPW.dismiss();
        } else {
            mToolBoxPW.showAsDropDown(parent, 0, parent.getLayoutParams().height / 2);
        }
    }

    @OnClick(R.id.workspace_toolbox_btn)
    public void toolBoxClick() {
        showToolBoxPopuWindow(mToolbox);
    }

    class SaveBitmapTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSaveImageProgressDialog = new ProgressDialog(WorkSpaceActivity.this);
            mSaveImageProgressDialog.show();
            mSaveImageProgressDialog.setMessage(getResources().getString(R.string.dialog_message));
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            saveBitmap();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                mSaveImageProgressDialog.dismiss();
                Util.showToast(WorkSpaceActivity.this, SAVE_PATH + fileName);
            } else {
                Util.showToast(WorkSpaceActivity.this, getResources().getString(R.string.save_failed));
            }
        }
    }

}
