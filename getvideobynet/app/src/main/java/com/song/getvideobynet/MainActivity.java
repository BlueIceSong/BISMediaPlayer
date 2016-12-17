package com.song.getvideobynet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {
    private InJavaScriptLocalObj inJavaScriptLocalObj;
    private WebView mWebView;
    private static final int GET_REALPLAYURL_COMPLETE = 0;
    private static boolean GETHTML5_COMPLETE = false;
    private String realPlayUrl;
    private String playUriTemp = "";

    Handler mEventHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GET_REALPLAYURL_COMPLETE:
                    Log.v("URI","  "+realPlayUrl);
            }
        }
    };

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inJavaScriptLocalObj = new InJavaScriptLocalObj();
        mWebView = (WebView) findViewById(R.id.webview);
        initWebView(mWebView);
        getUriFromHtml();
        inJavaScriptLocalObj.showSource(playUriTemp);
    }

    @SuppressLint("JavascriptInterface")
    private void getUriFromHtml(){
        initWebView(mWebView);
        mWebView.getSettings()
                .setUserAgentString(
                        "Mozilla/5.0 (iPhone; CPU iPhone OS 7_1 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Version/7.0 Mobile/11D5145e Safari/9537.53");
        mWebView.addJavascriptInterface(new InJavaScriptLocalObj(), "js_method");//添加java script接口
        mWebView.loadUrl(playUriTemp);
    }

    private void initWebView(WebView webView) {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        // 开启应用程序缓存
        webSettings.setAppCacheEnabled(true);
        String appCacheDir = this.getApplicationContext()
                .getDir("cache", Context.MODE_PRIVATE).getPath();
        webSettings.setAppCachePath(appCacheDir);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheMaxSize(1024 * 1024 * 10);// 设置缓冲大小，我设的是10M
        webSettings.setAllowFileAccess(true);

        mWebView.setWebViewClient(mWebViewClient);
    }

    private WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public void onPageFinished(WebView view, String url) {
            view.loadUrl("javascript:window.js_method.showSource(document.getElementsByTagName('video')[0].src);"); // iqiyi
            // view.loadUrl("javascript:window.js_method.showSource('<head>'+" +
            // "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            super.onPageFinished(view, url);
        }
    };

    class InJavaScriptLocalObj {
        public void showSource(String html5url) {
            if (html5url != null && !GETHTML5_COMPLETE) {
                GETHTML5_COMPLETE = true;
                realPlayUrl = html5url;
                mEventHandler.sendEmptyMessage(GET_REALPLAYURL_COMPLETE);
            }
            Log.i("conowen", "html5url=" + html5url);
        }
    }

}
