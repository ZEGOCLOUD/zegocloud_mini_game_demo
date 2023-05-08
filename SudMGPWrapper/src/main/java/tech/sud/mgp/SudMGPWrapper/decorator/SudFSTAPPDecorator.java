/*
 * Copyright © Sud.Tech
 * https://sud.tech
 */

package tech.sud.mgp.SudMGPWrapper.decorator;

import java.nio.ByteBuffer;
import java.util.List;

import tech.sud.mgp.SudMGPWrapper.state.SudMGPAPPState;
import tech.sud.mgp.SudMGPWrapper.utils.SudJsonUtils;
import tech.sud.mgp.core.ISudFSTAPP;
import tech.sud.mgp.core.ISudListenerNotifyStateChange;

/**
 * A decorator class for ISudFSTAPP
 */
public class SudFSTAPPDecorator {

    /**
     * Interface for APP to call game functions
     */
    private ISudFSTAPP iSudFSTAPP;

    /**
     * To set the object for app to call the SDK
     *
     * @param iSudFSTAPP
     */
    public void setISudFSTAPP(ISudFSTAPP iSudFSTAPP) {
        this.iSudFSTAPP = iSudFSTAPP;
    }


    /**
     * Send
     * 1. Join game status
     *
     * @param isIn         true to join the game, false to exit the game
     * @param seatIndex    seatIndex the seat index to join the game, starting from 0, or -1 for random seat assignment
     * @param isSeatRandom whether to randomly assign an empty seat if the specified seat is already occupied, true for random seat assignment, false otherwise
     * @param teamId       for games that do not support team division, enter 1; for games that support team division, enter 1 or 2 (for two teams)
     */
    public void notifyAPPCommonSelfIn(boolean isIn, int seatIndex, boolean isSeatRandom, int teamId) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonSelfIn state = new SudMGPAPPState.APPCommonSelfIn();
            state.isIn = isIn;
            state.seatIndex = seatIndex;
            state.isSeatRandom = isSeatRandom;
            state.teamId = teamId;
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_IN, SudJsonUtils.toJson(state), null);
        }
    }

    /**
     * Send
     * 2. Ready status
     * User (self) ready/cancel ready
     *
     * @param isReady true for ready, false for cancel ready
     */
    public void notifyAPPCommonSelfReady(boolean isReady) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonSelfReady state = new SudMGPAPPState.APPCommonSelfReady();
            state.isReady = isReady;
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_READY, SudJsonUtils.toJson(state), null);
        }
    }

    /**
     * Sends
     * 3. Game State
     *
     * @param isPlaying            true to start the game, false to end the game
     * @param reportGameInfoExtras String type, Https service callback report_game_info parameter, maximum length 1024 bytes, truncated if exceeded (2022-01-21)
     */
    public void notifyAPPCommonSelfPlaying(boolean isPlaying, String reportGameInfoExtras, String reportGameInfoKey) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonSelfPlaying state = new SudMGPAPPState.APPCommonSelfPlaying();
            state.isPlaying = isPlaying;
            state.reportGameInfoExtras = reportGameInfoExtras;
            state.reportGameInfoKey = reportGameInfoKey;
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_PLAYING, SudJsonUtils.toJson(state), null);
        }
    }

    /**
     * Send
     * 4. Captain status
     * The user can be the captain of the game and has the right to start the game.
     * After sending this state, the captain's identity will be transferred to another user.
     * Note: This is only effective when sent by the captain. You can get the current captain's id with {@link SudFSMMGDecorator#getCaptainUserId()}
     *
     * @param curCaptainUID Required, specifies the UID of the new captain.
     */
    public void notifyAPPCommonSelfCaptain(String curCaptainUID) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonSelfCaptain state = new SudMGPAPPState.APPCommonSelfCaptain();
            state.curCaptainUID = curCaptainUID;
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_CAPTAIN, SudJsonUtils.toJson(state), null);
        }
    }

    /**
     * Send
     * 5. Kick-out state
     * A user (self or captain) kicks out another player.
     * Only the captain can kick out players.
     *
     * @param kickedUID The UID of the user being kicked out
     */
    public void notifyAPPCommonSelfKick(String kickedUID) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonSelfKick state = new SudMGPAPPState.APPCommonSelfKick();
            state.kickedUID = kickedUID;
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_KICK, SudJsonUtils.toJson(state), null);
        }
    }

    /**
     * Send
     * 6. End game
     * The user (self, captain) ends the (current round) game.
     * Note: Only the captain can end the game. You can get the current captain id through {@link SudFSMMGDecorator#getCaptainUserId()}.
     */
    public void notifyAPPCommonSelfEnd() {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonSelfEnd state = new SudMGPAPPState.APPCommonSelfEnd();
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_END, SudJsonUtils.toJson(state), null);
        }
    }

    /**
     * Send
     * 9. Microphone status
     * User's (own) microphone status. It is recommended to:
     * Send an initial notification after entering the room;
     * Send a notification every time the status changes (mute/unmute/disable/enable).
     *
     * @param isOn       true to turn on the microphone, false to mute it
     * @param isDisabled true if the microphone is disabled, false if it is enabled
     */
    public void notifyAPPCommonSelfMicrophone(boolean isOn, boolean isDisabled) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonSelfMicrophone state = new SudMGPAPPState.APPCommonSelfMicrophone();
            state.isOn = isOn;
            state.isDisabled = isDisabled;
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_MICROPHONE, SudJsonUtils.toJson(state), null);
        }
    }

    /**
    * Send
    * 10. Text hit status
    * User's (self) chat message hit keyword status, recommended:
    * Precise matching;
    * After the first chat content hits the keyword, the subsequent chat content will not be flipped to unmatched;
    * Until the keyword is updated on the game side, then flip the status to unmatched;
    *
    * @param isHit          true if hit, false if not hit
    * @param keyWord        single keyword, compatible with old versions
    * @param text           return transcription text
    * @param wordType       text: text contains match; number: number equals match
    * @param keyWordList    hit keywords, can contain multiple keywords
    * @param numberList     in number mode only, return multiple numbers of transcription
    */
    public void notifyAPPCommonSelfTextHitState(boolean isHit, String keyWord, String text,
                                                String wordType, List<String> keyWordList, List<Integer> numberList) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonSelfTextHitState state = new SudMGPAPPState.APPCommonSelfTextHitState();
            state.isHit = isHit;
            state.keyWord = keyWord;
            state.text = text;
            state.wordType = wordType;
            state.keyWordList = keyWordList;
            state.numberList = numberList;
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_SELF_TEXT_HIT, SudJsonUtils.toJson(state), null);
        }
    }

    /**
     * Send
     * 11. Turn on or off background music (added on 2021-12-27)
     *
     * @param isOpen true to turn on background music, false to turn off background music
     */
    public void notifyAPPCommonOpenBgMusic(boolean isOpen) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonOpenBgMusic state = new SudMGPAPPState.APPCommonOpenBgMusic();
            state.isOpen = isOpen;
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_OPEN_BG_MUSIC, SudJsonUtils.toJson(state), null);
        }
    }

    /**
     * Send
     * 12. Turn on or off sound effects (added on 2021-12-27)
     *
     * @param isOpen true to turn on sound effects, false to turn off sound effects
     */
    public void notifyAPPCommonOpenSound(boolean isOpen) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonOpenSound state = new SudMGPAPPState.APPCommonOpenSound();
            state.isOpen = isOpen;
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_OPEN_SOUND, SudJsonUtils.toJson(state), null);
        }
    }

    /**
     * Send
     * 13. Turn on or off vibration effect in the game (added on 2021-12-27)
     *
     * @param isOpen true to turn on vibration effect, false to turn off vibration effect
     */
    public void notifyAPPCommonOpenVibrate(boolean isOpen) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonOpenVibrate state = new SudMGPAPPState.APPCommonOpenVibrate();
            state.isOpen = isOpen;
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_OPEN_VIBRATE, SudJsonUtils.toJson(state), null);
        }
    }

    /**
     * Send
     * 14. Set the volume of the game (added on 2021-12-31)
     *
     * @param volume The volume size, from 0 to 100
     */
    public void notifyAPPCommonGameSoundVolume(int volume) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonGameSoundVolume state = new SudMGPAPPState.APPCommonGameSoundVolume();
            state.volume = volume;
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_GAME_SOUND_VOLUME, SudJsonUtils.toJson(state), null);
        }
    }

    /**
     * Send
     * 15. Set game mode options (added on 2022-05-10)
     *
     * @param ludo Ludo game
     */
    public void notifyAPPCommonGameSettingSelectInfo(SudMGPAPPState.Ludo ludo) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonGameSettingSelectInfo state = new SudMGPAPPState.APPCommonGameSettingSelectInfo();
            state.ludo = ludo;
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_GAME_SETTING_SELECT_INFO, SudJsonUtils.toJson(state), null);
        }
    }

    /**
     * Send
     * 16. Set AI players in the game (added on 2022-05-11)
     * Only valid when set by the team leader
     *
     * @param aiPlayers AI players
     * @param isReady   Whether the robot automatically prepares after joining 1: automatic preparation, 0: no automatic preparation, default is 1
     */
    public void notifyAPPCommonGameAddAIPlayers(List<SudMGPAPPState.AIPlayers> aiPlayers, int isReady) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonGameAddAIPlayers state = new SudMGPAPPState.APPCommonGameAddAIPlayers();
            state.aiPlayers = aiPlayers;
            state.isReady = isReady;
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_GAME_ADD_AI_PLAYERS, SudJsonUtils.toJson(state), null);
        }
    }

    /**
     * Send
     * 17. Notify the game to retry connection after the app receives a disconnection notification (added on 2022-06-21, temporarily supporting ludo)
     */
    public void notifyAPPCommonGameReconnect() {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonGameReconnect state = new SudMGPAPPState.APPCommonGameReconnect();
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_GAME_RECONNECT, SudJsonUtils.toJson(state), null);
        }
    }

    /**
     * Send
     * 18. Return the player's current score
     */
    public void notifyAPPCommonGameScore(long score) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.APPCommonGameScore state = new SudMGPAPPState.APPCommonGameScore();
            state.score = score;
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_GAME_SCORE, SudJsonUtils.toJson(state), null);
        }
    }
    // endregion Status Notification, ISudFSTAPP.notifyStateChange

    // region Lifecycle
    public void startMG() {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.startMG();
        }
    }

    public void pauseMG() {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.pauseMG();
        }
    }

    public void playMG() {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.playMG();
        }
    }

    public void stopMG() {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.stopMG();
        }
    }

    public void destroyMG() {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.destroyMG();
        }
    }

    // endregion Lifecycle

    /**
     * update code
     *
     * @param code
     * @param listener
     */
    public void updateCode(String code, ISudListenerNotifyStateChange listener) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.updateCode(code, listener);
        }
    }

    /**
     * Audio stream data
     */
    public void pushAudio(ByteBuffer buffer, int bufferLength) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.pushAudio(buffer, bufferLength);
        }
    }

    /**
     * Send
     * 1. Settings for the Disco Dance in the Metaverse
     *
     * @param actionId Required parameter used to specify the type of action number. Different numbers are used to distinguish between different functions in the game. If not provided, it will be judged as an invalid command. See the table below for specific function numbers.
     * @param cooldown Duration in seconds. For some functions that require a duration, the corresponding value needs to be passed. If not passed or passed incorrectly, it will be processed according to the default value of each function (see the table below).
     * @param isTop    Whether to be on top, for some functions can queue to the top (false: not on top; true: on top; default is false)
     * @param field1   Additional parameter 1, which has specific meanings for some functions
     * @param field2   Additional parameter 2, which has specific meanings for some functions
     */
    public void notifyAppCommonGameDiscoAction(int actionId, Integer cooldown, Boolean isTop, String field1, String field2) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            SudMGPAPPState.AppCommonGameDiscoAction state = new SudMGPAPPState.AppCommonGameDiscoAction();
            state.actionId = actionId;
            state.cooldown = cooldown;
            state.isTop = isTop;
            state.field1 = field1;
            state.field2 = field2;
            iSudFSTAPP.notifyStateChange(SudMGPAPPState.APP_COMMON_GAME_DISCO_ACTION, SudJsonUtils.toJson(state), null);
        }
    }

    /**
     * Notify app state to the mini game.
     *
     * @param state    the state identifier.
     * @param dataJson the data in JSON format.
     * @param listener  the callback listener.
     */
    public void notifyStateChange(String state, String dataJson, ISudListenerNotifyStateChange listener) {
        ISudFSTAPP iSudFSTAPP = this.iSudFSTAPP;
        if (iSudFSTAPP != null) {
            iSudFSTAPP.notifyStateChange(state, dataJson, listener);
        }
    }

    /**
     * Notifies the mini-game of the APP state.
     *
     * @param state    the state identifier
     * @param dataJson the data associated with the state
     */
    public void notifyStateChange(String state, String dataJson) {
        notifyStateChange(state, dataJson, null);
    }

    /**
     * Notifies the mini-game of the APP state.
     *
     * @param state the state identifier
     * @param obj   the data associated with the state
     */
    public void notifyStateChange(String state, Object obj) {
        notifyStateChange(state, SudJsonUtils.toJson(obj), null);
    }

}
