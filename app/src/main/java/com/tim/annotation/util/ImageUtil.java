package com.tim.annotation.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by TimFei on 16/9/7.
 */
public class ImageUtil {

    public static int getImageItemWidth(Context context) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
        int cols = screenWidth / densityDpi;
        cols = cols < 3 ? 3 : cols;
        int columnSpace = (int) (2 * context.getResources().getDisplayMetrics().density);
        return (screenWidth - columnSpace * (cols - 1)) / cols;
    }

    public static File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) folder.mkdirs();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    public static void scanPhoto(Context context, File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public static Bitmap getBitmap(Context context, Uri uri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        return bitmap;
    }

    /**
     * zoom bitmap
     * @param file
     * @return
     */
    public static Bitmap decodeSampledBitmapFromFile(File file) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);

        int width_tmp = options.outWidth, height_tmp = options.outHeight;

        if (width_tmp <= 0 || height_tmp <= 0) return null;//error bitmap

        int REQUIRED_SIZE = 2048;
        if (width_tmp / height_tmp < 3 || height_tmp / width_tmp < 3) {
            REQUIRED_SIZE = 2048;
        }

        final float reqwidth = width_tmp > height_tmp ? REQUIRED_SIZE
                : (width_tmp * REQUIRED_SIZE / height_tmp);
        final float reqheight = height_tmp > width_tmp ? REQUIRED_SIZE
                : (height_tmp * REQUIRED_SIZE / width_tmp);

        final int minSideLength = Math.min((int) reqwidth, (int) reqheight);
        options.inSampleSize = computeSampleSize(options, minSideLength,
                (int) reqwidth * (int) reqheight);
        options.inJustDecodeBounds = false;
        options.inInputShareable = true;
        options.inPurgeable = true;

        Bitmap b = null;
        try {
            b = BitmapFactory.decodeFile(file.getPath(), options);
        } catch (OutOfMemoryError error) {
        }
        return b;
    }

    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }


}
