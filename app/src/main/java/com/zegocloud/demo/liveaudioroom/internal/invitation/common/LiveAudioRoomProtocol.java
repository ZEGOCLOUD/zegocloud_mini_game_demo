package com.zegocloud.demo.liveaudioroom.internal.invitation.common;

import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class LiveAudioRoomProtocol {

    // live audio room
    public static final int ApplyToBecomeSpeaker = 20000;
    public static final int CancelSpeakerApply = 20001;
    public static final int HostRefuseSpeakerApply = 20002;
    public static final int HostAcceptSpeakerApply = 20003;

    public static final int HostInviteToBecomeSpeaker = 20100;
    public static final int HostCancelSpeakerInvitation = 20101;
    public static final int RefuseSpeakerInvitation = 20102;
    public static final int AcceptSpeakerInvitation = 20103;

    private int actionType;
    private String operatorID;
    private String targetID;

    public static LiveAudioRoomProtocol parse(String string) {
        if (TextUtils.isEmpty(string)) {
            return null;
        }
        LiveAudioRoomProtocol protocol = new LiveAudioRoomProtocol();
        try {
            JSONObject jsonObject = new JSONObject(string);
            protocol.actionType = getIntFromJson("actionType", jsonObject);
            protocol.operatorID = getStringFromJson("operatorID", jsonObject);
            protocol.targetID = getStringFromJson("targetID", jsonObject);
        } catch (JSONException e) {
            protocol = null;
        }
        return protocol;
    }


    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("actionType", actionType);
            jsonObject.put("operatorID", operatorID);
            jsonObject.put("targetID", targetID);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject.toString();
    }

    private static int getIntFromJson(String key, JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) {
            throw new JSONException("jsonObject is null");
        }
        int result = 0;
        if (jsonObject.has(key)) {
            result = jsonObject.getInt(key);
        }
        return result;
    }

    private static String getStringFromJson(String key, JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) {
            throw new JSONException("jsonObject is null");
        }
        String result = null;
        if (jsonObject.has(key)) {
            result = jsonObject.getString(key);
        }
        return result;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public String getOperatorID() {
        return operatorID;
    }

    public void setOperatorID(String operatorID) {
        this.operatorID = operatorID;
    }

    public String getTargetID() {
        return targetID;
    }

    public void setTargetID(String targetID) {
        this.targetID = targetID;
    }


    public boolean isInvite() {
        return getActionType() == LiveAudioRoomProtocol.ApplyToBecomeSpeaker
            || getActionType() == LiveAudioRoomProtocol.HostInviteToBecomeSpeaker;
    }

    public void accept() {
        if (getActionType() == ApplyToBecomeSpeaker) {
            setActionType(LiveAudioRoomProtocol.HostAcceptSpeakerApply);
        } else if (getActionType() == HostInviteToBecomeSpeaker) {
            setActionType(LiveAudioRoomProtocol.AcceptSpeakerInvitation);
        }
    }

    public boolean isAccept() {
        return getActionType() == LiveAudioRoomProtocol.AcceptSpeakerInvitation
            || getActionType() == LiveAudioRoomProtocol.HostAcceptSpeakerApply;
    }

    public void reject() {
        if (getActionType() == ApplyToBecomeSpeaker) {
            setActionType(LiveAudioRoomProtocol.HostRefuseSpeakerApply);
        } else if (getActionType() == HostInviteToBecomeSpeaker) {
            setActionType(LiveAudioRoomProtocol.RefuseSpeakerInvitation);
        }
    }

    public boolean isReject() {
        return getActionType() == LiveAudioRoomProtocol.RefuseSpeakerInvitation
            || getActionType() == LiveAudioRoomProtocol.HostRefuseSpeakerApply;
    }

    public void cancel() {
        if (getActionType() == ApplyToBecomeSpeaker) {
            setActionType(LiveAudioRoomProtocol.CancelSpeakerApply);
        } else if (getActionType() == HostInviteToBecomeSpeaker) {
            setActionType(LiveAudioRoomProtocol.HostCancelSpeakerInvitation);
        }
    }

    public boolean isCancel() {
        return getActionType() == LiveAudioRoomProtocol.CancelSpeakerApply
            || getActionType() == LiveAudioRoomProtocol.HostCancelSpeakerInvitation;
    }
}
