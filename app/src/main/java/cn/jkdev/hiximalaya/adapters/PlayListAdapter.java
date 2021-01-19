package cn.jkdev.hiximalaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

import cn.jkdev.hiximalaya.R;
import cn.jkdev.hiximalaya.base.BaseApplication;
import cn.jkdev.hiximalaya.views.SobPopWindow;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.InnerHolder> {

    private List<Track> mData = new ArrayList<>();
    private int playingIndex = 0;
    private SobPopWindow.PlayListItemClickListener mItemClickListener = null;

    @Override
    public PlayListAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //显示的内容首先-->View--载入布局
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_play_list, parent, false);
        return new InnerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListAdapter.InnerHolder holder, final int position) {
        //在holder被点击时使用-->设置监听
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(position);
                }
            }
        });

        //设置数据
         Track track = mData.get(position);


         TextView trackTitleTv = holder.itemView.findViewById(R.id.track_title_tv);

         trackTitleTv.setTextColor(
                 BaseApplication.getAppContext().getResources().getColor(playingIndex == position ?
                 R.color.main_color : R.color.play_list_text_color));

         trackTitleTv.setText(track.getTrackTitle());

         //找到播放状态的图标
        View playingIconView = holder.itemView.findViewById(R.id.play_icon_iv);
        playingIconView.setVisibility(playingIndex == position ? View.VISIBLE : View.GONE);//图标的显示与消失




    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<Track> data){
        //设置数据，更新列表
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();

    }

    public void setCurrentPlayPosition(int position) {
        playingIndex = position;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(SobPopWindow.PlayListItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
