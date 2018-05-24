package com.march.gallery.list;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.march.common.model.ImageInfo;
import com.march.common.utils.CheckUtils;
import com.march.common.utils.DimensUtils;
import com.march.common.utils.ToastUtils;
import com.march.gallery.Gallery;
import com.march.gallery.ImageDirDialog;
import com.march.gallery.R;
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
import com.march.uikit.annotation.UITitle;
import com.march.uikit.app.BaseActivity;
import com.march.uikit.app.BaseFragment;
import com.march.uikit.widget.SlidingSelectLayout;
import com.march.uikit.widget.TitleView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * CreateAt : 2018/2/27
 * Describe :
 *
 * @author chendong
 */
@UITitle(hasTitle = true)
public class GalleryListFragment extends BaseFragment implements GalleryPreviewFragment.PreviewService {

    private static final String PIC_DATE_PATTERN = "yyyy年M月d日 HH:mm:ss";

    private int mSpanCount = 3;
    private int mItemSize;

    private TextView mDirTv;
    private TextView mDateTv;
    private TextView mEnsureTv;
    private SlidingSelectLayout mSlidingSelectLy;
    private RecyclerView mImageRv;

    private LightAdapter<ImageInfo> mImageAdapter;
    private GridLayoutManager mLayoutManager;

    private ImageDirDialog mDirDialog;
    private SimpleDateFormat mDateFormat;

    private SelectManager<ImageInfo> mSelectManager;
    private ObjectAnimator mFadeOutAnim;
    private MyScanImageTask mScanImageTask;

    private Map<String, List<ImageInfo>> mImageListMap;
    private int mMaxNum;


