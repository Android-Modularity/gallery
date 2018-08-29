package com.march.gallery.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.march.common.extensions.BarUI;

/**
 * CreateAt : 2018/8/1
 * Describe :
 *
 * @author chendong
 */
public class GalleryListActivity extends FragmentContainerActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUI.setStatusBarColor(this, Color.parseColor("#ffffff"));
        BarUI.setStatusBarLightMode(this);
    }

    @Override
    Fragment getFragment() {
        return GalleryListFragment.newInst(getIntent().getExtras());
    }

}
