package cn.jkdev.hiximalaya.interfaces;

import cn.jkdev.hiximalaya.base.IBasePresenter;

/**
 *   推荐的   逻辑层
 *   主界面主动发起的动作
 */
public interface IRecommendPresenter  extends IBasePresenter<IRecommendViewCallback>{


    /**
     获取推荐内容
     */
    void getRecommendList();

    /**
     * 下拉刷新更多内容
     */
    void pullResearchMore();

    /**
     * 上接加载内容
     */
    void loadMore();

//    /**
//     * 这个方法用于注册UI的回调
//     * 也就是返回数据
//     * @param callBack
//     */
//    void registerViewCallback(IRecommendViewCallBack callBack);
//
//    /**
//     * 这个方法用于取消UI的注册回调
//     * @param callBack
//     */
//    void noRegisterViewCallback(IRecommendViewCallBack callBack);


}

