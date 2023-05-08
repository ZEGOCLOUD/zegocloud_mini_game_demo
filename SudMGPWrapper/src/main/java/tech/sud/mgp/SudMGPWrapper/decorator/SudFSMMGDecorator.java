/*
 * Copyright © Sud.Tech
 * https://sud.tech
 */

package tech.sud.mgp.SudMGPWrapper.decorator;

import java.util.logging.Logger;

import tech.sud.mgp.SudMGPWrapper.state.SudMGPMGState;
import tech.sud.mgp.SudMGPWrapper.utils.ISudFSMStateHandleUtils;
import tech.sud.mgp.SudMGPWrapper.utils.SudJsonUtils;
import tech.sud.mgp.core.ISudFSMMG;
import tech.sud.mgp.core.ISudFSMStateHandle;

/**
 * ISudFSMMG is a decorator class for game callback in SudAPP.
 */
public class SudFSMMGDecorator implements ISudFSMMG {

    // callback
    private SudFSMMGListener sudFSMMGListener;

    // Encapsulation of data status.
    private final SudFSMMGCache sudFSMMGCache = new SudFSMMGCache();

    /**
     * setting callback
     *
     * @param listener Listener
     */
    public void setSudFSMMGListener(SudFSMMGListener listener) {
        sudFSMMGListener = listener;
    }

