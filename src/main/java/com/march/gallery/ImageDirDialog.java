package com.march.gallery;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.widget.ImageView;


import com.march.common.model.ImageInfo;
import com.march.common.utils.DimensUtils;
import com.march.uikit.dialog.BaseDialog;
import com.march.gallery.model.ImageDirInfo;
import com.march.lightadapter.LightAdapter;
import com.march.lightadapter.LightHolder;
import com.march.lightadapter.event.SimpleItemListener;
import com.march.lightadapter.extend.SelectManager;
import com.march.lightadapter.listener.AdapterViewBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * CreateAt : 2017/11/9
 * Describe : 目录弹窗
 *
 * @author luyuan
 */
public class ImageDirDialog extends BaseDialog {

    private OnImageDirClickListener listener;
    private RecyclerView mDirRv;
    private List<ImageDirInfo> mImageDirs;
    private LightAdapter<ImageDirInfo> mDirAdapter;
    private SelectManager<ImageDirInfo> mSelectManager;

    public ImageDirDialog(Context context, List<ImageDirInfo> imageDirs) {
        super(context);
        mImageDirs = imageDirs;
    }


    public interface OnImageDirClickListener {
        void onClickDir(int pos, ImageDirInfo dir);
    }

    public void setListener(OnImageDirClickListener listener) {
        this.listener = listener;
    }

    private void createAdapter() {
        mDirAdapter = new LightAdapter<ImageDirInfo>(getContext(), mImageDirs, R.layout.gallery_dir_item) {

            int size = DimensUtils.dp2px(100);

            @Override
            public void onBindView(LightHolder holder, final ImageDirInfo data, int pos, int type) {
                holder.setText(R.id.tv_dir_name, data.getDirName())
                        .setText(R.id.tv_dir_img_num, String.valueOf(data.getPicNum()))
                        .setCallback(R.id.iv_dir_cover, new LightHolder.Callback<ImageView>() {
                            @Override
                            public void bind(LightHolder holder, ImageView view, int pos) {
                                Gallery.getGalleryService().loadImg(getContext(), data.getCoverInfo().getPath(),
                                        size, size, (ImageView) holder.getView(R.id.iv_dir_cover));
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
                holder.setVisibleInVisible(R.id.iv_dir_sign, mSelectManager.isSelect(data));
            }
        });
        mDirAdapter.bind(this, mDirRv, new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void initViewOnCreate() {
        mDirRv = getView(R.id.rv_image_dir);
        createAdapter();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.gallery_dir_dialog;
    }

    @Override
    protected void setWindowParams() {
        int maxHeight = DimensUtils.dp2px(100) * 4;
        int minHeight = DimensUtils.dp2px(100) * 3;
        int currentHeight = DimensUtils.dp2px(80) * mImageDirs.size();
        int dialogHeight = Math.min(maxHeight, Math.max(minHeight, currentHeight));
        setDialogAttributes(MATCH, dialogHeight, 1.0f, 0.5f, Gravity.BOTTOM);
        setAnimationBottomToCenter();
    }

    @Override
    public void show() {
        super.show();
        mDirRv.scrollToPosition(0);
    }
}
