package com.march.gallery.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.march.common.extensions.ActFragmentMixin;
import com.march.common.model.ImageInfo;
import com.march.common.utils.StatusBarUtils;
import com.march.common.utils.immersion.ImmersionStatusBarUtils;
import com.march.gallery.Gallery;
import com.march.gallery.preview.GalleryPreviewFragment;

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

    public static void startActivityForResult(ActFragmentMixin mixin, List<ImageInfo> allImgs, List<ImageInfo> selectImgs, int index) {
        Intent intent = new Intent(mixin.getContext(), GalleryPreviewActivity.class);
        intent.putParcelableArrayListExtra(Gallery.KEY_ALL_IMGS, new ArrayList<Parcelable>(allImgs));
        intent.putParcelableArrayListExtra(Gallery.KEY_SELECT_IMGS, new ArrayList<Parcelable>(selectImgs));
        intent.putExtra(Gallery.KEY_INDEX, index);
        mixin.startActivityForResult(intent, CODE_REQ_PREVIEW);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.setStatusBarColor(this, Color.parseColor("#ffffff"));
        ImmersionStatusBarUtils.setStatusBarLightMode(this);
    }

    @Override
    Fragment getFragment() {
        return GalleryPreviewFragment.newInst(getIntent().getExtras());
    }
}
