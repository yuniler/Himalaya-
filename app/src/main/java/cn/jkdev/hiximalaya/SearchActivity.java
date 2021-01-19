package cn.jkdev.hiximalaya;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;

import java.util.ArrayList;
import java.util.List;

import cn.jkdev.hiximalaya.base.BaseActivity;
import cn.jkdev.hiximalaya.interfaces.ISearchCallback;
import cn.jkdev.hiximalaya.presenters.SearchPresenter;
import cn.jkdev.hiximalaya.utils.LogUtil;
import cn.jkdev.hiximalaya.views.FlowTextLayout;

public class SearchActivity extends BaseActivity implements ISearchCallback {

    private static final String TAG = "SearchActivity";
    private View mBackBtn;
    private EditText mInputBox;
    private View mSearchBtn;
    private FrameLayout mResultContainer;
    private SearchPresenter mSearchPresenter;
    private FlowTextLayout mFlowTextLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initView();
        initEvent();
        initPresenter();
    }


    private void initPresenter() {

        mSearchPresenter = SearchPresenter.getSearchPresenter();
        //注册UI更新接口
        mSearchPresenter.registerViewCallback(this);//注册UI接口
        //去拿热词
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSearchPresenter != null) {
            //干掉UI更新的接口
            mSearchPresenter.unRegisterViewCallback(this);
            mSearchPresenter = null;
        }
    }

    private void initEvent() {
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();//结束当前界面
            }
        });

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:执行搜索--调用搜索的逻辑            }
            }
        });

        mInputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                LogUtil.d(TAG,"content - - >" + s);
                LogUtil.d(TAG,"start- - >" + start);
                LogUtil.d(TAG,"before- - >" + before);
                LogUtil.d(TAG,"count- - >" + count);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initView() {
        mBackBtn = this.findViewById(R.id.search_back);
        mInputBox = this.findViewById(R.id.search_input);
        mSearchBtn = this.findViewById(R.id.search_btn);
        mResultContainer = this.findViewById(R.id.search_container);
        mFlowTextLayout = this.findViewById(R.id.flow_text_layout);


    }

    @Override
    public void onSearchRecsultLoaded(List<Album> result) {

    }

    @Override
    public void onHotWordLoaded(List<HotWord> hotWordList) {

        LogUtil.d(TAG,"hotWordList --- > " + hotWordList.size());//逻辑层需要拿到回调
        List<String> hotWords = new ArrayList<>();
        hotWords.clear();
        for (HotWord hotWord : hotWordList) {
            String searchWord = hotWord.getSearchword();
            hotWords.add(searchWord);
        }
        //更新UI
        mFlowTextLayout.setTextContents(hotWords);

    }

    @Override
    public void lonLoadedMoreResult(List<Album> result, boolean isOkey) {

    }

    @Override
    public void onRecommendWordLoaded(List<QueryResult> keyWordList) {

    }
}
