package com.hfs.alipayall;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.RecyclerView.RecycledViewPool;
import android.support.v7.widget.RecyclerView.SmoothScroller;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hfs.alipayall.adapter.ContentItemAdapter;
import com.hfs.alipayall.adapter.TitleAdapter;
import com.hfs.alipayall.bean.Item;
import com.hfs.alipayall.bean.Item.SubItem;
import com.hfs.alipayall.widget.FixAppBarLayoutBehavior;
import com.hfs.alipayall.widget.pager.PagerGridLayoutManager;
import com.hfs.alipayall.widget.pager.PagerGridSnapHelper;
import com.hfs.alipayall.widget.tablayout.ExTabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HuangFusheng
 * @date 2019-11-11
 * description MainActivity
 */
public class MainActivity extends AppCompatActivity implements PagerGridLayoutManager.PageListener, TitleAdapter.OnItemClickListener {

    private static final String TAG = "MainActivity";
    private static final int M_ROWS = 3;
    private static final int M_COLUMNS = 3;

    /**
     * TabLayout
     */
    private ExTabLayout mTabLayout;
    /**
     * 内容列表
     */
    private RecyclerView mRecyclerView;
    /**
     * AppBarLayout
     */
    private AppBarLayout mAppBar;

    /**
     * LinearLayoutManager
     */
    private LinearLayoutManager mLinearLayoutManager;
    /**
     * 数据源
     */
    private ArrayList<Item> mContentList = new ArrayList<>();


    /**
     * 是否处于滚动状态，避免连锁反应
     */
    private boolean isScroll;
    /**
     * RecyclerView高度
     */
    private int mRecyclerViewHeight;

    /**
     * 平滑滚动 Scroller
     */
    private SmoothScroller mSmoothScroller;

    private LinearLayout mSubView;
    private RecyclerView mRvTitle;
    private TitleAdapter mTitleAdapter;
    private MyAdapter mMyAdapter;
    private List<SubItem> mTitleList = new ArrayList<>();
    private PagerGridLayoutManager mLayoutManager;
    private ProgressDialog mProgressDialog;
    private boolean mEditMode = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mProgressDialog.dismiss();
            initData();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTabLayout = findViewById(R.id.tab_layout);
        mRecyclerView = findViewById(R.id.recycler_view);
        mAppBar = findViewById(R.id.app_bar);
        mSubView = findViewById(R.id.ll_sub_view);
        mRvTitle = findViewById(R.id.recycler_view_title);
        ((CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams()).setBehavior(new FixAppBarLayoutBehavior());
        initRecyclerView();
        initTitleRecyclerView();
        initTabLayout();

        mProgressDialog = ProgressDialog.show(this, "提示", "获取数据中...", false, false);
        mProgressDialog.show();
        mHandler.sendEmptyMessageDelayed(1, 2000);
    }

