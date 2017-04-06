package com.tim.annotation.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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


/**
 * Created by TimFei on 16/9/18.
 */
public class WorkSpaceActivity extends AppCompatActivity implements View.OnClickListener {

    private AnnotatedView mAnnotatedView;
    private ImageView mToolbox;
    private View mLl_ToolBar;
    private ImageItem mImageItem;
    private ProgressDialog mSaveImageProgressDialog;
    private ProgressDialog mLoadingImageProgressDialog;
    private String fileName;
    private static final String SAVE_PATH = "/storage/emulated/0/Annotation/";
    private LayoutInflater mInflater = null;
    private PopupWindow mToolBoxPW;
    private View toolBoxContentView;
    private ImageView mUnDo;
    private ImageView mReDo;
    private boolean isLoaded;

    private CircleButton mToolboxGesture;
    private CircleButton mToolboxArrow;
    private CircleButton mToolboxText;
    private CircleButton mToolboxRect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
        initData();
        setToolBoxPopupWindow();
        loadImage();
    }

    private void loadImage() {
        ViewTreeObserver viewTreeObserver = mAnnotatedView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (!isLoaded) {
                        new LoadImageTask().execute();
                        isLoaded = true;
                    }
                }
            });
        }
    }

    private void initView() {
        mLl_ToolBar = findViewById(R.id.workspace_bottom_bar_ll);
        mAnnotatedView = (AnnotatedView) findViewById(R.id.annotatedview);
        mToolbox = (ImageView) findViewById(R.id.workspace_toolbox);
        mUnDo = (ImageView) findViewById(R.id.workspace_undo);
        mReDo = (ImageView) findViewById(R.id.workspace_redo);
        mUnDo.setOnClickListener(this);
        mReDo.setOnClickListener(this);
        mToolbox.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        mImageItem = (ImageItem) intent.getSerializableExtra(Constant.EXTRA_PICK_IMAGE_ITEM);
    }

    /**
     * scale the image to fit screen
     *
     * @param path
     * @return
     */
    private Bitmap scaleBitmap(String path) {
        File file = new File(path);
        Bitmap bitmap = ImageUtil.decodeSampledBitmapFromFile(file);
        int bitmapHeight = bitmap.getHeight();
        int bitmapWidth = bitmap.getWidth();
        int canvasWidth = mAnnotatedView.getWidth();
        int canvasHeight = mAnnotatedView.getHeight();
        Matrix matrix = new Matrix();
        if ((bitmapHeight > canvasHeight && bitmapHeight > bitmapWidth) || (bitmapHeight > bitmapWidth && bitmapHeight < canvasHeight)) {
            int newWidth = canvasHeight * bitmapWidth / bitmapHeight;
            float scaleWidth = ((float) newWidth) / bitmapWidth;
            float scaleHeight = ((float) canvasHeight) / bitmapHeight;
            matrix.postScale(scaleWidth, scaleHeight);
        } else {
            int newHeight = canvasWidth * bitmapHeight / bitmapWidth;
            float scaleWidth = ((float) canvasWidth) / bitmapWidth;
            float scaleHeight = ((float) newHeight) / bitmapHeight;
            matrix.postScale(scaleWidth, scaleHeight);
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
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

            case android.R.id.home:
                if (mAnnotatedView.getSavePathCount() != 0) {
                    backConfirm();
                } else {
                    finish();
                }
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
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        toolBoxContentView = mInflater.inflate(R.layout.popuwindow_toolbox, null);
        mToolBoxPW = new PopupWindow(toolBoxContentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mToolBoxPW.setFocusable(true);
        mToolBoxPW.setOutsideTouchable(true);
        mToolBoxPW.update();
        ColorDrawable backgroundColor = new ColorDrawable(0000000000);
        mToolBoxPW.setBackgroundDrawable(backgroundColor);
        initToolBoxView(toolBoxContentView);

    }

    private void initToolBoxView(View contentView) {
        mToolboxGesture = (CircleButton) contentView.findViewById(R.id.toolbox_gesture);
        mToolboxArrow = (CircleButton) contentView.findViewById(R.id.toolbox_arrow);
        mToolboxText = (CircleButton) contentView.findViewById(R.id.toolbox_text);
        mToolboxRect = (CircleButton) contentView.findViewById(R.id.toolbox_rect);
        mToolboxGesture.setOnClickListener(this);
        mToolboxArrow.setOnClickListener(this);
        mToolboxText.setOnClickListener(this);
        mToolboxRect.setOnClickListener(this);
    }

    private void showToolBoxPopupWindow(View parent) {
        if (mToolBoxPW.isShowing()) {
            mToolBoxPW.dismiss();
        } else {
            mToolBoxPW.showAsDropDown(parent, 0, mLl_ToolBar.getHeight() / 2);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.workspace_toolbox:
                new MaterialDialog.Builder(this)
                        .items(R.array.work_tool_items)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                switch (position) {
                                    case 0:
                                        mAnnotatedView.setTool(Constant.CODE_TOOL_GESTURE);
                                        mToolbox.setImageDrawable(getDrawable(R.drawable.ic_gesture_48));
                                        break;

                                    case 1:
                                        mAnnotatedView.setTool(Constant.CODE_TOOL_ARROW);
                                        mToolbox.setImageDrawable(getDrawable(R.drawable.ic_arrow_48));
                                        break;

                                    case 2:
                                        mAnnotatedView.setTool(Constant.CODE_TOOL_RECT);
                                        mToolbox.setImageDrawable(getDrawable(R.drawable.ic_rect_48));
                                        break;
                                }

                            }
                        }).show();
//                showToolBoxPopupWindow(mToolbox);
                break;

            case R.id.workspace_undo:
                mAnnotatedView.unDo();
                break;

            case R.id.workspace_redo:
                mAnnotatedView.recover();
                break;

            case R.id.toolbox_gesture:
                mAnnotatedView.setTool(Constant.CODE_TOOL_GESTURE);
                mToolbox.setImageDrawable(getDrawable(R.drawable.ic_gesture_48));
                mToolBoxPW.dismiss();
                break;


            case R.id.toolbox_arrow:
                mAnnotatedView.setTool(Constant.CODE_TOOL_ARROW);
                mToolbox.setImageDrawable(getDrawable(R.drawable.ic_arrow_48));
                mToolBoxPW.dismiss();
                break;

            case R.id.toolbox_text:
                mAnnotatedView.setTool(Constant.CODE_TOOL_TEXT);
                mToolbox.setImageDrawable(getDrawable(R.drawable.ic_text_48));
                mToolBoxPW.dismiss();
                break;

            case R.id.toolbox_rect:
                mAnnotatedView.setTool(Constant.CODE_TOOL_RECT);
                mToolbox.setImageDrawable(getDrawable(R.drawable.ic_rect_48));
                mToolBoxPW.dismiss();
                break;
        }

    }

    private class SaveBitmapTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSaveImageProgressDialog = new ProgressDialog(WorkSpaceActivity.this);
            mSaveImageProgressDialog.show();
            mSaveImageProgressDialog.setCanceledOnTouchOutside(false);
            mSaveImageProgressDialog.setMessage(getResources().getString(R.string.dialog_message_save));
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

    private class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingImageProgressDialog = new ProgressDialog(WorkSpaceActivity.this);
            mLoadingImageProgressDialog.setCanceledOnTouchOutside(false);
            mLoadingImageProgressDialog.show();
            mLoadingImageProgressDialog.setMessage(getResources().getString(R.string.dialog_message_load));
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = scaleBitmap(mImageItem.path);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                mLoadingImageProgressDialog.dismiss();
                mAnnotatedView.setBitmap(bitmap);
            } else {
                Util.showToast(WorkSpaceActivity.this, getResources().getString(R.string.save_failed));
            }


        }

    }

    private void backConfirm() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.dialog_discard_title))
                .content(getResources().getString(R.string.dialog_discard_message))
                .positiveText(getResources().getString(R.string.dialog_positive_button))
                .negativeText(getResources().getString(R.string.dialog_negative_button))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (mAnnotatedView.getSavePathCount() != 0) {
            backConfirm();
        } else {
            super.onBackPressed();
        }
    }

    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}
