package com.march.gallery;

import android.content.Context;
import android.widget.ImageView;

import com.march.common.model.ImageInfo;
import com.march.gallery.list.GalleryListFragment;
import com.march.gallery.model.ImageDirInfo;

import java.util.List;

/**
 * CreateAt : 2018/2/27
 * Describe :
 *
 * @author chendong
 */
public class Gallery {

    public static final String KEY_LIMIT = "KEY_LIMIT";
    public static final String KEY_LIST = "KEY_LIST";


    public interface GalleryService {

        void loadImg(Context context, String path, int width, int height, ImageView imageView);

        void onSuccess(List<ImageInfo> list);

        int getPreviewContainerId();
    }

    private static GalleryService sGalleryService;

    public static void setGalleryService(GalleryService galleryService) {
        sGalleryService = galleryService;
    }

    public static GalleryService getGalleryService() {
        return sGalleryService;
    }

}
