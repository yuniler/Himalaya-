package cn.jkdev.hiximalaya;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;

import java.util.List;

import cn.jkdev.hiximalaya.adapters.IndicatorAdapter;
import cn.jkdev.hiximalaya.adapters.MainContentAdapter;
import cn.jkdev.hiximalaya.interfaces.IPlayerCallback;
import cn.jkdev.hiximalaya.presenters.PlayerPresenter;
import cn.jkdev.hiximalaya.presenters.RecommendPresenter;
import cn.jkdev.hiximalaya.utils.LogUtil;

public class MainActivity extends FragmentActivity implements IPlayerCallback {//AppCompatActivity extends F..

    private static final String TAG = "MainActivity";//全局可以TAG
    private MagicIndicator mMagicIndicator;
    private ViewPager mContentPager;
    private IndicatorAdapter mIndicatorAdapter;
    private ImageView mRoundRectImageView;
    private TextView mHeadTitle;
    private TextView mSubTitle;
    private ImageView mPlayControl;
    private PlayerPresenter mPlayerPresenter;
    private View mPlayControlItem;
    private View mSearchBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initEvent();
        //
        initPresenter();

    }

    private void initPresenter() {
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        mPlayerPresenter.registerViewCallback(this);//注册回调，通知UI更新
    }

    private void initEvent() {
        mIndicatorAdapter.setOnIndicatorTapClickListener(new IndicatorAdapter.OnIndicatorTapClickListener() {
            @Override
            public void onTabClick(int index) {
                LogUtil.d(TAG, "index is ---> " + index);
                if (mContentPager != null) {
                    mContentPager.setCurrentItem(index);
                }
            }
        });

        mPlayControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置联动
                if (mPlayerPresenter != null) {
                    boolean hasPlayList = mPlayerPresenter.hasPlayList();
                    if (!hasPlayList) {
                        //没有设置播放列表，我们就默认的第一个推荐专辑
                        //第一个推荐专辑每天都会变
                        playFirstRecommend();
                    } else {
                        if (mPlayerPresenter.isPlaying()) {
                            mPlayerPresenter.pause();//意思是，判断正在播放时，点击了--暂停
                        } else {
                            mPlayerPresenter.play();
                        }
                    }

                }
            }
        });

        mPlayControlItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //要跳转到播放器界面，也要判断是否有播放列表
                boolean hasPlayList = mPlayerPresenter.hasPlayList();
                if (!hasPlayList) {
                    playFirstRecommend();
                }
                //跳转到播放页面 Intent
                startActivity(new Intent(MainActivity.this, PlayerActivity.class));
            }
        });
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 播放第一个推荐的内容
     */
    private void playFirstRecommend() {
        List<Album> currentRecommend = RecommendPresenter.getInstance().getCurrentRecommend();
        if (currentRecommend != null && currentRecommend.size() > 0) {
            Album album = currentRecommend.get(0);
            long albumId = album.getId();
            mPlayerPresenter.playByAlbumId(albumId);//UI层
        }
    }

    private void initView() {
        mMagicIndicator = this.findViewById(R.id.main_indicator);
        mMagicIndicator.setBackgroundColor(this.getResources().getColor(R.color.main_color));

        //创建indicator适配器
        mIndicatorAdapter = new IndicatorAdapter(this);
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdjustMode(true);//均衡放置indicator
        commonNavigator.setAdapter(mIndicatorAdapter);

        //ViewPager，--》内容显示还需要adapter
        mContentPager = findViewById(R.id.content_pager);
//        mContentPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {//设置监听，滑动。。
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });
        //创建内容适配器
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        MainContentAdapter mainContentAdapter = new MainContentAdapter(supportFragmentManager);

        mContentPager.setAdapter(mainContentAdapter);

        //把ViewPager和indicator绑定一起
        mMagicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mMagicIndicator, mContentPager);//绑定过程

        //播放控制相关
        mRoundRectImageView = this.findViewById(R.id.main_track_cover);
        mHeadTitle = this.findViewById(R.id.main_head_title);
        mHeadTitle.setSelected(true);//转动
        mSubTitle = this.findViewById(R.id.main_sub_title);
        mPlayControl = this.findViewById(R.id.main_play_control);
        mPlayControlItem = this.findViewById(R.id.main_play_control_item);
        //搜索
        mSearchBtn = this.findViewById(R.id.search_btn);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unRegisterViewCallback(this);//取消注册
        }
    }

    @Override
    public void onPlayStart() {
        updataPlayControl(true);
    }

    private void updataPlayControl(boolean isPlaying) {
        if (mPlayControl != null) {
            mPlayControl.setImageResource(isPlaying ? R.drawable.selector_player_pause : R.drawable.selector_player_play);
        }
    }

    @Override
    public void onPlayPause() {
        updataPlayControl(false);
    }

    @Override
    public void onPlayStop() {
        updataPlayControl(false);
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
        if (track != null) {
            //所有使用到的
            String trackTitle = track.getTrackTitle();
            String nickname = track.getAnnouncer().getNickname();
            String coverUrlLarge = track.getCoverUrlLarge();
            LogUtil.d(TAG, "trackTitle" + trackTitle);
            if (trackTitle != null) {
                mHeadTitle.setText(trackTitle);
            }

            LogUtil.d(TAG, "nickname" + nickname);
            if (nickname != null) {
                mSubTitle.setText(nickname);
            }

            LogUtil.d(TAG, "coverUrlLarge" + coverUrlLarge);
            Picasso.with(this).load(coverUrlLarge).into(mRoundRectImageView);
        }

    }

    @Override
    public void updateListOrder(boolean isReverse) {

    }
}
