package com.willdev.openvpn.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class SubscriptionPlans implements Parcelable {
    private String name;
    private String product_id;
    private String price;
    private String currency;


    public SubscriptionPlans() {
    }

    public SubscriptionPlans(String name, String product_id, String price, String currency) {
        this.name = name;
        this.product_id = product_id;
        this.price = price;
        this.currency = currency;
    }

    protected SubscriptionPlans(Parcel in) {
        name = in.readString();
        product_id = in.readString();
        price = in.readString();
        currency = in.readString();
    }

    public static final Creator<SubscriptionPlans> CREATOR = new Creator<SubscriptionPlans>() {
        @Override
        public SubscriptionPlans createFromParcel(Parcel in) {
            return new SubscriptionPlans(in);
        }

        @Override
        public SubscriptionPlans[] newArray(int size) {
            return new SubscriptionPlans[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(product_id);
        dest.writeString(price);
        dest.writeString(currency);
    }
}
