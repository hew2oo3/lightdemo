package com.example.hew_2.lightdomo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mylibrary.SingleFingerGestureDetector;

/**
 * Created by hew_2 on 2019/7/12.
 */

public class Control_frame extends Fragment{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.control_frame,container,false);
        super.onCreate(savedInstanceState);
        RadioButton color =(RadioButton)view.findViewById(R.id.color);
        RadioButton white =(RadioButton)view.findViewById(R.id.white);
        color.setOnClickListener(l);
        white.setOnClickListener(l);
        return view;
    }

    View.OnClickListener l = new View.OnClickListener(){
        @Override
                public void onClick(View v){
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment f= null;
            switch (v.getId()){
                case R.id.color:
                    f = new ColorLight();
                    break;
                case R.id.white:
                    f = new WhiteLight();
                    break;
            }
            ft.replace(R.id.fragment,f);
            ft.commit();
        }
    };
}
