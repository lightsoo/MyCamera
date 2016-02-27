package com.mycamera.lightsoo.mycamera.RestAPI;

import com.mycamera.lightsoo.mycamera.Data.Files;
import com.squareup.okhttp.RequestBody;

import retrofit.Call;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;

/**
 * Created by LG on 2016-02-27.
 */
public interface FileAPI {
    @Multipart
    @POST("/upload")
    Call<Files> upload(@Part("img\"; filename=\"image.jpg\" ")RequestBody file1,
                        @Part("title") String title,
                        @Part("director") String director,
                        @Part("year") int year);



}
