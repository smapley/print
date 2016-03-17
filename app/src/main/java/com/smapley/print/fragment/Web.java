package com.smapley.print.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.smapley.print.R;
import com.smapley.print.util.HttpUtils;
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
import java.util.HashMap;

/**
 * Created by smapley on 15/10/23.
 */
public class Web extends Fragment {


    private static final int JIEZHANG = 1;
    public static String url;
    public static String url1 = "http://";
    public static String CookieStr;
    public Double nowNum;
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

    private TextView state;

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

        state = (TextView) view.findViewById(R.id.title_state);
        state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HashMap map = new HashMap();
                        map.put("user1", MyData.UserName);
                        mhandler.obtainMessage(JIEZHANG, HttpUtils.updata(map, MyData.URL_addJiezhang)).sendToTarget();
                    }
                }).start();
            }
        });

        title_item2 = (EditText) view.findViewById(R.id.title_item2);
        title_item2.setSelectAllOnFocus(true);
//        title_item2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                title_item2.setSelection(0, 5);
//            }
//        });
        commit = (TextView) view.findViewById(R.id.title_item3);
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.loadUrl(url1 + title_item2.getText().toString());
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

        url = sp.getString("url", "7.139139555.com");
        cookieManager.setCookie(url1 + url, sp.getString(url1 + url + "cookie", ""));
        title_item2.setText(url.split("/")[0]);
        webView.loadUrl(url1 + url.split("/")[0]);
    }


    private class WebViewClient extends android.webkit.WebViewClient {


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("asdf", url);
            String[] urls = url.split("=");
            if (urls.length > 1) {
                //点击历史账单
                if (urls[1].equals("memberhistory")) {
                    Toast.makeText(getActivity(), "账单以快打明细中的为准\n" +
                            "个别断码的和超量的会补到别的网(盈亏2)", Toast.LENGTH_SHORT).show();

                } else if (urls[1].equals("orderadmin")) {
                    Toast.makeText(getActivity(), "请在快打中查看明细\n" +
                            "因为在此处退码结算时会有误差", Toast.LENGTH_SHORT).show();

                } else {
                    if(urls[1].equals("logout")){
                        LogUtil.e("logout");
                        MyData.RenZheng=false;
                        formhash=null;
                    }
                    url_now = url;
                    title_item2.setText(url.substring(7));
                    view.loadUrl(url);
                }
            } else {
                url_now = url;
                title_item2.setText(url);
                view.loadUrl(url);

            }
            GetNetIp();

            return true;
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            String CookieStrs = cookieManager.getCookie(url);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(url + "cookie", CookieStrs);
            editor.putString("url",url.substring(7));
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
                nowNum = Double.parseDouble(num[3]);
                Log.e("nowNum", "nowNum======>>" + nowNum);
                name = num[1].substring(0, num[1].length() - 3);
                Log.e("name", "name======>>" + name);
                qihao = num[2].substring(0, num[2].length() - 3);
                Log.e("qihao", "qihao======>>" + qihao);
                String[] forms = html.split("formhash");
                formhash = forms[1].substring(2).split("\"")[1];
                Log.e("formhash", "----->" + formhash);
                //保存
                if (name != null && !name.equals("")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name", name);
                    editor.commit();
                    updateMess2();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }


    private void updateMess2() {
        if (MyData.RenZheng || formhash == null || ip == null||CookieStr==null||formhash.length()<3||CookieStr.length()<3) {
            Log.e("updateMess2", "notRun");
            return;
        }
        Log.e("updateMess2", "start");
        RequestParams requestParams = new RequestParams(MyData.URL_updateMess2);
        requestParams.addBodyParameter("murl", url.split("/")[0]);
        requestParams.addBodyParameter("cookie", CookieStr);
        requestParams.addBodyParameter("ip", ip);
        requestParams.addBodyParameter("formhash", formhash);
        requestParams.addBodyParameter("user1", MyData.UserName);
        requestParams.addBodyParameter("mi", MyData.PassWord);
        LogUtil.e("murl" + url.split("/")[0]);
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
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                    builder1.setMessage("对接失败");
                    builder1.setNegativeButton("确定", null);
                    builder1.create().show();

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


    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                switch (msg.what) {
                    case JIEZHANG:
                        String result = JSON.parseObject(msg.obj.toString(), new TypeReference<String>() {
                        });
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(result);
                        builder.setNegativeButton("确定", null);
                        builder.create().show();

                        break;
                }
            } catch (Exception e) {

            }
        }
    };

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
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    GetNetIp();
                }
            }
        }).start();

    }

}
