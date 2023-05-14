package com.hst.beautifulwall.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import com.hst.beautifulwall.R;
import com.hst.beautifulwall.data.constant.AppConstant;
import com.hst.beautifulwall.listeners.WebListener;
import com.hst.beautifulwall.utility.AppUtilities;
import com.hst.beautifulwall.utility.FilePickerUtilities;
import com.hst.beautifulwall.utility.PermissionUtilities;
import com.hst.beautifulwall.webengine.WebEngine;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class CustomUrlActivity extends BaseActivity {

    private Activity mActivity;
    private Context mContext;
    private String mPageTitle, mPageUrl;

    private WebView mWebView;
    private WebEngine mWebEngine;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean mFromPush = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVar();
        initView();
        initFunctionality();
        initListener();
    }

    private void initVar() {
        mActivity = CustomUrlActivity.this;
        mContext = mActivity.getApplicationContext();

        Intent intent = getIntent();
        if (intent != null) {
            mPageTitle = intent.getStringExtra(AppConstant.BUNDLE_KEY_TITLE);
            mPageUrl = intent.getStringExtra(AppConstant.BUNDLE_KEY_URL);
            mFromPush = intent.getBooleanExtra(AppConstant.BUNDLE_FROM_PUSH, false);
        }
    }

    private void initView() {
        setContentView(R.layout.activity_custom_url);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        initWebEngine();

        initLoader();
        initToolbar(true);
        setToolbarTitle(mPageTitle);
        enableUpButton();
    }


    public void initWebEngine() {

        mWebView = (WebView) findViewById(R.id.web_view);

        mWebEngine = new WebEngine(mWebView, mActivity);
        mWebEngine.initWebView();


        mWebEngine.initListeners(new WebListener() {
            @Override
            public void onStart() {
                showLoader();
            }

            @Override
            public void onLoaded() {
                hideLoader();
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onProgress(int progress) {
                hideLoader();
            }

            @Override
            public void onNetworkError() {
                showEmptyView();
            }

            @Override
            public void onPageTitle(String title) {
            }
        });
    }

    private void initFunctionality() {

        mWebEngine.loadPage(mPageUrl);
    }

    public void initListener() {
        //swipe refresh layout listener
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadPage();
            }
        });
    }

    public void reloadPage() {
        mWebEngine.reloadPage();
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (PermissionUtilities.isPermissionResultGranted(grantResults)) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mWebEngine.reloadPage();
                }
            } else {
                AppUtilities.showToast(mActivity, getString(R.string.permission_not_granted));
            }
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (reqCode == WebEngine.KEY_FILE_PICKER) {
                String picturePath = FilePickerUtilities.getPickedFilePath(this, data);
                if (mWebEngine != null) {
                    mWebEngine.uploadFile(data, picturePath);
                } else {
                    AppUtilities.showToast(mContext, getString(R.string.failed));
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (mWebEngine != null) {
                mWebEngine.cancelUpload();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goToHome();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        goToHome();
    }

    private void goToHome() {
        if (mFromPush) {
            Intent intent = new Intent(CustomUrlActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }
}
