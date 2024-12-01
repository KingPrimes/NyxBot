package com.nyx.bot.utils.http;

import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Http请求解压
 */
public class BrotliInterceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        //判断是否指定压缩格式
        if (chain.request().header("Accept-Encoding") == null) {
            //添加压缩格式
            Request request = chain.request().newBuilder()
                    .header("Accept-Encoding", "br,gzip")
                    .build();
            Response proceed = chain.proceed(request);
            return uncompressed(proceed);
        } else {
            return chain.proceed(chain.request());
        }
    }

    //处理压缩格式
    public Response uncompressed(Response response) throws IOException {
        if (!HttpHeaders.promisesBody(response)) {
            return response;
        }
        ResponseBody body = response.body();
        if (body == null) {
            return response;
        }
        String encoding = response.header("Content-Encoding");
        if (encoding == null) {
            return response;
        }
        BufferedSource decompressedSource;
        switch (encoding) {
            case "br" -> //Brotli压缩格式解压
                    decompressedSource = Okio.buffer(Okio.source(new BrotliInputStream(body.source().inputStream())));
            case "gzip" -> //gzip压缩格式解压
                    decompressedSource = Okio.buffer(new GzipSource(body.source()));
            default -> {
                //默认不处理
                return response;
            }
        }
        //返回解压后的数据
        return response.newBuilder()
                .body(ResponseBody.create(decompressedSource, body.contentType(), -1))
                .build();

    }
}
