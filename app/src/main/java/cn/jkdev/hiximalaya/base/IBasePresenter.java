package cn.jkdev.hiximalaya.base;

public interface IBasePresenter<T>{

    /**
     * 注册UI的回调接口
     * @param t
     */
    void registerViewCallback(T t);

    /**
     * 取消UI的回调接口
     * @param t
     */
    void unRegisterViewCallback(T t);
}
