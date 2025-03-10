package com.example.googlemaptest.fragment;

// 필요한 기본 import 문들
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.googlemaptest.MyApplication;
import com.example.googlemaptest.R;
import com.example.googlemaptest.activity.ChatActivity;
import com.example.googlemaptest.api.API;
import com.example.googlemaptest.api.Party;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerInfoDialogFragment extends DialogFragment implements OnMapReadyCallback {
    TextView textView, textView2, textView3, textView4, textView5, textView6;
    private int id;
    private MarkerReloadListener listener;
    TextView textViewRecruit; // 모집인원 표시를 위한 TextView

    private MapView mapView;
    public MarkerInfoDialogFragment() {
        // 필요한 초기화 코드가 있다면 여기에 추가
    }
    public MarkerInfoDialogFragment(int id) {
        this.id = id;
    }
    public static MarkerInfoDialogFragment newInstance(int someInt, String someString) {
        MarkerInfoDialogFragment fragment = new MarkerInfoDialogFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", someInt);
        args.putString("someString", someString);
        fragment.setArguments(args);
        return fragment;
    }
    public static MarkerInfoDialogFragment newInstance(int partyId) {
        MarkerInfoDialogFragment fragment = new MarkerInfoDialogFragment();
        Bundle args = new Bundle();
        args.putInt("partyId", partyId);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getInt("partyId", -1);
        }
    }

    public interface MarkerReloadListener {
        void onReload();
    }

    public void setListener(MarkerReloadListener reloadListener) {
        this.listener = reloadListener;
    }
    private void openChatRoom(int partyId, String nickname) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("partyId", partyId);
        intent.putExtra("nickname", nickname);
        startActivity(intent);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 레이아웃 인플레이션
        View root = inflater.inflate(R.layout.activity_information, container, false);
        // Party 객체 가져오기
        Party party = MyApplication.getPartyMap().get(id);

        // Party 객체가 null인 경우 처리
        if (party == null) {
            Log.e("MarkerInfoDialogFragment", "Party object is null.");
            Toast.makeText(requireActivity(), "파티 정보를 불러올 수 없습니다.", Toast.LENGTH_LONG).show();
            dismiss(); // DialogFragment 닫기
            return root; // 이후 코드 실행 방지
        }
        // 각 요소를 ID로 찾아 초기화
        TextView textViewTitle = root.findViewById(R.id.title);
        TextView textViewDescription = root.findViewById(R.id.description);
        TextView textViewMenu = root.findViewById(R.id.menu);
        TextView textViewRestaurantName = root.findViewById(R.id.restaurantName);
        TextView textViewRecruit = root.findViewById(R.id.recruitTextView);



        // 각 TextView에 텍스트 설정
        textViewTitle.setText("제목: " + party.getName());
        textViewDescription.setText("내용: " + party.getDescription());
        textViewMenu.setText("메뉴: " + party.getMenu());
        textViewRestaurantName.setText("식당 이름: " + party.getRestaurant());
        textViewRecruit.setText("모집인원: " + party.getRecruitNumber() + " 명");

        // MapView 초기화 및 설정
        mapView = root.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);  // this는 OnMapReadyCallback 구현을 가리킵니다.

        // 채팅방으로 이동 버튼 이벤트 핸들러
        Button chatButton = root.findViewById(R.id.chatButton);
        chatButton.setOnClickListener(view -> {
            Log.d("DialogDebug", "Chat button clicked");
            if (getContext() != null) {
                Log.d("DialogDebug", "Context is not null");
                showNicknameInputDialog(id);
            } else {
                Log.e("DialogDebug", "Context is null");
            }
        });

        // 삭제 버튼 이벤트 핸들러
        Button deleteButton = root.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(view -> {
            // 삭제 관련 로직 구현
            MarkerDeleteDialogFragment fragment = new MarkerDeleteDialogFragment(id);
            fragment.show(requireActivity().getSupportFragmentManager(), "marker_delete_dialog");
            fragment.setListener(
                    id -> {
                        // SUCCESS
                        API.deleteParty(id);
                        if (listener != null) {
                            listener.onReload();
                        }
                        Toast.makeText(requireActivity(), "파티를 삭제하였습니다.", Toast.LENGTH_LONG).show();
                        dismiss();
                    },
                    id -> {
                        // FAILURE
                        Toast.makeText(requireActivity(), "비밀번호가 틀렸습니다.", Toast.LENGTH_LONG).show();
                    });
        });

        // 나가기 버튼 이벤트 핸들러
        Button exitButton = root.findViewById(R.id.exitButton);
        exitButton.setOnClickListener(view -> dismiss()); // 다이얼로그 닫기

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Party party = MyApplication.getPartyMap().get(id);
        if (party != null) {
            try {
                // Party 객체에서 위도와 경도 정보를 가져옵니다.
                double lat = Double.parseDouble(party.getDeliveryLat());
                double lon = Double.parseDouble(party.getDeliveryLon());

                // LatLng 객체를 생성합니다.
                LatLng location = new LatLng(lat, lon);

                // 지도에 마커를 추가합니다.
                googleMap.addMarker(new MarkerOptions().position(location).title(party.getName()));

                // 카메라를 해당 위치로 이동시킵니다.
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            } catch (NumberFormatException e) {
                Log.e("MarkerInfoDialogFragment", "Invalid latitude or longitude format", e);
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        WindowManager windowManager = (WindowManager) requireActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Dialog dialog = new Dialog(requireActivity());
        Party party = MyApplication.getPartyMap().get(id);
        dialog.setContentView(R.layout.activity_information);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (size.y * 0.8));
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    private void showNicknameInputDialog(int partyId) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("ChatApp", Context.MODE_PRIVATE);
        String savedNickname = sharedPreferences.getString("nickname", null);

        if (savedNickname != null) {
            // 저장된 닉네임이 있는 경우, 변경 여부를 묻는 대화상자 표시
            new AlertDialog.Builder(getContext())
                    .setTitle("닉네임 변경")
                    .setMessage("저장된 닉네임 '" + savedNickname + "'을 사용하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> {
                        // 기존 닉네임 사용
                        openChatRoom(partyId, savedNickname);
                    })
                    .setNegativeButton("아니오", (dialog, which) -> {
                        // 새로운 닉네임 입력
                        promptNewNickname(partyId);
                    })
                    .show();
        } else {
            // 저장된 닉네임이 없는 경우, 새 닉네임 입력
            promptNewNickname(partyId);
        }
    }

    private void promptNewNickname(int partyId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("새 닉네임 입력");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("확인", (dialog, which) -> {
            String newNickname = input.getText().toString();
            SharedPreferences.Editor editor = requireActivity().getSharedPreferences("ChatApp", Context.MODE_PRIVATE).edit();
            editor.putString("nickname", newNickname);
            editor.apply();

            openChatRoom(partyId, newNickname);
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
