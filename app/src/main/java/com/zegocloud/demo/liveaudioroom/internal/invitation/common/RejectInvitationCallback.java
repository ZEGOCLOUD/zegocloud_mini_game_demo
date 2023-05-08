package com.zegocloud.demo.liveaudioroom.internal.invitation.common;

public interface RejectInvitationCallback {

    void onResult(int errorCode, String invitationID);
}
