package com.zegocloud.demo.liveaudioroom.activity;

import android.Manifest.permission;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.demo.liveaudioroom.R;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.liveaudioroom.backend.Backend.Result;
import com.zegocloud.demo.liveaudioroom.backend.BackendUser;
import com.zegocloud.demo.liveaudioroom.backend.Game;
import com.zegocloud.demo.liveaudioroom.databinding.ActivityMainBinding;
import com.zegocloud.demo.liveaudioroom.internal.ZEGOSDKManager;
import com.zegocloud.demo.liveaudioroom.internal.ZIMService.PeerMessageListener;
import com.zegocloud.demo.liveaudioroom.internal.minigame.MiniGameManager;
import im.zego.zim.entity.ZIMCommandMessage;
import im.zego.zim.entity.ZIMMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Dialog loading;
    private List<Game> gameList = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (loading != null) {
                loading.dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tabAudioRoom.setOnClickListener(v -> {
            binding.layoutAudioRoom.setVisibility(View.VISIBLE);
            binding.layoutGame.setVisibility(View.GONE);
        });

        binding.tabGame.setOnClickListener(v -> {
            binding.layoutAudioRoom.setVisibility(View.GONE);
            binding.layoutGame.setVisibility(View.VISIBLE);
        });

        Game ludo = new Game(1468180338417074177L, "Ludo", 10);
        Game umo = new Game(1472142559912517633L, "UMO", 10);
        Game rockScissionsPaper = new Game(1468434723902660610L, "Rock Scissions Paper", 10);
        gameList.add(ludo);
        gameList.add(umo);
        gameList.add(rockScissionsPaper);

        BackendUser backendUser = ZEGOLiveAudioRoomManager.getInstance().getBackendUser();
        binding.coinsCount.setText(String.valueOf(backendUser.getCoins()));

        binding.gameLudo.setOnClickListener(v -> {
            if (loading == null) {
                loading = new Dialog(MainActivity.this);
                loading.setCanceledOnTouchOutside(false);
                loading.setCancelable(false);
                loading.setContentView(R.layout.layout_loading);
            }
            handler.postDelayed(runnable, 60 * 1000);
            loading.show();
            ZEGOLiveAudioRoomManager.getInstance().playerMatch(backendUser.getUid(), ludo.gameID, new Result() {
                @Override
                public void onResult(int errorCode, String message) {
                    if (errorCode != 0) {
                        if (loading != null) {
                            loading.dismiss();
                        }
                        handler.removeCallbacks(runnable);
                    }
                }
            });
        });

        binding.gameTeenPatti.setOnClickListener(v -> {
            if (loading == null) {
                loading = new Dialog(MainActivity.this);
                loading.setCanceledOnTouchOutside(false);
                loading.setCancelable(false);
                loading.setContentView(R.layout.layout_loading);
            }
            handler.postDelayed(runnable, 60 * 1000);
            loading.show();
            ZEGOLiveAudioRoomManager.getInstance().playerMatch(backendUser.getUid(), umo.gameID, new Result() {
                @Override
                public void onResult(int errorCode, String message) {
                    if (errorCode != 0) {
                        if (loading != null) {
                            loading.dismiss();
                        }
                        handler.removeCallbacks(runnable);
                    }
                }
            });
        });

        List<Long> mgIDList = new ArrayList<Long>(Arrays.asList(ludo.gameID.longValue(), umo.gameID.longValue(),
            rockScissionsPaper.gameID.longValue()));
        preloadGame(mgIDList);

        ZEGOSDKManager.getInstance().zimService.addPeerMessageListener(new PeerMessageListener() {
            @Override
            public void onReceivePeerMessage(ArrayList<ZIMMessage> messageList, String fromUserID) {
                for (ZIMMessage zimMessage : messageList) {
                    if (zimMessage instanceof ZIMCommandMessage) {
                        ZIMCommandMessage commandMessage = (ZIMCommandMessage) zimMessage;
                        String message = new String(commandMessage.message);
                        Game findGame = null;
                        String roomID = null;
                        try {
                            JSONObject jsonObject = new JSONObject(message);
                            roomID = jsonObject.getString("roomID");
                            long gameID = Long.parseLong(jsonObject.getString("gm_id"));
                            for (Game game : gameList) {
                                if (game.gameID == gameID) {
                                    findGame = game;
                                    break;
                                }
                            }
                        } catch (JSONException e) {

                        }
                        if (roomID != null && findGame != null) {
                            onGameMatched(findGame, roomID);
                        }
                        Log.d(TAG, "addPeerMessageListener() ，onReceivePeerMessage called with: commandMessage = [" + message + "],  = ["
                            + commandMessage.extendedData + "]");
                    }
                }
            }
        });
        initAudioRoom();
    }

    private static final String TAG = "MainActivity";

    private void preloadGame(List<Long> mgIDList) {
        MiniGameManager.getInstance().preloadGameList(this, mgIDList);
    }

    private void initAudioRoom() {
        binding.liveId.getEditText().setText("2321121");

        binding.startLive.setOnClickListener(v -> {
            String liveID = binding.liveId.getEditText().getText().toString();
            if (TextUtils.isEmpty(liveID)) {
                binding.liveId.setError("please input liveID");
                return;
            }
            List<String> permissions = Arrays.asList(permission.RECORD_AUDIO);
            requestPermissionIfNeeded(permissions, new RequestCallback() {
                @Override
                public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                    @NonNull List<String> deniedList) {
                    if (allGranted) {
                        Intent intent = new Intent(MainActivity.this, LiveAudioRoomActivity.class);
                        intent.putExtra("host", true);
                        intent.putExtra("roomID", liveID);
                        startActivity(intent);
                    }
                }
            });
        });

        binding.watchLive.setOnClickListener(v -> {
            String liveID = binding.liveId.getEditText().getText().toString();
            if (TextUtils.isEmpty(liveID)) {
                binding.liveId.setError("please input liveID");
                return;
            }
            Intent intent = new Intent(MainActivity.this, LiveAudioRoomActivity.class);
            intent.putExtra("host", false);
            intent.putExtra("roomID", liveID);
            startActivity(intent);

        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            ZEGOSDKManager.getInstance().disconnectUser();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private void requestPermissionIfNeeded(List<String> permissions, RequestCallback requestCallback) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (allGranted) {
            requestCallback.onResult(true, permissions, new ArrayList<>());
            return;
        }

        PermissionX.init(this).permissions(permissions).onExplainRequestReason((scope, deniedList) -> {
            String message = "";
            if (permissions.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = this.getString(R.string.permission_explain_camera);
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = this.getString(R.string.permission_explain_mic);
                }
            } else {
                if (deniedList.size() == 1) {
                    if (deniedList.contains(permission.CAMERA)) {
                        message = this.getString(R.string.permission_explain_camera);
                    } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                        message = this.getString(R.string.permission_explain_mic);
                    }
                } else {
                    message = this.getString(R.string.permission_explain_camera_mic);
                }
            }
            scope.showRequestReasonDialog(deniedList, message, getString(R.string.ok));
        }).onForwardToSettings((scope, deniedList) -> {
            String message = "";
            if (permissions.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = this.getString(R.string.settings_camera);
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = this.getString(R.string.settings_mic);
                }
            } else {
                if (deniedList.size() == 1) {
                    if (deniedList.contains(permission.CAMERA)) {
                        message = this.getString(R.string.settings_camera);
                    } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                        message = this.getString(R.string.settings_mic);
                    }
                } else {
                    message = this.getString(R.string.settings_camera_mic);
                }
            }
            scope.showForwardToSettingsDialog(deniedList, message, getString(R.string.settings),
                getString(R.string.cancel));
        }).request(new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                @NonNull List<String> deniedList) {
                if (requestCallback != null) {
                    requestCallback.onResult(allGranted, grantedList, deniedList);
                }
            }
        });
    }

    private void onGameMatched(Game game, String roomID) {
        if (loading != null) {
            loading.dismiss();
        }
        AlertDialog.Builder builder = new Builder(MainActivity.this);
        builder.setTitle("Entry Fee");
        builder.setMessage(game.coin + " coins");
        builder.setPositiveButton("Start", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                BackendUser backendUser = ZEGOLiveAudioRoomManager.getInstance().getBackendUser();
                ZEGOLiveAudioRoomManager.getInstance().coinsConsumption(backendUser.getUid(), game.coin, new Result() {
                    @Override
                    public void onResult(int errorCode, String message) {
                        Intent intent = new Intent(MainActivity.this, GameRoomActivity.class);
                        intent.putExtra("gameID", game.gameID);
                        intent.putExtra("roomID", roomID);
                        startActivity(intent);
                    }
                });
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
}