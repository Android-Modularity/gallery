package com.march.gallery;

import android.content.Context;
import android.widget.ImageView;

import com.march.common.model.ImageInfo;

import java.util.List;

/**
 * CreateAt : 2018/2/27
 * Describe :
 *
 * @author chendong
 */
public class Gallery {

    public interface GalleryService {

        void loadImg(Context context, String path, int width, int height, ImageView imageView);

        void onSuccess(List<ImageInfo> list);
    }

    private static GalleryService sGalleryService;

    public static void setGalleryService(GalleryService galleryService) {
        sGalleryService = galleryService;
    }

    public static GalleryService getGalleryService() {
        return sGalleryService;
    }
}
