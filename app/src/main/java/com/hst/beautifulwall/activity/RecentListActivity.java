package com.hst.beautifulwall.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hst.beautifulwall.R;
import com.hst.beautifulwall.adapters.HomeRecentPostAdapter;
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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RecentListActivity extends BaseActivity {

    private Activity mActivity;
    private Context mContext;

    private ArrayList<Posts> mContentList;
    private HomeRecentPostAdapter mAdapter = null;
    private RecyclerView mRecycler;

    // Favourites view
    private List<FavoriteModel> mFavoriteList;
    private FavoriteDbController mFavoriteDbController;

    // Firebase Database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;


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
        mActivity = RecentListActivity.this;
        mContext = mActivity.getApplicationContext();

        mContentList = new ArrayList<>();
        mFavoriteList = new ArrayList<>();
    }

    private void initView() {
        setContentView(R.layout.activity_common_recycler);

        mRecycler = (RecyclerView) findViewById(R.id.rvContent);
        mRecycler.setLayoutManager(new GridLayoutManager(mActivity, 2, GridLayoutManager.VERTICAL, false));
        mAdapter = new HomeRecentPostAdapter(mContext, mActivity, mContentList);
        mRecycler.setAdapter(mAdapter);

        initLoader();
        initToolbar(true);
        enableUpButton();
        setToolbarTitle(getString(R.string.recent));
    }

    private void initFunctionality() {
        loadData();

    }

    public void initListener() {
        // recycler list item click listener
        mAdapter.setItemClickListener(new ListItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                Posts model = mContentList.get(position);

                switch (view.getId()) {
                    case R.id.btn_fav:
                        if (model.isFavorite()) {
                            mFavoriteDbController.deleteEachFav(mContentList.get(position).getTitle());
                            mContentList.get(position).setFavorite(false);
                            mAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), getString(R.string.removed_from_fav), Toast.LENGTH_SHORT).show();

                        } else {
                            mFavoriteDbController.insertData(model.getTitle(), model.getImageUrl(), model.getCategory());
                            mContentList.get(position).setFavorite(true);
                            mAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), getString(R.string.added_to_fav), Toast.LENGTH_SHORT).show();

                        }
                        break;
                    case R.id.card_view_top:
                        if (model.getIsNeedPoint()) {
                            if (MyApplication.getInstance().getValueCoin() >= 2 && model.getIsNeedPoint()) {
                                MyApplication.getInstance().setValueCoin(MyApplication.getInstance().getValueCoin() - 2);
                                ActivityUtilities.getInstance().invokeDetailsActiviy(mActivity, DetailsActivity.class, mContentList, position, false);
                            }
                            else {
                                Toast.makeText(RecentListActivity.this, "You need more coin to using this image!", Toast.LENGTH_LONG).show();
                            }
                        }
                        else {
                            ActivityUtilities.getInstance().invokeDetailsActiviy(mActivity, DetailsActivity.class, mContentList, position, false);

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
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
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
        for (int i = 0; i < mContentList.size(); i++) {
            boolean isFavorite = false;
            for (int j = 0; j < mFavoriteList.size(); j++) {
                if (mFavoriteList.get(j).getTitle().equals(mContentList.get(i).getTitle())) {
                    isFavorite = true;
                    break;
                }
            }
            mContentList.get(i).setFavorite(isFavorite);
        }
        mAdapter.notifyDataSetChanged();
        if (!mContentList.isEmpty() && mContentList.size() > 0) {
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
