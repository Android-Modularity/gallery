package com.march.gallery.list;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.march.common.model.ImageInfo;
import com.march.common.utils.CheckUtils;
import com.march.common.utils.ToastUtils;
import com.march.gallery.Gallery;
import com.march.gallery.ImageDirDialog;
import com.march.gallery.ImageDirPop;
import com.march.gallery.R;
import com.march.gallery.common.CommonUtils;
import com.march.gallery.common.ScanImageTask;
import com.march.gallery.model.ImageDirInfo;
import com.march.gallery.preview.GalleryPreviewFragment;
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
public class GalleryListFragment extends Fragment implements GalleryPreviewFragment.PreviewService {

    private static final String PIC_DATE_PATTERN = "yyyy年M月d日 HH:mm:ss";

    private int mSpanCount = 4;
    private int mItemSize;

    private TextView     mDirTv;
    private TextView     mDateTv;
    private RecyclerView mImageRv;
    private TextView     mTitleLeftTv, mTitleCenterTv, mTitleRightTv;
    private ProgressBar mProgressBar;

    private LightAdapter<ImageInfo> mImageAdapter;
    private GridLayoutManager mLayoutManager;

    private ImageDirDialog   mDirDialog;
    private ImageDirPop      mImageDirPop;
    private SimpleDateFormat mDateFormat;

    private SelectManager<ImageInfo> mSelectManager;
    private ObjectAnimator mFadeOutAnim;
    private MyScanImageTask mScanImageTask;

    private Map<String, List<ImageInfo>> mImageListMap;
    private int                          mMaxNum;
    private View                         mBotLy;
    private View                         mPopCoverView;
    private View                         mPreviewTv;


    public static GalleryListFragment newInst(int limit) {
        GalleryListFragment fragment = new GalleryListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Gallery.KEY_LIMIT, limit);
        fragment.setArguments(bundle);
        return fragment;
    }


    public void addToContainer(FragmentActivity activity, int containerId) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(containerId, this, "list")
                .show(this)
                .commit();
    }

    @Override
    public void onPreviewFinish(List<ImageInfo> selectImages) {
        mSelectManager.setSelectDatas(selectImages);
        updateOnResume();
    }

    @Override
    public int getMaxNum() {
        return mMaxNum;
    }


    static class MyScanImageTask extends ScanImageTask {

        private WeakReference<GalleryListFragment> mHost;

        MyScanImageTask(GalleryListFragment host) {
            super(host.getContext());
            mHost = new WeakReference<>(host);
        }

        @Override
        public void onScanSuccess(Map<String, List<ImageInfo>> imageListMap, List<ImageDirInfo> mDirInfos, ImageDirInfo dirInfo) {
            GalleryListFragment fragment = mHost.get();
            fragment.mImageListMap = imageListMap;
            fragment.newDirDialog(mDirInfos);
            fragment.newDirPop(mDirInfos);
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
        List<ImageInfo> results = mSelectManager.getResults();
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
            mTitleRightTv.setText("完成(" + mSelectManager.size() + ")");
            mTitleRightTv.setSelected(true);
            mTitleRightTv.setClickable(true);

            mTitleRightTv.setTextColor(Color.rgb(249, 52, 80));
        }
    }

    public void updateOnResume() {
        if (mImageAdapter != null) {
            mImageAdapter.update().notifyDataSetChanged();
        }
        updateTitleRight();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_list_fragment, container, false);
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

//        mImageRv.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
//            @Override
//            public void onChildViewAttachedToWindow(View view) {
//                // 获取第一个位置
//                int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();
//                if (firstVisibleItemPosition < 0) {
//                    return;
//                }
//                String date = mCurrentImages.get(firstVisibleItemPosition).getDate();
//                mDateTv.setText(mDateFormat.format(Long.parseLong(date) * 1000));
//            }
//
//            @Override
//            public void onChildViewDetachedFromWindow(View view) {
//
//            }
//        });

        // 滑动时显示时间条
