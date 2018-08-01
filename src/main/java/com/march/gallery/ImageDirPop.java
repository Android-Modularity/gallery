package com.march.gallery;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.march.common.utils.DimensUtils;
import com.march.gallery.model.ImageDirInfo;
import com.march.lightadapter.LightAdapter;
import com.march.lightadapter.LightHolder;
import com.march.lightadapter.LightInjector;
import com.march.lightadapter.extend.SelectManager;
import com.march.lightadapter.helper.LightManager;
import com.march.lightadapter.inject.AdapterConfig;
import com.march.lightadapter.listener.AdapterViewBinder;
import com.march.lightadapter.listener.SimpleItemListener;

import java.util.List;

/**
 * CreateAt : 2017/11/9
 * Describe : 目录弹窗
 *
 * @author luyuan
 */
public class ImageDirPop extends PopupWindow {

    private OnImageDirClickListener     listener;
    private RecyclerView                mDirRv;
    private List<ImageDirInfo>          mImageDirs;
    private LightAdapter<ImageDirInfo>  mDirAdapter;
    private SelectManager<ImageDirInfo> mSelectManager;
    private Context                     mContext;

    private int width, height;


    public ImageDirPop(Context context, List<ImageDirInfo> imageDirs) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mContext = context;
        mImageDirs = imageDirs;
        View inflate = LayoutInflater.from(context).inflate(R.layout.gallery_dir_dialog, null);
        if (inflate == null) {
            return;
        }
        setContentView(inflate);
        initViews(inflate);
        setAttributes();
    }


    /* 全部参数设置属性 */
    private void setAttributes() {
        // 在PopupWindow里面就加上下面代码，让键盘弹出时，不会挡住pop窗口。
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(20);
        }
        setAnimationStyle(R.style.dialog_anim_bottom_center);
        // 这个设置了可以按返回键dismiss
        setFocusable(true);
        // 可以点击外面dismiss需要一下两个条件
        setBackgroundDrawable(new ColorDrawable());
        setOutsideTouchable(true);
    }

    public interface OnImageDirClickListener {
        void onClickDir(int pos, ImageDirInfo dir);
    }

    public void setListener(OnImageDirClickListener listener) {
        this.listener = listener;
    }

    private void createAdapter() {
        mDirAdapter = new LightAdapter<ImageDirInfo>(mContext, mImageDirs, R.layout.gallery_dir_item) {

            int size = DimensUtils.dp2px(100);

            @Override
            public void onBindView(LightHolder holder, final ImageDirInfo data, int pos, int type) {
                holder.setText(R.id.tv_dir_name, data.getDirName())
                        .setText(R.id.tv_dir_img_num, String.valueOf(data.getPicNum()))
                        .setCallback(R.id.iv_dir_cover, new LightHolder.Callback<ImageView>() {
                            @Override
                            public void bind(LightHolder holder, ImageView view, int pos) {
                                Gallery.getGalleryService().loadImg(getContext(), data.getCoverInfo().getPath(),
                                        size, size, holder.<ImageView>getView(R.id.iv_dir_cover));
                            }
                        });
            }
        };
        mDirAdapter.setOnItemListener(new SimpleItemListener<ImageDirInfo>() {
            @Override
            public void onClick(int pos, LightHolder holder, ImageDirInfo data) {
                mSelectManager.select(pos);
                if (listener != null) {
                    listener.onClickDir(pos, mImageDirs.get(pos));
                }
                dismiss();
            }
        });

        mSelectManager = new SelectManager<>(mDirAdapter, SelectManager.TYPE_SINGLE, new AdapterViewBinder<ImageDirInfo>() {
            @Override
            public void onBindViewHolder(LightHolder holder, ImageDirInfo data, int pos, int type) {
                holder.setVisibleInVisible(R.id.siv_dir_sign, mSelectManager.isSelect(data));
            }
        });
        mSelectManager.initSelect(0);
        AdapterConfig config = AdapterConfig.newConfig().itemLayoutId(R.layout.gallery_dir_item);
        LightInjector.initAdapter(mDirAdapter, config, mDirRv, LightManager.vLinear(getContext()));
    }

    public Context getContext() {
        return mContext;
    }

    public void initViews(View view) {
        width = getContext().getResources().getDisplayMetrics().widthPixels;
        int maxHeight = DimensUtils.dp2px(90) * 4;
        int minHeight = DimensUtils.dp2px(90) * 3;
        int currentHeight = DimensUtils.dp2px(90) * mImageDirs.size();
        int dialogHeight = Math.min(maxHeight, Math.max(minHeight, currentHeight));
        height = dialogHeight;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(mContext.getResources().getDisplayMetrics().widthPixels, dialogHeight);
        } else {
            params.height = dialogHeight;
        }
        view.setLayoutParams(params);
        mDirRv = (RecyclerView) view.findViewById(R.id.rv_image_dir);
        createAdapter();
    }

    public void onShow() {
//        WindowManager.LayoutParams lp = ((Activity) getContext()).getWindow().getAttributes();
//        lp.alpha = 0.6f; // 0.0-1.0
//        ((Activity) getContext()).getWindow().setAttributes(lp);
        mDirRv.scrollToPosition(0);
    }


    /**
     * 根据view来设置位置
     *
     * @param anchor
     * @param xoff
     * @param yoff
     * @param showAlpha
     */
    public void showAsDropDown(View anchor, int xoff, int yoff, float showAlpha) {
        onShow();
        super.showAsDropDown(anchor, xoff, yoff);
    }

    public void showAtLocation(View parent, int gravity, int x, int y, float showAlpha) {
        onShow();
        super.showAtLocation(parent, gravity, x, y);
    }


    @Override
    public void showAsDropDown(View anchor) {
        onShow();
        super.showAsDropDown(anchor);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        onShow();
        super.showAsDropDown(anchor, xoff, yoff, gravity);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        onShow();
        super.showAsDropDown(anchor, xoff, yoff);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        onShow();
        super.showAtLocation(parent, gravity, x, y);
    }

}
