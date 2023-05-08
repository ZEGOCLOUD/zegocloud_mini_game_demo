package com.zegocloud.demo.liveaudioroom.activity;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import com.zegocloud.demo.liveaudioroom.R;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.liveaudioroom.backend.BackendUser;
import com.zegocloud.demo.liveaudioroom.internal.ZEGOSDKManager;
import com.zegocloud.demo.liveaudioroom.internal.minigame.MiniGameManager;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOCLOUDUser;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOExpressService.RoomStreamChangeListener;
import im.zego.zegoexpress.constants.ZegoViewMode;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import org.json.JSONObject;
import tech.sud.mgp.SudMGPWrapper.decorator.SudFSMMGDecorator;
import tech.sud.mgp.SudMGPWrapper.state.SudMGPMGState;

public class GameRoomActivity extends AppCompatActivity {

    private RoomStreamChangeListener roomStreamChangeListener;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_room);

        MiniGameManager.getInstance().autoJoinGame = true;

        roomStreamChangeListener = new RoomStreamChangeListener() {
            @Override
            public void onStreamAdd(List<ZEGOCLOUDUser> userList) {
                Log.d(TAG, "onStreamAdd() called with: userList = [" + userList + "]");
                // When `updateType` is set to `ZegoUpdateType.ADD`, an audio and video
                // stream is added, and you can call the `startPlayingStream` method to
                // play the stream.
                for (ZEGOCLOUDUser zegocloudUser : userList) {
                    String streamID = ZEGOSDKManager.getInstance().rtcService.generateStream(zegocloudUser.userID);
                    ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
                    if (!Objects.equals(localUser, zegocloudUser)) {
                        startPlayStream(streamID);
                    }
                }
            }

            @Override
            public void onStreamRemove(List<ZEGOCLOUDUser> userList) {
                for (ZEGOCLOUDUser zegocloudUser : userList) {
                    String streamID = ZEGOSDKManager.getInstance().rtcService.generateStream(zegocloudUser.userID);
                    ZEGOCLOUDUser localUser = ZEGOSDKManager.getInstance().rtcService.getLocalUser();
                    if (!Objects.equals(localUser, zegocloudUser)) {
                        stopPlayStream(streamID);
                    }
                }
            }
        };

        startListenEvent();

        FrameLayout gameContainer = findViewById(R.id.game_container);
        MiniGameManager.getInstance().gameViewLiveData.observe(this, new Observer<View>() {
            public void onChanged(@Nullable View view) {
                if (view == null) {
                    gameContainer.removeAllViews();
                } else {
                    gameContainer.addView(view, FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);
                    MiniGameManager.getInstance().joinGame();
                    MiniGameManager.getInstance().updateReadyStatus();
                }
            }
        });

        MiniGameManager.getInstance().setCallback(new MiniGameManager.MiniGameCallback() {
            @Override
            public void onGamePlayerIconPositionUpdate(SudMGPMGState.MGCommonGamePlayerIconPosition model) {
                TextureView view = findViewById(R.id.remoteUserView);
                Logger.getLogger(SudFSMMGDecorator.class.getName()).info("======Remote User." + model.uid);
                if (Objects.equals(model.uid, ZEGOLiveAudioRoomManager.getInstance().getBackendUser().getUid())) {
                    view = findViewById(R.id.preview);
                    Logger.getLogger(SudFSMMGDecorator.class.getName()).info("======Local User." + model.uid);
                }
                FrameLayout container = findViewById(R.id.game_container);
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();

                int width = dpToPx(55);
                layoutParams.leftToLeft = R.id.game_container;
                layoutParams.topToTop = R.id.game_container;
                layoutParams.leftMargin = (int) (container.getLeft() + model.position.x - width / 2.0);
                layoutParams.topMargin = (int) (container.getTop() + model.position.y - width / 2.0);
                layoutParams.width = width;
                layoutParams.height = width;

                view.setLayoutParams(layoutParams);
            }

            @Override
            public void onGameMGCommonGameState(SudMGPMGState.MGCommonGameState model) {
                TextureView remoteUserView = findViewById(R.id.remoteUserView);
                TextureView localUserView = findViewById(R.id.preview);
                if (model.gameState == 2) {
                    Logger.getLogger(SudFSMMGDecorator.class.getName())
                        .info("======onGameMGCommonGameState" + model.gameState);
                    remoteUserView.setVisibility(View.VISIBLE);
                    localUserView.setVisibility(View.VISIBLE);
                } else {
                    Logger.getLogger(SudFSMMGDecorator.class.getName())
                        .info("======onGameMGCommonGameState INVISIBLE" + model.gameState);
                    remoteUserView.setVisibility(View.INVISIBLE);
                    localUserView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPlayerMGCommonPlayerIn(String userId, SudMGPMGState.MGCommonPlayerIn model) {

            }
        });

        String[] permissionNeeded = {"android.permission.CAMERA", "android.permission.RECORD_AUDIO"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA")
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO")
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissionNeeded, 101);
            }
        }

        String roomID = getIntent().getStringExtra("roomID");
        loginRoom(roomID);
    }

    private static final String TAG = "GameRoomActivity";

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            MiniGameManager.getInstance().onDestroy();

            stopPublish();
            stopListenEvent();
            logoutRoom();
        }
    }

    public int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    void loginRoom(String roomID) {
        ZEGOSDKManager.getInstance().joinRoom(roomID, (int error, JSONObject extendedData) -> {
            // Room login result. This callback is sufficient if you only need to
            // check the login result.
            if (error == 0) {
                // Login successful.
                // Start the preview and stream publishing.
                Toast.makeText(this, "Login successful.", Toast.LENGTH_LONG).show();
                startPreview();
                startPublish(roomID);

                long gameID = getIntent().getLongExtra("gameID", 0L);
                BackendUser backendUser = ZEGOLiveAudioRoomManager.getInstance().getBackendUser();
                MiniGameManager.getInstance().loadGame(this, backendUser.getUid(), roomID, gameID,
                    ZEGOLiveAudioRoomManager.getInstance().getCode());

            } else {
                // Login failed. For details, see [Error codes\|_blank](/404).
                Toast.makeText(this, "Login failed. error = " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    void logoutRoom() {
        ZEGOSDKManager.getInstance().leaveRoom();
    }

    void startPreview() {
        TextureView textureView = findViewById(R.id.preview);
        ZEGOSDKManager.getInstance().rtcService.startCameraPreview(textureView, ZegoViewMode.ASPECT_FILL);
    }

    void stopPreview() {
        ZEGOSDKManager.getInstance().rtcService.stopCameraPreview();
    }

    void startPublish(String roomID) {
        // After calling the `loginRoom` method, call this method to publish streams.
        // The StreamID must be unique in the room.
        TextureView textureView = findViewById(R.id.preview);
        ZEGOSDKManager.getInstance().rtcService.startCameraPreview(textureView, ZegoViewMode.ASPECT_FILL);
        ZEGOSDKManager.getInstance().rtcService.startPublishLocalAudioVideo();
    }


    void stopPublish() {
        ZEGOSDKManager.getInstance().rtcService.stopPublishLocalAudioVideo();
    }

    void startPlayStream(String streamID) {
        Log.d(TAG, "startPlayStream() called with: streamID = [" + streamID + "]");
        TextureView textureView = findViewById(R.id.remoteUserView);
        ZEGOSDKManager.getInstance().rtcService.startPlayRemoteAudioVideo(textureView, streamID,
            ZegoViewMode.ASPECT_FILL);
    }

    void stopPlayStream(String streamID) {
        ZEGOSDKManager.getInstance().rtcService.stopPlayRemoteAudioVideo(streamID);
    }

    private void startListenEvent() {
        ZEGOSDKManager.getInstance().rtcService.addStreamChangeListener(roomStreamChangeListener);
    }

    void stopListenEvent() {
        ZEGOSDKManager.getInstance().rtcService.removeStreamChangeListener(roomStreamChangeListener);
    }
}