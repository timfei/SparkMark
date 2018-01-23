package com.tim.annotation.loader;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.tim.annotation.R;

import java.io.File;

/**
 * Created by TimFei on 16/9/7.
 */
public class GlideImageLoader implements ImageLoader {
    @Override
    public void displayImage(Context context, String path, ImageView imageView) {

        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_default_image);

        Glide.with(context)
                .load(Uri.fromFile(new File(path)))
                .apply(options)
                .into(imageView);
    }

    @Override
    public void clearMemoryCache() {

    }
}