    public static GalleryListFragment newInst(int limit) {
        GalleryListFragment fragment = new GalleryListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Gallery.KEY_LIMIT, limit);
        fragment.setArguments(bundle);
        return fragment;
    }


    public void addToContainer(BaseActivity activity, int containerId) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(containerId, this, "list")
                .show(this).commit();
    }

    @Override
    public void onPreviewFinish(List<ImageInfo> selectImages) {
        mSelectManager.setSelectDatas(selectImages);
        update();
        mGalleryPreviewFragment = null;
    }

    @Override
    public int getMaxNum() {
        return mMaxNum;
    }


    static class MyScanImageTask extends ScanImageTask {

        private WeakReference<GalleryListFragment> mHost;

        public MyScanImageTask(GalleryListFragment host) {
            super(host.getContext());
            mHost = new WeakReference<>(host);
        }

        @Override
        public void onScanSuccess(Map<String, List<ImageInfo>> imageListMap, List<ImageDirInfo> mDirInfos, ImageDirInfo dirInfo) {
            GalleryListFragment fragment = mHost.get();
            fragment.mImageListMap = imageListMap;
            fragment.newDirDialog(mDirInfos);
            fragment.updateOnChangeDir(dirInfo);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.gallery_list_fragment;
    }

    @Override
    public void initAfterViewCreated() {
        super.initAfterViewCreated();
        mItemSize = (int) (DimensUtils.WIDTH * 1f / mSpanCount);
        mDateFormat = new SimpleDateFormat(PIC_DATE_PATTERN, Locale.getDefault());
        mEnsureTv.setText("未选择");
        mScanImageTask = new MyScanImageTask(this);
        mScanImageTask.execute();

         mViewDelegate.setTitleText(TitleView.RIGHT, "预览");
         mViewDelegate.getTitleView().setListener(TitleView.RIGHT, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击预览，只预览选中的
                List<ImageInfo> results = mSelectManager.getResults();
                if (CheckUtils.isEmpty(results)) {
                    ToastUtils.show("请先选择照片");
                    return;
                }
                previewImages(results, results, 0);
            }
        });
    }


    public void update() {
        if (mImageAdapter != null) {
            mImageAdapter.update().notifyDataSetChanged();
        }
        if (mSelectManager != null) {
            if (mSelectManager.size() <= 0) {
                mEnsureTv.setSelected(false);
                mEnsureTv.setText("未选择");
            } else {
                mEnsureTv.setText("完成(" + mSelectManager.size() + ")");
                mEnsureTv.setSelected(true);
            }
        }
    }

    @Override
    public void initCreateView() {
        super.initCreateView();
        mMaxNum = getData().getInt(Gallery.KEY_LIMIT, 1);
        mImageRv =  mViewDelegate.findView(R.id.rv_select_image);
        mSlidingSelectLy =  mViewDelegate.findView(R.id.sliding);
        mDateTv =  mViewDelegate.findView(R.id.tv_time_image);
        mDirTv =  mViewDelegate.findView(R.id.tv_select_image_dir);
        mEnsureTv =  mViewDelegate.findView(R.id.tv_ensure);

        if (mMaxNum <= 1) {
            mSlidingSelectLy.setEnabled(false);
        }
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

        // 滑动选中
        mSlidingSelectLy.setOnSlidingSelectListener(new SlidingSelectLayout.OnSlidingSelectListener<ImageInfo>() {
            @Override
            public void onSlidingSelect(int pos, View parentView, ImageInfo data) {
                mSelectManager.select(pos);
                updateOtherHolder();
            }
        });

         mViewDelegate.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 选择相册
                if (v.getId() == R.id.tv_select_image_dir) {
                    showDirDialog();
                }
                // 确认按钮
                else if (v.getId() == R.id.tv_ensure) {
                    List<ImageInfo> selectDatas = mSelectManager.getResults();
                    if (selectDatas.size() > 0) {
                        Gallery.getGalleryService().onSuccess(selectDatas);
                    } else {
                        ToastUtils.show("请至少选择一张照片");
                    }
                }
            }
        }, R.id.bot_rl, R.id.tv_select_image_dir, R.id.tv_ensure);
    }


    private void updateOnChangeDir(ImageDirInfo info) {
        if (info == null) {
            return;
        }
        mCurrentImages = mImageListMap.get(info.getDirName());
        mDirTv.setText(info.getDirName());
        createOrUpdateAdapter(mCurrentImages);
        mImageRv.post(new Runnable() {
            @Override
            public void run() {
                mImageRv.scrollToPosition(0);
            }
        });
         mViewDelegate.setTitleCenterText(info.getDirName());
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
        mImageAdapter = new LightAdapter<ImageInfo>(getContext(), imageInfos) {
            @Override
            public void onBindView(LightHolder holder, ImageInfo data, final int pos, int type) {
                mSlidingSelectLy.markView(holder.getItemView(), pos, data);
                holder.setLayoutParams(mItemSize, mItemSize)
                        .setClick(R.id.view_click_cover, new View.OnClickListener() {
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

            @SuppressLint("SetTextI18n")
            @Override
            public void onAfterSelect(boolean toSelect, ImageInfo data) {
                if (mSelectManager.size() <= 0) {
                    mEnsureTv.setSelected(false);
                    mEnsureTv.setText("未选择");
                } else {
                    mEnsureTv.setText("完成(" + mSelectManager.size() + ")");
                    mEnsureTv.setSelected(true);
                }
            }
        });
        // 点击监听
        mImageAdapter.setOnItemListener(new SimpleItemListener<ImageInfo>() {
            @Override
            public void onClick(int pos, LightHolder holder, ImageInfo data) {
                previewImages(mCurrentImages, mSelectManager.getResults(), pos);
            }
        });
        mLayoutManager = new GridLayoutManager(getContext(), mSpanCount);

        LightInjector.initAdapter(mImageAdapter,AdapterConfig.newConfig().itemLayoutId(R.layout.gallery_list_item), mImageRv, mLayoutManager);
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
        boolean isSelect = mSelectManager.isSelect(data);
        holder.setVisibleGone(R.id.cover_select_image, isSelect)
                .setSelect(R.id.tv_select_image, isSelect);
       // if (mMaxNum > 1) {
            String selectNum = isSelect ? String.valueOf(mSelectManager.indexOf(data) + 1) : "";
            holder.setText(R.id.tv_select_image, selectNum);
       // }
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

    // 显示文件夹 dialog
    private void showDirDialog() {
        if (mDirDialog == null) {
            return;
        }
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

    private GalleryPreviewFragment mGalleryPreviewFragment;

    private void previewImages(List<ImageInfo> allImages, List<ImageInfo> selectImages, int index) {
        FragmentTransaction transaction = getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.translate_in, R.anim.translate_out);
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
                    .setCustomAnimations(R.anim.translate_in, R.anim.translate_out)
                    .hide(mGalleryPreviewFragment)
                    .commit();
            return true;
        }
        return false;
    }

}
