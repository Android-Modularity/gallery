package com.march.gallery.ui;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.march.common.Common;
import com.march.common.exts.AppUIMixin;
import com.march.common.exts.EmptyX;
import com.march.common.exts.ListX;
import com.march.common.exts.SizeX;
import com.march.common.exts.ToastX;
import com.march.gallery.Gallery;
import com.march.gallery.R;
import com.march.gallery.common.ScanImageTask;
import com.march.gallery.model.GalleryItem;
import com.march.gallery.model.ImageDirInfo;
import com.zfy.adapter.LightAdapter;
import com.zfy.adapter.LightHolder;
import com.zfy.adapter.collections.LightDiffList;
import com.zfy.adapter.common.LightValues;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;


/**
 * CreateAt : 2018/2/27
 * Describe :
 *
 * @author chendong
 */
public class GalleryListFragment extends Fragment implements View.OnClickListener {

    static class MyScanImageTask extends ScanImageTask {

        private WeakReference<GalleryListFragment> mHost;

        MyScanImageTask(GalleryListFragment host) {
            super(host.getContext());
            mHost = new WeakReference<>(host);
        }

        @Override
        public void onScanSuccess(Map<String, List<GalleryItem>> imageListMap, List<ImageDirInfo> mDirInfos, ImageDirInfo dirInfo) {
            GalleryListFragment fragment = mHost.get();
            fragment.mImageListMap = imageListMap;
            fragment.newDirDialog(mDirInfos);
            // fragment.newDirPop(mDirInfos);
            fragment.newDirDialog(mDirInfos);
            fragment.mProgressBar.setVisibility(View.GONE);
            fragment.updateOnChangeDir(dirInfo);
        }
    }

    private static final String PIC_DATE_PATTERN = "yyyy年M月d日 HH:mm:ss";

    public static GalleryListFragment newInst(Bundle bundle) {
        GalleryListFragment fragment = new GalleryListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private int mSpanCount = 4;
    private int mItemSize;

    private TextView     mDirTv;
    private TextView     mDateTv;
    private RecyclerView mImageRv;
    private TextView     mTitleLeftTv, mTitleCenterTv, mTitleRightTv;
    private ProgressBar mProgressBar;

    private LightAdapter<GalleryItem> mImageAdapter;
    private GridLayoutManager              mLayoutManager;

    private ImageDirDialog   mDirDialog;
    private SimpleDateFormat mDateFormat;

    private ObjectAnimator                  mFadeOutAnim;
    private MyScanImageTask                 mScanImageTask;

    private Map<String, List<GalleryItem>> mImageListMap;
    private LightDiffList<GalleryItem> mCurImages;
    private int                                 mMaxNum;
    private View                                mBotLy;
    private View                                mPopCoverView;
    private View                                mPreviewTv;
//    private MsgBus.Subscriber                   mSubscriber;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_list_fragment, container, false);
        init(view);
        return view;
    }


