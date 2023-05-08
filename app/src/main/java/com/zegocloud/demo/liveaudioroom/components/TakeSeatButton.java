package com.zegocloud.demo.liveaudioroom.components;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.demo.liveaudioroom.R;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.liveaudioroom.internal.ZEGOSDKManager;
import com.zegocloud.demo.liveaudioroom.internal.components.ZEGOTextButton;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.CancelInvitationCallback;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.OutgoingInvitationListener;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.SendInvitationCallback;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.ZEGOInvitation;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.LiveAudioRoomProtocol;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOCLOUDUser;
import com.zegocloud.demo.liveaudioroom.utils.Utils;
import java.util.List;
import java.util.Objects;

public class TakeSeatButton extends ZEGOTextButton {

    private ZEGOInvitation mInvitationData;

    public TakeSeatButton(@NonNull Context context) {
        super(context);
    }

    public TakeSeatButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TakeSeatButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
        setTextColor(Color.WHITE);
        setTextSize(13);
        setGravity(Gravity.CENTER);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        setPadding(Utils.dp2px(14, displayMetrics), 0, Utils.dp2px(16, displayMetrics), 0);
        setCompoundDrawablePadding(Utils.dp2px(6, displayMetrics));

        ZEGOSDKManager.getInstance().zimService.addOutgoingInvitationListener(new OutgoingInvitationListener() {
            @Override
            public void onActionSendInvitation(int errorCode, String invitationID, String extendedData,
                List<String> errorInvitees) {
                if (mInvitationData != null && Objects.equals(invitationID, mInvitationData.invitationID)) {
                    updateUI();
                }
            }

            @Override
            public void onActionCancelInvitation(int errorCode, String invitationID, List<String> errorInvitees) {
                if (errorCode == 0) {
                    if (mInvitationData != null && Objects.equals(invitationID, mInvitationData.invitationID)) {
                        mInvitationData = null;
                        updateUI();
                    }
                }
            }

            @Override
            public void onSendInvitationButReceiveResponseTimeout(String invitationID, List<String> invitees) {
                if (mInvitationData != null && Objects.equals(invitationID, mInvitationData.invitationID)) {
                    mInvitationData = null;
                    updateUI();
                }
            }

            @Override
            public void onSendInvitationAndIsAccepted(String invitationID, String invitee, String extendedData) {
                if (mInvitationData != null && Objects.equals(invitationID, mInvitationData.invitationID)) {
                    mInvitationData = null;
                    updateUI();
                }
            }

            @Override
            public void onSendInvitationButIsRejected(String invitationID, String invitee, String extendedData) {
                if (mInvitationData != null && Objects.equals(invitationID, mInvitationData.invitationID)) {
                    mInvitationData = null;
                    updateUI();
                }
            }
        });

        updateUI();
    }

    private static final String TAG = "TakeSeatButton";

    @Override
    protected void afterClick() {
        super.afterClick();
        ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
        ZEGOCLOUDUser hostUser = ZEGOLiveAudioRoomManager.getInstance().getHostUser();
        if (localUser == null || hostUser == null) {
            return;
        }
        if (mInvitationData == null) {
            LiveAudioRoomProtocol protocol = new LiveAudioRoomProtocol();
            protocol.setActionType(LiveAudioRoomProtocol.ApplyToBecomeSpeaker);
            protocol.setOperatorID(localUser.userID);
            protocol.setTargetID(hostUser.userID);
            ZEGOSDKManager.getInstance().zimService.inviteUser(hostUser.userID, protocol.toString(),
                new SendInvitationCallback() {
                    @Override
                    public void onResult(int errorCode, String invitationID, List<String> errorInvitees) {
                        Log.d(TAG,
                            "onResult() called with: errorCode = [" + errorCode + "], invitationID = [" + invitationID
                                + "], errorInvitees = [" + errorInvitees + "]");
                        if (errorCode == 0) {
                            mInvitationData = new ZEGOInvitation();
                            mInvitationData.invitationID = invitationID;
                            mInvitationData.extendedData = protocol.toString();
                        }
                    }
                });
        } else {
            LiveAudioRoomProtocol protocol = new LiveAudioRoomProtocol();
            protocol.setActionType(LiveAudioRoomProtocol.CancelSpeakerApply);
            protocol.setOperatorID(localUser.userID);
            protocol.setTargetID(hostUser.userID);
            ZEGOInvitation zegoInvitation = ZEGOSDKManager.getInstance().zimService.getZEGOInvitation(
                mInvitationData.invitationID);
            ZEGOSDKManager.getInstance().zimService.cancelInvite(zegoInvitation, new CancelInvitationCallback() {
                @Override
                public void onResult(int errorCode, String invitationID, List<String> errorInvitees) {
                    Log.d(TAG,
                        "onResult() called with: errorCode = [" + errorCode + "], invitationID = [" + invitationID
                            + "], errorInvitees = [" + errorInvitees + "]");
                }
            });

        }
    }

    public void updateUI() {
        Log.d(TAG, "updateUI: ");
        if (mInvitationData == null) {
            setText("Apply to Take Seat");
            setBackgroundResource(R.drawable.bg_cohost_btn);
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.liveaudioroom_bottombar_cohost, 0, 0, 0);
        } else {
            ZEGOInvitation zegoInvitation = ZEGOSDKManager.getInstance().zimService.getZEGOInvitation(
                mInvitationData.invitationID);
            if (zegoInvitation == null || zegoInvitation.isFinished()) {
                setText("Apply to Take Seat");
                setBackgroundResource(R.drawable.bg_cohost_btn);
                setCompoundDrawablesWithIntrinsicBounds(R.drawable.liveaudioroom_bottombar_cohost, 0, 0, 0);
            } else {
                setText("Cancel Take Seat");
                setBackgroundResource(R.drawable.bg_cohost_btn);
                setCompoundDrawablesWithIntrinsicBounds(R.drawable.liveaudioroom_bottombar_cohost, 0, 0, 0);
            }
        }

    }
}
