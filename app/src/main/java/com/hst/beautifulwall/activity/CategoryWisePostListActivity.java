package com.hst.beautifulwall.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.hst.beautifulwall.R;
import com.hst.beautifulwall.adapters.HomeRecentPostAdapter;
import com.hst.beautifulwall.adapters.PostsPagerAdapter;
import com.hst.beautifulwall.app.MyApplication;
import com.hst.beautifulwall.data.constant.AppConstant;
import com.hst.beautifulwall.data.sqlite.FavoriteDbController;
import com.hst.beautifulwall.io.APIClient;
import com.hst.beautifulwall.io.APIInterface;
import com.hst.beautifulwall.io.model.Image;
import com.hst.beautifulwall.io.model.WallResponse;
import com.hst.beautifulwall.listeners.ListItemClickListener;
import com.hst.beautifulwall.models.content.Posts;
import com.hst.beautifulwall.models.favorite.FavoriteModel;
import com.hst.beautifulwall.utility.ActivityUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CategoryWisePostListActivity extends BaseActivity {

    private Activity mActivity;
    private Context mContext;

    private ArrayList<Posts> mContentList;
    private ArrayList<Posts> mCatWisePostList;
    private HomeRecentPostAdapter mAdapter = null;
    private RecyclerView mRecycler;
    private String mCategoryName;

    // Favourites view
    private List<FavoriteModel> mFavoriteList;
    private FavoriteDbController mFavoriteDbController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFirebase();

        initVar();
        initView();
        initFunctionality();
        initListener();

    }

    private void initVar() {
        mActivity = CategoryWisePostListActivity.this;
        mContext = mActivity.getApplicationContext();

        Intent intent = getIntent();
        if (intent != null) {
            mCategoryName = intent.getStringExtra(AppConstant.BUNDLE_KEY_TITLE);
        }

        mContentList = new ArrayList<>();
        mFavoriteList = new ArrayList<>();
        mCatWisePostList = new ArrayList<>();
    }

    private void initView() {
        setContentView(R.layout.activity_common_recycler);

        mRecycler = (RecyclerView) findViewById(R.id.rvContent);
        mRecycler.setLayoutManager(new GridLayoutManager(mActivity, 2, GridLayoutManager.VERTICAL, false));
        mAdapter = new HomeRecentPostAdapter(mContext, mActivity, mCatWisePostList);
        mRecycler.setAdapter(mAdapter);

        initLoader();
        initToolbar(true);
        enableUpButton();
        setToolbarTitle(Html.fromHtml(mCategoryName).toString());
    }

    private void initFunctionality() {
        loadData();
    }

    public void initListener() {
        // recycler list item click listener
        mAdapter.setItemClickListener(new ListItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                Posts model = mCatWisePostList.get(position);

                switch (view.getId()) {
                    case R.id.btn_fav:
                        if (model.isFavorite()) {
                            mFavoriteDbController.deleteEachFav(mCatWisePostList.get(position).getTitle());
                            mContentList.get(position).setFavorite(false);
                            mCatWisePostList.get(position).setFavorite(false);
                            mAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), getString(R.string.removed_from_fav), Toast.LENGTH_SHORT).show();

                        } else {
                            mFavoriteDbController.insertData(model.getTitle(), model.getImageUrl(), model.getCategory());
                            mContentList.get(position).setFavorite(true);
                            mCatWisePostList.get(position).setFavorite(true);
                            mAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), getString(R.string.added_to_fav), Toast.LENGTH_SHORT).show();

                        }
                        break;
                    case R.id.card_view_top:
                        if (model.getIsNeedPoint()) {
                            if (MyApplication.getInstance().getValueCoin() >= 2) {
                                MyApplication.getInstance().setValueCoin(MyApplication.getInstance().getValueCoin() - 2);
                                ActivityUtilities.getInstance().invokeDetailsActiviy(mActivity, DetailsActivity.class, mCatWisePostList, position, false);
                                // ActivityUtilities.getInstance().invokeDetailsActiviy(mActivity, DetailsActivity.class, mHomeRecentPostList, position, false);
                            }
                            else {
                                Toast.makeText(CategoryWisePostListActivity.this, "You need more coin to using this image!", Toast.LENGTH_LONG).show();
                            }
                        }
                        else {
                            ActivityUtilities.getInstance().invokeDetailsActiviy(mActivity, DetailsActivity.class, mCatWisePostList, position, false);
                        }

                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void loadData() {
        showLoader();
        loadPostsFromFirebase();
    }

    private void initFirebase() {
        FirebaseApp.initializeApp(this);
    }
    private void loadPostsFromFirebase() {
        Retrofit apiClient = APIClient.Companion.getRetrofit();
        if (apiClient != null) {
            APIInterface apiInterface = apiClient.create(APIInterface.class);
            apiInterface.getImages().enqueue(new Callback<WallResponse<Image>>() {
                @Override
                public void onResponse(Call<WallResponse<Image>> call, Response<WallResponse<Image>> response) {
                    Log.d("Thuchs", response.code() + "");
                    if (response.isSuccessful() && response.code() == 200) {
                        WallResponse<Image> wallResponse = response.body();
                        if (wallResponse != null) {
                            for (int i =0 ; i < wallResponse.getData().size(); i++) {
                                mContentList.add(wallResponse.getData().get(i).toPost());
                            }

                            for (int i = 0; i < mContentList.size(); i++) {
                                if (mContentList.get(i).getCategory().equals(mCategoryName)) {
                                    mCatWisePostList.add(mContentList.get(i));
                                }
                            }
                            updateUI();
                        }
                        else {
                            showEmptyView();
                        }
                    }
                    else {
                        showEmptyView();
                    }
                }

                @Override
                public void onFailure(Call<WallResponse<Image>> call, Throwable t) {
                    Log.d("Thuchs", t.toString());
                    showEmptyView();
                }
            });
        }
    }

    private void updateUI() {
        if (mFavoriteDbController == null) {
            mFavoriteDbController = new FavoriteDbController(mContext);
        }
        mFavoriteList.clear();
        mFavoriteList.addAll(mFavoriteDbController.getAllData());

        // Check for favorite
        for (int i = 0; i < mCatWisePostList.size(); i++) {
            boolean isFavorite = false;
            for (int j = 0; j < mFavoriteList.size(); j++) {
                if (mFavoriteList.get(j).getTitle().equals(mCatWisePostList.get(i).getTitle())) {
                    isFavorite = true;
                    break;
                }
            }
            mCatWisePostList.get(i).setFavorite(isFavorite);
        }
        mAdapter.notifyDataSetChanged();
        if (!mCatWisePostList.isEmpty() && mCatWisePostList.size() > 0) {
            hideLoader();
        } else {
            showEmptyView();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mContentList.size() != 0) {
            updateUI();
        }
    }
}
