package com.tim.annotation.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;


/**
 * Created by TimFei on 16/9/8.
 */
public class Util {

    /**
     * check permission
     *
     * @param context
     * @param permission
     * @return
     */
    @TargetApi(23)
    public static boolean checkSelfPermission(Object context, String permission) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return ActivityCompat.checkSelfPermission(activity,
                    permission) == PackageManager.PERMISSION_GRANTED;
        } else if (context instanceof Fragment) {
            Fragment fragment = (Fragment) context;
            return fragment.getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

}
