package com.march.gallery;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * CreateAt : 2018/8/1
 * Describe :
 *
 * @author chendong
 */
public class ShopIconView extends View{

    private int mNormalIconColor;
    private String mNormalIconText;

    public ShopIconView(Context context) {
        super(context);
    }

    public ShopIconView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ShopIconView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setNormalIconColor(int normalIconColor) {
        mNormalIconColor = normalIconColor;
    }

    public void setNormalIconText(String normalIconText) {
        mNormalIconText = normalIconText;
    }
}
