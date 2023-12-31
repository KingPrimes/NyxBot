package com.nyx.bot.utils;

import com.nyx.bot.enums.HttpCodeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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

    private static final X509TrustManager manager = HttpUtils.getX509TrustManager();
    private static final OkHttpClient client = new OkHttpClient().newBuilder()
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


    /**
     * Http Get请求
     *
     * @param url     请求地址
     * @param param   请求参数
     * @param headers 请求头
     * @return 返回的文本
     */
    public static Body sendGet(String url, String param, Headers.Builder headers) {
        log.debug("发送请求\nUrl:{}\n参数:{}\n请求头:{}", url, param, headers.toString());
        try (

                Response response = client.newCall(send(url, param, headers)).execute()
        ) {
            Body body = new Body();
            Optional.of(response.body().string()).ifPresentOrElse(r -> {
                body.setBody(r);
                body.setCode(HttpCodeEnum.SUCCESS);
                response.close();
            }, () -> {
                body.setCode(HttpCodeEnum.REQUEST_TIMEOUT);
            });

            return body;
        } catch (IOException e) {
            log.error(e.getMessage());
            return new Body(HttpCodeEnum.ERROR);
        }
    }

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
     * 发送Get请求
     *
     * @param url     请求地址
     * @param param   请求参数 key=v&key=v
     * @param headers 请求头
     * @return 返回值
     */
    public static String sendGetOkHttp(String url, String param, Headers.Builder headers) {
        try {
            Call call = new OkHttpClient().newCall(send(url, param, headers));
            call.timeout().timeout(30, TimeUnit.SECONDS);
            Response response = call.execute();
            String tmp = Objects.requireNonNull(response.body()).string();
            response.close();
            return tmp;
        } catch (Exception e) {
            if (e.getMessage().equals("timeout")) {
                log.error("Url:{} 请求超时", url + param);
                return "timeout";
            }
            log.error("Url:{} \n\t\t错误信息:{}", url + param, e.getMessage());
            return "";
        }

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
                log.error("【调用HTTP请求异常】 code:{},message:{}", response.code(), response.message());
                return null;
            }
            inputStream = response.body().byteStream();
            return new Body(inputToByte(inputStream), HttpCodeEnum.SUCCESS);
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

            return new Body(inputToByte(inputStream), HttpCodeEnum.SUCCESS);

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

    @Data
    public static class Body {
        String body;
        byte[] file;
        HttpCodeEnum code;

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

}
