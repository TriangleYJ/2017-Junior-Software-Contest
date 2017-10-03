package com.yjprojects.jsctest2.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.yjprojects.jsctest2.R;
import com.yjprojects.jsctest2.User;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

// Created by jyj on 2017-08-05.

public class ImageActivity extends AppCompatActivity {


    private String name = "";
    private Toolbar toolbar;
    private View grad;
    private BottomNavigationView bnv;
    private String location = "";
    private PhotoView imageView;
    private boolean visibility = true;
    private Animation sanimation;
    private Animation hanimation;

    private boolean filter = true;
    private boolean rotation = true;
    private boolean onPause = false;
    ProgressDialog dialog;

    Bitmap filterBmp;
    Bitmap origin;

    public native void Convert(long input, long matAddrResult, long a, long b, long c, long d, boolean[] e, int f);
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");

        if(!OpenCVLoader.initDebug()) {
            Log.d("ERROR", "Unable to load OpenCV");
        } else {
            Log.d("SUCCESS", "OpenCV loaded");
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        onPause = false;
        if(savedInstanceState != null){
            rotation = (boolean) savedInstanceState.getSerializable("rotation");
        }

        getData();
        check();
        init();
        initToolbar();

    }

    @Override
    protected void onPause() {
        imageView.setImageBitmap(null);
        onPause = true;
        super.onPause();
    }

    @Override
    protected void onResume(){
        if(onPause){
            new FilterAsyncTask().execute();
            onPause = false;
        }

        super.onResume();
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
        /*Picasso.with(this).load(new File(location))
                .error(R.drawable.failed)
                .placeholder(R.drawable.ready)
                .into(imageView);*/


        new FilterAsyncTask().execute();

        grad = findViewById(R.id.im_view_top);
        bnv = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.disableShiftMode(bnv);
        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.baction_one:
                        if(origin == null){
                            Toast.makeText(ImageActivity.this, "Eyeshot 이미지입니다.", Toast.LENGTH_LONG).show();
                            return false;
                        }
                        if(filter){
                            item.setIcon(R.drawable.ic_remove_circle_outline_white_24dp);
                            item.setTitle("꺼짐");
                            imageView.setImageBitmap(origin);
                        }
                        else{
                            item.setIcon(R.drawable.ic_remove_red_eye_white_24dp);
                            item.setTitle("켜짐");
                            imageView.setImageBitmap(filterBmp);
                        }
                        filter = !filter;
                        return false;

                    case R.id.baction_two:
                        if(rotation) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        rotation = !rotation;
                        return false;

                    case R.id.baction_three:
                        if(!saveFilteredImage()) Toast.makeText(ImageActivity.this, "Already Exist Image.", Toast.LENGTH_LONG).show();
                        else Toast.makeText(ImageActivity.this, "Successfully Saved.", Toast.LENGTH_LONG).show();
                        return false;
                }
                return false;
            }
        });

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
        hanimation = new AlphaAnimation(1, 0);
        hanimation.setDuration(300);
        bnv.setVisibility(View.INVISIBLE);
        bnv.setAnimation(hanimation);

    }

    private void showToolbar(){
        toolbar.setVisibility(View.VISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        grad.setVisibility(View.VISIBLE);
        bnv.setVisibility(View.VISIBLE);
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


    @Override
    public void onSaveInstanceState(Bundle b){
        super.onSaveInstanceState(b);
        b.putSerializable("rotation", rotation);
    }


    //TODO Imperfect source
    private Bitmap initFilter() throws IOException {

        Bitmap bmp = BitmapFactory.decodeFile(location).copy(Bitmap.Config.ARGB_8888,true);
        
        ExifInterface ei = new ExifInterface(location);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        
        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                bmp = rotateImage(bmp, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                bmp = rotateImage(bmp, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                bmp = rotateImage(bmp, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
                break;
            default:
                break;
        }

        if(new File(location).getParentFile().getName().equals("Sighted")){
            origin = null;
            return bmp;
        }

        
        origin = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Mat img = new Mat();
        Utils.bitmapToMat(bmp, img);
        Mat rig = new Mat(img.rows(), img.cols(), img.type());

        Mat a = drawable2M(R.drawable.strip2);
        Mat b = drawable2M(R.drawable.strip3);
        Mat c = drawable2M(R.drawable.dot);
        Mat d = drawable2M(R.drawable.xx);

        User.setMode(User.MODE_ALL);
        Convert(img.getNativeObjAddr(), rig.getNativeObjAddr(), a.getNativeObjAddr(), b.getNativeObjAddr(), c.getNativeObjAddr(), d.getNativeObjAddr(), User.getModeDetail(), User.getQuality());

        Bitmap bp = Bitmap.createBitmap(rig.cols(), rig.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rig, bp);
        return bp;

    }

    private Mat drawable2M(int r){
        BitmapDrawable drawable = (BitmapDrawable) ContextCompat.getDrawable(this, r);
        Mat out = new Mat();
        Utils.bitmapToMat(Bitmap.createScaledBitmap(drawable.getBitmap(), User.getDensity(), User.getDensity(), true), out);
        return out;
    }

    private class FilterAsyncTask extends AsyncTask<String, Void, Integer> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ImageActivity.this);
            dialog.setTitle("");
            dialog.setMessage("이미지 처리중입니다..");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCanceledOnTouchOutside(false);

            dialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                if(filterBmp == null) filterBmp = initFilter();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            //첫 리스트 업데이트
            if (result == 0) {
                dialog.dismiss();
                imageView.setImageBitmap(filterBmp);
            }
        }
    }

    private boolean saveFilteredImage(){
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard
        String folder_name = "/ColorSighted/Sighted";
        String[] spl = name.split("Eyeshot_|Sighted_|(\\.[a-z0-9]+)");
        File folder = new File(ex_storage + folder_name);
        if(!folder.exists()) folder.mkdirs();
        String file_name = "Sighted_"+spl[spl.length - 1]+".jpeg";
        String string_path = ex_storage+folder_name + "/";

        boolean outb = false;
        try{
            if(!new File(string_path + file_name).exists()){
                FileOutputStream out = new FileOutputStream(string_path + file_name);
                filterBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.close();
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+string_path + file_name)));
                outb =  true;
            }

        }catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch(IOException exception){
            Log.e("IOException", exception.getMessage());
        }
        return outb;

    }


    private boolean saveDrawable(int r, String name){
        boolean outb = false;
        try {
            if(!new File(drawableDir(name)).exists()) {
                BitmapDrawable bd = (BitmapDrawable) ContextCompat.getDrawable(this, r);
                FileOutputStream out = new FileOutputStream(drawableDir(name));
                bd.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
                outb = true;
            }
        }catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        } catch (IOException exception) {
            Log.e("IOException", exception.getMessage());
        }
        return outb;
    }

    private String drawableDir(String name){
        String base = Environment.getExternalStorageDirectory() + "/temp/";
        return base + name + ".png";
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void check(){
        Bitmap bmp = BitmapFactory.decodeFile(location).copy(Bitmap.Config.ARGB_8888,true);
        if(bmp.getWidth() > bmp.getHeight()) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        rotation = !rotation;
    }


}
