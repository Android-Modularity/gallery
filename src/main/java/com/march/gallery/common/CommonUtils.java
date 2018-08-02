package com.march.gallery.common;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * CreateAt : 2018/3/9
 * Describe :
 *
 * @author chendong
 */
public class CommonUtils {

    public static <T> void addOrRemoveForContains(List<T> list, T data) {
        if (list.contains(data)) {
            list.remove(data);
        } else {
            list.add(data);
        }
    }

    public static File generateImageFile(File dir, String sign, String suffix) {
        // 通过uuid生成照片唯一名字
        String mOutFileName = UUID.randomUUID().toString() + "_" + sign + "_image." + suffix;
        return new File(dir, mOutFileName);
    }

}
