package com.example.googlemaptest.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.example.googlemaptest.R;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private View loginButton, logoutButton;
    private TextView nickName;
    private ImageView profileImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.googlemaptest.R.layout.activity_login);

        loginButton = findViewById(R.id.login);
        View nav_headerLayout = getLayoutInflater().inflate(R.layout.nav_header, null);
        nickName = nav_headerLayout.findViewById(R.id.textView2);
        profileImage = nav_headerLayout.findViewById(R.id.imageViewHeader);


        // 카카오가 설치되어 있는지 확인 하는 메서드또한 카카오에서 제공 콜백 객체를 이용함
        Function2<OAuthToken, Throwable, Unit> callback = (oAuthToken, throwable) -> {
            // 이때 토큰이 전달이 되면 로그인이 성공한 것이고 토큰이 전달되지 않았다면 로그인 실패
            if(oAuthToken != null) {
                Log.d(TAG, "토큰: " + oAuthToken.getAccessToken());
            }
            if (throwable != null) {
                Log.e(TAG, "예외: " + throwable.getMessage());
            }
            updateKakaoLoginUi();
            return null;
        };
        // 로그인 버튼
        loginButton.setOnClickListener(view -> {
            if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)) {
                UserApiClient.getInstance().loginWithKakaoTalk(LoginActivity.this, callback);
            }else {
                UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this, callback);
            }
        });

        updateKakaoLoginUi();
    }
    private void updateKakaoLoginUi() {
        UserApiClient.getInstance().me((user, throwable) -> {
            if (user != null) {
                SharedPreferences sharedPref = getSharedPreferences("login_pref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                // 사용자 정보가 null이 아닌지 확인한 후 저장
                String nickname = user.getKakaoAccount().getProfile().getNickname();
                String profileImageUrl = user.getKakaoAccount().getProfile().getThumbnailImageUrl();

                if (nickname != null) {
                    editor.putString("kakaoNickname", nickname);
                }
                if (profileImageUrl != null) {
                    editor.putString("kakaoProfileImage", profileImageUrl);
                }
                editor.apply();

                // 사용자의 닉네임을 TextView에 설정
                nickName.setText(nickname);

                // 사용자의 프로필 이미지를 설정
                setProfileImage(profileImageUrl);

                onLoginSuccess();
            } else {
                // 로그인되지 않은 상태 처리
                nickName.setText(null);
                profileImage.setImageBitmap(null);
            }
            return null;
        });
    }
    private void onLoginSuccess() {
        // SharedPreferences에 로그인 상태를 저장합니다.
        SharedPreferences sharedPref = getSharedPreferences("login_pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);

        finish(); // LoginActivity 종료
    }

    private void onLogoutSuccess() {
        // SharedPreferences에 로그인 상태를 저장합니다.
        SharedPreferences sharedPref = getSharedPreferences("login_pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();
    }
    private void setProfileImage(String profileImageUrl) {
        // Glide 또는 다른 이미지 로딩 라이브러리를 사용하여 프로필 이미지를 설정합니다.
        Glide.with(getApplicationContext())
                .load(profileImageUrl)
                .placeholder(R.mipmap.ic_launcher) // 이미지가 로드되는 동안 보여줄 이미지
                .error(R.mipmap.ic_launcher) // 이미지 로드 실패 시 보여줄 이미지
                .into(profileImage);
    }

}
