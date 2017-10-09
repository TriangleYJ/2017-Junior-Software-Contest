package com.yjprojects.jsctest2.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.WindowManager;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.yjprojects.jsctest2.R;

/**
 * Created by jyj on 2017-10-04.
 */

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        addSlide(AppIntroFragment.newInstance("환영합니다", "Color-Sighted 어플리케이션을 소개합니다.", R.drawable.aq, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("쉽고 간단한 색각이상 검사", "이 색상 검사를 통하여 당신이 어떤 색각이상을 가지고 알 수 있습니다. ", R.drawable.aw, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("모든 이미지를 필터링", "당신의 스마트폰 안의 모든 이미지를 리스트에 날짜순으로 정렬합니다. 클릭 시 필터링된 이미지를 볼 수 있습니다.", R.drawable.at, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("화면상의 모든 것을 순식간에 캡쳐", "앱을 무심코 닫을 시 눈 모양의 아이콘을 띄웁니다. 클릭 시 화면을 캡쳐한 후 필터링을 하게 됩니다. x 버튼을 누를 시 이 버튼을 띄우지 않습니다.", R.drawable.ae, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("색구분을 도와줄 특별한 패턴들", "당신이 가지고 있는 색각 이상 질환에 따라 빨강, 초록, 파랑, 노랑이 다른 패턴으로 덮여져서 보입니다.", R.drawable.ay, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("다양한 부가 기능들", "필터링 된 이미지를 저장, 정렬 할 수 있는 기능, 설정 기능, 필터 켜짐 / 꺼짐 기능 등 여러 기능 또한 있습니다. \n본격적으로 시작해 볼까요?", R.drawable.ar, getResources().getColor(R.color.colorPrimary)));

        setBarColor(getResources().getColor(R.color.colorPrimaryDark));showSkipButton(true);
        setProgressButtonEnabled(true);

        setVibrate(true);
        setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        initMain();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        savePreferences();
        initMain();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }

    private void initMain(){
        Intent main = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(main);
        finish();
    }

    private void savePreferences(){
        SharedPreferences pref = getSharedPreferences("eye", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("one", 1);
        editor.apply();
    }

}