    /**
     * 初始化TabLayout
     */
    private void initTabLayout() {

        mTabLayout.setTabMode(ExTabLayout.MODE_SCROLLABLE);
        mTabLayout.addOnTabSelectedListener(new ExTabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(ExTabLayout.Tab tab) {
                //点击tab的时候，RecyclerView自动滑到该tab对应的item位置
                int position = tab.getPosition();
                if (!isScroll) {
                    mSmoothScroller.setTargetPosition(position);
                    mLinearLayoutManager.startSmoothScroll(mSmoothScroller);
                    if (!mEditMode) {
                        smoothScroll(mSubView.getHeight());
                    }

                }
            }

            @Override
            public void onTabUnselected(ExTabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(ExTabLayout.Tab tab) {

            }
        });


    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        mSmoothScroller = new LinearSmoothScroller(this) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Nullable
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return mLinearLayoutManager.computeScrollVectorForPosition(targetPosition);
            }
        };

        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isScroll = false;
                } else {
                    isScroll = true;
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //滑动RecyclerView list的时候，根据最上面一个Item的position来切换tab
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                ExTabLayout.Tab tabAt = mTabLayout.getTabAt(firstVisibleItemPosition);
                if (tabAt != null && !tabAt.isSelected()) {
                    tabAt.select();
                }
            }
        });

        mMyAdapter = new MyAdapter(mContentList);
        mRecyclerView.setAdapter(mMyAdapter);
        mRecyclerView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
    }

    /**
     * 初始化已选应用
     */
    private void initTitleRecyclerView() {
        mLayoutManager = new PagerGridLayoutManager(M_ROWS, M_COLUMNS, PagerGridLayoutManager.HORIZONTAL);
        mLayoutManager.setPageListener(this);
        mRvTitle.setLayoutManager(mLayoutManager);

        // 设置滚动辅助工具
        PagerGridSnapHelper pageSnapHelper = new PagerGridSnapHelper();
        pageSnapHelper.attachToRecyclerView(mRvTitle);

        // 使用原生的 Adapter 即可
        mTitleAdapter = new TitleAdapter(mTitleList, this);
        mRvTitle.setAdapter(mTitleAdapter);
    }

    @Override
    public void onPageSizeChanged(int pageSize) {
        Log.e(TAG, "onPageSizeChanged: " + pageSize);
    }

    @Override
    public void onPageSelect(int pageIndex) {
        Log.e(TAG, "onPageSelect: " + pageIndex);
    }

    @Override
    public void onItemClick(Item.SubItem subItem, int position) {
        if (!mEditMode) {
            Toast.makeText(this, "点击了" + subItem.getName() + "  id为" + subItem.getId(), Toast.LENGTH_SHORT).show();
            return;
        }
        mTitleList.remove(position);
        mTitleAdapter.notifyDataSetChanged();
        scrollToPage();

        for (int i = 0; i < mContentList.size(); i++) {
            Item item = mContentList.get(i);
            List<SubItem> subItems = item.mSubItems;
            for (int j = 0; j < subItems.size(); j++) {
                SubItem contentItem = subItems.get(j);
                if (subItem.getId().equals(contentItem.getId())) {
                    contentItem.setAdded(false);
                    MyAdapter.ItemViewHolder viewHolderForLayoutPosition = (MyAdapter.ItemViewHolder) mRecyclerView.findViewHolderForLayoutPosition(i);
                    if (viewHolderForLayoutPosition != null) {
                        ContentItemAdapter.ViewHolder holder = (ContentItemAdapter.ViewHolder) viewHolderForLayoutPosition.mRecyclerView.findViewHolderForLayoutPosition(j);
                        if (holder != null) {
                            holder.mIvPot.setImageResource(R.drawable.icon_add_commonly);
                        }
                    } else {
                        mMyAdapter.notifyItemChanged(i);
                    }
                    break;
                }
            }
        }

    }


    class MyAdapter extends RecyclerView.Adapter {

        public static final int VIEW_TYPE_ITEM = 1;
        public static final int VIEW_TYPE_FOOTER = 2;
        /**
         * 是否是编辑模式
         */
        private boolean mEditMode = false;
        /**
         * 数据源
         */
        private List<Item> mData;
        /**
         * 复用同一个View对象池
         */
        private RecycledViewPool mRecycledViewPool;
        /**
         * item高度
         */
        private int itemHeight;


        public MyAdapter(@Nullable List<Item> data) {
            mData = data;
            mRecycledViewPool = new RecycledViewPool();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.demo_item, parent, false);
                return new ItemViewHolder(view);
            } else {
                View footView = LayoutInflater.from(parent.getContext()).inflate(R.layout.demo_item_footer, parent, false);
                return new FooterViewHolder(footView);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
                final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                itemViewHolder.mTitle.setText(mData.get(position).name);
                RecyclerView recyclerView = itemViewHolder.mRecyclerView;
                recyclerView.setRecycledViewPool(mRecycledViewPool);
                recyclerView.setHasFixedSize(false);
                recyclerView.setNestedScrollingEnabled(false);
                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
                recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), 3) {
                    @Override
                    public boolean canScrollVertically() {
                        //保证嵌套滚动不冲突
                        return false;
                    }

                    @Override
                    public void onLayoutCompleted(State state) {
                        super.onLayoutCompleted(state);
                        mRecyclerViewHeight = mRecyclerView.getHeight();
                        itemHeight = itemViewHolder.itemView.getHeight();
                    }
                });
                final ContentItemAdapter contentItemAdapter = new ContentItemAdapter(mData.get(position).mSubItems);
                contentItemAdapter.setEditMode(mEditMode);
                recyclerView.setAdapter(contentItemAdapter);
                contentItemAdapter.setOnItemClickListener(new ContentItemAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(SubItem subItem, int position) {
                        addToTitle(subItem, position, contentItemAdapter);
                    }
                });
            } else {
                FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
                int tilteHeight = footerViewHolder.mFooterTilte.getHeight();
                View emptyView = footerViewHolder.mFooterEmpty;
                emptyView.setLayoutParams(
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                mRecyclerViewHeight - itemHeight - tilteHeight));
            }

        }

        @Override
        public int getItemViewType(int position) {
            if (position == mData.size()) {
                return VIEW_TYPE_FOOTER;
            } else {
                return VIEW_TYPE_ITEM;
            }
        }

        @Override
        public int getItemCount() {
            return mData.size() == 0 ? 0 : mData.size() + 1;
        }

        public void setEditMode(boolean edit) {
            mEditMode = edit;
            notifyDataSetChanged();
        }

        class FooterViewHolder extends RecyclerView.ViewHolder {

            private TextView mFooterTilte;
            private View mFooterEmpty;

            public FooterViewHolder(View itemView) {
                super(itemView);
                mFooterTilte = itemView.findViewById(R.id.tv_footer);
                mFooterEmpty = itemView.findViewById(R.id.view_footer_empty);
            }
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            private TextView mTitle;
            private RecyclerView mRecyclerView;

            public ItemViewHolder(View itemView) {
                super(itemView);
                mTitle = (TextView) itemView.findViewById(R.id.demo_item_text);
                mRecyclerView = (RecyclerView) itemView.findViewById(R.id.demo_item_recycler_view);
            }
        }
    }

    /**
     * 点击添加到已选应用
     */
    private void addToTitle(SubItem subItem, int itemPsoition, ContentItemAdapter contentItemAdapter) {
        if (!mEditMode) {
            Toast.makeText(this, "点击了" + subItem.getName() + "  id为" + subItem.getId(), Toast.LENGTH_SHORT).show();
            return;
        }
        int position = getExistPosition(subItem);
        //表示不存在
        if (position == -1) {
            mTitleList.add(subItem);
            mTitleAdapter.notifyDataSetChanged();
            subItem.setAdded(true);
            contentItemAdapter.notifyItemChanged(itemPsoition);

        } else {
            mTitleList.remove(position);
            mTitleAdapter.notifyDataSetChanged();
            subItem.setAdded(false);
            contentItemAdapter.notifyItemChanged(itemPsoition);
        }
        scrollToPage();

    }

    /**
     * 已选应用滑动到指定页数
     */
    private void scrollToPage() {
        int page = mTitleList.size() / 9;
        if (mTitleList.size() % 9 == 0) {
            page--;
        }
        final int finalPage = page;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mLayoutManager.smoothScrollToPage(finalPage);
            }
        }, 100);
    }


    /**
     * 获取已选应用存在的位置 不存在则返回-1
     */
    private int getExistPosition(SubItem subItem) {
        if (mTitleList.size() == 0) {
            return -1;
        }

        for (int i = 0; i < mTitleList.size(); i++) {
            SubItem item = mTitleList.get(i);
            if (item.getId().equals(subItem.getId())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 完成
     */
    public void complete(View view) {
        if (!mEditMode) {
            return;
        }
        mEditMode = false;
        defaultScroll();
        mMyAdapter.setEditMode(false);
        mTitleAdapter.setEditMode(false);
    }

    /**
     * 编辑
     */
    public void edit(View view) {
        if (mEditMode) {
            return;
        }
        mEditMode = true;
        mMyAdapter.setEditMode(true);
        mTitleAdapter.setEditMode(true);
        mRecyclerView.smoothScrollToPosition(0);
        noScroll();
//        smoothScrollToTop();
//        ExTabLayout.Tab tabAt = mTabLayout.getTabAt(0);
//        if (tabAt != null && !tabAt.isSelected()) {
//            tabAt.select();
//        }

    }

    /**
     * 滑动到顶部
     */
    private void smoothScrollToTop() {
        CoordinatorLayout.Behavior behavior =
                ((CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams()).getBehavior();
        if (behavior instanceof AppBarLayout.Behavior) {
            final AppBarLayout.Behavior appBarLayoutBehavior = (AppBarLayout.Behavior) behavior;
            int topAndBottomOffset = appBarLayoutBehavior.getTopAndBottomOffset();
            if (topAndBottomOffset != 0) {
                appBarLayoutBehavior.setTopAndBottomOffset(0);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        appBarLayoutBehavior.setTopAndBottomOffset(0);
                    }
                }, 200);
            }

        }
    }

    /**
     * 滑动到指定距离
     *
     * @param dx
     */
    private void smoothScroll(int dx) {
        CoordinatorLayout.Behavior behavior =
                ((CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams()).getBehavior();
        if (behavior instanceof AppBarLayout.Behavior) {
            AppBarLayout.Behavior appBarLayoutBehavior = (AppBarLayout.Behavior) behavior;
            appBarLayoutBehavior.setTopAndBottomOffset(-dx);

        }
    }

    /**
     * 已选应用禁止滑动
     */
    private void noScroll() {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mSubView.getLayoutParams();
        params.setScrollFlags(0);
        mSubView.setLayoutParams(params);
    }

    /**
     * 已选应用恢复滑动
     */
    private void defaultScroll() {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mSubView.getLayoutParams();
        params.setScrollFlags(0x1);
        mSubView.setLayoutParams(params);
    }

    /**
     * 模拟网络数据
     */
    private void initData() {
        ArrayList<SubItem> titleItems = new ArrayList<>();
        titleItems.add(new SubItem("便民生活", "这是描述", "b2"));
        titleItems.add(new SubItem("便民生活", "这是描述", "b3"));
        titleItems.add(new SubItem("财富管理", "这是描述", "c1"));
        titleItems.add(new SubItem("财富管理", "这是描述", "c6"));
        titleItems.add(new SubItem("资金往来", "这是描述", "z5"));
        titleItems.add(new SubItem("资金往来", "这是描述", "z7"));
        titleItems.add(new SubItem("娱乐购物", "这是描述", "y8"));
        titleItems.add(new SubItem("娱乐购物", "这是描述", "y9"));
        mTitleList.clear();
        mTitleList.addAll(titleItems);
        mTitleAdapter.notifyDataSetChanged();

        ArrayList<Item> contentList = new ArrayList<>();
        Item item = new Item();
        item.name = "便民生活";
        ArrayList<SubItem> itemsub = new ArrayList<>();
        itemsub.add(new SubItem("便民生活", "这是描述", "b1"));
        itemsub.add(new SubItem("便民生活", "这是描述", "b2"));
        itemsub.add(new SubItem("便民生活", "这是描述", "b3"));
        itemsub.add(new SubItem("便民生活", "这是描述", "b4"));
        itemsub.add(new SubItem("便民生活", "这是描述", "b5"));
        itemsub.add(new SubItem("便民生活", "这是描述", "b6"));
        itemsub.add(new SubItem("便民生活", "这是描述", "b7"));
        itemsub.add(new SubItem("便民生活", "这是描述", "b8"));
        itemsub.add(new SubItem("便民生活", "这是描述", "b9"));
        item.mSubItems = itemsub;
        contentList.add(item);

        item = new Item();
        item.name = "财富";
        ArrayList<SubItem> itemsub1 = new ArrayList<>();
        itemsub1.add(new SubItem("财富管理", "这是描述", "c1"));
        itemsub1.add(new SubItem("财富管理", "这是描述", "c2"));
        itemsub1.add(new SubItem("财富管理", "这是描述", "c3"));
        itemsub1.add(new SubItem("财富管理", "这是描述", "c4"));
        itemsub1.add(new SubItem("财富管理", "这是描述", "c5"));
        itemsub1.add(new SubItem("财富管理", "这是描述", "c6"));
        itemsub1.add(new SubItem("财富管理", "这是描述", "c7"));
        itemsub1.add(new SubItem("财富管理", "这是描述", "c8"));
        itemsub1.add(new SubItem("财富管理", "这是描述", "c9"));
        item.mSubItems = itemsub1;
        contentList.add(item);

        item = new Item();
        item.name = "资金";
        ArrayList<SubItem> itemsub2 = new ArrayList<>();
        itemsub2.add(new SubItem("资金往来", "这是描述", "z1"));
        itemsub2.add(new SubItem("资金往来", "这是描述", "z2"));
        itemsub2.add(new SubItem("资金往来", "这是描述", "z3"));
        itemsub2.add(new SubItem("资金往来", "这是描述", "z4"));
        itemsub2.add(new SubItem("资金往来", "这是描述", "z5"));
        itemsub2.add(new SubItem("资金往来", "这是描述", "z6"));
        itemsub2.add(new SubItem("资金往来", "这是描述", "z7"));
        itemsub2.add(new SubItem("资金往来", "这是描述", "z8"));
        itemsub2.add(new SubItem("资金往来", "这是描述", "z9"));
        item.mSubItems = itemsub2;
        contentList.add(item);

        item = new Item();
        item.name = "销售";
        ArrayList<SubItem> itemsub3 = new ArrayList<>();
        itemsub3.add(new SubItem("销售管理", "这是描述", "x1"));
        itemsub3.add(new SubItem("销售管理", "这是描述", "x2"));
        itemsub3.add(new SubItem("销售管理", "这是描述", "x3"));
        itemsub3.add(new SubItem("销售管理", "这是描述", "x4"));
        itemsub3.add(new SubItem("销售管理", "这是描述", "x5"));
        item.mSubItems = itemsub3;
        contentList.add(item);

        item = new Item();
        item.name = "娱乐购物";
        ArrayList<SubItem> itemsub4 = new ArrayList<>();
        itemsub4.add(new SubItem("娱乐购物", "这是描述", "y1"));
        itemsub4.add(new SubItem("娱乐购物", "这是描述", "y2"));
        itemsub4.add(new SubItem("娱乐购物", "这是描述", "y3"));
        itemsub4.add(new SubItem("娱乐购物", "这是描述", "y4"));
        itemsub4.add(new SubItem("娱乐购物", "这是描述", "y5"));
        itemsub4.add(new SubItem("娱乐购物", "这是描述", "y6"));
        itemsub4.add(new SubItem("娱乐购物", "这是描述", "y7"));
        itemsub4.add(new SubItem("娱乐购物", "这是描述", "y8"));
        itemsub4.add(new SubItem("娱乐购物", "这是描述", "y9"));
        item.mSubItems = itemsub4;
        contentList.add(item);

        item = new Item();
        item.name = "库存";
        ArrayList<SubItem> itemsub5 = new ArrayList<>();
        itemsub5.add(new SubItem("库存", "这是描述", "k1"));
        itemsub5.add(new SubItem("库存", "这是描述", "k2"));
        itemsub5.add(new SubItem("库存", "这是描述", "k3"));
        itemsub5.add(new SubItem("库存", "这是描述", "k4"));
        itemsub5.add(new SubItem("库存", "这是描述", "k5"));
        itemsub5.add(new SubItem("库存", "这是描述", "k6"));
        item.mSubItems = itemsub5;
        contentList.add(item);

        item = new Item();
        item.name = "营销";
        ArrayList<SubItem> itemsub6 = new ArrayList<>();
        itemsub6.add(new SubItem("营销", "这是描述", "a1"));
        itemsub6.add(new SubItem("营销", "这是描述", "a2"));
        itemsub6.add(new SubItem("营销", "这是描述", "a3"));
        itemsub6.add(new SubItem("营销", "这是描述", "a4"));
        itemsub6.add(new SubItem("营销", "这是描述", "a5"));
        itemsub6.add(new SubItem("营销", "这是描述", "a6"));
        item.mSubItems = itemsub6;
        contentList.add(item);

        item = new Item();
        item.name = "商品";
        ArrayList<SubItem> itemsub7 = new ArrayList<>();
        itemsub7.add(new SubItem("商品", "这是描述", "s1"));
        itemsub7.add(new SubItem("商品", "这是描述", "s2"));
        itemsub7.add(new SubItem("商品", "这是描述", "s3"));
        itemsub7.add(new SubItem("商品", "这是描述", "s4"));
        itemsub7.add(new SubItem("商品", "这是描述", "s5"));
        itemsub7.add(new SubItem("商品", "这是描述", "s6"));
        itemsub7.add(new SubItem("商品", "这是描述", "s7"));
        item.mSubItems = itemsub7;
        contentList.add(item);

        mContentList.clear();
        mContentList.addAll(contentList);

        //判断下面列表哪些已经被添加到我已选列表里面
        for (int i = 0; i < mContentList.size(); i++) {
            Item contentItem = mContentList.get(i);
            List<SubItem> subItems = contentItem.mSubItems;
            for (int j = 0; j < subItems.size(); j++) {
                for (int k = 0; k < mTitleList.size(); k++) {
                    SubItem subItem = subItems.get(j);
                    SubItem titleItem = mTitleList.get(k);
                    if (subItem.getId().equals(titleItem.getId())) {
                        subItem.setAdded(true);
                    }
                }
            }
        }

        //这里模仿接口回调，动态设置TabLayout和RecyclerView 相同数据。保证position
        for (Item it : contentList) {
            mTabLayout.addTab(mTabLayout.newTab().setText(it.name));
        }

        mMyAdapter.notifyDataSetChanged();
        smoothScrollToTop();

    }


}
