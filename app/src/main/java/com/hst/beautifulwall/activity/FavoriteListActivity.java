package com.hst.beautifulwall.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.hst.beautifulwall.R;
import com.hst.beautifulwall.adapters.FavoriteAdapter;
import com.hst.beautifulwall.data.constant.AppConstant;
import com.hst.beautifulwall.data.sqlite.FavoriteDbController;
import com.hst.beautifulwall.listeners.ListItemClickListener;
import com.hst.beautifulwall.models.content.Posts;
import com.hst.beautifulwall.models.favorite.FavoriteModel;
import com.hst.beautifulwall.utility.ActivityUtilities;
import com.hst.beautifulwall.utility.DialogUtilities;

import java.util.ArrayList;

public class FavoriteListActivity extends BaseActivity {

    private Activity mActivity;
    private Context mContext;

    private ArrayList<Posts> mPostList;
    private ArrayList<FavoriteModel> mFavouriteList;
    private FavoriteAdapter mFavoriteAdapter = null;
    private RecyclerView mRecycler;

    private FavoriteDbController mFavoriteDbController;
    private MenuItem mMenuItemDeleteAll;
    private int mAdapterPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVar();
        initView();
        initFunctionality();
        initListener();
    }

    private void initVar() {
        mActivity = FavoriteListActivity.this;
        mContext = mActivity.getApplicationContext();

        mPostList = new ArrayList<>();
        mFavouriteList = new ArrayList<>();
    }

    private void initView() {
        setContentView(R.layout.activity_common_recycler);

        mRecycler = (RecyclerView) findViewById(R.id.rvContent);
        mRecycler.setLayoutManager(new GridLayoutManager(mActivity, 2, GridLayoutManager.VERTICAL, false));
        mFavoriteAdapter = new FavoriteAdapter(mContext, mActivity, mFavouriteList);
        mRecycler.setAdapter(mFavoriteAdapter);

        initToolbar(true);
        setToolbarTitle(getString(R.string.site_menu_fav));
        enableUpButton();
        initLoader();
    }

    private void initFunctionality() {
    }

    public void updateUI() {
        showLoader();

        if (mFavoriteDbController == null) {
            mFavoriteDbController = new FavoriteDbController(mContext);
        }
        mFavouriteList.clear();
        mFavouriteList.addAll(mFavoriteDbController.getAllData());

        mPostList.clear();
        for (int i = 0; i < mFavouriteList.size(); i++) {
            FavoriteModel model = mFavouriteList.get(i);
            Posts post = new Posts(model.getTitle(), model.getPostCategory(), AppConstant.EMPTY_STRING, model.getImageUrl(), true);
            mPostList.add(post);
        }

        mFavoriteAdapter.notifyDataSetChanged();

        hideLoader();

        if (mFavouriteList.size() == 0) {
            showEmptyView();
            if (mMenuItemDeleteAll != null) {
                mMenuItemDeleteAll.setVisible(false);
            }
        } else {
            if (mMenuItemDeleteAll != null) {
                mMenuItemDeleteAll.setVisible(true);
            }
        }
    }

    public void initListener() {
        // recycler list item click listener
        mFavoriteAdapter.setItemClickListener(new ListItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                mAdapterPosition = position;
                FavoriteModel favoriteModel = mFavouriteList.get(position);

                switch (view.getId()) {
                    case R.id.btn_fav:
                        FragmentManager manager = getSupportFragmentManager();
                        DialogUtilities dialog = DialogUtilities.newInstance(getString(R.string.site_menu_fav), getString(R.string.delete_fav_item), getString(R.string.yes), getString(R.string.no), AppConstant.BUNDLE_KEY_DELETE_EACH_FAV);
                        dialog.show(manager, AppConstant.BUNDLE_KEY_DIALOG_FRAGMENT);
                        break;
                    case R.id.card_view_top:
                        ActivityUtilities.getInstance().invokeDetailsActiviy(mActivity, DetailsActivity.class, mPostList, position, false);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menus_delete_all:
                FragmentManager manager = getSupportFragmentManager();
                DialogUtilities dialog = DialogUtilities.newInstance(getString(R.string.site_menu_fav), getString(R.string.delete_all_fav_item), getString(R.string.yes), getString(R.string.no), AppConstant.BUNDLE_KEY_DELETE_ALL_FAV);
                dialog.show(manager, AppConstant.BUNDLE_KEY_DIALOG_FRAGMENT);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_delete_all, menu);
        mMenuItemDeleteAll = menu.findItem(R.id.menus_delete_all);

        updateUI();

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFavoriteAdapter != null) {
            updateUI();
        }
    }

    @Override
    public void onComplete(Boolean isOkPressed, String viewIdText) {
        if (isOkPressed) {
            if (viewIdText.equals(AppConstant.BUNDLE_KEY_DELETE_ALL_FAV)) {
                mFavoriteDbController.deleteAllFav();
                updateUI();
            } else if (viewIdText.equals(AppConstant.BUNDLE_KEY_DELETE_EACH_FAV)) {
                mFavoriteDbController.deleteEachFav(mFavouriteList.get(mAdapterPosition).getTitle());
                updateUI();
            }
        }
    }
}
