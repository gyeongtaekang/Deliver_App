package com.example.googlemaptest;

import android.app.Application;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.googlemaptest.api.API;
import com.example.googlemaptest.api.Party;
import com.google.firebase.FirebaseApp;
import com.kakao.sdk.common.KakaoSdk;

import java.util.ArrayList;
import java.util.HashMap;

public class MyApplication extends Application {
    private static MyApplication instance;
    private static HashMap<Integer, Party> partyMap;

    public static int LOCATION_REQUEST_CODE = 1;

    private static RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);

        instance = this;
        partyMap = new HashMap<>();
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        reloadPartyMap();
        KakaoSdk.init(this,"f49ce8c37991d813280980ccf85a0b1a");
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public static void reloadPartyMap() {
        partyMap.clear();
        try {
            ArrayList<Party> partyList = API.getPartyList();
            if (partyList != null) {
                for (Party party : partyList) {
                    partyMap.put(party.getId(), party);
                }
            } else {
                // partyList가 null인 경우에 대한 처리를 추가합니다.
                Log.e("MyApplication", "Failed to load party list from API.");
            }
        } catch (Exception e) {
            // 예외 처리를 추가합니다.
            Log.e("MyApplication", "Error while loading party list: " + e.getMessage());
        }
    }

    public static HashMap<Integer, Party> getPartyMap() {
        return partyMap;
    }

    public static RequestQueue getRequestQueue() {
        return requestQueue;
    }
}
