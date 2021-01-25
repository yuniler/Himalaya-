package cn.jkdev.hiximalaya.api;

import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.HashMap;
import java.util.Map;

import cn.jkdev.hiximalaya.utils.Constants;

public class XimalayApi {
    //model层 拿数据
    //需要做成单例，因为很多地方需要从这里get数据

    private XimalayApi() {
    }

    public static XimalayApi sXimalayApi;

    public static XimalayApi getXimalayApi() {
        if (sXimalayApi == null) {
            synchronized (XimalayApi.class){
                if (sXimalayApi == null) {
                    sXimalayApi = new XimalayApi();
                }
            }
        }
        return sXimalayApi;
    }


    /**
     * 重构代码，推荐的
     * 获取推荐内容
     *
     * @param callback 请求结果的回调接口
     * @param
     */
    public void getRecommendList(IDataCallBack<GussLikeAlbumList> callback) {
        //封装参数,参数表示一页返回多少条数据
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.LIKE_COUNT, Constants.COUNT_RECOMMEND + "");
        CommonRequest.getGuessLikeAlbum(map, callback);
    }

    //新加
    /**
     * 根据专辑id获取专辑内容详情
     *
     * @param callBack 获取专辑详情的回调
     * @param albumId  专辑的ID
     * @param pageIndex     页码，第几页
     */
    public void getAlbumDetail(IDataCallBack<TrackList> callBack, long albumId, int pageIndex) {
        //创建集合
        Map<String, String> map = new HashMap<>();
        //封装参数
        map.put(DTransferConstants.ALBUM_ID, albumId + "");
        map.put(DTransferConstants.SORT, "asc");
        map.put(DTransferConstants.PAGE, pageIndex + "");
        map.put(DTransferConstants.PAGE_SIZE, Constants.COUNT_DEFAULT + "");
        //请求数据
        CommonRequest.getTracks(map, callBack);
    }

    /**
     * 根据关键词子进行搜索
     *
     * @param keyword
     */
    public void searchByKeyword(String keyword, int page, IDataCallBack<SearchAlbumList> callBack) {
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.SEARCH_KEY, keyword);
        map.put(DTransferConstants.PAGE, page + "");
        map.put(DTransferConstants.PAGE_SIZE,Constants.COUNT_DEFAULT + "");
        CommonRequest.getSearchedAlbums(map, callBack);
    }

    /**
     * 获取推荐的热词
     * @param callback
     */
    public void getHotWords(IDataCallBack<HotWordList> callback){
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.TOP, String.valueOf(Constants.COUNT_HOT_WORD));
        CommonRequest.getHotWords(map, callback);
    }

    /**
     * 根据关键字获取联想词
     * @param keyword 关键字
     * @param callback 回调
     */

    public void getSuggestWord(String keyword,IDataCallBack<SuggestWords> callback){
        Map<String, String> map = new HashMap<>();
        map.put(DTransferConstants.SEARCH_KEY, keyword);
        CommonRequest.getSuggestWord(map, callback);
    }

}
