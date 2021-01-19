package cn.jkdev.hiximalaya.presenters;

import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jkdev.hiximalaya.api.XimalayApi;
import cn.jkdev.hiximalaya.interfaces.IAlbumDetailPresenter;
import cn.jkdev.hiximalaya.interfaces.IAlbumDetailViewCallback;
import cn.jkdev.hiximalaya.utils.Constants;
import cn.jkdev.hiximalaya.utils.LogUtil;

public class AlbumDetailPresenter implements IAlbumDetailPresenter {

    //presenter才是具体的逻辑实现
    private List<IAlbumDetailViewCallback> mCallbacks = new ArrayList<>();//逻辑层的数组
    private List<Track> mTracks = new ArrayList<>();//集合装--，最开始的列表
    private Album mTargetAlbum = null;//
    private static final String TAG = "AlbumDetailPresenter";
    //当前的专辑ID
    private int mCurrentAlbumId = -1;
    //当前页
    private int mCurrentPageIndex = 0;

    //多地方调用--->设计单列
    private AlbumDetailPresenter() {
    }

    private static AlbumDetailPresenter sInstance = null;

    public static AlbumDetailPresenter getInstance() {
        if (sInstance == null) {
            synchronized (AlbumDetailPresenter.class) {
                if (sInstance == null) {
                    sInstance = new AlbumDetailPresenter();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void pullRefreshMore() {

    }

    @Override
    public void loadMore() {
        //去加载更多内容
        mCurrentPageIndex++;
        //传入true，表示结果会追加到列表的后方
        doLoaded(true);

    }
    public void doLoaded(final boolean isLoaderMore){
        XimalayApi ximalayApi = XimalayApi.getXimalayApi();

        //进行回调
        ximalayApi.getAlbumDetail(new IDataCallBack<TrackList>() {
            //请求
            @Override
            public void onSuccess(TrackList trackList) {
                if (trackList != null) {
                    List<Track> tracks = trackList.getTracks();//tracks就是里面播放的内容,当前track保存下来，进行叠加
                    LogUtil.d(TAG, "tracks size " + tracks.size());
                    //针对tracks还要有要求
                    if (isLoaderMore) {
                        //上拉刷新，结果放到后
                        mTracks.addAll(tracks);//添加进来列表
                        int size = tracks.size();
                        handlerLoaderMoreResult(size);
                    } else {
                        //下拉加载，结果放到前
                        mTracks.addAll(0,tracks);
                    }

                    handlerAlbumDetailResult(mTracks);//刷新新列表---注意列表，别拿错了，原列表和加载的列表
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                //
                if (isLoaderMore) {
                    mCurrentPageIndex--;
                }
                LogUtil.d(TAG, "errorCode -->" + errorCode);
                LogUtil.d(TAG, "errorMsg --> " + errorMsg);
                handlerError(errorCode, errorMsg);
            }
        },mCurrentAlbumId,mCurrentPageIndex);
    }

    /**
     * 处理加载更多的结果
     * @param size
     */
    private void handlerLoaderMoreResult(int size) {
        for (IAlbumDetailViewCallback callback : mCallbacks) {
            callback.onLoaderMoreFinished(size);
        }
    }

    @Override
    public void getAlubmDetail(int albumId, int page) {
        mTracks.clear();//第一次进来
        this.mCurrentAlbumId = albumId;//ID
        this.mCurrentPageIndex = page;
        //根据页码和专辑id去获取列表
        doLoaded(false);


    }

    /**
     * 如果是发送错误，那么就通知UI
     *
     * @param errorCode
     * @param errorMsg
     */
    private void handlerError(int errorCode, String errorMsg) {
        for (IAlbumDetailViewCallback callback : mCallbacks) {
            callback.onNetworkError(errorCode, errorMsg);
        }
    }

    private void handlerAlbumDetailResult(List<Track> tracks) {

        for (IAlbumDetailViewCallback mCallback : mCallbacks) {
            mCallback.onDetailListLoaded(tracks);
        }
    }

    @Override
    public void registerViewCallback(IAlbumDetailViewCallback detailViewCallBack) {
        if (!mCallbacks.contains(detailViewCallBack)) {//操作数组内的数据
            mCallbacks.add(detailViewCallBack);
            if (mTargetAlbum != null) {
                detailViewCallBack.onAlbumLoaded(mTargetAlbum);
            }
        }


    }


    @Override
    public void unRegisterViewCallback(IAlbumDetailViewCallback detailViewCallBack) {
        mCallbacks.remove(detailViewCallBack);
    }

    /**
     * 跳转设置数据
     */
    public void setTargetAlbum(Album targetAlbum) {
        this.mTargetAlbum = targetAlbum;
    }
}
