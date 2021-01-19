package cn.jkdev.hiximalaya.interfaces;

import cn.jkdev.hiximalaya.base.IBasePresenter;

public interface IAlbumDetailPresenter extends IBasePresenter<IAlbumDetailViewCallback> {

    /**
     * 下拉刷新更多内容
     */
    void pullRefreshMore();

    /**
     * 上接加载更多
     */
    void loadMore();

    /**
     * 获取专辑详情
     */
    void getAlubmDetail(int albumId,int page);

//    /**
//     * 注册UI通知的接口
//     * @param detailViewCallBack
//     */
//    void registerViewCallback(IAlbumDetailViewCallBack detailViewCallBack);
//
//    /**
//     * 删除UI通知的接口
//     * @param detailViewCallBack
//     */
//    void unregisterViewCallback(IAlbumDetailViewCallBack detailViewCallBack);
}
