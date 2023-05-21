package com.hst.beautifulwall.webengine;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.hst.beautifulwall.R;
import com.hst.beautifulwall.data.constant.AppConstant;
import com.hst.beautifulwall.data.preference.AppPreference;
import com.hst.beautifulwall.listeners.WebListener;
import com.hst.beautifulwall.utility.FilePickerUtilities;
import com.hst.beautifulwall.utility.PermissionUtilities;

import java.io.File;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static android.content.Context.DOWNLOAD_SERVICE;

public class WebEngine {

    private WebView webView;
    private Activity mActivity;
    private Context mContext;
    private Fragment mFragment;

    public static final int KEY_FILE_PICKER = 554;
    private static final String GOOGLE_DOCS_VIEWER = "https://docs.google.com/viewerng/viewer?url=";

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;

    private WebListener mWebListener;
    private String mDownloadUrl;
    private VideoViewer mVideoViewer;
    private WebChromeClient.CustomViewCallback mVideoViewCallback;

    public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101;
    private PermissionRequest myRequest;

    public WebEngine(WebView webView, Activity activity) {
        this.webView = webView;
        this.mActivity = activity;
        this.mContext = mActivity.getApplicationContext();
        mVideoViewer = VideoViewer.getInstance();
    }

    public void initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
       // webView.getSettings().setAppCacheMaxSize(AppConstant.SITE_CACHE_SIZE);
        //webView.getSettings().setAppCachePath(mContext.getCacheDir().getAbsolutePath());
        webView.getSettings().setAllowFileAccess(true);
       // webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        if (!isNetworkAvailable(mContext)) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        if (AppPreference.getInstance(mContext).getTextSize().equals(mContext.getResources().getString(R.string.small_text))) {
            webView.getSettings().setTextSize(WebSettings.TextSize.SMALLER);
        } else if (AppPreference.getInstance(mContext).getTextSize().equals(mContext.getResources().getString(R.string.default_text))) {
            webView.getSettings().setTextSize(WebSettings.TextSize.NORMAL);
        } else if (AppPreference.getInstance(mContext).getTextSize().equals(mContext.getResources().getString(R.string.large_text))) {
            webView.getSettings().setTextSize(WebSettings.TextSize.LARGER);
        }
    }

    public void initListeners(final WebListener webListener) {

        this.mWebListener = webListener;

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                webListener.onProgress(newProgress);
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, FileChooserParams fileChooserParams) {

                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePath;
                invokeImagePickerActivity();
                return true;
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                webListener.onPageTitle(webView.getTitle());
            }


            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                mVideoViewCallback = callback;
                mVideoViewer.show(mActivity);
                mVideoViewer.setVideoLayout(view);
            }

            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
                mVideoViewer.dismiss();
                mVideoViewCallback.onCustomViewHidden();
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                myRequest = request;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    for (String permission : request.getResources()) {
                        switch (permission) {
                            case "android.webkit.resource.AUDIO_CAPTURE": {
                                askForPermission(request.getOrigin().toString(), Manifest.permission.RECORD_AUDIO, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                                break;
                            }
                        }
                    }
                }
            }


        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String webUrl) {

                loadPage(webUrl);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                webListener.onStart();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                webListener.onLoaded();
            }

        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                try {
                    if (PermissionUtilities.isPermissionGranted(mActivity, PermissionUtilities.SD_WRITE_PERMISSIONS, PermissionUtilities.REQUEST_WRITE_STORAGE_DOWNLOAD)) {
                        String string = String.valueOf((URLUtil.guessFileName(url, contentDisposition, mimetype)));

                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setMimeType(mimetype);
                        String cookies = CookieManager.getInstance().getCookie(url);

                        String description = mActivity.getResources().getString(R.string.downloading);

                        request.addRequestHeader("cookie", cookies);
                        request.addRequestHeader("User-Agent", userAgent);
                        request.allowScanningByMediaScanner();
                        request.setDescription(description);
                        request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
                        request.allowScanningByMediaScanner();

                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, string);

                        DownloadManager dm = (DownloadManager) mActivity.getSystemService(DOWNLOAD_SERVICE);
                        dm.enqueue(request);

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    String string = mActivity.getResources().getString(R.string.went_wrong);
                    Toast.makeText(mContext, string, Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    public void askForPermission(String origin, String permission, int requestCode) {

        if (ContextCompat.checkSelfPermission(mActivity.getApplicationContext(),
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                    permission)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{permission},
                        requestCode);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                myRequest.grant(myRequest.getResources());
            }
        }
    }

    public void loadPage(String webUrl) {
        if (isNetworkAvailable(mContext)) {

            if (webUrl.startsWith("tel:") || webUrl.startsWith("sms:") || webUrl.startsWith("smsto:")
                    || webUrl.startsWith("mms:") || webUrl.startsWith("mmsto:")
                    || webUrl.startsWith("mailto:") /*|| webUrl.contains("youtube.com")*/
                    || webUrl.contains("geo:")) {
                invokeNativeApp(webUrl);
            } else if (webUrl.contains("?target=blank")) {
                invokeNativeApp(webUrl.replace("?target=blank", ""));
            } else if (webUrl.endsWith(".doc") || webUrl.endsWith(".docx") || webUrl.endsWith(".xls")
                    || webUrl.endsWith(".xlsx") || webUrl.endsWith(".pptx") || webUrl.endsWith(".pdf")) {
                webView.loadUrl(GOOGLE_DOCS_VIEWER + webUrl);
                webView.getSettings().setBuiltInZoomControls(true);
            } else if (webUrl.contains("whatsapp://")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
                intent.setPackage("com.whatsapp");
                mActivity.startActivity(intent);
            } else if (webUrl.contains("https://maps") || webUrl.contains("https://www.google.com/maps")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
                intent.setPackage("com.google.android.apps.maps");
                mActivity.startActivity(intent);
            } else {
                webView.loadUrl(webUrl);
            }

        } else {
            mWebListener.onNetworkError();
        }
    }

    public void loadHtml(String htmlString) {
        if (htmlString.startsWith("tel:") || htmlString.startsWith("sms:") || htmlString.startsWith("smsto:")
                || htmlString.startsWith("mms:") || htmlString.startsWith("mmsto:")
                || htmlString.startsWith("mailto:") /*|| htmlString.contains("youtube.com")*/
                || htmlString.contains("geo:")) {
            invokeNativeApp(htmlString);
        } else if (htmlString.contains("?target=blank")) {
            invokeNativeApp(htmlString.replace("?target=blank", ""));
        } else if (htmlString.endsWith(".doc") || htmlString.endsWith(".docx") || htmlString.endsWith(".xls")
                || htmlString.endsWith(".xlsx") || htmlString.endsWith(".pptx") || htmlString.endsWith(".pdf")) {
            webView.loadUrl(GOOGLE_DOCS_VIEWER + htmlString);
            webView.getSettings().setBuiltInZoomControls(true);
        } else {
            // load data in LTR mode
            webView.loadDataWithBaseURL(null, htmlString, "text/html; charset=utf-8", "UTF-8", null);

            // load data in RTL mode
            // webView.loadDataWithBaseURL(null, "<html dir=\"rtl\" lang=\"\"><body>" + htmlString + "</body></html>", "text/html; charset=utf-8", "UTF-8", null);
        }
    }

    public void reloadPage() {
        webView.reload();
    }

    public boolean hasHistory() {
        return webView.canGoBack();
    }

    public void loadPreviousPage() {
        webView.goBack();
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void invokeNativeApp(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mActivity.startActivity(intent);
    }

    public void invokeImagePickerActivity() {
        if (PermissionUtilities.isPermissionGranted(mActivity, PermissionUtilities.SD_WRITE_PERMISSIONS, PermissionUtilities.REQUEST_WRITE_STORAGE_UPLOAD)) {
            Intent chooseImageIntent = FilePickerUtilities.getPickFileIntent(mActivity);
            if (mFragment == null) {
                mActivity.startActivityForResult(chooseImageIntent, KEY_FILE_PICKER);
            } else {
                mFragment.startActivityForResult(chooseImageIntent, KEY_FILE_PICKER);
            }
        }
    }

    public void uploadFile(Intent data, String filePath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Uri[] results = null;
            if (filePath != null) {
                results = new Uri[]{Uri.fromFile(new File(filePath))};
            }

            if (results == null) {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }


            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(results);
                mFilePathCallback = null;
            }
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            Uri result = data == null ? Uri.fromFile(new File(filePath)) : data.getData();
            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }

        }
    }

    public void cancelUpload() {
        if (mFilePathCallback != null) {
            mFilePathCallback.onReceiveValue(null);
        }
        mFilePathCallback = null;
    }

    private void dismissCustomView() {
        if (mVideoViewer != null) {
            mVideoViewer.dismiss();
        }

        if (mVideoViewCallback != null) {
            mVideoViewCallback.onCustomViewHidden();
        }
    }

    public void onPause() {
        if (webView != null) {
            webView.onPause();
        }
        dismissCustomView();
    }

    public void onResume() {
        if (webView != null) {
            webView.onResume();
        }
    }

}

