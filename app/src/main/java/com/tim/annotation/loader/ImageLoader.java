package com.tim.annotation.loader;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;

import java.io.Serializable;

/**
 * Created by TimFei on 16/9/7.
 */
public interface ImageLoader extends Serializable {

    void displayImage(Context context, String path, ImageView imageView);

    void clearMemoryCache();
}
