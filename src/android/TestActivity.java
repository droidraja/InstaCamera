package com.sphata.instacamera;

import android.app.Activity;
import android.os.Bundle;

public class TestActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource("activity_test"));
    }

    
    private int getLayoutResource(String name)
    {
        String package_name = getApplication().getPackageName();
        return getApplication().getResources().getIdentifier(name, "layout", package_name);
    }
}
