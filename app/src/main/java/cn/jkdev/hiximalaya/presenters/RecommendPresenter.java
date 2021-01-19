package cn.jkdev.hiximalaya.presenters;

import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jkdev.hiximalaya.api.XimalayApi;
import cn.jkdev.hiximalaya.interfaces.IRecommendPresenter;
import cn.jkdev.hiximalaya.interfaces.IRecommendViewCallback;
import cn.jkdev.hiximalaya.utils.Constants;
import cn.jkdev.hiximalaya.utils.LogUtil;

/**
 * 逻辑层
 */
public class RecommendPresenter implements IRecommendPresenter {
    //presenter才是具体的逻辑实现
    private static final String TAG = "RecommendPresenter";

    private List<IRecommendViewCallback> mCallbacks = new ArrayList<>();
    private List<Album> mCurrentRecommend = null;


    private RecommendPresenter(){}//构造器
    private static RecommendPresenter sInstance = null;

    /**如果别的地方用的上。。。
     * 单列模式--懒汉
     * @return
     */
    public static RecommendPresenter getInstance(){
        if (sInstance==null) {
            synchronized (RecommendPresenter.class){
                if (sInstance == null){
                    sInstance = new RecommendPresenter();
                }
            }
        }
        return sInstance;

    }

    /**
     * 获取当前推荐专辑列表
     * @return 推荐专辑列表，使用前判空
     */
    public List<Album> getCurrentRecommend(){
        return  mCurrentRecommend;
    }

    //获取推荐内容
    /**
     * 获取推荐内容，其实就是猜你喜欢
     * 这个接口：3.10.6 获取猜你喜欢专辑
     */
    @Override
    public void getRecommendList() {
        //最开始的加载--get后就要loading
        onLoading();
        XimalayApi ximalayApi = XimalayApi.getXimalayApi();

        ximalayApi.getRecommendList(new IDataCallBack<GussLikeAlbumList>() {
            @Override
            public void onSuccess(GussLikeAlbumList gussLikeAlbumList) {
                LogUtil.d(TAG,"thread name -->" + Thread.currentThread().getName());
                //数据获取成功
                if (gussLikeAlbumList != null){
                    List<Album> albumList = gussLikeAlbumList.getAlbumList();
                    //数据回来获取UI
                    //upRecommendUI(albumList);
                    handleRecommendResult(albumList);//传入

                }
            }

            @Override
            public void onError(int i, String s) {
                //数据获取失败
                LogUtil.d(TAG,"error --> " + i);
                LogUtil.d(TAG,"errorMsg --> " + s);
                handleError();
            }
        });
    }

    private void handleError() {
        if (mCallbacks != null){
            for (IRecommendViewCallback callback : mCallbacks){
                callback.onNetworkError();
            }
        }
    }

    private void handleRecommendResult(List<Album> albumList) {
        //通知UI更新
        if (albumList != null){
//            albumList.clear();-->测试内容为空
            if (albumList.size() == 0){
                for (IRecommendViewCallback callback : mCallbacks){
                    callback.onEmpty();
                }
            }else{
                for (IRecommendViewCallback callback : mCallbacks){
                    callback.onRecommendListLoaded(albumList);
                }
                //保持引用
                this.mCurrentRecommend = albumList;
            }
        }
    }
    public void onLoading(){
        for (IRecommendViewCallback callBack : mCallbacks){
            callBack.onLoading();
        }
    }

    @Override
    public void pullResearchMore() {

    }

    @Override
    public void loadMore() {

    }

    @Override
    public void registerViewCallback(IRecommendViewCallback callBack) {
        //获取UI
        if (mCallbacks != null && !mCallbacks.contains(callBack)){
            mCallbacks.add(callBack);//避免重复加入
        }

    }



    @Override
    public void unRegisterViewCallback(IRecommendViewCallback callBack) {

        if (mCallbacks != null){
            mCallbacks.remove(mCallbacks);
        }
    }
}
