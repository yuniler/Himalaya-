package cn.jkdev.hiximalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;

import java.util.List;

public interface ISearchCallback {
    //通知UI更新接口

    /**
     * 搜索结果的回调方法
     * @param result
     */
    void onSearchRecsultLoaded(List<Album> result);

    /**
     *获取推荐热词的结果回调方法
     */
    void onHotWordLoaded(List<HotWord> hotWordList);//搜索完后返回搜索的热词

    /**
     * 加载更多的结果返回
     * @param result 结果
     * @param isOkey true 表加载更多， false 没有更多
     */
    void lonLoadedMoreResult(List<Album> result,boolean isOkey);

    /**
     * 联想关键词的结果回调方法
     * @param keyWordList
     */
    void onRecommendWordLoaded(List<QueryResult> keyWordList);
}
