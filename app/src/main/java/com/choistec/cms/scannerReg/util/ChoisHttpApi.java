package com.choistec.cms.scannerReg.util;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ChoisHttpApi {
//    @GET('/posts')
//    Call<List<Post>> getData(@Query("userId") String id);

    @FormUrlEncoded
    @POST("/post_page")
    Call<tokenModel> postData(@FieldMap HashMap<String, Object> param);
}