    // 初始化 adapter
    private void initAdapter() {
        mImageAdapter = new LightAdapter<GalleryItem>(getContext(), mCurImages, R.layout.gallery_list_item) {
            @Override
            public void onBindView(LightHolder holder, final GalleryItem data, final int pos) {
                holder
                        .setLayoutParams(mItemSize, mItemSize)
                        .setVisibleGone(R.id.view_single_cover, isSingleMode())
                        .setClick(R.id.view_single_cover, v -> publish(ListX.listOf(data)))
                        .setClick(all(R.id.view_click_cover, R.id.select_yes_sign_tv, R.id.select_no_sign_iv), v -> {
                            mImageAdapter.selector().toggleItem(data);
                        })
                        .setCallback(R.id.image_item_iv, LightHolder.IMAGE, iv -> {
                            Common.exports.imageLoader.loadImg(getContext(), data.getPath(), mItemSize, mItemSize, iv);
                        });
            }
        };
        // 选择器支持
        mImageAdapter.selector().setSelectType(isSingleMode() ? LightValues.SINGLE : LightValues.MULTI);
        mImageAdapter.selector().setSelectorBinder((holder, position, obj, isSelect) -> {
            if (isSingleMode()) {
                holder.setVisibleGone(R.id.cover_select_image, false)
                        .setVisibleGone(R.id.select_yes_sign_tv, false);
            } else {
                holder.setVisibleGone(R.id.cover_select_image, isSelect)
                        .setSelect(R.id.select_yes_sign_tv, true)
                        .setVisibleGone(R.id.select_yes_sign_tv, isSelect)
                        .setImage(R.id.select_no_sign_iv, Gallery.getInst().getCfg().itemNoSelectIcon)
                        .setVisibleGone(R.id.select_no_sign_iv, !isSelect)
                        .setText(R.id.select_yes_sign_tv, String.valueOf(1));
            }
        });
        // 点击事件
        mImageAdapter.setClickCallback((holder, pos, data) -> {
            if (isSingleMode()) {
                publish(ListX.listOf(data));
            } else {
                List<GalleryItem> selectImages = ListX.filter(mImageAdapter.getDatas(), GalleryItem::isSelected);
                previewImages(mCurImages, selectImages, pos);
            }
        });
        mLayoutManager = new GridLayoutManager(getContext(), mSpanCount);
        mImageRv.setLayoutManager(mLayoutManager);
        mImageRv.setAdapter(mImageAdapter);
    }


    private void updateTitleRight() {
        int selectSize = ListX.count(mCurImages, GalleryItem::isSelected);
        if (selectSize == 0) {
            mTitleRightTv.setSelected(false);
            mTitleRightTv.setClickable(false);
            mTitleRightTv.setText("未选择");
            mTitleRightTv.setTextColor(Color.rgb(76, 76, 76));
        } else {
            mTitleRightTv.setText(String.format(Locale.getDefault(), "完成(%d)", selectSize));
            mTitleRightTv.setSelected(true);
            mTitleRightTv.setClickable(true);
            mTitleRightTv.setTextColor(getResources().getColor(R.color.gallery_main_color));
        }
    }


    private boolean isSingleMode() {
        return mMaxNum == 1;
    }

