package me.ag2s.cronet;

import android.content.Context;

import androidx.annotation.NonNull;

import org.chromium.net.ExperimentalCronetEngine;
import org.chromium.net.MyCronetEngine;
import org.json.JSONObject;

import java.util.concurrent.Executor;

@SuppressWarnings("unused")
public class CronetHolder {
    private static final Object lock = new Object();
    private static volatile ExperimentalCronetEngine engine;
    private static volatile Executor executorService;

    @NonNull
    public static Executor getExecutor() {
        if (executorService == null) {
            synchronized (lock) {
                if (executorService == null) {
                    executorService = createDefaultExecutorService();
                }
            }
        }
        return executorService;
    }

    public static void setExecutor(@NonNull Executor executorService) {
        CronetHolder.executorService = executorService;
    }

    @NonNull
    private static Executor createDefaultExecutorService() {
        return DirectExecutor.INSTANCE;
    }

    @NonNull
    public static ExperimentalCronetEngine getEngine() {
        if (engine == null) {
            synchronized (lock) {
                if (engine == null) {
                    engine = createDefaultCronetEngine(CronetInitializer.getCtx());
                }
            }
        }
        return engine;
    }

    public static void setEngine(@NonNull ExperimentalCronetEngine engine) {
        CronetHolder.engine = engine;
        Runtime.getRuntime().gc();
    }

    @NonNull
    private static String getExperimentalOptions() {
        JSONObject options = new JSONObject();

        try {
            JSONObject dnsSvcb = new JSONObject();
            dnsSvcb.put("enable", true);
            dnsSvcb.put("enable_insecure", true);
            dnsSvcb.put("use_alpn", true);
            options.put("UseDnsHttpsSvcb", dnsSvcb);

            options.put("AsyncDNS", new JSONObject("{'enable':true}"));

        } catch (Exception ignored) {

        }

        return options.toString();
    }

    @NonNull
    private static ExperimentalCronetEngine createDefaultCronetEngine(Context context) {
        MyCronetEngine.Builder builder = new MyCronetEngine.Builder(context)
                .enableHttp2(true)
                .enableQuic(true)//????????????http/3
                .enableHttp2(true)  //????????????http/2
                .setExperimentalOptions(getExperimentalOptions());
        builder.enableBrotli(true);//Brotli??????

        return builder.build();
    }
}
