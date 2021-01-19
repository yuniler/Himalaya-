package cn.jkdev.hiximalaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

import cn.jkdev.hiximalaya.R;

public class PlayerTrackPagerAdapter extends PagerAdapter {
    //现有集合
    private List<Track> mData = new ArrayList<>();

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        //绑定
        View itemView = LayoutInflater.from(container.getContext()).inflate(R.layout.item_track_pager,container,false);
        container.addView(itemView);//
        //设置数据
        //找到控件
        ImageView item = itemView.findViewById(R.id.track_pager_item);
        //设置内容-->图片
        Track track = mData.get(position);
        String coverUrlLarge = track.getCoverUrlLarge();//根据数据获取图片
        Picasso.with(container.getContext()).load(coverUrlLarge).into(item);//设置图
        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);//add后记得remove,过多易爆
    }


    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public void setData(List<Track> list) {
        mData.clear();
        mData.addAll(list);
        notifyDataSetChanged();//设置数据改变时

    }
}
