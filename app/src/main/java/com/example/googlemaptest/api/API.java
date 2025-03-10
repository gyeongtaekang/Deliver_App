package com.example.googlemaptest.api;

import android.util.Log;

import com.example.googlemaptest.task.DeleteTask;
import com.example.googlemaptest.task.GetTask;
import com.example.googlemaptest.task.PostTask;
import com.example.googlemaptest.task.PutTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class API {
    /**
     * 서버에서 파티 목록을 가져옵니다.
     *
     * @return 파티 목록 (ArrayList<Party>)
     */
    public static ArrayList<Party> getPartyList(){
        ArrayList<Party> list = new ArrayList<>();
        String msg;
        try {
            msg = new GetTask().execute().get();
            Log.d("API", "Response from GetTask: " + msg); // 로그 추가
        } catch (ExecutionException | InterruptedException exception) {
            Log.e("API", "Error in GetTask: " + exception.getMessage());
            return list;
        }
        try {
            JSONObject jo = new JSONObject(msg);
            if (!jo.getBoolean("success")) return list;
            JSONArray result = jo.getJSONArray("result");
            for (int i = 0; i < result.length(); i++){
                JSONObject obj = result.getJSONObject(i);
                Party pt = new Party();
                pt.setId(obj.getInt("id"));
                pt.setName(obj.getString("name"));
                pt.setCreaterEmail(obj.getString("creater_email"));
                pt.setCreatedDate(obj.getString("created_date"));
                pt.setMenu(obj.getString("menu"));
                pt.setRestaurant(obj.getString("restaurant"));
                pt.setMarker_password(obj.getString("marker_password"));
                pt.setDeliveryLat(obj.getString("delivery_lat"));
                pt.setDeliveryLon(obj.getString("delivery_lon"));
                pt.setKakaotalkChatLink(obj.getString("kakaotalk_chat_link"));
                pt.setDescription(obj.getString("description"));
                pt.setRecruitNumber(obj.getInt("recruit_number"));
                pt.setCurrentRecruitNumber(obj.getInt("current_recruit_number"));

                Log.d("API", "Party id: " + pt.getId() + ", Recruit number: " + pt.getRecruitNumber()); // 로그 추가

                list.add(pt);
            }
        } catch (JSONException exception) {
            Log.e("API", exception.getMessage());
            return list;
        }
        return list;
    }

    public static boolean updateParty(int id, Party party) {
        JSONObject ob = new JSONObject();
        try {
            ob.put("name", party.getName());
            ob.put("creater_email", party.getCreaterEmail());
            ob.put("menu", party.getMenu());
            ob.put("restaurant", party.getRestaurant());
            ob.put("marker_password", party.getMarker_password());
            ob.put("delivery_lat", party.getDeliveryLat());
            ob.put("delivery_lon", party.getDeliveryLon());
            ob.put("kakaotalk_chat_link", party.getKakaotalkChatLink());
            ob.put("description", party.getDescription());
            ob.put("recruit_number", party.getRecruitNumber());
            ob.put("current_recruit_number", party.getCurrentRecruitNumber());
        } catch (JSONException exception) {
            Log.e("API", exception.getMessage());
            return false;
        }

        String msg;
        try {
            msg = new PutTask(id, ob).execute().get();
        } catch (ExecutionException | InterruptedException exception) {
            Log.e("API", exception.getMessage());
            return false;
        }

        return true;
    }

    /**
     * 서버에 파티 정보를 추가합니다.
     *
     * @param name                  파티 이름
     * @param creater_email         파티 생성 유저 이메일
     * @param menu                  메뉴 카테고리 (치킨, 피자 등)
     * @param restaurant            식당 이름
     * @param marker_password       마커 비밀번호
     * @param delivery_lat          마커 좌표 (Latitude)
     * @param delivery_lon          마커 좌표 (Longitude)
     * @param kakaotalk_chat_link   오픈채팅 링크
     * @return 추가한 파티의 데이터베이스에서의 ID (오류가 발생한 경우 -1)
     */
    // 마커 생성 시 SeekBar에서 받은 모집 인원수와 함께 서버에 파티 정보를 추가하는 메소드
    public static int putParty(String name, String creater_email, String menu, String restaurant, String marker_password, String delivery_lat, String delivery_lon, String kakaotalk_chat_link, String description, int recruitNumber, int currentRecruitNumber){
        JSONObject ob = new JSONObject();
        try {
            ob.put("name", name);
            ob.put("creater_email", creater_email);
            ob.put("menu", menu);
            ob.put("restaurant", restaurant);
            ob.put("marker_password", marker_password);
            ob.put("delivery_lat", delivery_lat);
            ob.put("delivery_lon", delivery_lon);
            ob.put("kakaotalk_chat_link", kakaotalk_chat_link);
            ob.put("description", description);
            ob.put("recruit_number", recruitNumber); // 인원수 정보 추가
            ob.put("current_recruit_number", currentRecruitNumber);

            Log.d("API", "Sending data to server: " + ob.toString()); // 서버로 전송되는 데이터

        } catch (JSONException exception) {
            Log.e("API", exception.getMessage());
            return -1;
        }

        String msg;
        try {
            msg = new PostTask(ob).execute().get();
        } catch (ExecutionException | InterruptedException exception) {
            Log.e("API", exception.getMessage());
            return -1;
        }

        int id;
        try {
            JSONObject jo = new JSONObject(msg);
            if (!jo.getBoolean("success")) return -1;
            id = jo.getJSONObject("result").getInt("insertId");
        } catch (JSONException exception) {
            Log.e("API", exception.getMessage());
            return -1;
        }
        return id;
    }

    /**
     * 파티를 서버에서 삭제합니다.
     *
     * @param id 데이터베이스에서의 ID
     * @return 삭제가 완료되었는지의 여부
     */
    public static boolean deleteParty(int id){
        String msg;
        try {
            msg = new DeleteTask(id).execute().get();
        } catch (ExecutionException | InterruptedException exception) {
            Log.e("API", exception.getMessage());
            return false;
        }

        try {
            JSONObject jo = new JSONObject(msg);
            if (!jo.getBoolean("success")) return false;
        } catch (JSONException exception) {
            Log.e("API", exception.getMessage());
            return false;
        }
        return true;
    }
}
