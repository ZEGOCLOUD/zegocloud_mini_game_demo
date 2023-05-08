package com.zegocloud.demo.liveaudioroom.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.liveaudioroom.ZEGOSDKKeyCenter;
import com.zegocloud.demo.liveaudioroom.backend.Backend;
import com.zegocloud.demo.liveaudioroom.backend.Backend.Result;
import com.zegocloud.demo.liveaudioroom.backend.BackendUser;
import com.zegocloud.demo.liveaudioroom.databinding.ActivityLoginBinding;
import com.zegocloud.demo.liveaudioroom.internal.ZEGOSDKManager;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.ConnectCallback;
import com.zegocloud.demo.liveaudioroom.internal.minigame.MiniGameManager;

import im.zego.zim.callback.ZIMUserAvatarUrlUpdatedCallback;
import im.zego.zim.entity.ZIMError;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private String iconUrl = "https://storage.zego.im/IMKit/avatar/avatar-0.png";
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String userID = generateUserID();
        binding.liveLoginId.getEditText().setText(userID);

        binding.liveLoginBtn.setOnClickListener(v -> {
            ZEGOLiveAudioRoomManager.getInstance().loginBackend(userID, new Result() {
                @Override
                public void onResult(int errorCode, String message) {
                    if (errorCode == 0) {
                        BackendUser backendUser = ZEGOLiveAudioRoomManager.getInstance().getBackendUser();
                        signInZEGOSDK(backendUser.getUid(), backendUser.getNickName(), (errorCode1, message1) -> {
                            if (errorCode1 == 0) {
                                ZEGOLiveAudioRoomManager.getInstance()
                                    .updateUserAvatarUrl(iconUrl, new ZIMUserAvatarUrlUpdatedCallback() {
                                        @Override
                                        public void onUserAvatarUrlUpdated(String userAvatarUrl, ZIMError errorInfo) {
                                            Log.d(TAG, "onUserAvatarUrlUpdated()222 called with: userAvatarUrl = ["
                                                + userAvatarUrl + "], errorInfo = [" + errorInfo.message + "]");
                                        }
                                    });
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });
                    }
                }
            });
        });

        ZEGOLiveAudioRoomManager.getInstance().initBackend();
        initZEGOSDK();
        initMiniGameSDK();
    }

    private void initZEGOSDK() {
        ZEGOSDKManager.getInstance().initSDK(getApplication(), ZEGOSDKKeyCenter.appID, ZEGOSDKKeyCenter.appSign);
    }

    private void initMiniGameSDK() {
        boolean isTestEnv = true;
        MiniGameManager.getInstance()
            .initMiniGameSDK(this, ZEGOSDKKeyCenter.MiniGame_APP_ID, ZEGOSDKKeyCenter.MiniGame_APP_KEY, isTestEnv);
    }

    private void signInZEGOSDK(String userID, String userName, ConnectCallback callback) {
        ZEGOSDKManager.getInstance().connectUser(userID, userName, callback);
    }

    private static String generateUserID() {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        while (builder.length() < 6) {
            int nextInt = random.nextInt(10);
            if (builder.length() == 0 && nextInt == 0) {
                continue;
            }
            builder.append(nextInt);
        }
        return builder.toString();
    }
}