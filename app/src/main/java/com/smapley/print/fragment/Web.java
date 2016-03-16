package com.smapley.print.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.smapley.print.R;
import com.smapley.print.util.MyData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.xutils.common.Callback;
import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by smapley on 15/10/23.
 */
public class Web extends Fragment {


    public static String url;
    public static String CookieStr;
    public int nowNum;
    public static String name;
    public static String qihao;
    public static String ip;
    private WebView webView;
    private SharedPreferences sharedPreferences;
    public static String formhash;

    public boolean isThread = false;

    private String url_now = null;

    private CookieManager cookieManager;
    private SharedPreferences sp;

    private EditText title_item2;

    private TextView commit;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.web, container, false);
        sharedPreferences = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
        sp = getActivity().getSharedPreferences("URL", getActivity().MODE_PRIVATE);
        GetNetIp();
        cookieManager = CookieManager.getInstance();
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {

        title_item2 = (EditText) view.findViewById(R.id.title_item2);
        commit = (TextView) view.findViewById(R.id.title_item3);
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.loadUrl(title_item2.getText().toString());
            }
        });


        webView = (WebView) view.findViewById(R.id.webView);
        //支持JavaScript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new InJavaScriptLocalObj(), "local_obj");
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.requestFocus();
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setSavePassword(true);
    }

    public void initData() {

        url = sp.getString("url", "http://7.139139555.com");
        cookieManager.setCookie(url, sp.getString(url + "cookie", ""));
        formhash = sp.getString(url + "formhash", "");
        title_item2.setText(url);
        webView.loadUrl(url);
    }


    private class WebViewClient extends android.webkit.WebViewClient {


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Log.e("asdf", url);
            GetNetIp();
            String[] urls = url.split("=");
            if (urls.length > 1) {
                //点击历史账单
                if (urls[1].equals("memberhistory")) {
                    Toast.makeText(getActivity(), "请在快打明细里查看账单", Toast.LENGTH_SHORT).show();

                } else if (urls[1].equals("orderadmin")) {
                    Toast.makeText(getActivity(), "请在快打中查看明细", Toast.LENGTH_SHORT).show();

                } else {
                    url_now = url;
                    title_item2.setText(url);
                    view.loadUrl(url);
                }
            } else {
                url_now = url;
                title_item2.setText(url);
                view.loadUrl(url);

            }

            return true ;
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            String CookieStrs = cookieManager.getCookie(url);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(url + "cookie", CookieStrs);
            editor.commit();
            try {
                CookieStr = CookieStrs.split("=")[1].split(";")[0];
            } catch (Exception e) {
                e.printStackTrace();
            }
            view.loadUrl("javascript:window.local_obj.showSource('<body>'+"
                    + "document.getElementsByTagName('html')[0].innerHTML+'</body>');");
        }
    }

    final class InJavaScriptLocalObj {

        @JavascriptInterface
        public void showSource(String html) {
            try {
                Document document = Jsoup.parse(html);
                Elements body = document.getElementsByTag("body");
                Elements span = body.get(0).getElementsByTag("span");
                String[] num = span.get(1).text().split(":");
                nowNum = Integer.parseInt(num[3]);
                Log.e("nowNum", "nowNum======>>" + nowNum);
                name = num[1].substring(0, num[1].length() - 3);
                Log.e("name", "name======>>" + name);
                qihao = num[2].substring(0, num[2].length() - 3);
                Log.e("qihao", "qihao======>>" + qihao);
                String[] forms = html.split("formhash");
                formhash = forms[1].substring(2).split("\"")[1];
                Log.e("formhash", "----->" + formhash);
                Elements script = document.getElementsByTag("script");
                String[] formhashs = script.get(2).toString().split(";");
                formhash = formhashs[1].split("'")[1];
                Log.e("formhash", "----->" + formhash);
                //保存
                if (name != null && !name.equals("")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name", name);
                    if (formhash.length() > 5) {
                        editor.putString(url + "formhash", formhash);
                        updateMess2();
                    }
                    editor.commit();
                }


            } catch (Exception e) {

            }


        }
    }


    private void updateMess2() {
        if (MyData.RenZheng) {
            return;
        }
        Log.e("url", "start");
        RequestParams requestParams = new RequestParams(MyData.URL_updateMess2);
        requestParams.addBodyParameter("murl", url.substring(7));
        requestParams.addBodyParameter("cookie", CookieStr);
        requestParams.addBodyParameter("ip", ip);
        requestParams.addBodyParameter("formhash", formhash);
        requestParams.addBodyParameter("user1", MyData.UserName);
        requestParams.addBodyParameter("mi", MyData.PassWord);
        LogUtil.e("murl" + url.substring(7));
        LogUtil.e("cookie" + CookieStr);
        LogUtil.e("ip" + ip);
        LogUtil.e("formhash" + formhash);
        LogUtil.e("user1" + MyData.UserName);
        LogUtil.e("mi" + MyData.PassWord);
        x.http().post(requestParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.e("url", "onSuccess" + result);
                if (Integer.parseInt(result) > 0) {
                    MyData.RenZheng = true;
                    Toast.makeText(getActivity(), "对接成功", Toast.LENGTH_SHORT).show();

                    Log.e("url", "success");
                } else {
                    MyData.RenZheng = false;
                    Log.e("url", "fail");
                    Toast.makeText(getActivity(), "对接失败", Toast.LENGTH_SHORT).show();


                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                Log.e("url", "onError");
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.e("url", "onCancelled");
            }

            @Override
            public void onFinished() {
                Log.e("url", "onFinished");
            }
        });
    }


    /**
     * 获取IP地址
     *
     * @return
     */
    public void GetNetIp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ipaddr = "http://www.cmyip.com/";
                URL infoUrl = null;
                InputStream inStream = null;
                try {
                    infoUrl = new URL(ipaddr);
                    URLConnection connection = infoUrl.openConnection();
                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    int responseCode = httpConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        inStream = httpConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                        StringBuilder strber = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null)
                            strber.append(line + "\n");
                        inStream.close();
                        Document document = Jsoup.parse(strber.toString());
                        String result = document.getElementsByTag("h1").get(0).text();
                        String[] ips = result.split(" ");
                        ip = ips[4];
                        updateMess2();
                    }
                }catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    GetNetIp();
                }
            }
        }).start();

    }

}
