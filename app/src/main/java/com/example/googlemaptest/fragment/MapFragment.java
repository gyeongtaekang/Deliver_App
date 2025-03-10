package com.example.googlemaptest.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;

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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;


public class MapFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private ArrayList<Marker> markers;
    private Marker temporaryMarker;
    private Bundle savedInstanceState;
    private String userGrade; // 사용자 등급을 저장하는 변수
    private int markerCreateCount; // 생성된 마커 수를 추적하는 변수
    public MapFragment() {
        // 필요한 생성자 구현
    }

    private Party getPartyById(int partyId) {
        // partyId에 해당하는 Party 정보를 가져와 반환하는 로직 작성
        // 여기에서 실제로 데이터베이스 또는 다른 데이터 소스에서 Party 정보를 조회하고 반환하는 코드를 작성해야 합니다.
        Party party = new Party();
        party.setId(partyId);
        party.setName("파티 이름");
        party.setMenu("음식 종류");
        party.setRestaurant("레스토랑");
        // 필요한 다른 정보들도 설정합니다.

        return party;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        markers = new ArrayList<>();
        loadUserGradeAndMarkerCount();
    }
    private void loadUserGradeAndMarkerCount() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userGrade = sharedPreferences.getString("userGrade", "Bronze"); // 기본값은 "Bronze"
        markerCreateCount = sharedPreferences.getInt("markerCreateCount", 0);
        Log.d("MapFragment", "Loaded userGrade: " + userGrade + ", markerCreateCount: " + markerCreateCount);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = root.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return root;
    }
    public void reloadMap() {
        onMapReady(map);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MyApplication.LOCATION_REQUEST_CODE);
            return;
        }

        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

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
                currentLocation = location;
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            }
        });

        map.setOnMapClickListener(clickedLatLng -> {
            Log.d("MapFragment", "Map clicked at: " + clickedLatLng.toString());
            View root = LayoutInflater.from(requireActivity()).inflate(R.layout.marker_creation_dialog, null);

            EditText nameInput = root.findViewById(R.id.nameInput);
            EditText restaurantInput = root.findViewById(R.id.restaurantInput);
            EditText passwordInput = root.findViewById(R.id.passwordInput);
//            EditText chatLinkInput = root.findViewById(R.id.chatLinkInput);
            EditText descriptionInput = root.findViewById(R.id.descriptionInput);
            Spinner spinner = root.findViewById(R.id.menuSpinner);
            SeekBar seekBar = root.findViewById(R.id.seekBar);
            TextView seekBarValueText = root.findViewById(R.id.seekBarValue);
            MapView dialogMapView = root.findViewById(R.id.mapView);

            Drawable thumb = ContextCompat.getDrawable(getContext(), R.drawable.de);
            seekBar.setThumb(thumb);

            // SeekBar 리스너 설정
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
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

            AtomicReference<LatLng> finalMarkerPosition = new AtomicReference<>(clickedLatLng);

            dialogMapView.getMapAsync(gMap -> {
                MarkerOptions initialMarkerOptions = new MarkerOptions().position(clickedLatLng);
                temporaryMarker = gMap.addMarker(initialMarkerOptions);

                // 클릭한 위치로 카메라를 이동시킵니다.
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(clickedLatLng, 15f));
                gMap.setOnMapClickListener(clickedLatlng -> {
                    // 'gMap'에서 클릭한 위치로 마커를 이동시킵니다.
                    if (temporaryMarker != null) {
                        temporaryMarker.remove();
                    }
                    MarkerOptions markerOptions = new MarkerOptions().position(clickedLatlng);
                    temporaryMarker = gMap.addMarker(markerOptions);

                    // 클릭한 위치로 카메라를 이동시킵니다.
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(clickedLatlng, gMap.getCameraPosition().zoom));
                    // 마커의 최종 위치를 저장합니다.
                    finalMarkerPosition.set(clickedLatlng);
                });

                gMap.getUiSettings().setZoomControlsEnabled(true);
            });

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    requireActivity(),
                    R.array.food_options_for_creation,
                    android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            AlertDialog alertDialog = new AlertDialog.Builder(requireActivity())
                    .setTitle("마커 정보 입력")
                    .setView(root)
                    .setPositiveButton("저장", (dialog, which) -> {
                        String name = nameInput.getText().toString().trim();
                        String restaurant = restaurantInput.getText().toString().trim();
                        String password = passwordInput.getText().toString().trim();
//                        String chatLink = chatLinkInput.getText().toString().trim();
                        String description = descriptionInput.getText().toString().trim();
                        String selectedCategory = spinner.getSelectedItem().toString();

                        int selectedValue = seekBar.getProgress() + 1; // SeekBar에서 선택한 값

                        // Party 객체에 모집 인원 설정
                        Party newParty = new Party();
                        newParty.setRecruitNumber(selectedValue);

                        // 스페셜 마커 선택 및 사용자 등급이 프리미엄이 아닌 경우
                        if ("스페셜".equals(selectedCategory) && !"Premium".equals(userGrade)) {
                            Toast.makeText(requireActivity(), "스페셜 마커는 프리미엄 등급에서만 생성 가능합니다.", Toast.LENGTH_SHORT).show();
                            return; // 여기서 함수 종료
                        }
                        // 마커 생성 제한을 체크합니다.
                        if (markerCreateCount >= getMaxMarkerCountForGrade(userGrade)) {
                            Toast.makeText(requireActivity(), "마커 생성 한도에 도달했습니다.", Toast.LENGTH_SHORT).show();
                            return; // 여기서 함수를 종료합니다.
                        }

                        if (temporaryMarker != null) {
                            LatLng latLng = temporaryMarker.getPosition();
                            Log.d("MapFragment", "Attempting to create marker at: " + latLng.toString());
                            tryCreateMarker(latLng, selectedCategory);
                            // 여기에서 마커 생성 제한을 체크합니다.

                            if (markerCreateCount < getMaxMarkerCountForGrade(userGrade)) {
                                createMarker(latLng, selectedCategory);
                                markerCreateCount++; // 마커 생성 카운트 증가
                                updateMarkerCreateCount(); // 새 카운트를 저장
                            } else {
                               /* Toast.makeText(requireActivity(), "마커 생성 한도에 도달했습니다.", Toast.LENGTH_SHORT).show();*/
                            }
                        }

                        if (name.isEmpty() || restaurant.isEmpty() || password.isEmpty() ) {
                            Toast.makeText(requireActivity(), "모든 정보를 입력해주세요.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        String creator_email = "test@nomail.com"; // 예시 이메일
                        String chatLink="a";
                        int partyId = API.putParty(name, creator_email, selectedCategory, restaurant, password, String.valueOf(finalMarkerPosition.get().latitude), String.valueOf(finalMarkerPosition.get().longitude), chatLink, description, selectedValue, 0);

                        if (partyId != -1) {
                            Log.d("MapFragment", "Creating chat room for party ID: " + partyId);
                            createChatRoom(partyId, newParty.getName());
                        }
                        else{
                            Log.e("MapFragment", "Failed to create party on server");
                        }
                        MyApplication.reloadPartyMap();
                        drawMarkers();
                    })
                    .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                    .create();

            alertDialog.show();
            dialogMapView.onCreate(savedInstanceState);
        });

        map.setOnInfoWindowClickListener(marker -> {
            Party party = getParty(marker);
            if (party != null) {
                MarkerInfoDialogFragment fragment = new MarkerInfoDialogFragment(party.getId());
                fragment.show(requireActivity().getSupportFragmentManager(), "marker_info_dialog");
                fragment.setListener(() -> {
                    MyApplication.reloadPartyMap();
                    drawMarkers();
                });
            }
        });

        map.getUiSettings().setZoomControlsEnabled(true);
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        drawMarkers();
    }

    private void tryCreateMarker(LatLng position, String menuCategory) {
        Log.d("MapFragment", "createMarker 호출됨: " + position + ", " + menuCategory);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int currentMarkerCount = sharedPreferences.getInt("markerCreateCount", 0);
        int maxMarkerCount = getMaxMarkerCountForGrade(userGrade);
        Log.d("MapFragment", "Trying to create marker. Current count: " + markerCreateCount + ", Max count: " + maxMarkerCount);

        if (markerCreateCount >= maxMarkerCount) {
            Log.d("MapFragment", "Marker creation limit reached for grade: " + userGrade);
            Toast.makeText(requireActivity(), "마커 생성 한도에 도달했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("스페셜".equals(menuCategory)) {
            if (!"Premium".equals(userGrade)) {
                Toast.makeText(requireActivity(), "스페셜 마커는 프리미엄 등급에서만 생성 가능합니다.", Toast.LENGTH_SHORT).show();
                return; // 여기서 함수를 종료합니다.
            }
            // 프리미엄 사용자일 경우에만 스페셜 마커 생성
            createMarker(position, menuCategory);
        } else {
            // 일반 마커 생성 로직
            createMarker(position, menuCategory);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("markerCreateCount", ++markerCreateCount);
        editor.apply();
        Log.d("MapFragment", "Marker create count incremented: " + currentMarkerCount);

        updateMarkerCreateCount();
    }

    // 사용자 등급에 따른 최대 마커 생성 횟수 반환
    private int getMaxMarkerCountForGrade(String grade) {
        if ("Bronze".equals(grade)) {
            return 2;
        } else if ("Silver".equals(grade)) {
            return 5;
        } else if ("Premium".equals(grade)) {
            return Integer.MAX_VALUE;
        } else {
            return 0;
        }
    }

    private void updateMarkerCreateCount() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("markerCreateCount", markerCreateCount);
        editor.apply();
        Log.d("MapFragment", "Saved new marker create count: " + markerCreateCount);
    }
    public void changeUserGrade(String newGrade) {
        Log.d("MapFragment", "Changing user grade to: " + newGrade);
        userGrade = newGrade;
        resetMarkerCreateCount();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userGrade", newGrade);
        editor.apply();
    }

    private void resetMarkerCreateCount() {
        markerCreateCount = 0;
        Log.d("MapFragment", "Marker create count reset to 0");
        updateMarkerCreateCount();
    }
    private void createMarker(LatLng position, String menuCategory) {
        Log.d("MapFragment", "createMarker 호출됨: " + position + ", " + menuCategory);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(position);
        markerOptions.title("새 마커"); // 예시 제목
        markerOptions.snippet("마커 상세 정보"); // 예시 상세 정보

        // menuCategory를 사용하여 마커의 아이콘 또는 다른 특성을 설정
        BitmapDescriptor icon = getIconForParty(menuCategory);
        markerOptions.icon(icon);

        Marker newMarker = map.addMarker(markerOptions);
        if (newMarker != null) {
            markers.add(newMarker);
            Log.d("MapFragment", "Marker successfully added to map");
            drawMarkers(); // drawMarkers 호출하여 마커를 지도에 반영
        } else {
            Log.e("MapFragment", "Failed to add marker to map");
        }
    }



    private void createChatRoom(int partyId, String name) {
        // Firebase Database에 채팅방 생성
        DatabaseReference chatRoomRef = FirebaseDatabase.getInstance().getReference("chatrooms").child(String.valueOf(partyId));
        ChatRoom chatRoom = new ChatRoom(name); // ChatRoom은 채팅방 정보를 담는 클래스
        chatRoomRef.setValue(chatRoom);
    }

        private void addMarkerToMap(LatLng latlng, String title, int partyId) {
            // 지도에 마커 추가
            MarkerOptions markerOptions = new MarkerOptions().position(latlng).title(title);
            Marker marker = map.addMarker(markerOptions);
            marker.setTag(partyId); // 마커에 파티 ID 저장
        }

        private void openChatRoom(int partyId) {
            // 새로운 채팅 화면으로 이동합니다.
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("partyId", partyId);
            startActivity(intent);
        }

    public void drawMarkers() {
        if (map == null) {
            Log.e("MapFragment", "Map is not ready yet");
            return;
        }

        // 기존에 있던 마커들을 지도에서 삭제
        map.clear();
        markers.clear();  // 리스트 초기화

        for (int id : MyApplication.getPartyMap().keySet()) {
            Party party = MyApplication.getPartyMap().get(id);
            try {
                double lat = Double.parseDouble(party.getDeliveryLat());
                double lon = Double.parseDouble(party.getDeliveryLon());
                LatLng position = new LatLng(lat, lon);

                BitmapDescriptor icon = getIconForParty(party.getMenu());

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(party.getName())
                        .snippet(party.getRestaurant())
                        .icon(icon); // 아이콘 설정

                Marker marker = map.addMarker(markerOptions);

                // 마커에 태그로 추가 정보 저장
                HashMap<String, String> markerInfo = new HashMap<>();
                markerInfo.put("id", String.valueOf(party.getId()));
                marker.setTag(markerInfo);

                // 생성된 마커를 리스트에 추가
                markers.add(marker);
            } catch (NumberFormatException e) {
                Log.e("loadMarkers", "Invalid number format", e);
            }
        }
    }


    private BitmapDescriptor getIconForParty(String menuCategory) {
        BitmapDescriptor icon; // 지역 변수 선언

        if (menuCategory == null) {
            icon = BitmapDescriptorFactory.defaultMarker(); // 기본 마커
        } else if (menuCategory.equals("치킨")) {
            icon = getBitmapDescriptor(R.drawable.chicken);
        } else if (menuCategory.equals("피자")) {
            icon = getBitmapDescriptor(R.drawable.pizza);
        } else if (menuCategory.equals("족발")) {
            icon = getBitmapDescriptor(R.drawable.jokbal);
        } else if (menuCategory.equals("디저트")) {
            icon = getBitmapDescriptor(R.drawable.dissert);
        }
        else if (menuCategory.equals("기타 음식")) {
            icon = getBitmapDescriptor(R.drawable.gita);
        }
        else if (menuCategory.equals("스페셜")) {
            icon = getBitmapDescriptor(R.drawable.specail);
        }else {
            icon = BitmapDescriptorFactory.defaultMarker(); // 그 외 기본 마커
        }

        return icon; // 최종 아이콘 반환
    }

    private BitmapDescriptor getBitmapDescriptor(int id) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
        int width = (int) ((float) height * bitmap.getWidth() / bitmap.getHeight());
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }
    public void filterMarkers(String foodCategory, int range, int participantCount) {
        for (Marker marker : markers) {
            Party party = getParty(marker);
            if (party != null) {
                String category = party.getMenu();
                LatLng markerLatLng = marker.getPosition();
                Location markerLocation = new Location("");
                markerLocation.setLatitude(markerLatLng.latitude);
                markerLocation.setLongitude(markerLatLng.longitude);

                float distance = currentLocation.distanceTo(markerLocation);
                boolean matchesCategory = foodCategory.equals("모두 보기") || category.equals(foodCategory);
                boolean withinDistance = distance <= range * 10; // 'range * 10'을 조정하여 거리 범위를 변경할 수 있습니다.
                boolean withinParticipantCount = party.getRecruitNumber() <= participantCount;

                marker.setVisible(matchesCategory && withinDistance && withinParticipantCount);
            }
        }
    }

    private Party getParty(Marker marker) {
        HashMap<String, String> markerInfo = (HashMap<String, String>) marker.getTag();
        if (markerInfo != null) {
            String idString = markerInfo.get("id");
            if (idString == null) return null;
            int id;
            try {
                id = Integer.parseInt(idString);
            } catch (NumberFormatException exception) {
                return null;
            }
            if (!MyApplication.getPartyMap().containsKey(id)) {
                return null;
            }
            return MyApplication.getPartyMap().get(id);
        }
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        refreshMarkerCreateCount();
    }

    private void refreshMarkerCreateCount() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        markerCreateCount = sharedPreferences.getInt("markerCreateCount", 0);
        Log.d("MapFragment", "Refreshed markerCreateCount: " + markerCreateCount);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}