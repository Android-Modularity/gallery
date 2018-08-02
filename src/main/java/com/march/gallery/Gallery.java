package com.march.gallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.march.common.model.ImageInfo;
import com.march.gallery.common.CommonUtils;
import com.march.gallery.ui.GalleryListActivity;

import java.io.File;
import java.util.List;


/**
 * CreateAt : 2018/2/27
 * Describe :
 *
 * @author chendong
 */
public class Gallery {

    public static final String KEY_LIMIT       = "KEY_LIMIT";
    public static final String KEY_LIST        = "KEY_LIST";
    public static final String KEY_ALL_IMGS    = "KEY_ALL_IMGS";
    public static final String KEY_SELECT_IMGS = "KEY_SELECT_IMGS";
    public static final String KEY_INDEX       = "KEY_INDEX";

    public interface GalleryService {

        void loadImg(Context context, String path, int width, int height, ImageView imageView);

        void onSuccess(List<ImageInfo> list);


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


    /**
     * 使用系统相册获取图片，支持单选
     *
     * @param starter starter
     */
    public void chooseImgUseSystemGallery(ActivityStarter starter) {
        Intent intent = new Intent(Intent.ACTION_PICK);// 系统相册action
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        starter.startActivityForResult(intent, SYSTEM_GALLERY_REQ_CODE);
    }

    /**
     * 使用自定义相册获取图片，支持多选、单选
     *
     * @param starter starter
     */
    public void chooseImgUseDesignGallery(ActivityStarter starter, Context context, int maxNum) {
        Intent intent = new Intent(context, GalleryListActivity.class);
        intent.putExtra(Gallery.KEY_LIMIT, maxNum);
        starter.startActivityForResult(intent, DESIGN_GALLERY_REQ_CODE);
    }

    /**
     * 使用系统相机拍照，支持单张
     *
     * @param starter starter
     * @return 文件
     */
    public File captureImgUseSystemCamera(ActivityStarter starter, Context context) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = CommonUtils.generateImageFile(context.getCacheDir(), "capture", "png");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        starter.startActivityForResult(intent, CAPTURE_REQ_CODE);
        return file;
    }

    /**
     * 裁剪图片
     *
     * @param starter starter
     * @param uri     文件 Uri
     * @param xRatio  x
     * @param yRatio  y
     * @return 文件
     */
    public File cropImg(ActivityStarter starter, Uri uri, float xRatio, float yRatio) {
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
        File file = CommonUtils.generateImageFile(activity.getCacheDir(), "crop", "png");
        intent.putExtra("output", Uri.fromFile(file)); // 专入目标文件
        activity.startActivityForResult(intent, CROP_REQ_CODE);
        return file;
    }


}
