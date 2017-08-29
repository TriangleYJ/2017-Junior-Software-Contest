package com.yjprojects.jsctest2.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.yjprojects.jsctest2.R;

import java.io.File;

// Created by jyj on 2017-08-05.

public class ImageActivity extends AppCompatActivity {


    private String name = "";
    private Toolbar toolbar;
    private View grad;
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
        grad = findViewById(R.id.im_view);


        imageView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                if(visibility) hideToolbar();
                else showToolbar();

                visibility = !visibility;
            }
        });

        imageView.setOnScaleChangeListener(new OnScaleChangedListener() {
            @Override
            public void onScaleChange(float scaleFactor, float focusX, float focusY) {
                if(visibility) {
                    hideToolbar();
                    visibility = !visibility;
                }

            }
        });
    }

    private void initToolbar(){
       getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


    }


    private void hideToolbar(){
        hanimation = new AlphaAnimation(1, 0);
        hanimation.setDuration(300);
        toolbar.setVisibility(View.INVISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        grad.setVisibility(View.INVISIBLE);
        grad.setAnimation(hanimation);

    }

    private void showToolbar(){
        toolbar.setVisibility(View.VISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        grad.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
