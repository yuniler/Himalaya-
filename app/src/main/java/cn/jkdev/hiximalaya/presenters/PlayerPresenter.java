package cn.jkdev.hiximalaya.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.advertis.Advertis;
import com.ximalaya.ting.android.opensdk.model.advertis.AdvertisList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.advertis.IXmAdsStatusListener;
import com.ximalaya.ting.android.opensdk.player.constants.PlayerConstants;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.jkdev.hiximalaya.api.XimalayApi;
import cn.jkdev.hiximalaya.base.BaseApplication;
import cn.jkdev.hiximalaya.interfaces.IPlayerCallback;
import cn.jkdev.hiximalaya.interfaces.IPlayerPresenter;
import cn.jkdev.hiximalaya.utils.LogUtil;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;

public class PlayerPresenter implements IPlayerPresenter, IXmAdsStatusListener, IXmPlayerStatusListener {//添加回调
    //数据从presenter层来-->控制player的逻辑层---播放的一个类，一个实例
    private List<IPlayerCallback> mIPlayerCallbacks = new ArrayList<>();//callback注册回来时，用集合保存

    private static final String TAG = "PlayerPresenter";
    private final XmPlayerManager mPlayerManager;
    private Track mCurrentTrack;
    public static final int DEFAULT_PLAY_INDEX = 0;
    private int mCurrentIndex = DEFAULT_PLAY_INDEX;
    private final SharedPreferences mPlayModSp;
    private XmPlayListControl.PlayMode mCurrentPlayMode = PLAY_MODEL_LIST;
    private boolean mIsReverse = false;

    /**
     * INT
     * /**1。默认：PLAY_MODEL_LIST
     * * 2.列表播放：PLAY_MODEL_LIST_LOOP
     * * 3.随机播放：PLAY_MODEL_RANDOM
     * * 4.单曲循环：PLAY_MODEL_SINGLE_LOOP
     */
    public static final int PLAY_MODEL_LIST_INT = 0;
    public static final int PLAY_MODEL_LIST_LOOP_INT = 1;
    public static final int PLAY_MODEL_RANDOM_INT = 2;
    public static final int PLAY_MODEL_SINGLE_LOOP_INT = 3;

    //sp key and name
    public static final String PLAY_MODE_SP_NAME = "PlayMod";
    public static final String PLAY_MODE_SP_KEY = "currentPlayMode";
    private int mCurrentProgressPosition = 0;
    private int mProgressDuration = 0;


    //这个地方拿到presenter直接调用就完事--->给个单列

    private PlayerPresenter() {//播放列表。XmPlayerManager-->填装数据
        mPlayerManager = XmPlayerManager.getInstance(BaseApplication.getAppContext());
        //注册进去、、---广告的接口
        mPlayerManager.addAdsStatusListener(this);
        //注册播放器状态相关的接口
        mPlayerManager.addPlayerStatusListener(this);
        //需要记录当前播放模式-->数据持久化
        mPlayModSp = BaseApplication.getAppContext().getSharedPreferences(PLAY_MODE_SP_NAME, Context.MODE_PRIVATE);
    }

    private static PlayerPresenter sPlayerPresenter;

    public static PlayerPresenter getPlayerPresenter() {
        if (sPlayerPresenter == null) {
            synchronized (PlayerPresenter.class) {
                if (sPlayerPresenter == null) {
                    sPlayerPresenter = new PlayerPresenter();
                }
            }
        }
        return sPlayerPresenter;
    }

    private boolean isPlayListSet = false;

    //暴露接口，因为从列表进来到播放器
    public void setPlayList(List<Track> list, int playIndex) {//播放列表，播放的index
        if (mPlayerManager != null) {//manager传到列表
            mPlayerManager.setPlayList(list, playIndex);
            isPlayListSet = true;
            mCurrentTrack = list.get(playIndex);

        } else {
            LogUtil.d(TAG, "mXmPlayerManager is null");
        }


    }

