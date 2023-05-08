package com.zegocloud.demo.liveaudioroom.internal;

import android.app.Application;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.ConnectCallback;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.JoinRoomCallback;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOExpressService;
import com.zegocloud.demo.liveaudioroom.utils.LogUtil;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import org.json.JSONObject;

public class ZEGOSDKManager {

    public ZEGOExpressService rtcService = new ZEGOExpressService();
    public ZIMService zimService = new ZIMService();

    private static final class Holder {

        private static final ZEGOSDKManager INSTANCE = new ZEGOSDKManager();
    }

    public static ZEGOSDKManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initSDK(Application application, long appID, String appSign) {
        rtcService.initSDK(application, appID, appSign);
        zimService.initSDK(application, appID, appSign);
    }

    public void connectUser(String userID, String userName, ConnectCallback callback) {
        rtcService.connectUser(userID, userName);
        zimService.connectUser(userID, userName, callback);
    }

    public void disconnectUser() {
        zimService.disconnectUser();
        rtcService.disconnectUser();

    }

    public void joinRoom(String roomID, IZegoRoomLoginCallback callback) {
        rtcService.joinRoom(roomID, new IZegoRoomLoginCallback() {
            @Override
            public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
                if (errorCode == 0) {
                    zimService.joinRoom(roomID, new JoinRoomCallback() {
                        @Override
                        public void onResult(int errorCode, String message) {
                            if (callback != null) {
                                callback.onRoomLoginResult(errorCode, new JSONObject());
                            }
                        }
                    });
                } else {
                    if (callback != null) {
                        callback.onRoomLoginResult(errorCode, extendedData);
                    }
                }
            }
        });
    }

    public void leaveRoom() {
        setBusy(false);
        rtcService.leaveRoom();
        zimService.leaveRoom();
    }

    public void setBusy(boolean busy) {
        zimService.setBusy(busy);
    }

    public boolean isBusy() {
        return zimService.isBusy();
    }

    public void setDebugMode(boolean debugMode) {
        LogUtil.setDebug(debugMode);
    }
}
