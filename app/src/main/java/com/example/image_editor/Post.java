package com.example.image_editor;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Query;

class Post {
    @SerializedName("base64")
    @Expose
    private String base64;

    String getBase64() {
        return base64;
    }

}