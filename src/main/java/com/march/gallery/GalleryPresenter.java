package com.march.gallery;


import android.os.AsyncTask;

import com.march.common.model.ImageInfo;
import com.march.common.utils.LocalImageUtils;
import com.march.uikit.mvp.P.MvpPresenter;
import com.march.uikit.mvp.V.MvpView;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * CreateAt : 2018/2/27
 * Describe :
 *
 * @author chendong
 */
public class GalleryPresenter extends MvpPresenter<GalleryPresenter.GalleryView> {

    public interface GalleryView extends MvpView<GalleryPresenter> {

        void createOrUpdateAdapter(List<ImageInfo> imageInfos);

        void updateNumText(int currentNum, int maxNum);
    }

    public static final String KEY_LIMIT = "KEY_LIMIT";

    private int mMaxNum;
    private Map<String, List<ImageInfo>> mImagesMap; // 目录 － 目录下的图片列表
    private List<ImageInfo> mCurrentImages; // 当前的图片列表

    @Override
    public void onAttachView(GalleryView view) {
        super.onAttachView(view);
        mMaxNum = mView.getData().getInt(KEY_LIMIT, 10);
    }

    @Override
    public void onViewReady() {
        super.onViewReady();
        mView.updateNumText(0, mMaxNum);
        new ScanImageTask(this).execute();
    }

    static class ScanImageTask extends AsyncTask<Void, Void, List<ImageInfo>> {

        private WeakReference<GalleryPresenter> mGalleryPresenterRef;

        public ScanImageTask(GalleryPresenter galleryPresenterRef) {
            mGalleryPresenterRef = new WeakReference<>(galleryPresenterRef);
        }

        @Override
        protected List<ImageInfo> doInBackground(Void... voids) {
            GalleryPresenter presenter = mGalleryPresenterRef.get();
            Map<String, List<ImageInfo>> map = LocalImageUtils.formatImages4EachDir(presenter.mView.getActivity());
            List<ImageInfo> imageInfos = map.get(LocalImageUtils.ALL_IMAGE_KEY);
            presenter.mImagesMap = map;
            presenter.mCurrentImages = imageInfos;
            return imageInfos;
        }

        @Override
        protected void onPostExecute(List<ImageInfo> imageInfos) {
            mGalleryPresenterRef.get().getView().createOrUpdateAdapter(imageInfos);
        }
    }


    // 最大选择数量
    int getMaxNum() {
        return mMaxNum;
    }

    // 当前显示的列表
    List<ImageInfo> getCurrentImages() {
        return mCurrentImages;
    }

    // 全部数据
    Map<String, List<ImageInfo>> getImagesMap() {
        return mImagesMap;
    }

    // 更新当前显示的列表
    void updateCurrentImageList(String key) {
        mCurrentImages = mImagesMap.get(key);
    }
}
