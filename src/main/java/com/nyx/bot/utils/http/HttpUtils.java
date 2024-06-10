package com.nyx.bot.utils.http;

import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.MarketFormEnums;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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

    private static final X509TrustManager manager = HttpUtils.getX509TrustManager();
    private static final OkHttpClient client = new OkHttpClient().newBuilder()
            .addInterceptor(new BrotliInterceptor())
            //调用超时
            .callTimeout(30, TimeUnit.SECONDS)
            //链接超时
            .connectTimeout(30, TimeUnit.SECONDS)
            //读取超时
            .readTimeout(30, TimeUnit.SECONDS)
            //忽略SSL校验
            .sslSocketFactory(HttpUtils.getSocketFactory(manager), manager)
            //忽略校验
            .hostnameVerifier(HttpUtils.getHostnameVerifier())
            //连接池
            .connectionPool(new ConnectionPool(10, 20, TimeUnit.MINUTES))
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
        log.debug("发送请求\nUrl:{}\n参数:{}\n请求头:{}", url, param, headers.build());
        try (

                Response response = client.newCall(send(url, param, headers)).execute()
        ) {
            //返回体
            return getBody(response);
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
            return getBody(response);
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

            response.close();
        }, () -> {
            body.setCode(HttpCodeEnum.getCode(response.code()));
        });
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
        InputStream inputStream = null;
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response;
        try {
            response = new OkHttpClient().newCall(req).execute();
            if (!response.isSuccessful()) {
                log.error("【调用HTTP请求异常】 code:{},headers:{},message:{}", response.code(), response.headers(), response.message());
                return null;
            }
            inputStream = response.body().byteStream();
            return new Body(inputToByte(inputStream), HttpCodeEnum.getCode(response.code()), response.headers());
        } catch (IOException e) {
            log.error("发起请求出现异常:", e);
            return new Body(HttpCodeEnum.ERROR);
        } finally {
            try {
                inputStream.close();
            } catch (IOException var12) {
                log.error("【关闭流异常】");
            }
        }
    }

    /**
     * 根据URL网址获取文件
     *
     * @param url - url
     * @return byte[] 文件
     */
    public static InputStream sendGetForFileInputStream(String url) {
        InputStream inputStream = null;
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response;
        try {
            response = new OkHttpClient().newCall(req).execute();
            if (!response.isSuccessful()) {
                log.error("【调用HTTP请求异常】 code:{},message:{}", response.code(), response.message());
                return null;
            }
            inputStream = response.body().byteStream();
            return inputStream;
        } catch (IOException e) {
            log.error("发起请求出现异常:", e);
            return inputStream;
        } finally {
            try {
                inputStream.close();
            } catch (IOException var12) {
                log.error("【关闭流异常】");
            }
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
        InputStream inputStream = null;
        try {
            RequestBody requestBody = RequestBody.create(json, MEDIA_TYPE_JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            Response response = new OkHttpClient().newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("【调用HTTP请求异常】 code:{},message:{}", response.code(), response.message());
                return new Body(HttpCodeEnum.ERROR);
            }
            inputStream = response.body().byteStream();

            return new Body(inputToByte(inputStream), HttpCodeEnum.getCode(response.code()));

        } catch (IOException var13) {
            log.error("发起请求出现异常:{}", var13.getMessage());
            return new Body(HttpCodeEnum.ERROR);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException var12) {
                log.error("【关闭流异常】:{}", var12.getMessage());
            }

        }
    }

    private static byte[] inputToByte(InputStream inputStream) throws IOException {
        //创建一个字节数组输出流
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        //创建一个字节数组
        byte[] buff = new byte[1024];
        //创建一个输入流
        int rc;
        //循环读取字节数组
        while ((rc = inputStream.read(buff, 0, 1024)) > 0) {
            //将读取的字节数组写入字节数组输出流
            swapStream.write(buff, 0, rc);
        }
        //返回字节数组输出流
        return swapStream.toByteArray();
    }

    private static SSLSocketFactory getSocketFactory(TrustManager manager) {
        SSLSocketFactory factory = null;
        try {
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, new TrustManager[]{manager}, new SecureRandom());
            factory = context.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
        return factory;
    }

    public static X509TrustManager getX509TrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private static HostnameVerifier getHostnameVerifier() {
        return (s, sslSession) -> true;
    }


    @Data
    public static class Body {
        String body;
        byte[] file;
        HttpCodeEnum code;
        Headers headers;

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
