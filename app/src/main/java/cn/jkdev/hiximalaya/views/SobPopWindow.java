package cn.jkdev.hiximalaya.views;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

import cn.jkdev.hiximalaya.R;
import cn.jkdev.hiximalaya.adapters.PlayListAdapter;
import cn.jkdev.hiximalaya.base.BaseApplication;

public class SobPopWindow extends PopupWindow {

    private final View mPopView;
    private View mCloseBtn;
    private RecyclerView mTracksList;
    private PlayListAdapter mPlayListAdapter;
    private ImageView mPlayModeIv;
    private TextView mPlayModeTv;
    private View mPlayModeContainer;
    private PlayListActionListener mPlayModeClickListener = null;
    private View mOrderContainer;
    private ImageView mOrderIcon;
    private TextView mOrderText;

    public SobPopWindow(){
        //设置宽高
        super(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        //这里要注意设置setOutsideTouchable之前，要先设置：setBackgroundDrawable
        //否则点击外部，无法关闭Pop
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setOutsideTouchable(true);
        //载进来View

        mPopView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.pop_play_list,null);
        //设置内容
        setContentView(mPopView);
        //设置窗口进入和退出的动画
        setAnimationStyle(R.style.pop_animation);
        initView();
        initEvent();

    }

    private void initView() {
        mCloseBtn = mPopView.findViewById(R.id.play_list_close_btn);
        //找到控件
        mTracksList = mPopView.findViewById(R.id.play_list_rv);
        //设置管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(BaseApplication.getAppContext());
        mTracksList.setLayoutManager(layoutManager);
        //设置适配器
        mPlayListAdapter = new PlayListAdapter();
        mTracksList.setAdapter(mPlayListAdapter);
        //播放模式相关
        mPlayModeTv = mPopView.findViewById(R.id.play_list_play_mode_tv);
        mPlayModeIv = mPopView.findViewById(R.id.play_list_play_mode_iv);
        mPlayModeContainer = mPopView.findViewById(R.id.play_list_play_mode_container);
        mOrderContainer = mPopView.findViewById(R.id.play_list_order_container);
        mOrderIcon = mPopView.findViewById(R.id.play_list_order_iv);
        mOrderText = mPopView.findViewById(R.id.play_list_play_order_tv);
    }
    private void initEvent() {
        //点击关闭，窗口消失
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SobPopWindow.this.dismiss();
//                dismiss();
            }
        });

        mPlayModeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换播放模式--->接口交给playActivity去做
                if (mPlayModeClickListener != null) {
                    mPlayModeClickListener.onPlayModeClick();
                }
            }
        });
        mOrderContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //播放逆序或顺序播放
                mPlayModeClickListener.onOrderClick();
            }
        });
    }

    /**
     * 给适配器设置数据
     * @param data
     */
    public void setListData(List<Track> data){
        mPlayListAdapter.setData(data);
    }

    public void setCurrentPlayPosition(int position){
        if (mPlayListAdapter != null) {
            mPlayListAdapter.setCurrentPlayPosition(position);
            mTracksList.scrollToPosition(position);
        }

    }

    //设置适配器点击
    public void setPlayListItemClickListner(PlayListItemClickListener listener){
        mPlayListAdapter.setOnItemClickListener(listener);
    }

    /**
     * 更新播放列表的播放模式
     * @param currentMode
     */
    public void updatePlayMode(XmPlayListControl.PlayMode currentMode) {
        updataPlayModeBtnImg(currentMode);
    }

    /**
     * 暴露方法更新UI
     * 更新切换列表的顺序和逆序的列表和文字
     */
    public void updateOrderIcon(boolean isOrder){
        mOrderIcon.setImageResource(isOrder?R.drawable.selector_player_mode_list_order:R.drawable.selector_player_mode_list_revers);
        mOrderText.setText(BaseApplication.getAppContext().getResources().getString(isOrder?R.string.order_text:R.string.revers_text));
    }
    /**
     * 根据当前的状态更新播放模式
     * /**1。默认：PLAY_MODEL_LIST
     * * 2.列表播放：PLAY_MODEL_LIST_LOOP
     * * 3.随机播放：PLAY_MODEL_RANDOM
     * * 4.单曲循环：PLAY_MODEL_SINGLE
     */
    private void updataPlayModeBtnImg(XmPlayListControl.PlayMode playMode) {
        int resId = R.drawable.selector_player_mode_list_order;
        int textId = R.string.play_mode_order_text;
        switch (playMode) {
            case PLAY_MODEL_LIST:
                resId = R.drawable.selector_player_mode_list_order;
                textId = R.string.play_mode_order_text;
                break;
            case PLAY_MODEL_RANDOM:
                resId = R.drawable.selector_player_mode_random;
                textId = R.string.play_mode_random_text;
                break;
            case PLAY_MODEL_LIST_LOOP:
                resId = R.drawable.selector_player_mode_list_order_looper;
                textId = R.string.play_mode_list_play_text;
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                resId = R.drawable.selector_player_mode_singer_looper;
                textId = R.string.play_mode_single_play_text;
                break;
        }
        mPlayModeIv.setImageResource(resId);//图片更新
        mPlayModeTv.setText(textId);


    }


    //向外暴露接口
    public interface PlayListItemClickListener{
        void onItemClick(int position);
    }


    public void setPlayListActionListener(PlayListActionListener playModeListener){
        mPlayModeClickListener = playModeListener;
    }

    public interface PlayListActionListener {
        //播放模式被点击
        void onPlayModeClick();
        //播放逆序或顺序被点击
        void onOrderClick();
        
    }

}
