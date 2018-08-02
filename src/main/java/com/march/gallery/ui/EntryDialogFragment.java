package com.march.gallery.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.march.common.extensions.ActFragmentMixin;
import com.march.common.extensions.Permission;
import com.march.common.funcs.Action;
import com.march.common.utils.ToastUtils;
import com.march.gallery.Gallery;
import com.march.gallery.R;

/**
 * CreateAt : 2018/8/2
 * Describe :
 *
 * @author chendong
 */
public class EntryDialogFragment extends DialogFragment {

    public static final int REQ_PERMISSION_CODE = 199;

    private ActFragmentMixin mMixin;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.dialog_theme);
        mMixin = new ActFragmentMixin(this);
    }

    private Action mCurAction;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mCurAction != null && Permission.hasAllPermission(permissions, grantResults)) {
            // mCurAction.run();
            ToastUtils.show("获得权限执行");
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entry_dialog, container, false);
        view.findViewById(R.id.dialog_capture_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurAction = new Action() {
                    @Override
                    public void run() {
                        Gallery.getInst().captureImgUseSystemCamera(mMixin);
                    }
                };
                Permission.requestPermissions(getActivity(),
                        mMixin, REQ_PERMISSION_CODE,
                        mCurAction, Manifest.permission.CAMERA);
                dismiss();
            }
        });
        view.findViewById(R.id.dialog_gallery_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurAction = new Action() {
                    @Override
                    public void run() {
                        Gallery.getInst().chooseImgUseDesignGallery(mMixin, getArguments());
                    }
                };
                Permission.requestPermissions(getActivity(),
                        mMixin, REQ_PERMISSION_CODE,
                        mCurAction, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                dismiss();
            }
        });
        view.findViewById(R.id.dialog_dismiss_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
