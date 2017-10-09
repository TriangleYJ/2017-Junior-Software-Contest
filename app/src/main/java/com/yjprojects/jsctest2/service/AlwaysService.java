package com.yjprojects.jsctest2.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yjprojects.jsctest2.R;

/**
 * Created by jyj on 2017-08-05.
 */

public class AlwaysService extends Service{

    public interface ICallback {
        public void recvData(); //액티비티에서 선언한 콜백 함수.
        public void unBind();
    }

    private ICallback mCallback;
    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }
    private IBinder mBinder = new AlwaysServiceBinder();

    public class AlwaysServiceBinder extends Binder {
        public AlwaysService getService() {
            return AlwaysService.this; //현재 서비스를 반환.
        }
    }



    private static final String				TAG = "Service";


    private LayoutInflater inflater;
    private WindowManager.LayoutParams		params;
    private WindowManager.LayoutParams removeParams;
    private WindowManager					winMgr;
    private LinearLayout view, removeView;

    private int x_init_cord, y_init_cord, x_init_margin, y_init_margin;
    private Point szWindow = new Point();
    private boolean isLeft = true;
    ImageView rButton;

    @Override
    public void onCreate() {
        super.onCreate();

        // service_main.xml 에 있는 view 의 정의를 실제 view 객체로 만들기 위해서 inflater 를 얻어옵니다.
        inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        // inflater 를 사용해서 실제 view 객체로 만듭니다.
        view = (LinearLayout)inflater.inflate(R.layout.service_activity, null);
        removeView = (LinearLayout)inflater.inflate(R.layout.remove, null);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        removeParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        removeParams.gravity = Gravity.TOP | Gravity.START;
        removeView.setVisibility(View.GONE);

        try {
            winMgr = (WindowManager) getSystemService( WINDOW_SERVICE );
            winMgr.addView( view, params );
            winMgr.addView( removeView, removeParams);
            winMgr.getDefaultDisplay().getSize(szWindow);
        }
        catch(Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception message : " + e.getLocalizedMessage());
        }

        ImageButton button = (ImageButton) view.findViewById(R.id.sc_fab);
        rButton = (ImageView) removeView.findViewById(R.id.remove);

        button.setOnTouchListener(new View.OnTouchListener() {
            long time_start = 0, time_end = 0;
            boolean isLongclick = false, inBounded = false;
            int remove_img_width = 0, remove_img_height = 0;

            Handler handler_longClick = new Handler();
            Runnable runnable_longClick = new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "Into runnable_longClick");

                    isLongclick = true;
                    removeView.setVisibility(View.VISIBLE);
                    chathead_longclick();
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x_cord = (int) event.getRawX();
                int y_cord = (int) event.getRawY();
                int x_cord_Destination, y_cord_Destination;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        time_start = System.currentTimeMillis();
                        handler_longClick.postDelayed(runnable_longClick, 600);

                        remove_img_width = rButton.getLayoutParams().width;
                        remove_img_height = rButton.getLayoutParams().height;
                        x_init_cord = x_cord;
                        y_init_cord = y_cord;
                        x_init_margin = params.x;
                        y_init_margin = params.y;
                        break;
                    case MotionEvent.ACTION_UP:
                        isLongclick = false;
                        removeView.setVisibility(View.GONE);
                        rButton.getLayoutParams().height = remove_img_height;
                        rButton.getLayoutParams().width = remove_img_width;
                        handler_longClick.removeCallbacks(runnable_longClick);

                        if(inBounded){
                            mCallback.unBind();
                            inBounded = false;
                            break;
                        }

                        int x_diff = x_cord - x_init_cord;
                        int y_diff = y_cord - y_init_cord;
                        if(Math.abs(x_diff) < 15 && Math.abs(y_diff) < 15){
                            time_end = System.currentTimeMillis();
                            if((time_end - time_start) < 300){
                                chathead_click();
                            }
                        }

                        y_cord_Destination = y_init_margin + y_diff;

                        int BarHeight =  getStatusBarHeight();
                        if (y_cord_Destination < 0) {
                            y_cord_Destination = 0;
                        } else if (y_cord_Destination + (view.getHeight() + BarHeight) > szWindow.y) {
                            y_cord_Destination = szWindow.y - (view.getHeight() + BarHeight );
                        }
                        params.y = y_cord_Destination;

                        inBounded = false;
                        resetPosition(x_cord);

                        break;
                    case MotionEvent.ACTION_MOVE:
                        int x_diff_move = x_cord - x_init_cord;
                        int y_diff_move = y_cord - y_init_cord;
                        x_cord_Destination = x_init_margin + x_diff_move;
                        y_cord_Destination = y_init_margin + y_diff_move;
                        if(isLongclick){
                            int x_bound_left = szWindow.x / 2 - (int)(remove_img_width * 1.5);
                            int x_bound_right = szWindow.x / 2  + (int)(remove_img_width * 1.5);
                            int y_bound_top = szWindow.y - (int)(remove_img_height * 1.5);

                            if((x_cord >= x_bound_left && x_cord <= x_bound_right) && y_cord >= y_bound_top){
                                inBounded = true;

                                int x_cord_remove = (int) ((szWindow.x - (remove_img_height * 1.5)) / 2);
                                int y_cord_remove = (int) (szWindow.y - ((remove_img_width * 1.5) + getStatusBarHeight() ));

                                if(rButton.getLayoutParams().height == remove_img_height){
                                    rButton.getLayoutParams().height = (int) (remove_img_height * 1.5);
                                    rButton.getLayoutParams().width = (int) (remove_img_width * 1.5);

                                    WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeView.getLayoutParams();
                                    param_remove.x = x_cord_remove;
                                    param_remove.y = y_cord_remove;

                                    winMgr.updateViewLayout(removeView, param_remove);
                                }

                                params.x = x_cord_remove + (Math.abs(removeView.getWidth() - view.getWidth())) / 2;
                                params.y = y_cord_remove + (Math.abs(removeView.getHeight() - view.getHeight())) / 2 ;

                                winMgr.updateViewLayout(view, params);
                                break;
                            }else{
                                inBounded = false;
                                rButton.getLayoutParams().height = remove_img_height;
                                rButton.getLayoutParams().width = remove_img_width;

                                WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeView.getLayoutParams();
                                int x_cord_remove = (szWindow.x - removeView.getWidth()) / 2;
                                int y_cord_remove = szWindow.y - (removeView.getHeight() + getStatusBarHeight() );

                                param_remove.x = x_cord_remove;
                                param_remove.y = y_cord_remove;

                                winMgr.updateViewLayout(removeView, param_remove);
                            }

                        }
                        params.x = x_cord_Destination;
                        params.y = y_cord_Destination;
                        winMgr.updateViewLayout(view, params);
                        break;

                }

                return false;
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        winMgr.getDefaultDisplay().getSize(szWindow);

        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) view.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {


            if(layoutParams.y + (view.getHeight() + getStatusBarHeight()) > szWindow.y){
                layoutParams.y = szWindow.y- (view.getHeight() + getStatusBarHeight());
                if(winMgr != null)winMgr.updateViewLayout(view, layoutParams);
            }

            if(layoutParams.x != 0 && layoutParams.x < szWindow.x){
                resetPosition(szWindow.x);
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            if(layoutParams.x > szWindow.x){
                resetPosition(szWindow.x);
            }
        }

    }

    /*  Reset position of Floating Widget view on dragging  */
    private void resetPosition(int x_cord_now) {
        if (x_cord_now <= szWindow.x / 2) {
            isLeft = true;
            moveToLeft(x_cord_now);
        } else {
            isLeft = false;
            moveToRight(x_cord_now);
        }

    }


    /*  Method to move the Floating widget view to Left  */
    private void moveToLeft(final int current_x_cord) {
        final int x = szWindow.x - current_x_cord;

        new CountDownTimer(500, 5) {
            //get params of Floating Widget view
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) view.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;

                mParams.x = 0 - (int) (current_x_cord * current_x_cord * step);

                //If you want bounce effect uncomment below line and comment above line
                // mParams.x = 0 - (int) (double) bounceValue(step, x);


                //Update window manager for Floating Widget
                if(winMgr != null)winMgr.updateViewLayout(view, mParams);
            }

            public void onFinish() {
                mParams.x = 0;

                //Update window manager for Floating Widget
                if(winMgr != null)winMgr.updateViewLayout(view, mParams);
            }
        }.start();
    }

    /*  Method to move the Floating widget view to Right  */
    private void moveToRight(final int current_x_cord) {
        new CountDownTimer(500, 5) {
            //get params of Floating Widget view
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) view.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;

                mParams.x = (int) (szWindow.x + (current_x_cord * current_x_cord * step) - view.getWidth());

                //If you want bounce effect uncomment below line and comment above line
                //  mParams.x = szWindow.x + (int) (double) bounceValue(step, x_cord_now) - view.getWidth();

                //Update window manager for Floating Widget
                if(winMgr != null)winMgr.updateViewLayout(view, mParams);
            }

            public void onFinish() {
                mParams.x = szWindow.x - view.getWidth();

                //Update window manager for Floating Widget
                if(winMgr != null)winMgr.updateViewLayout(view, mParams);
            }
        }.start();
    }


    private int getStatusBarHeight() {
        int statusBarHeight = (int) Math.ceil(25 * getApplicationContext().getResources().getDisplayMetrics().density);
        return statusBarHeight;
    }

    private void chathead_click(){
        Log.d(TAG, "Clicked");
        mCallback.recvData();
        Toast.makeText(AlwaysService.this, "Captured Image.", Toast.LENGTH_LONG).show();
        //mCallback.unBind();
        onDestroy();
    }

    private void chathead_longclick(){
        Log.d(TAG, "Into ChatHeadService.chathead_longclick() ");

        WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeView.getLayoutParams();
        int x_cord_remove = (szWindow.x - removeView.getWidth()) / 2;
        int y_cord_remove = szWindow.y - (removeView.getHeight() + getStatusBarHeight() );

        param_remove.x = x_cord_remove;
        param_remove.y = y_cord_remove;

        if(winMgr != null)winMgr.updateViewLayout(removeView, param_remove);
    }

    @Override
    public void onDestroy() {
        if(winMgr != null) winMgr.removeView(view);

        mBinder = null;
        winMgr = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null) {
            mBinder = new AlwaysServiceBinder();
        }
        return mBinder;
    }

    
}

