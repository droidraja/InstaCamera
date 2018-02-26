package com.sphata.instacamera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class InstaCamera extends CordovaPlugin {

    private CallbackContext callbackContext;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        Context context = cordova.getActivity().getApplicationContext();
        Intent intent = new Intent(context, TestActivity.class);
        intent.putExtra("maxVideoDuration",args.getInt(0));
        if (action.equals("start_camera")) {
            cordova.startActivityForResult(this, intent, 10);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        String[] files = intent.getStringArrayExtra("files");
        try {
            JSONArray jsonArray = new JSONArray(files);
            this.callbackContext.success(jsonArray);
        } catch (Exception e) {
            Log.e("InstaCamera", "JsonArray transformation not possible");
        }


    }

    @Override
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        super.onRestoreStateForActivityResult(state, callbackContext);
        this.callbackContext = callbackContext;
    }
}
