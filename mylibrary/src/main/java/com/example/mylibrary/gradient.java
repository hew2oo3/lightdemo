package com.example.mylibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by hew_2 on 2018/11/3.
 */

public class gradient extends View {
    public int coldcolor;
    public int warmcolor;
    public gradient(Context context) {
            super(context);
        }
    public gradient(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public gradient(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            //获取View的宽高
            int width = getWidth();
            int height = getHeight();
            int colorStart = coldcolor;
            int colorEnd = warmcolor;

            Paint paint = new Paint();
            LinearGradient backGradient = new LinearGradient(0, 0, 0, height, new int[]{colorStart ,colorEnd}, null, Shader.TileMode.CLAMP);
            paint.setShader(backGradient);
            canvas.drawRect(0, 0, width, height, paint);
        }
}
