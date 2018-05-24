package com.march.gallery;

import android.content.Context;
import android.widget.ImageView;

import com.march.common.model.ImageInfo;
import com.march.common.utils.LogUtils;
import com.march.gallery.list.GalleryListFragment;
import com.march.uikit.app.BaseActivity;

import java.util.List;

/**
 * CreateAt : 2018/2/28
 * Describe :
 *
 * @author chendong
 */
public abstract class GalleryActivity extends BaseActivity {

    private GalleryListFragment mGalleryListFragment;

    @Override
    public int getLayoutId() {
        return R.layout.gallery_activity;
    }

    @Override
    public void initAfterViewCreated() {
        super.initAfterViewCreated();
        mGalleryListFragment = GalleryListFragment.newInst(1);
        mGalleryListFragment.addToContainer(this, R.id.fragment_container_list);
    }

    @Override
    public void initBeforeViewCreated() {
        Gallery.setGalleryService(new Gallery.GalleryService() {
            @Override
            public void loadImg(Context context, String path, int width, int height, ImageView imageView) {
                GalleryActivity.this.loadImg(context, path, width, height, imageView);
            }

            @Override
            public void onSuccess(List<ImageInfo> list) {
                LogUtils.object(list);
                GalleryActivity.this.onResult(list);
            }

            @Override
            public int getPreviewContainerId() {
                return R.id.fragment_container_preview;
            }
        });
    }

    protected abstract void loadImg(Context context, String path, int width, int height, ImageView imageView);

    protected abstract void onResult(List<ImageInfo> list);

    @Override
    public void onBackPressed() {
        if (!mGalleryListFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
