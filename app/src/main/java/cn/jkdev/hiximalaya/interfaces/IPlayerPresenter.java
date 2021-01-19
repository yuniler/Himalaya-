package cn.jkdev.hiximalaya.interfaces;

import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import cn.jkdev.hiximalaya.base.IBasePresenter;

public interface IPlayerPresenter extends IBasePresenter<IPlayerCallback> {

    /**
     * 播放
     */
    void play();

    /**
     * 暂停
     */
    void pause();

    /**
     * 停止播放
     */
    void stop();

    /**
     * 上一首
     */
    void playPre();

    /**
     * 下一首
     */
    void playNext();

    /**
     * 切换播放模式
     */
    void switchPlayMode(XmPlayListControl.PlayMode mode);

    /**
     * 获取播放列表
     */
    void getPlayList();

    /**
     * 根据节目位置进行播放
     */
    void playByIndex(int index);

    /**
     * 切换播放进度
     * @param progress
     */
    void seekTo(int progress);

    /**
     * 判断是否在播放
     * @return
     */
    boolean isPlaying();

    /**
     * 将播放器列表内容反转
     */
    void reversePlayList();

    /**
     * 播放专辑的第一首节目
     * @param id
     */
    void playByAlbumId(long id);
}
