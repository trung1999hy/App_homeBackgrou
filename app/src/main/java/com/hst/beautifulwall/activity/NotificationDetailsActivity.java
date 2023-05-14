package com.hst.beautifulwall.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hst.beautifulwall.R;
import com.hst.beautifulwall.data.constant.AppConstant;

public class NotificationDetailsActivity extends BaseActivity {

    private Context mContext;
    private Activity mActivity;

    private TextView mTitleView, mMessageView;
    private Button mLinkButton;
    private String mTitle, mMessage, mUrl;
    private boolean mFromPush = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = NotificationDetailsActivity.this;
        mContext = mActivity.getApplicationContext();

        initVar();
        initView();
        initFunctionality();
        initListener();
    }

    private void initVar() {
        Bundle extras = getIntent().getExtras();
        mTitle = extras.getString(AppConstant.BUNDLE_KEY_TITLE);
        mMessage = extras.getString(AppConstant.BUNDLE_KEY_MESSAGE);
        mUrl = extras.getString(AppConstant.BUNDLE_KEY_URL);
        mFromPush = extras.getBoolean(AppConstant.BUNDLE_FROM_PUSH, false);
    }

    private void initView() {
        setContentView(R.layout.activity_notification_details);

        mTitleView = (TextView) findViewById(R.id.title);
        mMessageView = (TextView) findViewById(R.id.message);
        mLinkButton = (Button) findViewById(R.id.btn_read);

        initToolbar(true);
        setToolbarTitle(getString(R.string.notifications));
        enableUpButton();
    }


    private void initFunctionality() {

        mTitleView.setText(mTitle);
        mMessageView.setText(mMessage);

        if (mUrl != null && !mUrl.isEmpty()) {
            mLinkButton.setEnabled(true);
        } else {
            mLinkButton.setEnabled(false);
        }

    }

    private void initListener() {
        mLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ActivityUtilities.getInstance().invokeCustomUrlActivity(mActivity, CustomUrlActivity.class, mTitle, mUrl, false);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
                mActivity.startActivity(intent);
            }
        });
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
            Intent intent = new Intent(NotificationDetailsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }
}
