package com.march.gallery.model;


import android.support.annotation.NonNull;

import com.march.common.model.ImageInfo;


/**
 * Project  : CommonLib
 * Package  : com.march.baselib
 * CreateAt : 16/8/15
 * Describe : 图像目录信息
 *
 * @author chendong
 */
public class ImageDirInfo implements Comparable<ImageDirInfo> {

    private int       picNum;
    private String    dirName;
    private ImageInfo coverInfo;

    public ImageDirInfo(int picNum, String dirName, ImageInfo coverInfo) {
        this.picNum = picNum;
        this.dirName = dirName;
        this.coverInfo = coverInfo;
    }


    public int getPicNum() {
        return picNum;
    }

    public String getDirName() {
        return dirName;
    }

    public ImageInfo getCoverInfo() {
        return coverInfo;
    }

    @Override
    public int compareTo(@NonNull ImageDirInfo another) {
        return another.picNum - picNum;
    }
}
