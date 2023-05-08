package com.zegocloud.demo.liveaudioroom.internal.invitation;

import android.util.Log;
import com.zegocloud.demo.liveaudioroom.internal.ZEGOSDKManager;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.AcceptInvitationCallback;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.CancelInvitationCallback;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.IncomingInvitationListener;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.LiveAudioRoomProtocol;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.OutgoingInvitationListener;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.RejectInvitationCallback;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.SendInvitationCallback;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.ZEGOInvitation;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.ZEGOInvitationState;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.ZEGOInviteeState;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOCLOUDUser;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOExpressService;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMMessageSentCallback;
import im.zego.zim.entity.ZIMCommandMessage;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMessageType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class InvitationService {

    private Map<String, ZEGOInvitation> zegoInvitationMap = new HashMap<>();
    private List<OutgoingInvitationListener> outgoingInvitationListenerList = new ArrayList<>();
    private List<IncomingInvitationListener> incomingInvitationListenerList = new ArrayList<>();
    private boolean busy;
    private static final String TAG = "InvitationService";

    public ZEGOInvitation getZEGOInvitation(String invitationID) {
        if (invitationID == null) {
            return null;
        }
        ZEGOInvitation invitation = zegoInvitationMap.get(invitationID);
        if (invitation == null) {
            for (String key : zegoInvitationMap.keySet()) {
                if (key.contains(invitationID)) {
                    return zegoInvitationMap.get(key);
                }
            }
        }
        return invitation;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public boolean isBusy() {
        return busy;
    }

    public void onInComingNewInvitation(String invitationID, String inviterID, String extendedData) {
        Log.d(TAG,
            "onInComingNewInvitation() called with: invitationID = [" + invitationID + "], inviterID = [" + inviterID
                + "], extendedData = [" + extendedData + "]");
        ZEGOInvitation ZEGOInvitation = new ZEGOInvitation();
        ZEGOInvitation.inviter = inviterID;
        ZEGOInvitation.invitationID = invitationID;
        ZEGOInvitation.extendedData = extendedData;
        ZEGOInvitation.invitees = new ArrayList<>();
        ZEGOExpressService rtcService = ZEGOSDKManager.getInstance().rtcService;
        ZEGOCLOUDUser localUser = rtcService.getLocalUser();
        ZEGOInvitation.invitees.add(localUser.userID);
        ZEGOInvitation.inviteeStateMap = new HashMap<>();
        ZEGOInvitation.inviteeStateMap.put(localUser.userID, ZEGOInviteeState.RECV);
        ZEGOInvitation.setState(ZEGOInvitationState.RECV_NEW);
        zegoInvitationMap.put(ZEGOInvitation.invitationID, ZEGOInvitation);

        for (IncomingInvitationListener listener : incomingInvitationListenerList) {
            listener.onReceiveNewInvitation(invitationID, inviterID, extendedData);
        }
    }

    public void onInComingInvitationButIsCancelled(String invitationID, String inviter, String extendedData) {
        Log.d(TAG,
            "onInComingInvitationButIsCancelled() called with: invitationID = [" + invitationID + "], inviter = ["
                + inviter + "], extendedData = [" + extendedData + "]");
        ZEGOInvitation zegoInvitation = getZEGOInvitation(invitationID);
        if (zegoInvitation != null) {
            zegoInvitation.setState(ZEGOInvitationState.RECV_IS_CANCELLED);
        }

        for (IncomingInvitationListener listener : incomingInvitationListenerList) {
            listener.onReceiveInvitationButIsCancelled(invitationID, inviter, extendedData);
        }

        if (zegoInvitation != null) {
            zegoInvitationMap.remove(zegoInvitation.invitationID);
        }
    }

    public void onOutgoingInvitationAndIsAccepted(String invitationID, String invitee, String extendedData) {
        Log.d(TAG, "onOutgoingInvitationAndIsAccepted() called with: invitationID = [" + invitationID + "], invitee = ["
            + invitee + "], extendedData = [" + extendedData + "]");
        ZEGOInvitation zegoInvitation = getZEGOInvitation(invitationID);
        if (zegoInvitation != null) {
            zegoInvitation.setState(ZEGOInvitationState.SEND_IS_ACCEPTED);
            zegoInvitation.inviteeStateMap.put(invitee, ZEGOInviteeState.ACCEPT);
        }

        for (OutgoingInvitationListener listener : outgoingInvitationListenerList) {
            listener.onSendInvitationAndIsAccepted(invitationID, invitee, extendedData);
        }

        if (zegoInvitation != null) {
            zegoInvitationMap.remove(zegoInvitation.invitationID);
        }
    }

    public void onOutgoingInvitationButIsRejected(String invitationID, String invitee, String extendedData) {
        Log.d(TAG, "onOutgoingInvitationButIsRejected() called with: invitationID = [" + invitationID + "], invitee = ["
            + invitee + "], extendedData = [" + extendedData + "]");
        ZEGOInvitation zegoInvitation = getZEGOInvitation(invitationID);
        if (zegoInvitation != null) {
            zegoInvitation.setState(ZEGOInvitationState.SEND_IS_REJECTED);
            zegoInvitation.inviteeStateMap.put(invitee, ZEGOInviteeState.REJECT);
        }

        for (OutgoingInvitationListener listener : outgoingInvitationListenerList) {
            listener.onSendInvitationButIsRejected(invitationID, invitee, extendedData);
        }

        if (zegoInvitation != null) {
            zegoInvitationMap.remove(zegoInvitation.invitationID);
        }
    }


    public void addOutgoingInvitationListener(OutgoingInvitationListener listener) {
        outgoingInvitationListenerList.add(listener);
    }

    public void removeOutgoingInvitationListener(OutgoingInvitationListener listener) {
        outgoingInvitationListenerList.remove(listener);
    }

    public void addIncomingInvitationListener(IncomingInvitationListener listener) {
        incomingInvitationListenerList.add(listener);
    }

    public void removeIncomingInvitationListener(IncomingInvitationListener listener) {
        incomingInvitationListenerList.remove(listener);
    }

    public void inviteUser(String userID, String extendedData, SendInvitationCallback callback) {
        Log.d(TAG,
            "inviteUser() called with: userID = [" + userID + "], extendedData = [" + extendedData + "], callback = ["
                + callback + "]");
        ZEGOInvitation zegoInvitation = new ZEGOInvitation();
        zegoInvitation.invitees = Collections.singletonList(userID);
        zegoInvitation.extendedData = extendedData;
        ZEGOExpressService rtcService = ZEGOSDKManager.getInstance().rtcService;
        ZEGOCLOUDUser localUser = rtcService.getLocalUser();
        zegoInvitation.inviter = localUser.userID;
        zegoInvitation.inviteeStateMap = new HashMap<>();

        ZEGOCLOUDUser targetUser = rtcService.getUser(userID);
        if (targetUser == null) {
            return;
        }
        byte[] bytes = extendedData.getBytes(StandardCharsets.UTF_8);
        ZIMCommandMessage commandMessage = new ZIMCommandMessage(bytes);

        ZEGOSDKManager.getInstance().zimService.sendRoomMessage(commandMessage, new ZIMMessageSentCallback() {
            @Override
            public void onMessageAttached(ZIMMessage message) {
            }

            @Override
            public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                List<String> errorInvitees = new ArrayList<>();
                if (errorInfo.code != ZIMErrorCode.SUCCESS) {
                    errorInvitees.add(userID);
                }
                if (errorInfo.code.value() == 0) {
                    zegoInvitation.invitationID = String.valueOf(message.getMessageID());
                    zegoInvitation.setState(ZEGOInvitationState.SEND_NEW);
                    for (String invitee : errorInvitees) {
                        zegoInvitation.inviteeStateMap.put(invitee, ZEGOInviteeState.UNKNOWN);
                    }
                    List<String> successUserList = new ArrayList<>(zegoInvitation.invitees);
                    successUserList.removeAll(errorInvitees);
                    for (String invitee : successUserList) {
                        zegoInvitation.inviteeStateMap.put(invitee, ZEGOInviteeState.RECV);
                    }
                    zegoInvitationMap.put(zegoInvitation.invitationID, zegoInvitation);
                }
                if (callback != null) {
                    callback.onResult(errorInfo.code.value(), zegoInvitation.invitationID, errorInvitees);
                }
                for (OutgoingInvitationListener outgoingInvitationListener : outgoingInvitationListenerList) {
                    outgoingInvitationListener.onActionSendInvitation(errorInfo.code.value(),
                        zegoInvitation.invitationID, extendedData, errorInvitees);
                }
            }
        });
    }


    public void acceptInvite(ZEGOInvitation invitation, AcceptInvitationCallback callback) {
        Log.d(TAG, "acceptInvite() called with: invitation = [" + invitation + "], callback = [" + callback + "]");
        ZEGOExpressService rtcService = ZEGOSDKManager.getInstance().rtcService;
        ZEGOCLOUDUser localUser = rtcService.getLocalUser();
        ZEGOCLOUDUser targetUser = rtcService.getUser(invitation.inviter);
        if (localUser == null || targetUser == null) {
            return;
        }
        LiveAudioRoomProtocol protocol = LiveAudioRoomProtocol.parse(invitation.extendedData);
        protocol.accept();

        byte[] bytes = protocol.toString().getBytes(StandardCharsets.UTF_8);
        ZIMCommandMessage commandMessage = new ZIMCommandMessage(bytes);
        commandMessage.extendedData = invitation.invitationID;

        ZEGOSDKManager.getInstance().zimService.sendRoomMessage(commandMessage, new ZIMMessageSentCallback() {
            @Override
            public void onMessageAttached(ZIMMessage message) {

            }

            @Override
            public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                ZEGOInvitation zegoInvitation = getZEGOInvitation(invitation.invitationID);
                if (zegoInvitation != null) {
                    zegoInvitation.setState(ZEGOInvitationState.RECV_ACCEPT);
                    ZEGOExpressService rtcService = ZEGOSDKManager.getInstance().rtcService;
                    ZEGOCLOUDUser localUser = rtcService.getLocalUser();
                    zegoInvitation.inviteeStateMap.put(localUser.userID, ZEGOInviteeState.ACCEPT);
                }

                if (callback != null) {
                    callback.onResult(errorInfo.code.value(), invitation.invitationID);
                }
                for (IncomingInvitationListener incomingInvitationListener : incomingInvitationListenerList) {
                    incomingInvitationListener.onActionAcceptInvitation(errorInfo.code.value(),
                        invitation.invitationID);
                }

                if (zegoInvitation != null) {
                    zegoInvitationMap.remove(zegoInvitation.invitationID);
                }
            }
        });
    }


    public void rejectInvite(ZEGOInvitation invitation, RejectInvitationCallback callback) {
        Log.d(TAG, "rejectInvite() called with: invitation = [" + invitation + "], callback = [" + callback + "]");
        ZEGOExpressService rtcService = ZEGOSDKManager.getInstance().rtcService;
        ZEGOCLOUDUser localUser = rtcService.getLocalUser();
        ZEGOCLOUDUser targetUser = rtcService.getUser(invitation.inviter);
        if (localUser == null || targetUser == null) {
            return;
        }
        LiveAudioRoomProtocol protocol = LiveAudioRoomProtocol.parse(invitation.extendedData);
        protocol.reject();
        byte[] bytes = protocol.toString().getBytes(StandardCharsets.UTF_8);
        ZIMCommandMessage commandMessage = new ZIMCommandMessage(bytes);
        commandMessage.extendedData = invitation.invitationID;

        ZEGOSDKManager.getInstance().zimService.sendRoomMessage(commandMessage, new ZIMMessageSentCallback() {
            @Override
            public void onMessageAttached(ZIMMessage message) {

            }

            @Override
            public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                if (callback != null) {
                    callback.onResult(errorInfo.code.value(), invitation.invitationID);
                }

                ZEGOInvitation zegoInvitation = getZEGOInvitation(invitation.invitationID);
                if (zegoInvitation != null) {
                    zegoInvitation.setState(ZEGOInvitationState.RECV_REJECT);
                    ZEGOExpressService rtcService = ZEGOSDKManager.getInstance().rtcService;
                    ZEGOCLOUDUser localUser = rtcService.getLocalUser();
                    zegoInvitation.inviteeStateMap.put(localUser.userID, ZEGOInviteeState.REJECT);
                }

                if (callback != null) {
                    callback.onResult(errorInfo.code.value(), invitation.invitationID);
                }

                for (IncomingInvitationListener incomingInvitationListener : incomingInvitationListenerList) {
                    incomingInvitationListener.onActionRejectInvitation(errorInfo.code.value(),
                        invitation.invitationID);
                }

                if (zegoInvitation != null) {
                    zegoInvitationMap.remove(zegoInvitation.invitationID);
                }
            }
        });
    }


    public void cancelInvite(ZEGOInvitation invitation, CancelInvitationCallback callback) {
        Log.d(TAG, "cancelInvite() called with: invitation = [" + invitation + "], callback = [" + callback + "]");
        ZEGOExpressService rtcService = ZEGOSDKManager.getInstance().rtcService;
        ZEGOCLOUDUser localUser = rtcService.getLocalUser();
        ZEGOCLOUDUser targetUser = rtcService.getUser(invitation.inviter);
        if (localUser == null || targetUser == null) {
            return;
        }
        LiveAudioRoomProtocol protocol = LiveAudioRoomProtocol.parse(invitation.extendedData);
        protocol.cancel();
        byte[] bytes = protocol.toString().getBytes(StandardCharsets.UTF_8);
        ZIMCommandMessage commandMessage = new ZIMCommandMessage(bytes);
        commandMessage.extendedData = invitation.invitationID;

        ZEGOSDKManager.getInstance().zimService.sendRoomMessage(commandMessage, new ZIMMessageSentCallback() {
            @Override
            public void onMessageAttached(ZIMMessage message) {

            }

            @Override
            public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                if (callback != null) {
                    List<String> errorInvitees = new ArrayList<>();
                    if (errorInfo.code != ZIMErrorCode.SUCCESS) {
                        errorInvitees.add(targetUser.userID);
                    }
                    callback.onResult(errorInfo.code.value(), invitation.invitationID, errorInvitees);
                    ZEGOInvitation zegoInvitation = getZEGOInvitation(invitation.invitationID);
                    if (zegoInvitation != null) {
                        zegoInvitation.setState(ZEGOInvitationState.SEND_CANCEL);
                    }

                    if (callback != null) {
                        callback.onResult(errorInfo.code.value(), invitation.invitationID, errorInvitees);
                    }
                    for (OutgoingInvitationListener outgoingInvitationListener : outgoingInvitationListenerList) {
                        outgoingInvitationListener.onActionCancelInvitation(errorInfo.code.value(),
                            invitation.invitationID, errorInvitees);
                    }

                    if (zegoInvitation != null) {
                        zegoInvitationMap.remove(zegoInvitation.invitationID);
                    }
                }
            }
        });
    }

    public void clearInvitations() {
        zegoInvitationMap.clear();
        busy = false;
    }

    public void clearListeners() {
        outgoingInvitationListenerList.clear();
        incomingInvitationListenerList.clear();
    }

    public ZEGOInvitation getUserInvitation(String inviterUserID) {
        for (ZEGOInvitation invitation : zegoInvitationMap.values()) {
            if (!invitation.isFinished()) {
                if (Objects.equals(invitation.inviter, inviterUserID)) {
                    return invitation;
                }
            }
        }
        return null;
    }

    public List<ZEGOCLOUDUser> getOtherUserInviteList() {
        List<ZEGOCLOUDUser> userList = new ArrayList<>();
        ZEGOExpressService rtcService = ZEGOSDKManager.getInstance().rtcService;
        ZEGOCLOUDUser localUser = rtcService.getLocalUser();
        for (Entry<String, ZEGOInvitation> entry : zegoInvitationMap.entrySet()) {
            ZEGOInvitation invitation = entry.getValue();
            if (!invitation.isFinished()) {
                String inviter = invitation.inviter;
                if (!Objects.equals(localUser.userID, inviter)) {
                    userList.add(rtcService.getUser(inviter));
                }
            }
        }
        return userList;
    }

    public void onReceiveRoomMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromRoomID) {
        for (ZIMMessage zimMessage : messageList) {
            if (zimMessage.getType() == ZIMMessageType.COMMAND) {
                ZIMCommandMessage commandMessage = (ZIMCommandMessage) zimMessage;
                String message = new String(commandMessage.message, StandardCharsets.UTF_8);
                LiveAudioRoomProtocol protocol = LiveAudioRoomProtocol.parse(message);
                ZEGOExpressService rtcService = ZEGOSDKManager.getInstance().rtcService;
                ZEGOCLOUDUser localUser = rtcService.getLocalUser();
                if (protocol != null && localUser != null) {
                    String operatorID = protocol.getOperatorID();
                    if (protocol.isInvite()) {
                        if (Objects.equals(localUser.userID, protocol.getTargetID())) {
                            onInComingNewInvitation(String.valueOf(commandMessage.getMessageID()),
                                operatorID, message);
                        }
                    } else if (protocol.isCancel()) {
                        if (Objects.equals(localUser.userID, protocol.getTargetID())) {
                            onInComingInvitationButIsCancelled(commandMessage.extendedData,
                                operatorID, message);
                        }
                    } else if (protocol.isReject()) {
                        if (Objects.equals(localUser.userID, protocol.getOperatorID())) {
                            onOutgoingInvitationButIsRejected(commandMessage.extendedData,
                                operatorID, message);
                        }
                    } else if (protocol.isAccept()) {
                        if (Objects.equals(localUser.userID, protocol.getOperatorID())) {
                            onOutgoingInvitationAndIsAccepted(commandMessage.extendedData,
                                operatorID, message);
                        }
                    }
                }
            }
        }
    }
}
