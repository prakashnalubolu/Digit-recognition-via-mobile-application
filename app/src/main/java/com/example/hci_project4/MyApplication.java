// File: app/src/main/java/com/example/hci_project3/MyApplication.java

package com.example.hci_project3;

import android.app.Application;
import com.facebook.soloader.SoLoader;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SoLoader.init(this, /* native exopackage */ false);
    }
}
