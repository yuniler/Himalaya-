package cn.jkdev.hiximalaya.utils;

import java.util.HashMap;
import java.util.Map;

import cn.jkdev.hiximalaya.base.BaseFragment;
import cn.jkdev.hiximalaya.fragments.HistoryFragment;
import cn.jkdev.hiximalaya.fragments.RecommentFragment;
import cn.jkdev.hiximalaya.fragments.SubscriptionFragment;

/**
 * 1创建Fragment
 * 2创建缓存，避免重复
 */
public class FragmentCreator {


    //定义常量
    private final static int INDET_RECOMMENT = 0;
    private final static int INDET_SUBSCRIPTION = 1;
    private final static int INDET_HISTORY = 2;

    public static final int PAGE_COUNT = 3;

    public static Map<Integer, BaseFragment> sCache = new HashMap<>();//这里是一个缓存，每次操作(前后)保证最大利用，每次直接拿，不用重复创建

    public static BaseFragment getFragment(int index){
        BaseFragment baseFragment = sCache.get(index);
        if (baseFragment != null){
            return baseFragment;
        }
        switch (index){//将index作为key
            case INDET_RECOMMENT :
                baseFragment = new RecommentFragment();
                break;
            case INDET_SUBSCRIPTION :
                baseFragment = new SubscriptionFragment();
                break;
            case INDET_HISTORY :
                baseFragment = new HistoryFragment();
                break;
        }

        sCache.put(index,baseFragment);//添加
        return baseFragment;

    }



}
