package com.yjprojects.jsctest2.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.yjprojects.jsctest2.R;
import com.yjprojects.jsctest2.User;
import com.yjprojects.jsctest2.recycler.BaseListClass;
import com.yjprojects.jsctest2.recycler.MainRecyclerViewAdapter;
import com.yjprojects.jsctest2.service.AlwaysService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ServiceConnection, AlwaysService.ICallback{

    private Toolbar toolbar;
    private FastScrollRecyclerView recycler;
    private MainRecyclerViewAdapter adapter;
    private FloatingActionButton fab;
    private LinearLayoutManager glm;
    private Switch swt;

    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

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


        check();
        permission();
        initUser();
        init();
        initToolbar();
        initrecyclerview();
        new MediaAsyncTask(false).execute();

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
        if(swt != null){
            list.clear();
            new MediaAsyncTask(swt.isChecked()).execute();
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
                    STORE_DIRECTORY = externalFilesDir.getAbsolutePath() + "/ColorSighted/";
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
        drawerLayout = (DrawerLayout) findViewById(R.id.mn_drawer);
        navigationView = (NavigationView) findViewById(R.id.mn_navi);
        swt = (Switch) findViewById(R.id.mn_switch);



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

        swt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                list.clear();
                new MediaAsyncTask(isChecked).execute();
            }
        });

        View header = navigationView.getHeaderView(0);
        final TextView nameT = (TextView) header.findViewById(R.id.head_name);
        final TextView modeT = (TextView) header.findViewById(R.id.head_mode);
        modeT.setText(User.getModeName());
        nameT.setText(User.getName());
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if(getSupportActionBar() != null) {
                    close = 0;
                    switch (menuItem.getItemId()) {
                        case R.id.navigation_item_1:
                            //This Part Will be better to do with fragments, but there is really complicated problems about communicating between activity and fragments.
                            break;
                        case R.id.navigation_item_2:
                            Intent userInit = new Intent(MainActivity.this, UserInitActivity.class);
                            startActivity(userInit);
                            break;
                        case R.id.navigation_item_3:
                            Intent setting = new Intent(MainActivity.this, SettingActivity.class);
                            startActivity(setting);
                            break;
                        case R.id.navigation_subitem_1:
                            String url = "https://namu.wiki/w/%EC%83%89%EA%B0%81%20%EC%9D%B4%EC%83%81";
                            String url1 = "https://ko.wikipedia.org/wiki/%EC%83%89%EA%B0%81_%EC%9D%B4%EC%83%81";
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((Math.random() < 0.5) ? url : url1));
                            startActivity(browserIntent);
                            break;
                        case R.id.navigation_subitem_2:
                            Intent intro = new Intent(MainActivity.this, IntroActivity.class);
                            startActivity(intro);
                            finish();
                            break;
                        case R.id.navigation_subitem_3:
                            Intent develop = new Intent(MainActivity.this, DeveloperActivity.class);
                            startActivity(develop);
                            break;

                    }
                    drawerLayout.closeDrawers();
                }

                return false;
            }
        });


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

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar, 0, 0);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
    }

    private void initrecyclerview() {
        recycler = (FastScrollRecyclerView) findViewById(R.id.mn_recyclerView);
        glm = new LinearLayoutManager(this);
        recycler.setNestedScrollingEnabled(false);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(glm);
    }

    private void recyclerPatch(List<BaseListClass> list){
        adapter = new MainRecyclerViewAdapter(list);
        recycler.setAdapter(adapter);
    }

    public void onViewClicked(BaseListClass data, int mode){
        close = mode;
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("name", data.getTitle());
        intent.putExtra("location", data.getId());
        startActivity(intent);
    }

    private void loadimage(boolean eyeshot){
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
                    if(!eyeshot || new File(path).getParentFile().getName().equals("Sighted")){

                        String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
                        if(!eyeshot || fileName.substring(fileName.lastIndexOf(".")).equals(".jpeg")) {


                            int pos = fileName.lastIndexOf(".");

                            Long ldate = new File(path).lastModified();
                            Date date = new Date(ldate);
                            BaseListClass mediaFileInfo = new BaseListClass(fileName, date, path);
                            list.add(mediaFileInfo);
                        }
                    }

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

                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String rand = sdf.format(date);
                String name = "Eyeshot_" + rand + ".png";
                // write bitmap to a file
                fos = new FileOutputStream(STORE_DIRECTORY + name);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+STORE_DIRECTORY + name)));

                IMAGES_PRODUCED++;
                BaseListClass newImage = new BaseListClass(name, null, STORE_DIRECTORY + name);
                onViewClicked(newImage, 1);
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

    private void initUser(){

        int result = PreferenceManager.getDefaultSharedPreferences(this).getInt("density", 50);
        int density = 2000 - result * 10;
        User.setDensity(density);
        int result1 = PreferenceManager.getDefaultSharedPreferences(this).getInt("quality", 750);
        if(result1 == 0) result1 = 1;
        User.setQuality(result1);
        String result2 = PreferenceManager.getDefaultSharedPreferences(this).getString("username", "코타나");
        User.setName(result2);
        int result3 = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("mode", "0"));
        User.setMode(result3);


    }

    private class MediaAsyncTask extends AsyncTask<String, Void, Integer> {
        boolean eyeshot;
        ProgressDialog dialog;

        public MediaAsyncTask(boolean eyeshot) {
            super();
            this.eyeshot = eyeshot;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle("");
            dialog.setMessage("로딩중..");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCanceledOnTouchOutside(false);

            dialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            int result = 1;
            try {
                loadimage(eyeshot);
                Collections.sort(list, new Comparator<BaseListClass>() {
                    @Override
                    public int compare(BaseListClass o1, BaseListClass o2) {
                        return o2.getDate().compareTo(o1.getDate());
                    }
                });

            }catch (Exception e) {
                e.printStackTrace();
                result = 0;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {

            //첫 리스트 업데이트
            if (result == 1) {
                recyclerPatch(list);
                dialog.dismiss();
            } else {
                Log.e("TAG", "Failed to fetch data!");
            }
        }
    }

    private void check(){
        SharedPreferences pref = getSharedPreferences("eye", MODE_PRIVATE);
        if(pref.getInt("one", 0) == 0){
            Intent intro = new Intent(MainActivity.this, IntroActivity.class);
            startActivity(intro);
            finish();
        }
        else if(pref.getInt("two", 0) == 0){
            Intent userInit = new Intent(MainActivity.this, UserInitActivity.class);
            startActivity(userInit);
        }

    }


    private void permission(){
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }

        };


        TedPermission.with(this)
                .setRationaleTitle("6.0이상 버전부터는 권한 동의가 필요함")
                .setRationaleMessage(" 이 앱에서는 다른 앱 위에 그리기, 저장공간 권한을 정상적인 앱 구동을 위하여 필요로 합니다. 확인 메시지가 나올 시 동의로 체크해 주시기 바랍니다.\n 또한 스크린샷 캡쳐 기능을 사용하고자 하니 초기 실행시 '화면상의 모든 것을 캡쳐하시겠습니까' 메시지에 대하여 다시 묻지 않기로 '시작하기' 버튼을 눌러 주시기 바랍니다.")
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.VIBRATE)
                .check();


    }


}