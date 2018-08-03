package com.march.gallery.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.march.common.extensions.AppUIMixin;
import com.march.common.extensions.ListX;
import com.march.common.extensions.Permission;
import com.march.common.extensions.UriX;
import com.march.common.funcs.Action;
import com.march.common.funcs.Consumer;
import com.march.gallery.model.GalleryImageInfo;
import com.march.common.utils.FileUtils;
import com.march.common.utils.ToastUtils;
import com.march.gallery.Gallery;
import com.march.gallery.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * CreateAt : 2018/8/2
 * Describe :
 *
 * @author chendong
 */
public class EntryDialogFragment extends DialogFragment {

    public static final int REQ_PERMISSION_CODE = 199;

    private AppUIMixin mMixin;
    private Action     mCurAction;
    private File       mCropFile;
    private File       mCaptureFile;
    private boolean    mCrop;
    private int        mMaxNum;

    private Consumer<List<String>> mResultListener;

    public static EntryDialogFragment newInst(int maxNum, boolean crop) {
        EntryDialogFragment entryDialogFragment = new EntryDialogFragment();
        Bundle bundle = new Bundle();
        if (maxNum != 1) {
            // only one pic can crop
            crop = false;
        }
        bundle.putInt(Gallery.KEY_MAX_NUM, maxNum);
        bundle.putBoolean(Gallery.KEY_CROP, crop);
        entryDialogFragment.setArguments(bundle);
        return entryDialogFragment;
    }

    public void setResultListener(Consumer<List<String>> resultListener) {
        mResultListener = resultListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.dialog_theme);
        mMixin = new AppUIMixin(this);
        if (getArguments() != null) {
            mMaxNum = getArguments().getInt(Gallery.KEY_MAX_NUM, 0);
            mCrop = getArguments().getBoolean(Gallery.KEY_CROP, false) && mMaxNum == 1;
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entry_dialog, container, false);
        view.findViewById(R.id.dialog_capture_tv).setOnClickListener(v -> {
            Action captureAction = () -> mCaptureFile = Gallery.getInst().captureImgUseSystemCamera(mMixin);
            if (Permission.requestPermissions(mMixin, REQ_PERMISSION_CODE, Manifest.permission.CAMERA)) {
                captureAction.run();
            } else {
                mCurAction = captureAction;
            }
        });
        view.findViewById(R.id.dialog_gallery_tv).setOnClickListener(v -> {
            Action galleryAction = () -> Gallery.getInst().chooseImgUseDesignGallery(mMixin, getArguments());
            if (Permission.requestPermissions(mMixin, REQ_PERMISSION_CODE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                galleryAction.run();
            } else {
                mCurAction = galleryAction;
            }
        });
        view.findViewById(R.id.dialog_dismiss_tv).setOnClickListener(v -> dismiss());
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (Permission.hasAllPermission(permissions, grantResults)) {
            mCurAction.run();
        }
    }

    private void startCrop(File file) {
        mCropFile = Gallery.getInst().cropImg(mMixin, UriX.fromFile(getActivity(), file), 1, 1);
    }

    private void publishResult(List<String> paths) {
        this.mResultListener.accept(paths);
        dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || getActivity() == null) {
            return;
        }
        // 自定义相册
        if (requestCode == Gallery.DESIGN_GALLERY_REQ_CODE) {
            ArrayList<GalleryImageInfo> GalleryImageInfos = data.getParcelableArrayListExtra(Gallery.KEY_SELECT_IMG);
            if (GalleryImageInfos != null)
                if (mCrop && GalleryImageInfos.size() == 1) {
                    startCrop(new File(GalleryImageInfos.get(0).getPath()));
                } else {
                    // 发布结果
                    publishResult(ListX.map(GalleryImageInfos, GalleryImageInfo::getPath));
                }
            else {
                ToastUtils.show("获取图片失败[-1]～");
            }
        }
        // 相册返回,存放在path路径的文件中
        if (requestCode == Gallery.SYSTEM_GALLERY_REQ_CODE) {
            File systemGalleryImg = findSystemGalleryImg(data);
            if (!FileUtils.isNotExist(systemGalleryImg)) {
                if (mCrop) {
                    startCrop(systemGalleryImg);
                } else {
                    // 发布结果
                    publishResult(ListX.listOf(systemGalleryImg.getAbsolutePath()));
                }
            } else {
                ToastUtils.show("获取图片失败[0]～");
            }
        }
        if (requestCode == Gallery.CAPTURE_REQ_CODE) {
            if (!FileUtils.isNotExist(mCaptureFile)) {
                if (mCrop) {
                    startCrop(mCaptureFile);
                } else {
                    // 发布结果
                    publishResult(ListX.listOf(mCaptureFile.getAbsolutePath()));
                }
            } else {
                ToastUtils.show("获取图片失败[1]~");
            }
        }
        // 裁剪返回
        if (requestCode == Gallery.CROP_REQ_CODE) {
            if (!FileUtils.isNotExist(mCropFile)) {
                // 发布结果
                publishResult(ListX.listOf(mCropFile.getAbsolutePath()));
            } else {
                ToastUtils.show("获取图片失败[2]~");
            }
        }
    }

    private File findSystemGalleryImg(Intent intent) {
        String path = null;
        if (getActivity() == null || intent == null || intent.getData() == null) {
            return null;
        }
        // 获得相册中图片的路径
        if ("file".equals(intent.getData().getScheme())) {
            path = intent.getData().getPath();
        } else if ("content".equals(intent.getData().getScheme())) {
            Uri selectedImage = intent.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            assert cursor != null;
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(filePathColumns[0]));
            }
            cursor.close();
        }
        if (path != null && !FileUtils.isNotExist(path)) {
            return new File(path);
        }
        return null;
    }


    @Override
    public void onStart() {
        super.onStart();
        setDialogAttributes(getDialog());
    }

    /* 全部参数设置属性 */
    private void setDialogAttributes(Dialog dialog) {
        setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            // setContentView设置布局的透明度，0为透明，1为实际颜色,该透明度会使layout里的所有空间都有透明度，不仅仅是布局最底层的view
            params.alpha = 1;
            // 窗口的背景，0为透明，1为全黑
            params.dimAmount = 0.6f;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.BOTTOM;
            window.setAttributes(params);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setWindowAnimations(R.style.dialog_anim_bottom_center);
        }
    }
}