    //presenter才是逻辑实现
    //播放器初始化
    @Override
    public void play() {

        if (isPlayListSet) {
//            LogUtil.d(TAG,playerStatus + "playerStatus");
            mPlayerManager.play();
        }
    }

    @Override
    public void pause() {
        if (mPlayerManager != null) {
            mPlayerManager.pause();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void playPre() {
        if (mPlayerManager != null) {
            mPlayerManager.playPre();
        }

    }

    @Override
    public void playNext() {
        if (mPlayerManager != null) {
            mPlayerManager.playNext();
        }
    }

    /**
     * 判断是否有播放列表的节目列表
     *
     * @return
     */
    public boolean hasPlayList() {
        return isPlayListSet;
    }

    @Override
    public void switchPlayMode(XmPlayListControl.PlayMode mode) {
        if (mPlayerManager != null) {
            mCurrentPlayMode = mode;//切换点
            mPlayerManager.setPlayMode(mode);
            for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
                iPlayerCallback.onPlayModeChange(mode);
            }
            //保存到sp中
            SharedPreferences.Editor edit = mPlayModSp.edit();
            edit.putInt(PLAY_MODE_SP_KEY, getIntByPlayMode(mode));
            edit.commit();
        }

    }

    /**
     * INT
     * /**1。默认：PLAY_MODEL_LIST
     * * 2.列表播放：PLAY_MODEL_LIST_LOOP
     * * 3.随机播放：PLAY_MODEL_RANDOM
     * * 4.单曲循环：PLAY_MODEL_SINGLE_LOOP
     */
    private int getIntByPlayMode(XmPlayListControl.PlayMode mode) {
        switch (mode) {
            case PLAY_MODEL_LIST:
                return PLAY_MODEL_LIST_INT;
            case PLAY_MODEL_LIST_LOOP:
                return PLAY_MODEL_LIST_LOOP_INT;
            case PLAY_MODEL_RANDOM:
                return PLAY_MODEL_RANDOM_INT;
            case PLAY_MODEL_SINGLE_LOOP:
                return PLAY_MODEL_SINGLE_LOOP_INT;
        }
        return PLAY_MODEL_LIST_INT;
    }

    private XmPlayListControl.PlayMode getModeByIndex(int index) {
        switch (index) {
            case PLAY_MODEL_LIST_INT:
                return PLAY_MODEL_LIST;
            case PLAY_MODEL_LIST_LOOP_INT:
                return PLAY_MODEL_LIST_LOOP;
            case PLAY_MODEL_RANDOM_INT:
                return PLAY_MODEL_RANDOM;
            case PLAY_MODEL_SINGLE_LOOP_INT:
                return PLAY_MODEL_SINGLE_LOOP;
        }
        return PLAY_MODEL_LIST;
    }

    @Override
    public void getPlayList() {
        if (mPlayerManager != null) {
            List<Track> playList = mPlayerManager.getPlayList();//拿数据
            for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {//遍历数据集合
                iPlayerCallback.onListLoader(playList);
            }
        }


    }

    @Override
    public void playByIndex(int index) {
        //切换播放器到index位置进行播放
        if (mPlayerManager != null) {
            mPlayerManager.play(index);
        }
    }

    @Override
    public void seekTo(int progress) {
        //更新播放器的进度
        mPlayerManager.seekTo(progress);//逻辑层管理器绑定。
    }

    /**
     * 返回是否在播放
     *
     * @return
     */
    @Override
    public boolean isPlaying() {
        //返回是否正在播放
        return mPlayerManager.isPlaying();
    }

    @Override
    public void reversePlayList() {
        //把播放列表反转
        List<Track> playList = mPlayerManager.getPlayList();
        Collections.reverse(playList);
        //已经反转成功
        mIsReverse = !mIsReverse;
        //第一个参数是播放的列表，第二个参数是开始播放的下标
        //怎么变 = 新的下标 = 总的内容个数 - 1 - 当前的下标
        mCurrentIndex = playList.size() - 1 - mCurrentIndex;
        mPlayerManager.setPlayList(playList, mCurrentIndex);
        //更新UI
        mCurrentTrack = (Track) mPlayerManager.getCurrSound();//点播状态
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onListLoader(playList);
            iPlayerCallback.onTrackUpdata(mCurrentTrack, mCurrentIndex);
            iPlayerCallback.updateListOrder(mIsReverse);
        }

    }

