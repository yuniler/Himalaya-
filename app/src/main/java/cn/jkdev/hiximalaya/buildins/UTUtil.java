package cn.jkdev.hiximalaya.buildins;

import android.content.Context;

/**
 * dp-->px
 * px-->dp
 */
public final class UTUtil {
    public UTUtil(){
    }
    public static int dip2px(Context context,double dpValue){
        float density = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * (double)density + 0.50);
    }
    public static int getScreenWidth(Context context){
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}
