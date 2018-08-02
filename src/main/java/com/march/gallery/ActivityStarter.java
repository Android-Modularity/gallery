package com.march.gallery;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * CreateAt : 2018/8/2
 * Describe :
 *
 * @author chendong
 */
public class ActivityStarter {

    private Activity                        appActivity;
    private AppCompatActivity               supportActivity;
    private Fragment                        appFragment;
    private android.support.v4.app.Fragment supportFragment;

    public ActivityStarter(AppCompatActivity supportActivity) {
        this.supportActivity = supportActivity;
    }

    public ActivityStarter(Activity appActivity) {
        this.appActivity = appActivity;
    }

    public ActivityStarter(Fragment appFragment) {
        this.appFragment = appFragment;
    }

    public ActivityStarter(android.support.v4.app.Fragment supportFragment) {
        this.supportFragment = supportFragment;
    }


    public void startActivity(Intent intent) {
        if (appActivity != null) {
            appActivity.startActivity(intent);
        }
        if (supportActivity != null) {
            supportActivity.startActivity(intent);
        }
        if (appFragment != null) {
            appFragment.startActivity(intent);
        }
        if (supportFragment != null) {
            supportFragment.startActivity(intent);
        }
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        if (appActivity != null) {
            appActivity.startActivityForResult(intent, requestCode);
        }
        if (supportActivity != null) {
            supportActivity.startActivityForResult(intent, requestCode);
        }
        if (appFragment != null) {
            appFragment.startActivityForResult(intent, requestCode);
        }
        if (supportFragment != null) {
            supportFragment.startActivityForResult(intent, requestCode);
        }
    }
}
