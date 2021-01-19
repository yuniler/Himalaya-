package cn.jkdev.hiximalaya.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.ArrayList;
import java.util.List;

import cn.jkdev.hiximalaya.R;

public class RecommendListAdapter extends RecyclerView.Adapter<RecommendListAdapter.InnerHolder> {
    private List<Album> mData = new ArrayList<>();
    private static final String TAG = "RecommendListAdapter";
    private OnRecommendItemClickListener mItemClickListner = null;

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //这里为载人view
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommend,parent,false);
        return new InnerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder holder, final int position) {
        //这里设置数据，每一个item
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mItemClickListner != null) {
                    int clickPosition = (int) v.getTag();
                    mItemClickListner.onItemClick(clickPosition, mData.get(clickPosition));
                }
                Log.d(TAG,"holder.itemView click " + v.getTag());
            }
        });
        holder.setData(mData.get(position));

    }

    @Override
    public int getItemCount() {
        //返回显示个数
        if (mData != null){
            return mData.size();
        }
        return 0;
    }

    public void setData(List<Album> albumList) {
        if (mData != null){
            mData.clear();
            mData.addAll(albumList);
        }
        //更新UI
        notifyDataSetChanged();
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setData(Album album) {
            //找到各个控件，设置数据
            //专辑封面
            ImageView albumCoverTv = itemView.findViewById(R.id.album_cover);
            //title
            TextView albumTitleTv = itemView.findViewById(R.id.album_title_tv);
            //description
            TextView albumDesTv = itemView.findViewById(R.id.album_description_tv);
            //play_count
            TextView albumPlayCountTv = itemView.findViewById(R.id.album_play_count);
            //专辑内容数量
            TextView albumContentCountTv = itemView.findViewById(R.id.album_content_size);

            albumTitleTv.setText(album.getAlbumTitle());
            albumDesTv.setText(album.getAlbumIntro());
            albumPlayCountTv.setText(album.getPlayCount() + "");
            albumContentCountTv.setText(album.getIncludeTrackCount() + "");

            Picasso.with(itemView.getContext()).load(album.getCoverUrlLarge()).into(albumCoverTv);

        }
    }
    public void setOnRecommendItemClickListner(OnRecommendItemClickListener listner){//调接口
        this.mItemClickListner = listner;
    }
    public interface OnRecommendItemClickListener {//接口
        void onItemClick(int position, Album album);
    }
}
