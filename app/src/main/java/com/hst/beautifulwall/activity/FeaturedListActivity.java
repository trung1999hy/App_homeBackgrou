package com.hst.beautifulwall.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.hst.beautifulwall.adapters.SearchOrFeaturedAdapter;
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

public class FeaturedListActivity extends BaseActivity {

    private Activity mActivity;
    private Context mContext;

    private ArrayList<Posts> mContentList;
    private ArrayList<Posts> mFeaturedList;
    private SearchOrFeaturedAdapter mAdapter = null;
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
        mActivity = FeaturedListActivity.this;
        mContext = mActivity.getApplicationContext();

        mContentList = new ArrayList<>();
        mFavoriteList = new ArrayList<>();
        mFeaturedList = new ArrayList<>();
    }

    private void initView() {
        setContentView(R.layout.activity_common_recycler);

        mRecycler = (RecyclerView) findViewById(R.id.rvContent);
        mRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new SearchOrFeaturedAdapter(mContext, mActivity, mFeaturedList);
        mRecycler.setAdapter(mAdapter);

        initLoader();
        initToolbar(true);
        enableUpButton();
        setToolbarTitle(getString(R.string.featured));
    }

    private void initFunctionality() {
        loadData();
    }

    public void initListener() {
        // recycler list item click listener
        mAdapter.setItemClickListener(new ListItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                Posts model = mFeaturedList.get(position);

                switch (view.getId()) {
                    case R.id.btn_fav:
                        if (model.isFavorite()) {
                            mFavoriteDbController.deleteEachFav(mFeaturedList.get(position).getTitle());
                            mContentList.get(position).setFavorite(false);
                            mFeaturedList.get(position).setFavorite(false);
                            mAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), getString(R.string.removed_from_fav), Toast.LENGTH_SHORT).show();
                        } else {
                            mFavoriteDbController.insertData(model.getTitle(), model.getImageUrl(), model.getCategory());
                            mContentList.get(position).setFavorite(true);
                            mFeaturedList.get(position).setFavorite(true);
                            mAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), getString(R.string.added_to_fav), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.card_view_top:
                        ActivityUtilities.getInstance().invokeDetailsActiviy(mActivity, DetailsActivity.class, mFeaturedList, position, false);
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

                            for (int i = 0; i < mContentList.size(); i++) {
                                if (mContentList.get(i).getIsFeatured().equals(AppConstant.JSON_KEY_YES)) {
                                    mFeaturedList.add(mContentList.get(i));
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
        for (int i = 0; i < mFeaturedList.size(); i++) {
            boolean isFavorite = false;
            for (int j = 0; j < mFavoriteList.size(); j++) {
                if (mFavoriteList.get(j).getTitle().equals(mFeaturedList.get(i).getTitle())) {
                    isFavorite = true;
                    break;
                }
            }
            mFeaturedList.get(i).setFavorite(isFavorite);
        }
        mAdapter.notifyDataSetChanged();
        if (!mFeaturedList.isEmpty() && mFeaturedList.size() > 0) {
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
