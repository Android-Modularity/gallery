package com.march.gallery.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.march.common.Common;
import com.march.common.extensions.MsgBus;
import com.march.common.extensions.SizeX;
import com.march.common.extensions.ViewX;
import com.march.common.utils.CheckUtils;
import com.march.gallery.Gallery;
import com.march.gallery.R;
import com.march.gallery.model.GalleryImageInfo;
import com.march.lightadapter.LightHolder;
import com.march.lightadapter.pager.LightPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * CreateAt : 2018/3/2
 * Describe : 图片预览界面
 *
 * @author chendong
 */
public class GalleryPreviewFragment extends Fragment {

    private LightPagerAdapter<GalleryImageInfo> mImagePagerAdapter;
    private ViewPager                           mImageVp;
    private TextView                            mEnsureTv;
    private ImageView                           mSelectSiv;

    private List<GalleryImageInfo> mAllImages;
    private List<GalleryImageInfo> mSelectImages;
    private int                    mInitIndex;
    private int                    mMaxNum;

    private int width, height;


    public static GalleryPreviewFragment newInst(Bundle bundle) {
        GalleryPreviewFragment fragment = new GalleryPreviewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public void update(List<GalleryImageInfo> allImages, List<GalleryImageInfo> selectImages, int index) {
        mAllImages = new ArrayList<>(allImages);
        mSelectImages = new ArrayList<>(selectImages);
        mInitIndex = index;
        if (mImageVp == null) {
            return;
        }
        if (mImagePagerAdapter != null) {
            mImagePagerAdapter.notifyDataSetChanged();
        } else {
            mImagePagerAdapter = new LightPagerAdapter<GalleryImageInfo>(mAllImages, R.layout.gallery_preview_item) {
                @Override
                public void onBindView(LightHolder holder, GalleryImageInfo data) {
                    ImageView view = holder.getView(R.id.iv_image);
                    Common.getInst().getImgLoadAdapter().loadImg(view.getContext(), data.getPath(), width, height, view);
                }
            };
            mImageVp.setAdapter(mImagePagerAdapter);
        }
        mImageVp.setCurrentItem(mInitIndex);
        if (mSelectSiv != null) {
            GalleryImageInfo GalleryImageInfo = mAllImages.get(mInitIndex);
            updateSelectStatus(GalleryImageInfo);
        }
        updateEnsureText();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        width = SizeX.WIDTH;
        height = SizeX.HEIGHT - SizeX.dp2px(90);
        View view = inflater.inflate(R.layout.gallery_preview_fragment, container, false);
        Bundle arguments = getArguments();
        mAllImages = new ArrayList<>();
        mSelectImages = new ArrayList<>();
        if (arguments != null) {
            ArrayList<GalleryImageInfo> allImgs = arguments.getParcelableArrayList(Gallery.KEY_ALL_IMGS);
            if (!CheckUtils.isEmpty(allImgs)) {
                mAllImages.addAll(allImgs);
            }
            ArrayList<GalleryImageInfo> selectImgs = arguments.getParcelableArrayList(Gallery.KEY_SELECT_IMGS);
            if (!CheckUtils.isEmpty(selectImgs)) {
                mSelectImages.addAll(selectImgs);
            }
            mInitIndex = arguments.getInt(Gallery.KEY_INDEX, 0);
            mMaxNum = arguments.getInt(Gallery.KEY_LIMIT, 0);
        }
        initView(view);
        return view;
    }

    // 更新选中图标
    private void updateSelectStatus(GalleryImageInfo GalleryImageInfo) {
        if (!mSelectImages.contains(GalleryImageInfo)) {
            mSelectSiv.setImageResource(Gallery.getInst().getCfg().previewUnSelectIcon);
        } else {
            mSelectSiv.setImageResource(Gallery.getInst().getCfg().previewSelectIcon);
        }
    }

    public void initView(View view) {
        mImageVp = (ViewPager) view.findViewById(R.id.vp_image);
        mEnsureTv = (TextView) view.findViewById(R.id.tv_ensure);
        mSelectSiv = (ImageView) view.findViewById(R.id.detail_select_iv);
        mSelectSiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEnsureText();
                GalleryImageInfo imageInfo = mAllImages.get(mImageVp.getCurrentItem());
                if (mMaxNum == 1) {
                    mSelectImages.clear();
                    mSelectImages.add(imageInfo);
                } else {
                    if (mSelectImages.contains(imageInfo)) {
                        mSelectImages.remove(imageInfo);
                    } else if (mSelectImages.size() < mMaxNum) {
                        mSelectImages.add(imageInfo);
                    }
                }
                updateSelectStatus(imageInfo);
                updateEnsureText();
                MsgBus.getInst().post(Gallery.EVENT_SELECT, imageInfo);
            }
        });
        ViewX.click(view, R.id.siv_back, v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
        mEnsureTv.setOnClickListener(v -> publish());
        mImageVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                GalleryImageInfo GalleryImageInfo = mAllImages.get(position);
                updateSelectStatus(GalleryImageInfo);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        update(mAllImages, mSelectImages, mInitIndex);
    }

    public void updateEnsureText() {
        if (mSelectImages.size() == 0) {
            mEnsureTv.setSelected(false);
            mEnsureTv.setText("未选择");
        } else {
            mEnsureTv.setText(String.format(Locale.CHINA, "完成(%d)", mSelectImages.size()));
            mEnsureTv.setSelected(true);
        }
    }

    private void publish() {
        Intent intent = new Intent();
        intent.putExtra(Gallery.KEY_COMPLETE, true);
        intent.putParcelableArrayListExtra(Gallery.KEY_SELECT_IMGS, new ArrayList<>(mSelectImages));
        if (getActivity() != null) {
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }
    }

}
