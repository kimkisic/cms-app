package com.choistec.cms.scannerReg.util;

import com.google.gson.annotations.SerializedName;

public class tokenModel{
    @SerializedName("userId")
    String userId;
    @SerializedName("token")
    String token;
    tokenModel(String userId, String token){
        this.userId = userId;
        this.token = token;
    }
    public String getUserId(){
        return userId;
    }
    public String getToken(){
        return token;
    }
    public void setUserId(String userId){this.userId = userId;};
    public void setToken(String token){this.token = token;};
}
