package com.hst.beautifulwall.utility;

import android.app.Activity;
import android.content.Intent;

import com.hst.beautifulwall.data.constant.AppConstant;
import com.hst.beautifulwall.models.content.Posts;

import java.util.ArrayList;

public class ActivityUtilities {

    private static ActivityUtilities sActivityUtilities = null;

    public static ActivityUtilities getInstance() {
        if (sActivityUtilities == null) {
            sActivityUtilities = new ActivityUtilities();
        }
        return sActivityUtilities;
    }

    public void invokeNewActivity(Activity activity, Class<?> tClass, boolean shouldFinish) {
        Intent intent = new Intent(activity, tClass);
        activity.startActivity(intent);
        if (shouldFinish) {
            activity.finish();
        }
    }

    public void invokeCustomUrlActivity(Activity activity, Class<?> tClass, String pageTitle, String pageUrl, boolean shouldFinish) {
        Intent intent = new Intent(activity, tClass);
        intent.putExtra(AppConstant.BUNDLE_KEY_TITLE, pageTitle);
        intent.putExtra(AppConstant.BUNDLE_KEY_URL, pageUrl);
        activity.startActivity(intent);
        if (shouldFinish) {
            activity.finish();
        }
    }

    public void invokeCatWisePostListActiviy(Activity activity, Class<?> tClass, String categoryName, boolean shouldFinish) {
        Intent intent = new Intent(activity, tClass);
        intent.putExtra(AppConstant.BUNDLE_KEY_TITLE, categoryName);
        activity.startActivity(intent);
        if (shouldFinish) {
            activity.finish();
        }
    }

    public void invokeDetailsActiviy(Activity activity, Class<?> tClass, ArrayList<Posts> model, int clickedPosition, boolean shouldFinish) {
        Intent intent = new Intent(activity, tClass);
        intent.putParcelableArrayListExtra(AppConstant.BUNDLE_KEY_ITEM, model);
        intent.putExtra(AppConstant.BUNDLE_KEY_INDEX, clickedPosition);
        activity.startActivity(intent);
        if (shouldFinish) {
            activity.finish();
        }
    }

    public void invokeWallPreviewNCropSetActiviy(Activity activity, Class<?> tClass, String imgUrl, boolean shouldFinish) {
        Intent intent = new Intent(activity, tClass);
        intent.putExtra(AppConstant.BUNDLE_KEY_URL, imgUrl);
        activity.startActivity(intent);
        if (shouldFinish) {
            activity.finish();
        }
    }

}
