package com.march.gallery.ui;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.march.common.Common;
import com.march.common.extensions.AppUIMixin;
import com.march.common.extensions.MsgBus;
import com.march.common.utils.CheckUtils;
import com.march.common.utils.ToastUtils;
import com.march.gallery.Gallery;
import com.march.gallery.R;
import com.march.gallery.common.ScanImageTask;
import com.march.gallery.model.GalleryImageInfo;
import com.march.gallery.model.ImageDirInfo;
import com.march.lightadapter.LightAdapter;
import com.march.lightadapter.LightHolder;
import com.march.lightadapter.LightInjector;
import com.march.lightadapter.extend.SelectManager;
import com.march.lightadapter.inject.AdapterConfig;
import com.march.lightadapter.listener.AdapterViewBinder;
import com.march.lightadapter.listener.SimpleItemListener;
import com.march.lightadapter.model.Ids;

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
public class GalleryListFragment extends Fragment {


    private static final String PIC_DATE_PATTERN = "yyyy年M月d日 HH:mm:ss";

    private int mSpanCount = 4;
    private int mItemSize;

    private TextView     mDirTv;
    private TextView     mDateTv;
    private RecyclerView mImageRv;
    private TextView     mTitleLeftTv, mTitleCenterTv, mTitleRightTv;
    private ProgressBar mProgressBar;

    private LightAdapter<GalleryImageInfo> mImageAdapter;
    private GridLayoutManager              mLayoutManager;

    private ImageDirDialog   mDirDialog;
    private SimpleDateFormat mDateFormat;

    private SelectManager<GalleryImageInfo> mSelectManager;
    private ObjectAnimator                  mFadeOutAnim;
    private MyScanImageTask                 mScanImageTask;

    private Map<String, List<GalleryImageInfo>> mImageListMap;
    private int                                 mMaxNum;
    private View                                mBotLy;
    private View                                mPopCoverView;
    private View                                mPreviewTv;
    private MsgBus.Subscriber                   mSubscriber;

