package com.example.xeno14.irtank;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by ryo on 16/05/22.
 */
public class LeverSwitchView extends View {

    float touchY;
    Paint paint = new Paint(Color.GRAY);
    Callback onDownCallback, onMoveCallback, onUpCallback;
    int myWidth = 0, myHeight = 0;

    public LeverSwitchView(Context context, AttributeSet attr){
        super(context, attr);
        init();
    }

    public LeverSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public LeverSwitchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public LeverSwitchView(Context context) {
        super(context);
        init();
    }

    private void init() {
        touchY = myHeight / 2;
        onUpCallback = new DummyCallback();
        onDownCallback = new DummyCallback();
        onMoveCallback = new DummyCallback();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        myWidth = w;
        myHeight = h;

        touchY = myHeight / 2;
    }

    public float getBarHeight() {
        return myHeight / 4;
    }

    public float getYmin() {
        return this.getBarHeight() / 2;
    }

    public float getYmax() {
        return myHeight - this.getYmin();
    }

    private float restrictTouchY(float y) {
        float ymin = this.getYmin();
        float ymax = this.getYmax();
        if (y < ymin) y = ymin;
        else if (y > ymax) y = ymax;
        return y;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float h = this.getBarHeight();
        canvas.drawRect(0, touchY - h / 2, this.getWidth(), touchY + h / 2, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchY = restrictTouchY(event.getY());
                onDownCallback.apply(getValue());
                break;
            case MotionEvent.ACTION_MOVE:
                touchY = restrictTouchY(event.getY());
                onMoveCallback.apply(getValue());
                break;
            case MotionEvent.ACTION_UP:
                touchY = myHeight/2;
                onUpCallback.apply(getValue());
                break;
            case MotionEvent.ACTION_CANCEL:
                touchY = myHeight/2;
                onUpCallback.apply(getValue());
                break;
        }
        invalidate();
        return true;
    }

    public float getValue() {
        float ymin = this.getBarHeight() / 2;
        float ymax = myHeight - this.getBarHeight() / 2;
        float val = (touchY - ymin) / (ymax - ymin);     // map to [0,1]
        val = val * 2 - 1;      //map to [-1, 1]
        val *= -1;              // touch coordinate is opposite to math
        return val;
    }

    public interface Callback {
        public void apply(float value);
    }

    private class DummyCallback implements Callback {
        @Override
        public void apply(float value) {
            // do nothing
        }
    }

    public void setOnDownCallback(Callback onDownCallback) {
        this.onDownCallback = onDownCallback;
    }

    public void setOnMoveCallback(Callback onMoveCallback) {
        this.onMoveCallback = onMoveCallback;
    }

    public void setOnUpCallback(Callback onDownCallback) {
        this.onUpCallback = onDownCallback;
    }
}
