package com.march.gallery.common;

import android.content.Context;
import android.os.AsyncTask;

import com.march.common.model.ImageInfo;
import com.march.common.model.WeakContext;
import com.march.common.utils.CheckUtils;
import com.march.common.utils.LocalImageUtils;
import com.march.gallery.model.ImageDirInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * CreateAt : 2018/3/5
 * Describe :
 *
 * @author chendong
 */
public abstract class ScanImageTask extends AsyncTask<Void, Void, ImageDirInfo> {

    private Map<String, List<ImageInfo>> mImageListMap;
    private List<ImageDirInfo> mDirInfos;
    private WeakContext mWeakContext;


    public ScanImageTask(Context context) {
        mWeakContext = new WeakContext(context);
    }

    @Override
    protected ImageDirInfo doInBackground(Void... voids) {
        mImageListMap = LocalImageUtils.formatImages4EachDir(mWeakContext.get());
        mDirInfos = new ArrayList<>();
        for (String dirName : mImageListMap.keySet()) {
            List<ImageInfo> imageInfoList = mImageListMap.get(dirName);
            if (!CheckUtils.isEmpty(imageInfoList)) {
                mDirInfos.add(new ImageDirInfo(imageInfoList.size(), dirName, imageInfoList.get(0)));
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
    public abstract void onScanSuccess(Map<String, List<ImageInfo>> mImageListMap, List<ImageDirInfo> mDirInfos, ImageDirInfo dirInfo);
}
