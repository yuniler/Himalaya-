package cn.jkdev.hiximalaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.jkdev.hiximalaya.R;

public class DetailListAdapter extends RecyclerView.Adapter<DetailListAdapter.InnerHolder> {
    //内部最后持有一个集合
    private List<Track> mDetailData = new ArrayList<>();

    //格式化时间
    private SimpleDateFormat mUpdataDataFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat mDurationFormat = new SimpleDateFormat("mm:ss");
    private static final String TAG = "DetailListAdapter";
    private ItemClickListener mItemClickListener = null;

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_detail, parent, false);
        return new InnerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder holder, final int position) {
        //找到控件设置数据
        final View itemView = holder.itemView;
        //顺序ID
        TextView ordetTv = itemView.findViewById(R.id.order_text);
        //标题
        TextView titleTv = itemView.findViewById(R.id.detail_item_title);
        //播放次数
        TextView playCountTv = itemView.findViewById(R.id.detail_item_play_count);
        //时长
        TextView durationTv = itemView.findViewById(R.id.detail_item_duration);
        //更新日期
        TextView updataDataTv = itemView.findViewById(R.id.detail_item_updata_time);

        //设置数据
        Track track = mDetailData.get(position);//扔进集合
        ordetTv.setText((position + 1) + "");
        titleTv.setText(track.getTrackTitle());
        playCountTv.setText(track.getPlayCount() + "");
        int durationMiltrack = track.getDuration() * 1000;
        String Duration = mDurationFormat.format(durationMiltrack);
        durationTv.setText(Duration);
        String updataTimeText = mUpdataDataFormat.format(track.getUpdatedAt());
        updataDataTv.setText(updataTimeText);

        //设置item的点击事件
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(v.getContext(),"you click " + position + " item",Toast.LENGTH_LONG).show();
                //点击时改
                if (mItemClickListener != null) {//点击时调用
                    //拿列表
                    //参数需要有列表和参数

                    mItemClickListener.onItemClick(mDetailData,position);//通过方法调接口
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDetailData.size();
    }

    public void setData(List<Track> tracks) {
        //清除原来数据
        mDetailData.clear();
        //添加新的数据
        mDetailData.addAll(tracks);
        //更新UI
        notifyDataSetChanged();

    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.mItemClickListener = listener;

    }

    //将点击事件暴露出去，暴露接口
    public interface ItemClickListener {
        void onItemClick(List<Track> detailData, int position);
    }
}
