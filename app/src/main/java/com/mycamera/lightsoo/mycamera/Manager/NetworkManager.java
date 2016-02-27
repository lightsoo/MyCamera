package com.mycamera.lightsoo.mycamera.Manager;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.io.IOException;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by LG on 2016-02-27.
 */
public class NetworkManager {
    private static final String serverURL ="http://192.168.0.17:3000/";
    Retrofit client;

    private NetworkManager(){

        //Retrofit설정, req를 인터셉트한다
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                return response;
            }
        });


        client = new Retrofit.Builder()
                .baseUrl(serverURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }

    //싱글톤 패턴, 프로그램 종료시점까지 하나의 인스턴스만을 생성해서 관리한다.
    public static class InstanceHolder{
        public static final NetworkManager INSTANCE = new NetworkManager();
    }
    public static NetworkManager getInstance(){return InstanceHolder.INSTANCE;}
    //나의 restAPI를 호출
    public <T> T getAPI(Class<T> serviceClass){
        return client.create(serviceClass);
    }



}
