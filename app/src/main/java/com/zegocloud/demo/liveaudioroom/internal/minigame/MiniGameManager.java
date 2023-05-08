package com.zegocloud.demo.liveaudioroom.internal.minigame;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.liveaudioroom.backend.Backend;
import com.zegocloud.demo.liveaudioroom.internal.ZEGOSDKManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tech.sud.mgp.SudMGPWrapper.decorator.SudFSMMGDecorator;
import tech.sud.mgp.SudMGPWrapper.decorator.SudFSMMGListener;
import tech.sud.mgp.SudMGPWrapper.decorator.SudFSTAPPDecorator;
import tech.sud.mgp.SudMGPWrapper.model.GameConfigModel;
import tech.sud.mgp.SudMGPWrapper.model.GameViewInfoModel;
import tech.sud.mgp.SudMGPWrapper.state.MGStateResponse;
import tech.sud.mgp.SudMGPWrapper.state.SudMGPMGState;
import tech.sud.mgp.SudMGPWrapper.utils.ISudFSMStateHandleUtils;
import tech.sud.mgp.SudMGPWrapper.utils.SudJsonUtils;
import tech.sud.mgp.core.ISudFSMStateHandle;
import tech.sud.mgp.core.ISudFSTAPP;
import tech.sud.mgp.core.ISudListenerInitSDK;
import tech.sud.mgp.core.SudMGP;


public class MiniGameManager implements SudFSMMGListener {

    private String gameRoomID; // game room ID
    private long playingGameID; // current play game id
    public final SudFSTAPPDecorator sudFSTAPPDecorator = new SudFSTAPPDecorator(); // Mini game interface encapsulation
    private final SudFSMMGDecorator sudFSMMGDecorator = new SudFSMMGDecorator(); // Mini game callback encapsulation

    public View gameView; // ganmeView
    public GameConfigModel gameConfigModel = new GameConfigModel(); // game config
    protected final Handler handler = new Handler(Looper.getMainLooper());

    public String languageCode = "en-US";   // https://docs.sud.tech/en-US/app/Client/Languages/
    public String userID = "";
    public String miniGame_APP_ID = "";
    public String miniGame_APP_KEY = "";
    public MiniGameCallback mCallback;
    public boolean autoJoinGame = false;

    public int safeZoneLeft;
    public int safeZoneTop;
    public int safeZoneRight;
    public int safeZoneBottom;

    public final MutableLiveData<View> gameViewLiveData = new MutableLiveData<>();

    private static final class Holder {

        private static final MiniGameManager INSTANCE = new MiniGameManager();
    }

    public static MiniGameManager getInstance() {
        return MiniGameManager.Holder.INSTANCE;
    }

    public interface MiniGameCallback {
        void onGamePlayerIconPositionUpdate(SudMGPMGState.MGCommonGamePlayerIconPosition model);
        void onGameMGCommonGameState(SudMGPMGState.MGCommonGameState model);
        void onPlayerMGCommonPlayerIn(String userId, SudMGPMGState.MGCommonPlayerIn model);
    }

    public void setCallback(MiniGameCallback callback) {
        mCallback = callback;
    }