    public static GalleryListFragment newInst(Bundle bundle) {
        GalleryListFragment fragment = new GalleryListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    static class MyScanImageTask extends ScanImageTask {

        private WeakReference<GalleryListFragment> mHost;

        MyScanImageTask(GalleryListFragment host) {
            super(host.getContext());
            mHost = new WeakReference<>(host);
        }

        @Override
        public void onScanSuccess(Map<String, List<GalleryImageInfo>> imageListMap, List<ImageDirInfo> mDirInfos, ImageDirInfo dirInfo) {
            GalleryListFragment fragment = mHost.get();
            fragment.mImageListMap = imageListMap;
            fragment.newDirDialog(mDirInfos);
            // fragment.newDirPop(mDirInfos);
            fragment.newDirDialog(mDirInfos);
            fragment.mProgressBar.setVisibility(View.GONE);
            fragment.updateOnChangeDir(dirInfo);
        }
    }

    public void initAfterViewCreated() {
        int WIDTH = getContext().getResources().getDisplayMetrics().widthPixels;
        mItemSize = (int) (WIDTH * 1f / mSpanCount);
        mDateFormat = new SimpleDateFormat(PIC_DATE_PATTERN, Locale.getDefault());
        mTitleRightTv.setText("未选择");
        mTitleRightTv.setTextColor(Color.rgb(76, 76, 76));
        mScanImageTask = new MyScanImageTask(this);
        mScanImageTask.executeOnExecutor(Executors.newCachedThreadPool());
    }

    public void clickPreviewImages() { // 点击预览，只预览选中的
        List<GalleryImageInfo> results = mSelectManager.getResults();
        if (CheckUtils.isEmpty(results)) {
            ToastUtils.show("请先选择照片");
            return;
        }
        previewImages(results, results, 0);
    }

    private void updateTitleRight() {
        if (mSelectManager == null || mSelectManager.size() <= 0) {
            mTitleRightTv.setSelected(false);
            mTitleRightTv.setClickable(false);
            mTitleRightTv.setText("未选择");
            mTitleRightTv.setTextColor(Color.rgb(76, 76, 76));
        } else {
            mTitleRightTv.setText(String.format(Locale.getDefault(), "完成(%d)", mSelectManager.size()));
            mTitleRightTv.setSelected(true);
            mTitleRightTv.setClickable(true);
            mTitleRightTv.setTextColor(getResources().getColor(R.color.gallery_main_color));
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_list_fragment, container, false);
        mSubscriber = MsgBus.getInst().register(Gallery.EVENT_SELECT, (MsgBus.SubscriberInvoker<GalleryImageInfo>) data -> {
            int index = -1;
            for (GalleryImageInfo GalleryImageInfo : mCurrentImages) {
                if (GalleryImageInfo.getPath().equals(data.getPath())) {
                    index = mCurrentImages.indexOf(GalleryImageInfo);
                    break;
                }
            }
            mSelectManager.select(index);
        });
        initCreateView(view);
        initAfterViewCreated();
        return view;
    }

    private boolean isSingleMode() {
        return mMaxNum == 1;
    }

    public void initCreateView(View view) {
        mMaxNum = getArguments().getInt(Gallery.KEY_LIMIT, 1);
        mPopCoverView = view.findViewById(R.id.view_pop_cover);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mTitleLeftTv = (TextView) view.findViewById(R.id.tv_title_left);
        mTitleCenterTv = (TextView) view.findViewById(R.id.tv_title_center);
        mTitleRightTv = (TextView) view.findViewById(R.id.tv_title_right);
        mImageRv = (RecyclerView) view.findViewById(R.id.rv_select_image);
        mDateTv = (TextView) view.findViewById(R.id.tv_time_image);
        mDirTv = (TextView) view.findViewById(R.id.tv_select_image_dir);
        mDateTv.setVisibility(View.GONE);
        mPreviewTv = view.findViewById(R.id.tv_preview);
        mBotLy = view.findViewById(R.id.bot_rl);

        updateTitleRight();

        changeDisplayOnSingleMode();

        mImageRv.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                // 获取第一个位置
                int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItemPosition < 0) {
                    return;
                }
                String date = mCurrentImages.get(firstVisibleItemPosition).getDate();
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

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 选择相册
                if (v.getId() == R.id.rl_select_image_dir) {
                    showDirDialog();
                }
                // 确认按钮
                else if (v.getId() == R.id.tv_title_right) {
                    List<GalleryImageInfo> selectDatas = mSelectManager.getResults();
                    if (selectDatas.size() > 0) {
                        publish(selectDatas);
                    } else {
                        ToastUtils.show("请至少选择一张照片");
                    }
                }
            }
        };

        mBotLy.setOnClickListener(listener);
        mTitleLeftTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        view.findViewById(R.id.rl_select_image_dir).setOnClickListener(listener);
        view.findViewById(R.id.tv_title_right).setOnClickListener(listener);

        view.findViewById(R.id.tv_preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPreviewImages();
            }
        });
    }

    private void changeDisplayOnSingleMode() {
        if (!isSingleMode()) {
            return;
        }
        mTitleRightTv.setVisibility(View.GONE);
        mPreviewTv.setVisibility(View.GONE);
    }


    private void updateOnChangeDir(ImageDirInfo info) {
        if (info == null) {
            return;
        }
        mCurrentImages = mImageListMap.get(info.getDirName());
        mDirTv.setText(info.getDirName());
        mTitleCenterTv.setText(info.getDirName());
        createOrUpdateAdapter(mCurrentImages);
        mImageRv.post(new Runnable() {
            @Override
            public void run() {
                mImageRv.scrollToPosition(0);
            }
        });
    }

    public void createOrUpdateAdapter(List<GalleryImageInfo> GalleryImageInfos) {
        if (CheckUtils.isEmpty(GalleryImageInfos)) {
            return;
        }
        if (mImageAdapter != null) {
            mImageAdapter.update().update(GalleryImageInfos);
            return;
        }

        // 创建adapter
        mImageAdapter = new LightAdapter<GalleryImageInfo>(getContext(), GalleryImageInfos, R.layout.gallery_list_item) {
            @Override
            public void onBindView(LightHolder holder, final GalleryImageInfo data, final int pos, int type) {
                holder.setLayoutParams(mItemSize, mItemSize)
                        .setVisibleGone(R.id.view_single_cover, isSingleMode())
                        .setClick(R.id.view_single_cover, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ArrayList<GalleryImageInfo> list = new ArrayList<>();
                                list.add(data);
                                publish(list);
                            }
                        })
                        .setClick(Ids.all(R.id.view_click_cover, R.id.select_yes_sign_tv, R.id.select_no_sign_iv),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mSelectManager.select(pos);
                                        updateOtherHolder();
                                    }
                                });
                ImageView iv = holder.getView(R.id.image_item_iv);
                Common.getInst().getImgLoadAdapter().loadImg(getContext(), data.getPath(), mItemSize, mItemSize, iv);
            }
        };
        int type = mMaxNum == 1 ? SelectManager.TYPE_SINGLE : SelectManager.TYPE_MULTI;
        mSelectManager = new SelectManager<>(mImageAdapter, type, new AdapterViewBinder<GalleryImageInfo>() {
            @Override
            public void onBindViewHolder(LightHolder holder, GalleryImageInfo data, int pos, int type) {
                bindItemView(holder, data);
            }
        });
        mSelectManager.setSelectListener(new SelectManager.OnSelectListener<GalleryImageInfo>() {
            @Override
            public boolean onBeforeSelect(boolean toSelect, GalleryImageInfo data) {
                if (toSelect) {
                    if (mSelectManager.getResults().size() >= mMaxNum) {
                        ToastUtils.show("最多选择" + mMaxNum + "张");
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void onAfterSelect(boolean toSelect, GalleryImageInfo data) {
                updateTitleRight();
            }
        });
        // 点击监听
        mImageAdapter.setOnItemListener(new SimpleItemListener<GalleryImageInfo>() {
            @Override
            public void onClick(int pos, LightHolder holder, GalleryImageInfo data) {
                if (isSingleMode()) {
                    ArrayList<GalleryImageInfo> list = new ArrayList<>();
                    list.add(data);
                    publish(list);
                    return;
                }
                previewImages(mCurrentImages, mSelectManager.getResults(), pos);
            }
        });
        mLayoutManager = new GridLayoutManager(getContext(), mSpanCount);
        AdapterConfig adapterConfig = AdapterConfig.newConfig().itemLayoutId(R.layout.gallery_list_item);
        LightInjector.initAdapter(mImageAdapter, adapterConfig, mImageRv, mLayoutManager);
    }

    private void updateOtherHolder() {
        // 更新其他的holder
        GalleryImageInfo data;
        TextView tv;
        for (LightHolder holder : mImageAdapter.getHolderSet()) {
            if (holder != null
                    && (tv = holder.getView(R.id.select_yes_sign_tv)) != null
                    && tv.isSelected()) {
                data = mImageAdapter.getRealItem(holder.getLayoutPosition());
                bindItemView(holder, data);
            }
        }
    }

    private void bindItemView(LightHolder holder, GalleryImageInfo data) {
        if (data == null) {
            return;
        }
        if (isSingleMode()) {
            holder.setVisibleGone(R.id.cover_select_image, false)
                    .setVisibleGone(R.id.select_yes_sign_tv, false);
//                    .setVisibleGone(R.id.siv_select_image, false);
            return;
        }
        boolean isSelect = mSelectManager.isSelect(data);
        holder.setVisibleGone(R.id.cover_select_image, isSelect)
                .setSelect(R.id.select_yes_sign_tv, true)
                .setVisibleGone(R.id.select_yes_sign_tv, isSelect)
                .setImage(R.id.select_no_sign_iv, Gallery.getInst().getCfg().itemNoSelectIcon)
                .setVisibleGone(R.id.select_no_sign_iv, !isSelect);
        if (mMaxNum > 1) {
            String selectNum = isSelect ? String.valueOf(mSelectManager.indexOf(data) + 1) : "";
            holder.setText(R.id.select_yes_sign_tv, selectNum);
        }
    }

    private List<GalleryImageInfo> mCurrentImages;

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
            if (data.hasExtra(Gallery.KEY_SELECT_IMGS)) {
                ArrayList<GalleryImageInfo> GalleryImageInfos = data.getParcelableArrayListExtra(Gallery.KEY_SELECT_IMGS);
                if (data.getBooleanExtra(Gallery.KEY_COMPLETE, false)) {
                    publish(GalleryImageInfos);
                }
            }
        }
    }

    private void publish(List<GalleryImageInfo> GalleryImageInfos) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Gallery.KEY_SELECT_IMGS, new ArrayList<>(GalleryImageInfos));
        if (getActivity() != null) {
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }
    }


    private void previewImages(List<GalleryImageInfo> allImages, List<GalleryImageInfo> selectImages, int index) {
        List<GalleryImageInfo> tempAllImgs = new ArrayList<>();
        if (allImages.size() > 100) {
            tempAllImgs.addAll(allImages.subList(Math.max(0, index - 50), Math.min(allImages.size() - 1, index + 50)));
        } else {
            tempAllImgs.addAll(allImages);
        }
        GalleryPreviewActivity.startActivityForResult(new AppUIMixin(this),
                tempAllImgs, selectImages, index, mMaxNum);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        MsgBus.getInst().unRegister(mSubscriber);
    }
}
