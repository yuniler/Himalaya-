package cn.jkdev.hiximalaya;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.header.bezierlayout.BezierLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

import cn.jkdev.hiximalaya.adapters.DetailListAdapter;
import cn.jkdev.hiximalaya.base.BaseActivity;
import cn.jkdev.hiximalaya.base.BaseApplication;
import cn.jkdev.hiximalaya.buildins.UTUtil;
import cn.jkdev.hiximalaya.interfaces.IAlbumDetailViewCallback;
import cn.jkdev.hiximalaya.interfaces.IPlayerCallback;
import cn.jkdev.hiximalaya.presenters.AlbumDetailPresenter;
import cn.jkdev.hiximalaya.presenters.PlayerPresenter;
import cn.jkdev.hiximalaya.utils.LogUtil;
import cn.jkdev.hiximalaya.views.ImageBlur;
import cn.jkdev.hiximalaya.views.RoundRectImageView;
import cn.jkdev.hiximalaya.views.UILoader;

public class DetailActivity extends BaseActivity implements IAlbumDetailViewCallback, UILoader.OnRetryClickListener, DetailListAdapter.ItemClickListener, IPlayerCallback {

    private ImageView mLargeCover;
    private RoundRectImageView mSmallCover;
    private TextView mAlbumTitle;
    private TextView mAlbumAuthor;
    private AlbumDetailPresenter mAlbumDetailPresenter;
    private static final String TAG = "DetailActivity";
    private int mCurrentPage = 1;
    private RecyclerView mDetailList;
    private DetailListAdapter mDetailListAdapter;
    private FrameLayout mDetailListContainer;
    private UILoader mUiLoader;
    private long mCurrentId = -1;
    private ImageView mPlayControlBtn;
    private TextView mPlayControlTips;
    private PlayerPresenter mPlayerPresenter;
    private List<Track> mCurrentTracks = null;
    private final static int DEFAULT_PLAY_INDEX = 0;
    private TwinklingRefreshLayout mRefreshLayout;
    private String mCurrentTrackTitle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        initView();

        //这个是专辑详情的presenter
        mAlbumDetailPresenter = AlbumDetailPresenter.getInstance();
        mAlbumDetailPresenter.registerViewCallback(this);

        //播放器的presenter
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();//拿到presenter的实例，注册回来，实现方法
        mPlayerPresenter.registerViewCallback(this);

