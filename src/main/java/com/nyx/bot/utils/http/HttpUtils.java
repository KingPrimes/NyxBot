package com.nyx.bot.utils.http;

import com.nyx.bot.core.SpringValues;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.MarketFormEnums;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.SpringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.brotli.BrotliInterceptor;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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

    private static final OkHttpClient client;

    private static double lastProgress = -1;

    static {
        try {
            final X509ExtendedTrustManager trustAllCerts = new X509ExtendedTrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {

                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
                }
            };


            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{trustAllCerts}, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            String proxyHost = System.getProperty("proxyHost");
            String proxyPort = System.getProperty("proxyPort");
            Proxy proxy;
            if (proxyHost != null && proxyPort != null) {
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
            } else {

                try {
                    // 仅在测试环境中生效
                    var utils = SpringUtils.getBean(SpringValues.class);
                    proxyHost = utils.proxyHost;
                    proxyPort = utils.proxyPort;
                    if (proxyHost.isEmpty() || proxyPort.isEmpty()) {
                        proxy = Proxy.NO_PROXY;
                    } else {
                        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
                    }
                } catch (Exception e) {
                    //
                    proxy = Proxy.NO_PROXY;
                }
            }
            client = new OkHttpClient().newBuilder()
                    .addInterceptor(BrotliInterceptor.INSTANCE)
                    .proxy(proxy)
                    //调用超时
                    .callTimeout(60, TimeUnit.SECONDS)
                    //链接超时
                    .connectTimeout(60, TimeUnit.SECONDS)
                    //读取超时
                    .readTimeout(60, TimeUnit.SECONDS)
                    .sslSocketFactory(sslSocketFactory, trustAllCerts)
                    .hostnameVerifier((home, seen) -> true)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

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
            body.setUrl(url);
            //log.debug("Url：{}，Param:{} TakeTime：{}ms", url, param, body.getTakeTime());
            return body;
        } catch (IOException e) {
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
                log.warn("Response Code Is Not Successful code:{},message:{}", response.code(), response.message());
                return new Body(HttpCodeEnum.ERROR);
            }
            return getBody(response);
        } catch (IOException e) {
            log.warn("sendPost", e);
            return new Body(HttpCodeEnum.ERROR);
        }
    }


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

            r.close();
        }, () -> {
            body.setCode(HttpCodeEnum.getCode(response.code()));
            response.close();
        });
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
     * @param url  - url
     * @param path - 文件输出路径
     */
    public static Boolean sendGetForFile(String url, String path) {
        // 用于下载完成返回标志符
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        File outputFile = new File(path);
        // 若目录不存在,创建目录
        FileUtils.createDir(outputFile);
        // 构建请求
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            // 执行请求
            client.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    log.error("onFailure Error", e);
                    future.complete(false);
                    future.completeExceptionally(e);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        log.warn("File Download： code:{},headers:{},message:{}", response.code(), response.headers(), response.message());
                        future.complete(false);
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        log.warn("File Download： body is null");
                        return;
                    }
                    long fileSize = body.contentLength();
                    long downloaded = 0;
                    // 获取数据流，创建文件
                    try (InputStream in = body.byteStream();
                         FileOutputStream out = new FileOutputStream(outputFile)) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                            downloaded += bytesRead;
                            // 输出进度
                            printDownloadProgress(fileSize, downloaded);
                        }
                        future.complete(true);
                    }
                }
            });
        } catch (Exception e) {
            log.warn("sendGetForFile An abnormality occurred:", e);
            future.complete(false);
            future.completeExceptionally(e);
        }
        return future.join();
    }

    private static void printDownloadProgress(long fileSize, long downloaded) {
        double progress = (double) downloaded / fileSize * 100;
        progress = Math.floor(progress); // Round down to nearest integer

        if (progress - lastProgress >= 1) {
            log.info("File download progress:{}%", String.format("%.2f", progress));
            lastProgress = progress;
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
            Headers.Builder headers = new Headers.Builder();
            headers.add("Accept-Encoding", "application/octet-stream");
            RequestBody requestBody = RequestBody.create(json, MEDIA_TYPE_JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .headers(headers.build())
                    .build();
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.warn("Response Code Is Not Successful code:{},message:{}", response.code(), response.message());
                return new Body(HttpCodeEnum.ERROR);
            }
            Body body = getBodyForFile(response);
            body.setUrl(url);
            return body;

        } catch (IOException e) {
            log.warn("An exception occurred in the initiation request", e);
            return new Body(HttpCodeEnum.ERROR);
        }
    }


    private static Body getBodyForFile(Response response) throws IOException {
        InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
        Body body = new Body(inputToByte(inputStream, response.body().contentLength()), HttpCodeEnum.getCode(response.code()), response.headers());
        body.setTakeTime(response.receivedResponseAtMillis() - response.sentRequestAtMillis());
        response.close();
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
                log.info("File download progress: {}%", tip);
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
        String url;

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
