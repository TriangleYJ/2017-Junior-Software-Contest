package com.yjprojects.jsctest2;


import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Created by jyj on 2017-10-04.
 */

public class TestFragment extends Fragment {
    private ImageView iv;
    private RadioGroup rg;
    private RadioButton r1;
    private RadioButton r2;
    private RadioButton r3;
    private RadioButton rdk;

    private int ivr;
    private String d1;
    private String d2;
    private String d3;
    private int answer;

    private int checked = -1;

    public TestFragment(){

    }
    public static TestFragment newInstance(int resource, String d1, String d2, String d3, int correct){
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        args.putInt("ivr", resource);
        args.putString("d1", d1);
        args.putString("d2", d2);
        args.putString("d3", d3);
        args.putInt("ans", correct);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            ivr = getArguments().getInt("ivr");
            d1 = getArguments().getString("d1");
            d2 = getArguments().getString("d2");
            d3 = getArguments().getString("d3");
            answer = getArguments().getInt("ans");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View main = inflater.inflate(R.layout.test_fragment, container, false);
        iv = (ImageView) main.findViewById(R.id.tf_iv);
        rg = (RadioGroup) main.findViewById(R.id.radioGroup);
        r1 = (RadioButton) main.findViewById(R.id.rb_1);
        r2 = (RadioButton) main.findViewById(R.id.rb_2);
        r3 = (RadioButton) main.findViewById(R.id.rb_3);
        rdk = (RadioButton) main.findViewById(R.id.rb_idk);
        
        iv.setImageResource(ivr);
        r1.setText(d1);
        r2.setText(d2);
        r3.setText(d3);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch(checkedId){
                    case R.id.rb_1 :
                        checked = 1;
                        break;
                    case R.id.rb_2 :
                        checked = 2;
                        break;
                    case R.id.rb_3 :
                        checked = 3;
                        break;
                    case R.id.rb_idk :
                        checked = 0;
                        break;
                }
            }
        });
        return main;
    }

    public int getCurrent(){
        return checked;
    }
}
