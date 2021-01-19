package cn.jkdev.hiximalaya.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.jkdev.hiximalaya.R;
import cn.jkdev.hiximalaya.base.BaseApplication;

public abstract class UILoader extends FrameLayout {

    private View mLoadingView;
    private View mSuccessView;
    private View mNetworkErrorView;
    private View mEmptyView;
    private OnRetryClickListener mOnRetryClickListener = null;

    public enum UIStatus {
        LOADING, SUCCESS, NEWWORK_ERROR, EMPTY, NONE;
    }

    public UIStatus mCurrentStatus = UIStatus.NONE;//设置当前状态

    /**
     * 调this，为了，最后同一个指向同一个出口，入口。
     *
     * @param context
     */
    public UILoader(@NonNull Context context) {
        this(context, null);
    }

    public UILoader(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UILoader(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化
        init();
    }
    /**
     * 更新UI
     */
    public void updataStatus(UIStatus status){
        mCurrentStatus = status;
        //更新UI一定要在主线程上
        BaseApplication.getsHandler().post(new Runnable() {
            @Override
            public void run() {
                switchUIByCurrentStatus();
            }
        });
    }

    /**
     * 初始化UI
     * 相关view加载进来
     */
    private void init() {
        switchUIByCurrentStatus();
    }

    private void switchUIByCurrentStatus() {
        //加载中
        if (mLoadingView == null) {
            mLoadingView = getLoadingView();
            addView(mLoadingView);
        }
        //根据状态设置是否可见
        mLoadingView.setVisibility(mCurrentStatus == UIStatus.LOADING ? VISIBLE : GONE);

        //加载成功       -->不知道要展现什么，设抽象类，子类加载
        if (mSuccessView == null) {
            mSuccessView = getSuccessView(this);
            addView(mSuccessView);
        }
        //根据状态设置是否可见
        mSuccessView.setVisibility(mCurrentStatus == UIStatus.SUCCESS ? VISIBLE : GONE);

        //网络错误页面
        if (mNetworkErrorView == null) {
            mNetworkErrorView = getNetworkErrorView();
            addView(mNetworkErrorView);
        }
        //根据状态设置是否可见
        mNetworkErrorView.setVisibility(mCurrentStatus == UIStatus.NEWWORK_ERROR ? VISIBLE : GONE);

        //数据为空
        if (mEmptyView == null) {
            mEmptyView = getEmptyView();
            addView(mEmptyView);
        }
        //根据状态设置是否可见
        mEmptyView.setVisibility(mCurrentStatus == UIStatus.EMPTY ? VISIBLE : GONE);
    }

    /**
     * 初始化完成，根据状态去显示
     * @return
     */
    private View getEmptyView() {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view,this,false);
    }

    private View getNetworkErrorView() {
        View networkErrorView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_error_view,this,false);
        networkErrorView.findViewById(R.id.Network_error_icon).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //：点击后重新获取数据
                if (mOnRetryClickListener != null){
                    mOnRetryClickListener.onRetryClick();
                }
            }
        });
        return networkErrorView;
    }

    //container--填充的容器
    protected abstract View getSuccessView(ViewGroup container);//子类去实现
//    {
//        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_loading_view,this,false);
//    }

    private View getLoadingView() {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_loading_view,this,false);
    }

    public void setOnRetryClickListener(OnRetryClickListener listener){
        this.mOnRetryClickListener = listener;
    }

    public interface OnRetryClickListener{
        void onRetryClick();
    }
}
