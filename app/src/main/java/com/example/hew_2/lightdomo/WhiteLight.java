package com.example.hew_2.lightdomo;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.TextView;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
import com.example.mylibrary.gradient;

public class WhiteLight extends Fragment implements OnTouchListener {
    public static WhiteLight newInstance() {
        return new WhiteLight();
    }
    public static final String ACTION_LAMP_RGB_COLOR_CHANGED = "ACTION_LAMP_RGB_COLOR_CHANGED";;
    public static final String EXTRA_LAMP_RGB_COLOR_R = "EXTRA_LAMP_RGB_COLOR_R";
    public static final String EXTRA_LAMP_RGB_COLOR_W = "EXTRA_LAMP_RGB_COLOR_W";
    private TextView getMouse_xy;
    private FrameLayout mLayout;
    private gradient Gradient;
    private float Y0;
    private float Y1;
    private float mFocusX = 0.f;
    private float mFocusY = 0.f;
    private float DisplayY;
    int R1;
    int G1;
    int B1;
    int R2;
    int G2;
    int B2;
    double dR;
    double dW=150;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.white_fragment, container, false);
        mLayout = (FrameLayout) view.findViewById(R.id.selectedCT);
        getMouse_xy = (TextView) view.findViewById(R.id.CT);
        mLayout.setOnTouchListener(this);
        Gradient = (gradient) view.findViewById(R.id.gradient);
        Gradient.coldcolor = getResources().getColor(R.color.cold);
        Gradient.warmcolor = getResources().getColor(R.color.warm);
        super.onCreate(savedInstanceState);
        Display display = super.getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mFocusX = size.x/2f;    //屏幕的宽度一半
        mFocusY = size.y/2f;   //屏幕的高度一半
        DisplayY = size.y;
        return view;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                System.out.println("---action down-----");
                getMouse_xy.setText("起始位置为：" + "(" + event.getX() + " , " + event.getY() + ")"+mFocusY);
                Y0=event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                System.out.println("---action move-----");
                getMouse_xy.setText("移动中坐标为：" + "(" + event.getX() + " , " + event.getY() + ")"+mFocusY);
                Y1=event.getY();
                ChangeCT(Y0,Y1);
                Gradient.coldcolor= Color.rgb(R1,G1,B1);
                Gradient.warmcolor= Color.rgb(R2,G2,B2);
                Gradient.invalidate();
                this.broadCastLightValue();
                break;
            case MotionEvent.ACTION_UP:
                System.out.println("---action up-----");
                getMouse_xy.setText("最后位置为：" + "(" + event.getX() + " , " + event.getY() + ")"+mFocusY);
                Y1=event.getY();
                ChangeCT(Y0,Y1);
                Gradient.coldcolor= Color.rgb(R1,G1,B1);
                Gradient.warmcolor= Color.rgb(R2,G2,B2);
                Gradient.invalidate();
                this.broadCastLightValue();
                break;
            default:
                break;
        }
        return true;
    }
    public void ChangeCT(float P1,float P2) {
        float delta;
        float ratio;
        delta=P1-P2;
        if((mFocusY+delta)<DisplayY&&(mFocusY+delta)>=0){
            mFocusY=mFocusY+delta;
        }
        Y0=P2;
        ratio = mFocusY/DisplayY*100;
        dR=(double)ratio*0.6;
        R1=(int)(0.0224*ratio*ratio-0.66*ratio+96);
        G1=(int)(0.012*ratio*ratio-0.04*ratio+137);
        B1=200;
        if(ratio<50){
            R2=(int)(200+1.1*ratio);
            G2=214;
        }
        if(ratio>=50){
            R2=255;
            G2=(int)(214-1.18*(ratio-50));
        }
        B2=(int)(0.0074*ratio*ratio-1.63*ratio+221);
    }
    void broadCastLightValue(){
        final Intent intent = new Intent(ACTION_LAMP_RGB_COLOR_CHANGED);
        intent.putExtra(EXTRA_LAMP_RGB_COLOR_R,dR);
        intent.putExtra(EXTRA_LAMP_RGB_COLOR_W,dW);
        getActivity().sendBroadcast(intent);
    }

}