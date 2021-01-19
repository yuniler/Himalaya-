package cn.jkdev.hiximalaya.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import cn.jkdev.hiximalaya.R;

@SuppressLint("AppCompatCustomView")
public class LoadingView extends ImageView {
    //旋转角度
    private int rotateDegree = 0;

    private boolean mNeedRotate = false;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //设置图标
        setImageResource(R.mipmap.loading);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mNeedRotate = true;//绑定为true
        //绑定到windows时
        post(new Runnable() {//切换线程
            @Override
            public void run() {
                rotateDegree += 30;//+=
                rotateDegree = rotateDegree <= 360 ? rotateDegree : 0;
                invalidate();//调用onDraw
                //是否继续旋转
                if (mNeedRotate){
                    postDelayed(this,200);
                }
            }
        });

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //解除绑定到windows时
        mNeedRotate = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /**
         * 旋转角度
         * 旋转的x坐标
         * 旋转的Y坐标
         */
        canvas.rotate(rotateDegree, getWidth() / 2, getHeight() / 2);//中心点
        super.onDraw(canvas);
    }
}
