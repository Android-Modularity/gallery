package com.march.gallery.common;

import java.util.List;

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

}
