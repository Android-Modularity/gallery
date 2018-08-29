package com.march.gallery.adapter;

import com.march.common.model.ImageInfo;

/**
 * CreateAt : 2018/8/3
 * Describe :
 *
 * @author chendong
 */
public interface GalleryAdapter {

    GalleryAdapter EMPTY = new GalleryAdapter() {
        @Override
        public boolean filterImg(ImageInfo info) {
            return true;
        }
    };

    boolean filterImg(ImageInfo info);
}
