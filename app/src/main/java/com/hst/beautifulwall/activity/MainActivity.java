package com.hst.beautifulwall.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Bundle;

import androidx.viewpager.widget.ViewPager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hst.beautifulwall.R;
import com.hst.beautifulwall.adapters.HomeCategoryAdapter;
import com.hst.beautifulwall.adapters.PostsPagerAdapter;
import com.hst.beautifulwall.adapters.HomeRecentPostAdapter;
import com.hst.beautifulwall.app.MyApplication;
import com.hst.beautifulwall.data.constant.AppConstant;
import com.hst.beautifulwall.data.sqlite.FavoriteDbController;
import com.hst.beautifulwall.data.sqlite.NotificationDbController;
import com.hst.beautifulwall.listeners.ListItemClickListener;
import com.hst.beautifulwall.models.content.Categories;
import com.hst.beautifulwall.models.content.Posts;
import com.hst.beautifulwall.models.favorite.FavoriteModel;
import com.hst.beautifulwall.models.notification.NotificationModel;
import com.hst.beautifulwall.utility.ActivityUtilities;
import com.hst.beautifulwall.utility.AppUtilities;
import com.hst.beautifulwall.utility.RateItDialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity {

    private Activity mActivity;
    private Context mContext;

    private RelativeLayout mNotificationView;

    private TextView mPointWallet;

    private LinearLayout mPointView;
    private ImageButton mImgBtnSearch;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RelativeLayout mLytCategory, mLytFeatured, mLytRecent;
    private LinearLayout mLytMain;

    // Featured
    private ArrayList<Posts> mFeaturedList;
    private ViewPager mFeaturedPager;
    private PostsPagerAdapter mPostsPagerAdapter = null;

    // Category
    private ArrayList<Categories> mCategoryList;
    private HomeCategoryAdapter mHomeCategoryAdapter = null;
    private RecyclerView mCategoryRecycler;

    // Recent
    private ArrayList<Posts> mRecentPostList;
    private ArrayList<Posts> mHomeRecentPostList;
    private HomeRecentPostAdapter mRecentPostAdapter = null;
    private RecyclerView mRecentRecycler;

    // Favourites view
    private List<FavoriteModel> mFavoriteList;
    private FavoriteDbController mFavoriteDbController;

    private TextView mViewAllFeatured, mViewAllRecent;

    // Firebase Database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFirebase();

        RateItDialogFragment.show(this, getSupportFragmentManager());

        initVar();
        initView();
        loadData();
        initListener();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //unregister broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newNotificationReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //register broadcast receiver
        IntentFilter intentFilter = new IntentFilter(AppConstant.NEW_NOTI);
        LocalBroadcastManager.getInstance(this).registerReceiver(newNotificationReceiver, intentFilter);

        initNotification();

        if (mRecentPostList.size() != 0) {
            updateUI();
        }
    }

    // received new broadcast
    private BroadcastReceiver newNotificationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            initNotification();
        }
    };


    @Override
    public void onBackPressed() {
        AppUtilities.tapPromptToExit(mActivity);
    }

    private void initVar() {
        mActivity = MainActivity.this;
        mContext = getApplicationContext();

        mCategoryList = new ArrayList<>();
        mFeaturedList = new ArrayList<>();
        mRecentPostList = new ArrayList<>();
        mHomeRecentPostList = new ArrayList<>();
        mFavoriteList = new ArrayList<>();
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        mNotificationView = (RelativeLayout) findViewById(R.id.notificationView);
        mPointView = (LinearLayout) findViewById(R.id.pointView);
        mPointWallet = (TextView) findViewById(R.id.pointWallet);
        mImgBtnSearch = (ImageButton) findViewById(R.id.imgBtnSearch);

        mCategoryRecycler = (RecyclerView) findViewById(R.id.rvCategories);
        mCategoryRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mHomeCategoryAdapter = new HomeCategoryAdapter(mContext, mActivity, mCategoryList);
        mCategoryRecycler.setAdapter(mHomeCategoryAdapter);

        mFeaturedPager = (ViewPager) findViewById(R.id.pager_featured_posts);
        mViewAllFeatured = (TextView) findViewById(R.id.view_all_featured);

        mRecentRecycler = (RecyclerView) findViewById(R.id.rvRecent);
        mRecentRecycler.setLayoutManager(new GridLayoutManager(mActivity, 2, GridLayoutManager.VERTICAL, false));
        mRecentPostAdapter = new HomeRecentPostAdapter(mContext, mActivity, mHomeRecentPostList);
        mRecentRecycler.setAdapter(mRecentPostAdapter);
        mViewAllRecent = (TextView) findViewById(R.id.view_all_recent);


        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mLytCategory = (RelativeLayout) findViewById(R.id.lyt_categories);
        mLytFeatured = (RelativeLayout) findViewById(R.id.lyt_featured);
        mLytRecent = (RelativeLayout) findViewById(R.id.lyt_recent);
        mLytMain = (LinearLayout) findViewById(R.id.lyt_main);


        initToolbar(false);
        initDrawer();
        initLoader();
    }

    private void initListener() {
        //notification view click listener
        mNotificationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtilities.getInstance().invokeNewActivity(mActivity, NotificationListActivity.class, false);
            }
        });

        mPointView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtilities.getInstance().invokeNewActivity(mActivity, PurchaseInAppActivity.class, false);
            }
        });

        // Search button click listener
        mImgBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityUtilities.getInstance().invokeNewActivity(mActivity, SearchActivity.class, false);
            }
        });

        // recycler list item click listener
        mHomeCategoryAdapter.setItemClickListener(new ListItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                Categories model = mCategoryList.get(position);
                switch (view.getId()) {
                    case R.id.lyt_container:
                        ActivityUtilities.getInstance().invokeCatWisePostListActiviy(mActivity, CategoryWisePostListActivity.class, model.getTitle(), false);
                        break;
                    default:
                        break;
                }
            }

        });

        mRecentPostAdapter.setItemClickListener(new ListItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                Posts model = mRecentPostList.get(position);
                switch (view.getId()) {
                    case R.id.btn_fav:
                        if (model.isFavorite()) {
                            mFavoriteDbController.deleteEachFav(mRecentPostList.get(position).getTitle());
                            mRecentPostList.get(position).setFavorite(false);
                            mHomeRecentPostList.get(position).setFavorite(false);
                            mRecentPostAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), getString(R.string.removed_from_fav), Toast.LENGTH_SHORT).show();

                        } else {
                            mFavoriteDbController.insertData(model.getTitle(), model.getImageUrl(), model.getCategory());
                            mRecentPostList.get(position).setFavorite(true);
                            mHomeRecentPostList.get(position).setFavorite(true);
                            mRecentPostAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), getString(R.string.added_to_fav), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.card_view_top:
                        if (model.getIsNeedPoint()) {
                            if (MyApplication.getInstance().getValueCoin() >= 2) {
                                MyApplication.getInstance().setValueCoin(MyApplication.getInstance().getValueCoin() - 2);
                                ActivityUtilities.getInstance().invokeDetailsActiviy(mActivity, DetailsActivity.class, mHomeRecentPostList, position, false);
                            }
                            else {
                                Toast.makeText(MainActivity.this, "You need more coin to using this image!", Toast.LENGTH_LONG).show();
                            }
                        }
                        else {
                            ActivityUtilities.getInstance().invokeDetailsActiviy(mActivity, DetailsActivity.class, mHomeRecentPostList, position, false);
                        }
                        break;
                    default:
                        break;
                }
            }

        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mLytMain.setVisibility(View.GONE);
                mCategoryList.clear();
                mFeaturedList.clear();
                mRecentPostList.clear();
                mHomeRecentPostList.clear();
                loadData();
            }
        });

        mFeaturedPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                swipeRefreshController(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });

        mViewAllFeatured.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityUtilities.getInstance().invokeNewActivity(mActivity, FeaturedListActivity.class, false);
            }
        });

        mViewAllRecent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityUtilities.getInstance().invokeNewActivity(mActivity, RecentListActivity.class, false);
            }
        });

    }

    private void initFirebase() {
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
    }

    private void updateUI() {
        if (mFavoriteDbController == null) {
            mFavoriteDbController = new FavoriteDbController(mContext);
        }
        mFavoriteList.clear();
        mFavoriteList.addAll(mFavoriteDbController.getAllData());

        // Check for favorite
        for (int i = 0; i < mRecentPostList.size(); i++) {
            boolean isFavorite = false;
            for (int j = 0; j < mFavoriteList.size(); j++) {
                if (mFavoriteList.get(j).getTitle().equals(mRecentPostList.get(i).getTitle())) {
                    isFavorite = true;
                    break;
                }
            }
            mRecentPostList.get(i).setFavorite(isFavorite);
        }

        if (mRecentPostList.size() == 0) {
            showEmptyView();
        } else {
            mRecentPostAdapter.notifyDataSetChanged();
            hideLoader();
        }

        mPointWallet.setText(MyApplication.getInstance().getValueCoin() + "");
    }

    private void swipeRefreshController(boolean enable) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(enable);
        }
    }

    private void loadData() {
        showLoader();

        loadCategoriesFromFirebase();
        loadPostsFromFirebase();

        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

    }

    private void loadCategoriesFromFirebase() {
        mDatabaseReference.child(AppConstant.JSON_KEY_CATEGORIES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot contentSnapShot : dataSnapshot.getChildren()) {
                    Categories category = contentSnapShot.getValue(Categories.class);
                    mCategoryList.add(category);
                }
                mLytCategory.setVisibility(View.VISIBLE);
                mLytMain.setVisibility(View.VISIBLE);
                mHomeCategoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showEmptyView();
            }
        });
    }

    private void loadPostsFromFirebase() {
        mDatabaseReference.child(AppConstant.JSON_KEY_IMAGES).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot contentSnapShot : dataSnapshot.getChildren()) {
                    Posts post = contentSnapShot.getValue(Posts.class);
                    mRecentPostList.add(post);
                }

                Collections.shuffle(mRecentPostList);

                int maxLoop;
                if (AppConstant.BUNDLE_KEY_HOME_INDEX > mRecentPostList.size()) {
                    maxLoop = mRecentPostList.size();
                } else {
                    maxLoop = AppConstant.BUNDLE_KEY_HOME_INDEX;
                }
                for (int i = 0; i < maxLoop; i++) {
                    mHomeRecentPostList.add(mRecentPostList.get(i));
                }

                for (int i = 0; i < mRecentPostList.size(); i++) {
                    if (mRecentPostList.get(i).getIsFeatured().equals(AppConstant.JSON_KEY_YES)) {
                        mFeaturedList.add(mRecentPostList.get(i));
                    }
                }
                Collections.shuffle(mFeaturedList);
                mPostsPagerAdapter = new PostsPagerAdapter(mActivity, (ArrayList<Posts>) mFeaturedList);
                mFeaturedPager.setAdapter(mPostsPagerAdapter);
                mPostsPagerAdapter.setItemClickListener(new ListItemClickListener() {
                    @Override
                    public void onItemClick(int position, View view) {
                        if (mFeaturedList.get(position).getIsNeedPoint()){
                            if (MyApplication.getInstance().getValueCoin() >= 2) {
                                MyApplication.getInstance().setValueCoin(MyApplication.getInstance().getValueCoin() - 2);
                                ActivityUtilities.getInstance().invokeDetailsActiviy(mActivity, DetailsActivity.class, mFeaturedList, position, false);
                            }
                            else {
                                Toast.makeText(MainActivity.this, "You need more coin to using this image!", Toast.LENGTH_LONG).show();
                            }
                        }
                        else
                        ActivityUtilities.getInstance().invokeDetailsActiviy(mActivity, DetailsActivity.class, mFeaturedList, position, false);
                    }
                });
                if (mFeaturedList.size() > 0) {
                    mLytFeatured.setVisibility(View.VISIBLE);
                }

                updateUI();

                if (mRecentPostList.size() > 0) {
                    mLytRecent.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showEmptyView();
            }
        });
    }

    public void initNotification() {
        NotificationDbController notificationDbController = new NotificationDbController(mContext);
        TextView notificationCount = (TextView) findViewById(R.id.notificationCount);
        notificationCount.setVisibility(View.INVISIBLE);

        ArrayList<NotificationModel> notiArrayList = notificationDbController.getUnreadData();

        if (notiArrayList != null && !notiArrayList.isEmpty()) {
            int totalUnread = notiArrayList.size();
            if (totalUnread > 0) {
                notificationCount.setVisibility(View.VISIBLE);
                notificationCount.setText(String.valueOf(totalUnread));
            } else {
                notificationCount.setVisibility(View.INVISIBLE);
            }
        }
    }
}
