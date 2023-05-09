package com.zegocloud.demo.liveaudioroom.internal.rtc;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import com.zegocloud.demo.liveaudioroom.utils.LogUtil;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoCustomVideoProcessHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoIMSendBarrageMessageCallback;
import im.zego.zegoexpress.callback.IZegoIMSendCustomCommandCallback;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.callback.IZegoRoomSetRoomExtraInfoCallback;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRemoteDeviceState;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoBarrageMessageInfo;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoCustomVideoProcessConfig;
import im.zego.zegoexpress.entity.ZegoEngineConfig;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public class ZEGOExpressService {

    private ZegoExpressEngine engine;
    private ZEGOCLOUDUser localUser;
    private String currentRoomID;
    // order by default time s
    private List<String> userIDList = new ArrayList<>();
    private Map<String, ZEGOCLOUDUser> roomUserMap = new HashMap<>();
    private Map<String, ZegoRoomExtraInfo> roomExtraInfoMap = new HashMap<>();

    private IZegoEventHandler eventHandler;
    private List<RoomStateChangeListener> roomStateChangeListenerList = new ArrayList<>();
    private List<RoomUserChangeListener> roomUserChangeListenerList = new ArrayList<>();
    private List<RoomStreamChangeListener> roomStreamChangeListenerList = new ArrayList<>();
    private List<IMCustomCommandListener> customCommandListenerList = new ArrayList<>();
    private List<IMBarrageMessageListener> barrageMessageListenerList = new ArrayList<>();
    private List<CameraListener> cameraListenerList = new ArrayList<>();
    private List<MicrophoneListener> microphoneListenerList = new ArrayList<>();
    private List<RoomExtraInfoListener> roomExtraInfoListenerList = new ArrayList<>();


    public void initSDK(Application application, long appID, String appSign) {
        if (engine != null) {
            return;
        }
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.appSign = appSign;
        profile.scenario = ZegoScenario.DEFAULT;
        profile.application = application;
        ZegoEngineConfig config = new ZegoEngineConfig();
        config.advancedConfig.put("notify_remote_device_unknown_status", "true");
        config.advancedConfig.put("notify_remote_device_init_status", "true");
        ZegoExpressEngine.setEngineConfig(config);
        engine = ZegoExpressEngine.createEngine(profile, new IZegoEventHandler() {

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList,
                JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                LogUtil.d("onRoomStreamUpdate() called with: roomID = [" + roomID + "], updateType = [" + updateType
                    + "], streamList = [" + streamList + "], extendedData = [" + extendedData + "]");
                List<ZEGOCLOUDUser> userList = new ArrayList<>();
                if (updateType == ZegoUpdateType.ADD) {
                    for (ZegoStream zegoStream : streamList) {
                        Log.d(TAG, "onRoomStreamUpdate streamID: " + zegoStream.streamID + ",extraInfo: "
                            + zegoStream.extraInfo);
                        ZEGOCLOUDUser liveUser = getUser(zegoStream.user.userID);
                        if (liveUser == null) {
                            liveUser = new ZEGOCLOUDUser(zegoStream.user.userID, zegoStream.user.userName);
                            saveUserInfo(liveUser);
                        }
                        liveUser.setStreamID(zegoStream.streamID);
                        if (!TextUtils.isEmpty(zegoStream.extraInfo)) {
                            try {
                                JSONObject jsonObject = new JSONObject(zegoStream.extraInfo);
                                if (jsonObject.has("cam")) {
                                    boolean isCameraOpen = jsonObject.getBoolean("cam");
                                    boolean changed = liveUser.isCameraOpen() != isCameraOpen;
                                    liveUser.setCameraOpen(isCameraOpen);
                                    if (changed) {
                                        for (CameraListener listener : cameraListenerList) {
                                            listener.onCameraOpen(liveUser.userID, isCameraOpen);
                                        }
                                    }
                                }
                                if (jsonObject.has("mic")) {
                                    boolean isMicOpen = jsonObject.getBoolean("mic");
                                    boolean changed = liveUser.isMicrophoneOpen() != isMicOpen;
                                    liveUser.setMicrophoneOpen(isMicOpen);
                                    if (changed) {
                                        for (MicrophoneListener listener : microphoneListenerList) {
                                            listener.onMicrophoneOpen(liveUser.userID, isMicOpen);
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        userList.add(liveUser);
                    }
                    for (RoomStreamChangeListener listener : roomStreamChangeListenerList) {
                        listener.onStreamAdd(userList);
                    }
                } else {
                    for (ZegoStream zegoStream : streamList) {
                        ZEGOCLOUDUser liveUser = getUser(zegoStream.user.userID);
                        if (liveUser != null) {
                            liveUser.deleteStream(zegoStream.streamID);
                            boolean notifyCamera = liveUser.isCameraOpen();
                            boolean notifyMic = liveUser.isMicrophoneOpen();
                            liveUser.setCameraOpen(false);
                            liveUser.setMicrophoneOpen(false);
                            if (notifyCamera) {
                                for (CameraListener listener : cameraListenerList) {
                                    listener.onCameraOpen(liveUser.userID, false);
                                }
                            }
                            if (notifyMic) {
                                for (MicrophoneListener listener : microphoneListenerList) {
                                    listener.onMicrophoneOpen(liveUser.userID, false);
                                }
                            }
                        } else {
                            liveUser = new ZEGOCLOUDUser(zegoStream.user.userID, zegoStream.user.userName);
                        }
                        userList.add(liveUser);
                    }
                    for (RoomStreamChangeListener listener : roomStreamChangeListenerList) {
                        listener.onStreamRemove(userList);
                    }
                }

                if (eventHandler != null) {
                    eventHandler.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                }
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode,
                JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                Log.d(TAG, "onPublisherStateUpdate() called with: streamID = [" + streamID + "], state = [" + state
                    + "], errorCode = [" + errorCode + "], extendedData = [" + extendedData + "]");
                ArrayList<ZegoStream> streamList = new ArrayList<>(1);
                ZegoStream zegoStream = new ZegoStream();
                zegoStream.user = new ZegoUser(localUser.userID, localUser.userName);
                zegoStream.streamID = streamID;
                zegoStream.extraInfo = extendedData.toString();
                streamList.add(zegoStream);

                if (state == ZegoPublisherState.PUBLISHING) {
                    localUser.setStreamID(streamID);

                    for (RoomStreamChangeListener listener : roomStreamChangeListenerList) {
                        listener.onStreamAdd(Collections.singletonList(localUser));
                    }
                    if (eventHandler != null) {
                        eventHandler.onRoomStreamUpdate(currentRoomID, ZegoUpdateType.ADD, streamList, extendedData);
                    }
                } else if (state == ZegoPublisherState.NO_PUBLISH) {
                    localUser.deleteStream(streamID);

                    for (RoomStreamChangeListener listener : roomStreamChangeListenerList) {
                        listener.onStreamRemove(Collections.singletonList(localUser));
                    }
                    if (eventHandler != null) {
                        eventHandler.onRoomStreamUpdate(currentRoomID, ZegoUpdateType.DELETE, streamList, extendedData);
                    }
                }

            }

            @Override
            public void onRoomStreamExtraInfoUpdate(String roomID, ArrayList<ZegoStream> streamList) {
                super.onRoomStreamExtraInfoUpdate(roomID, streamList);

                for (ZegoStream zegoStream : streamList) {
                    Log.d(TAG, "onRoomStreamExtraInfoUpdate() called with: zegoStream = [" + zegoStream.streamID
                        + "], extraInfo = [" + zegoStream.extraInfo + "]");
                    if (!TextUtils.isEmpty(zegoStream.extraInfo)) {
                        ZEGOCLOUDUser liveUser = getUser(zegoStream.user.userID);
                        try {
                            JSONObject jsonObject = new JSONObject(zegoStream.extraInfo);
                            if (jsonObject.has("cam")) {
                                boolean isCameraOpen = jsonObject.getBoolean("cam");
                                boolean changed = liveUser.isCameraOpen() != isCameraOpen;
                                liveUser.setCameraOpen(isCameraOpen);
                                if (changed) {
                                    for (CameraListener listener : cameraListenerList) {
                                        listener.onCameraOpen(liveUser.userID, isCameraOpen);
                                    }
                                }
                            }
                            if (jsonObject.has("mic")) {
                                boolean isMicOpen = jsonObject.getBoolean("mic");
                                boolean changed = liveUser.isMicrophoneOpen() != isMicOpen;
                                liveUser.setMicrophoneOpen(isMicOpen);
                                if (changed) {
                                    for (MicrophoneListener listener : microphoneListenerList) {
                                        listener.onMicrophoneOpen(liveUser.userID, isMicOpen);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }

            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                super.onRoomUserUpdate(roomID, updateType, userList);
                Log.d(TAG, "onRoomUserUpdate() called with: roomID = [" + roomID + "], updateType = [" + updateType
                    + "], userList = [" + userList + "]");
                List<ZEGOCLOUDUser> liveUserList = new ArrayList<>();
                for (ZegoUser zegoUser : userList) {
                    ZEGOCLOUDUser liveUser = getUser(zegoUser.userID);
                    if (liveUser != null) {
                    } else {
                        liveUserList.add(new ZEGOCLOUDUser(zegoUser.userID, zegoUser.userName));
                    }
                }

                if (updateType == ZegoUpdateType.ADD) {
                    for (ZEGOCLOUDUser liveUser : liveUserList) {
                        saveUserInfo(liveUser);
                    }
                    for (RoomUserChangeListener listener : roomUserChangeListenerList) {
                        listener.onUserEnter(liveUserList);
                    }
                } else {
                    for (ZEGOCLOUDUser liveUser : liveUserList) {
                        removeUserInfo(liveUser.userID);
                    }
                    for (RoomUserChangeListener listener : roomUserChangeListenerList) {
                        listener.onUserLeft(liveUserList);
                    }
                }

                if (eventHandler != null) {
                    eventHandler.onRoomUserUpdate(roomID, updateType, userList);
                }
            }

            @Override
            public void onRemoteCameraStateUpdate(String streamID, ZegoRemoteDeviceState state) {
                super.onRemoteCameraStateUpdate(streamID, state);
                LogUtil.d(
                    "onRemoteCameraStateUpdate() called with: streamID = [" + streamID + "], state = [" + state + "]");

                boolean isCameraOpen = state == ZegoRemoteDeviceState.OPEN;
                ZEGOCLOUDUser liveUser = getUserFromStreamID(streamID);
                if (liveUser == null) {
                    return;
                }
                boolean changed = liveUser.isCameraOpen() != isCameraOpen;
                liveUser.setCameraOpen(isCameraOpen);
                if (changed) {
                    for (CameraListener listener : cameraListenerList) {
                        listener.onCameraOpen(liveUser.userID, isCameraOpen);
                    }
                }

                if (eventHandler != null) {
                    eventHandler.onRemoteCameraStateUpdate(streamID, state);
                }
            }

            @Override
            public void onRemoteMicStateUpdate(String streamID, ZegoRemoteDeviceState state) {
                super.onRemoteMicStateUpdate(streamID, state);
                Log.d(TAG,
                    "onRemoteMicStateUpdate() called with: streamID = [" + streamID + "], state = [" + state + "]");

                boolean isMicOpen = state == ZegoRemoteDeviceState.OPEN;
                ZEGOCLOUDUser liveUser = getUserFromStreamID(streamID);
                if (liveUser == null) {
                    return;
                }
                boolean changed = liveUser.isMicrophoneOpen() != isMicOpen;
                liveUser.setMicrophoneOpen(isMicOpen);
                if (changed) {
                    for (MicrophoneListener listener : microphoneListenerList) {
                        listener.onMicrophoneOpen(liveUser.userID, isMicOpen);
                    }
                }

                if (eventHandler != null) {
                    eventHandler.onRemoteMicStateUpdate(streamID, state);
                }
            }

            @Override
            public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode,
                JSONObject extendedData) {
                super.onRoomStateChanged(roomID, reason, errorCode, extendedData);
                LogUtil.d("onRoomStateChanged() called with: roomID = [" + roomID + "], reason = [" + reason
                    + "], errorCode = [" + errorCode + "], extendedData = [" + extendedData + "]");
                if (eventHandler != null) {
                    eventHandler.onRoomStateChanged(roomID, reason, errorCode, extendedData);
                }
                for (RoomStateChangeListener listener : roomStateChangeListenerList) {
                    listener.onRoomStateChanged(roomID, reason, errorCode, extendedData);
                }
            }

            @Override
            public void onIMRecvCustomCommand(String roomID, ZegoUser fromUser, String command) {
                super.onIMRecvCustomCommand(roomID, fromUser, command);
                Log.d(TAG, "onIMRecvCustomCommand() called with: roomID = [" + roomID + "], fromUser = [" + fromUser
                    + "], command = [" + command + "]");
                for (IMCustomCommandListener listener : customCommandListenerList) {
                    listener.onIMRecvCustomCommand(roomID, fromUser, command);
                }
                if (eventHandler != null) {
                    eventHandler.onIMRecvCustomCommand(roomID, fromUser, command);
                }
            }

            @Override
            public void onIMRecvBarrageMessage(String roomID, ArrayList<ZegoBarrageMessageInfo> messageList) {
                super.onIMRecvBarrageMessage(roomID, messageList);
                Log.d(TAG,
                    "onIMRecvBarrageMessage() called with: roomID = [" + roomID + "], messageList = [" + messageList
                        + "]");
                for (IMBarrageMessageListener listener : barrageMessageListenerList) {
                    listener.onIMRecvBarrageMessage(roomID, messageList);
                }
                if (eventHandler != null) {
                    eventHandler.onIMRecvBarrageMessage(roomID, messageList);
                }
            }

            @Override
            public void onRoomExtraInfoUpdate(String roomID, ArrayList<ZegoRoomExtraInfo> roomExtraInfoList) {
                super.onRoomExtraInfoUpdate(roomID, roomExtraInfoList);
                for (ZegoRoomExtraInfo roomExtraInfo : roomExtraInfoList) {
                    ZegoRoomExtraInfo oldRoomExtraInfo = roomExtraInfoMap.get(roomExtraInfo.key);
                    Log.d(TAG, "onRoomExtraInfoUpdate()000 called with: roomExtraInfo.key = [" + roomExtraInfo.key
                        + "], value = [" + roomExtraInfo.value + "]");
                    if (oldRoomExtraInfo != null) {
                        if (Objects.equals(roomExtraInfo.updateUser.userID, getLocalUser().userID)) {
                            continue;
                        }
                        if (roomExtraInfo.updateTime < oldRoomExtraInfo.updateTime) {
                            continue;
                        }
                    }
                    Log.d(TAG, "onRoomExtraInfoUpdate()111 called with: roomExtraInfo.key = [" + roomExtraInfo.key
                        + "], value = [" + roomExtraInfo.value + "]");
                    roomExtraInfoMap.put(roomExtraInfo.key, roomExtraInfo);

                    for (RoomExtraInfoListener listener : roomExtraInfoListenerList) {
                        listener.onRoomExtraInfoUpdate(roomExtraInfoList);
                    }
                }
            }
        });
    }

    public void startCameraPreview(TextureView textureView, ZegoViewMode viewMode) {
        LogUtil.d("startCameraPreview() called with: textureView = [" + textureView + "]");
        if (engine == null) {
            return;
        }
        ZegoCanvas canvas = new ZegoCanvas(textureView);
        canvas.viewMode = viewMode;
        engine.startPreview(canvas);
    }

    public void stopCameraPreview() {
        LogUtil.d("stopCameraPreview() called");
        if (engine == null) {
            return;
        }
        engine.stopPreview();
    }

    public String generateStream(String userID) {
        return currentRoomID + "_" + userID + "_main";
    }

    /**
     * preview before publish
     */
    public void startPublishLocalAudioVideo() {
        if (engine == null || localUser == null) {
            return;
        }
        String streamID = generateStream(localUser.userID);
        LogUtil.d("startPublishLocalVideo() called: " + streamID);
        engine.startPublishingStream(streamID);
    }

    public void stopPublishLocalAudioVideo() {
        if (engine == null) {
            return;
        }
        engine.stopPublishingStream();
    }

    public void startPlayRemoteAudioVideo(TextureView textureView, String streamID, ZegoViewMode viewMode) {
        LogUtil.d(
            "startPlayRemoteAudioVideo() called with: textureView = [" + textureView + "], streamID = [" + streamID
                + "]");
        if (engine == null) {
            return;
        }
        ZegoCanvas canvas = new ZegoCanvas(textureView);
        canvas.viewMode = viewMode;
        engine.startPlayingStream(streamID, canvas);
    }

    public void startPlayRemoteAudioVideo(String streamID) {
        if (engine == null) {
            return;
        }
        engine.startPlayingStream(streamID);
    }

    public void stopPlayRemoteAudioVideo(String streamID) {
        if (engine == null) {
            return;
        }
        engine.stopPlayingStream(streamID);
    }

    public void setEventHandler(IZegoEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public void joinRoom(String roomID, IZegoRoomLoginCallback callback) {
        if (engine == null || localUser == null) {
            return;
        }
        currentRoomID = roomID;
        ZegoRoomConfig config = new ZegoRoomConfig();
        config.isUserStatusNotify = true;
        ZegoUser zegoUser = new ZegoUser(localUser.userID, localUser.userName);
        engine.loginRoom(roomID, zegoUser, config, new IZegoRoomLoginCallback() {
            @Override
            public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
                LogUtil.d(
                    "onRoomLoginResult() called with: errorCode = [" + errorCode + "], extendedData = [" + extendedData
                        + "]");
                if (errorCode != 0) {
                    currentRoomID = null;
                }
                if (callback != null) {
                    callback.onRoomLoginResult(errorCode, extendedData);
                }
            }
        });
    }

    public void leaveRoom() {
        if (engine == null) {
            return;
        }
        engine.logoutRoom();

    }

    public void clear() {
        roomUserMap.clear();
        roomExtraInfoMap.clear();
        userIDList.clear();
        roomUserChangeListenerList.clear();
        roomStreamChangeListenerList.clear();
        barrageMessageListenerList.clear();
        cameraListenerList.clear();
        microphoneListenerList.clear();
        roomExtraInfoListenerList.clear();
        currentRoomID = null;
    }

    public void connectUser(String userID, String userName) {
        Log.d(TAG, "connectUser() called with: userID = [" + userID + "], userName = [" + userName + "]");
        localUser = new ZEGOCLOUDUser(userID, userName);
    }

    public void disconnectUser() {
        Log.d(TAG, "disconnectUser() called");
        localUser = null;
    }

    public ZEGOCLOUDUser getLocalUser() {
        return localUser;
    }

    private void saveUserInfo(ZEGOCLOUDUser liveUser) {
        boolean contains = roomUserMap.containsKey(liveUser.userID);
        roomUserMap.put(liveUser.userID, liveUser);

        if (contains) {
            userIDList.remove(liveUser.userID);
        }

        Log.d(TAG, "saveUserInfo() called with: liveUser = [" + liveUser + "]");
        userIDList.add(liveUser.userID);
    }

    private void removeUserInfo(String userID) {
        Log.d(TAG, "removeUserInfo() called with: userID = [" + userID + "]");
        roomUserMap.remove(userID);
        userIDList.remove(userID);
    }

    public List<ZEGOCLOUDUser> getUserList() {
        List<ZEGOCLOUDUser> userList = new ArrayList<>();
        userList.add(localUser);
        for (String userID : userIDList) {
            ZEGOCLOUDUser liveUser = roomUserMap.get(userID);
            userList.add(liveUser);
        }
        return userList;
    }

    public ZEGOCLOUDUser getUser(String userID) {
        if (userID != null && localUser != null && userID.equals(localUser.userID)) {
            return localUser;
        }
        return roomUserMap.get(userID);
    }

    private static final String TAG = "ZEGOExpressService";

    public void enableCamera(boolean enable) {
        Log.d(TAG, "enableCamera() called with: enable = [" + enable + "]");
        if (engine == null) {
            return;
        }
        if (localUser != null) {
            boolean changed = enable != localUser.isCameraOpen();
            localUser.setCameraOpen(enable);
            if (changed) {
                for (CameraListener listener : cameraListenerList) {
                    listener.onCameraOpen(localUser.userID, enable);
                }
            }
        }
        engine.enableCamera(enable);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cam", enable);
            jsonObject.put("mic", localUser.isMicrophoneOpen());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String extraInfo = jsonObject.toString();
        engine.setStreamExtraInfo(extraInfo, null);
    }

    public void openMicrophone(boolean open) {
        Log.d(TAG, "openMicrophone() called with: open = [" + open + "]");
        if (engine == null) {
            return;
        }
        if (localUser != null) {
            boolean changed = localUser.isMicrophoneOpen() != open;
            localUser.setMicrophoneOpen(open);
            if (changed) {
                for (MicrophoneListener listener : microphoneListenerList) {
                    listener.onMicrophoneOpen(localUser.userID, open);
                }
            }
        }
        engine.muteMicrophone(!open);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cam", localUser.isCameraOpen());
            jsonObject.put("mic", open);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String extraInfo = jsonObject.toString();
        engine.setStreamExtraInfo(extraInfo, null);
    }

    public void useFrontCamera(boolean useFront) {
        if (engine == null) {
            return;
        }
        engine.useFrontCamera(useFront);
    }

    public ZEGOCLOUDUser getUserFromStreamID(String streamID) {
        if (getLocalUser() != null && Objects.equals(getLocalUser().getMainStreamID(), streamID)) {
            return getLocalUser();
        }
        for (ZEGOCLOUDUser liveUser : roomUserMap.values()) {
            if (Objects.equals(liveUser.getMainStreamID(), streamID)) {
                return liveUser;
            }
        }
        return null;
    }

    public String getCurrentRoomID() {
        return currentRoomID;
    }

    public void audioRouteToSpeaker(boolean routeToSpeaker) {
        if (engine == null) {
            return;
        }
        engine.setAudioRouteToSpeaker(routeToSpeaker);
    }

    public boolean isLocalUser(String userID) {
        if (localUser != null) {
            return Objects.equals(userID, localUser.userID);
        } else {
            return false;
        }
    }

    public void setRTCRoomExtraInfo(String key, String value) {
        Log.d(TAG, "setRTCRoomExtraInfo() called with: key = [" + key + "], value = [" + value + "]");
        if (engine == null || TextUtils.isEmpty(currentRoomID)) {
            return;
        }
        engine.setRoomExtraInfo(currentRoomID, key, value, new IZegoRoomSetRoomExtraInfoCallback() {
            @Override
            public void onRoomSetRoomExtraInfoResult(int errorCode) {
                if (errorCode == 0) {
                    ZegoRoomExtraInfo roomExtraInfo = roomExtraInfoMap.get(key);
                    if (roomExtraInfo == null) {
                        roomExtraInfo = new ZegoRoomExtraInfo();
                        roomExtraInfo.key = key;
                        roomExtraInfo.updateUser = new ZegoUser(getLocalUser().userID, getLocalUser().userName);
                    }
                    roomExtraInfo.updateTime = System.currentTimeMillis();
                    roomExtraInfo.value = value;
                    roomExtraInfoMap.put(roomExtraInfo.key, roomExtraInfo);
                    ArrayList<ZegoRoomExtraInfo> extraInfoList = new ArrayList<>();
                    extraInfoList.add(roomExtraInfo);

                    Log.d(TAG,
                        "setRTCRoomExtraInfo() result with: roomExtraInfo.key = [" + roomExtraInfo.key + "], value = ["
                            + roomExtraInfo.value + "]");

                    for (RoomExtraInfoListener listener : roomExtraInfoListenerList) {
                        listener.onRoomExtraInfoUpdate(extraInfoList);
                    }
                }
            }
        });
    }

    public void enableCustomVideoProcessing(boolean enable, ZegoCustomVideoProcessConfig config,
        ZegoPublishChannel channel) {
        if (engine == null) {
            return;
        }
        engine.enableCustomVideoProcessing(enable, config, channel);
    }

    public void setCustomVideoProcessHandler(IZegoCustomVideoProcessHandler handler) {
        if (engine == null) {
            return;
        }
        engine.setCustomVideoProcessHandler(handler);
    }

    public ZegoVideoConfig getVideoConfig() {
        if (engine == null) {
            return null;
        }
        return engine.getVideoConfig();
    }

    public void sendCustomVideoProcessedTextureData(int textureID, int width, int height,
        long referenceTimeMillisecond) {
        if (engine == null) {
            return;
        }
        engine.sendCustomVideoProcessedTextureData(textureID, width, height, referenceTimeMillisecond);
    }

    public void sendCustomCommand(String command, ArrayList<ZegoUser> toUserList,
        IZegoIMSendCustomCommandCallback callback) {
        Log.d(TAG, "sendCustomCommand() called with: command = [" + command + "], toUserList = [" + toUserList
            + "], callback = [" + callback + "]");
        if (engine == null) {
            return;
        }
        engine.sendCustomCommand(currentRoomID, command, toUserList, callback);
    }

    public void sendBarrageMessage(String message, IZegoIMSendBarrageMessageCallback callback) {
        if (engine == null) {
            return;
        }
        engine.sendBarrageMessage(currentRoomID, message, new IZegoIMSendBarrageMessageCallback() {
            @Override
            public void onIMSendBarrageMessageResult(int errorCode, String messageID) {
                if (callback != null) {
                    callback.onIMSendBarrageMessageResult(errorCode, messageID);
                }
                for (IMBarrageMessageListener listener : barrageMessageListenerList) {
                    listener.onIMSendBarrageMessageResult(errorCode, message, messageID);
                }
            }
        });
    }

    public void addRoomStateChangeListener(RoomStateChangeListener listener) {
        roomStateChangeListenerList.add(listener);
    }

    public void removeRoomStateChangeListener(RoomStateChangeListener listener) {
        roomStateChangeListenerList.remove(listener);
    }

    public void addUserChangeListener(RoomUserChangeListener listener) {
        roomUserChangeListenerList.add(listener);
    }

    public void removeUserChangeListener(RoomUserChangeListener listener) {
        roomUserChangeListenerList.remove(listener);
    }

    public void addStreamChangeListener(RoomStreamChangeListener listener) {
        roomStreamChangeListenerList.add(listener);
    }

    public void removeStreamChangeListener(RoomStreamChangeListener listener) {
        roomStreamChangeListenerList.remove(listener);
    }

    public void addCustomCommandListener(IMCustomCommandListener listener) {
        customCommandListenerList.add(listener);
    }

    public void removeCustomCommandListener(IMCustomCommandListener listener) {
        customCommandListenerList.remove(listener);
    }

    public void addBarrageMessageListener(IMBarrageMessageListener listener) {
        barrageMessageListenerList.add(listener);
    }

    public void removeBarrageMessageListener(IMBarrageMessageListener listener) {
        barrageMessageListenerList.remove(listener);
    }

    public void addCameraListener(CameraListener listener) {
        cameraListenerList.add(listener);
    }

    public void removeCameraListener(CameraListener listener) {
        cameraListenerList.remove(listener);
    }

    public void addMicrophoneListener(MicrophoneListener listener) {
        microphoneListenerList.add(listener);
    }

    public void removeMicrophoneListener(MicrophoneListener listener) {
        microphoneListenerList.remove(listener);
    }

    public void addRoomExtraInfoListener(RoomExtraInfoListener listener) {
        roomExtraInfoListenerList.add(listener);
    }

    public void removeRoomExtraInfoListener(RoomExtraInfoListener listener) {
        roomExtraInfoListenerList.remove(listener);
    }


    public interface RoomStateChangeListener {

        void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode,
            JSONObject extendedData);
    }

    public interface RoomUserChangeListener {

        void onUserEnter(List<ZEGOCLOUDUser> userList);

        void onUserLeft(List<ZEGOCLOUDUser> userList);
    }

    public interface RoomStreamChangeListener {

        void onStreamAdd(List<ZEGOCLOUDUser> userList);

        void onStreamRemove(List<ZEGOCLOUDUser> userList);
    }

    public interface IMCustomCommandListener {

        void onIMRecvCustomCommand(String roomID, ZegoUser fromUser, String command);
    }

    public interface IMBarrageMessageListener {

        void onIMRecvBarrageMessage(String roomID, ArrayList<ZegoBarrageMessageInfo> messageList);

        void onIMSendBarrageMessageResult(int errorCode, String message, String messageID);
    }

    public interface CameraListener {

        void onCameraOpen(String userID, boolean open);
    }

    public interface MicrophoneListener {

        void onMicrophoneOpen(String userID, boolean open);
    }

    public interface RoomExtraInfoListener {

        void onRoomExtraInfoUpdate(ArrayList<ZegoRoomExtraInfo> roomExtraInfoList);
    }
}
