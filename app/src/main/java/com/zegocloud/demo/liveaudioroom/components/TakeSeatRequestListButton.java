package com.zegocloud.demo.liveaudioroom.components;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import com.zegocloud.demo.liveaudioroom.R;
import com.zegocloud.demo.liveaudioroom.internal.ZEGOSDKManager;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.IncomingInvitationListener;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOCLOUDUser;
import com.zegocloud.demo.liveaudioroom.utils.Utils;
import java.util.List;

public class TakeSeatRequestListButton extends FrameLayout {

    private ImageView imageView;
    private ImageFilterView redPoint;

    public TakeSeatRequestListButton(@NonNull Context context) {
        super(context);
        initView();
    }

    public TakeSeatRequestListButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TakeSeatRequestListButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public TakeSeatRequestListButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    protected void initView() {
        imageView = new ImageView(getContext());
        addView(imageView);
        imageView.setImageResource(R.drawable.audioroom_icon_seat);
        redPoint = new ImageFilterView(getContext());
        redPoint.setBackgroundColor(Color.parseColor("#FF0D23"));
        redPoint.setRoundPercent(1.0f);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        LayoutParams redPointParams = new LayoutParams(Utils.dp2px(8, displayMetrics), Utils.dp2px(8, displayMetrics));
        redPointParams.gravity = Gravity.TOP | Gravity.END;
        addView(redPoint, redPointParams);

        hideRedPoint();

        ZEGOSDKManager.getInstance().zimService.addIncomingInvitationListener(new IncomingInvitationListener() {
            @Override
            public void onReceiveNewInvitation(String invitationID, String userID, String extendedData) {
                showRedPoint();
            }

            @Override
            public void onReceiveInvitationButResponseTimeout(String invitationID) {
                List<ZEGOCLOUDUser> otherUserInviteList = ZEGOSDKManager.getInstance().zimService.getOtherUserInviteList();
                if (otherUserInviteList.isEmpty()) {
                    hideRedPoint();
                } else {
                    showRedPoint();
                }
            }

            @Override
            public void onReceiveInvitationButIsCancelled(String invitationID, String inviter, String extendedData) {
                List<ZEGOCLOUDUser> otherUserInviteList = ZEGOSDKManager.getInstance().zimService.getOtherUserInviteList();
                if (otherUserInviteList.isEmpty()) {
                    hideRedPoint();
                } else {
                    showRedPoint();
                }
            }

            @Override
            public void onActionAcceptInvitation(int errorCode, String invitationID) {
                List<ZEGOCLOUDUser> otherUserInviteList = ZEGOSDKManager.getInstance().zimService.getOtherUserInviteList();
                if (otherUserInviteList.isEmpty()) {
                    hideRedPoint();
                } else {
                    showRedPoint();
                }
            }

            @Override
            public void onActionRejectInvitation(int errorCode, String invitationID) {
                List<ZEGOCLOUDUser> otherUserInviteList = ZEGOSDKManager.getInstance().zimService.getOtherUserInviteList();
                if (otherUserInviteList.isEmpty()) {
                    hideRedPoint();
                } else {
                    showRedPoint();
                }
            }
        });
    }

    public void showRedPoint() {
        redPoint.setVisibility(View.VISIBLE);
    }

    public void hideRedPoint() {
        redPoint.setVisibility(View.GONE);
    }
}
