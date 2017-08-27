package com.yjprojects.jsctest2.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Toast;

import com.yjprojects.jsctest2.R;
import com.yjprojects.jsctest2.recycler.BaseListClass;
import com.yjprojects.jsctest2.recycler.MainRecyclerViewAdapter;
import com.yjprojects.jsctest2.service.AlwaysService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ServiceConnection, AlwaysService.ICallback{

    private Toolbar toolbar;
    private RecyclerView recycler;
    private MainRecyclerViewAdapter adapter;
    private FloatingActionButton fab;
    private LinearLayoutManager glm;

    private List<BaseListClass> list = new ArrayList<>();

    //미디어 프로젝션 / 서비스 관련 변수 설정
    private static final String TAG = MainActivity.class.getName();
    private static final int REQUEST_CODE = 100;
    private static String STORE_DIRECTORY;
    private static int IMAGES_PRODUCED;
    private static final String SCREENCAP_NAME = "screencap";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private static MediaProjection sMediaProjection;

    private MediaProjectionManager mProjectionManager;
    private ImageReader mImageReader;
    private Handler mHandler;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    private OrientationChangeCallback mOrientationChangeCallback;
    private Intent serviceIntent;
    private AlwaysService mService;
    /////////////////

    //////////////

    private int close = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initToolbar();
        initrecyclerview();
        new MediaAsyncTask().execute();

    }



    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }

    @Override
    protected void onPause(){
        Log.d(TAG,"pause");
        if(close == 0){
            close = 1; // 첫 pause 호출 무시 && 다른 액티비티로 이동
        } else if(close == 1){
            serviceIntent = new Intent(getBaseContext(), AlwaysService.class); // 일반적으로 닫힌 경우 : 서비스 시작
            bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        } else {
            stopProjection(); // x버튼 : 프로젝션 종료
        }
        super.onPause();

    }

    @Override
    protected void onResume(){
        Log.d(TAG, "resume");
        super.onResume();
        if(serviceIntent != null){ //앱 화면으로 돌아온 경우 : 서비스 종료
            unbindService(this);
            serviceIntent = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "destroy");
        stopProjection();
        if(serviceIntent != null){ // 앱이 완전종료 될 경우 ; 서비스 종료, 프로젝션 종료
            unbindService(this);
            serviceIntent = null;
        }
    }


    //미디어 프로젝션
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);

            if (sMediaProjection != null) {
                File externalFilesDir = Environment.getExternalStorageDirectory();
                if (externalFilesDir != null) {
                    STORE_DIRECTORY = externalFilesDir.getAbsolutePath() + "/screenshots/";
                    File storeDirectory = new File(STORE_DIRECTORY);
                    if (!storeDirectory.exists()) {
                        boolean success = storeDirectory.mkdirs();
                        if (!success) {
                            Log.e(TAG, "failed to create file storage directory.");
                            return;
                        }
                    }
                } else {
                    Log.e(TAG, "failed to create file storage directory, getExternalFilesDir is null.");
                    return;
                }

                // display metrics
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                mDensity = metrics.densityDpi;
                mDisplay = getWindowManager().getDefaultDisplay();

                // create virtual display depending on device width / height
                createVirtualDisplay();

                // register orientation change callback
                mOrientationChangeCallback = new OrientationChangeCallback(this);
                if (mOrientationChangeCallback.canDetectOrientation()) {
                    mOrientationChangeCallback.enable();
                }

                // register media projection stop callback
                sMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
            }
        }
    }

    public void init(){
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close = 2;
                finish();
            }
        });

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();

        initCapture();
    }

    private void initCapture(){
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE); // 권한 획득
        startProjection();
    }

    private void initToolbar(){
        toolbar = (Toolbar) findViewById(R.id.mn_toolbar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);
    }

    private void initrecyclerview() {
        recycler = (RecyclerView) findViewById(R.id.mn_recyclerView);
        glm = new LinearLayoutManager(this);
        recycler.setNestedScrollingEnabled(false);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(glm);
    }

    private void recyclerPatch(List<BaseListClass> list){
        adapter = new MainRecyclerViewAdapter(list);
        recycler.setAdapter(adapter);
    }

    public void onViewClicked(String id, BaseListClass data){
        //TODO : 클릭시 위치 맨 앞으로 변경
        Toast.makeText(this, data.getTitle(), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("name", data.getTitle());
        intent.putExtra("location", data.getId());

        close = 0;
        startActivity(intent);
    }

    private void loadimage(){
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, // Which columns to return
                    null,       // Return all rows
                    null,
                    null);


            int size = cursor.getCount();
            /*******  If size is 0, there are no images on the SD Card. *****/
            if (size == 0) {
            } else {
                while (cursor.moveToNext()) {

                    int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                    /**************** Captured image details ************/

                    /*****  Used to show image on view in LoadImagesFromSDCard class ******/
                    String path = cursor.getString(file_ColumnIndex);

                    String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());

                    int pos = fileName.lastIndexOf(".");
                    String ext = fileName.substring(pos+1);

                    Long ldate = new File(path).lastModified();
                    Date date = new Date(ldate);
                    BaseListClass mediaFileInfo = new BaseListClass(fileName, date, path);
                    list.add(mediaFileInfo);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //미디어 프로젝션 관련 함수
    private void capture(Image image){
        FileOutputStream fos = null;
        Bitmap bitmap = null;

        try {
            if (image != null) {
                Image.Plane[] planes = image.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * mWidth;

                // create bitmap
                bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);

                String random = "";
                for(int i = 0; i < 10; i++){
                    random += (char)(65 + Math.floor(Math.random()*26));
                }

                // write bitmap to a file
                fos = new FileOutputStream(STORE_DIRECTORY + "myscreen_" + random + ".png");
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+STORE_DIRECTORY + "myscreen_" + random + ".png")));

                IMAGES_PRODUCED++;
                Log.e(TAG, "captured image: " + IMAGES_PRODUCED);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            if (bitmap != null) {
                bitmap.recycle();
            }

            if (image != null) {
                image.close();
            }
        }
    }

    private void startProjection() {
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    private void stopProjection() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (sMediaProjection != null) {
                    sMediaProjection.stop();
                }
            }
        });
    }

    private void createVirtualDisplay() {
        // get width and height
        Point size = new Point();
        mDisplay.getSize(size);
        mWidth = size.x;
        mHeight = size.y;

        // start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = sMediaProjection.createVirtualDisplay(SCREENCAP_NAME, mWidth, mHeight, mDensity, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, mHandler);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        AlwaysService.AlwaysServiceBinder binder = (AlwaysService.AlwaysServiceBinder) service;
        mService = binder.getService(); //서비스 받아옴
        mService.registerCallback(this); //콜백 등록
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }

    @Override
    public void recvData() {
        capture(mImageReader.acquireLatestImage());
    }

    @Override
    public void unBind() {
        finish();
    }


    //클래스
    private class OrientationChangeCallback extends OrientationEventListener {

        OrientationChangeCallback(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            final int rotation = mDisplay.getRotation();
            if (rotation != mRotation) {
                mRotation = rotation;
                try {
                    // clean up
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.e("ScreenCapture", "stopping projection.");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);
                    if (mOrientationChangeCallback != null) mOrientationChangeCallback.disable();
                    sMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                }
            });
        }
    }

    private class MediaAsyncTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(String... params) {
            int result = 1;
            try {
                loadimage();
                Collections.sort(list, new Comparator<BaseListClass>() {
                    @Override
                    public int compare(BaseListClass o1, BaseListClass o2) {
                        return o2.getDate().compareTo(o1.getDate());
                    }
                });
            }catch (Exception e) {
                e.printStackTrace();
                result =0;
            }

            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {

            //첫 리스트 업데이트
            if (result == 1) {
                recyclerPatch(list);
            } else {
                Log.e("TAG", "Failed to fetch data!");
            }
        }
    }



}
