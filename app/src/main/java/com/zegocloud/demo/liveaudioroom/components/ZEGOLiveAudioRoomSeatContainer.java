package com.zegocloud.demo.liveaudioroom.components;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;
import com.zegocloud.demo.liveaudioroom.R;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager.HostChangeListener;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager.SeatLockChangeListener;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager.SeatUserChangeListener;
import com.zegocloud.demo.liveaudioroom.internal.ZEGOSDKManager;
import com.zegocloud.demo.liveaudioroom.internal.ZIMService.UserAvatarListener;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOCLOUDUser;
import im.zego.zim.callback.ZIMRoomAttributesBatchOperatedCallback;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.entity.ZIMError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZEGOLiveAudioRoomSeatContainer extends LinearLayout {

    private long lastClickTime;
    private ZEGOCLOUDUser roomHostUser;
    private BottomListDialog requestTakeSeatDialog;
    private BottomListDialog switchSeatDialog;
    private BottomListDialog leaveSeatDialog;

    public ZEGOLiveAudioRoomSeatContainer(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZEGOLiveAudioRoomSeatContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZEGOLiveAudioRoomSeatContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setOrientation(LinearLayout.VERTICAL);
        ZEGOLiveAudioRoomManager.getInstance().addHostChangeListener(new HostChangeListener() {
            @Override
            public void onHostChanged(ZEGOCLOUDUser hostUser) {
                onHostUserChanged(hostUser);
            }
        });
        ZEGOLiveAudioRoomManager.getInstance().addSeatUserChangeListener(new SeatUserChangeListener() {
            @Override
            public void onSeatChanged(List<ZEGOLiveAudioRoomSeat> changedSeats) {
                for (ZEGOLiveAudioRoomSeat changedSeat : changedSeats) {
                    ZEGOLiveAudioRoomSeatView seatView = getSeatView(changedSeat);
                    seatView.onUserUpdate(changedSeat.getUser());
                }

                checkUsersAvatar();
            }
        });
        ZEGOLiveAudioRoomManager.getInstance().addSeatLockChangeListener(new SeatLockChangeListener() {
            @Override
            public void onSeatLockChanged(boolean lock) {
                List<ZEGOLiveAudioRoomSeat> seatList = ZEGOLiveAudioRoomManager.getInstance().getAudioRoomSeatList();
                for (ZEGOLiveAudioRoomSeat seat : seatList) {
                    ZEGOLiveAudioRoomSeatView seatView = getSeatView(seat);
                    seatView.onLockChanged(lock);
                }
            }
        });
        ZEGOLiveAudioRoomManager.getInstance().addUserAvatarListener(new UserAvatarListener() {
            @Override
            public void onUserAvatarUpdated(String userID, String url) {
                List<ZEGOLiveAudioRoomSeat> seatList = ZEGOLiveAudioRoomManager.getInstance().getAudioRoomSeatList();
                for (ZEGOLiveAudioRoomSeat seat : seatList) {
                    if (seat.isNotEmpty() && seat.getUser().userID.equals(userID)) {
                        ZEGOLiveAudioRoomSeatView seatView = getSeatView(seat);
                        seatView.onUserAvatarUpdated(url);
                    }
                }
            }
        });
    }

    private void checkUsersAvatar() {
        List<String> speakerUserIDs = new ArrayList<>();
        List<ZEGOLiveAudioRoomSeat> seatList = ZEGOLiveAudioRoomManager.getInstance().getAudioRoomSeatList();
        for (ZEGOLiveAudioRoomSeat seat : seatList) {
            if (seat.isNotEmpty()) {
                String userAvatar = ZEGOLiveAudioRoomManager.getInstance().getUserAvatar(seat.getUser().userID);
                if (TextUtils.isEmpty(userAvatar)) {
                    speakerUserIDs.add(seat.getUser().userID);
                }
            }
        }
        if (!speakerUserIDs.isEmpty()) {
            ZEGOLiveAudioRoomManager.getInstance().queryUsersInfo(speakerUserIDs, null);
        }
    }

    public void onHostUserChanged(ZEGOCLOUDUser hostUser) {
        roomHostUser = hostUser;
    }

    public void updateSeats(List<ZEGOLiveAudioRoomSeat> seatList) {
        Log.d(TAG, "updateSeats() called with: seatList = [" + seatList + "]");
        for (ZEGOLiveAudioRoomSeat seat : seatList) {
            ZEGOLiveAudioRoomSeatView seatView = getSeatView(seat);
            seatView.onUserUpdate(seat.getUser());
        }
    }

    public void setLayoutConfig(ZEGOLiveAudioRoomLayoutConfig layoutConfig) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof ViewGroup) {
                ((ViewGroup) child).removeAllViews();
            }
        }
        removeAllViews();

        int seatIndex = 0;
        for (int rowIndex = 0; rowIndex < layoutConfig.rowConfigs.size(); rowIndex++) {
            ZEGOLiveAudioRoomLayoutRowConfig rowConfig = layoutConfig.rowConfigs.get(rowIndex);
            FlexboxLayout flexboxLayout = new FlexboxLayout(getContext());
            LayoutParams params = new LayoutParams(-1, -2);
            params.bottomMargin = layoutConfig.rowSpacing;
            addView(flexboxLayout, params);
            for (int columnIndex = 0; columnIndex < rowConfig.count; columnIndex++) {
                ZEGOLiveAudioRoomSeatView seatView = new ZEGOLiveAudioRoomSeatView(getContext());
                seatView.setOnClickListener(v -> {
                    if (System.currentTimeMillis() - lastClickTime < 500) {
                        return;
                    }
                    onSeatViewClicked(seatView);
                    lastClickTime = System.currentTimeMillis();
                });
                seatView.setTag(seatIndex);
                flexboxLayout.addView(seatView);
                seatIndex = seatIndex + 1;

                FlexboxLayout.LayoutParams layoutParams = (FlexboxLayout.LayoutParams) seatView.getLayoutParams();
                if (rowConfig.alignment == ZEGOLiveAudioRoomLayoutAlignment.SPACE_EVENLY
                    || rowConfig.alignment == ZEGOLiveAudioRoomLayoutAlignment.SPACE_BETWEEN
                    || rowConfig.alignment == ZEGOLiveAudioRoomLayoutAlignment.SPACE_AROUND) {
                    layoutParams.rightMargin = 0;
                } else {
                    layoutParams.rightMargin = rowConfig.seatSpacing;
                }
            }

            if (rowConfig.alignment == ZEGOLiveAudioRoomLayoutAlignment.SPACE_EVENLY) {
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_EVENLY);
            } else if (rowConfig.alignment == ZEGOLiveAudioRoomLayoutAlignment.SPACE_BETWEEN) {
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_BETWEEN);
            } else if (rowConfig.alignment == ZEGOLiveAudioRoomLayoutAlignment.SPACE_AROUND) {
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_AROUND);
            } else if (rowConfig.alignment == ZEGOLiveAudioRoomLayoutAlignment.CENTER) {
                flexboxLayout.setJustifyContent(JustifyContent.CENTER);
            } else if (rowConfig.alignment == ZEGOLiveAudioRoomLayoutAlignment.FLEX_START) {
                flexboxLayout.setJustifyContent(JustifyContent.FLEX_START);
            } else if (rowConfig.alignment == ZEGOLiveAudioRoomLayoutAlignment.FLEX_END) {
                flexboxLayout.setJustifyContent(JustifyContent.FLEX_END);
            }
        }
    }

    private ZEGOLiveAudioRoomSeatView getSeatView(ZEGOLiveAudioRoomSeat audioRoomSeat) {
        FlexboxLayout flexboxLayout = (FlexboxLayout) getChildAt(audioRoomSeat.rowIndex);
        return ((ZEGOLiveAudioRoomSeatView) flexboxLayout.getChildAt(audioRoomSeat.columnIndex));
    }


    private ZEGOLiveAudioRoomSeat getSeat(ZEGOLiveAudioRoomSeatView seatView) {
        int seatIndex = (int) seatView.getTag();
        return ZEGOLiveAudioRoomManager.getInstance().getAudioRoomSeatList().get(seatIndex);
    }

    private static final String TAG = "ZEGOLiveAudioRoomSeatCo";

    private void onSeatViewClicked(ZEGOLiveAudioRoomSeatView clickSeatView) {
        ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
        if (localUser == null) {
            return;
        }
        ZEGOLiveAudioRoomSeat clickSeat = getSeat(clickSeatView);
        int hostSeatIndex = ZEGOLiveAudioRoomManager.getInstance().getHostSeatIndex();
        if (clickSeat.isEmpty()) {
            if (localUser.equals(roomHostUser) || clickSeat.seatIndex == hostSeatIndex
                || ZEGOLiveAudioRoomManager.getInstance().isSeatLocked()) {
            } else {
                int mySeatIndex = ZEGOLiveAudioRoomManager.getInstance().findMyRoomSeatIndex();
                if (mySeatIndex == -1) {
                    showTakeSeatDialog(clickSeat);
                } else {
                    switchToTheSeat(clickSeat);
                }
            }
        } else {
            int mySeatIndex = ZEGOLiveAudioRoomManager.getInstance().findMyRoomSeatIndex();
            if (!localUser.equals(roomHostUser) && clickSeat.seatIndex == mySeatIndex) {
                showLeaveSeatDialog(clickSeat);
            }
        }
    }

    private void showLeaveSeatDialog(ZEGOLiveAudioRoomSeat clickSeat) {
        List<String> stringList = Arrays.asList("Leave the Seat", getContext().getString(R.string.cancel));
        leaveSeatDialog = new BottomListDialog(getContext(), stringList);
        leaveSeatDialog.show();
        leaveSeatDialog.setOnDialogClickListener((dialog, which) -> {
            if (which == 0) {
                ZEGOLiveAudioRoomManager.getInstance()
                    .emptySeat(clickSeat.seatIndex, new ZIMRoomAttributesOperatedCallback() {
                        @Override
                        public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys,
                            ZIMError errorInfo) {

                        }
                    });
            }
            dialog.dismiss();
        });
    }

    private void switchToTheSeat(ZEGOLiveAudioRoomSeat clickSeat) {
        List<String> stringList = Arrays.asList("Switch to the Seat", getContext().getString(R.string.cancel));
        switchSeatDialog = new BottomListDialog(getContext(), stringList);
        switchSeatDialog.show();
        switchSeatDialog.setOnDialogClickListener((dialog, which) -> {
            if (which == 0) {
                int myRoomSeatIndex = ZEGOLiveAudioRoomManager.getInstance().findMyRoomSeatIndex();
                ZEGOLiveAudioRoomManager.getInstance()
                    .switchSeat(myRoomSeatIndex, clickSeat.seatIndex, new ZIMRoomAttributesBatchOperatedCallback() {
                        @Override
                        public void onRoomAttributesBatchOperated(String roomID, ZIMError errorInfo) {

                        }
                    });

            }
            dialog.dismiss();
        });
    }

    private void showTakeSeatDialog(ZEGOLiveAudioRoomSeat clickSeat) {
        List<String> stringList = Arrays.asList("Take the Seat", getContext().getString(R.string.cancel));
        requestTakeSeatDialog = new BottomListDialog(getContext(), stringList);
        requestTakeSeatDialog.show();
        requestTakeSeatDialog.setOnDialogClickListener((dialog, which) -> {
            if (which == 0) {
                ZEGOLiveAudioRoomManager.getInstance()
                    .takeSeat(clickSeat.seatIndex, new ZIMRoomAttributesOperatedCallback() {
                        @Override
                        public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys,
                            ZIMError errorInfo) {

                        }
                    });

            }
            dialog.dismiss();
        });
    }

    public void updateSeatViewSize(boolean gameMode) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) child;
                for (int j = 0; j < viewGroup.getChildCount(); j++) {
                    ZEGOLiveAudioRoomSeatView seatView = (ZEGOLiveAudioRoomSeatView) viewGroup.getChildAt(j);
                    if (gameMode) {
                        seatView.onEnterGameMode();
                    } else {
                        seatView.onEnterNormalMode();
                    }
                }
            }
        }
    }
}
