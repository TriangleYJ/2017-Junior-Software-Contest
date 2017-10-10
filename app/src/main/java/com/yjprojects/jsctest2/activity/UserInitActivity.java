package com.yjprojects.jsctest2.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.yjprojects.jsctest2.R;
import com.yjprojects.jsctest2.TestFragment;

/**
 * Created by jyj on 2017-08-05.
 */

public class UserInitActivity extends AppIntro2 {
    Fragment fr1;
    Fragment fr2;
    Fragment fr3;
    Fragment fr4;
    Fragment fr5;
    Fragment fr6;


    int red = 0;
    int green = 0;
    int all = 0;
    int none = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        addSlide(AppIntroFragment.newInstance("색각 이상 검사", "당신이 어떤 색각 이상 질환을 가지고 있는지를 검사합니다.", R.drawable.aw, getResources().getColor(R.color.colorPrimary)));


        fr1 = TestFragment.newInstance(R.drawable.e1, "6", "16", "46", 2);
        fr2 = TestFragment.newInstance(R.drawable.e2, "5", "3", "7", 1);
        fr3 = TestFragment.newInstance(R.drawable.e3, "17", "15", "7", 2);
        fr4 = TestFragment.newInstance(R.drawable.e5, "12", "1", "3", 1);
        fr5 = TestFragment.newInstance(R.drawable.e6, "26", "6", "2", 1);
        fr6 = TestFragment.newInstance(R.drawable.e7, "빨강색 선", "보라색 선", "초록색 선", 4);


        addSlide(fr1);
        addSlide(fr2);
        addSlide(fr3);
        addSlide(fr4);
        addSlide(fr5);
        addSlide(fr6);

        setProgressButtonEnabled(true);

        setVibrate(true);
        setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        initMain();
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.


        //색맹 판단 알고리즘
        if(((TestFragment)fr1).getCurrent() == 2) none++;
        else{red++; green++; all++;}

        if(((TestFragment)fr2).getCurrent() == 1) none++;
        else{red++; green++; all++;}

        if(((TestFragment)fr3).getCurrent() == 2) none++;
        else if(((TestFragment)fr3).getCurrent() == 1) {red++; green++;}
        else all++;
        if(((TestFragment)fr4).getCurrent() == 1){none++; red++; green++;}
        else{all++;}
        if(((TestFragment)fr5).getCurrent() == 1) none++;
        else if(((TestFragment)fr5).getCurrent() == 2){red++;}
        else if(((TestFragment)fr5).getCurrent() == 3){green++;}
        else {all++;}
        if(((TestFragment)fr6).getCurrent() == 4) none++;
        else if(((TestFragment)fr6).getCurrent() == 1){red++;}
        else if(((TestFragment)fr6).getCurrent() == 2){red++; green++;}
        else {all++;}


        String modename = "정상";
        int max = findMax(none, red, green, all);

        if(max == green) modename = "녹색맹";
        else if(max == red) modename = "적색맹";
        else if(max == all) modename = "전색맹";

        String message = "당신은 " + modename+" 입니다.\n 설정의 Color 옵션에서 당신의 색각이상을 선택해 주시기 바랍니다.";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        initSetting();
        savePreferences();
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }

    private void initSetting(){
        Intent main = new Intent(UserInitActivity.this, SettingActivity.class);
        startActivity(main);
    }

    private void initMain(){
        Intent main = new Intent(UserInitActivity.this, MainActivity.class);
        startActivity(main);
    }

    private int findMax(int... vals) {
        int max = Integer.MIN_VALUE;

        for (int d : vals) {
            if (d > max) max = d;
        }

        return max;
    }

    private void savePreferences(){
        SharedPreferences pref = getSharedPreferences("eye", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("two", 1);
        editor.apply();
    }

}
