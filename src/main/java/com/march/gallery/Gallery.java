package com.march.gallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.march.common.extensions.AppUIMixin;
import com.march.common.extensions.UriX;
import com.march.gallery.adapter.GalleryAdapter;
import com.march.gallery.ui.GalleryListActivity;

import java.io.File;
import java.util.UUID;


/**
 * CreateAt : 2018/2/27
 * Describe :
 *
 * @author chendong
 */
public class Gallery {

    public static final String KEY_MAX_NUM    = "KEY_MAX_NUM";
    public static final String KEY_CROP       = "KEY_CROP";
    public static final String KEY_ALL_IMG    = "KEY_ALL_IMG";
    public static final String KEY_SELECT_IMG = "KEY_SELECT_IMG";
    public static final String KEY_INDEX      = "KEY_INDEX";
    public static final String KEY_COMPLETE   = "KEY_COMPLETE";
    public static final String EVENT_SELECT   = "EVENT_SELECT";


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

    private GalleryCfg mCfg;
    private GalleryAdapter mGalleryAdapter = GalleryAdapter.EMPTY;

    public GalleryAdapter getGalleryAdapter() {
        return mGalleryAdapter;
    }

    public void setGalleryAdapter(GalleryAdapter galleryAdapter) {
        mGalleryAdapter = galleryAdapter;
    }

    public GalleryCfg getCfg() {
        return mCfg;
    }

    public static void init(GalleryCfg cfg) {
        getInst().mCfg = cfg;
    }
    /**
     * 使用系统相册获取图片，支持单选
     *
     * @param mixin mixin
     */
    public void chooseImgUseSystemGallery(AppUIMixin mixin) {
        Intent intent = new Intent(Intent.ACTION_PICK);// 系统相册action
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        mixin.startActivityForResult(intent, SYSTEM_GALLERY_REQ_CODE);
    }

    /**
     * 使用自定义相册获取图片，支持多选、单选
     *
     * @param mixin mixin
     */
    public void chooseImgUseDesignGallery(AppUIMixin mixin, Bundle bundle) {
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
    public File captureImgUseSystemCamera(AppUIMixin mixin) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = generateImageFile(mixin.getContext().getCacheDir(), "capture", "png");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, UriX.fromFile(mixin.getContext(), file));
        mixin.startActivityForResult(intent, CAPTURE_REQ_CODE);
        return file;
    }

    public static File generateImageFile(File dir, String sign, String suffix) {
        // 通过uuid生成照片唯一名字
        String mOutFileName = UUID.randomUUID().toString() + "_" + sign + "_image." + suffix;
        return new File(dir, mOutFileName);
    }

    /**
     * 裁剪图片
     *
     * @param mixin mixin
     * @param originUri     文件 Uri
     * @param xRatio  x
     * @param yRatio  y
     * @return 文件
     */
    public File cropImg(AppUIMixin mixin, Uri originUri, float xRatio, float yRatio) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(originUri, "image/*");
        UriX.grantUriPermission(mixin.getContext(), intent, originUri);
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
        File file = generateImageFile(mixin.getContext().getCacheDir(), "crop", "png");
        Uri outputUri = UriX.fromFile(mixin.getContext(), file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri); // 专入目标文件
        UriX.grantUriPermission(mixin.getContext(), intent, outputUri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        mixin.startActivityForResult(intent, CROP_REQ_CODE);
        return file;
    }


}
