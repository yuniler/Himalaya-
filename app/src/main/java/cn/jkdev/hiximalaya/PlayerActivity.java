package cn.jkdev.hiximalaya;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jkdev.hiximalaya.adapters.PlayerTrackPagerAdapter;
import cn.jkdev.hiximalaya.base.BaseActivity;
import cn.jkdev.hiximalaya.interfaces.IPlayerCallback;
import cn.jkdev.hiximalaya.presenters.PlayerPresenter;
import cn.jkdev.hiximalaya.utils.LogUtil;
import cn.jkdev.hiximalaya.views.SobPopWindow;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;

public class PlayerActivity extends BaseActivity implements IPlayerCallback, ViewPager.OnPageChangeListener {
    //activity界面层


    private static final String TAG = "playerActivity";
    private ImageView mControlBtn;
    private PlayerPresenter mPlayerPresenter;
    private SimpleDateFormat mMinFormat = new SimpleDateFormat("mm:ss");
    private SimpleDateFormat mHourFormat = new SimpleDateFormat("hh:mm:ss");
    private TextView mTotalDuration;
    private TextView mCurrentPosition;
    private SeekBar mDurationBar;
    private int mCurrentProgress = 0;
    private boolean isUserTouchProgressBar = false;
    private ImageView mPlayPre;
    private ImageView mPlayNext;
    private TextView mTitleTitleTv;
    private String mTitleTitleText;
    private ViewPager mTrackPagerView;
    private PlayerTrackPagerAdapter mTrackPagerAdapter;
    private boolean mIsUserSlidePager = false;
    private ImageView mPlayModeSwitchBtn;
    //当前的
    private XmPlayListControl.PlayMode mCurrentMode = PLAY_MODEL_LIST;
    //
    private static Map<XmPlayListControl.PlayMode, XmPlayListControl.PlayMode> sPlayModeRule = new HashMap<>();


    /**1。默认：PLAY_MODEL_LIST
     * 2.列表播放：PLAY_MODEL_LIST_LOOP
     * 3.随机播放：PLAY_MODEL_RANDOM
     * 4.单曲循环：PLAY_MODEL_SINGLE
     */
    static {
        sPlayModeRule.put(PLAY_MODEL_LIST, PLAY_MODEL_LIST_LOOP);//指向
        sPlayModeRule.put(PLAY_MODEL_LIST_LOOP, PLAY_MODEL_RANDOM);
        sPlayModeRule.put(PLAY_MODEL_RANDOM, PLAY_MODEL_SINGLE_LOOP);
        sPlayModeRule.put(PLAY_MODEL_SINGLE_LOOP, PLAY_MODEL_LIST);

    }

