package com.march.gallery.model;


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
    private GalleryItem coverInfo;

    public ImageDirInfo(int picNum, String dirName, GalleryItem coverInfo) {
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

    public GalleryItem getCoverInfo() {
        return coverInfo;
    }

    @Override
    public int compareTo( ImageDirInfo another) {
        return another.picNum - picNum;
    }
}