        updatePlayState(mPlayerPresenter.isPlaying());
        initListener();

    }


    private void initListener() {
        if (mPlayControlBtn != null) {
            mPlayControlBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //判断播放器是否有播放列表
                    if (mPlayerPresenter != null) {
                        //控制播放器的状态
                        boolean has = mPlayerPresenter.hasPlayList();
                        if (has) {
                            handlePlayControl();
                        } else {
                            handleNoPlayList();

                        }


                    }

                }
            });

        }
    }

    /**
     * 当播放器里面没有内容，我们进行处理
     */
    private void handleNoPlayList() {
        //播放并且设置
        mPlayerPresenter.setPlayList(mCurrentTracks, DEFAULT_PLAY_INDEX);//默认地方
    }

    private void handlePlayControl() {
        if (mPlayerPresenter.isPlaying()) {
            //正在播放，那么暂停
            mPlayerPresenter.pause();
        } else {
            //
            mPlayerPresenter.play();
        }
    }

    private void initView() {
        mDetailListContainer = this.findViewById(R.id.detail_list_container);
        if (mUiLoader == null) {//为空时才创建，避免重复创建
            mUiLoader = new UILoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView(container);
                }
            };
            //添加前，先clear
            mDetailListContainer.removeAllViews();
            mDetailListContainer.addView(mUiLoader);//loader里
            mUiLoader.setOnRetryClickListener(this);//重新点击
        }

        mLargeCover = this.findViewById(R.id.iv_large_cover);
        mSmallCover = this.findViewById(R.id.viv_small_cover);
        mAlbumTitle = this.findViewById(R.id.tv_album_title);
        mAlbumAuthor = this.findViewById(R.id.tv_album_author);

        //播放控制的图标
        mPlayControlBtn = this.findViewById(R.id.detail_play_control);
        mPlayControlTips = this.findViewById(R.id.play_control_tv);
        mPlayControlTips.setSelected(true);//继承自Textview 去复写


    }

    private boolean mIsLoaderMore = false;

    private View createSuccessView(ViewGroup container) {
        View detailListView = LayoutInflater.from(this).inflate(R.layout.item_detail_list, container, false);//detail的多种情形显示
        mDetailList = detailListView.findViewById(R.id.album_detail_list);
        mRefreshLayout = detailListView.findViewById(R.id.refresh_layout);

        //RecyclerView 使用步骤
        //第一步，设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mDetailList.setLayoutManager(layoutManager);
        //第二步，设置适配器
        mDetailListAdapter = new DetailListAdapter();
        mDetailList.setAdapter(mDetailListAdapter);
        //设置item上下间距
        mDetailList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UTUtil.dip2px(view.getContext(), 2);
                outRect.bottom = UTUtil.dip2px(view.getContext(), 2);
                outRect.left = UTUtil.dip2px(view.getContext(), 2);
                outRect.right = UTUtil.dip2px(view.getContext(), 2);
            }
        });

        mDetailListAdapter.setItemClickListener(this);
        //设置下拉 新样式
        BezierLayout handerView = new BezierLayout(this);
        mRefreshLayout.setHeaderView(handerView);
        mRefreshLayout.setMaxBottomHeight(140);
        //找到控件，设置点击事件
        mRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                super.onRefresh(refreshLayout);

                //进行复原，设置延迟
                BaseApplication.getsHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DetailActivity.this, "刷新成功...", Toast.LENGTH_SHORT).show();
                        mRefreshLayout.finishRefreshing();//加载完成
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                super.onLoadMore(refreshLayout);

                //加载更多内容。
                if (mAlbumDetailPresenter != null) {
                    mAlbumDetailPresenter.loadMore();
                    //上拉
                    mIsLoaderMore = true;
                }
                //上下是一样的

            }
        });
        return detailListView;
    }

    @Override
    public void onDetailListLoaded(List<Track> tracks) {
        if (mIsLoaderMore && mRefreshLayout != null) {
            mRefreshLayout.finishLoadmore();
            mIsLoaderMore = false;//可能同时下拉，上拉
        }


        this.mCurrentTracks = tracks;
        //判断数据结果,根据结果控制UI显示
        if (tracks == null || tracks.size() == 0) {
            if (mUiLoader == null) {
                mUiLoader.updataStatus(UILoader.UIStatus.EMPTY);
            }
        }
        if (mUiLoader != null) {
            mUiLoader.updataStatus(UILoader.UIStatus.SUCCESS);
        }

        //更新：设置UI数据
        mDetailListAdapter.setData(tracks);


    }

    @Override
    public void onNetworkError(int errorCode, String errorMsg) {
        //请求发生错误，显示网络异常状态
        mUiLoader.updataStatus(UILoader.UIStatus.NEWWORK_ERROR);
    }


    @Override
    public void onAlbumLoaded(Album album) {

        long id = album.getId();

        Log.d(TAG, "album" + id);

        mCurrentId = id;

        if (mAlbumDetailPresenter != null) {
            //获取专辑的详情内容
            mAlbumDetailPresenter.getAlubmDetail((int) id, mCurrentPage);//拿到列表数据以后。
        }
        //拿数据，显示loading状态
        if (mUiLoader != null) {
            mUiLoader.updataStatus(UILoader.UIStatus.LOADING);

        }

        if (mAlbumTitle != null) {
            mAlbumTitle.setText(album.getAlbumTitle());
        }
        if (mAlbumAuthor != null) {
            mAlbumAuthor.setText(album.getAnnouncer().getNickname());

        }
        //毛玻璃效果
        if (mLargeCover != null && null != mLargeCover) {
            Picasso.with(this).load(album.getCoverUrlLarge()).into(mLargeCover, new Callback() {
                @Override
                public void onSuccess() {
                    Drawable drawable = mLargeCover.getDrawable();
                    if (drawable != null) {
                        ImageBlur.makeBlur(mLargeCover, DetailActivity.this);//引入view框架
                    }
                }

                @Override
                public void onError() {
                    LogUtil.d(TAG, "onError ");
                }
            });
//        Picasso.with(this).load(album.getCoverUrlLarge()).into(mLargeCover);//放在内部为空，换方法

        }
        if (mSmallCover != null) {
            Picasso.with(this).load(album.getCoverUrlSmall()).into(mSmallCover);
        }
    }

    @Override
    public void onLoaderMoreFinished(int size) {
        if (size > 0) {
            Toast.makeText(this,"成功加载" + size + "条节目",Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(this,"没有更多节目",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefreshFinished(int size) {

    }

    @Override
    public void onRetryClick() {
        //这里表示用户网络不佳，点击了重新加载
        if (mAlbumDetailPresenter != null) {
            //获取专辑的详情内容
            mAlbumDetailPresenter.getAlubmDetail((int) mCurrentId, mCurrentPage);
        }
    }

    @Override
    public void onItemClick(List<Track> detailData, int position) {
        //跳转前，设置播放器的数据
        PlayerPresenter playerPresenter = PlayerPresenter.getPlayerPresenter();
        playerPresenter.setPlayList(detailData, position);
        //TODO:跳转到播放页面
        Intent intent = new Intent(this, PlayerActivity.class);
        startActivity(intent);

    }

    /**
     * 根据播放状态，修改图标和文字
     *
     * @param playing
     */
    private void updatePlayState(boolean playing) {
        if (mPlayControlBtn != null && mPlayControlTips != null) {//当前状态是正在播放，但图标为暂停图标，点了就暂停。这个意思

            mPlayControlBtn.setImageResource(playing ? R.drawable.selector_play_control_pause : R.drawable.selector_play_control_play);
            if (!playing) {
                mPlayControlTips.setText(R.string.click_play_tips_text);
            } else {
                if (!TextUtils.isEmpty(mCurrentTrackTitle)) {
                    mPlayControlTips.setText(mCurrentTrackTitle);
                }
            }

        }
    }

    @Override
    public void onPlayStart() {
        //修改图标为暂停状态，文字改为正在播放
//        if (mPlayControlBtn != null && mPlayControlTips != null) {//当前状态是正在播放，但图标为暂停图标，点了就暂停。这个意思
//            mPlayControlBtn.setImageResource(R.drawable.selector_play_control_pause);
//            mPlayControlTips.setText(R.string.playing_tips_text);
//        }
        updatePlayState(true);


    }

    @Override
    public void onPlayPause() {
        //设置成播放状态，文字为已暂停
        updatePlayState(false);


    }

    @Override
    public void onPlayStop() {
        //设置成播放状态，文字为已暂停
        updatePlayState(false);
    }

    @Override
    public void onPlayError() {

    }

    @Override
    public void nextPlay(Track track) {

    }

    @Override
    public void prePlay(Track track) {

    }

    @Override
    public void onListLoader(List<Track> list) {

    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode playMode) {

    }

    @Override
    public void onProgressChange(int currentProgress, int totle) {

    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void onTrackUpdata(Track track, int playIndex) {
        //此方法表示一直回调，导致第一次，出来后，再次暂停，没了

        if (track != null) {
            mCurrentTrackTitle = track.getTrackTitle();
            if (!TextUtils.isEmpty(mCurrentTrackTitle) && mPlayControlTips != null) {
                mPlayControlTips.setText(mCurrentTrackTitle);

            }

        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {

    }
}
