package nctu.nasa.gotobeach;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Http {
    private static OkHttpClient okHttpClient = new OkHttpClient();
    public static void get(String url, Callback cb) {
        okHttpClient.newCall(new Request.Builder().url(url).build()).enqueue(cb);
    }
}