    private View mPlayListBtn;
    private SobPopWindow mSobPopWindow;
    private ValueAnimator mEnterBgAnimation;
    private ValueAnimator mOutBgAnimation;
    private final int BG_ANIMATION_DURATION = 500;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        setContentView(R.layout.activity_player);
        initView();//初始化控件
        //初始化好View在注册callback.UI先准备好，数据直接，填给UI
        //TODO:测试播放
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        //开始播放后怎么知道他的状态，-->回调
        mPlayerPresenter.registerViewCallback(this);//UI接口注册过去--->通知UI
        //在界面初始化后才去获取数据-->viewPager填充数据
        mPlayerPresenter.getPlayList();
        initEvent();//初始化事件
        initBgAnimation();
//        startPlay();

    }

    private void initBgAnimation() {
        mEnterBgAnimation = ValueAnimator.ofFloat(1.0f,0.7f);
        mEnterBgAnimation.setDuration(BG_ANIMATION_DURATION);
        mEnterBgAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                //处理一下背景，有点透明度
                updateBgAlpha(value);//渐变
            }
        });

        mOutBgAnimation = ValueAnimator.ofFloat(0.7f,1.0f);
        mOutBgAnimation.setDuration(BG_ANIMATION_DURATION);
        mOutBgAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                updateBgAlpha(value);
            }
        });
    }

    /**
     * 取消注册
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放资源
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unRegisterViewCallback(this);
            mPlayerPresenter = null;
        }
    }


    /**
     * 给控件设置相关事件
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {
        mControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果现在的状态是正在播放，暂停
                if (mPlayerPresenter.isPlaying()) {
                    mPlayerPresenter.pause();
                } else {
                    //如果现在是非播放，我们让其播放
                    mPlayerPresenter.play();
                }
            }
        });

        //设置seekBar
        mDurationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
                if (isFromUser) {
                    mCurrentProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserTouchProgressBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserTouchProgressBar = false;
                //手离开进度条时，更新进度
                mPlayerPresenter.seekTo(mCurrentProgress);//逻辑层去seekTo设置

            }
        });
        mPlayPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //播放上一个界面
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.playPre();
                }
            }
        });
        mPlayNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //播放下一个界面
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.playNext();
                }
            }
        });
        //视图改变
        mTrackPagerView.setOnPageChangeListener(this);

        mTrackPagerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mIsUserSlidePager = true;
                        break;
                }

                return false;
            }
        });
        //处理切换模式的改变

        mPlayModeSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchPlayMode();


            }
        });
        mPlayListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //以屏幕为标准
                mSobPopWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);

                //修改背景的透明有一个渐变的过程
                mEnterBgAnimation.start();
            }
        });
        mSobPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //点击回复
//                updateBgAlpha(1.0f);
                mOutBgAnimation.start();
            }
        });

        mSobPopWindow.setPlayListItemClickListner(new SobPopWindow.PlayListItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //播放列表的item被点击了
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.playByIndex(position);
                }

            }
        });

        mSobPopWindow.setPlayListActionListener(new SobPopWindow.PlayListActionListener() {
            @Override
            public void onPlayModeClick() {
                //切换播放模式
                switchPlayMode();
            }

            @Override
            public void onOrderClick() {
                //点击切换顺序，逆序
//                Toast.makeText(playerActivity.this,"切换列表顺序",Toast.LENGTH_LONG).show();
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.reversePlayList();//调用
                }

            }
        });
    }


    private void switchPlayMode() {
        //根据当前的mode获取到下一个mode
        XmPlayListControl.PlayMode PlayMode = sPlayModeRule.get(mCurrentMode);
        //修改播放模式
        if (mPlayerPresenter != null) {
            mPlayerPresenter.switchPlayMode(PlayMode);
        }
    }

    public void updateBgAlpha(float alpha) {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.alpha = alpha;
        window.setAttributes(attributes);

    }


    /**
     * 根据当前的状态更新播放模式
     * /**1。默认：PLAY_MODEL_LIST
     * * 2.列表播放：PLAY_MODEL_LIST_LOOP
     * * 3.随机播放：PLAY_MODEL_RANDOM
     * * 4.单曲循环：PLAY_MODEL_SINGLE
     */
    private void updataPlayModeBtnImg() {
        int resId = R.drawable.selector_player_mode_list_order;
        switch (mCurrentMode) {
            case PLAY_MODEL_LIST:
                resId = R.drawable.selector_player_mode_list_order;
                break;
            case PLAY_MODEL_RANDOM:
                resId = R.drawable.selector_player_mode_random;
                break;
            case PLAY_MODEL_LIST_LOOP:
                resId = R.drawable.selector_player_mode_list_order_looper;
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                resId = R.drawable.selector_player_mode_singer_looper;
                break;
        }
        mPlayModeSwitchBtn.setImageResource(resId);



    }

    /**
     * 找到各个控件
     */
    private void initView() {
        mControlBtn = this.findViewById(R.id.play_or_pause_btn);
        mTotalDuration = this.findViewById(R.id.track_duration);
        mCurrentPosition = this.findViewById(R.id.current_position);
        mDurationBar = this.findViewById(R.id.track_seek_bar);
        mPlayPre = this.findViewById(R.id.play_pre);
        mPlayNext = this.findViewById(R.id.play_next);
        mTitleTitleTv = this.findViewById(R.id.track_title);
        if (!TextUtils.isEmpty(mTitleTitleText)) {
            mTitleTitleTv.setText(mTitleTitleText);//
        }
        mTrackPagerView = this.findViewById(R.id.track_pager_view);//viewPager显示是一定要适配器的
        //创建适配器
        mTrackPagerAdapter = new PlayerTrackPagerAdapter();
        //设置适配器
        mTrackPagerView.setAdapter(mTrackPagerAdapter);
        //切换播放模式的按钮
        mPlayModeSwitchBtn = this.findViewById(R.id.player_mode_switch_btn);
        //播放列表
        mPlayListBtn = this.findViewById(R.id.player_list);
        mSobPopWindow = new SobPopWindow();

    }

    //注意异步操作，有可能UI没有准备好，但数据已经开始播放-->判空
    @Override
    public void onPlayStart() {
        //点击后的状态返回，开始播放，修改UI层暂停按钮
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_pause);
        }
    }

    //点击后正好是相反
    @Override
    public void onPlayPause() {
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_play);
        }

    }

    @Override
    public void onPlayStop() {
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_play);
        }
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
        LogUtil.d(TAG, list + "list");
        //把数据设置到适配器中loader加载器
        if (mTrackPagerAdapter != null) {
            mTrackPagerAdapter.setData(list);//数据布局绑定
        }

        //数据回来，给节目列表一份
        if (mSobPopWindow != null) {
            mSobPopWindow.setListData(list);
        }

    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode playMode) {
        //更新播放模式，并且修改UI
        mCurrentMode = playMode;
        //更新pop里的播放模式
        mSobPopWindow.updatePlayMode(mCurrentMode);
        //UI如何修改-->等回调
        updataPlayModeBtnImg();

    }

    @Override
    public void onProgressChange(int currentDuration, int totle) {//UI这边
        mDurationBar.setMax(totle);
        //更新播放速度,更新进度条
        String totleDuration;//两种时间格式
        String currentPosition;
        if (totle > 1000 * 60 * 60) {
            totleDuration = mHourFormat.format(totle);
            currentPosition = mHourFormat.format(currentDuration);
        } else {
            totleDuration = mMinFormat.format(totle);
            currentPosition = mMinFormat.format(currentDuration);
        }
        if (mTotalDuration != null) {
            mTotalDuration.setText(totleDuration);
        }

        //更新当前时间
        if (mCurrentPosition != null) {
            mCurrentPosition.setText(currentPosition);
        }
        //更新进度
        //计算当前进度
        if (!isUserTouchProgressBar) {
//            int percent = (int) (currentDuration * 1.0f / totle * 100);
            mDurationBar.setProgress(currentDuration);//设置进程。。自己.   --直接绑定
//        LogUtil.d(TAG,"percent --->" + percent);
        }

    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void onTrackUpdata(Track track, int playIndex) {
        if (track == null) {
            LogUtil.d(TAG,"onTrackUpdate --- > track null");
            return;
        }
        this.mTitleTitleText = track.getTrackTitle();
        if (mTitleTitleTv != null) {
            //设置当节目的标题
            mTitleTitleTv.setText(mTitleTitleText);
        }
        //当节目改变时，获取当前播放的位置
        //当前节目修改以后，修改页面图片
        if (mTrackPagerView != null) {
            mTrackPagerView.setCurrentItem(playIndex, true);
        }
        //设置播放列表当前的播放位置
        if (mSobPopWindow != null) {
            mSobPopWindow.setCurrentPlayPosition(playIndex);
        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {
        //更新UI
        mSobPopWindow.updateOrderIcon(isReverse);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //滑动时
    }

    @Override
    public void onPageSelected(int position) {

        //页面选中时，切换播放内容  ··                      //为什么要加这个touch
        if (mPlayerPresenter != null && mIsUserSlidePager) {//mIsUserSlidePager -->true时才能循环
            mPlayerPresenter.playByIndex(position);//页面已经切了，没必要再次拨放这一首。等于说拨了2次
        }
        mIsUserSlidePager = false;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}