package com.yjprojects.jsctest2.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.yjprojects.jsctest2.R;

/**
 * Created by jyj on 2017-08-05.
 */

public class AlwaysService extends Service {

    public class AlwaysServiceBinder extends Binder {
        public AlwaysService getService() {
            return AlwaysService.this; //현재 서비스를 반환.
        }
    }
    private final IBinder mBinder = new AlwaysServiceBinder();
    public interface ICallback {
        public void recvData(); //액티비티에서 선언한 콜백 함수.
    }
    private ICallback mCallback;

    //액티비티에서 콜백 함수를 등록하기 위함.
    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }





    private static final String				TAG = "Service";

    private LayoutInflater					inflater;
    private WindowManager.LayoutParams		params;
    private WindowManager					winMgr;
    private View view;

    private float mTouchX, mTouchY;
    private int mViewX, mViewY;




    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // service_main.xml 에 있는 view 의 정의를 실제 view 객체로 만들기 위해서 inflater 를 얻어옵니다.
        inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        // inflater 를 사용해서 실제 view 객체로 만듭니다.
        view = inflater.inflate(R.layout.service_activity, null);
        
        // view 에 적용시킬 파라미터들을 정의합니다.
        // 전체 화면에 적용하기 위해, width 와 height 를 match_parent 로
        // 터치 이벤트를 받을 수 있게 처리하기 위해서 type 은 type_phone 으로
        // 마찬가지로 터치 이벤트를 처리할 수 있게 하기 위해서 flag 도 flag_not_focusable 로
        // alpah 값도 처리할 수 있게끔 PixelFormat 을 translucent 로 설정해줬습니다.
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;

        try {
            winMgr = (WindowManager) getSystemService( WINDOW_SERVICE );
            winMgr.addView( view, params );
        }
        catch(Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception message : " + e.getLocalizedMessage());
        }

        ImageButton button = (ImageButton) view.findViewById(R.id.sc_fab);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked");
                mCallback.recvData();

            }
        });


        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchX = event.getRawX();
                        mTouchY = event.getRawY();
                        mViewX = params.x;
                        mViewY = params.y;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int x = (int) (event.getRawX() - mTouchX);
                        int y = (int) (event.getRawY() - mTouchY);
                        params.x = mViewX + x;
                        params.y = mViewY + y;
                        winMgr.updateViewLayout(view, params);
                        break;
                }

                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        winMgr.removeView(view);
        super.onDestroy();
    }

}