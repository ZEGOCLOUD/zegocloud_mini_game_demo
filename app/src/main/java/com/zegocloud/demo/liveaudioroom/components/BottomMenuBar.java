package com.zegocloud.demo.liveaudioroom.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.zegocloud.demo.liveaudioroom.R;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager.HostChangeListener;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager.SeatLockChangeListener;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager.SeatUserChangeListener;
import com.zegocloud.demo.liveaudioroom.backend.Game;
import com.zegocloud.demo.liveaudioroom.internal.ZEGOSDKManager;
import com.zegocloud.demo.liveaudioroom.internal.components.ToggleMicrophoneButton;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOCLOUDUser;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOExpressService.MicrophoneListener;
import com.zegocloud.demo.liveaudioroom.utils.Utils;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.entity.ZIMError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BottomMenuBar extends LinearLayout {


    private LinearLayout childLinearLayout;
    private ToggleMicrophoneButton microphoneButton;
    private LockSeatButton lockSeatButton;
    private TakeSeatButton takeSeatButton;
    private TakeSeatRequestListButton takeSeatRequestListButton;
    private ImageView gameView;
    private BottomListDialog gameListDialog;

    public BottomMenuBar(Context context) {
        super(context);
        initView();
    }

    public BottomMenuBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BottomMenuBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public BottomMenuBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        setOrientation(LinearLayout.HORIZONTAL);
        setLayoutParams(new LayoutParams(-1, -2));
        setGravity(Gravity.END);

        childLinearLayout = new LinearLayout(getContext());
        childLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        childLinearLayout.setGravity(Gravity.END);
        LayoutParams params = new LayoutParams(0, -2, 1);
        addView(childLinearLayout, params);
        int paddingEnd = Utils.dp2px(8, getResources().getDisplayMetrics());
        childLinearLayout.setPadding(0, 0, paddingEnd, 0);

        microphoneButton = new ToggleMicrophoneButton(getContext());
        microphoneButton.updateState(false);
        childLinearLayout.addView(microphoneButton, generateChildImageLayoutParams());
        ZEGOSDKManager.getInstance().rtcService.addMicrophoneListener(new MicrophoneListener() {
            @Override
            public void onMicrophoneOpen(String userID, boolean open) {
                ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
                if (userID.equals(localUser.userID)) {
                    microphoneButton.updateState(open);
                }
            }
        });

        lockSeatButton = new LockSeatButton(getContext());
        childLinearLayout.addView(lockSeatButton, generateChildImageLayoutParams());

        takeSeatButton = new TakeSeatButton(getContext());
        childLinearLayout.addView(takeSeatButton, generateChildTextLayoutParams());

        takeSeatRequestListButton = new TakeSeatRequestListButton(getContext());
        takeSeatRequestListButton.setOnClickListener(v -> {
            TakeSeatRequestDialog dialog = new TakeSeatRequestDialog(getContext());
            dialog.show();
        });
        childLinearLayout.addView(takeSeatRequestListButton, generateChildImageLayoutParams());

        gameView = new ImageView(getContext());
        gameView.setImageResource(R.drawable.icon_game);
        gameView.setOnClickListener(v -> {
            Game currentGame = ZEGOLiveAudioRoomManager.getInstance().getCurrentGame();
            if (currentGame == null) {
                List<Game> gameList = ZEGOLiveAudioRoomManager.getInstance().getAudioRoomGameList();
                List<String> stringList = Arrays.asList(gameList.get(0).name, gameList.get(1).name);
                gameListDialog = new BottomListDialog(getContext(), stringList);
                gameListDialog.show();
                gameListDialog.setOnDialogClickListener((dialog, which) -> {
                    Game game = gameList.get(which);
                    ZEGOLiveAudioRoomManager.getInstance().startGameMode(game);
                    dialog.dismiss();
                });
            } else {
                ZEGOLiveAudioRoomManager.getInstance().stopGameMode();
            }

        });
        childLinearLayout.addView(gameView, generateChildImageLayoutParams());

        ZEGOLiveAudioRoomManager.getInstance().addSeatUserChangeListener(new SeatUserChangeListener() {
            @Override
            public void onSeatChanged(List<ZEGOLiveAudioRoomSeat> changedSeats) {
                updateWidgets();
            }
        });
        ZEGOLiveAudioRoomManager.getInstance().addHostChangeListener(new HostChangeListener() {
            @Override
            public void onHostChanged(ZEGOCLOUDUser hostUser) {
                updateWidgets();
            }
        });
        ZEGOLiveAudioRoomManager.getInstance().addSeatLockChangeListener(new SeatLockChangeListener() {
            @Override
            public void onSeatLockChanged(boolean lock) {
                updateWidgets();
            }
        });

        microphoneButton.setVisibility(GONE);
        lockSeatButton.setVisibility(GONE);
        takeSeatButton.setVisibility(GONE);
        takeSeatRequestListButton.setVisibility(GONE);
        gameView.setVisibility(INVISIBLE);
    }

    private static final String TAG = "BottomMenuBar";

    private void updateWidgets() {

        int myRoomSeatIndex = ZEGOLiveAudioRoomManager.getInstance().findMyRoomSeatIndex();
        ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
        ZEGOCLOUDUser hostUser = ZEGOLiveAudioRoomManager.getInstance().getHostUser();

        if (localUser.equals(hostUser)) {
            // host widget
            microphoneButton.setVisibility(VISIBLE);
            lockSeatButton.setVisibility(VISIBLE);
            takeSeatButton.setVisibility(GONE);
            takeSeatRequestListButton.setVisibility(VISIBLE);
            gameView.setVisibility(VISIBLE);
        } else {
            if (myRoomSeatIndex >= 0) {
                // speaker widget
                microphoneButton.setVisibility(VISIBLE);
                lockSeatButton.setVisibility(GONE);
                takeSeatButton.setVisibility(GONE);
                takeSeatRequestListButton.setVisibility(GONE);
                gameView.setVisibility(VISIBLE);
            } else {
                // audience widget
                microphoneButton.setVisibility(GONE);
                lockSeatButton.setVisibility(GONE);
                if (ZEGOLiveAudioRoomManager.getInstance().isSeatLocked()) {
                    takeSeatButton.setVisibility(VISIBLE);
                } else {
                    takeSeatButton.setVisibility(GONE);
                }
                takeSeatRequestListButton.setVisibility(GONE);
                gameView.setVisibility(INVISIBLE);
            }
        }

    }

    private LayoutParams generateChildImageLayoutParams() {
        int size = Utils.dp2px(36f, getResources().getDisplayMetrics());
        int marginTop = Utils.dp2px(10f, getResources().getDisplayMetrics());
        int marginBottom = Utils.dp2px(16f, getResources().getDisplayMetrics());
        int marginEnd = Utils.dp2px(8, getResources().getDisplayMetrics());
        LayoutParams layoutParams = new LayoutParams(size, size);
        layoutParams.topMargin = marginTop;
        layoutParams.bottomMargin = marginBottom;
        layoutParams.rightMargin = marginEnd;
        return layoutParams;
    }

    private LayoutParams generateChildTextLayoutParams() {
        int size = Utils.dp2px(36f, getResources().getDisplayMetrics());
        int marginTop = Utils.dp2px(10f, getResources().getDisplayMetrics());
        int marginBottom = Utils.dp2px(16f, getResources().getDisplayMetrics());
        int marginEnd = Utils.dp2px(8, getResources().getDisplayMetrics());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, size);
        layoutParams.topMargin = marginTop;
        layoutParams.bottomMargin = marginBottom;
        layoutParams.rightMargin = marginEnd;
        return layoutParams;
    }
}
