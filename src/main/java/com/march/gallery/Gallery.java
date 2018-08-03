package com.march.gallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.march.common.extensions.ActFragmentMixin;
import com.march.common.extensions.UriX;
import com.march.gallery.common.CommonUtils;
import com.march.gallery.ui.GalleryListActivity;

import java.io.File;
import java.io.IOException;


/**
 * CreateAt : 2018/2/27
 * Describe :
 *
 * @author chendong
 */
public class Gallery {

    public static final String KEY_LIMIT       = "KEY_LIMIT";
    public static final String KEY_CROP        = "KEY_CROP";
    public static final String KEY_ALL_IMGS    = "KEY_ALL_IMGS";
    public static final String KEY_SELECT_IMGS = "KEY_SELECT_IMGS";
    public static final String KEY_INDEX       = "KEY_INDEX";

    public interface GalleryService {

        void loadImg(Context context, String path, int width, int height, ImageView imageView);

        Config getConfig();
    }

    private static GalleryService sGalleryService;

    public static void setGalleryService(GalleryService galleryService) {
        sGalleryService = galleryService;
    }

    public static GalleryService getGalleryService() {
        return sGalleryService;
    }

    public static class Config {
        public int dirSignIcon         = 0;
        public int itemNoSelectIcon    = 0;
        public int previewSelectIcon   = 0;
        public int previewUnSelectIcon = 0;
    }

    public static final int CROP_REQ_CODE           = 1001;
    public static final int SYSTEM_GALLERY_REQ_CODE = 1002;
    public static final int DESIGN_GALLERY_REQ_CODE = 1003;
    public static final int CAPTURE_REQ_CODE        = 1004;


    private static Gallery sInst;


    public static Gallery getInst() {
        if (sInst == null) {
            synchronized (Gallery.class) {
                if (sInst == null) {
                    sInst = new Gallery();
                }
            }
        }
        return sInst;
    }


    private Gallery() {

    }

    /**
     * 使用系统相册获取图片，支持单选
     *
     * @param mixin mixin
     */
    public void chooseImgUseSystemGallery(ActFragmentMixin mixin) {
        Intent intent = new Intent(Intent.ACTION_PICK);// 系统相册action
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        mixin.startActivityForResult(intent, SYSTEM_GALLERY_REQ_CODE);
    }

    /**
     * 使用自定义相册获取图片，支持多选、单选
     *
     * @param mixin mixin
     */
    public void chooseImgUseDesignGallery(ActFragmentMixin mixin, Bundle bundle) {
        Intent intent = new Intent(mixin.getActivity(), GalleryListActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        mixin.startActivityForResult(intent, DESIGN_GALLERY_REQ_CODE);
    }

    /**
     * 使用系统相机拍照，支持单张
     *
     * @param mixin mixin
     * @return 文件
     */
    public File captureImgUseSystemCamera(ActFragmentMixin mixin) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = CommonUtils.generateImageFile(mixin.getContext().getCacheDir(), "capture", "png");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, UriX.fromFile(mixin.getContext(), file));
        mixin.startActivityForResult(intent, CAPTURE_REQ_CODE);
        return file;
    }

    /**
     * 裁剪图片
     *
     * @param mixin mixin
     * @param uri     文件 Uri
     * @param xRatio  x
     * @param yRatio  y
     * @return 文件
     */
    public File cropImg(ActFragmentMixin mixin, Uri uri, float xRatio, float yRatio) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", xRatio);
        intent.putExtra("aspectY", yRatio);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true); // 黑边
        intent.putExtra("noFaceDetection", true); // no face detection
        // outputX,outputY 是剪裁图片的宽高
        // 指定之后会将图片缩放到指定 size
        // intent.putExtra("outputX", width);
        // intent.putExtra("outputY", height);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        // 剪切返回，头像存放的路径
        File file = CommonUtils.generateImageFile(mixin.getContext().getCacheDir(), "crop", "png");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file)); // 专入目标文件
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        mixin.startActivityForResult(intent, CROP_REQ_CODE);
        return file;
    }


}
