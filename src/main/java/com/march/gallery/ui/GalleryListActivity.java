package com.march.gallery.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.march.common.utils.StatusBarUtils;
import com.march.common.utils.immersion.ImmersionStatusBarUtils;
import com.march.gallery.Gallery;

/**
 * CreateAt : 2018/8/1
 * Describe :
 *
 * @author chendong
 */
public class GalleryListActivity extends FragmentContainerActivity {

    public static void startActivityForResult(Activity context, int maxNum, int requestCode) {
        Intent intent = new Intent(context, GalleryListActivity.class);
        intent.putExtra(Gallery.KEY_MAX_NUM, maxNum);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.setStatusBarColor(this, Color.parseColor("#ffffff"));
        ImmersionStatusBarUtils.setStatusBarLightMode(this);
    }

    @Override
    Fragment getFragment() {
        return GalleryListFragment.newInst(getIntent().getExtras());
    }

}
