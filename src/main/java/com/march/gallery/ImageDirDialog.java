package com.march.gallery;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

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
public class ImageDirDialog extends Dialog {

    private OnImageDirClickListener listener;
    private RecyclerView mDirRv;
    private List<ImageDirInfo> mImageDirs;
    private LightAdapter<ImageDirInfo> mDirAdapter;
    private SelectManager<ImageDirInfo> mSelectManager;

    public ImageDirDialog(Context context, List<ImageDirInfo> imageDirs) {
        super(context, R.style.dialog_theme);
        mImageDirs = imageDirs;
        setContentView(R.layout.gallery_dir_dialog);
    }


    /* 全部参数设置属性 */
    private void setDialogAttributes(int width, int height, float alpha, float dim, int gravity) {
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        Window window = getWindow();
        if (window == null)
            return;
        WindowManager.LayoutParams params = window.getAttributes();
        // setContentView设置布局的透明度，0为透明，1为实际颜色,该透明度会使layout里的所有空间都有透明度，不仅仅是布局最底层的view
        params.alpha = alpha;
        // 窗口的背景，0为透明，1为全黑
        params.dimAmount = dim;
        params.width = width;
        params.height = height;
        params.gravity = gravity;
        window.setAttributes(params);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    public interface OnImageDirClickListener {
        void onClickDir(int pos, ImageDirInfo dir);
    }

    public void setListener(OnImageDirClickListener listener) {
        this.listener = listener;
    }

    private void createAdapter() {
        mDirAdapter = new LightAdapter<ImageDirInfo>(getContext(), mImageDirs) {
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
        AdapterConfig config = AdapterConfig.newConfig().itemLayoutId(R.layout.gallery_dir_item);
        LightInjector.initAdapter(mDirAdapter, config, mDirRv, LightManager.vLinear(getContext()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDirRv = (RecyclerView) findViewById(R.id.rv_image_dir);
        createAdapter();

        if (getWindow() != null) {
            getWindow().setWindowAnimations(R.style.dialog_anim_bottom_center);
        }
        int maxHeight = DimensUtils.dp2px(100) * 4;
        int minHeight = DimensUtils.dp2px(100) * 3;
        int currentHeight = DimensUtils.dp2px(80) * mImageDirs.size();
        int dialogHeight = Math.min(maxHeight, Math.max(minHeight, currentHeight));
        setDialogAttributes(ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight, 1.0f, 0.5f, Gravity.BOTTOM);
    }


    @Override
    public void show() {
        super.show();
        mDirRv.scrollToPosition(0);
    }
}
