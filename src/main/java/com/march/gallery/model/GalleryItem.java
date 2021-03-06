package com.march.gallery.model;

import android.os.Parcel;

import com.march.common.model.ImageInfo;
import com.zfy.adapter.able.Diffable;

import java.util.Set;

/**
 * CreateAt : 2016/10/31
 * Describe : 本地照片信息
 *
 * @author chendong
 */

public class GalleryItem implements Comparable<GalleryItem>, Diffable<GalleryItem> {

    // 设置id为自增长的组件
    private int id;
    // 文件地址
    private String path;
    //0未选中,1选中未插入数据库,||(这边是已经插入数据库的可能状态)2选中插入数据库,3已经上传照片,4完全发布
    private int status;
    // 照片名字
    private String name;
    // 秒数
    private String date;
    private int width;
    private int height;
    private int fileId;

    private boolean selected;


    public GalleryItem(ImageInfo imageInfo) {
        this.id = imageInfo.getId();
        this.path = imageInfo.getPath();
        this.name = imageInfo.getName();
        this.date = imageInfo.getDate();
        this.width = imageInfo.getWidth();
        this.height = imageInfo.getHeight();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GalleryItem)) {
            return false;
        }
        GalleryItem another = (GalleryItem) obj;
        return path.equals(another.path);
    }

    @Override
    public int compareTo(GalleryItem another) {
        try {
            long a = Long.parseLong(date);
            long b = Long.parseLong(another.getDate());
            if (b > a) {
                return 1;
            } else if (b < a) {
                return -1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    public GalleryItem() {
    }

    @Override
    public String toString() {
        return "GalleryImageInfo{" +
                "path='" + path + '\'' +
                ", date='" + date + '\'' +
                ", width=" + width +
                ", height=" + height +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.path);
        dest.writeInt(this.status);
        dest.writeString(this.name);
        dest.writeString(this.date);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeInt(this.fileId);
    }

    public GalleryItem(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.path = in.readString();
        this.status = in.readInt();
        this.name = in.readString();
        this.date = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
        this.fileId = in.readInt();
    }

    public static final Creator<GalleryItem> CREATOR = new Creator<GalleryItem>() {
        @Override
        public GalleryItem createFromParcel(Parcel source) {
            return new GalleryItem(source);
        }

        @Override
        public GalleryItem[] newArray(int size) {
            return new GalleryItem[size];
        }
    };

    @Override
    public boolean areItemsTheSame(GalleryItem newItem) {
        return id == newItem.id;
    }

    @Override
    public boolean areContentsTheSame(GalleryItem newItem) {
        return path.equals(newItem.path);
    }

    @Override
    public Set<String> getChangePayload(GalleryItem newItem) {
        return null;
    }
}
