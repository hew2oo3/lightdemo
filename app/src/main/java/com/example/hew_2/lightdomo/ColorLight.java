package com.example.hew_2.lightdomo;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnTouchListener;
import android.graphics.Color;
import android.support.v4.view.ViewPager;


import com.example.mylibrary.SingleFingerGestureDetector;

/**
 * Created by hew_2 on 2018/6/23.
 */

public class ColorLight extends Fragment implements OnTouchListener {
    public static ColorLight newInstance() {
        return new ColorLight();
    }
    public static final String ACTION_LAMP_RGB_COLOR_CHANGED = "ACTION_LAMP_RGB_COLOR_CHANGED";
    public static final String EXTRA_LAMP_RGB_COLOR_R = "EXTRA_LAMP_RGB_COLOR_R";
    public static final String EXTRA_LAMP_RGB_COLOR_G = "EXTRA_LAMP_RGB_COLOR_G";
    public static final String EXTRA_LAMP_RGB_COLOR_B = "EXTRA_LAMP_RGB_COLOR_B";
    public static final String EXTRA_LAMP_RGB_COLOR_W = "EXTRA_LAMP_RGB_COLOR_W";
    double dR;
    double dG;
    double dB;
    FrameLayout mLayout;
    TextView a;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.color_fragment,container,false);
        mLayout = (FrameLayout)view.findViewById(R.id.selectedColor);
        super.onCreate(savedInstanceState);
        // 获取屏幕的中心点
        Display display = super.getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mFocusX = size.x/2f;    //屏幕的宽度一半
        mFocusY = size.y/2f;   //屏幕的高度一半

        // 将图片设置触碰监听
        ImageView rotateView = (ImageView) view.findViewById(R.id.rotateView);
        rotateView.setOnTouchListener(this);

        // 确定维度的地球图像”
        Drawable d = this.getResources().getDrawable(R.drawable.colorpicker_450);
        mImageHeight = d.getIntrinsicHeight();
        mImageWidth = d.getIntrinsicWidth();
        // 视图缩放和转动的矩阵
        scaledImageCenterX = (mImageWidth * mScaleFactor1) / 2;
        scaledImageCenterY = (mImageHeight * mScaleFactor1) / 2;
        if(mMatrix.isIdentity()==true)
        {   mMatrix.postScale(mScaleFactor1, mScaleFactor1);
            mMatrix.postTranslate(mFocusX - scaledImageCenterX, mFocusY - scaledImageCenterY);
        }
        a = (TextView) view.findViewById(R.id.angleDegree);
        a.setText("旋转角度"+selectedColor((double)DegreeConvertion((int)mRotationDegrees)));
        mLayout.setBackgroundColor(Color.parseColor("#"+selectedColor((double)DegreeConvertion(mRotationDegrees))));

        /**
         * 用Matrix类中的平移方法设置转盘的位置
         * */
        rotateView.setImageMatrix(mMatrix);     //将转盘设置到屏幕中心

        // 设置手势检测器
        mRotateDetector = new SingleFingerGestureDetector(getActivity().getApplicationContext(), new RotateListener());  //图片的旋转
        //
        return view;

    }

    private Matrix mMatrix = new Matrix();
    private float mScaleFactor1 = .4f;
    private float mRotationDegrees;
    private float mFocusX = 0.f;
    private float mFocusY = 0.f;
    private int mImageHeight, mImageWidth;
    private float scaledImageCenterX;
    private float scaledImageCenterY;
    //private ScaleGestureDetector mScaleDetector;  //定义图片的放大缩小
    private SingleFingerGestureDetector mRotateDetector;  //设置图片旋转


    /**
     * 设置手势
     * */
    @SuppressWarnings("deprecation")
    public boolean onTouch(View v, MotionEvent event) {
        mRotateDetector.onTouchEvent(event);  //设置图片的旋转监听
        scaledImageCenterX = (mImageWidth * mScaleFactor1) / 2;
        scaledImageCenterY = (mImageHeight * mScaleFactor1) / 2;
        mMatrix.reset();
        mMatrix.postScale(mScaleFactor1, mScaleFactor1);
        mMatrix.postRotate(mRotationDegrees, scaledImageCenterX, scaledImageCenterY);
        mMatrix.postTranslate(mFocusX - scaledImageCenterX, mFocusY - scaledImageCenterY);


        ImageView view = (ImageView) v;
        view.setImageMatrix(mMatrix);
        a.setText("旋转角度"+selectedColor((double)DegreeConvertion(mRotationDegrees)) );
        mLayout.setBackgroundColor(Color.parseColor("#"+selectedColor((double)DegreeConvertion(mRotationDegrees))));
        broadCastLightValue();
        return true; // 指示事件处理
    }

    /**
     * 图片转动的监听
     * */
    private class RotateListener extends SingleFingerGestureDetector.SimpleOnRotateGestureListener {

        @Override
        public boolean onRotate(SingleFingerGestureDetector detector) {
            mRotationDegrees += detector.getRotationDegreesDelta(mFocusX,mFocusY);
            return true;
        }
    }
    /**
     * RBG取值计算
     * */
    private  float DegreeConvertion(float actualDegree){
        float H=actualDegree% 360;
        if(H<0){
            H=360+H;
        }
        return H;
    }

    public String selectedColor(double H) {
        double cos_h, cos_1047_h;
        int R;
        int G;
        int B;
        H = 3.14159 * H / (float)180; // Convert to radians.
        if(H < 2.09439) {
            cos_h = Math.cos(H);
            cos_1047_h = Math.cos(1.047196667 - H);
            R = H<1.047198?255:(int)(255*(1+cos_h/cos_1047_h)/(1+(1-cos_h/cos_1047_h)));
            G = H<1.047198?(int)(255*(1+(1-cos_h/cos_1047_h))/(1+cos_h/cos_1047_h)):255;
            B = (int)0;
        } else if(H < 4.188787) {
            H = H - 2.09439;
            cos_h = Math.cos(H);
            cos_1047_h = Math.cos(1.047196667-H);
            G = H<1.047198?255:(int)(255*(1+cos_h/cos_1047_h)/(1+(1-cos_h/cos_1047_h)));
            B = H<1.047198?(int)(255*(1+(1-cos_h/cos_1047_h))/(1+cos_h/cos_1047_h)):255;
            R = 0;
        } else {
            H = H - 4.188787;
            cos_h = Math.cos(H);
            cos_1047_h = Math.cos(1.047196667-H);
            B = H<1.047198?255:(int)(255*(1+cos_h/cos_1047_h)/(1+(1-cos_h/cos_1047_h)));
            R = H<1.047198?(int)(255*(1+(1-cos_h/cos_1047_h))/(1+cos_h/cos_1047_h)):255;
            G = 0;
        }
        dR=(double)R;
        dB=(double)B;
        dG=(double)G;

        String Red = Integer.toHexString(R);
        if(Red.length()<2){
            Red="0"+Red;
        }
        String Green = Integer.toHexString(G);
        if(Green.length()< 2){
            Green="0"+Green;
        }
        String Blue = Integer.toHexString(B);
        if(Blue.length()<2){
            Blue="0"+Blue;
        }
        return Red+Green+Blue;

    }
    void broadCastLightValue(){
        final Intent intent = new Intent(ACTION_LAMP_RGB_COLOR_CHANGED);
        intent.putExtra(EXTRA_LAMP_RGB_COLOR_R,dR);
        intent.putExtra(EXTRA_LAMP_RGB_COLOR_G,dG);
        intent.putExtra(EXTRA_LAMP_RGB_COLOR_B,dB);
        getActivity().sendBroadcast(intent);
    }

}
