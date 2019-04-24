package com.example.image_editor;

import android.app.DownloadManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface JSONPlaceHolderApi {
    @GET("mypost")
    Call<Post> getMyThing(@Query("mid") String param1);
}
