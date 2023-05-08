package com.zegocloud.demo.liveaudioroom.internal.invitation.common;

import java.util.List;

public interface CancelInvitationCallback {

    void onResult(int errorCode, String invitationID, List<String> errorInvitees);
}
