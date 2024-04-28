package com.zegocloud.minigame.demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoStreamResourceMode;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoPlayerConfig;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import tech.sud.mgp.SudMGPWrapper.state.SudMGPAPPState;


public class GameActivity extends AppCompatActivity {

    public static int appID = Your App ID; // Get from ZEGOCLOUD console https://console.zegocloud.com/
    public static  String appSign = "Your App Sign"; // Get from ZEGOCLOUD console https://console.zegocloud.com/
    public static String userID = QuickStartUtils.genUserID();;
    public static String userName = "Name" + QuickStartUtils.genUserID();
    public static String roomID = "room_111";

    private final QuickStartGameViewModel gameViewModel = new QuickStartGameViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent intent = getIntent();
        long gameID = intent.getLongExtra("gameID", 1468180338417074177L);

        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameViewModel.gameViewLiveData.observe(this, new Observer<View>() {
            @Override
            public void onChanged(View view) {
                if (view == null) {
                    gameContainer.removeAllViews();
                } else {
                    gameContainer.addView(view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

                    SudMGPAPPState.Ludo ludo = new SudMGPAPPState.Ludo();
                    ludo.chessNum = 2;
                    ludo.item = 1;
                    ludo.mode =0;
                    gameViewModel.sudFSTAPPDecorator.notifyAPPCommonGameSettingSelectInfo(ludo);
                    gameViewModel.sudFSTAPPDecorator.notifyAPPCommonSelfIn(true, -1, true, 1);
                    gameViewModel.sudFSTAPPDecorator.notifyAPPCommonSelfReady(true);
                }
            }
        });

        joinGame(gameID);

        createEngine();
        startListenEvent();
        loginRoom();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameViewModel.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameViewModel.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameViewModel.onDestroy();

        stopListenEvent();
        logoutRoom();
        destroyEngine();

        finish();
    }
    public void onImageButtonClick(View view) {
        view.setSelected(!view.isSelected());
        muteMicrophone(view.isSelected());
    }

    private  void joinGame(long gameID) {
        String appRoomId = roomID;
        long mgId = gameID;
        gameViewModel.switchGame(this, appRoomId, mgId);
    }

    void createEngine() {
        ZegoEngineProfile profile = new ZegoEngineProfile();

        // Get your AppID and AppSign from ZEGOCLOUD Console
        //[My Projects -> AppID] : https://console.zegocloud.com/project
        profile.appID = appID;
        profile.appSign = appSign;
        profile.scenario = ZegoScenario.BROADCAST; // General scenario.
        profile.application = getApplication();
        ZegoExpressEngine.createEngine(profile, null);
    }
    // destroy engine
    private void destroyEngine() {
        ZegoExpressEngine.destroyEngine(null);
    }
    void startListenEvent() {
        ZegoExpressEngine.getEngine().setEventHandler(new IZegoEventHandler() {
            @Override
            // Callback for updates on the status of the streams in the room.
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                // When `updateType` is set to `ZegoUpdateType.ADD`, an audio and video
                // stream is added, and you can call the `startPlayingStream` method to
                // play the stream.
                if (updateType == ZegoUpdateType.ADD) {
                    startPlayStream(streamList.get(0).streamID);
                } else {
                    stopPlayStream(streamList.get(0).streamID);
                }
            }
        });
    }
    void stopListenEvent() {
        ZegoExpressEngine.getEngine().setEventHandler(null);
    }
    void loginRoom() {
        ZegoUser user = new ZegoUser(userID, userName);
        ZegoRoomConfig roomConfig = new ZegoRoomConfig();
        // The `onRoomUserUpdate` callback can be received only when
        // `ZegoRoomConfig` in which the `isUserStatusNotify` parameter is set to
        // `true` is passed.
        roomConfig.isUserStatusNotify = true;
        ZegoExpressEngine.getEngine().loginRoom(roomID, user, roomConfig, (int error, JSONObject extendedData) -> {
            // Room login result. This callback is sufficient if you only need to
            // check the login result.
            if (error == 0) {
                // Login successful.
                // Start the preview and stream publishing.
                Toast.makeText(this, "Login successful.", Toast.LENGTH_LONG).show();
                startPublish();
            } else {
                // Login failed. For details, see [Error codes\|_blank](/404).
                Toast.makeText(this, "Login failed. error = " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
    void logoutRoom() {
        ZegoExpressEngine.getEngine().logoutRoom();
    }
    void startPublish() {
        // After calling the `loginRoom` method, call this method to publish streams.
        // The StreamID must be unique in the room.
        String streamID = roomID + "_" + userID + "_call";
        ZegoExpressEngine.getEngine().enableCamera(false);
        ZegoExpressEngine.getEngine().startPublishingStream(streamID);
    }
    void stopPublish() {
        ZegoExpressEngine.getEngine().stopPublishingStream();
    }
    void startPlayStream(String streamID) {
        ZegoPlayerConfig config = new ZegoPlayerConfig();
        config.resourceMode = ZegoStreamResourceMode.DEFAULT;
        ZegoExpressEngine.getEngine().startPlayingStream(streamID, config);
    }
    void stopPlayStream(String streamID) {
        ZegoExpressEngine.getEngine().stopPlayingStream(streamID);
    }

    void muteMicrophone(Boolean mute) {
        ZegoExpressEngine.getEngine().muteMicrophone(mute);
    }

}