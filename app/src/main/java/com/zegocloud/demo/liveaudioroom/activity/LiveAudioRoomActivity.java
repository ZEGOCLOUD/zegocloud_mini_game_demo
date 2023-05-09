package com.zegocloud.demo.liveaudioroom.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager.GameModeListener;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager.SeatUserChangeListener;
import com.zegocloud.demo.liveaudioroom.backend.BackendUser;
import com.zegocloud.demo.liveaudioroom.backend.Game;
import com.zegocloud.demo.liveaudioroom.components.ZEGOLiveAudioRoomLayoutAlignment;
import com.zegocloud.demo.liveaudioroom.components.ZEGOLiveAudioRoomLayoutConfig;
import com.zegocloud.demo.liveaudioroom.components.ZEGOLiveAudioRoomLayoutRowConfig;
import com.zegocloud.demo.liveaudioroom.components.ZEGOLiveAudioRoomSeat;
import com.zegocloud.demo.liveaudioroom.databinding.ActivityLiveAudioRoomBinding;
import com.zegocloud.demo.liveaudioroom.internal.ZEGOSDKManager;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.OutgoingInvitationListener;
import com.zegocloud.demo.liveaudioroom.internal.minigame.MiniGameManager;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOCLOUDUser;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOExpressService.RoomStreamChangeListener;
import com.zegocloud.demo.liveaudioroom.utils.Utils;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.entity.ZIMError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.json.JSONObject;
import tech.sud.mgp.SudMGPWrapper.state.SudMGPMGState;

public class LiveAudioRoomActivity extends AppCompatActivity {

    private ActivityLiveAudioRoomBinding binding;
    private ZEGOLiveAudioRoomLayoutConfig seatLayoutConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveAudioRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean isHost = getIntent().getBooleanExtra("host", false);
        MiniGameManager.getInstance().autoJoinGame = isHost;

        String roomID = getIntent().getStringExtra("roomID");
        if (TextUtils.isEmpty(roomID)) {
            finish();
            return;
        }

        ZEGOLiveAudioRoomManager.getInstance().init();
        initSeat();

