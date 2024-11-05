package com.willdev.openvpn.fromanother.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.willdev.openvpn.R;
import com.willdev.openvpn.fromanother.interfaces.VideoAd;
import com.willdev.openvpn.fromanother.util.util.API;
import com.willdev.openvpn.fromanother.util.util.Constant;
import com.willdev.openvpn.fromanother.util.util.Method;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import rubikstudio.library.LuckyWheelView;
import rubikstudio.library.model.LuckyItem;

public class Spinner extends AppCompatActivity {

    private Method method;
    private VideoAd videoAd;
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private List<LuckyItem> spinnerLists;
    private LuckyWheelView luckyWheelView;
    private Button button_spinner;
    private MaterialTextView textView_noData, textView_msg;
    private RelativeLayout relativeLayout_root;
    private ProgressDialog progressDialog;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_willdev_spinner);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.e("REWARDED INITIALIZ", initializationStatus.getAdapterStatusMap().toString());
            }
        });

        videoAd = type -> {
            int indexPosition = getRandomIndex();
            luckyWheelView.startLuckyWheelWithTargetIndex(indexPosition);
        };
        method = new Method(Spinner.this, videoAd);
        method.forceRTLIfSupported();
        spinnerLists = new ArrayList<>();

        toolbar = findViewById(R.id.toolbar_spinner);
        toolbar.setTitle(getResources().getString(R.string.spinner));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(Spinner.this);

        luckyWheelView = findViewById(R.id.luckyWheel_spinner);
        progressBar = findViewById(R.id.progressbar_spinner);
        button_spinner = findViewById(R.id.button_spinner);
        relativeLayout_root = findViewById(R.id.relativeLayout_root_spinner);
        textView_noData = findViewById(R.id.textView_spinner);
        textView_msg = findViewById(R.id.textView_msg_spinner);
        LinearLayout linearLayout = findViewById(R.id.linearLayout_spinner);

        progressBar.setVisibility(View.GONE);
        textView_noData.setVisibility(View.GONE);
        relativeLayout_root.setVisibility(View.GONE);
        method.adView(linearLayout);

        if (method.isNetworkAvailable()) {
            if (method.isLogin()) {
                SpinnerData(method.userId());
            } else {
                textView_noData.setVisibility(View.VISIBLE);
                textView_noData.setText(getResources().getString(R.string.you_have_not_login));
                method.alertBox(getResources().getString(R.string.you_have_not_login));
            }
        } else {
            textView_noData.setVisibility(View.VISIBLE);
            textView_noData.setText(getResources().getString(R.string.no_data_found));
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    private void SpinnerData(String userId) {

        spinnerLists.clear();
        progressBar.setVisibility(View.VISIBLE);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Spinner.this));
        jsObj.addProperty("method_name", "get_spinner");
        jsObj.addProperty("user_id", method.userId());
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant.url, params, new AsyncHttpResponseHandler() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String res = new String(responseBody);

                try {
                    JSONObject jsonObject = new JSONObject(res);

                    if (jsonObject.has(Constant.STATUS)) {

                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        if (status.equals("-2")) {
                            method.suspend(message);
                        } else {
                            method.alertBox(message);
                        }
                        textView_noData.setVisibility(View.VISIBLE);

                    } else {

                        int daily_spinner_limit = jsonObject.getInt("daily_spinner_limit");
                        int remain_spin = jsonObject.getInt("remain_spin");
                        String ad_on_spin = jsonObject.getString("ad_on_spin");

                        String msg_one = getResources().getString(R.string.daily_total_spins);
                        String msg_two = getResources().getString(R.string.remaining_spins_today);

                        textView_msg.setText(msg_one
                                + " " + daily_spinner_limit + " "
                                + msg_two + " " + remain_spin);

                        JSONArray jsonArray = jsonObject.getJSONArray(Constant.tag);

                        for (int i = 0; i < jsonArray.length(); i++) {

                            LuckyItem objItem = new LuckyItem();
                            JSONObject object = jsonArray.getJSONObject(i);
                            objItem.topText = object.getString("points");
                            objItem.icon = R.drawable.coins;
                            objItem.color = Color.parseColor(object.getString("bg_color"));

                            spinnerLists.add(objItem);
                        }

                        if (spinnerLists.size() == 0) {
                            textView_noData.setVisibility(View.VISIBLE);
                        } else {

                            luckyWheelView.setData(spinnerLists);
                            luckyWheelView.setRound(2);

                            if (remain_spin == 0) {
                                button_spinner.setVisibility(View.GONE);
                            } else {
                                button_spinner.setVisibility(View.VISIBLE);
                            }

                            relativeLayout_root.setVisibility(View.VISIBLE);

                            button_spinner.setOnClickListener(view -> {

                                if (ad_on_spin.equals("true")) {
                                    method.VideoAdDialog("spinner", "view_ad", Spinner.this);
                                } else {
                                    method.VideoAdDialog("spinner", "", Spinner.this);
                                }
                            });

                            luckyWheelView.setLuckyRoundItemSelectedListener(index -> {
                                if (index != 0) {
                                    index = index - 1;
                                }
                                int pointAdd = Integer.parseInt(spinnerLists.get(index).topText);
                                if (pointAdd != 0) {
                                    sendSpinnerData(userId, pointAdd);
                                }
                            });

                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                progressBar.setVisibility(View.GONE);
                textView_noData.setVisibility(View.VISIBLE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    private void sendSpinnerData(String user_id, int point) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Spinner.this));
        jsObj.addProperty("method_name", "save_spinner_points");
        jsObj.addProperty("user_id", user_id);
        jsObj.addProperty("ponints", String.valueOf(point));
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant.url, params, new AsyncHttpResponseHandler() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String res = new String(responseBody);

                try {
                    JSONObject jsonObject = new JSONObject(res);

                    if (jsonObject.has(Constant.STATUS)) {

                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        if (status.equals("-2")) {
                            method.suspend(message);
                        } else {
                            method.alertBox(message);
                        }

                    } else {

                        JSONArray jsonArray = jsonObject.getJSONArray(Constant.tag);

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject object = jsonArray.getJSONObject(i);
                            String msg = object.getString("msg");
                            String success = object.getString("success");
                            int daily_spinner_limit = object.getInt("daily_spinner_limit");
                            int remain_spin = object.getInt("remain_spin");

                            String msg_one = getResources().getString(R.string.daily_total_spins);
                            String msg_two = getResources().getString(R.string.remaining_spins_today);

                            textView_msg.setText(msg_one
                                    + " " + daily_spinner_limit + " "
                                    + msg_two + " " + remain_spin);

                            if (success.equals("1")) {
                                if (remain_spin == 0) {
                                    button_spinner.setVisibility(View.GONE);
                                } else {
                                    button_spinner.setVisibility(View.VISIBLE);
                                }
                            } else {
                                button_spinner.setVisibility(View.GONE);
                            }

                            method.alertBox(msg);

                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                progressDialog.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    private int getRandomIndex() {
        Random rand = new Random();
        return rand.nextInt(spinnerLists.size() - 1);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
