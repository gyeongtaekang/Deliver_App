package com.example.googlemaptest.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlemaptest.MyApplication;
import com.example.googlemaptest.R;
import com.example.googlemaptest.activity.ChatActivity;
import com.example.googlemaptest.api.API;
import com.example.googlemaptest.api.Party;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import com.google.android.material.tabs.TabLayout;

public class ListFragment extends Fragment {
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private LatLng markerPosition;
    private TextView currentLocationTextView;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private GoogleMap map;
    private Context context;
    private ArrayList<Party> partyList;
    private Handler repeatHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        repeatHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);

        currentLocationTextView = root.findViewById(R.id.currentLocationTextView);
        TabLayout tabLayout = root.findViewById(R.id.tabLayout);

        // 탭 선택 리스너 설정
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterByCategory(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 필요한 경우 여기에 코드 추가
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 필요한 경우 여기에 코드 추가
            }
        });

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.setAdapter(new PartyAdapter(requireActivity(), new ArrayList<>(), currentLocation)); // 초기 어댑터 설정

        floatingActionButton = root.findViewById(R.id.floatingButton);
        floatingActionButton.setOnClickListener(view -> {
            LayoutInflater dialog_inflater = LayoutInflater.from(requireActivity());
            View root_dialog = dialog_inflater.inflate(R.layout.marker_creation_dialog, null);

            SeekBar seekBar = root_dialog.findViewById(R.id.seekBar);
            TextView seekBarValueText = root_dialog.findViewById(R.id.seekBarValue); // SeekBar 값을 표시할 TextView

// SeekBar 리스너 설정
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // SeekBar 값이 변경될 때 TextView 업데이트
                    seekBarValueText.setText("모집 인원: " + (progress + 1) + "명");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // 추적 시작 시 필요한 경우 코드 추가
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // 추적 종료 시 필요한 경우 코드 추가
                }
            });

            MapView mapView = root_dialog.findViewById(R.id.mapView);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(googleMap -> {
                map = googleMap;
                // GoogleMap이 준비되면 실행되는 콜백

                // 지도 클릭 이벤트 처리
                map.setOnMapClickListener(latLng -> {
                    // 마커 위치 변경
                    if (currentLocation != null) {
                        LatLng newMarkerPosition = new LatLng(latLng.latitude, latLng.longitude);
                        MarkerOptions newMarkerOptions = new MarkerOptions().position(newMarkerPosition);
                        map.clear(); // 기존 마커 제거
                        map.addMarker(newMarkerOptions); // 새로운 마커 추가
                        // 카메라도 이동
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(newMarkerOptions.getPosition(), googleMap.getCameraPosition().zoom));
                        map.getUiSettings().setZoomControlsEnabled(true);

                        // 마커 위치 업데이트
                        markerPosition = newMarkerPosition;
                    }
                });

                // 지도 초기 설정
                if (currentLocation != null) {
                    LatLng currentPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 15f));
                }

                // 내 위치에 마커 추가
                if (currentLocation != null) {
                    LatLng currentPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions().position(currentPosition);
                    map.addMarker(markerOptions);

                    // 초기 마커 위치 설정
                    markerPosition = currentPosition;
                }
            });

            EditText nameInput = root_dialog.findViewById(R.id.nameInput);
            EditText restaurantInput = root_dialog.findViewById(R.id.restaurantInput);
            EditText passwordInput = root_dialog.findViewById(R.id.passwordInput);
            /*EditText chatLinkInput = root_dialog.findViewById(R.id.chatLinkInput);*/
            EditText descritonInput = root_dialog.findViewById(R.id.descriptionInput);

            Spinner spinner = root_dialog.findViewById(R.id.menuSpinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    requireActivity(),
                    R.array.food_options_for_creation,
                    android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            androidx.appcompat.app.AlertDialog alertDialog = new AlertDialog.Builder(requireActivity())
                    .setTitle("마커 정보 입력")
                    .setPositiveButton("저장", (dialog, which) -> {
                        String name = nameInput.getText().toString().trim();
                        String restaurant = restaurantInput.getText().toString().trim();
                        String password = passwordInput.getText().toString().trim();
                        /*String chatLink = chatLinkInput.getText().toString().trim();*/
                        String description = descritonInput.getText().toString().trim();
                        String selectedCategory = spinner.getSelectedItem().toString();

                        if (name.isEmpty() ||
                                restaurant.isEmpty() ||
                                password.isEmpty() ) {
                            Toast.makeText(requireActivity(), "모든 정보를 입력해주세요.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String creater_email = "test@nomail.com";
                        LatLng position = markerPosition; // 마커 위치 정보 사용

                        int selectedValue = seekBar.getProgress() + 1; // SeekBar에서 선택한 값
                        String chatLink="a";

                        API.putParty(name, creater_email, selectedCategory, restaurant, password, String.valueOf(position.latitude), String.valueOf(position.longitude), chatLink, description, selectedValue, 0);
                        MyApplication.reloadPartyMap();
                        drawMarkers();
                    })
                    .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                    .setView(root_dialog)
                    .create();
            // 다이얼로그가 화면에 나타난 후에 맵을 표시하기 위해 다음 코드를 추가하세요.
            alertDialog.setOnShowListener(dialogInterface -> {
                // MapView가 초기화되었는지 확인하고 표시합니다.
                if (mapView != null) {
                    mapView.onResume();
                }
            });

            // 다이얼로그를 표시합니다.
            alertDialog.show();
        });

        loadCurrentLocation();
        reloadCurrentLocation();
        return root;
    }
    // 필터링 로직 구현
    private void filterByCategory(String category) {
        ArrayList<Party> filteredList = new ArrayList<>();
        for (Party party : MyApplication.getPartyMap().values()) {
            if (party.getMenu().equals(category) || category.equals("모두 보기")) {
                filteredList.add(party);
            }
        }
        updateRecyclerView(filteredList);
    }
    // 리사이클러뷰 업데이트 메소드
    private void updateRecyclerView(ArrayList<Party> partyList) {
        PartyAdapter partyAdapter = new PartyAdapter(requireActivity(), partyList, currentLocation);
        recyclerView.setAdapter(partyAdapter);
    }
    private void reloadCurrentLocation() {
        repeatHandler.postDelayed(() -> {
            if (isAdded()) { // 프래그먼트가 활동에 첨부된 경우에만 현재 위치를 다시 로드
                loadCurrentLocation();
                reloadCurrentLocation();
            }
        }, 60000); // 예를 들어 60초마다 반복
    }

    public void loadCurrentLocation() {
        // 현재 액티비티 참조를 가져오기 전에 프래그먼트가 활동에 첨부되었는지 확인
        if (!isAdded()) {
            return; // 프래그먼트가 활동에 첨부되어 있지 않으면 메서드를 더 이상 진행하지 않음
        }
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MyApplication.LOCATION_REQUEST_CODE);
            return;
        }
        FragmentActivity activity = requireActivity();

        // 위치 권한 확인 및 요청
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MyApplication.LOCATION_REQUEST_CODE);
            return;
        }
        Handler handler = new Handler();
        fusedLocationClient.getCurrentLocation(
                new CurrentLocationRequest.Builder()
                        .setDurationMillis(20000)
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .build(),
                new CancellationToken() {
                    @Override
                    public CancellationToken onCanceledRequested(OnTokenCanceledListener onTokenCanceledListener) {
                        return new CancellationTokenSource().getToken();
                    }

                    @Override
                    public boolean isCancellationRequested() {
                        return false;
                    }
                }
        ).addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                currentLocation = location; // 현재 위치 업데이트
                new Thread() {
                    public void run() {
                        Geocoder geocoder = new Geocoder(requireActivity(), Locale.getDefault());
                        List<Address> addresses = null;
                        String addressText = "";

                        try {
                            addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                            if (addresses != null && addresses.size() > 0) {
                                Address address = addresses.get(0);
                                addressText = address.getAddressLine(0);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String finalAddressText = addressText;
                        handler.post(() -> {
                            currentLocationTextView.setText(finalAddressText);
                        });
                    }
                }.start();
                drawMarkers();
            }
        });
    }
    public ArrayList<Party> getSortedPartyList() {
        ArrayList<Party> partyList = new ArrayList<>();
        partyList.addAll(MyApplication.getPartyMap().values());

        Collections.sort(partyList, (party1, party2) -> {
            float[] result1 = new float[1];
            float[] result2 = new float[1];

            try {
                double lat1 = party1.getDeliveryLat() != null ? Double.parseDouble(party1.getDeliveryLat()) : 0;
                double lon1 = party1.getDeliveryLon() != null ? Double.parseDouble(party1.getDeliveryLon()) : 0;
                double lat2 = party2.getDeliveryLat() != null ? Double.parseDouble(party2.getDeliveryLat()) : 0;
                double lon2 = party2.getDeliveryLon() != null ? Double.parseDouble(party2.getDeliveryLon()) : 0;

                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), lat1, lon1, result1);
                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), lat2, lon2, result2);
            } catch (NumberFormatException e) {
                // 적절한 오류 처리
            }
            return Float.compare(result1[0], result2[0]);
        });

        return partyList;
    }


    public void drawMarkers() {
        ArrayList<Party> partyList = getSortedPartyList();
        PartyAdapter partyAdapter = new PartyAdapter(requireActivity(), partyList, currentLocation);
        recyclerView.setAdapter(partyAdapter);
    }

    public void filterMarkers(String foodCategory, int range, int participantCount) {
        ArrayList<Party> filteredList = new ArrayList<>();
        for (Party party : MyApplication.getPartyMap().values()) {
            float[] results = new float[1];
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    Double.parseDouble(party.getDeliveryLat()), Double.parseDouble(party.getDeliveryLon()), results);
            float distanceInMeters = results[0];

            boolean matchesCategory = foodCategory.equals("모두 보기") || party.getMenu().equals(foodCategory);
            boolean withinDistance = distanceInMeters <= range * 10; // 'range * 10'을 조정하여 거리 범위를 변경할 수 있습니다.
            boolean withinParticipantCount = party.getRecruitNumber() <= participantCount;

            if (matchesCategory && withinDistance && withinParticipantCount) {
                filteredList.add(party);
            }
        }
        updateRecyclerView(filteredList);
    }


    public class PartyAdapter extends RecyclerView.Adapter<PartyViewHolder> {
        private Context context;
        private ArrayList<Party> partyList;
        private Location currentLocation;

        public PartyAdapter(Context context, ArrayList<Party> partyList, Location currentLocation) {
            this.context = context;
            this.partyList = partyList;
            this.currentLocation = currentLocation;
        }

        @Override
        public PartyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_party, parent, false);
            return new PartyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PartyViewHolder holder, int position) {
            Party party = partyList.get(position);
            holder.bind(party);

            holder.itemView.setOnClickListener(v -> {
                // MarkerInfoDialogFragment를 시작합니다.
                MarkerInfoDialogFragment dialogFragment = MarkerInfoDialogFragment.newInstance(party.getId());
                dialogFragment.show(((FragmentActivity)context).getSupportFragmentManager(), "marker_info");
            });
        }

        @Override
        public int getItemCount() {
            return partyList.size();
        }
    }
    public class PartyViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewMenu;
        private TextView textViewPartyName, textViewPartyDetails, textViewDistance;
        private Party currentParty; // 현재 Party 객체를 저장하기 위한 필드

        PartyViewHolder(View itemView) {
            super(itemView);
            imageViewMenu = itemView.findViewById(R.id.imageViewMenu);
            textViewPartyName = itemView.findViewById(R.id.textViewPartyName);
            textViewPartyDetails = itemView.findViewById(R.id.textViewPartyDetails);
            textViewDistance = itemView.findViewById(R.id.textViewDistance);

            itemView.setOnClickListener(v -> {
                // 새로운 대화상자나 액티비티를 여기서 시작합니다.
                // 예: PartyDetailDialogFragment 또는 PartyDetailActivity를 시작
                Intent intent = new Intent(context, MarkerInfoDialogFragment.class);
                intent.putExtra("partyId", currentParty.getId()); // 현재 파티 ID 전달
                context.startActivity(intent);
            });
        }

        void bind(Party party) {
            this.currentParty = party; // 현재 Party 객체 저장

            textViewPartyName.setText(party.getName());
            textViewPartyDetails.setText("식당: " + party.getRestaurant() + "\n" + "오픈채팅방: " + party.getKakaotalkChatLink());

            // 모집 인원수 및 거리 정보 표시
            if (textViewPartyDetails != null) {
                String partyDetails = "식당: " + party.getRestaurant() + "\n" +
                        /*"오픈채팅방: " + party.getKakaotalkChatLink() + "\n" +*/
                        "모집 인원: " + party.getRecruitNumber() + "명";
                textViewPartyDetails.setText(partyDetails);
            }

            if (currentLocation != null && party.getDeliveryLat() != null && party.getDeliveryLon() != null) {
                try {
                    double lat = Double.parseDouble(party.getDeliveryLat());
                    double lon = Double.parseDouble(party.getDeliveryLon());
                    float[] distanceResult = new float[1];
                    Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), lat, lon, distanceResult);
                    textViewDistance.setText(String.format("%.2f km", distanceResult[0] / 1000));
                } catch (NumberFormatException e) {
                    Log.e("ListFragment", "Invalid format for latitude or longitude", e);
                    textViewDistance.setText("거리 정보 없음");
                }
            } else {
                textViewDistance.setText("거리 정보 없음");
            }

            switch (party.getMenu()) {
                case "치킨":
                    imageViewMenu.setImageResource(R.drawable.chicken);
                    break;
                case "피자":
                    imageViewMenu.setImageResource(R.drawable.pizza);
                    break;
                case "족발":
                    imageViewMenu.setImageResource(R.drawable.jokbal);
                    break;
                case "디저트":
                    imageViewMenu.setImageResource(R.drawable.dissert);
                    break;
                case "기타 음식":
                    imageViewMenu.setImageResource(R.drawable.gita);
                    break;
                case "스페셜":
                    imageViewMenu.setImageResource(R.drawable.specail);
                    break;
                // 다른 메뉴에 대한 처리...
                default:
                    imageViewMenu.setImageResource(R.drawable.ic_launcher_background);
                    break;
            }
        }
    }
}
