package cn.jkdev.hiximalaya.base;
import android.content.Context;
import android.os.Handler;
import android.app.Application;

import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;


import cn.jkdev.hiximalaya.utils.LogUtil;

public class BaseApplication extends Application {
    //写一个os的handler,注意包别倒错了
    public static Handler sHandler = null;

    //写一个共用Context
    private static Context sContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        CommonRequest mXimalaya = CommonRequest.getInstanse();
        if(DTransferConstants.isRelease) {
            String mAppSecret = "afe063d2e6df361bc9f1fb8bb8210d67";
            mXimalaya.setAppkey("af1d317b871e0e7e2ce45872caa34d9a");
            mXimalaya.setPackid("com.humaxdigital.automotive.ximalaya");
            mXimalaya.init(this ,mAppSecret);
        } else {
            String mAppSecret = "0a09d7093bff3d4947a5c4da0125972e";
            mXimalaya.setAppkey("f4d8f65918d9878e1702d49a8cdf0183");
            mXimalaya.setPackid("com.ximalaya.qunfeng");
            mXimalaya.init(this ,mAppSecret);
        }

        //初始化播放器..detail里的每一个item
        XmPlayerManager.getInstance(this).init();//一次就完事

        //初始化LogUtil
        LogUtil.init(this.getPackageName(), false);//对log可控，true,false
        sHandler = new Handler();
        sContext = getBaseContext();
    }

    public static Context getAppContext(){
        return sContext;
    }

    public static Handler getsHandler(){
        return sHandler;
    }

}
