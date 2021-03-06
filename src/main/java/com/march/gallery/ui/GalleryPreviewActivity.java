package com.march.gallery.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.march.common.exts.AppUIMixin;
import com.march.common.exts.BarUI;
import com.march.gallery.Gallery;
import com.march.gallery.model.GalleryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * CreateAt : 2018/8/1
 * Describe :
 *
 * @author chendong
 */
public class GalleryPreviewActivity extends FragmentContainerActivity {

    public static final int CODE_REQ_PREVIEW = 100;

    public static void startActivityForResult(AppUIMixin mixin,
            List<GalleryItem> allImgs,
            List<GalleryItem> selectImgs,
            int index,
            int maxNum) {
        Intent intent = new Intent(mixin.getContext(), GalleryPreviewActivity.class);
        intent.putParcelableArrayListExtra(Gallery.KEY_ALL_IMG, new ArrayList<Parcelable>(allImgs));
        intent.putParcelableArrayListExtra(Gallery.KEY_SELECT_IMG, new ArrayList<Parcelable>(selectImgs));
        intent.putExtra(Gallery.KEY_INDEX, index);
        intent.putExtra(Gallery.KEY_MAX_NUM, maxNum);
        mixin.startActivityForResult(intent, CODE_REQ_PREVIEW);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUI.setStatusBarColor(this, Color.parseColor("#ffffff"));
        BarUI.setStatusBarLightMode(this);
    }

    private GalleryPreviewFragment mFragment;

    @Override
    Fragment getFragment() {
        if (mFragment == null) {
            mFragment = GalleryPreviewFragment.newInst(getIntent().getExtras());
        }
        return mFragment;
    }


//    @Override
//    public void finish() {
//        if (!mFragment.isComplete()) {
//            mFragment.publishOnBack();
//        }
//        super.finish();
//    }
}