    public void init(View view) {
        if (getArguments() != null) {
            mMaxNum = getArguments().getInt(Gallery.KEY_MAX_NUM, 1);
        }
        mCurImages = new LightDiffList<>();
        mItemSize = (int) (SizeX.WIDTH * 1f / mSpanCount);
        mDateFormat = new SimpleDateFormat(PIC_DATE_PATTERN, Locale.getDefault());

        mPopCoverView = view.findViewById(R.id.view_pop_cover);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mTitleLeftTv = view.findViewById(R.id.tv_title_left);
        mTitleCenterTv = view.findViewById(R.id.tv_title_center);
        mTitleRightTv = view.findViewById(R.id.tv_title_right);
        mImageRv = view.findViewById(R.id.rv_select_image);
        mDateTv = view.findViewById(R.id.tv_time_image);
        mDirTv = view.findViewById(R.id.tv_select_image_dir);
        mDateTv.setVisibility(View.GONE);
        mPreviewTv = view.findViewById(R.id.tv_preview);
        mBotLy = view.findViewById(R.id.bot_rl);

        if (isSingleMode()) {
            mTitleRightTv.setVisibility(View.GONE);
            mPreviewTv.setVisibility(View.GONE);
        }

        initAdapter();

        updateTitleRight();

        mImageRv.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                // 获取第一个位置
                int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItemPosition < 0) {
                    return;
                }
                String date = mCurImages.get(firstVisibleItemPosition).getDate();
                mDateTv.setText(mDateFormat.format(Long.parseLong(date) * 1000));
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {

            }
        });

        // 滑动时显示时间条
        mImageRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    mDateTv.setAlpha(1f);
                    stopFadeOutAnim();
                    mFadeOutAnim = ObjectAnimator.ofFloat(mDateTv, "alpha", 1f, 0f).setDuration(1500);
                    mFadeOutAnim.start();
                } else {
                    stopFadeOutAnim();
                    mDateTv.clearAnimation();
                    mDateTv.setAlpha(1f);
                }
            }
        });


        mBotLy.setOnClickListener(this);
        mTitleLeftTv.setOnClickListener(this);
        view.findViewById(R.id.rl_select_image_dir).setOnClickListener(this);
        view.findViewById(R.id.tv_title_right).setOnClickListener(this);

        view.findViewById(R.id.tv_preview).setOnClickListener(v -> {
            List<GalleryItem> selectImages = ListX.filter(mImageAdapter.getDatas(), GalleryItem::isSelected);
            if (EmptyX.isEmpty(selectImages)) {
                ToastX.show("请先选择照片");
                return;
            }
            previewImages(selectImages, selectImages, 0);
        });

        mTitleRightTv.setText("未选择");
        mTitleRightTv.setTextColor(Color.rgb(76, 76, 76));
        mScanImageTask = new MyScanImageTask(this);
        mScanImageTask.executeOnExecutor(Executors.newCachedThreadPool());


    }

    private void updateOnChangeDir(ImageDirInfo info) {
        if (info == null) {
            return;
        }
        mDirTv.setText(info.getDirName());
        mTitleCenterTv.setText(info.getDirName());
        mCurImages.update(mImageListMap.get(info.getDirName()));
        mImageRv.post(() -> mImageRv.scrollToPosition(0));
    }



    private void newDirDialog(List<ImageDirInfo> imageDirInfos) {
        mDirDialog = new ImageDirDialog(getActivity(), imageDirInfos);
        mDirDialog.setListener(new ImageDirDialog.OnImageDirClickListener() {
            @Override
            public void onClickDir(int pos, ImageDirInfo dir) {
                updateOnChangeDir(dir);
            }
        });
    }


    // 显示文件夹 dialog
    private void showDirDialog() {
        if (mDirDialog == null) {
            return;
        }
        mPopCoverView.setVisibility(View.GONE);
        mDirDialog.show();

    }

    private void stopFadeOutAnim() {
        if (mFadeOutAnim != null) {
            if (mFadeOutAnim.isRunning()) {
                mFadeOutAnim.cancel();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopFadeOutAnim();
        if (mFadeOutAnim != null) {
            mFadeOutAnim.setTarget(null);
        }
        if (mScanImageTask != null) {
            mScanImageTask.cancel(true);
            mScanImageTask = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data.hasExtra(Gallery.KEY_SELECT_IMG)) {
                ArrayList<GalleryItem> GalleryImageInfos = data.getParcelableArrayListExtra(Gallery.KEY_SELECT_IMG);
                if (data.getBooleanExtra(Gallery.KEY_COMPLETE, false)) {
                    publish(GalleryImageInfos);
                }
            }
        }
    }

    private void publish(List<GalleryItem> GalleryImageInfos) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Gallery.KEY_SELECT_IMG, new ArrayList<>(GalleryImageInfos));
        if (getActivity() != null) {
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }
    }


    private void previewImages(List<GalleryItem> allImages, List<GalleryItem> selectImages, int index) {
        List<GalleryItem> tempAllImgs = new ArrayList<>();
        if (allImages.size() > 100) {
            tempAllImgs.addAll(allImages.subList(Math.max(0, index - 50), Math.min(allImages.size() - 1, index + 50)));
        } else {
            tempAllImgs.addAll(allImages);
        }
        GalleryPreviewActivity.startActivityForResult(AppUIMixin.from(this),
                tempAllImgs, selectImages, index, mMaxNum);
    }


    @Override
    public void onDetach() {
        super.onDetach();
//        MsgBus.getInst().unRegister(mSubscriber);
    }


    @Override
    public void onClick(View v) {
        // 选择相册
        if (v.getId() == R.id.rl_select_image_dir) {
            showDirDialog();
        }
        // 确认按钮
        else if (v.getId() == R.id.tv_title_right) {
            List<GalleryItem> selectImages = ListX.filter(mImageAdapter.getDatas(), GalleryItem::isSelected);
            if (selectImages.size() > 0) {
                publish(selectImages);
            } else {
                ToastX.show("请至少选择一张照片");
            }
        }
        // 返回
        else if (v.getId() == R.id.tv_title_left) {
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }
}
