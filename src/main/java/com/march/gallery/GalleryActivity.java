package com.march.gallery;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.march.common.model.ImageInfo;
import com.march.common.utils.LgUtils;
import com.march.gallery.list.GalleryListFragment;

import java.util.List;

/**
 * CreateAt : 2018/2/28
 * Describe :
 *
 * @author chendong
 */
public abstract class GalleryActivity extends AppCompatActivity {

    private GalleryListFragment mGalleryListFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity);

        mGalleryListFragment = GalleryListFragment.newInst(10);
        mGalleryListFragment.addToContainer(this, R.id.fragment_container_list);

        Gallery.setGalleryService(new Gallery.GalleryService() {
            @Override
            public void loadImg(Context context, String path, int width, int height, ImageView imageView) {
                GalleryActivity.this.loadImg(context, path, width, height, imageView);
            }

            @Override
            public void onSuccess(List<ImageInfo> list) {
                LgUtils.object(list);
                GalleryActivity.this.onResult(list);
            }


            @Override
            public Gallery.Config getConfig() {
                return null;
            }
        });
    }

    protected abstract void loadImg(Context context, String path, int width, int height, ImageView imageView);

    protected abstract void onResult(List<ImageInfo> list);

//    @Override
//    public void onBackPressed() {
//        if (!mGalleryListFragment.onBackPressed()) {
//            super.onBackPressed();
//        }
//    }
}
