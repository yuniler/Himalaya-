package cn.jkdev.hiximalaya.presenters;

import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.ArrayList;
import java.util.List;

import cn.jkdev.hiximalaya.SearchActivity;
import cn.jkdev.hiximalaya.api.XimalayApi;
import cn.jkdev.hiximalaya.interfaces.ISearchCallback;
import cn.jkdev.hiximalaya.interfaces.ISearchPresenter;
import cn.jkdev.hiximalaya.utils.LogUtil;

public class SearchPresenter implements ISearchPresenter {

    private static final String TAG = "SearchPresenter";
    List<ISearchCallback> mCallbacks = new ArrayList<>();
    //当前的搜索关键字
    private String mCurrentKeyword = null;
    private XimalayApi mXimalayApi;
    private static final int DEFAULT_PAGE = 1;
    private int mCurrentPage = DEFAULT_PAGE;

    private SearchPresenter(){
        mXimalayApi = XimalayApi.getXimalayApi();
    }

    private static SearchPresenter sSearchPresenter = null;

    public static SearchPresenter getSearchPresenter(){
        if (sSearchPresenter == null) {
            synchronized (SearchPresenter.class){
                if (sSearchPresenter == null) {
                    sSearchPresenter = new SearchPresenter();
                }
            }

        }

        return sSearchPresenter;
    }

    @Override
    public void doSearch(String keyword) {

        //当网络不好时，用户点击重新搜索
        search(keyword);
    }

    private void search(String keyword) {
        this.mCurrentKeyword = keyword;
        mXimalayApi.searchByKeyword(keyword, mCurrentPage, new IDataCallBack<SearchAlbumList>() {//此时将数据返回给UI层
            @Override
            public void onSuccess(SearchAlbumList searchAlbumList) {
                List<Album> albums = searchAlbumList.getAlbums();
                if (albums != null) {
                    LogUtil.d(TAG,"albums size " + albums.size());
                } else {
                    LogUtil.d(TAG,"album is null");
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.d(TAG,"errorCode --->" + errorCode);
                LogUtil.d(TAG,"errorMsg --->" + errorMsg);

            }
        });
    }

    @Override
    public void reSearch() {
        //等于说复写一遍
        search(mCurrentKeyword);

    }

    @Override
    public void loadMore() {

        //TODO:
    }

    @Override
    public void getHotWord() {
        mXimalayApi.getHotWord(new IDataCallBack<HotWordList>() {
            @Override
            public void onSuccess(HotWordList hotWordList) {
                if (hotWordList != null) {
                    List<HotWord> hotWord = hotWordList.getHotWordList();
                    LogUtil.d(TAG,"hotWord size" + hotWord.size());
                    for (ISearchCallback iSearchCallback : mCallbacks) {//presenter拿回调--》再回到UI层
                        iSearchCallback.onHotWordLoaded(hotWord);

                    }
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {

                LogUtil.d(TAG,"getHotWord errorCode --->" + errorCode);
                LogUtil.d(TAG,"getHotWord errorMsg --->" + errorMsg);
            }
        });

    }

    @Override
    public void getRecommendWord(String keyword) {
        mXimalayApi.getSuggestWord(keyword, new IDataCallBack<SuggestWords>() {
            @Override
            public void onSuccess(SuggestWords suggestWords) {
                if (suggestWords != null) {
                    List<QueryResult> keyWordList = suggestWords.getKeyWordList();
                    LogUtil.d(TAG,"keyWordList size " + keyWordList.size());
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.d(TAG,"getRecommendWord errorCode --->" + errorCode);
                LogUtil.d(TAG,"getRecommendWord errorMsg --->" + errorMsg);
            }
        });
    }

    /**
     * 多个地方使用接口注入进来，那么用集合保存进来
     * @param iSearchCallback
     */
    @Override
    public void registerViewCallback(ISearchCallback iSearchCallback) {
        if (!mCallbacks.contains(iSearchCallback)) {
            mCallbacks.add(iSearchCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(ISearchCallback iSearchCallback) {
        mCallbacks.remove(iSearchCallback);
    }
}
