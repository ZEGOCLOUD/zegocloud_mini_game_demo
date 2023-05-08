package com.zegocloud.demo.liveaudioroom;

import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.zegocloud.demo.liveaudioroom.backend.Backend;
import com.zegocloud.demo.liveaudioroom.backend.Backend.Result;
import com.zegocloud.demo.liveaudioroom.backend.BackendUser;
import com.zegocloud.demo.liveaudioroom.backend.Game;
import com.zegocloud.demo.liveaudioroom.components.ZEGOLiveAudioRoomLayoutConfig;
import com.zegocloud.demo.liveaudioroom.components.ZEGOLiveAudioRoomLayoutRowConfig;
import com.zegocloud.demo.liveaudioroom.components.ZEGOLiveAudioRoomSeat;
import com.zegocloud.demo.liveaudioroom.internal.ZEGOSDKManager;
import com.zegocloud.demo.liveaudioroom.internal.ZIMService.RoomAttributeListener;
import com.zegocloud.demo.liveaudioroom.internal.ZIMService.UserAvatarListener;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOCLOUDUser;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOExpressService.RoomExtraInfoListener;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOExpressService.RoomUserChangeListener;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import im.zego.zim.callback.ZIMRoomAttributesBatchOperatedCallback;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.callback.ZIMUserAvatarUrlUpdatedCallback;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public class ZEGOLiveAudioRoomManager {

    private static final class Holder {

        private static final ZEGOLiveAudioRoomManager INSTANCE = new ZEGOLiveAudioRoomManager();
    }

    public static ZEGOLiveAudioRoomManager getInstance() {
        return ZEGOLiveAudioRoomManager.Holder.INSTANCE;
    }

    private static final String KEY = "audioRoom";
    private JSONObject jsonObject = new JSONObject();
    private String hostUserID;
    private List<SeatUserChangeListener> seatUserChangeListenerList = new ArrayList<>();
    private List<SeatLockChangeListener> seatLockChangeListenerList = new ArrayList<>();
    private List<HostChangeListener> hostChangeListenerList = new ArrayList<>();
    private List<ZEGOLiveAudioRoomSeat> seatList = new ArrayList<>();
    private boolean batchOperation = false;
    private boolean lockSeat = false;
    private int hostSeatIndex = 0;
    private static final String TAG = "ZEGOLiveAudioRoomManage";


    private Backend backend = new Backend();
    private BackendUser backendUser;
    private Gson gson = new Gson();
    private String code;
    private GameModeListener gameModeListener;
    private Game currentGame;
    private List<Game> audioRoomGameList = Arrays.asList(new Game(1468180338417074177L, "Ludo", 10),
        new Game(1472142559912517633L, "UMO", 10));

    public void initBackend() {
        backend.init();
    }

    public void coinsConsumption(String uid, int coin, Result result) {
        backend.coinsConsumption(uid, coin, result);
    }

    public void playerMatch(String uid, long gm_id, Result result) {
        backend.playerMatch(uid, gm_id, result);
    }

    public BackendUser getBackendUser() {
        return backendUser;
    }

    public String getCode() {
        return code;
    }

    public void loginBackend(String userID, Result result) {
        backend.login(userID, new Result() {
            @Override
            public void onResult(int errorCode, String message) {
                if (errorCode == 0) {
                    backendUser = gson.fromJson(message, BackendUser.class);
                    getCode(result);
                } else {
                    if (result != null) {
                        result.onResult(errorCode, message);
                    }
                }
            }
        });
    }

    public void getCode(Result result) {
        String userID = this.backendUser.getUid();
        backend.getCode(userID, new Result() {
            @Override
            public void onResult(int errorCode, String message) {
                JSONObject object = null;
                try {
                    object = new JSONObject(message);
                    code = (String) object.get("code");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                if (result != null) {
                    result.onResult(errorCode, message);
                }
            }
        });
    }

    public void startGameMode(Game game) {
        try {
            jsonObject.put("gameID", game.gameID);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        ZEGOSDKManager.getInstance().rtcService.setRTCRoomExtraInfo(KEY, jsonObject.toString());
    }

    public void stopGameMode() {
        try {
            jsonObject.put("gameID", 0);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        ZEGOSDKManager.getInstance().rtcService.setRTCRoomExtraInfo(KEY, jsonObject.toString());
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public void setGameModeListener(GameModeListener gameModeListener) {
        this.gameModeListener = gameModeListener;
    }

    public interface GameModeListener {

        void onGameModeStart(Game game);

        void onGameModeEnd();
    }

    public void init() {
        ZEGOSDKManager.getInstance().rtcService.addRoomExtraInfoListener(new RoomExtraInfoListener() {
            @Override
            public void onRoomExtraInfoUpdate(ArrayList<ZegoRoomExtraInfo> roomExtraInfoList) {
                for (ZegoRoomExtraInfo extraInfo : roomExtraInfoList) {
                    if (extraInfo.key.equals(KEY)) {
                        try {
                            jsonObject = new JSONObject(extraInfo.value);
                            if (jsonObject.has("host")) {
                                String tempUserID = (String) jsonObject.get("host");
                                boolean notifyHostChange = !Objects.equals(tempUserID, hostUserID);
                                hostUserID = tempUserID;
                                if (notifyHostChange && getHostUser() != null) {
                                    for (HostChangeListener listener : hostChangeListenerList) {
                                        listener.onHostChanged(getHostUser());
                                    }
                                }
                            }
                            if (jsonObject.has("lockseat")) {
                                boolean temp = (boolean) jsonObject.get("lockseat");
                                boolean changed = lockSeat != temp;
                                lockSeat = temp;
                                if (changed) {
                                    for (SeatLockChangeListener listener : seatLockChangeListenerList) {
                                        listener.onSeatLockChanged(temp);
                                    }
                                }
                            }
                            if (jsonObject.has("gameID")) {
                                long temp = jsonObject.getLong("gameID");
                                if (temp == 0) {
                                    // exit
                                    currentGame = null;
                                    if (gameModeListener != null) {
                                        gameModeListener.onGameModeEnd();
                                    }
                                } else {
                                    if (currentGame != null && currentGame.gameID == temp) {
                                        // equal,dont action
                                    } else {
                                        for (Game game : audioRoomGameList) {
                                            if (game.gameID == temp) {
                                                currentGame = game;
                                                break;
                                            }
                                        }
                                        if (gameModeListener != null) {
                                            gameModeListener.onGameModeStart(currentGame);
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        ZEGOSDKManager.getInstance().rtcService.addUserChangeListener(new RoomUserChangeListener() {
            @Override
            public void onUserEnter(List<ZEGOCLOUDUser> userList) {
                List<ZEGOLiveAudioRoomSeat> changedSeatList = new ArrayList<>();
                for (ZEGOCLOUDUser zegocloudUser : userList) {
                    if (!TextUtils.isEmpty(hostUserID)) {
                        if (Objects.equals(zegocloudUser.userID, hostUserID)) {
                            for (HostChangeListener listener : hostChangeListenerList) {
                                listener.onHostChanged(zegocloudUser);
                            }
                        }
                    }
                    for (ZEGOLiveAudioRoomSeat seat : seatList) {
                        if (zegocloudUser.equals(seat.getUser())) {
                            if (!Objects.equals(zegocloudUser.userName, seat.getUser().userName)) {
                                seat.setUser(zegocloudUser);
                                changedSeatList.add(seat);
                            }
                        }
                    }
                }
                if (!changedSeatList.isEmpty()) {
                    for (SeatUserChangeListener listener : seatUserChangeListenerList) {
                        listener.onSeatChanged(changedSeatList);
                    }
                }
            }

            @Override
            public void onUserLeft(List<ZEGOCLOUDUser> userList) {

            }
        });
        ZEGOSDKManager.getInstance().zimService.addRoomAttributeListener(new RoomAttributeListener() {
            @Override
            public void onRoomAttributesUpdated(List<Map<String, String>> setProperties,
                List<Map<String, String>> deleteProperties) {
                Log.d(TAG, "onRoomAttributesUpdated() called with: setProperties = [" + setProperties
                    + "], deleteProperties = [" + deleteProperties + "]");
                List<ZEGOLiveAudioRoomSeat> changedSeats = new ArrayList<>();
                for (Map<String, String> setProperty : setProperties) {
                    for (Map.Entry<String, String> entry : setProperty.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        ZEGOLiveAudioRoomSeat seat = seatList.get(Integer.parseInt(key));
                        ZEGOCLOUDUser cloudUser = ZEGOSDKManager.getInstance().rtcService.getUser(value);
                        if (cloudUser == null) {
                            cloudUser = new ZEGOCLOUDUser(value, value);
                        }
                        seat.setUser(cloudUser);
                        changedSeats.add(seat);
                    }
                }
                for (Map<String, String> deleteProperty : deleteProperties) {
                    for (Map.Entry<String, String> entry : deleteProperty.entrySet()) {
                        String key = entry.getKey();
                        ZEGOLiveAudioRoomSeat seat = seatList.get(Integer.parseInt(key));
                        seat.setUser(null);
                        changedSeats.add(seat);
                    }
                }
                for (SeatUserChangeListener listener : seatUserChangeListenerList) {
                    listener.onSeatChanged(changedSeats);
                }
            }
        });
    }

    public List<Game> getAudioRoomGameList() {
        return audioRoomGameList;
    }

    public void setSeatConfig(ZEGOLiveAudioRoomLayoutConfig layoutConfig) {
        seatList.clear();
        for (int rowIndex = 0; rowIndex < layoutConfig.rowConfigs.size(); rowIndex++) {
            ZEGOLiveAudioRoomLayoutRowConfig rowConfig = layoutConfig.rowConfigs.get(rowIndex);
            for (int columnIndex = 0; columnIndex < rowConfig.count; columnIndex++) {
                ZEGOLiveAudioRoomSeat audioRoomSeat = new ZEGOLiveAudioRoomSeat();
                audioRoomSeat.rowIndex = rowIndex;
                audioRoomSeat.columnIndex = columnIndex;
                audioRoomSeat.seatIndex = seatList.size();
                seatList.add(audioRoomSeat);
            }
        }
    }

    public void updateSeatConfig(ZEGOLiveAudioRoomLayoutConfig layoutConfig) {
        int seatIndex = 0;
        for (int rowIndex = 0; rowIndex < layoutConfig.rowConfigs.size(); rowIndex++) {
            ZEGOLiveAudioRoomLayoutRowConfig rowConfig = layoutConfig.rowConfigs.get(rowIndex);
            for (int columnIndex = 0; columnIndex < rowConfig.count; columnIndex++) {
                ZEGOLiveAudioRoomSeat audioRoomSeat = seatList.get(seatIndex);
                audioRoomSeat.rowIndex = rowIndex;
                audioRoomSeat.columnIndex = columnIndex;
                seatIndex++;
            }
        }
    }

    public void lockSeat(boolean lock) {
        try {
            jsonObject.put("lockseat", lock);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        ZEGOSDKManager.getInstance().rtcService.setRTCRoomExtraInfo(KEY, jsonObject.toString());
    }

    public boolean isSeatLocked() {
        return lockSeat;
    }

    public void setSelfHost() {
        ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
        try {
            jsonObject.put("host", localUser.userID);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        ZEGOSDKManager.getInstance().rtcService.setRTCRoomExtraInfo(KEY, jsonObject.toString());
    }

    public ZEGOCLOUDUser getHostUser() {
        if (TextUtils.isEmpty(hostUserID)) {
            return null;
        }
        return ZEGOSDKManager.getInstance().rtcService.getUser(hostUserID);
    }

    public void addHostChangeListener(HostChangeListener listener) {
        this.hostChangeListenerList.add(listener);
    }

    public void removeHostChangeListener(HostChangeListener listener) {
        this.hostChangeListenerList.remove(listener);
    }

    public interface HostChangeListener {

        void onHostChanged(ZEGOCLOUDUser hostUser);
    }

    public List<ZEGOLiveAudioRoomSeat> getAudioRoomSeatList() {
        return seatList;
    }

    public int getSeatCount() {
        return seatList.size();
    }

    public int findFirstAvailableSeatIndex() {
        int firstEmptyIndex = -1;
        for (int i = 0; i < seatList.size(); i++) {
            if (i == hostSeatIndex) {
                continue;
            }
            if (seatList.get(i).isEmpty()) {
                firstEmptyIndex = i;
                break;
            }
        }
        return firstEmptyIndex;
    }

    public ZEGOLiveAudioRoomSeat findUserRoomSeat(String userID) {
        ZEGOLiveAudioRoomSeat seat = null;
        for (int i = 0; i < seatList.size(); i++) {
            ZEGOLiveAudioRoomSeat audioRoomSeat = seatList.get(i);
            if (audioRoomSeat.isNotEmpty() && Objects.equals(userID, audioRoomSeat.getUser().userID)) {
                seat = audioRoomSeat;
                break;
            }
        }
        return seat;
    }

    public int findMyRoomSeatIndex() {
        ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
        ZEGOLiveAudioRoomSeat userRoomSeat = findUserRoomSeat(localUser.userID);
        if (userRoomSeat != null) {
            return userRoomSeat.seatIndex;
        } else {
            return -1;
        }
    }

    public ZEGOLiveAudioRoomSeat getAudioRoomSeat(int seatIndex) {
        if (seatIndex < 0 || seatIndex >= seatList.size()) {
            return null;
        }
        return seatList.get(seatIndex);
    }

    public void takeSeat(int seatIndex, ZIMRoomAttributesOperatedCallback callback) {
        ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
        if (localUser == null) {
            return;
        }
        String key = String.valueOf(seatIndex);
        String value = localUser.userID;
        ZEGOSDKManager.getInstance().zimService.setRoomAttributes(key, value, true,
            new ZIMRoomAttributesOperatedCallback() {
                @Override
                public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys, ZIMError errorInfo) {
                    if (callback != null) {
                        callback.onRoomAttributesOperated(roomID, errorKeys, errorInfo);
                    }
                }
            });
    }

    public void switchSeat(int fromSeatIndex, int toSeatIndex, ZIMRoomAttributesBatchOperatedCallback callback) {
        ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
        if (localUser == null) {
            return;
        }
        if (!batchOperation) {
            ZEGOSDKManager.getInstance().zimService.beginRoomPropertiesBatchOperation();
            batchOperation = true;
            tryTakeSeat(toSeatIndex, null);
            emptySeat(fromSeatIndex, null);
            ZEGOSDKManager.getInstance().zimService.endRoomPropertiesBatchOperation(
                new ZIMRoomAttributesBatchOperatedCallback() {
                    @Override
                    public void onRoomAttributesBatchOperated(String roomID, ZIMError errorInfo) {
                        batchOperation = false;
                        if (callback != null) {
                            callback.onRoomAttributesBatchOperated(roomID, errorInfo);
                        }
                    }
                });
        }
    }

    public void tryTakeSeat(int seatIndex, ZIMRoomAttributesOperatedCallback callback) {
        ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
        if (localUser == null) {
            return;
        }
        String key = String.valueOf(seatIndex);
        String value = localUser.userID;
        ZEGOSDKManager.getInstance().zimService.setRoomAttributes(key, value, false,
            new ZIMRoomAttributesOperatedCallback() {
                @Override
                public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys, ZIMError errorInfo) {
                    if (callback != null) {
                        callback.onRoomAttributesOperated(roomID, errorKeys, errorInfo);
                    }
                }
            });
    }

    public void emptySeat(int seatIndex, ZIMRoomAttributesOperatedCallback callback) {
        ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
        if (localUser == null) {
            return;
        }
        List<String> list = Collections.singletonList(String.valueOf(seatIndex));
        ZEGOSDKManager.getInstance().zimService.deleteRoomAttributes(list, new ZIMRoomAttributesOperatedCallback() {
            @Override
            public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys, ZIMError errorInfo) {
                if (callback != null) {
                    callback.onRoomAttributesOperated(roomID, errorKeys, errorInfo);
                }
            }
        });
    }

    public void removeSpeakerFromSeat(String userID, ZIMRoomAttributesOperatedCallback callback) {
        ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
        if (localUser == null) {
            return;
        }
        for (ZEGOLiveAudioRoomSeat seat : seatList) {
            int seatIndex = seat.seatIndex;
            ZEGOCLOUDUser seatUser = seat.getUser();
            if (seatUser != null) {
                String seatUserID = seatUser.userID;
                if (Objects.equals(userID, seatUserID)) {
                    emptySeat(seatIndex, callback);
                    break;
                }
            }

        }
    }

    public void updateUserAvatarUrl(String url, ZIMUserAvatarUrlUpdatedCallback callback) {
        ZEGOSDKManager.getInstance().zimService.updateUserAvatarUrl(url, callback);
    }

    public void queryUsersInfo(List<String> userIDList, ZIMUsersInfoQueriedCallback callback) {
        ZEGOSDKManager.getInstance().zimService.queryUsersInfo(userIDList, callback);
    }

    public String getUserAvatar(String userID) {
        return ZEGOSDKManager.getInstance().zimService.getUserAvatar(userID);
    }

    public void addUserAvatarListener(UserAvatarListener listener) {
        ZEGOSDKManager.getInstance().zimService.addUserAvatarListener(listener);
    }

    public void removeUserAvatarListener(UserAvatarListener listener) {
        ZEGOSDKManager.getInstance().zimService.removeUserAvatarListener(listener);
    }

    public int getHostSeatIndex() {
        return hostSeatIndex;
    }

    public void setHostSeatIndex(int hostSeatIndex) {
        this.hostSeatIndex = hostSeatIndex;
    }

    public void leaveRoom() {
        batchOperation = false;
        lockSeat = false;
        seatList.clear();
        jsonObject = new JSONObject();
        seatLockChangeListenerList.clear();
        hostChangeListenerList.clear();
        seatUserChangeListenerList.clear();
        hostUserID = null;
    }

    public void addSeatUserChangeListener(SeatUserChangeListener listener) {
        this.seatUserChangeListenerList.add(listener);
    }

    public void removeSeatUserChangeListener(SeatUserChangeListener listener) {
        this.seatUserChangeListenerList.remove(listener);
    }

    public void addSeatLockChangeListener(SeatLockChangeListener listener) {
        this.seatLockChangeListenerList.add(listener);
    }

    public void removeSeatLockChangeListener(SeatLockChangeListener listener) {
        this.seatLockChangeListenerList.remove(listener);
    }

    public interface SeatUserChangeListener {

        void onSeatChanged(List<ZEGOLiveAudioRoomSeat> changedSeats);
    }

    public interface SeatLockChangeListener {

        void onSeatLockChanged(boolean lock);
    }
}
