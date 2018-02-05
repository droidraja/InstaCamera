package com.sphata.instacamera;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

class PermissionDelegate {

    private static final int REQUEST_CODE = 10;
    private final Activity activity;

    PermissionDelegate(Activity activity) {
        this.activity = activity;
    }

    boolean hasCameraPermission() {
        int permissionCheckResult = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
        );
        return permissionCheckResult == PackageManager.PERMISSION_GRANTED;
    }

    void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CODE
        );
    }

    boolean resultGranted(int requestCode,
                          String[] permissions,
                          int[] grantResults) {

        if (requestCode != REQUEST_CODE) {
            return false;
        }

        if (grantResults.length < 1) {
            return false;
        }
        if (!(permissions[0].equals(Manifest.permission.CAMERA))) {
            return false;
        }

        View noPermissionView = activity.findViewById(getRId(activity,"no_permission","id"));

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            noPermissionView.setVisibility(View.GONE);
            return true;
        }

        requestCameraPermission();
        noPermissionView.setVisibility(View.VISIBLE);
        return false;
    }

    private int getRId(Activity activity, String identifier,String folder)
    {
        String package_name = activity.getApplication().getPackageName();
        return activity.getApplication().getResources().getIdentifier(identifier, folder, package_name);
    }
}
