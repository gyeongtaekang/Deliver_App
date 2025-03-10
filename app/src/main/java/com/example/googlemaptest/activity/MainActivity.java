package com.example.googlemaptest.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.googlemaptest.MyApplication;
import com.example.googlemaptest.R;
import com.example.googlemaptest.fragment.ChatFragment;
import com.example.googlemaptest.fragment.FilterDialogFragment;
import com.example.googlemaptest.fragment.ListFragment;
import com.example.googlemaptest.fragment.MapFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.kakao.sdk.user.UserApiClient;
import com.example.googlemaptest.fragment.ChatRoomsFragment;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;

    private MapFragment mapFragment;
    private ListFragment listFragment;
    private ChatFragment chatFragment;
    private ChatRoomsFragment chatRoomsFragment;
    private void loadChatRoom(int partyId) {
        // partyId에 해당하는 채팅 방을 로드하는 로직을 작성
        // 예시: 채팅 화면으로 이동하는 코드 또는 채팅 방 정보를 표시하는 등의 동작을 수행

        // 예시: 채팅 화면으로 이동하는 코드
        Intent chatIntent = new Intent(this, ChatFragment.class); // 수정된 부분: this 사용
        chatIntent.putExtra("partyId", partyId);
        startActivity(chatIntent);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapFragment = new MapFragment();
        listFragment = new ListFragment();
        chatFragment = new ChatFragment();
        chatRoomsFragment = new ChatRoomsFragment();

        SharedPreferences sharedPref = getSharedPreferences("login_pref", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean("isLoggedIn", false);
        if (!isLoggedIn) {
            //showLoginScreen();
        }

        // SharedPreferences에서 닉네임 불러오기
        SharedPreferences sharedPreferences = getSharedPreferences("ChatApp", Context.MODE_PRIVATE);
        String nick = sharedPreferences.getString("nickname", "defaultNick");

// getArguments()가 null이 아닌지 확인하고 partyId를 가져옵니다.
        Bundle args = getIntent().getExtras();
        if (args != null && args.containsKey("partyId")) {
            int partyId = args.getInt("partyId", -1);
            if (partyId != -1) {
                loadChatRoom(partyId);
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Spinner 설정
        Spinner spinner = findViewById(R.id.toolbar_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        getSupportActionBar().setTitle("배달 조각");

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_item_one) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainFrame, mapFragment)
                        .commit();
            } else if (id == R.id.nav_item_two) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainFrame, listFragment)
                        .commit();
            }
           else if (id == R.id.nav_item_three) {
/*                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainFrame, chatRoomsFragment) // 여기를 수정합니다.
                        .commit();*/
                String url = "https://menurecommende.netlify.app";

                // 웹 페이지 열기
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
            else if (id == R.id.nav_item_four) {
                // 웹 페이지 URL
                String url = "https://naver.me/xylKYa45";

                // 웹 페이지 열기
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
            else if (id == R.id.nav_item_five) {
                // 웹 페이지 URL
                String url = "https://deliverypeace.netlify.app/";

                // 웹 페이지 열기
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFrame, mapFragment)
                    .commit();
            navigationView.setCheckedItem(R.id.nav_item_one);
        }

        View headerView = navigationView.getHeaderView(0);
        ImageView imageViewProfile = headerView.findViewById(R.id.imageViewHeader);
        TextView textViewNickname = headerView.findViewById(R.id.textView2);

        String nickname = sharedPref.getString("kakaoNickname", "사용자 이름");
        String profileImageUrl = sharedPref.getString("kakaoProfileImage", "");
        textViewNickname.setText(nickname);
        if (!profileImageUrl.equals("")) {
            Glide.with(this).load(profileImageUrl).into(imageViewProfile);
        } else {
            imageViewProfile.setImageResource(R.mipmap.ic_launcher);
        }

        Button logoutButton = headerView.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(view -> {
            Log.d("LogoutTest", "Logout button clicked");
            UserApiClient.getInstance().logout(throwable -> {
                if (throwable != null) {
                    Log.e("LogoutError", "Logout failed: " + throwable.getMessage());
                } else {
                    Log.d("LogoutTest", "Logout successful");
                }

                SharedPreferences sharedPref1 = getSharedPreferences("login_pref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref1.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.apply();

                Intent intent = new Intent(com.example.googlemaptest.activity.MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return null;
            });
        });
        // 스피너 참조 및 어댑터 설정
        Spinner gradeSpinner = findViewById(R.id.toolbar_spinner);
        ArrayAdapter<CharSequence> gradeAdapter = ArrayAdapter.createFromResource(this, R.array.spinner_list, android.R.layout.simple_spinner_item);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeSpinner.setAdapter(gradeAdapter);

        // 선택 항목 변경 이벤트 리스너 추가
        gradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGrade = parent.getItemAtPosition(position).toString();
                if (mapFragment != null) {
                    mapFragment.changeUserGrade(selectedGrade);
                } else {
                    Log.e("MainActivity", "MapFragment is not initialized");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (mapFragment != null) {
                    mapFragment.changeUserGrade("Bronze");
                } else {
                    Log.e("MainActivity", "MapFragment is not initialized");
                }
            }

        });
        Button adButton = findViewById(R.id.btn_ad);
        adButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 새로운 액티비티 시작
                Intent intent = new Intent(MainActivity.this, AdActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filter) {
            FilterDialogFragment filterDialog = new FilterDialogFragment();
            filterDialog.show(getSupportFragmentManager(), "filter_dialog");
            filterDialog.setFilterDialogListner((foodCategory, range, participantCount) -> {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mainFrame);
                if (fragment instanceof MapFragment) {
                    // MapFragment에 필터 옵션 적용 (참가 인원 수 포함)
                    ((MapFragment) fragment).filterMarkers(foodCategory, range, participantCount);
                } else if (fragment instanceof ListFragment) {
                    // ListFragment에 필터 옵션 적용 (참가 인원 수 포함)
                    ((ListFragment) fragment).filterMarkers(foodCategory, range, participantCount);
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MyApplication.LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mainFrame);
                if (fragment instanceof MapFragment) {
                    ((MapFragment) fragment).reloadMap();
                } else if (fragment instanceof ListFragment) {

                }
            } else {
                Toast.makeText(this, "위치 정보 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
