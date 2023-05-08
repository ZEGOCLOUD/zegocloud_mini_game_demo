package com.zegocloud.demo.liveaudioroom.internal.invitation.common;

public interface InvitationInterface {

    void init();

    void inviteUser(String userID, String extendedData, SendInvitationCallback invitationCallback);

    void acceptInvite(ZEGOInvitation invitation, AcceptInvitationCallback callback);

    void rejectInvite(ZEGOInvitation invitation, RejectInvitationCallback callback);

    void cancelInvite(ZEGOInvitation invitation, CancelInvitationCallback callback);

    void setInvitationListener(InvitationListener listener);
}
