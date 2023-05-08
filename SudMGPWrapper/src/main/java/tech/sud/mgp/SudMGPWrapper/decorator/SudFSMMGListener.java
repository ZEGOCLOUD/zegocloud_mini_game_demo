/*
 * Copyright © Sud.Tech
 * https://sud.tech
 */

package tech.sud.mgp.SudMGPWrapper.decorator;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import tech.sud.mgp.SudMGPWrapper.state.SudMGPMGState;
import tech.sud.mgp.SudMGPWrapper.utils.ISudFSMStateHandleUtils;
import tech.sud.mgp.core.ISudFSMStateHandle;

/**
 * {@link SudFSMMGDecorator} Callback definition
 */
public interface SudFSMMGListener {

    /**
     * Game log
     * Minimum version：v1.1.30.xx
     */
    default void onGameLog(String str) {
    }

    /**
     * Game loading progress
     *
     * @param stage    Stage：start=1,loading=2,end=3
     * @param retCode  Error code: 0 for success
     * @param progress Progress：[0, 100]
     */
    default void onGameLoadingProgress(int stage, int retCode, int progress) {
    }

    /**
     * Game start, implementation required
     * Minimum version：v1.1.30.xx
     */
    void onGameStarted();

    /**
     * Game destruction, implementation required
     * Minimum version：v1.1.30.xx
     */
    void onGameDestroyed();

    /**
     * Code expired, implementation required
     * APP接入方需要调用handle.success或handle.fail
     *
     * @param dataJson {"code":"value"}
     */
    void onExpireCode(ISudFSMStateHandle handle, String dataJson);

    /**
     * Get game view information, implementation required
     * The APP access party needs to call handle.success or handle.fail
     *
     * @param handle
     * @param dataJson {}
     */
    void onGetGameViewInfo(ISudFSMStateHandle handle, String dataJson);

    /**
     * Get game Config, implementation required
     * The APP access party needs to call handle.success or handle.fail
     *
     * @param handle
     * @param dataJson {}
     *                 Minimum version：v1.1.30.xx
     */
    void onGetGameCfg(ISudFSMStateHandle handle, String dataJson);


