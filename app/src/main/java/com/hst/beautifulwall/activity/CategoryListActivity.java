package com.hst.beautifulwall.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hst.beautifulwall.R;
import com.hst.beautifulwall.adapters.CategoryAdapter;
import com.hst.beautifulwall.data.constant.AppConstant;
import com.hst.beautifulwall.io.APIClient;
import com.hst.beautifulwall.io.APIInterface;
import com.hst.beautifulwall.io.model.Category;
import com.hst.beautifulwall.io.model.WallResponse;
import com.hst.beautifulwall.listeners.ListItemClickListener;
import com.hst.beautifulwall.models.content.Categories;
import com.hst.beautifulwall.utility.ActivityUtilities;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CategoryListActivity extends BaseActivity {

    private Activity mActivity;
    private Context mContext;

    private ArrayList<Categories> mCategoryList;
    private CategoryAdapter mAdapter = null;
    private RecyclerView mRecycler;

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
        mActivity = CategoryListActivity.this;
        mContext = mActivity.getApplicationContext();

        mCategoryList = new ArrayList<>();
    }

    private void initView() {
        setContentView(R.layout.activity_common_recycler);

        mRecycler = (RecyclerView) findViewById(R.id.rvContent);
        mRecycler.setLayoutManager(new GridLayoutManager(mActivity, 3, GridLayoutManager.VERTICAL, false));
        mAdapter = new CategoryAdapter(mContext, mActivity, mCategoryList);
        mRecycler.setAdapter(mAdapter);

        initLoader();
        initToolbar(true);
        enableUpButton();
        setToolbarTitle(getString(R.string.category));
    }

    private void initFunctionality() {
        loadData();
    }

    public void initListener() {
        // recycler list item click listener
        mAdapter.setItemClickListener(new ListItemClickListener() {
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
    }

    private void loadData() {
        showLoader();
        loadCategoriesFromFirebase();
    }

    private void initFirebase() {
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
    }

    private void loadCategoriesFromFirebase() {
        Retrofit apiClient = APIClient.Companion.getRetrofit();
        if (apiClient != null) {
            APIInterface apiInterface = apiClient.create(APIInterface.class);
            apiInterface.getCategories().enqueue(new Callback<WallResponse<Category>>() {
                @Override
                public void onResponse(Call<WallResponse<Category>> call, Response<WallResponse<Category>> response) {
                    if (response.isSuccessful() && response.code() == 200) {
                        WallResponse<Category> wallResponse = response.body();
                        if (wallResponse != null && wallResponse.getSuccess()) {
                            for (int i=0; i < wallResponse.getData().size() ; i++) {
                                mCategoryList.add(wallResponse.getData().get(i).toCategory());
                            }
                            hideLoader();
                            mAdapter.notifyDataSetChanged();
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
                public void onFailure(Call<WallResponse<Category>> call, Throwable t) {
                    showEmptyView();
                }
            });
        }

    }

    @Override
    public void onResume() {
        super.onResume();
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
}
