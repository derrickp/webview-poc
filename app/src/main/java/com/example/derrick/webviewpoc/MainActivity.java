package com.example.derrick.webviewpoc;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebMessage;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private WebApplicationInterface mWebApplicationInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebView.setWebContentsDebuggingEnabled(true);
        mWebView = findViewById(R.id.activity_main_webview);
        mWebView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebApplicationInterface = new WebApplicationInterface(mWebView.getContext());
        mWebView.addJavascriptInterface(mWebApplicationInterface, "AppInterface");
        mWebView.loadUrl("file:///android_asset/index.html");
//        Uri uri = Uri.parse("file:///android_asset/index.html");
//        WebMessage message =
//        mWebView.postWebMessage("", uri);

        mWebView.loadUrl("javascript:alert(\"test\")");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == mWebApplicationInterface.bluetoothRequestCode) {
            mWebApplicationInterface.findBTComplete();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