    @Override
    public void playByAlbumId(long id) {
        //1.要获取专辑内容
        XimalayApi ximalayApi = XimalayApi.getXimalayApi();
        ximalayApi.getAlbumDetail(new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                //2.把专辑内容传给播放器
                List<Track> tracks = trackList.getTracks();
                if (trackList != null && tracks.size() > 0) {
                    mPlayerManager.setPlayList(tracks, DEFAULT_PLAY_INDEX);
                    isPlayListSet = true;
                    mCurrentTrack = tracks.get(DEFAULT_PLAY_INDEX);
                    mCurrentIndex = DEFAULT_PLAY_INDEX;
                }
            }

            @Override
            public void onError(int errorCode, String Msg) {
                LogUtil.d(TAG, "errorCode -- > " + errorCode);
                LogUtil.d(TAG, "Msg -- > " + Msg);
                Toast.makeText(BaseApplication.getAppContext(), "请求数据错误", Toast.LENGTH_SHORT).show();
            }
        }, (int) id, 1);

        //3.播放了..
    }

    /**
     * 用register将数据包起来，解决多个页面使用同一个presenter
     *
     * @param iPlayerCallback
     */
    @Override
    public void registerViewCallback(IPlayerCallback iPlayerCallback) {
        //通知当前的界面
        iPlayerCallback.onTrackUpdata(mCurrentTrack, mCurrentIndex);//注册
        //更新进度条
        iPlayerCallback.onProgressChange(mCurrentProgressPosition,mProgressDuration);
        //更新状态
        handlePlayState(iPlayerCallback);

        //从sp里拿--->数据持久化，拿状态
        int modeIndex = mPlayModSp.getInt(PLAY_MODE_SP_KEY, PLAY_MODEL_LIST_INT);
        //回显
        mCurrentPlayMode = getModeByIndex(modeIndex);//
        iPlayerCallback.onPlayModeChange(mCurrentPlayMode);
        if (!mIPlayerCallbacks.contains(iPlayerCallback)) {
            mIPlayerCallbacks.add(iPlayerCallback);
        }

    }

    private void handlePlayState(IPlayerCallback iPlayerCallback) {
        int playerStatus = mPlayerManager.getPlayerStatus();
        //根据当前状态调用接口方法
        if (PlayerConstants.STATE_STARTED == playerStatus) {
            iPlayerCallback.onPlayStart();
        } else {
            iPlayerCallback.onPlayPause();
        }
    }

    @Override
    public void unRegisterViewCallback(IPlayerCallback iPlayerCallback) {
        mIPlayerCallbacks.remove(iPlayerCallback);
    }

    //=========================广告相关的回调方法 start=============
    @Override
    public void onStartGetAdsInfo() {
        LogUtil.d(TAG, "onStartGetAdsInfo...");

    }

    @Override
    public void onGetAdsInfo(AdvertisList advertisList) {
        LogUtil.d(TAG, "onGetAdsInfo...");
    }

    @Override
    public void onAdsStartBuffering() {
        LogUtil.d(TAG, "onAdsStartBuffering...");
    }

    @Override
    public void onAdsStopBuffering() {
        LogUtil.d(TAG, "onAdsStopBuffering...");
    }

    @Override
    public void onStartPlayAds(Advertis advertis, int i) {
        LogUtil.d(TAG, "onStartPlayAds...");
    }

    @Override
    public void onCompletePlayAds() {
        LogUtil.d(TAG, "onCompletePlayAds...");
    }

    @Override
    public void onError(int what, int extra) {
        LogUtil.d(TAG, "onError..." + "what==>" + what + "extra ==>" + extra);
    }
    //=========================广告相关的回调方法 end=============

    //=========================播放器相关的回调接口 start=============
    @Override
    public void onPlayStart() {
        LogUtil.d(TAG, "onPlayStart...");//播放状态改变通知UI
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onPlayStart();
        }
    }

    @Override
    public void onPlayPause() {
        LogUtil.d(TAG, "onPlayPause...");
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onPlayPause();
        }
    }

    @Override
    public void onPlayStop() {
        LogUtil.d(TAG, "onPlayStop...");
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onPlayStop();
        }
    }

    @Override
    public void onSoundPlayComplete() {//播放完成
        LogUtil.d(TAG, "onSoundPlayComplete...");
    }

    //当prepared准备完成，才执行play，加一个判断
    @Override
    public void onSoundPrepared() {//播放器准备完成--->在进行播放
        LogUtil.d(TAG, "onSoundPrepared...");
        //播放器准备好时，修改播放模式，设置模式
        mPlayerManager.setPlayMode(mCurrentPlayMode);
//        LogUtil.d(TAG,"onSoundPrepared " + mXmPlayerManager.getPlayerStatus());//--->0,还未加载
        if (mPlayerManager.getPlayerStatus() == PlayerConstants.STATE_PREPARED) {
            mPlayerManager.play();
        }


    }

    /**
     * 当播放器进行改变时
     *
     * @param lastModel
     * @param curModel
     */
    @Override
    public void onSoundSwitch(PlayableModel lastModel, PlayableModel curModel) {
        LogUtil.d(TAG, "onSoundSwitch...");
        if (lastModel != null) {
            LogUtil.d(TAG, lastModel.getKind() + "lastModel");
        }
        LogUtil.d(TAG, curModel.getKind() + "curModel");
        //currModel表示当前播放的内容
        //getKind()获取当前它是什么类型
        //track表示是track类型
        //第一种写法--不推荐。。后台可能改字段
//        if ("track".equals(curModel.getKind())){
//            Track curTrack = (Track) curModel;
//            LogUtil.d(TAG,"title" + curTrack.getTrackTitle());
//        }
        //第二种写法
        mCurrentIndex = mPlayerManager.getCurrentIndex();
        if (curModel instanceof Track) {
            Track currentTrack = (Track) curModel;
            mCurrentTrack = currentTrack;
//            LogUtil.d(TAG,"title-->" + currentTrack.getTrackTitle());
            //更新UI
            for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {//从数组拿-->得到标题
                iPlayerCallback.onTrackUpdata(mCurrentTrack, mCurrentIndex);
            }

        }

    }

    @Override
    public void onBufferingStart() {//开始缓冲
        LogUtil.d(TAG, "onBufferingStart...");
    }

    @Override
    public void onBufferingStop() {//缓冲完成
        LogUtil.d(TAG, "onBufferingStop...");
    }

    @Override
    public void onBufferProgress(int progress) {//缓冲进度
        LogUtil.d(TAG, "onBufferProgress..." + progress);
    }

    @Override
    public void onPlayProgress(int currPos, int duration) {//更新进程。current ,duration
        this.mCurrentProgressPosition = currPos;
        this.mProgressDuration = duration;
        //单位是毫秒
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onProgressChange(currPos, duration);
        }
//        LogUtil.d(TAG, "onPlayProgress..." + currPos + " duration" + duration);
    }

    @Override
    public boolean onError(XmPlayerException e) {
        LogUtil.d(TAG, "onError e -->.." + e);
        return false;
    }
    //=========================播放器相关的回调接口 end=============
}