    /**
     * 1.Game public chat message
     * mg_common_public_message
     */
    default void onGameMGCommonPublicMessage(ISudFSMStateHandle handle, SudMGPMGState.MGCommonPublicMessage model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 2. key word status
     * mg_common_key_word_to_hit
     */
    default void onGameMGCommonKeyWordToHit(ISudFSMStateHandle handle, SudMGPMGState.MGCommonKeyWordToHit model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 3. game settle status
     * mg_common_game_settle
     */
    default void onGameMGCommonGameSettle(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameSettle model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 4. Join game button click status
     * mg_common_self_click_join_btn
     */
    default void onGameMGCommonSelfClickJoinBtn(ISudFSMStateHandle handle, SudMGPMGState.MGCommonSelfClickJoinBtn model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 5. Cancel join (exit) game button click status
     * mg_common_self_click_cancel_join_btn
     */
    default void onGameMGCommonSelfClickCancelJoinBtn(ISudFSMStateHandle handle, SudMGPMGState.MGCommonSelfClickCancelJoinBtn model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 6. Ready button click status
     * mg_common_self_click_ready_btn
     */
    default void onGameMGCommonSelfClickReadyBtn(ISudFSMStateHandle handle, SudMGPMGState.MGCommonSelfClickReadyBtn model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 7. Cancel ready button click status
     * mg_common_self_click_cancel_ready_btn
     */
    default void onGameMGCommonSelfClickCancelReadyBtn(ISudFSMStateHandle handle, SudMGPMGState.MGCommonSelfClickCancelReadyBtn model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 8. Start game button click status
     * mg_common_self_click_start_btn
     */
    default void onGameMGCommonSelfClickStartBtn(ISudFSMStateHandle handle, SudMGPMGState.MGCommonSelfClickStartBtn model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 9. Share button click status
     * mg_common_self_click_share_btn
     */
    default void onGameMGCommonSelfClickShareBtn(ISudFSMStateHandle handle, SudMGPMGState.MGCommonSelfClickShareBtn model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 10. game status
     * mg_common_game_state
     */
    default void onGameMGCommonGameState(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameState model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 11. Settlement interface close button click status (added on 2021-12-27)
     * mg_common_self_click_game_settle_close_btn
     */
    default void onGameMGCommonSelfClickGameSettleCloseBtn(ISudFSMStateHandle handle, SudMGPMGState.MGCommonSelfClickGameSettleCloseBtn model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 12. Settlement interface 'play again' button click status (added on 2021-12-27)
     * mg_common_self_click_game_settle_again_btn
     */
    default void onGameMGCommonSelfClickGameSettleAgainBtn(ISudFSMStateHandle handle, SudMGPMGState.MGCommonSelfClickGameSettleAgainBtn model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 13. Game reports the list of sounds in the game (added on 2021-12-30, currently only supported by PongPong Wo Zui Qiang)
     * mg_common_game_sound_list
     */
    default void onGameMGCommonGameSoundList(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameSoundList model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 14. The game notifies the app layer to play sounds (added on 2021-12-30, currently only supported by PongPong Wo Zui Qiang)
     * mg_common_game_sound
     */
    default void onGameMGCommonGameSound(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameSound model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 15. The game notifies the app layer of the background music playback status (added on 2022-01-07, currently only supported by PongPong Wo Zui Qiang)
     * mg_common_game_bg_music_state
     */
    default void onGameMGCommonGameBgMusicState(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameBgMusicState model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 16. The game notifies the app layer of the sound effects playback status (added on 2022-01-07, currently only supported by PongPong Wo Zui Qiang)
     * mg_common_game_sound_state
     */
    default void onGameMGCommonGameSoundState(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameSoundState model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 17. ASR status (Enable and disable speech recognition status, added in version v1.1.45.xx)
     * mg_common_game_asr
     */
    default void onGameMGCommonGameASR(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameASR model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 18. Microphone status (added on 2022-02-08)
     * mg_common_self_microphone
     */
    default void onGameMGCommonSelfMicrophone(ISudFSMStateHandle handle, SudMGPMGState.MGCommonSelfMicrophone model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 19. Headphone (earpiece, speaker) status (added on 2022-02-08)
     * mg_common_self_headphone
     */
    default void onGameMGCommonSelfHeadphone(ISudFSMStateHandle handle, SudMGPMGState.MGCommonSelfHeadphone model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 20. Error codes for app common status operation results (added on 2022-05-10)
     * mg_common_app_common_self_x_resp
     */
    default void onGameMGCommonAPPCommonSelfXResp(ISudFSMStateHandle handle, SudMGPMGState.MGCommonAPPCommonSelfXResp model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 21. Game notifies app layer whether adding a game companion robot is successful (added on 2022-05-17)
     * mg_common_game_add_ai_players
     */
    default void onGameMGCommonGameAddAIPlayers(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameAddAIPlayers model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 22. in Chinese means "Game notifies app layer of current network connection status (added on 2022-06-21)
     * mg_common_game_network_state
     */
    default void onGameMGCommonGameNetworkState(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameNetworkState model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 23. Game notifies app to get points
     * mg_common_game_score
     */
    default void onGameMGCommonGameGetScore(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameGetScore model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 24. Game notifies app to bring in points
     * mg_common_game_set_score
     */
    default void onGameMGCommonGameSetScore(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameSetScore model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 25. Create order
     * mg_common_game_create_order
     */
    default void onGameMGCommonGameCreateOrder(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameCreateOrder model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 26. Game notifies app of player roles (only applicable to Werewolf game)
     * mg_common_player_role_id
     */
    default void onGameMGCommonPlayerRoleId(ISudFSMStateHandle handle, SudMGPMGState.MGCommonPlayerRoleId model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }



    /**
     * 1.Join status (already modified)
     * mg_common_player_in
     */
    default void onPlayerMGCommonPlayerIn(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonPlayerIn model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 2.Ready status (already modified)
     * mg_common_player_ready
     */
    default void onPlayerMGCommonPlayerReady(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonPlayerReady model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 3.Captain status (already modified)
     * mg_common_player_captain
     */
    default void onPlayerMGCommonPlayerCaptain(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonPlayerCaptain model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 4.Game status (already modified)
     * mg_common_player_playing
     */
    default void onPlayerMGCommonPlayerPlaying(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonPlayerPlaying model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 5.Player online status
     * mg_common_player_online
     */
    default void onPlayerMGCommonPlayerOnline(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonPlayerOnline model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 6.Player switch game position status
     * mg_common_player_change_seat
     */
    default void onPlayerMGCommonPlayerChangeSeat(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonPlayerChangeSeat model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 7. Game notifies app of player avatar click
     * mg_common_self_click_game_player_icon
     */
    default void onPlayerMGCommonSelfClickGamePlayerIcon(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonSelfClickGamePlayerIcon model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 8. Game notifies app of player death
     * mg_common_self_die_status
     */
    default void onPlayerMGCommonSelfDieStatus(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonSelfDieStatus model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 9. Game notifies app of player's turn status (added on 2022-04-24)
     * mg_common_self_turn_status
     */
    default void onPlayerMGCommonSelfTurnStatus(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonSelfTurnStatus model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 10. Game notifies app of player's selection status (added on 2022-04-24)
     * mg_common_self_select_status
     */
    default void onPlayerMGCommonSelfSelectStatus(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonSelfSelectStatus model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 11. Game notifies app layer of current remaining game time (added on 2022-05-23, currently effective in UMO)
     * mg_common_game_countdown_time
     */
    default void onPlayerMGCommonGameCountdownTime(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonGameCountdownTime model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 12. Game notifies app layer that the current player becomes an observer after death (added on 2022-08-23, currently effective in Werewolf game)
     * mg_common_self_ob_status
     */
    default void onPlayerMGCommonSelfObStatus(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonSelfObStatus model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    // endregion Game callback app player status


    /**
     * 1. Status in word selection
     * mg_dg_selecting
     */
    default void onPlayerMGDGSelecting(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGDGSelecting model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 2. Status during drawing (already modified)
     * mg_dg_painting
     */
    default void onPlayerMGDGPainting(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGDGPainting model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 3. Display incorrect answer status (already modified)
     * mg_dg_erroranswer
     */
    default void onPlayerMGDGErroranswer(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGDGErroranswer model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 4. Display total points status (already modified)
     * mg_dg_totalscore
     */
    default void onPlayerMGDGTotalscore(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGDGTotalscore model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 5. Status of points earned in this session (already modified)
     * mg_dg_score
     */
    default void onPlayerMGDGScore(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGDGScore model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }


    /**
     * 1. Sand Dance command callback in the Metaverse
     * mg_common_game_disco_action
     */
    default void onGameMGCommonGameDiscoAction(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameDiscoAction model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 2. Notification of completion of Sand Dance command in the Metaverse
     * mg_common_game_disco_action_end
     */
    default void onGameMGCommonGameDiscoActionEnd(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameDiscoActionEnd model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 1. Gift configuration file (Rocket)
     * mg_custom_rocket_config
     */
    default void onGameMGCustomRocketConfig(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketConfig model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 2. List of owned models (Rocket)
     * mg_custom_rocket_model_list
     */
    default void onGameMGCustomRocketModelList(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketModelList model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 3. List of owned components (Rocket)
     * mg_custom_rocket_component_list
     */
    default void onGameMGCustomRocketComponentList(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketComponentList model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 4. Get user information (rocket)
     * mg_custom_rocket_user_info
     */
    default void onGameMGCustomRocketUserInfo(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketUserInfo model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 5. Order record list (rocket)
     * mg_custom_rocket_order_record_list
     */
    default void onGameMGCustomRocketOrderRecordList(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketOrderRecordList model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 6. List inside the exhibition hall (rocket)
     * mg_custom_rocket_room_record_list
     */
    default void onGameMGCustomRocketRoomRecordList(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketRoomRecordList model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 7. Player gift giving record inside the exhibition hall (rocket)
     * mg_custom_rocket_user_record_list
     */
    default void onGameMGCustomRocketUserRecordList(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketUserRecordList model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 8. Set default model (rocket)
     * mg_custom_rocket_set_default_model
     */
    default void onGameMGCustomRocketSetDefaultModel(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketSetDefaultModel model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 9. Dynamically calculate one-click send price (rocket)
     * mg_custom_rocket_dynamic_fire_price
     */
    default void onGameMGCustomRocketDynamicFirePrice(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketDynamicFirePrice model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 10. List of exhibits in the venue (rocket)
     * mg_custom_rocket_fire_model
     */
    default void onGameMGCustomRocketFireModel(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketFireModel model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 11. Newly assembled model (rocket)
     * mg_custom_rocket_create_model
     */
    default void onGameMGCustomRocketCreateModel(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketCreateModel model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 12. Model component replacement (rocket)
     * mg_custom_rocket_replace_component
     */
    default void onGameMGCustomRocketReplaceComponent(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketReplaceComponent model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 13. Purchase components (rocket)
     * mg_custom_rocket_buy_component
     */
    default void onGameMGCustomRocketBuyComponent(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketBuyComponent model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 14. Start playback effect (Rocket)
     * mg_custom_rocket_play_effect_start
     */
    default void onGameMGCustomRocketPlayEffectStart(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketPlayEffectStart model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 15. Play effect completed (rocket)
     * mg_custom_rocket_play_effect_finish
     */
    default void onGameMGCustomRocketPlayEffectFinish(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketPlayEffectFinish model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 16. Verify signature compliance (rocket)
     * mg_custom_rocket_verify_sign
     */
    default void onGameMGCustomRocketVerifySign(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketVerifySign model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 17. Upload icon (rocket)
     * mg_custom_rocket_upload_model_icon
     */
    default void onGameMGCustomRocketUploadModelIcon(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketUploadModelIcon model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 18. Preparations completed (rocket)
     * mg_custom_rocket_prepare_finish
     */
    default void onGameMGCustomRocketPrepareFinish(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketPrepareFinish model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 19. Rocket main interface has been displayed (rocket)
     * mg_custom_rocket_show_game_scene
     */
    default void onGameMGCustomRocketShowGameScene(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketShowGameScene model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 20. Rocket main interface has been hidden (rocket)
     * mg_custom_rocket_hide_game_scene
     */
    default void onGameMGCustomRocketHideGameScene(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketHideGameScene model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 21. Click to lock component (rocket)
     * mg_custom_rocket_click_lock_component
     */
    default void onGameMGCustomRocketClickLockComponent(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketClickLockComponent model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 22. Rocket effect fly click (rocket)
     * mg_custom_rocket_fly_click
     */
    default void onGameMGCustomRocketFlyClick(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketFlyClick model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 23. Rocket effect fly ended (rocket)
     * mg_custom_rocket_fly_end
     */
    default void onGameMGCustomRocketFlyEnd(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketFlyEnd model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 24. Set click area (rocket)
     * mg_custom_rocket_set_click_rect
     */
    default void onGameMGCustomRocketSetClickRect(ISudFSMStateHandle handle, SudMGPMGState.MGCustomRocketSetClickRect model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }


    /**
     * 1.  Query leaderboard data (baseball)
     * mg_baseball_ranking
     */
    default void onGameMGBaseballRanking(ISudFSMStateHandle handle, SudMGPMGState.MGBaseballRanking model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 2. Query my ranking (baseball)
     * mg_baseball_my_ranking
     */
    default void onGameMGBaseballMyRanking(ISudFSMStateHandle handle, SudMGPMGState.MGBaseballMyRanking model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 3. Query data of players before and after me (baseball)
     * mg_baseball_range_info
     */
    default void onGameMGBaseballRangeInfo(ISudFSMStateHandle handle, SudMGPMGState.MGBaseballRangeInfo model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 4. Set clickable area provided by app for game (baseball)
     * mg_baseball_set_click_rect
     */
    default void onGameMGBaseballSetClickRect(ISudFSMStateHandle handle, SudMGPMGState.MGBaseballSetClickRect model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 5. Preparations completed (baseball)
     * mg_baseball_prepare_finish
     */
    default void onGameMGBaseballPrepareFinish(ISudFSMStateHandle handle, SudMGPMGState.MGBaseballPrepareFinish model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 6.  Main interface has been displayed (baseball)
     * mg_baseball_show_game_scene
     */
    default void onGameMGBaseballShowGameScene(ISudFSMStateHandle handle, SudMGPMGState.MGBaseballShowGameScene model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 7. Main interface has been hidden (baseball)
     * mg_baseball_hide_game_scene
     */
    default void onGameMGBaseballHideGameScene(ISudFSMStateHandle handle, SudMGPMGState.MGBaseballHideGameScene model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

    /**
     * 8. Get text configuration data (baseball)
     * mg_baseball_text_config
     */
    default void onGameMGBaseballTextConfig(ISudFSMStateHandle handle, SudMGPMGState.MGBaseballTextConfig model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }


    /**
     * game status change
     *
     * @param handle   handle method
     * @param state    state 
     * @param dataJson state json
     * @return Return true means that this method takes over the processing of the state, so it is important to pay attention to the call:ISudFSMStateHandleUtils.handleSuccess(handle);
     */
    default boolean onGameStateChange(ISudFSMStateHandle handle, String state, String dataJson) {
        return false;
    }

    /**
     * Game player state change
     *
     * @param handle   callback operation
     * @param userId   user ID
     * @param state    state command
     * @param dataJson state value
     * @param Return true means that this method takes over the processing of the state, so it is important to pay attention to the call: ISudFSMStateHandleUtils.handleSuccess(handle);
     */
    default boolean onPlayerStateChange(ISudFSMStateHandle handle, String userId, String state, String dataJson) {
        return false;
    }

    default void onGamePlayerIconPositionUpdate(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGamePlayerIconPosition model) {
        ISudFSMStateHandleUtils.handleSuccess(handle);
    }

}
