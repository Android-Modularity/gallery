package com.march.gallery;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.march.common.utils.CheckUtils;
import com.march.uikit.app.common.ViewConfig;
import com.march.common.model.ImageInfo;
import com.march.common.utils.ActivityAnimUtils;
import com.march.common.utils.DimensUtils;
import com.march.common.utils.DrawableUtils;
import com.march.common.utils.LocalImageUtils;
import com.march.common.utils.ToastUtils;
import com.march.common.utils.ViewUtils;
import com.march.gallery.model.ImageDirInfo;
import com.march.lightadapter.LightAdapter;
import com.march.lightadapter.LightHolder;
import com.march.lightadapter.event.SimpleItemListener;
import com.march.lightadapter.extend.SelectManager;
import com.march.lightadapter.listener.AdapterViewBinder;
import com.march.uikit.mvp.V.impl.MvpFragment;
import com.march.uikit.mvp.factory.BindPresenter;
import com.march.uikit.widget.SlidingSelectLayout;
import com.march.uikit.widget.TitleView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


/**
 * CreateAt : 2018/2/27
 * Describe :
 *
 * @author chendong
 */
@BindPresenter(GalleryPresenter.class)
public class GalleryFragment
        extends MvpFragment<GalleryPresenter.GalleryView, GalleryPresenter>
        implements GalleryPresenter.GalleryView {

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
    private ObjectAnimator mAlphaAnimator;

    private ImageDirDialog mDirDialog;
    private SimpleDateFormat mDateFormat;

    private SelectManager<ImageInfo> mSelectManager;

    @Override
    public int getLayoutId() {
        return R.layout.gallery_fragment;
    }

    @Override
    public ViewConfig getViewConfig() {
        return super.getViewConfig().setWithTitle(true);
    }

    @Override
    public void initAfterViewCreated() {
        mItemSize = (int) (DimensUtils.WIDTH * 1f / mSpanCount);
        mAlphaAnimator = ObjectAnimator.ofFloat(mDateTv, "alpha", 1.0f, 0f).setDuration(1500);
        mDateFormat = new SimpleDateFormat(PIC_DATE_PATTERN, Locale.getDefault());

        getTitleView().setText("返回", LocalImageUtils.ALL_IMAGE_KEY, null);
        getTitleView().setLeftBackListener(getActivity());

        findViews();
        initEvents();

        updateNumText(0, getPresenter().getMaxNum());
    }

    // find view
    private void findViews() {
        mImageRv = getView(R.id.rv_select_image);
        mSlidingSelectLy = getView(R.id.sliding);
        mDateTv = getView(R.id.tv_time_image);
        mDirTv = getView(R.id.tv_select_image_dir);
        mEnsureTv = getView(R.id.tv_ensure);
    }

    // init event
    public void initEvents() {
        mImageRv.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                // 获取第一个位置
                int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItemPosition < 0) {
                    return;
                }
                String date = getPresenter().getCurrentImages().get(firstVisibleItemPosition).getDate();
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
                ViewUtils.setVisibility(mDateTv, newState == RecyclerView.SCROLL_STATE_IDLE ? View.GONE : View.VISIBLE);
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

        // 确认按钮
        mEnsureTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ImageInfo> selectDatas = mSelectManager.getResults();
                if (selectDatas.size() > 0) {
                    Gallery.getGalleryService().onSuccess(selectDatas);
                    getActivity().finish();
                    ActivityAnimUtils.translateFinish(getActivity());
                } else {
                    ToastUtils.show("请至少选择一张照片");
                }
            }
        });
        // 文件夹按钮
        getView(R.id.tv_select_image_dir).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDirDialog();
            }
        });
        // 截断事件
        getView(R.id.bot_rl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void updateNumText(int currentNum, int maxNum) {
        mEnsureTv.setText("完成" + (currentNum == 0 ? "" : "(" + currentNum + ")"));
    }

    @Override
    public void createOrUpdateAdapter(List<ImageInfo> imageInfos) {
        if (CheckUtils.isEmpty(imageInfos)) {
            return;
        }
        if (mImageAdapter != null) {
            mImageAdapter.update().update(imageInfos);
            return;
        }
        // 创建adapter
        mImageAdapter = new LightAdapter<ImageInfo>(getContext(), imageInfos, R.layout.gallery_item) {
            @Override
            public void onBindView(LightHolder holder, ImageInfo data, int pos, int type) {
                mSlidingSelectLy.markView(holder.getItemView(), pos, data);
                ImageView iv = holder.getView(R.id.iv_select_image);
                holder.setLayoutParams(mItemSize, mItemSize);
                Gallery.getGalleryService().loadImg(getContext(), data.getPath(), mItemSize, mItemSize, iv);
            }
        };
        mSelectManager = new SelectManager<>(mImageAdapter, SelectManager.TYPE_MULTI, new AdapterViewBinder<ImageInfo>() {
            @Override
            public void onBindViewHolder(LightHolder holder, ImageInfo data, int pos, int type) {
                bindItemView(holder, data);
                updateNumText(mSelectManager.size(), getPresenter().getMaxNum());
            }
        });
        mSelectManager.setCheckSelectListener(new SelectManager.CheckSelectListener<ImageInfo>() {
            @Override
            public boolean onSelect(boolean toSelect, ImageInfo data) {
                if (toSelect) {
                    if (mSelectManager.getResults().size() >= getPresenter().getMaxNum()) {
                        ToastUtils.show("最多选择" + getPresenter().getMaxNum() + "张");
                        return false;
                    }
                }
                return true;
            }
        });
        // 点击监听
        mImageAdapter.setOnItemListener(new SimpleItemListener<ImageInfo>() {
            @Override
            public void onClick(int pos, LightHolder holder, ImageInfo data) {
                mSelectManager.select(pos);
                updateOtherHolder();
            }
        });
        mLayoutManager = new GridLayoutManager(getContext(), mSpanCount);
        mImageAdapter.bind(this, mImageRv, mLayoutManager);
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
        String selectNum = isSelect ? String.valueOf(mSelectManager.indexOf(data) + 1) : "";
        holder.setVisibleGone(R.id.cover_select_image, isSelect)
                .setSelect(R.id.tv_select_image, isSelect)
                .setText(R.id.tv_select_image, selectNum);
    }


    // 显示文件夹 dialog
    private void showDirDialog() {
        if (mDirDialog != null) {
            mDirDialog.show();
            return;
        }
        mDirDialog = new ImageDirDialog(getActivity(), getPresenter().getImagesMap());
        mDirDialog.setListener(new ImageDirDialog.OnImageDirClickListener() {
            @Override
            public void onClickDir(int pos, ImageDirInfo dir) {
                getPresenter().updateCurrentImageList(dir.getDirName());
                getTitleView().setText(TitleView.CENTER, dir.getDirName());
                mDirTv.setText(dir.getDirName());
                mImageAdapter.update().update(getPresenter().getCurrentImages());
                mImageRv.post(new Runnable() {
                    @Override
                    public void run() {
                        mImageRv.scrollToPosition(0);
                    }
                });
            }
        });
        mDirDialog.show();
    }

}
