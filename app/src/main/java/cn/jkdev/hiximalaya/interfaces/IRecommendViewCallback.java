package cn.jkdev.hiximalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

/**
 * UI通知接口
 * 逻辑层通知界面更新的接口
 */
public interface IRecommendViewCallback {

    /**
     * 获取推荐内容的结果-->成功
     * @param result
     */
    void onRecommendListLoaded(List<Album> result);

    /**
     * 网络错误
     */
    void onNetworkError();

    /**
     * 数据为空
     */
    void onEmpty();

    /**
     * 正在加载
     */
    void onLoading();
}
