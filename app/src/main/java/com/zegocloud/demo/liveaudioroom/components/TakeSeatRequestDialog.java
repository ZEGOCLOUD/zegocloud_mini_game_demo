package com.zegocloud.demo.liveaudioroom.components;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zegocloud.demo.liveaudioroom.databinding.DialogTakeseatReqestBinding;
import com.zegocloud.demo.liveaudioroom.internal.ZEGOSDKManager;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.IncomingInvitationListener;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.ZEGOInvitation;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOCLOUDUser;
import java.util.ArrayList;
import java.util.List;

public class TakeSeatRequestDialog extends BottomSheetDialog {

    private DialogTakeseatReqestBinding binding;
    private TakeSeatRequestAdapter seatRequestAdapter;

    public TakeSeatRequestDialog(@NonNull Context context) {
        super(context);
    }

    public TakeSeatRequestDialog(@NonNull Context context, int theme) {
        super(context, theme);
    }

    protected TakeSeatRequestDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogTakeseatReqestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.dimAmount = 0.1f;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        setCanceledOnTouchOutside(true);
        window.setBackgroundDrawable(new ColorDrawable());

        // both need setPeekHeight & setLayoutParams
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int height = (int) (displayMetrics.heightPixels * 0.6f);
        getBehavior().setPeekHeight(height);
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(-1, height);
        binding.liveRequestListLayout.setLayoutParams(params);

        binding.requestRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        seatRequestAdapter = new TakeSeatRequestAdapter();

        List<ZEGOCLOUDUser> otherUserInviteList = ZEGOSDKManager.getInstance().zimService.getOtherUserInviteList();
        List<String> userIDList = new ArrayList<>();
        for (ZEGOCLOUDUser zegocloudUser : otherUserInviteList) {
            userIDList.add(zegocloudUser.userID);
        }
        seatRequestAdapter.setItems(userIDList);

        binding.requestRecyclerview.setAdapter(seatRequestAdapter);
        ZEGOSDKManager.getInstance().zimService.addIncomingInvitationListener(new IncomingInvitationListener() {
            @Override
            public void onReceiveNewInvitation(String invitationID, String userID, String extendedData) {
                seatRequestAdapter.addItem(userID);
            }

            @Override
            public void onReceiveInvitationButResponseTimeout(String invitationID) {
                ZEGOInvitation invitation = ZEGOSDKManager.getInstance().zimService.getZEGOInvitation(
                    invitationID);
                if (invitation != null) {
                    seatRequestAdapter.removeItem(invitation.inviter);
                }
            }

            @Override
            public void onReceiveInvitationButIsCancelled(String invitationID, String inviter, String extendedData) {
                seatRequestAdapter.removeItem(inviter);
            }

            @Override
            public void onActionAcceptInvitation(int errorCode, String invitationID) {
                ZEGOInvitation invitation = ZEGOSDKManager.getInstance().zimService.getZEGOInvitation(
                    invitationID);
                if (invitation != null) {
                    seatRequestAdapter.removeItem(invitation.inviter);
                }
            }

            @Override
            public void onActionRejectInvitation(int errorCode, String invitationID) {
                ZEGOInvitation invitation = ZEGOSDKManager.getInstance().zimService.getZEGOInvitation(
                    invitationID);
                if (invitation != null) {
                    seatRequestAdapter.removeItem(invitation.inviter);
                }
            }
        });
    }
}
