package com.choistec.cms.scannerReg.service;

import com.choistec.cms.scannerReg.util.ChoisHttpApi;
import com.choistec.cms.scannerReg.util.tokenModel;

import java.util.List;

//import io.reactivex.Single;
import retrofit2.Retrofit;
//import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserTokenService {
    private static final String BASE_URL = "http://cms2.choistec.com";

    private static UserTokenService instance;

//    public ChoisHttpApi api = new Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//            .build()
//            .create(ChoisHttpApi.class);

    public static UserTokenService getInstance(){
        if(instance == null){
            instance = new UserTokenService();
        }
        return instance;
    }

//    public Single<tokenModel> getUserToken(){
//        return api.getToken();
//    }

}
