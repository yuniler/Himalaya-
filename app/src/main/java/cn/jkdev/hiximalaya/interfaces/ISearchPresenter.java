package cn.jkdev.hiximalaya.interfaces;

import cn.jkdev.hiximalaya.base.IBasePresenter;

public interface ISearchPresenter extends IBasePresenter<ISearchCallback> {//泛型为回调接口
    //声明接口的方法--->定义整个搜索页面的接口

    /**
     * 进行搜索
     * @param keyword
     */
    void doSearch(String keyword);

    /**
     * 重新搜索
     */
    void reSearch();

    /**
     * 加载更多
     */
    void loadMore();

    /**
     * 获取热词
     */
    void getHotWord();

    /**
     * 获取推荐的关键字（相关的关键字）
     * @param keyword
     */
    void getRecommendWord(String keyword);

}
