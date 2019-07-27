package com.example.mylibrary;
import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by hew_2 on 2018/5/25.
 */

public class SingleFingerGestureDetector extends BaseGestureDetector {


    /**
     * Listener which must be implemented which is used by RotateGestureDetector
     * to perform callbacks to any implementing class which is registered to a
     * RotateGestureDetector via the constructor.
     *
     * @see RotateGestureDetector.SimpleOnRotateGestureListener
     */
    public interface OnRotateGestureListener {
        public boolean onRotate(SingleFingerGestureDetector detector);
        public boolean onRotateBegin(SingleFingerGestureDetector detector);
        public void onRotateEnd(SingleFingerGestureDetector detector);
    }

    /**     * Helper class which may be extended and where the methods may be
     * implemented. This way it is not necessary to implement all methods
     * of OnRotateGestureListener.
     */
    public static class SimpleOnRotateGestureListener implements SingleFingerGestureDetector.OnRotateGestureListener {
        public boolean onRotate(SingleFingerGestureDetector detector) {
            return false;
        }

        public boolean onRotateBegin(SingleFingerGestureDetector detector) {
            return true;
        }

        public void onRotateEnd(SingleFingerGestureDetector detector) {
            // Do nothing, overridden implementation may be used
        }
    }


    private final SingleFingerGestureDetector.OnRotateGestureListener mListener;
    private boolean mSloppyGesture;

    public SingleFingerGestureDetector(Context context, SingleFingerGestureDetector.OnRotateGestureListener listener) {
        super(context);
        mListener = listener;
        ViewConfiguration config = ViewConfiguration.get(context);
        mEdgeSlop = config.getScaledEdgeSlop();
    }

    @Override
    protected void handleStartProgressEvent(int actionCode, MotionEvent event){
        switch (actionCode) {
            case MotionEvent.ACTION_DOWN:
                // At least the second finger is on screen now

                resetState(); // In case we missed an UP/CANCEL event
                mPrevEvent = MotionEvent.obtain(event);
                mTimeDelta = 0;

                updateStateByEvent(event);


                // See if we have a sloppy gesture
                mSloppyGesture = isSloppyGesture(event);
                if(!mSloppyGesture){
                    // No, start gesture now
                    mGestureInProgress = mListener.onRotateBegin(this);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (!mSloppyGesture) {
                    break;
                }

                // See if we still have a sloppy gesture
                mSloppyGesture = isSloppyGesture(event);
                if(!mSloppyGesture){
                    // No, start normal gesture now
                    mGestureInProgress = mListener.onRotateBegin(this);
                }

                break;

            case MotionEvent.ACTION_UP:
                if (!mSloppyGesture) {
                    break;
                }

                break;
        }
    }


    @Override
    protected void handleInProgressEvent(int actionCode, MotionEvent event){
        switch (actionCode) {
            case MotionEvent.ACTION_UP:
                // Gesture ended but
                updateStateByEvent(event);


                if (!mSloppyGesture) {
                    mListener.onRotateEnd(this);
                }

                resetState();
                break;

            case MotionEvent.ACTION_CANCEL:
                if (!mSloppyGesture) {
                    mListener.onRotateEnd(this);
                }

                resetState();
                break;

            case MotionEvent.ACTION_MOVE:
                updateStateByEvent(event);

                // Only accept the event if our relative pressure is within
                // a certain limit. This can help filter shaky data as a
                // finger is lifted.
                if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD) {
                    final boolean updatePrevious = mListener.onRotate(this);
                    if (updatePrevious) {
                        mPrevEvent.recycle();
                        mPrevEvent = MotionEvent.obtain(event);
                    }
                }
                break;
        }
    }

    @Override
    protected void resetState() {
        super.resetState();
        mSloppyGesture = false;
    }


    /**
     * Return the rotation difference from the previous rotate event to the current
     * event.
     *
     * @return The current rotation //difference in degrees.
     */
    public float getRotationDegreesDelta(float imageCenterX, float imageCenterY) {

        final float a=distance4PointF(imageCenterX,px0,imageCenterY,py0);
        final float b=distance4PointF(px0,cx0,py0,cy0);
        final float c=distance4PointF(imageCenterX,cx0,imageCenterY,cy0);
        final float cosArc = (a*a+c*c-b*b)/(2*a*c);
        float newDegree = (float) Math.toDegrees(Math.acos(cosArc));
        cpx=(px0-imageCenterX);
        cpy=(py0-imageCenterY);
        ccx=(cx0-imageCenterX);
        ccy=(cy0-imageCenterY);
        float result = cpx*ccy-cpy*ccx;
        if (result < 0) {
           newDegree = -newDegree;
        }
        if (Float.valueOf(newDegree).isNaN())
        {return 0;}
        return newDegree;
    }
    private final float mEdgeSlop;
    private float mRightSlopEdge;
    private float mBottomSlopEdge;


    float imgCenterX;
    float imgCenterY;
    float ccx;
    float ccy;
    float cpx;
    float cpy;
    float cx0;
    float cy0;
    float px0;
    float py0;

    private float mCurrLen;
    private float mPrevLen;
    PointF centerToProMove;
    PointF centerToCurMove;

    private float distance4PointF(float x1, float x2, float y1,float y2) {
        float disX = Math.abs(x2-x1);
        float disY = Math.abs(y2-y1);
        return (float) Math.sqrt(disX*disX+disY*disY);
    }
    protected void updateStateByEvent(MotionEvent curr) {
        super.updateStateByEvent(curr);

        final MotionEvent prev = mPrevEvent;

        mCurrLen = -1;
        mPrevLen = -1;


            // Previous
        px0 = prev.getX();
        py0 = prev.getY();



            // Current
        cx0 = curr.getX();
        cy0 = curr.getY();


    }
    /**
     * Check if we have a sloppy gesture. Sloppy gestures can happen if the edge
     * of the user's hand is touching the screen, for example.
     *
     * @param event
     * @return
     */
    protected boolean isSloppyGesture(MotionEvent event){
        // As orientation can change, query the metrics in touch down
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        mRightSlopEdge = metrics.widthPixels - mEdgeSlop;
        mBottomSlopEdge = metrics.heightPixels - mEdgeSlop;

        final float edgeSlop = mEdgeSlop;
        final float rightSlop = mRightSlopEdge;
        final float bottomSlop = mBottomSlopEdge;

        final float x0 = event.getRawX();
        final float y0 = event.getRawY();


        boolean p0sloppy = x0 < edgeSlop || y0 < edgeSlop
                || x0 > rightSlop || y0 > bottomSlop;


        if (p0sloppy) {
            return true;
        }
        return false;
    }

}
