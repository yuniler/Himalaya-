package cn.jkdev.hiximalaya.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

import cn.jkdev.hiximalaya.DetailActivity;
import cn.jkdev.hiximalaya.R;
import cn.jkdev.hiximalaya.adapters.RecommendListAdapter;
import cn.jkdev.hiximalaya.base.BaseFragment;
import cn.jkdev.hiximalaya.buildins.UTUtil;
import cn.jkdev.hiximalaya.interfaces.IRecommendViewCallback;
import cn.jkdev.hiximalaya.presenters.AlbumDetailPresenter;
import cn.jkdev.hiximalaya.presenters.RecommendPresenter;
import cn.jkdev.hiximalaya.utils.LogUtil;
import cn.jkdev.hiximalaya.views.UILoader;

public class RecommentFragment extends BaseFragment implements IRecommendViewCallback, UILoader.OnRetryClickListener, RecommendListAdapter.OnRecommendItemClickListener {
    private static final String TAG = "";
    private View mRootView;
    private RecyclerView mRecommendRv;
    private RecommendListAdapter mRecommendListAdapter;
    private RecommendPresenter mRecommendPresenter;
    private UILoader mUiLoader;

    @Override
    protected View onSubViewLoaded(final LayoutInflater layoutInflater, ViewGroup container) {

        mUiLoader = new UILoader(getContext()) {
            @Override
            protected View getSuccessView(ViewGroup container) {
                return createSuccessView(layoutInflater,container);
            }
        };




        //获取逻辑层的对象
        mRecommendPresenter = RecommendPresenter.getInstance();
        //先设置通知接口的对象
        mRecommendPresenter.registerViewCallback(this);
        //获取推荐列表
        mRecommendPresenter.getRecommendList();

        //与父类解除绑定
        if (mUiLoader.getParent() instanceof ViewGroup) {
            ((ViewGroup)mUiLoader.getParent()).removeView(mUiLoader);
        }

        mUiLoader.setOnRetryClickListener(this);

        //返回view，显示在页面上
//        return mRootView;
        return mUiLoader;
    }

    /**
     * 成功后显示的内容
     * @return
     */
    private View createSuccessView(LayoutInflater layoutInflater,ViewGroup container) {
        //view加载完成页面
        mRootView = layoutInflater.inflate(R.layout.fragment_rercommend,container,false);

        //RecycleView的使用
        //1.找到控件
        mRecommendRv = mRootView.findViewById(R.id.recommend_list);
        TwinklingRefreshLayout twinklingRefreshLayout = mRootView.findViewById(R.id.over_scroll_view);
        twinklingRefreshLayout.setPureScrollModeOn();
        //2.设置布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecommendRv.setLayoutManager(linearLayoutManager);
        mRecommendRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UTUtil.dip2px(view.getContext(),5);
                outRect.bottom = UTUtil.dip2px(view.getContext(), 5);
                outRect.left = UTUtil.dip2px(view.getContext(),5);
                outRect.right = UTUtil.dip2px(view.getContext(),5);
            }
        });
        //3.设置适配器
        mRecommendListAdapter = new RecommendListAdapter();
        mRecommendRv.setAdapter(mRecommendListAdapter);
        mRecommendListAdapter.setOnRecommendItemClickListner(this);

        return mRootView;
    }


    private void upRecommendUI(List<Album> albumList) {

    }

    @Override
    public void onRecommendListLoaded(List<Album> result) {
        LogUtil.d(TAG,"onRecommendListLoaded");
        //当我们获取推荐内容成功时，这个方法会被调用
        //数据回来后更新UI
        //把数据拿回来，并且更新UI
        mRecommendListAdapter.setData(result);
        mUiLoader.updataStatus(UILoader.UIStatus.SUCCESS);
    }
    @Override
    public void onNetworkError() {
        LogUtil.d(TAG,"onNetworkError");
        mUiLoader.updataStatus(UILoader.UIStatus.NEWWORK_ERROR);
    }
    @Override
    public void onEmpty() {
        LogUtil.d(TAG,"onEmpty");
        mUiLoader.updataStatus(UILoader.UIStatus.EMPTY);
    }
    @Override
    public void onLoading() {
        LogUtil.d(TAG,"onLoading");
        mUiLoader.updataStatus(UILoader.UIStatus.LOADING);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //取消接口注册
        if (mRecommendPresenter != null){
            mRecommendPresenter.unRegisterViewCallback(this);
        }
    }

    @Override
    public void onRetryClick() {
        //表示网络不佳时，用户点击的重试
        if (mRecommendPresenter != null) {
            mRecommendPresenter.getRecommendList();//重新获取推荐内容
        }
    }

    @Override
    public void onItemClick(int position,Album album) {
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);
        //item被点击,跳转到详情页面
        Intent intent = new Intent(getContext(), DetailActivity.class);
        startActivity(intent);

    }

}
