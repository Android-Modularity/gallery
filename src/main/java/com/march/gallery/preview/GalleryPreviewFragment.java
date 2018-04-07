package com.march.gallery.preview;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.march.common.model.ImageInfo;
import com.march.common.utils.ListUtils;
import com.march.gallery.Gallery;
import com.march.gallery.R;
import com.march.lightadapter.LightHolder;
import com.march.lightadapter.pager.LightPagerAdapter;
import com.march.uikit.app.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * CreateAt : 2018/3/2
 * Describe :
 *
 * @author chendong
 */
public class GalleryPreviewFragment extends BaseFragment {

    private LightPagerAdapter<ImageInfo> mImagePagerAdapter;
    private ViewPager mImageVp;
    private TextView mEnsureTv;
    private ImageView mSelectIv;

    @Override
    public int getLayoutId() {
        return R.layout.gallery_preview_fragment;
    }

    private List<ImageInfo> mAllImages = new ArrayList<>();
    private List<ImageInfo> mSelectImages = new ArrayList<>();
    private int mInitIndex;
    private PreviewService mPreviewService;

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
                    Gallery.getGalleryService().loadImg(view.getContext(), data.getPath(), -1, -1, view);
                }
            };
            mImageVp.setAdapter(mImagePagerAdapter);
        }
        mImageVp.setCurrentItem(mInitIndex);
        if (mSelectIv != null) {
            ImageInfo imageInfo = mAllImages.get(mInitIndex);
            mSelectIv.setSelected(mSelectImages.contains(imageInfo));
        }
        updateEnsureText();
    }

    @Override
    public void initCreateView() {
        super.initCreateView();
        mImageVp =  mViewDelegate.findView(R.id.vp_image);
        mEnsureTv =  mViewDelegate.findView(R.id.tv_ensure);
        mSelectIv =  mViewDelegate.findView(R.id.iv_select);
        mSelectIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEnsureText();
                ImageInfo imageInfo = mAllImages.get(mImageVp.getCurrentItem());
                if (mPreviewService.getMaxNum() == 1) {
                    mSelectImages.clear();
                    mSelectImages.add(imageInfo);
                } else {
                    ListUtils.addOrRemoveForContains(mSelectImages, imageInfo);
                }
                mSelectIv.setSelected(mSelectImages.contains(imageInfo));
                updateEnsureText();
            }
        });
         mViewDelegate.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        }, R.id.tv_back);
        mEnsureTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gallery.getGalleryService().onSuccess(mSelectImages);
                 mViewDelegate.finish();
            }
        });
        mImageVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ImageInfo imageInfo = mAllImages.get(position);
                mSelectIv.setSelected(mSelectImages.contains(imageInfo));
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
