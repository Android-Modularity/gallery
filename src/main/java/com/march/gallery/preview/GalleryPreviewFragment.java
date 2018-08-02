package com.march.gallery.preview;

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

import com.march.common.model.ImageInfo;
import com.march.common.utils.CheckUtils;
import com.march.common.utils.DimensUtils;
import com.march.gallery.Gallery;
import com.march.gallery.R;
import com.march.gallery.common.CommonUtils;
import com.march.lightadapter.LightHolder;
import com.march.lightadapter.pager.LightPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * CreateAt : 2018/3/2
 * Describe :
 *
 * @author chendong
 */
public class GalleryPreviewFragment extends Fragment {

    private LightPagerAdapter<ImageInfo> mImagePagerAdapter;
    private ViewPager                    mImageVp;
    private TextView                     mEnsureTv;
    private ImageView                    mSelectSiv;

    private List<ImageInfo> mAllImages;
    private List<ImageInfo> mSelectImages;
    private int             mInitIndex;
    private int             mMaxNum;

    private int width, height;


    public static GalleryPreviewFragment newInst(Bundle bundle) {
        GalleryPreviewFragment fragment = new GalleryPreviewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }



    public void update(List<ImageInfo> allImages, List<ImageInfo> selectImages, int index) {
        mAllImages = new ArrayList<>(allImages);
        mSelectImages = new ArrayList<>(selectImages);
        mInitIndex = index;
        if (mImageVp == null) {
            return;
        }
        if (mImagePagerAdapter != null) {
            mImagePagerAdapter.notifyDataSetChanged();
        } else {
            mImagePagerAdapter = new LightPagerAdapter<ImageInfo>(mAllImages, R.layout.gallery_preview_item) {
                @Override
                public void onBindView(LightHolder holder, ImageInfo data) {
                    ImageView view = holder.getView(R.id.iv_image);
                    Gallery.getGalleryService().loadImg(view.getContext(), data.getPath(), width, height, view);
                }
            };
            mImageVp.setAdapter(mImagePagerAdapter);
        }
        mImageVp.setCurrentItem(mInitIndex);
        if (mSelectSiv != null) {
            ImageInfo imageInfo = mAllImages.get(mInitIndex);
            updateSelectSiv(imageInfo);
        }
        updateEnsureText();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        width = getContext().getResources().getDisplayMetrics().widthPixels;
        height = getContext().getResources().getDisplayMetrics().heightPixels - DimensUtils.dp2px(90);
        View view = inflater.inflate(R.layout.gallery_preview_fragment, container, false);
        Bundle arguments = getArguments();
        mAllImages = new ArrayList<>();
        mSelectImages = new ArrayList<>();
        if (arguments != null) {
            ArrayList<ImageInfo> allImgs = arguments.getParcelableArrayList(Gallery.KEY_ALL_IMGS);
            if (!CheckUtils.isEmpty(allImgs)) {
                mAllImages.addAll(allImgs);
            }
            ArrayList<ImageInfo> selectImgs = arguments.getParcelableArrayList(Gallery.KEY_SELECT_IMGS);
            if (!CheckUtils.isEmpty(selectImgs)) {
                mSelectImages.addAll(selectImgs);
            }
            mInitIndex = arguments.getInt(Gallery.KEY_INDEX, 0);
            mMaxNum = arguments.getInt(Gallery.KEY_LIMIT, 0);
        }
        initView(view);
        return view;
    }

    private void updateSelectSiv(ImageInfo imageInfo) {
        if (!mSelectImages.contains(imageInfo)) {
            mSelectSiv.setImageResource(Gallery.getGalleryService().getConfig().previewUnSelectIcon);
            mSelectSiv.setClickable(false);
        } else {
            mSelectSiv.setImageResource(Gallery.getGalleryService().getConfig().previewSelectIcon);
            mSelectSiv.setClickable(true);
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
                ImageInfo imageInfo = mAllImages.get(mImageVp.getCurrentItem());
                if (mMaxNum == 1) {
                    mSelectImages.clear();
                    mSelectImages.add(imageInfo);
                } else {
                    CommonUtils.addOrRemoveForContains(mSelectImages, imageInfo);
                }
                updateSelectSiv(imageInfo);
                updateEnsureText();
            }
        });
        view.findViewById(R.id.siv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        mEnsureTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gallery.getGalleryService().onSuccess(mSelectImages);
            }
        });
        mImageVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ImageInfo imageInfo = mAllImages.get(position);
                updateSelectSiv(imageInfo);
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
}
