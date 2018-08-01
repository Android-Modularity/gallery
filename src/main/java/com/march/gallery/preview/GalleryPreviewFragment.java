package com.march.gallery.preview;

import android.graphics.Color;
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
import com.march.common.utils.DimensUtils;
import com.march.gallery.Gallery;
import com.march.gallery.R;
import com.march.gallery.ShopIconView;
import com.march.gallery.common.CommonUtils;
import com.march.lightadapter.LightHolder;
import com.march.lightadapter.pager.LightPagerAdapter;

import java.util.ArrayList;
import java.util.List;

//import com.showjoy.shop.common.view.ShopIconView;

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
    private ShopIconView                 mSelectSiv;

    private List<ImageInfo> mAllImages = new ArrayList<>();
    private List<ImageInfo> mSelectImages = new ArrayList<>();
    private int mInitIndex;
    private PreviewService mPreviewService;

    private int width, height;


    public interface PreviewService {

        void onPreviewFinish(List<ImageInfo> selectImages);

        int getMaxNum();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (mPreviewService != null && hidden) {
            mPreviewService.onPreviewFinish(mSelectImages);
        }
    }

    public List<ImageInfo> getSelectImages() {
        return mSelectImages;
    }

    public void setPreviewService(PreviewService previewService) {
        mPreviewService = previewService;
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
        initCreateView(view);
        return view;
    }

    private void updateSelectSiv(ImageInfo imageInfo) {
        if (!mSelectImages.contains(imageInfo)) {
            mSelectSiv.setNormalIconColor(Color.rgb(26, 18, 16));
            mSelectSiv.setNormalIconText("&#xe700;");
            mSelectSiv.setClickable(false);
        } else {
            mSelectSiv.setNormalIconColor(Color.rgb(249, 52, 80));
            mSelectSiv.setNormalIconText("&#xe6d2;");
            mSelectSiv.setClickable(true);
        }
    }

    public void initCreateView(View view) {
        mImageVp = (ViewPager) view.findViewById(R.id.vp_image);
        mEnsureTv = (TextView) view.findViewById(R.id.tv_ensure);
        mSelectSiv = (ShopIconView) view.findViewById(R.id.siv_select);
        mSelectSiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEnsureText();
                ImageInfo imageInfo = mAllImages.get(mImageVp.getCurrentItem());
                if (mPreviewService.getMaxNum() == 1) {
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
            mEnsureTv.setText("完成(" + mSelectImages.size() + ")");
            mEnsureTv.setSelected(true);
        }
    }
}
