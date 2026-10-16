package com.mahaam.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make window background clean
        getWindow().getDecorView().setBackgroundColor(0xFFFAFAF8);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Hardware acceleration
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        // Load the offline application bundle
        webView.loadUrl("file:///android_asset/www/index.html");
    }

    @Override
    public void onBackPressed() {
        if (webView != null) {
            // Close any open modals via JS before exiting
            webView.evaluateJavascript(
                "(function() {" +
                "  if (typeof app !== 'undefined') {" +
                "    const editModal = document.getElementById('modal-overlay');" +
                "    const entryModal = document.getElementById('habit-entry-modal-overlay');" +
                "    const habitEditModal = document.getElementById('habit-edit-modal-overlay');" +
                "    if (editModal && editModal.style.display !== 'none') { app.closeEditModal(); return true; }" +
                "    if (entryModal && entryModal.style.display !== 'none') { app.closeLogEntryModal(); return true; }" +
                "    if (habitEditModal && habitEditModal.style.display !== 'none') { app.closeEditHabitModal(); return true; }" +
                "    if (app.currentView !== 'dashboard') { app.switchView('dashboard'); return true; }" +
                "  }" +
                "  return false;" +
                "})()",
                result -> {
                    if ("false".equals(result) || result == null) {
                        super.onBackPressed();
                    }
                }
            );
        } else {
            super.onBackPressed();
        }
    }
}
