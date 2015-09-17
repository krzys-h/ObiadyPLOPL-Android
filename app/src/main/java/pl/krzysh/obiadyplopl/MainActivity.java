package pl.krzysh.obiadyplopl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://obiady.plopl.ml";
    private static final String TERRIBLE_HOSTING_PROTECTION_COOKIE = "__test=f4f84bf976b75cae1b35669c44dc31d7"; // TODO: To się naprawdę nie zmienia? :>
    private static final String SESSION_COOKIE_NAME = "PHPSESSID";
    private static final String INJECTED_JAVASCRIPT_ASSET_NAME = "script.js";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        cookieManager.setCookie(BASE_URL, TERRIBLE_HOSTING_PROTECTION_COOKIE);
        String session = loadSession();
        if (session != null)
            cookieManager.setCookie(BASE_URL, SESSION_COOKIE_NAME+"="+session);

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSavePassword(false); // Na nowszych androidach to jest domyślne, nie da się zapisywać haseł
        // TODO: Jak długo sesja trzymana jest na serwerze? Jak za krótko to trzeba dodać autologowanie
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                setProgress(progress * 100); // TODO: to nie działa, nie wiem czemu
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(BASE_URL)) {
                    return false;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            private boolean loading = false;
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loading = true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                String[] cookies = CookieManager.getInstance().getCookie(BASE_URL).split("; ");
                for (String cookie : cookies) {
                    String[] data = cookie.split("=", 2);
                    if (data[0].equals(SESSION_COOKIE_NAME)) {
                        saveSession(data[1]);
                    }
                }

                // Nie wywołujemy JS ponownie przy skokach wewnątrz strony (tych po #)
                if(!loading) return;
                loading = false;

                try {
                    // Uwielbiam wczytywanie plików w Javie :>
                    InputStream is = getAssets().open(INJECTED_JAVASCRIPT_ASSET_NAME);
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);

                    String scriptCode = "";
                    String line;
                    while ((line = br.readLine()) != null) {
                        scriptCode += line;
                    }
                    is.close();

                    view.loadUrl("javascript:(function() {" + scriptCode + "})()");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        webView.loadUrl(BASE_URL);
    }

    private void saveSession(String session) {
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putString("session", session);
        settingsEditor.commit();
    }

    private String loadSession() {
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        String session = settings.getString("session", null);
        return session;
    }
}
