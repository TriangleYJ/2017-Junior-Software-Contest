package com.yjprojects.jsctest2.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.yjprojects.jsctest2.R;

import java.io.File;

/**
 * Created by jyj on 2017-08-05.
 */

public class ImageActivity extends AppCompatActivity {


    private String name = "";
    private Toolbar toolbar;
    private String location = "";
    private PhotoView imageView;
    private boolean visibility = true;
    private Animation sanimation;
    private Animation hanimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        getData();
        init();
        initToolbar();

        Window w = getWindow(); // in Activity's onCreate() for instance
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void getData(){
        Intent intent = getIntent();
        if(intent != null){
            name = intent.getStringExtra("name");
            location = intent.getStringExtra("location");
        }
    }

    private void init(){
        imageView = (PhotoView) findViewById(R.id.im);
        toolbar = (Toolbar) findViewById(R.id.im_toolbar);
        Uri uri = Uri.fromFile(new File(location));
        imageView.setImageURI(uri);


        imageView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                if(visibility) {
                    hanimation = new AlphaAnimation(1, 0);
                    hanimation.setDuration(300);
                    toolbar.setVisibility(View.INVISIBLE);
                    toolbar.setAnimation(hanimation);

                } else {
                    sanimation = new AlphaAnimation(0, 1);
                    sanimation.setDuration(300);
                    toolbar.setVisibility(View.VISIBLE);
                    toolbar.setAnimation(sanimation);
                }
                visibility = !visibility;
            }
        });

        imageView.setOnScaleChangeListener(new OnScaleChangedListener() {
            @Override
            public void onScaleChange(float scaleFactor, float focusX, float focusY) {
                if(visibility) {
                    hanimation = new AlphaAnimation(1, 0);
                    hanimation.setDuration(375);
                    toolbar.setVisibility(View.INVISIBLE);
                    toolbar.setAnimation(hanimation);

                }

                visibility = !visibility;
            }
        });
    }

    private void initToolbar(){
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);
    }

    private void hideStatusBar(){

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

}
