package me.ag2s.cronet.okhttp;

import android.os.Build;

import androidx.annotation.NonNull;

import org.chromium.net.CronetEngine;
import org.chromium.net.UrlRequest;

import java.io.IOException;

import me.ag2s.cronet.CronetLoader;
import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Request;
import okhttp3.Response;

public class CronetInterceptor implements okhttp3.Interceptor {
    private final CookieJar mCookieJar;
    private final CronetEngine engine;

    public CronetInterceptor(@NonNull CronetEngine engine, @NonNull CookieJar cookieJar) {
        this.mCookieJar = cookieJar;
        this.engine = engine;
    }

    public CronetInterceptor(@NonNull CronetEngine engine) {
        this(engine, CookieJar.NO_COOKIES);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {


        final Request request = chain.request();
        chain.connectTimeoutMillis();


        if ((!CronetLoader.getInstance().install())) {
            return chain.proceed(request);
        }
        Request.Builder builder = request.newBuilder();
        builder.removeHeader("Keep-Alive");
        builder.removeHeader("Accept-Encoding");
        if (mCookieJar != null && mCookieJar != CookieJar.NO_COOKIES) {
            String cookieString = CronetHelper.getCookieString(mCookieJar, request.url());
            if (cookieString.length() > 4) {
                builder.header("Cookie", cookieString);
            }
        }
        final Request copy = builder.build();

        Response response = proceedWithCronet(engine, copy, chain.call());
        if (mCookieJar != null && mCookieJar != CookieJar.NO_COOKIES) {
            mCookieJar.saveFromResponse(copy.url(), Cookie.parseAll(copy.url(), response.headers()));
        }
        return response;

    }

    private Response proceedWithCronet(@NonNull CronetEngine engine, @NonNull Request request, @NonNull Call call) throws IOException {
        try {
            AbsCronetMemoryCallback callback;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                callback = new CronetCallBackNewMemory(request, call);
            } else {
                callback = new CronetCallBackOldMemory(request, call);
            }
            UrlRequest urlRequest = CronetHelper.buildRequest(engine, request, callback);
            urlRequest.start();
            return callback.waitForDone(urlRequest);

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage(), e);
        }


    }


}
