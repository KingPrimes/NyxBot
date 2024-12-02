package com.nyx.bot.utils.http;

import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.MarketFormEnums;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpUtils {

    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    public static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    public static final MediaType MEDIA_TYPE_GIF = MediaType.parse("image/gif");
    public static final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml");
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
    public static final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain");
    public static final String CONTENT_TYPE_FORM_DATA = "multipart/form-data";

    private static final OkHttpClient client = new OkHttpClient().newBuilder()
            .addInterceptor(new BrotliInterceptor())
            //调用超时
            .callTimeout(60, TimeUnit.SECONDS)
            //链接超时
            .connectTimeout(30, TimeUnit.SECONDS)
            //读取超时
            .readTimeout(30, TimeUnit.SECONDS)
            .build();


    public static Body sendGet(String url) {
        return sendGet(url, "");
    }

    public static Body sendGet(String url, String param) {
        return sendGet(url, param, Headers.of("*", "*").newBuilder());
    }

    public static Body sendGet(String url, Headers headers) {
        return sendGet(url, "", headers.newBuilder());
    }

    public static Body marketSendGet(String url, String param) {
        return marketSendGet(url, param, MarketFormEnums.PC);
    }

    public static Body marketSendGet(String url, String param, MarketFormEnums form) {
        Headers.Builder h = new Headers.Builder();
        h.add("Accept", "*/*");
        h.add("Content-Type", "application/json;charset=utf-8");
        h.add("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        h.add("Cache-Control", "no-cache");
        h.add("Language", "en");
        h.add("Platform", form.getForm());
        h.add("Origin", "https://warframe.market");
        h.add("Referer", "https://warframe.market/");
        h.add("Pragma", "no-cache");
        return sendGet(url, param, h);
    }


    /**
     * Http Get请求
     *
     * @param url     请求地址
     * @param param   请求参数
     * @param headers 请求头
     * @return 返回的文本
     */
    public static Body sendGet(String url, String param, Headers.Builder headers) {
        try (

                Response response = client.newCall(send(url, param, headers)).execute()
        ) {
            //返回体
            Body body = getBody(response);
            log.debug("Url：{}，Param:{} TakeTime：{}ms", url, param, body.getTakeTime());
            return body;
        } catch (IOException e) {
            log.error(e.getMessage());
            return new Body(HttpCodeEnum.ERROR);
        }
    }

    public static Body sendPost(String url, String json) {
        RequestBody requestBody = RequestBody.create(json, MEDIA_TYPE_JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("【调用HTTP请求异常】 code:{},message:{}", response.code(), response.message());
                return new Body(HttpCodeEnum.ERROR);
            }
            Body body = getBody(response);
            log.debug("Url：{}，TakeTime：{}", url, body.getTakeTime());
            return body;
        } catch (IOException e) {
            log.error(e.getMessage());
            return new Body(HttpCodeEnum.ERROR);
        }
    }

    @NotNull
    private static Body getBody(Response response) {
        Body body = new Body();
        Optional.ofNullable(response.body()).ifPresentOrElse(r -> {
            //响应体
            try {
                body.setBody(r.string());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //响应code
            body.setCode(HttpCodeEnum.getCode(response.code()));
            //响应头
            body.setHeaders(response.headers());
            //响应时间
            body.setTakeTime(response.receivedResponseAtMillis() - response.sentRequestAtMillis());

            response.close();
        }, () -> body.setCode(HttpCodeEnum.getCode(response.code())));
        response.close();
        return body;
    }

    //构造请求
    private static Request send(String url, String param, Headers.Builder headers) {
        String urlNameString;
        if (!param.isEmpty()) {
            urlNameString = url + "?" + param;
        } else {
            urlNameString = url;
        }

        if (headers == null) {
            return new Request.Builder()
                    .url(urlNameString)
                    .get()
                    .build();

        }

        return new Request.Builder()
                .url(urlNameString)
                .get()
                .headers(headers.build())
                .build();

    }

    /**
     * 根据URL网址获取文件
     *
     * @param url - url
     * @return byte[] 文件
     */
    public static Body sendGetForFile(String url) {
        log.debug("发送请求 Url:{}", url);
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response;
        try {
            response = new OkHttpClient().newCall(req).execute();
            if (!response.isSuccessful()) {
                log.error("文件下载异常： code:{},headers:{},message:{}", response.code(), response.headers(), response.message());
                return null;
            }
            Body body = getBodyForFile(response);
            log.debug("Url：{}，TakeTime：{}", url, body.getTakeTime());
            return body;
        } catch (Exception e) {
            log.error("发起请求出现异常:", e);
            return new Body(HttpCodeEnum.ERROR);
        }
    }

    /**
     * 发送Post请求 获取文件
     *
     * @param url  - url
     * @param json Json格式的请求参数
     * @return byte[] 文件
     */
    public static Body sendPostForFile(String url, String json) {
        Response response;
        try {
            RequestBody requestBody = RequestBody.create(json, MEDIA_TYPE_JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            response = new OkHttpClient().newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("【调用HTTP请求异常】 code:{},message:{}", response.code(), response.message());
                return new Body(HttpCodeEnum.ERROR);
            }
            Body body = getBodyForFile(response);
            log.debug("Url：{}，TakeTime：{}", url, body.getTakeTime());
            return body;

        } catch (IOException var13) {
            log.error("发起请求出现异常:{}", var13.getMessage());
            return new Body(HttpCodeEnum.ERROR);
        }
    }

    @NotNull
    private static Body getBodyForFile(Response response) throws IOException {
        InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
        Body body = new Body(inputToByte(inputStream, response.body().contentLength()), HttpCodeEnum.getCode(response.code()), response.headers());
        body.setTakeTime(response.receivedResponseAtMillis() - response.sentRequestAtMillis());
        return body;
    }

    private static byte[] inputToByte(InputStream inputStream, long lines) throws IOException {
        //创建一个字节数组输出流
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        //创建一个字节数组
        byte[] buff = new byte[1024];
        long downloadLength = 0;
        //创建一个输入流
        int rc;
        double tip = 0.0;
        //循环读取字节数组
        while ((rc = inputStream.read(buff, 0, 1024)) > 0) {
            //将读取的字节数组写入字节数组输出流
            swapStream.write(buff, 0, rc);
            //累加已下载的长度
            downloadLength += rc;
            //计算下载进度
            double progress = Math.round(downloadLength * 100.0 / lines);
            //如果下载进度大于提示进度
            if (tip < progress) {
                tip = progress;
                log.info("文件下载进度: {}%", tip);
            }

        }
        //返回字节数组输出流
        return swapStream.toByteArray();
    }

    @Data
    public static class Body {
        String body;
        byte[] file;
        HttpCodeEnum code;
        Headers headers;
        Long takeTime;

        public Body() {
        }

        public Body(HttpCodeEnum code) {
            this.code = code;
        }

        public Body(String body, HttpCodeEnum code) {
            this.body = body;
            this.code = code;
        }

        public Body(byte[] file, HttpCodeEnum code) {
            this.file = file;
            this.code = code;
        }

        public Body(String body, HttpCodeEnum code, Headers headers) {
            this.body = body;
            this.code = code;
            this.headers = headers;
        }

        public Body(byte[] file, HttpCodeEnum code, Headers headers) {
            this.file = file;
            this.code = code;
            this.headers = headers;
        }
    }

}
