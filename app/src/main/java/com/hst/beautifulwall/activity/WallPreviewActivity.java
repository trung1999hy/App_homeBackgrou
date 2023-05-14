package com.hst.beautifulwall.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.hst.beautifulwall.R;
import com.hst.beautifulwall.app.MyApplication;
import com.hst.beautifulwall.data.constant.AppConstant;
import com.hst.beautifulwall.utility.ActivityUtilities;
import com.hst.beautifulwall.utility.DialogUtilities;

public class WallPreviewActivity extends BaseActivity {

    private Context mContext;
    private Activity mActivity;
    private PhotoView mFullImage;
    private ImageButton mBtnExit, mBtnSetWall;
    private String mImgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVar();
        initView();
        initFunctionality();
        initListener();
    }

    private void initVar() {
        mActivity = WallPreviewActivity.this;
        mContext = mActivity.getApplicationContext();

        Intent intent = getIntent();
        if (intent != null) {
            mImgUrl = intent.getStringExtra(AppConstant.BUNDLE_KEY_URL);
        }
    }

    private void initView() {
        setContentView(R.layout.activity_wall_preview);

        mFullImage = findViewById(R.id.full_image);
        mBtnExit = findViewById(R.id.btn_exit);
        mBtnSetWall = findViewById(R.id.btn_set_image);
    }

    private void initFunctionality() {
        fullScreen();
        if (mImgUrl != null && !mImgUrl.isEmpty()) {
            Glide.with(mContext)
                    .load(mImgUrl)
                    .into(mFullImage);
        }
    }

    private void initListener() {
        mBtnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtnSetWall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getSupportFragmentManager();
                DialogUtilities dialog = DialogUtilities.newInstance(getString(R.string.information), getString(R.string.information_detail), getString(R.string.yes), getString(R.string.no), AppConstant.BUNDLE_KEY_YES);
                dialog.show(manager, AppConstant.BUNDLE_KEY_DIALOG_FRAGMENT);
                //ActivityUtilities.getInstance().invokeWallPreviewNCropSetActiviy(mActivity, WallCropNSetActivity.class, mImgUrl, true);
            }
        });
    }

    @Override
    public void onComplete(Boolean isOkPressed, String viewIdText) {
        if (viewIdText.equals(AppConstant.BUNDLE_KEY_YES)) {
            if (MyApplication.getInstance().getValueCoin() >= 2) {
                MyApplication.getInstance().setValueCoin(MyApplication.getInstance().getValueCoin() - 2);
                ActivityUtilities.getInstance().invokeWallPreviewNCropSetActiviy(mActivity, WallCropNSetActivity.class, mImgUrl, true);
            } else {
                Toast.makeText(this, "You need point to use this image!", Toast.LENGTH_LONG).show();
            }
        }
    }

    void fullScreen() {
        // BEGIN_INCLUDE (get_current_ui_flags)
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;

        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled = ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        // Immersive mode: Backward compatible to KitKat.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}