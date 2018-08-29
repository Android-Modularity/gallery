package com.march.gallery.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.march.gallery.R;

/**
 * CreateAt : 2018/8/1
 * Describe :
 *
 * @author chendong
 */
public abstract class FragmentContainerActivity extends AppCompatActivity {

    abstract Fragment getFragment();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_activity);
        Fragment fragment = getFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, fragment)
                .show(fragment)
                .commit();
    }
}
