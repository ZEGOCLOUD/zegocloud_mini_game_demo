package com.zegocloud.demo.liveaudioroom.internal;

import android.app.Application;
import android.util.Log;
import com.zegocloud.demo.liveaudioroom.internal.invitation.InvitationService;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.AcceptInvitationCallback;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.CancelInvitationCallback;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.ConnectCallback;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.IncomingInvitationListener;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.JoinRoomCallback;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.OutgoingInvitationListener;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.RejectInvitationCallback;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.SendInvitationCallback;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.ZEGOInvitation;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOCLOUDUser;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.callback.ZIMLoggedInCallback;
import im.zego.zim.callback.ZIMMessageSentCallback;
import im.zego.zim.callback.ZIMRoomAttributesBatchOperatedCallback;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.callback.ZIMRoomAttributesQueriedCallback;
import im.zego.zim.callback.ZIMRoomEnteredCallback;
import im.zego.zim.callback.ZIMRoomLeftCallback;
import im.zego.zim.callback.ZIMUserAvatarUrlUpdatedCallback;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMAppConfig;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageSendConfig;
import im.zego.zim.entity.ZIMRoomAdvancedConfig;
import im.zego.zim.entity.ZIMRoomAttributesBatchOperationConfig;
import im.zego.zim.entity.ZIMRoomAttributesDeleteConfig;
import im.zego.zim.entity.ZIMRoomAttributesSetConfig;
import im.zego.zim.entity.ZIMRoomAttributesUpdateInfo;
import im.zego.zim.entity.ZIMRoomFullInfo;
import im.zego.zim.entity.ZIMRoomInfo;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.entity.ZIMUsersInfoQueryConfig;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMRoomAttributesUpdateAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ZIMService {

    private Application application;
    private final AtomicBoolean isZIMInited = new AtomicBoolean();
    private ZIM zim;
    private String mRoomID;
    private static final String TAG = "ZIMService";
    private List<RoomAttributeListener> roomAttributeListenerList = new ArrayList<>();
    private List<UserAvatarListener> userAvatarListenerList = new ArrayList<>();
    private List<PeerMessageListener> peerMessageListenerList = new ArrayList<>();
    private Map<String, String> usersAvatarUrlMap = new HashMap<>();
    private InvitationService invitationService;

    public void initSDK(Application application, long appID, String appSign) {
        this.application = application;
        boolean result = isZIMInited.compareAndSet(false, true);
        if (!result) {
            return;
        }
        ZIMAppConfig zimAppConfig = new ZIMAppConfig();
        zimAppConfig.appID = appID;
        zimAppConfig.appSign = appSign;
        zim = ZIM.create(zimAppConfig, application);

        invitationService = new InvitationService();
        zim.setEventHandler(new ZIMEventHandler() {
            @Override
            public void onReceiveRoomMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromRoomID) {
                super.onReceiveRoomMessage(zim, messageList, fromRoomID);

                invitationService.onReceiveRoomMessage(zim, messageList, fromRoomID);
                Log.d(TAG, "onReceiveRoomMessage() called with: zim = [" + zim + "], messageList = [" + messageList
                    + "], fromRoomID = [" + fromRoomID + "]");
            }

            @Override
            public void onReceivePeerMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromUserID) {
                super.onReceivePeerMessage(zim, messageList, fromUserID);
                Log.d(TAG, "onReceivePeerMessage() called with: zim = [" + zim + "], messageList = [" + messageList
                    + "], fromUserID = [" + fromUserID + "]");
                for (PeerMessageListener listener : peerMessageListenerList) {
                    listener.onReceivePeerMessage(messageList, fromUserID);
                }
            }

            @Override
            public void onRoomAttributesUpdated(ZIM zim, ZIMRoomAttributesUpdateInfo info, String roomID) {
                super.onRoomAttributesUpdated(zim, info, roomID);

                HashMap<String, String> roomAttributes = info.roomAttributes;
                List<Map<String, String>> setProperties;
                List<Map<String, String>> deleteProperties;
                if (info.action == ZIMRoomAttributesUpdateAction.SET) {
                    setProperties = Collections.singletonList(roomAttributes);
                } else {
                    setProperties = new ArrayList<>();
                }
                if (info.action == ZIMRoomAttributesUpdateAction.DELETE) {
                    deleteProperties = Collections.singletonList(roomAttributes);
                } else {
                    deleteProperties = new ArrayList<>();
                }
                for (RoomAttributeListener listener : roomAttributeListenerList) {
                    listener.onRoomAttributesUpdated(setProperties, deleteProperties);
                }
            }


            @Override
            public void onRoomAttributesBatchUpdated(ZIM zim, ArrayList<ZIMRoomAttributesUpdateInfo> infos,
                String roomID) {
                super.onRoomAttributesBatchUpdated(zim, infos, roomID);

                List<Map<String, String>> setProperties = new ArrayList<>();
                List<Map<String, String>> deleteProperties = new ArrayList<>();
                for (ZIMRoomAttributesUpdateInfo info : infos) {
                    if (info.action == ZIMRoomAttributesUpdateAction.SET) {
                        setProperties.add(info.roomAttributes);
                    } else if (info.action == ZIMRoomAttributesUpdateAction.DELETE) {
                        deleteProperties.add(info.roomAttributes);
                    }
                }
                for (RoomAttributeListener listener : roomAttributeListenerList) {
                    listener.onRoomAttributesUpdated(setProperties, deleteProperties);
                }
            }
        });
    }

    public void connectUser(String userID, String userName, ConnectCallback callback) {
        ZIMUserInfo zimUserInfo = new ZIMUserInfo();
        zimUserInfo.userID = userID;
        zimUserInfo.userName = userName;

        if (zim == null) {
            return;
        }
        zim.login(zimUserInfo, "", new ZIMLoggedInCallback() {

            public void onLoggedIn(ZIMError errorInfo) {
                if (callback != null) {
                    int code = errorInfo.code == ZIMErrorCode.USER_HAS_ALREADY_LOGGED ? ZIMErrorCode.SUCCESS.value()
                        : errorInfo.code.value();
                    callback.onResult(code, errorInfo.message);
                }
            }
        });
    }


    public void disconnectUser() {
        if (zim == null) {
            return;
        }
        invitationService.clearListeners();
        peerMessageListenerList.clear();
        leaveRoom();
        zim.logout();
    }

    public void joinRoom(String roomID, JoinRoomCallback callback) {
        if (zim == null) {
            return;
        }
        ZIMRoomInfo zimRoomInfo = new ZIMRoomInfo();
        zimRoomInfo.roomID = roomID;
        ZIMRoomAdvancedConfig config = new ZIMRoomAdvancedConfig();
        if (ZIM.getInstance() == null) {
            return;
        }
        ZIM.getInstance().enterRoom(zimRoomInfo, config, new ZIMRoomEnteredCallback() {

            public void onRoomEntered(ZIMRoomFullInfo roomInfo, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    mRoomID = roomID;
                }
                if (callback != null) {
                    callback.onResult(errorInfo.code.value(), errorInfo.message);
                }
            }
        });
    }

    public void leaveRoom() {
        if (zim == null) {
            return;
        }
        roomAttributeListenerList.clear();
        userAvatarListenerList.clear();
        invitationService.clearInvitations();
        zim.leaveRoom(mRoomID, new ZIMRoomLeftCallback() {
            @Override
            public void onRoomLeft(String roomID, ZIMError errorInfo) {

            }
        });
    }

    public void sendMessage(ZIMMessage message, String toConversationID, ZIMConversationType conversationType,
        ZIMMessageSendConfig config, ZIMMessageSentCallback callback) {
        if (zim == null) {
            return;
        }
        zim.sendMessage(message, toConversationID, conversationType, config, callback);
    }

    public void sendRoomMessage(ZIMMessage message, ZIMMessageSentCallback callback) {
        if (zim == null) {
            return;
        }
        zim.sendMessage(message, mRoomID, ZIMConversationType.ROOM, new ZIMMessageSendConfig(), callback);
    }

    public void setRoomAttributes(String key, String value, boolean force, ZIMRoomAttributesOperatedCallback callback) {
        if (zim == null) {
            return;
        }
        ZIMRoomAttributesSetConfig config = new ZIMRoomAttributesSetConfig();
        config.isDeleteAfterOwnerLeft = true;
        config.isForce = force;
        config.isUpdateOwner = force;
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put(key, value);
        zim.setRoomAttributes(attributes, mRoomID, config, callback);
    }

    public void beginRoomPropertiesBatchOperation() {
        if (zim == null) {
            return;
        }
        ZIMRoomAttributesBatchOperationConfig config = new ZIMRoomAttributesBatchOperationConfig();
        config.isForce = true;
        config.isDeleteAfterOwnerLeft = false;
        config.isUpdateOwner = false;
        zim.beginRoomAttributesBatchOperation(mRoomID, config);
    }

    public void endRoomPropertiesBatchOperation(ZIMRoomAttributesBatchOperatedCallback callback) {
        if (zim == null) {
            return;
        }
        zim.endRoomAttributesBatchOperation(mRoomID, callback);
    }

    public void deleteRoomAttributes(List<String> keys, ZIMRoomAttributesOperatedCallback callback) {
        if (zim == null) {
            return;
        }
        ZIMRoomAttributesDeleteConfig config = new ZIMRoomAttributesDeleteConfig();
        config.isForce = true;
        zim.deleteRoomAttributes(keys, mRoomID, config, callback);
    }

    public void queryRoomProperties(String roomID, ZIMRoomAttributesQueriedCallback callback) {
        if (zim == null) {
            return;
        }
        zim.queryRoomAllAttributes(roomID, new ZIMRoomAttributesQueriedCallback() {

            public void onRoomAttributesQueried(String roomID, HashMap<String, String> roomAttributes,
                ZIMError errorInfo) {
                if (callback != null) {
                    callback.onRoomAttributesQueried(roomID, roomAttributes, errorInfo);
                }
            }
        });
    }

    public void updateUserAvatarUrl(String url, ZIMUserAvatarUrlUpdatedCallback callback) {
        if (zim == null) {
            return;
        }
        zim.updateUserAvatarUrl(url, new ZIMUserAvatarUrlUpdatedCallback() {
            @Override
            public void onUserAvatarUrlUpdated(String userAvatarUrl, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
                    Log.d(TAG,
                        "onUserAvatarUrlUpdated()111 called with: userAvatarUrl = [" + userAvatarUrl
                            + "], errorInfo = ["
                            + errorInfo + "]");
                    usersAvatarUrlMap.put(localUser.userID, url);
                }
                if (callback != null) {
                    callback.onUserAvatarUrlUpdated(userAvatarUrl, errorInfo);
                }
            }
        });
    }

    public void queryUsersInfo(List<String> userIDList, ZIMUsersInfoQueriedCallback callback) {
        Log.d(TAG, "queryUsersInfo() called with: userIDList = [" + userIDList + "], callback = [" + callback + "]");
        if (zim == null) {
            return;
        }
        ZIMUsersInfoQueryConfig config = new ZIMUsersInfoQueryConfig();
        zim.queryUsersInfo(userIDList, config, new ZIMUsersInfoQueriedCallback() {
            @Override
            public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList,
                ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {
                for (ZIMUserFullInfo zimUserFullInfo : userList) {
                    String userID = zimUserFullInfo.baseInfo.userID;
                    String beforeValue = usersAvatarUrlMap.get(userID);
                    Log.d(TAG, "onUsersInfoQueried() called with: userList = [" + userList + "], errorUserList = ["
                        + errorUserList + "], errorInfo = [" + errorInfo + "]");
                    usersAvatarUrlMap.put(userID, zimUserFullInfo.userAvatarUrl);

                    if (!zimUserFullInfo.userAvatarUrl.equals(beforeValue)) {
                        for (UserAvatarListener userAvatarListener : userAvatarListenerList) {
                            userAvatarListener.onUserAvatarUpdated(userID, zimUserFullInfo.userAvatarUrl);
                        }
                    }
                }
                if (callback != null) {
                    callback.onUsersInfoQueried(userList, errorUserList, errorInfo);
                }
            }
        });
    }


    public String getUserAvatar(String userID) {
        return usersAvatarUrlMap.get(userID);
    }

    public void addRoomAttributeListener(RoomAttributeListener listener) {
        roomAttributeListenerList.add(listener);
    }

    public void removeRoomAttributeListener(RoomAttributeListener listener) {
        roomAttributeListenerList.remove(listener);
    }

    public void addUserAvatarListener(UserAvatarListener listener) {
        userAvatarListenerList.add(listener);
    }

    public void removeUserAvatarListener(UserAvatarListener listener) {
        userAvatarListenerList.remove(listener);
    }

    public void setBusy(boolean busy) {
        invitationService.setBusy(busy);
    }

    public boolean isBusy() {
        return invitationService.isBusy();
    }

    public ZEGOInvitation getUserInvitation(String userID) {
        return invitationService.getUserInvitation(userID);
    }

    public void inviteUser(String userID, String extendedData, SendInvitationCallback sendInvitationCallback) {
        invitationService.inviteUser(userID, extendedData, sendInvitationCallback);
    }

    public void acceptInvite(ZEGOInvitation invitation, AcceptInvitationCallback acceptInvitationCallback) {
        invitationService.acceptInvite(invitation, acceptInvitationCallback);
    }

    public void rejectInvite(ZEGOInvitation invitation, RejectInvitationCallback rejectInvitationCallback) {
        invitationService.rejectInvite(invitation, rejectInvitationCallback);
    }

    public List<ZEGOCLOUDUser> getOtherUserInviteList() {
        return invitationService.getOtherUserInviteList();
    }

    public void addOutgoingInvitationListener(OutgoingInvitationListener listener) {
        invitationService.addOutgoingInvitationListener(listener);
    }

    public void removeOutgoingInvitationListener(OutgoingInvitationListener listener) {
        invitationService.removeOutgoingInvitationListener(listener);
    }

    public void addIncomingInvitationListener(IncomingInvitationListener listener) {
        invitationService.addIncomingInvitationListener(listener);
    }

    public void removeIncomingInvitationListener(IncomingInvitationListener listener) {
        invitationService.removeIncomingInvitationListener(listener);
    }

    public void addPeerMessageListener(PeerMessageListener listener) {
        peerMessageListenerList.add(listener);
    }

    public void removePeerMessageListener(PeerMessageListener listener) {
        peerMessageListenerList.remove(listener);
    }

    public void cancelInvite(ZEGOInvitation zegoInvitation, CancelInvitationCallback cancelInvitationCallback) {
        invitationService.cancelInvite(zegoInvitation, cancelInvitationCallback);
    }

    public ZEGOInvitation getZEGOInvitation(String invitationID) {
        return invitationService.getZEGOInvitation(invitationID);
    }

    public interface RoomAttributeListener {

        void onRoomAttributesUpdated(List<Map<String, String>> setProperties,
            List<Map<String, String>> deleteProperties);
    }

    public interface UserAvatarListener {

        void onUserAvatarUpdated(String userID, String url);
    }

    public interface PeerMessageListener {

        void onReceivePeerMessage(ArrayList<ZIMMessage> messageList, String fromUserID);
    }
}
