package com.tim.annotation.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

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


/**
 * Created by TimFei on 16/9/18.
 */
public class WorkSpaceActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageItem mImageItem;
    private String fileName;
    private static final String SAVE_PATH = "/storage/emulated/0/Annotation/";
    private boolean isLoaded;

    private ProgressDialog mSaveImageProgressDialog;
    private ProgressDialog mLoadingImageProgressDialog;
    private AnnotatedView mAnnotatedView;
    private ImageView mToolbox;
    private ImageView mUnDo;
    private ImageView mReDo;
    private ImageView mClearAll;
    private View mView_ToolBox;
    private View mView_UnDo;
    private View mView_ReDo;
    private View mView_ClearAll;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace);
        setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
        initData();
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
        mAnnotatedView = (AnnotatedView) findViewById(R.id.annotatedview);
        mToolbox = (ImageView) findViewById(R.id.workspace_toolbox_iv);
        mUnDo = (ImageView) findViewById(R.id.workspace_undo_iv);
        mReDo = (ImageView) findViewById(R.id.workspace_redo_iv);
        mView_ToolBox = findViewById(R.id.workspace_toolbox_rl);
        mView_UnDo = findViewById(R.id.workspace_undo_rl);
        mView_ReDo = findViewById(R.id.workspace_redo_rl);
        mView_ClearAll = findViewById(R.id.workspace_clear_all_rl);
        mView_ToolBox.setOnClickListener(this);
        mView_UnDo.setOnClickListener(this);
        mView_ReDo.setOnClickListener(this);
        mView_ClearAll.setOnClickListener(this);
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
                if (mAnnotatedView != null && mAnnotatedView.getSavePathCount() > 0) {
                    new SaveBitmapTask().execute();
                } else {
                    Util.showToast(WorkSpaceActivity.this, getString(R.string.save_no_effects_there));
                }
                break;

            case R.id.workspace_share:
                if (mAnnotatedView.getSavePathCount() > 0) {
                    File file = saveBitmap();
                    if (file != null) {
                        shareIntent(file);
                    } else {
                        Util.showToast(WorkSpaceActivity.this, getString(R.string.share_failed));
                    }
                } else {
                    File file = new File(mImageItem.path);
                    if (file != null) {
                        shareIntent(file);
                    } else {
                        Util.showToast(WorkSpaceActivity.this, getString(R.string.share_failed));
                    }

                }
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

    private void shareIntent(File file) {
        ImageUtil.scanPhoto(WorkSpaceActivity.this, file);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, getResources().getString(R.string.file_provider_name), file));
        shareIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(shareIntent, getString(R.string.title_send_file)), Constant.REQUEST_CODE_SHARE_INTENT);
    }

    private File saveBitmap() {
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
        return file;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.workspace_toolbox_rl:
                new MaterialDialog.Builder(this)
                        .items(R.array.work_tool_items)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                switch (position) {
                                    case 0:
                                        mAnnotatedView.setTool(Constant.CODE_TOOL_GESTURE);
                                        mToolbox.setImageDrawable(getDrawable(R.drawable.ic_gesture));
                                        break;

                                    case 1:
                                        mAnnotatedView.setTool(Constant.CODE_TOOL_ARROW);
                                        mToolbox.setImageDrawable(getDrawable(R.drawable.ic_arrow));
                                        break;

                                    case 2:
                                        mAnnotatedView.setTool(Constant.CODE_TOOL_RECT);
                                        mToolbox.setImageDrawable(getDrawable(R.drawable.ic_rect));
                                        break;
                                }

                            }
                        }).show();
                break;

            case R.id.workspace_undo_rl:
                mAnnotatedView.unDo();
                break;

            case R.id.workspace_redo_rl:
                mAnnotatedView.recover();
                break;

            case R.id.workspace_clear_all_rl:
                if (mAnnotatedView != null && mAnnotatedView.getSavePathCount() > 0) {
                    clearAllConfirmDialog();
                }
                break;

        }

    }

    /**
     * Clear all on workspace
     */
    private void clearAllConfirmDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(R.string.dialog_title_clear_all)
                .content(R.string.dialog_msg_clear_all)
                .positiveText(getResources().getString(R.string.dialog_positive_button_clear))
                .negativeText(getResources().getString(R.string.dialog_negative_button_cancel))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mAnnotatedView.reDo();
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

    private class SaveBitmapTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSaveImageProgressDialog = new ProgressDialog(WorkSpaceActivity.this);
            mSaveImageProgressDialog.show();
            mSaveImageProgressDialog.setCanceledOnTouchOutside(false);
            mSaveImageProgressDialog.setMessage(getResources().getString(R.string.dialog_msg_save));
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            File file = saveBitmap();
            if (file != null) {
                ImageUtil.scanPhoto(WorkSpaceActivity.this, file);
                return true;
            } else {
                return false;
            }
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
            mLoadingImageProgressDialog.setMessage(getResources().getString(R.string.dialog_msg_load));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_CODE_SHARE_INTENT) {
        }
    }

    private void backConfirm() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.dialog_title_discard))
                .content(getResources().getString(R.string.dialog_msg_discard))
                .positiveText(getResources().getString(R.string.dialog_positive_button_discard))
                .negativeText(getResources().getString(R.string.dialog_negative_button_cancel))
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