    private static final String TAG = "MiniGameManager";
    public void initMiniGameSDK(Activity activity, String appId, String appKey, boolean isTestEnv) {
        miniGame_APP_ID = appId;
        miniGame_APP_KEY = appKey;
        // init mini game SDK
        SudMGP.initSDK(activity, appId, appKey, isTestEnv, new ISudListenerInitSDK() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess() called");
            }

            @Override
            public void onFailure(int errCode, String errMsg) {
                Log.d(TAG, "onFailure() called with: errCode = [" + errCode + "], errMsg = [" + errMsg + "]");
            }
        });
    }

    public void preloadGameList(Activity activity, List<Long> mgIDList) {
        SudMGP.preloadMGPkgList(activity, mgIDList, null);
    }

    public  void setGameCaptain(String userID) {
        sudFSTAPPDecorator.notifyAPPCommonSelfCaptain(userID);
    }

    public void loadGame(Activity activity, String userID, String gameRoomID, long gameID, String code) {
        if (activity.isDestroyed() || gameID == playingGameID) {
            return;
        }
        this.playingGameID = gameID;
        this.gameRoomID = gameRoomID;
        this.userID = userID;

        sudFSMMGDecorator.setSudFSMMGListener(this);

        ISudFSTAPP iSudFSTAPP = SudMGP.loadMG(activity, userID, gameRoomID, code, gameID, getLanguageCode(), sudFSMMGDecorator);

        if (iSudFSTAPP == null) {
            Toast.makeText(activity, "loadMG params error", Toast.LENGTH_LONG).show();
            delayLoadGame(activity, userID, gameRoomID, gameID, code);
            return;
        }

        sudFSTAPPDecorator.setISudFSTAPP(iSudFSTAPP);

        gameView = iSudFSTAPP.getGameView();
        onAddGameView(gameView);
    }

    private void delayLoadGame(Activity activity, String userID, String gameRoomID, long gameID, String code) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadGame(activity, userID, gameRoomID, gameID, code);
            }
        }, 5000);
    }

    private void destroyMG() {
        if (playingGameID > 0) {
            sudFSTAPPDecorator.destroyMG();
            sudFSMMGDecorator.destroyMG();
            playingGameID = 0;
            gameView = null;
            onRemoveGameView();
        }
    }

    public void switchGame(Activity activity, String userID, String gameRoomID, long gameID, String code) {
        if (TextUtils.isEmpty(gameRoomID)) {
            Toast.makeText(activity, "gameRoomId can not be empty", Toast.LENGTH_LONG).show();
            return;
        }

        if (playingGameID == gameID && gameRoomID.equals(this.gameRoomID)) {
            return;
        }

        this.playingGameID = gameID;
        this.gameRoomID = gameRoomID;
        this.userID = userID;

        destroyMG();
        this.gameRoomID = gameRoomID;
        playingGameID = gameID;
        loadGame(activity, userID, gameRoomID, gameID, code);
    }

    public void onDestroy() {
        destroyMG();
    }

    public void joinGame() {
        sudFSTAPPDecorator.notifyAPPCommonSelfIn(true, -1, true, 1);
    }

    public  void updateReadyStatus() {
        sudFSTAPPDecorator.notifyAPPCommonSelfReady(true);
    }

    public void setGameSafeZoneMargin(int left,int top,int right,int bottom){
        safeZoneLeft = left;
        safeZoneTop = top;
        safeZoneRight = right;
        safeZoneBottom = bottom;
    }

    private void getCode(String userID, GameGetCodeListener listener){

        ZEGOLiveAudioRoomManager.getInstance().getCode(new Backend.Result() {
            @Override
            public void onResult(int errorCode, String message) {
                if(errorCode == 0) {
                    try {
                        JSONObject object = new JSONObject(message);
                        String code = (String) object.get("code");
                        listener.onSuccess(code);
                    } catch (JSONException e) {
                        listener.onFailed();
                        throw new RuntimeException(e);
                    }
                } else {
                    listener.onFailed();
                }
            }
        });
    }

    private String getUserID() { return this.userID; };
    private String getAppID() { return this.miniGame_APP_ID; };
    private String getAppKey() { return  this.miniGame_APP_KEY; };
    private String getLanguageCode() { return  this.languageCode; };


    private void getGameRect(GameViewInfoModel gameViewInfoModel) {};
    private void onAddGameView(View gameView) {
        gameViewLiveData.setValue(gameView);
    };
    private void onRemoveGameView() {
        gameViewLiveData.setValue(null);
    };

    @Override
    public void onGamePlayerIconPositionUpdate(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGamePlayerIconPosition model) {
        mCallback.onGamePlayerIconPositionUpdate(model);
    }

    public  void onGameMGCommonGameState(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameState model) {
        mCallback.onGameMGCommonGameState(model);
    }

    public void onPlayerMGCommonPlayerIn(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonPlayerIn model) {
        mCallback.onPlayerMGCommonPlayerIn(userId, model);
    }

    @Override
    public void onGameLog(String str) {
        SudFSMMGListener.super.onGameLog(str);
    }

    @Override
    public void onGameStarted() {
        if (autoJoinGame) {
            joinGame();
            updateReadyStatus();
        }
    }

    @Override
    public void onGameDestroyed() {
    }

    @Override
    public void onExpireCode(ISudFSMStateHandle handle, String dataJson) {
        processOnExpireCode(sudFSTAPPDecorator, handle);
    }

    @Override
    public void onGetGameViewInfo(ISudFSMStateHandle handle, String dataJson) {
        processOnGetGameViewInfo(gameView, handle);
    }

    @Override
    public void onGetGameCfg(ISudFSMStateHandle handle, String dataJson) {
        processOnGetGameCfg(handle, dataJson);
    }

    public void processOnExpireCode(SudFSTAPPDecorator sudFSTAPPDecorator, ISudFSMStateHandle handle) {
        getCode(getUserID(), new GameGetCodeListener() {
            @Override
            public void onSuccess(String code) {
                MGStateResponse mgStateResponse = new MGStateResponse();
                mgStateResponse.ret_code = MGStateResponse.SUCCESS;
                sudFSTAPPDecorator.updateCode(code, null);
                handle.success(SudJsonUtils.toJson(mgStateResponse));
            }

            @Override
            public void onFailed() {
                MGStateResponse mgStateResponse = new MGStateResponse();
                mgStateResponse.ret_code = -1;
                handle.failure(SudJsonUtils.toJson(mgStateResponse));
            }
        });
    }

    public void processOnGetGameViewInfo(View gameView, ISudFSMStateHandle handle) {
        int gameViewWidth = gameView.getMeasuredWidth();
        int gameViewHeight = gameView.getMeasuredHeight();
        if (gameViewWidth > 0 && gameViewHeight > 0) {
            notifyGameViewInfo(handle, gameViewWidth, gameViewHeight);
            return;
        }

        gameView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gameView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = gameView.getMeasuredWidth();
                int height = gameView.getMeasuredHeight();
                notifyGameViewInfo(handle, width, height);
            }
        });
    }

    private void notifyGameViewInfo(ISudFSMStateHandle handle, int gameViewWidth, int gameViewHeight) {
        GameViewInfoModel gameViewInfoModel = new GameViewInfoModel();
        gameViewInfoModel.ret_code = 0;

        gameViewInfoModel.view_size.width = gameViewWidth;
        gameViewInfoModel.view_size.height = gameViewHeight;

        getGameRect(gameViewInfoModel);
        gameViewInfoModel.view_game_rect.top = safeZoneTop;
        gameViewInfoModel.view_game_rect.left = safeZoneLeft;
        gameViewInfoModel.view_game_rect.bottom = safeZoneBottom;
        gameViewInfoModel.view_game_rect.right = safeZoneRight;

        String json = SudJsonUtils.toJson(gameViewInfoModel);
        // 如果设置安全区有疑问，可将下面的日志打印出来，分析json数据
         Log.d("SudBaseGameViewModel", "notifyGameViewInfo:" + json);
        handle.success(json);
    }

    public void onPause() {
        sudFSTAPPDecorator.pauseMG();
    }

    public void onResume() {
        sudFSTAPPDecorator.playMG();
    }

    public void processOnGetGameCfg(ISudFSMStateHandle handle, String dataJson) {
        handle.success(SudJsonUtils.toJson(gameConfigModel));
    }

    public interface GameGetCodeListener {
        void onSuccess(String code);
        void onFailed();
    }

}
