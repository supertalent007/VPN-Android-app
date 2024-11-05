package com.willdev.openvpn.fromanother.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textview.MaterialTextView;
import com.willdev.openvpn.R;
import com.willdev.openvpn.fromanother.util.util.API;
import com.willdev.openvpn.fromanother.util.util.Constant;
import com.willdev.openvpn.fromanother.util.util.Method;
import com.willdev.openvpn.utils.Config;
import com.willdev.openvpn.view.MainActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class RedeemActivity extends AppCompatActivity {

    private Method method;
    private MaterialToolbar toolbar;
    private String user_id;
    private InputMethodManager imm;
    private ProgressDialog progressDialog;
    private TextInputEditText editText;
    private MaterialButton button_continue, button_skip;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem_will_dev);

        method = new Method(RedeemActivity.this);
        method.forceRTLIfSupported();

        user_id = getIntent().getStringExtra("user_id");

        toolbar = findViewById(R.id.toolbar_erc);
        toolbar.setTitle("Redeem");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(RedeemActivity.this);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        editText = findViewById(R.id.etCode);
        button_continue = findViewById(R.id.button_continue_erc);
        button_skip = findViewById(R.id.button_skip_erc);

        button_continue.setOnClickListener(v -> {

            editText.setError(null);

            String code = editText.getText().toString();

            if (code.equals("") || code.isEmpty()) {
                editText.requestFocus();
                editText.setError("Please enter your code");
            } else {

                editText.clearFocus();
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                if (method.isNetworkAvailable()) {
                    redeem(code);
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }

            }

        });

        button_skip.setOnClickListener(v -> {
            startActivity(new Intent(RedeemActivity.this, MainActivity.class));
            finishAffinity();
        });

        MaterialButton btnRedeemStatus = findViewById(R.id.btnRedeemStatus);
        btnRedeemStatus.setOnClickListener(v -> {
                if (Objects.equals(Config.perks, ""))
                    method.alertBox("You have not redeemed any code yet");
                else
                    showRedeemDialog("Redeem Status", Config.perks, Config.expiration, "Close", false);
            }
        );

    }

    private void showRedeemDialog(String title, String perks, String exp, String btn, boolean isFinish) {
        Dialog dialog = new Dialog(RedeemActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_redeem_willdev_status);
        dialog.setCancelable(false);
        if (method.isRtl()) {
            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

        MaterialTextView tvTitle = dialog.findViewById(R.id.tvTitle);
        MaterialTextView tvPerks = dialog.findViewById(R.id.tvPerks);
        MaterialTextView tvExpire = dialog.findViewById(R.id.tvExpire);
        MaterialButton btnClose = dialog.findViewById(R.id.btnClose);

        tvTitle.setText(title);
        tvPerks.setText(perks);
        tvExpire.setText(exp);
        btnClose.setText(btn);

        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            if (isFinish) {
                finishAffinity();
                startActivity(new Intent(this, MainActivity.class));
            }
        });

        dialog.show();
    }


    public void redeem(String code) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(RedeemActivity.this));
        jsObj.addProperty("user_id", method.userId());
        jsObj.addProperty("code", code);
        jsObj.addProperty("method_name", "redeem_code");
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant.url, params, new AsyncHttpResponseHandler() {
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

                        JSONObject object = jsonObject.getJSONObject(Constant.tag);

                        String success = object.getString("success");
                        String msg = object.getString("msg");
                        String exp = object.getString("exp");
                        String perks = object.getString("perks");
                        int no_ads = object.getInt("no_ads");
                        int premium_servers = object.getInt("premium_servers");

                        Config.no_ads = no_ads == 1;
                        Config.premium_servers_access = premium_servers == 1;
                        Config.perks = perks;
                        Config.expiration = exp;

                        if (success.equals("1")) {
                            showRedeemDialog("Success!", perks, exp, "Back To Home", true);
                        } else {
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
