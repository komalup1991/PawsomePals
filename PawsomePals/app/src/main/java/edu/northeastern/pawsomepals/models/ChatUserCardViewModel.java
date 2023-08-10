package edu.northeastern.pawsomepals.models;

public class ChatUserCardViewModel {
    String userId;
    String userImg;
    String userName;

    public ChatUserCardViewModel(String userId,String userImg, String userName) {
        this.userId = userId;
        this.userImg = userImg;
        this.userName = userName;
    }

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }
    public String getUserId() {
        return  userId;
    }
}