        ZEGOSDKManager.getInstance().joinRoom(roomID, new IZegoRoomLoginCallback() {
            @Override
            public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
                if (errorCode != 0) {
                    finish();
                } else {
                    if (isHost) {
                        // wait roomextraInfo update
                        binding.getRoot().postDelayed(() -> {
                            ZEGOLiveAudioRoomManager.getInstance().setSelfHost();
                            ZEGOLiveAudioRoomManager.getInstance()
                                .takeSeat(0, new ZIMRoomAttributesOperatedCallback() {
                                    @Override
                                    public void onRoomAttributesOperated(String roomID1, ArrayList<String> errorKeys,
                                        ZIMError errorInfo) {

                                    }
                                });
                        }, 500);
                    }
                    initAfterJoinRoom();
                }
            }
        });

        MiniGameManager.getInstance().setCallback(new MiniGameManager.MiniGameCallback() {
            @Override
            public void onGamePlayerIconPositionUpdate(SudMGPMGState.MGCommonGamePlayerIconPosition model) {
            }

            @Override
            public void onGameMGCommonGameState(SudMGPMGState.MGCommonGameState model) {
            }

            @Override
            public void onPlayerMGCommonPlayerIn(String userId, SudMGPMGState.MGCommonPlayerIn model) {
                String localUserID = ZEGOLiveAudioRoomManager.getInstance().getBackendUser().getUid();
                int mySeat = ZEGOLiveAudioRoomManager.getInstance().findMyRoomSeatIndex();
                if (Objects.equals(userId, localUserID) && model.isIn && mySeat == -1) {
                    int seatIndex = ZEGOLiveAudioRoomManager.getInstance().findFirstAvailableSeatIndex();
                    ZEGOLiveAudioRoomManager.getInstance().takeSeat(seatIndex, new ZIMRoomAttributesOperatedCallback() {
                        @Override
                        public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys,
                            ZIMError errorInfo) {
                        }
                    });
                }
            }
        });

        MiniGameManager.getInstance().gameViewLiveData.observe(this, new Observer<View>() {
            public void onChanged(@Nullable View view) {
                if (view == null) {
                    binding.gameAllArea.removeAllViews();
                } else {
                    ViewGroup parent = (ViewGroup) view.getParent();
                    if (parent != null) {
                        parent.removeView(view);
                    }
                    binding.gameAllArea.addView(view, FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);
                    MiniGameManager.getInstance().joinGame();
                    MiniGameManager.getInstance().updateReadyStatus();
                }
            }
        });

        ZEGOLiveAudioRoomManager.getInstance().setGameModeListener(new GameModeListener() {
            @Override
            public void onGameModeStart(Game game) {
                String code = ZEGOLiveAudioRoomManager.getInstance().getCode();
                BackendUser backendUser = ZEGOLiveAudioRoomManager.getInstance().getBackendUser();
                String currentRoomID = ZEGOSDKManager.getInstance().rtcService.getCurrentRoomID();
                MiniGameManager.getInstance()
                    .loadGame(LiveAudioRoomActivity.this, backendUser.getUid(), currentRoomID, game.gameID, code);

                int top = Math.abs(binding.gameAllArea.getTop() - binding.gameRealArea.getTop());
                // enter game mode, seat will change size
                top = (int) (top * 0.5f);
                int bottom = Math.abs(binding.gameAllArea.getBottom() - binding.gameRealArea.getBottom());
                MiniGameManager.getInstance().setGameSafeZoneMargin(0, top, 0, bottom);

                int count = 0;
                for (ZEGOLiveAudioRoomLayoutRowConfig rowConfig : seatLayoutConfig.rowConfigs) {
                    count = count + rowConfig.count;
                }
                seatLayoutConfig.rowConfigs = Collections.singletonList(
                    new ZEGOLiveAudioRoomLayoutRowConfig(count, ZEGOLiveAudioRoomLayoutAlignment.SPACE_AROUND));
                ZEGOLiveAudioRoomManager.getInstance().updateSeatConfig(seatLayoutConfig);
                binding.seatContainer.setLayoutConfig(seatLayoutConfig);
                binding.seatContainer.updateSeats(ZEGOLiveAudioRoomManager.getInstance().getAudioRoomSeatList());
                binding.seatContainer.updateSeatViewSize(true);

            }

            @Override
            public void onGameModeEnd() {
                Log.d(TAG, "onGameModeEnd() called");
                MiniGameManager.getInstance().setGameSafeZoneMargin(0, 0, 0, 0);
                MiniGameManager.getInstance().onDestroy();

                seatLayoutConfig.rowConfigs = Arrays.asList(
                    new ZEGOLiveAudioRoomLayoutRowConfig(4, ZEGOLiveAudioRoomLayoutAlignment.SPACE_AROUND),
                    new ZEGOLiveAudioRoomLayoutRowConfig(4, ZEGOLiveAudioRoomLayoutAlignment.SPACE_AROUND));
                ZEGOLiveAudioRoomManager.getInstance().updateSeatConfig(seatLayoutConfig);
                binding.seatContainer.setLayoutConfig(seatLayoutConfig);
                binding.seatContainer.updateSeats(ZEGOLiveAudioRoomManager.getInstance().getAudioRoomSeatList());
                binding.seatContainer.updateSeatViewSize(false);

            }
        });
    }

    private void initSeat() {
        seatLayoutConfig = new ZEGOLiveAudioRoomLayoutConfig();
        seatLayoutConfig.rowSpacing = Utils.dp2px(8, getResources().getDisplayMetrics());
        ZEGOLiveAudioRoomManager.getInstance().setSeatConfig(seatLayoutConfig);
        binding.seatContainer.setLayoutConfig(seatLayoutConfig);
    }

    private void initAfterJoinRoom() {
        ZEGOSDKManager.getInstance().zimService.addOutgoingInvitationListener(new OutgoingInvitationListener() {
            @Override
            public void onActionSendInvitation(int errorCode, String invitationID, String extendedData,
                List<String> errorInvitees) {

            }

            @Override
            public void onActionCancelInvitation(int errorCode, String invitationID, List<String> errorInvitees) {

            }

            @Override
            public void onSendInvitationButReceiveResponseTimeout(String invitationID, List<String> invitees) {

            }

            @Override
            public void onSendInvitationAndIsAccepted(String invitationID, String invitee, String extendedData) {
                int seatIndex = ZEGOLiveAudioRoomManager.getInstance().findFirstAvailableSeatIndex();
                ZEGOLiveAudioRoomManager.getInstance().takeSeat(seatIndex, new ZIMRoomAttributesOperatedCallback() {
                    @Override
                    public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys,
                        ZIMError errorInfo) {

                    }
                });
            }

            @Override
            public void onSendInvitationButIsRejected(String invitationID, String invitee, String extendedData) {

            }
        });

        ZEGOLiveAudioRoomManager.getInstance().addSeatUserChangeListener(new SeatUserChangeListener() {
            @Override
            public void onSeatChanged(List<ZEGOLiveAudioRoomSeat> changedSeats) {
                int seatIndex = ZEGOLiveAudioRoomManager.getInstance().findMyRoomSeatIndex();
                Log.d(TAG, "onSeatChanged() called with: changedSeats = [" + changedSeats + "],seatIndex:" + seatIndex);
                if (seatIndex != -1) {
                    ZEGOSDKManager.getInstance().rtcService.openMicrophone(true);
                    ZEGOSDKManager.getInstance().rtcService.startPublishLocalAudioVideo();
                } else {
                    ZEGOSDKManager.getInstance().rtcService.openMicrophone(false);
                    ZEGOSDKManager.getInstance().rtcService.stopPublishLocalAudioVideo();
                }
            }
        });
        ZEGOSDKManager.getInstance().rtcService.addStreamChangeListener(new RoomStreamChangeListener() {
            @Override
            public void onStreamAdd(List<ZEGOCLOUDUser> userList) {
                for (ZEGOCLOUDUser zegocloudUser : userList) {
                    String streamID = ZEGOSDKManager.getInstance().rtcService.generateStream(zegocloudUser.userID);
                    ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
                    if (!Objects.equals(localUser, zegocloudUser)) {
                        ZEGOSDKManager.getInstance().rtcService.startPlayRemoteAudioVideo(streamID);
                    }
                }
            }

            @Override
            public void onStreamRemove(List<ZEGOCLOUDUser> userList) {
                for (ZEGOCLOUDUser zegocloudUser : userList) {
                    String streamID = ZEGOSDKManager.getInstance().rtcService.generateStream(zegocloudUser.userID);
                    ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
                    if (!Objects.equals(localUser, zegocloudUser)) {
                        ZEGOSDKManager.getInstance().rtcService.stopPlayRemoteAudioVideo(streamID);
                    }
                }
            }
        });
    }

    private static final String TAG = "LiveAudioRoomActivity";

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            ZEGOSDKManager.getInstance().leaveRoom();
            ZEGOSDKManager.getInstance().rtcService.clear();
            ZEGOSDKManager.getInstance().rtcService.openMicrophone(false);
            ZEGOLiveAudioRoomManager.getInstance().leaveRoom();
            MiniGameManager.getInstance().setGameSafeZoneMargin(0, 0, 0, 0);
            MiniGameManager.getInstance().onDestroy();

        }
    }
}