package com.willdev.openvpn;

import android.content.Context;
import android.content.SharedPreferences;

import com.willdev.openvpn.model.Server;

import static com.willdev.openvpn.Utils.getImgURL;

public class SharedPreference {

    private static final String APP_PREFS_NAME = "VPNPreference";

    private SharedPreferences mPreference;
    private SharedPreferences.Editor mPrefEditor;
    private Context context;

    private static final String SERVER_COUNTRY = "server_country";
    private static final String SERVER_FLAG = "server_flag";
    private static final String SERVER_OVPN = "server_ovpn";
    private static final String SERVER_OVPN_USER = "server_ovpn_user";
    private static final String SERVER_OVPN_PASSWORD = "server_ovpn_password";

    private static final String SERVER_COUNTRY_VIP = "server_country_vip";
    private static final String SERVER_FLAG_VIP = "server_flag_vip";
    private static final String SERVER_OVPN_VIP = "server_ovpn_vip";
    private static final String SERVER_OVPN_USER_VIP = "server_ovpn_user_vip";
    private static final String SERVER_OVPN_PASSWORD_VIP = "server_ovpn_password_vip";
    public SharedPreference(Context context) {
        this.mPreference = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE);
        this.mPrefEditor = mPreference.edit();
        this.context = context;
    }


    public void saveServer(Server server){
        mPrefEditor.putString(SERVER_COUNTRY, server.getCountry());
        mPrefEditor.putString(SERVER_FLAG, server.getFlagUrl());
        mPrefEditor.putString(SERVER_OVPN, server.getOvpn());
        mPrefEditor.putString(SERVER_OVPN_USER, server.getOvpnUserName());
        mPrefEditor.putString(SERVER_OVPN_PASSWORD, server.getOvpnUserPassword());
        mPrefEditor.commit();
    }
    public void saveVipServer(Server server){
        mPrefEditor.putString(SERVER_COUNTRY_VIP, server.getCountry());
        mPrefEditor.putString(SERVER_FLAG_VIP, server.getFlagUrl());
        mPrefEditor.putString(SERVER_OVPN_VIP, server.getOvpn());
        mPrefEditor.putString(SERVER_OVPN_USER_VIP, server.getOvpnUserName());
        mPrefEditor.putString(SERVER_OVPN_PASSWORD_VIP, server.getOvpnUserPassword());
        mPrefEditor.commit();
    }


    public Server getServer() {

        Server server = new Server(
                mPreference.getString(SERVER_COUNTRY,"Select Country"),
                mPreference.getString(SERVER_FLAG,getImgURL(R.drawable.ic_baseline_language_24)),
                mPreference.getString(SERVER_OVPN,"japan.ovpn"),
                mPreference.getString(SERVER_OVPN_USER,"vpn"),
                mPreference.getString(SERVER_OVPN_PASSWORD,"vpn")
        );

        return server;
    }
    public Server getVipServer() {

        return new Server(
                mPreference.getString(SERVER_COUNTRY_VIP,"Select Country"),
                mPreference.getString(SERVER_FLAG_VIP,getImgURL(R.drawable.ic_baseline_language_24)),
                mPreference.getString(SERVER_OVPN_VIP,"japan.ovpn"),
                mPreference.getString(SERVER_OVPN_USER_VIP,"vpn"),
                mPreference.getString(SERVER_OVPN_PASSWORD_VIP,"vpn")
        );
    }
}