    /**
     * Game log
     * Minimum version：v1.1.30.xx
     */
    @Override
    public void onGameLog(String dataJson) {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null) {
            listener.onGameLog(dataJson);
        }
    }

    /**
     * Game loading progress
     *
     * @param stage    Stage：start=1,loading=2,end=3
     * @param retCode  Error code: 0 for success
     * @param progress Progress: [0, 100]
     */
    @Override
    public void onGameLoadingProgress(int stage, int retCode, int progress) {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null) {
            listener.onGameLoadingProgress(stage, retCode, progress);
        }
    }

    /**
     * Game start
     * Minimum version：v1.1.30.xx
     */
    @Override
    public void onGameStarted() {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null) {
            listener.onGameStarted();
        }
    }

    /**
     * Game destruction
     * Minimum version：v1.1.30.xx
     */
    @Override
    public void onGameDestroyed() {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null) {
            listener.onGameDestroyed();
        }
    }

    /**
     * Code expired and needs to be implemented.
     * The APP needs to call handle.success or handle.fail.
     *
     * @param dataJson {"code":"value"}
     */
    @Override
    public void onExpireCode(ISudFSMStateHandle handle, String dataJson) {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null) {
            listener.onExpireCode(handle, dataJson);
        }
    }

    /**
     * Get game view information, needs implementation
     * APP integration needs to call handle.success or handle.fail
     *
     * @param handle   operation
     * @param dataJson {}
     */
    @Override
    public void onGetGameViewInfo(ISudFSMStateHandle handle, String dataJson) {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null) {
            listener.onGetGameViewInfo(handle, dataJson);
        }
    }

    /**
     * The game Config is obtained and needs to be implemented.
     * The APP integrator needs to call handle.success or handle.fail.
     *
     * @param handle   operation
     * @param dataJson {}
     *                 Minimum version：v1.1.30.xx
     */
    @Override
    public void onGetGameCfg(ISudFSMStateHandle handle, String dataJson) {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null) {
            listener.onGetGameCfg(handle, dataJson);
        }
    }

    /**
     * Game state change
     * The APP integration party needs to call handle.success or handle.fail
     *
     * @param handle   Action
     * @param state    State command
     * @param dataJson State value
     */
    @Override
    public void onGameStateChange(ISudFSMStateHandle handle, String state, String dataJson) {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null && listener.onGameStateChange(handle, state, dataJson)) {
            return;
        }
        switch (state) {
            case SudMGPMGState.MG_COMMON_PUBLIC_MESSAGE:
                SudMGPMGState.MGCommonPublicMessage mgCommonPublicMessage = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPublicMessage.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonPublicMessage(handle, mgCommonPublicMessage);
                }
                break;
            case SudMGPMGState.MG_COMMON_KEY_WORD_TO_HIT: 
                SudMGPMGState.MGCommonKeyWordToHit mgCommonKeyWordToHit = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonKeyWordToHit.class);
                sudFSMMGCache.onGameMGCommonKeyWordToHit(mgCommonKeyWordToHit);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonKeyWordToHit(handle, mgCommonKeyWordToHit);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_SETTLE: 
                SudMGPMGState.MGCommonGameSettle mgCommonGameSettle = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameSettle.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameSettle(handle, mgCommonGameSettle);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_JOIN_BTN:
                SudMGPMGState.MGCommonSelfClickJoinBtn mgCommonSelfClickJoinBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickJoinBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickJoinBtn(handle, mgCommonSelfClickJoinBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_CANCEL_JOIN_BTN: 
                SudMGPMGState.MGCommonSelfClickCancelJoinBtn selfClickCancelJoinBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickCancelJoinBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickCancelJoinBtn(handle, selfClickCancelJoinBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_READY_BTN:
                SudMGPMGState.MGCommonSelfClickReadyBtn mgCommonSelfClickReadyBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickReadyBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickReadyBtn(handle, mgCommonSelfClickReadyBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_CANCEL_READY_BTN:
                SudMGPMGState.MGCommonSelfClickCancelReadyBtn mgCommonSelfClickCancelReadyBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickCancelReadyBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickCancelReadyBtn(handle, mgCommonSelfClickCancelReadyBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_START_BTN: 
                SudMGPMGState.MGCommonSelfClickStartBtn mgCommonSelfClickStartBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickStartBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickStartBtn(handle, mgCommonSelfClickStartBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_SHARE_BTN: 
                SudMGPMGState.MGCommonSelfClickShareBtn mgCommonSelfClickShareBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickShareBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickShareBtn(handle, mgCommonSelfClickShareBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_STATE: 
                SudMGPMGState.MGCommonGameState mgCommonGameState = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameState.class);
                sudFSMMGCache.onGameMGCommonGameState(mgCommonGameState);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameState(handle, mgCommonGameState);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_GAME_SETTLE_CLOSE_BTN: 
                SudMGPMGState.MGCommonSelfClickGameSettleCloseBtn mgCommonSelfClickGameSettleCloseBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickGameSettleCloseBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickGameSettleCloseBtn(handle, mgCommonSelfClickGameSettleCloseBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_GAME_SETTLE_AGAIN_BTN: 
                SudMGPMGState.MGCommonSelfClickGameSettleAgainBtn mgCommonSelfClickGameSettleAgainBtn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickGameSettleAgainBtn.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfClickGameSettleAgainBtn(handle, mgCommonSelfClickGameSettleAgainBtn);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_SOUND_LIST: 
                SudMGPMGState.MGCommonGameSoundList mgCommonGameSoundList = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameSoundList.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameSoundList(handle, mgCommonGameSoundList);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_SOUND: 
                SudMGPMGState.MGCommonGameSound mgCommonGameSound = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameSound.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameSound(handle, mgCommonGameSound);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_BG_MUSIC_STATE: 
                SudMGPMGState.MGCommonGameBgMusicState mgCommonGameBgMusicState = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameBgMusicState.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameBgMusicState(handle, mgCommonGameBgMusicState);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_SOUND_STATE: 
                SudMGPMGState.MGCommonGameSoundState mgCommonGameSoundState = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameSoundState.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameSoundState(handle, mgCommonGameSoundState);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_ASR:
                SudMGPMGState.MGCommonGameASR mgCommonGameASR = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameASR.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameASR(handle, mgCommonGameASR);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_MICROPHONE:
                SudMGPMGState.MGCommonSelfMicrophone mgCommonSelfMicrophone = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfMicrophone.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfMicrophone(handle, mgCommonSelfMicrophone);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_HEADPHONE:
                SudMGPMGState.MGCommonSelfHeadphone mgCommonSelfHeadphone = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfHeadphone.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonSelfHeadphone(handle, mgCommonSelfHeadphone);
                }
                break;
            case SudMGPMGState.MG_COMMON_APP_COMMON_SELF_X_RESP:
                SudMGPMGState.MGCommonAPPCommonSelfXResp mgCommonAPPCommonSelfXResp = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonAPPCommonSelfXResp.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonAPPCommonSelfXResp(handle, mgCommonAPPCommonSelfXResp);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_ADD_AI_PLAYERS:
                SudMGPMGState.MGCommonGameAddAIPlayers mgCommonGameAddAIPlayers = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameAddAIPlayers.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameAddAIPlayers(handle, mgCommonGameAddAIPlayers);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_NETWORK_STATE: 
                SudMGPMGState.MGCommonGameNetworkState mgCommonGameNetworkState = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameNetworkState.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameNetworkState(handle, mgCommonGameNetworkState);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_GET_SCORE:
                SudMGPMGState.MGCommonGameGetScore mgCommonGameScore = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameGetScore.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameGetScore(handle, mgCommonGameScore);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_SET_SCORE:
                SudMGPMGState.MGCommonGameSetScore mgCommonGameSetScore = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameSetScore.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameSetScore(handle, mgCommonGameSetScore);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_CREATE_ORDER: 
                SudMGPMGState.MGCommonGameCreateOrder mgCommonGameCreateOrder = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameCreateOrder.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameCreateOrder(handle, mgCommonGameCreateOrder);
                }
                break;
            case SudMGPMGState.MG_COMMON_PLAYER_ROLE_ID: 
                SudMGPMGState.MGCommonPlayerRoleId mgCommonPlayerRoleId = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPlayerRoleId.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonPlayerRoleId(handle, mgCommonPlayerRoleId);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_DISCO_ACTION: 
                SudMGPMGState.MGCommonGameDiscoAction mgCommonGameDiscoAction = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameDiscoAction.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameDiscoAction(handle, mgCommonGameDiscoAction);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_DISCO_ACTION_END: 
                SudMGPMGState.MGCommonGameDiscoActionEnd mgCommonGameDiscoActionEnd = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameDiscoActionEnd.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCommonGameDiscoActionEnd(handle, mgCommonGameDiscoActionEnd);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_CONFIG: 
                SudMGPMGState.MGCustomRocketConfig mgCustomRocketConfig = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketConfig.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketConfig(handle, mgCustomRocketConfig);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_MODEL_LIST: 
                SudMGPMGState.MGCustomRocketModelList mgCustomRocketModelList = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketModelList.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketModelList(handle, mgCustomRocketModelList);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_COMPONENT_LIST: 
                SudMGPMGState.MGCustomRocketComponentList mgCustomRocketComponentList = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketComponentList.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketComponentList(handle, mgCustomRocketComponentList);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_USER_INFO:
                SudMGPMGState.MGCustomRocketUserInfo mgCustomRocketUserInfo = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketUserInfo.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketUserInfo(handle, mgCustomRocketUserInfo);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_ORDER_RECORD_LIST:
                SudMGPMGState.MGCustomRocketOrderRecordList mgCustomRocketOrderRecordList = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketOrderRecordList.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketOrderRecordList(handle, mgCustomRocketOrderRecordList);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_ROOM_RECORD_LIST: 
                SudMGPMGState.MGCustomRocketRoomRecordList mgCustomRocketRoomRecordList = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketRoomRecordList.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketRoomRecordList(handle, mgCustomRocketRoomRecordList);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_USER_RECORD_LIST: 
                SudMGPMGState.MGCustomRocketUserRecordList mgCustomRocketUserRecordList = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketUserRecordList.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketUserRecordList(handle, mgCustomRocketUserRecordList);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_SET_DEFAULT_MODEL: 
                SudMGPMGState.MGCustomRocketSetDefaultModel mgCustomRocketSetDefaultSeat = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketSetDefaultModel.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketSetDefaultModel(handle, mgCustomRocketSetDefaultSeat);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_DYNAMIC_FIRE_PRICE:
                SudMGPMGState.MGCustomRocketDynamicFirePrice mgCustomRocketDynamicFirePrice = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketDynamicFirePrice.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketDynamicFirePrice(handle, mgCustomRocketDynamicFirePrice);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_FIRE_MODEL: 
                SudMGPMGState.MGCustomRocketFireModel mGCustomRocketFireModel = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketFireModel.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketFireModel(handle, mGCustomRocketFireModel);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_CREATE_MODEL:
                SudMGPMGState.MGCustomRocketCreateModel mgCustomRocketCreateModel = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketCreateModel.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketCreateModel(handle, mgCustomRocketCreateModel);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_REPLACE_COMPONENT: 
                SudMGPMGState.MGCustomRocketReplaceComponent mgCustomRocketReplaceComponent = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketReplaceComponent.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketReplaceComponent(handle, mgCustomRocketReplaceComponent);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_BUY_COMPONENT: 
                SudMGPMGState.MGCustomRocketBuyComponent mgCustomRocketBuyComponent = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketBuyComponent.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketBuyComponent(handle, mgCustomRocketBuyComponent);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_PLAY_EFFECT_START:
                SudMGPMGState.MGCustomRocketPlayEffectStart mgCustomRocketPlayEffectStart = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketPlayEffectStart.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketPlayEffectStart(handle, mgCustomRocketPlayEffectStart);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_PLAY_EFFECT_FINISH: 
                SudMGPMGState.MGCustomRocketPlayEffectFinish mgCustomRocketPlayEffectFinish = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketPlayEffectFinish.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketPlayEffectFinish(handle, mgCustomRocketPlayEffectFinish);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_VERIFY_SIGN:
                SudMGPMGState.MGCustomRocketVerifySign mgCustomRocketVerifySign = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketVerifySign.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketVerifySign(handle, mgCustomRocketVerifySign);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_UPLOAD_MODEL_ICON: 
                SudMGPMGState.MGCustomRocketUploadModelIcon mgCustomRocketUploadModelIcon = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketUploadModelIcon.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketUploadModelIcon(handle, mgCustomRocketUploadModelIcon);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_PREPARE_FINISH: 
                SudMGPMGState.MGCustomRocketPrepareFinish mgCustomRocketPrepareFinish = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketPrepareFinish.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketPrepareFinish(handle, mgCustomRocketPrepareFinish);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_SHOW_GAME_SCENE: 
                SudMGPMGState.MGCustomRocketShowGameScene mgCustomRocketShowGameScene = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketShowGameScene.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketShowGameScene(handle, mgCustomRocketShowGameScene);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_HIDE_GAME_SCENE: 
                SudMGPMGState.MGCustomRocketHideGameScene mgCustomRocketHideGameScene = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketHideGameScene.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketHideGameScene(handle, mgCustomRocketHideGameScene);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_CLICK_LOCK_COMPONENT: 
                SudMGPMGState.MGCustomRocketClickLockComponent mgCustomRocketClickLockComponent = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketClickLockComponent.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketClickLockComponent(handle, mgCustomRocketClickLockComponent);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_FLY_CLICK: 
                SudMGPMGState.MGCustomRocketFlyClick mgCustomRocketFlyClick = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketFlyClick.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketFlyClick(handle, mgCustomRocketFlyClick);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_FLY_END: 
                SudMGPMGState.MGCustomRocketFlyEnd mgCustomRocketFlyEnd = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketFlyEnd.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketFlyEnd(handle, mgCustomRocketFlyEnd);
                }
                break;
            case SudMGPMGState.MG_CUSTOM_ROCKET_SET_CLICK_RECT: 
                SudMGPMGState.MGCustomRocketSetClickRect mgCustomRocketSetClickRect = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCustomRocketSetClickRect.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGCustomRocketSetClickRect(handle, mgCustomRocketSetClickRect);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_RANKING: 
                SudMGPMGState.MGBaseballRanking mgBaseballRanking = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballRanking.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballRanking(handle, mgBaseballRanking);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_MY_RANKING: 
                SudMGPMGState.MGBaseballMyRanking mgBaseballMyRanking = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballMyRanking.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballMyRanking(handle, mgBaseballMyRanking);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_RANGE_INFO: 
                SudMGPMGState.MGBaseballRangeInfo mgBaseballRangeInfo = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballRangeInfo.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballRangeInfo(handle, mgBaseballRangeInfo);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_SET_CLICK_RECT:
                SudMGPMGState.MGBaseballSetClickRect mgBaseballSetClickRect = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballSetClickRect.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballSetClickRect(handle, mgBaseballSetClickRect);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_PREPARE_FINISH: 
                SudMGPMGState.MGBaseballPrepareFinish mgBaseballPrepareFinish = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballPrepareFinish.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballPrepareFinish(handle, mgBaseballPrepareFinish);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_SHOW_GAME_SCENE: 
                SudMGPMGState.MGBaseballShowGameScene mgBaseballShowGameScene = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballShowGameScene.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballShowGameScene(handle, mgBaseballShowGameScene);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_HIDE_GAME_SCENE:
                SudMGPMGState.MGBaseballHideGameScene mgBaseballHideGameScene = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballHideGameScene.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballHideGameScene(handle, mgBaseballHideGameScene);
                }
                break;
            case SudMGPMGState.MG_BASEBALL_TEXT_CONFIG: 
                SudMGPMGState.MGBaseballTextConfig mgBaseballTextConfig = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGBaseballTextConfig.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGameMGBaseballTextConfig(handle, mgBaseballTextConfig);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_PLAYER_ICON_POSITION:
                Logger.getLogger(SudFSMMGDecorator.class.getName()).info("======This is an info log." + dataJson);
                SudMGPMGState.MGCommonGamePlayerIconPosition playerPosition  = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGamePlayerIconPosition.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onGamePlayerIconPositionUpdate(handle, playerPosition);
                }
                break;
            default:
                ISudFSMStateHandleUtils.handleSuccess(handle);
                break;
        }
    }


    @Override
    public void onPlayerStateChange(ISudFSMStateHandle handle, String userId, String state, String dataJson) {
        SudFSMMGListener listener = sudFSMMGListener;
        if (listener != null && listener.onPlayerStateChange(handle, userId, state, dataJson)) {
            return;
        }
        switch (state) {
            case SudMGPMGState.MG_COMMON_PLAYER_IN: 
                SudMGPMGState.MGCommonPlayerIn mgCommonPlayerIn = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPlayerIn.class);
                sudFSMMGCache.onPlayerMGCommonPlayerIn(userId, mgCommonPlayerIn);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonPlayerIn(handle, userId, mgCommonPlayerIn);
                }
                break;
            case SudMGPMGState.MG_COMMON_PLAYER_READY: 
                SudMGPMGState.MGCommonPlayerReady mgCommonPlayerReady = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPlayerReady.class);
                sudFSMMGCache.onPlayerMGCommonPlayerReady(userId, mgCommonPlayerReady);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonPlayerReady(handle, userId, mgCommonPlayerReady);
                }
                break;
            case SudMGPMGState.MG_COMMON_PLAYER_CAPTAIN: 
                SudMGPMGState.MGCommonPlayerCaptain mgCommonPlayerCaptain = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPlayerCaptain.class);
                sudFSMMGCache.onPlayerMGCommonPlayerCaptain(userId, mgCommonPlayerCaptain);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonPlayerCaptain(handle, userId, mgCommonPlayerCaptain);
                }
                break;
            case SudMGPMGState.MG_COMMON_PLAYER_PLAYING: 
                SudMGPMGState.MGCommonPlayerPlaying mgCommonPlayerPlaying = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPlayerPlaying.class);
                sudFSMMGCache.onPlayerMGCommonPlayerPlaying(userId, mgCommonPlayerPlaying);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonPlayerPlaying(handle, userId, mgCommonPlayerPlaying);
                }
                break;
            case SudMGPMGState.MG_COMMON_PLAYER_ONLINE: 
                SudMGPMGState.MGCommonPlayerOnline mgCommonPlayerOnline = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPlayerOnline.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonPlayerOnline(handle, userId, mgCommonPlayerOnline);
                }
                break;
            case SudMGPMGState.MG_COMMON_PLAYER_CHANGE_SEAT: 
                SudMGPMGState.MGCommonPlayerChangeSeat mgCommonPlayerChangeSeat = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonPlayerChangeSeat.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonPlayerChangeSeat(handle, userId, mgCommonPlayerChangeSeat);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_CLICK_GAME_PLAYER_ICON: 
                SudMGPMGState.MGCommonSelfClickGamePlayerIcon mgCommonSelfClickGamePlayerIcon = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfClickGamePlayerIcon.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonSelfClickGamePlayerIcon(handle, userId, mgCommonSelfClickGamePlayerIcon);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_DIE_STATUS:
                SudMGPMGState.MGCommonSelfDieStatus mgCommonSelfDieStatus = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfDieStatus.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonSelfDieStatus(handle, userId, mgCommonSelfDieStatus);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_TURN_STATUS: 
                SudMGPMGState.MGCommonSelfTurnStatus mgCommonSelfTurnStatus = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfTurnStatus.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonSelfTurnStatus(handle, userId, mgCommonSelfTurnStatus);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_SELECT_STATUS: 
                SudMGPMGState.MGCommonSelfSelectStatus mgCommonSelfSelectStatus = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfSelectStatus.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonSelfSelectStatus(handle, userId, mgCommonSelfSelectStatus);
                }
                break;
            case SudMGPMGState.MG_COMMON_GAME_COUNTDOWN_TIME: 
                SudMGPMGState.MGCommonGameCountdownTime mgCommonGameCountdownTime = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonGameCountdownTime.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonGameCountdownTime(handle, userId, mgCommonGameCountdownTime);
                }
                break;
            case SudMGPMGState.MG_COMMON_SELF_OB_STATUS: 
                SudMGPMGState.MGCommonSelfObStatus mgCommonSelfObStatus = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGCommonSelfObStatus.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGCommonSelfObStatus(handle, userId, mgCommonSelfObStatus);
                }
                break;
            case SudMGPMGState.MG_DG_SELECTING: 
                SudMGPMGState.MGDGSelecting mgdgSelecting = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGDGSelecting.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGDGSelecting(handle, userId, mgdgSelecting);
                }
                break;
            case SudMGPMGState.MG_DG_PAINTING: 
                SudMGPMGState.MGDGPainting mgdgPainting = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGDGPainting.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGDGPainting(handle, userId, mgdgPainting);
                }
                break;
            case SudMGPMGState.MG_DG_ERRORANSWER: 
                SudMGPMGState.MGDGErroranswer mgdgErroranswer = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGDGErroranswer.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGDGErroranswer(handle, userId, mgdgErroranswer);
                }
                break;
            case SudMGPMGState.MG_DG_TOTALSCORE: 
                SudMGPMGState.MGDGTotalscore mgdgTotalscore = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGDGTotalscore.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGDGTotalscore(handle, userId, mgdgTotalscore);
                }
                break;
            case SudMGPMGState.MG_DG_SCORE: 
                SudMGPMGState.MGDGScore mgdgScore = SudJsonUtils.fromJson(dataJson, SudMGPMGState.MGDGScore.class);
                if (listener == null) {
                    ISudFSMStateHandleUtils.handleSuccess(handle);
                } else {
                    listener.onPlayerMGDGScore(handle, userId, mgdgScore);
                }
                break;
            default:
                ISudFSMStateHandleUtils.handleSuccess(handle);
                break;
        }
    }

    /** Get the user ID of the team captain */
    public String getCaptainUserId() {
        return sudFSMMGCache.getCaptainUserId();
    }

    // Return whether the player is currently playing the game
    public boolean playerIsPlaying(String userId) {
        return sudFSMMGCache.playerIsPlaying(userId);
    }

    // Return whether the player is ready to play
    public boolean playerIsReady(String userId) {
        return sudFSMMGCache.playerIsReady(userId);
    }

    // Return whether the player has joined the game
    public boolean playerIsIn(String userId) {
        return sudFSMMGCache.playerIsIn(userId);
    }

    // Get the number of players currently in the game
    public int getPlayerInNumber() {
        return sudFSMMGCache.getPlayerInNumber();
    }

    // Whether it is a numerical bomb
    public boolean isHitBomb() {
        return sudFSMMGCache.isHitBomb();
    }

    // Destroy game
    public void destroyMG() {
        sudFSMMGCache.destroyMG();
    }

    /**
     * Return the current state of the game as a numerical value, which is defined by the MGCommonGameState enumeration
     */
    public int getGameState() {
        return sudFSMMGCache.getGameState();
    }

    /** Get the cached state */
    public SudFSMMGCache getSudFSMMGCache() {
        return sudFSMMGCache;
    }

}