//        mImageRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
//                    mDateTv.setAlpha(1f);
//                    stopFadeOutAnim();
//                    mFadeOutAnim = ObjectAnimator.ofFloat(mDateTv, "alpha", 1f, 0f).setDuration(1500);
//                    mFadeOutAnim.start();
//                } else {
//                    stopFadeOutAnim();
//                    mDateTv.clearAnimation();
//                    mDateTv.setAlpha(1f);
//                }
//            }
//        });

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 选择相册
                if (v.getId() == R.id.rl_select_image_dir) {
                    showDirDialog();
                }
                // 确认按钮
                else if (v.getId() == R.id.tv_title_right) {
                    List<ImageInfo> selectDatas = mSelectManager.getResults();
                    if (selectDatas.size() > 0) {
                        Gallery.getGalleryService().onSuccess(selectDatas);
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

    public void createOrUpdateAdapter(List<ImageInfo> imageInfos) {
        if (CheckUtils.isEmpty(imageInfos)) {
            return;
        }
        if (mImageAdapter != null) {
            mImageAdapter.update().update(imageInfos);
            return;
        }

        // 创建adapter
        mImageAdapter = new LightAdapter<ImageInfo>(getContext(), imageInfos, R.layout.gallery_list_item) {
            @Override
            public void onBindView(LightHolder holder, final ImageInfo data, final int pos, int type) {
                holder.setLayoutParams(mItemSize, mItemSize)
                        .setVisibleGone(R.id.view_single_cover, isSingleMode())
                        .setClick(R.id.view_single_cover, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ArrayList<ImageInfo> list = new ArrayList<>();
                                list.add(data);
                                Gallery.getGalleryService().onSuccess(list);
                            }
                        })
                        .setClick(Ids.all(R.id.view_click_cover, R.id.tv_select_image, R.id.siv_select_image),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mSelectManager.select(pos);
                                        updateOtherHolder();
                                    }
                                });
                ImageView iv = holder.getView(R.id.iv_select_image);
                Gallery.getGalleryService().loadImg(getContext(), data.getPath(), mItemSize, mItemSize, iv);
            }
        };
        int type = mMaxNum == 1 ? SelectManager.TYPE_SINGLE : SelectManager.TYPE_MULTI;
        mSelectManager = new SelectManager<>(mImageAdapter, type, new AdapterViewBinder<ImageInfo>() {
            @Override
            public void onBindViewHolder(LightHolder holder, ImageInfo data, int pos, int type) {
                bindItemView(holder, data);
            }
        });
        mSelectManager.setSelectListener(new SelectManager.OnSelectListener<ImageInfo>() {
            @Override
            public boolean onBeforeSelect(boolean toSelect, ImageInfo data) {
                if (toSelect) {
                    if (mSelectManager.getResults().size() >= mMaxNum) {
                        ToastUtils.show("最多选择" + mMaxNum + "张");
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void onAfterSelect(boolean toSelect, ImageInfo data) {
                updateTitleRight();
            }
        });
        // 点击监听
        mImageAdapter.setOnItemListener(new SimpleItemListener<ImageInfo>() {
            @Override
            public void onClick(int pos, LightHolder holder, ImageInfo data) {
                if (isSingleMode()) {
                    ArrayList<ImageInfo> list = new ArrayList<>();
                    list.add(data);
                    Gallery.getGalleryService().onSuccess(list);
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
        ImageInfo data;
        TextView tv;
        for (LightHolder holder : mImageAdapter.getHolderSet()) {
            if (holder != null
                    && (tv = holder.getView(R.id.tv_select_image)) != null
                    && tv.isSelected()) {
                data = mImageAdapter.getRealItem(holder.getLayoutPosition());
                bindItemView(holder, data);
            }
        }
    }

    private void bindItemView(LightHolder holder, ImageInfo data) {
        if (data == null) {
            return;
        }
        if (isSingleMode()) {
            holder.setVisibleGone(R.id.cover_select_image, false)
                    .setVisibleGone(R.id.tv_select_image, false);
//                    .setVisibleGone(R.id.siv_select_image, false);
            return;
        }
        boolean isSelect = mSelectManager.isSelect(data);
        holder.setVisibleGone(R.id.cover_select_image, isSelect)
                .setSelect(R.id.tv_select_image, true)
                .setVisibleGone(R.id.tv_select_image, isSelect)
                .setVisibleGone(R.id.siv_select_image, !isSelect);
        if (mMaxNum > 1) {
            String selectNum = isSelect ? String.valueOf(mSelectManager.indexOf(data) + 1) : "";
            holder.setText(R.id.tv_select_image, selectNum);
        }
    }

    private List<ImageInfo> mCurrentImages;

    private void newDirDialog(List<ImageDirInfo> imageDirInfos) {
        mDirDialog = new ImageDirDialog(getActivity(), imageDirInfos);
        mDirDialog.setListener(new ImageDirDialog.OnImageDirClickListener() {
            @Override
            public void onClickDir(int pos, ImageDirInfo dir) {
                updateOnChangeDir(dir);
            }
        });
    }

    private void newDirPop(List<ImageDirInfo> imageDirInfos) {
        mImageDirPop = new ImageDirPop(getActivity(), imageDirInfos);
        mImageDirPop.setListener(new ImageDirPop.OnImageDirClickListener() {
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

//        mPopCoverView.setAlpha(1);
//        if (mImageDirPop == null) {
//            return;
//        }
//        mImageDirPop.showAtLocation(mBotLy, Gravity.BOTTOM, 0, CommonUtils.dp2px(getContext(), 45));
//        mImageDirPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                mPopCoverView.setAlpha(0);
//            }
//        });
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

    private GalleryPreviewFragment mGalleryPreviewFragment;

    private void previewImages(List<ImageInfo> allImages, List<ImageInfo> selectImages, int index) {
        FragmentTransaction transaction = getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.act_translate_in, R.anim.act_translate_out);
        mGalleryPreviewFragment = new GalleryPreviewFragment();
        mGalleryPreviewFragment.setPreviewService(this);
        transaction.add(Gallery.getGalleryService().getPreviewContainerId(), mGalleryPreviewFragment, "preview");
        transaction.show(mGalleryPreviewFragment).commit();
        mGalleryPreviewFragment.update(allImages, selectImages, index);
    }


    public boolean onBackPressed() {
        if (mGalleryPreviewFragment != null && !mGalleryPreviewFragment.isHidden()) {
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.act_translate_in, R.anim.act_translate_out)
                    .hide(mGalleryPreviewFragment)
                    .remove(mGalleryPreviewFragment)
                    .commit();
            return true;
        }
        return false;
    }

}
