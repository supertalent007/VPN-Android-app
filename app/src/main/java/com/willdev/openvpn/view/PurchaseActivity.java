package com.willdev.openvpn.view;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.willdev.openvpn.R;
import com.willdev.openvpn.api.WillDevWebAPI;
import com.willdev.openvpn.model.SubscriptionPlans;
import com.willdev.openvpn.utils.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ph.gemeaux.materialloadingindicator.MaterialCircularIndicator;

import com.google.common.collect.ImmutableList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PurchaseActivity extends AppCompatActivity {


     String vpn1 = Config.all_month_id;
     String vpn2 = Config.all_threemonths_id;
     String vpn3 = Config.all_sixmonths_id;
     String vpn4 = Config.all_yearly_id;

    String subscription = "";


    private MutableLiveData<Integer> all_check = new MutableLiveData<>();

    RadioButton oneMonth;
    RadioButton threeMonth;
    RadioButton sixMonth;
    RadioButton oneYear;

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                if (purchases != null) {

                    if (purchases.get(0) != null) {
                        Log.v("CHECKBILLING", purchases.get(0).toString());
                        handlePurchase(purchases.get(0).getPurchaseToken());
                    }
                } else {
                    Toast.makeText(PurchaseActivity.this, "Something went wrong. Please try again", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private BillingClient billingClient;
    private ArrayList<SubscriptionPlans> subList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_willdev_unlock_all);

        oneMonth = findViewById(R.id.one_month);
        threeMonth = findViewById(R.id.three_month);
        sixMonth = findViewById(R.id.six_month);
        oneYear = findViewById(R.id.one_year);

        subList = new ArrayList<>();

        MaterialCircularIndicator progressDialog = new MaterialCircularIndicator(this);
        progressDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OkHttpClient okHttpClient = new OkHttpClient();
                            Request request = new Request.Builder().url(WillDevWebAPI.ADMIN_PANEL_API+"includes/api.php?get_subscription").build();
                            Response response = okHttpClient.newCall(request).execute();
                            subscription = response.body().string();

                            JSONArray jsonArray = new JSONArray(subscription);

                            for (int i=0; i < jsonArray.length();i++) {
                                JSONObject object = (JSONObject) jsonArray.get(i);
                                subList.add(new SubscriptionPlans(object.getString("name"),
                                        object.getString("product_id"),
                                        object.getString("price"),
                                        object.getString("currency")
                                ));
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView tvMonthly = findViewById(R.id.tvMonthly);
                                    TextView tvMonthlyPrice = findViewById(R.id.tvMonthlyPrice);
                                    TextView tv3Months = findViewById(R.id.tv3Months);
                                    TextView tv3MonthsPrice = findViewById(R.id.tv3MonthsPrice);
                                    TextView tv6Months = findViewById(R.id.tv6Months);
                                    TextView tv6MonthsPrice = findViewById(R.id.tv6MonthsPrice);
                                    TextView tvYearly = findViewById(R.id.tvYearly);
                                    TextView tvYearlyPrice = findViewById(R.id.tvYearlyPrice);

                                    RelativeLayout lytSubscriptions = findViewById(R.id.lytSubscriptions);

                                    tvMonthly.setText(subList.get(0).getName());
                                    tvMonthlyPrice.setText(subList.get(0).getCurrency() + subList.get(0).getPrice());
                                    tv3Months.setText(subList.get(1).getName());
                                    tv3MonthsPrice.setText(subList.get(1).getCurrency() + subList.get(1).getPrice());
                                    tv6Months.setText(subList.get(2).getName());
                                    tv6MonthsPrice.setText(subList.get(2).getCurrency() + subList.get(2).getPrice());
                                    tvYearly.setText(subList.get(3).getName());
                                    tvYearlyPrice.setText(subList.get(3).getCurrency() + subList.get(3).getPrice());

                                    lytSubscriptions.setVisibility(View.VISIBLE);

                                    vpn1 = subList.get(0).getProduct_id();
                                    vpn2 = subList.get(1).getProduct_id();
                                    vpn3 = subList.get(2).getProduct_id();
                                    vpn4 = subList.get(3).getProduct_id();

                                    progressDialog.dismiss();
                                }
                            });



                        } catch (IOException e) {
                            Log.v("willdev",e.toString());
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },1000);

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        all_check.setValue( -1);
        all_check.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                switch (integer){
                    case 0:
                        threeMonth.setChecked(false);
                        sixMonth.setChecked(false);
                        oneYear.setChecked(false);
                        break;
                    case 1:
                        oneMonth.setChecked(false);
                        sixMonth.setChecked(false);
                        oneYear.setChecked(false);
                        break;
                    case 2:
                        threeMonth.setChecked(false);
                        oneMonth.setChecked(false);
                        oneYear.setChecked(false);
                        break;
                    case 3:
                        threeMonth.setChecked(false);
                        sixMonth.setChecked(false);
                        oneMonth.setChecked(false);
                        break;

                }
            }
        });

        oneMonth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) all_check.postValue(0);
            }
        });
        threeMonth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) all_check.postValue(1);
            }
        });
        sixMonth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) all_check.postValue(2);
            }
        });
        oneYear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) all_check.postValue(3);
            }
        });

        billingSetup();

        TextView all_pur = findViewById(R.id.all_pur);

        all_pur.setOnClickListener(v -> {
            if(all_check.getValue() != null)unlock_all(all_check.getValue());
        });

        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void billingSetup() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    Log.v("CHECKBILLING", "ready");
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                Log.v("CHECKBILLING", "disconnected");
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                finish();
                Toast.makeText(PurchaseActivity.this, "Service temporarily unavailable. Please check your Google Play account or try again after some time.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void queryProduct(String productId) {
        Log.v("CHECKBILLING", "clicked");
        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(
                                ImmutableList.of(
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId(productId)
                                                .setProductType(BillingClient.ProductType.SUBS)
                                                .build()))
                        .build();

        billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                new ProductDetailsResponseListener() {
                    public void onProductDetailsResponse(BillingResult billingResult,
                                                         List<ProductDetails> productDetailsList) {

                        Log.v("CHECKBILLING", billingResult.toString());
                        Log.e("CHECKBILLING", productId + ": " + productDetailsList.toString());
                        if (productDetailsList.size() > 0) {

                            makePurchase(productDetailsList.get(0));

                        } else {
                            Log.e("CHECKBILLING", "onProductDetailsResponse: No products");

                            finish();
                            Toast.makeText(PurchaseActivity.this, "Sorry, this subscription is currently unavailable", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    private void makePurchase(ProductDetails productDetails) {
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                        ImmutableList.of(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .setOfferToken("")
                                        .build()
                        )
                )
                .build();

        Log.v("CHECKBILLING", "makePurchase");
        // Launch the billing flow
        BillingResult billingResult = billingClient.launchBillingFlow(this, billingFlowParams);
    }

    private void handlePurchase(String purchaseToken) {

        AcknowledgePurchaseParams acknowledgePurchaseParams =
                AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchaseToken)
                        .build();

        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Log.v("CHECKBILLING", "acknowledged");
                        Config.vip_subscription = true;
                        Config.all_subscription = true;
                    }
                };

                thread.start();
            }
        };

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
    }

    private void unlock_all(int i) {

        switch (i) {
            case 0:
                queryProduct(vpn1);
                break;

            case 1:
                queryProduct(vpn2);
                break;

            case 2:
                queryProduct(vpn3);
                break;

            case 3:
                queryProduct(vpn4);
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        billingClient.endConnection();
    }
}
