package com.march.gallery.common;

import android.content.Context;
import android.os.AsyncTask;

import com.march.common.model.ImageInfo;
import com.march.common.model.WeakContext;
import com.march.common.utils.CheckUtils;
import com.march.common.utils.LocalImageUtils;
import com.march.gallery.model.GalleryImageInfo;
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

    private Map<String, List<GalleryImageInfo>> mImageListMap;
    private List<ImageDirInfo>                  mDirInfos;
    private WeakContext                         mWeakContext;


    public ScanImageTask(Context context) {
        mWeakContext = new WeakContext(context);
    }

    @Override
    protected ImageDirInfo doInBackground(Void... voids) {
        mImageListMap = new HashMap<>();
        Map<String, List<ImageInfo>> imageListMap = LocalImageUtils.formatImages4EachDir(mWeakContext.get());
        List<GalleryImageInfo> list;
        for (Map.Entry<String, List<ImageInfo>> entry : imageListMap.entrySet()) {
            list = new ArrayList<>();
            for (ImageInfo imageInfo : entry.getValue()) {
                list.add(new GalleryImageInfo(imageInfo));
            }
            mImageListMap.put(entry.getKey(), list);
        }
        mDirInfos = new ArrayList<>();
        for (String dirName : mImageListMap.keySet()) {
            List<GalleryImageInfo> GalleryImageInfoList = mImageListMap.get(dirName);
            if (!CheckUtils.isEmpty(GalleryImageInfoList)) {
                mDirInfos.add(new ImageDirInfo(GalleryImageInfoList.size(), dirName, GalleryImageInfoList.get(0)));
            }
        }
        Collections.sort(mDirInfos);
        return CheckUtils.isEmpty(mDirInfos) ? null : mDirInfos.get(0);
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
    public abstract void onScanSuccess(Map<String, List<GalleryImageInfo>> mImageListMap, List<ImageDirInfo> mDirInfos, ImageDirInfo dirInfo);
}
