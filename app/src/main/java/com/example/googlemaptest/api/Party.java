package com.example.googlemaptest.api;

public class Party {
    private int id;
    private String name,
            creater_email,
            created_date,
            menu,
            restaurant,
            marker_password,
            delivery_lat,
            delivery_lon,
            kakaotalk_chat_link,
            description;
    private int recruitNumber,
            currentRecruitNumber; // 모집인원을 위한 새로운 필드
    // 모집인원 필드를 위한 setter 메서드
    public void setRecruitNumber(int recruitNumber) {
        this.recruitNumber = recruitNumber;
    }

    // 모집인원 필드를 위한 getter 메서드
    public int getRecruitNumber() {
        return recruitNumber;
    }

    public void setCurrentRecruitNumber(int currentRecruitNumber) { this.currentRecruitNumber = currentRecruitNumber; }
    public int getCurrentRecruitNumber() { return currentRecruitNumber; }

    // Setter 메서드 추가
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreaterEmail(String creater_email) {
        this.creater_email = creater_email;
    }

    public void setCreatedDate(String created_date) {
        this.created_date = created_date;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }
    public void setMarker_password(String marker_password) {this.marker_password = marker_password;}

    public void setDeliveryLat(String delivery_lat) {
        this.delivery_lat = delivery_lat;
    }

    public void setDeliveryLon(String delivery_lon) {
        this.delivery_lon = delivery_lon;
    }

    public void setKakaotalkChatLink(String kakaotalk_chat_link) {
        this.kakaotalk_chat_link = kakaotalk_chat_link;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getter 메서드 추가
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreaterEmail() {
        return creater_email;
    }

    public String getCreatedDate() {
        return created_date;
    }

    public String getMenu() {
        return menu;
    }

    public String getRestaurant() {
        return restaurant;
    }
    public String getMarker_password() {return marker_password;}

    public String getDeliveryLat() {
        return delivery_lat;
    }

    public String getDeliveryLon() {
        return delivery_lon;
    }

    public String getKakaotalkChatLink() {
        return kakaotalk_chat_link;
    }

    public String getDescription() { return description; }

    // toString() 메서드 오버라이드
    @Override
    public String toString() {
        return "Party{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", creater_email='" + creater_email + '\'' +
                ", created_date='" + created_date + '\'' +
                ", menu='" + menu + '\'' +
                ", restaurant='" + restaurant + '\'' +
                ", marker_password='" + marker_password + '\'' +
                ", description='" + description + '\'' +
                ", delivery_lat='" + delivery_lat + '\'' +
                ", delivery_lon='" + delivery_lon + '\'' +
                ", kakaotalk_chat_link='" + kakaotalk_chat_link + '\'' +
                '}';
    }
}
