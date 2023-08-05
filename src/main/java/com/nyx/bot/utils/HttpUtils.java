package com.nyx.bot.utils;

import okhttp3.*;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class HttpUtils {
    private static final OkHttpClient client = new OkHttpClient();


    public static String sendGet(String url){
        return  sendGet(url,"");
    }

    public static String sendGet(String url, String param){
        return sendGet(url,param,null);
    }


    /**
     * Http Get请求
     * @param url 请求地址
     * @param param 请求参数
     * @param headers 请求头
     * @return 返回的文本
     */
    public static String sendGet(String url, String param, Headers headers) {

        try (Response response = client.newCall(send(url, param, headers)).execute()) {

            AtomicReference<String> tmp = new AtomicReference<>("");

            Optional.ofNullable(response.body().string()).ifPresentOrElse(r -> {
                tmp.set(r);
                response.close();
            },()->{
                tmp.set("timeout");
            });

            return tmp.get();
        } catch (IOException e) {
            return "error";
        }
    }

    private static Request send(String url, String param, Headers headers) {
        String urlNameString;
        if (!param.isEmpty()) {
            urlNameString = url + "?" + param;
        } else {
            urlNameString = url;
        }

        if(headers == null){
            return new Request.Builder()
                    .url(urlNameString)
                    .get()
                    .build();

        }

        return new Request.Builder()
                .url(urlNameString)
                .get()
                .headers(headers)
                .build();

    }

}
