package com.zegocloud.demo.liveaudioroom.backend;

import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.json.JSONException;
import org.json.JSONObject;

public class Backend {

    private OkHttpClient client = new OkHttpClient();
    private String baseUrl = "http://45.32.180.197:3000/api";

    private static final String TAG = "Backend";

    public void init() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder().addInterceptor(loggingInterceptor).build();
    }

    public void login(String userID, Result result) {
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendPath("login");
        builder.appendQueryParameter("uid", userID);
        String url = builder.build().toString();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = "";
                try {
                    str = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    JSONObject jsonObject = new JSONObject(str);
                    JSONObject data = jsonObject.getJSONObject("data");
                    JSONObject user = data.getJSONObject("user");

                    if (result != null) {
                        result.onResult(0, user.toString());
                    }
                } catch (JSONException e) {

                    if (result != null) {
                        result.onResult(-1, "json error");
                    }
                }
            }
        });
    }

    public void getCode(String uid, Result result) {
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendPath("get_code");
        builder.appendQueryParameter("uid", uid);
        String url = builder.build().toString();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = "";
                try {
                    str = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    JSONObject jsonObject = new JSONObject(str);
                    JSONObject data = jsonObject.getJSONObject("data");

                    if (result != null) {
                        result.onResult(0, data.toString());
                    }
                } catch (JSONException e) {

                    if (result != null) {
                        result.onResult(-1, "json error");
                    }
                }
            }
        });
    }

    public void coinsConsumption(String uid, int coin, Result result) {
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendPath("coins_consumption");
        builder.appendQueryParameter("uid", uid);
        builder.appendQueryParameter("coins", String.valueOf(coin));
        String url = builder.build().toString();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = "";
                try {
                    str = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    JSONObject jsonObject = new JSONObject(str);
                    JSONObject data = jsonObject.getJSONObject("data");

                    if (result != null) {
                        result.onResult(0, data.toString());
                    }
                } catch (JSONException e) {

                    if (result != null) {
                        result.onResult(-1, "json error");
                    }
                }
            }
        });
    }

    public void playerMatch(String uid, long gm_id, Result result) {
        Log.d(TAG,
            "playerMatch() called with: uid = [" + uid + "], gm_id = [" + gm_id + "], result = [" + result + "]");
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendPath("player_match");
        builder.appendQueryParameter("uid", uid);
        builder.appendQueryParameter("gm_id", String.valueOf(gm_id));
        String url = builder.build().toString();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "onFailure() called with: call = [" + call + "], e = [" + e + "]");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG, "onResponse() called with: call = [" + call + "], response = [" + response + "]");
                String str = "";
                try {
                    str = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    JSONObject jsonObject = new JSONObject(str);
                    JSONObject data = jsonObject.getJSONObject("data");

                    if (result != null) {
                        result.onResult(0, data.toString());
                    }
                } catch (JSONException e) {

                    if (result != null) {
                        result.onResult(-1, "json error");
                    }
                }
            }
        });
    }

    public void stopMatch(String uid, String gm_id, Result result) {
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendPath("stop_match");
        builder.appendQueryParameter("uid", uid);
        builder.appendQueryParameter("gm_id", String.valueOf(gm_id));
        String url = builder.build().toString();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = "";
                try {
                    str = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    JSONObject jsonObject = new JSONObject(str);
                    JSONObject data = jsonObject.getJSONObject("data");

                    if (result != null) {
                        result.onResult(0, data.toString());
                    }
                } catch (JSONException e) {

                    if (result != null) {
                        result.onResult(-1, "json error");
                    }
                }
            }
        });
    }

    public interface Result {

        void onResult(int errorCode, String message);
    }
}
