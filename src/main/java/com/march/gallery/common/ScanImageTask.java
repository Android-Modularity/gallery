package com.march.gallery.common;

import android.content.Context;
import android.os.AsyncTask;

import com.march.common.exts.EmptyX;
import com.march.common.exts.LocalImageX;
import com.march.common.model.ImageInfo;
import com.march.common.model.WeakContext;
import com.march.gallery.Gallery;
import com.march.gallery.model.GalleryItem;
import com.march.gallery.model.ImageDirInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CreateAt : 2018/3/5
 * Describe :
 *
 * @author chendong
 */
public abstract class ScanImageTask extends AsyncTask<Void, Void, ImageDirInfo> {

    private Map<String, List<GalleryItem>> mImageListMap;
    private List<ImageDirInfo>                  mDirInfos;
    private WeakContext                         mWeakContext;


    public ScanImageTask(Context context) {
        mWeakContext = new WeakContext(context);
    }

    @Override
    protected ImageDirInfo doInBackground(Void... voids) {
        mImageListMap = new HashMap<>();
        Map<String, List<ImageInfo>> imageListMap = LocalImageX.formatImages4EachDir(mWeakContext.get(),"全部");
        List<GalleryItem> list;
        for (Map.Entry<String, List<ImageInfo>> entry : imageListMap.entrySet()) {
            list = new ArrayList<>();
            for (ImageInfo imageInfo : entry.getValue()) {
                if (Gallery.getInst().getGalleryAdapter().filterImg(imageInfo)) {
                    list.add(new GalleryItem(imageInfo));
                }
            }
            mImageListMap.put(entry.getKey(), list);
        }
        mDirInfos = new ArrayList<>();
        for (String dirName : mImageListMap.keySet()) {
            List<GalleryItem> GalleryImageInfoList = mImageListMap.get(dirName);
            if (!EmptyX.isEmpty(GalleryImageInfoList)) {
                mDirInfos.add(new ImageDirInfo(GalleryImageInfoList.size(), dirName, GalleryImageInfoList.get(0)));
            }
        }
        Collections.sort(mDirInfos);
        return EmptyX.isEmpty(mDirInfos) ? null : mDirInfos.get(0);
    }

    @Override
    protected void onPostExecute(ImageDirInfo dirInfo) {
        onScanSuccess(mImageListMap, mDirInfos, dirInfo);
    }

    /**
     * @param mImageListMap 目录-目录下的图片
     * @param mDirInfos     目录列表
     * @param dirInfo       当前选择的目录
     */
    public abstract void onScanSuccess(Map<String, List<GalleryItem>> mImageListMap, List<ImageDirInfo> mDirInfos, ImageDirInfo dirInfo);
}
