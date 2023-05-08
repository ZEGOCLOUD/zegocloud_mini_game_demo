/*
 * Copyright © Sud.Tech
 * https://sud.tech
 */

package tech.sud.mgp.SudMGPWrapper.decorator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import tech.sud.mgp.SudMGPWrapper.state.SudMGPMGState;

/**
 * Game callback data cache.
 */
public class SudFSMMGCache {

    private String captainUserId; // Record the user ID of the current team leader.
    private SudMGPMGState.MGCommonGameState mgCommonGameStateModel; // global game state
    private boolean isHitBomb = false; // Is it a number bomb
    private final HashSet<String> playerInSet = new HashSet<>(); // Record the players who have joined the game.
    private final HashSet<String> playerReadySet = new HashSet<>(); // Record the game players who are ready.
    private final HashMap<String, SudMGPMGState.MGCommonPlayerPlaying> playerPlayingMap = new HashMap<>(); // Record the game status of players

    // Handling of Captain's status.
    public void onPlayerMGCommonPlayerCaptain(String userId, SudMGPMGState.MGCommonPlayerCaptain model) {
        if (model != null) {
            if (model.isCaptain) {
                captainUserId = userId;
            } else {
                if (Objects.equals(captainUserId, userId)) {
                    captainUserId = null;
                }
            }
        }
    }

    // Processing game state.
    public void onGameMGCommonGameState(SudMGPMGState.MGCommonGameState model) {
        mgCommonGameStateModel = model;
    }

    // Player join status processing.
    public void onPlayerMGCommonPlayerIn(String userId, SudMGPMGState.MGCommonPlayerIn model) {
        if (model != null) {
            if (model.isIn) {
                playerInSet.add(userId);
            } else {
                playerInSet.remove(userId);
                playerReadySet.remove(userId);
            }
        }
    }

    // Player ready status
    public void onPlayerMGCommonPlayerReady(String userId, SudMGPMGState.MGCommonPlayerReady model) {
        if (model != null) {
            if (model.isReady) {
                playerReadySet.add(userId);
            } else {
                playerReadySet.remove(userId);
            }
        }
    }

    // player game status
    public void onPlayerMGCommonPlayerPlaying(String userId, SudMGPMGState.MGCommonPlayerPlaying model) {
        if (model != null) {
            playerPlayingMap.put(userId, model);
        }
    }

    // Keyword status.
    public void onGameMGCommonKeyWordToHit(SudMGPMGState.MGCommonKeyWordToHit model) {
        if (model != null) {
            isHitBomb = model.wordType.equals("number");
        }
    }

    // Return whether the player is currently in a game.
    public boolean playerIsPlaying(String userId) {
        SudMGPMGState.MGCommonPlayerPlaying mgCommonPlayerPlaying = playerPlayingMap.get(userId);
        if (mgCommonPlayerPlaying != null) {
            return mgCommonPlayerPlaying.isPlaying;
        }
        return false;
    }

    // Return whether the player is ready or not.
    public boolean playerIsReady(String userId) {
        return playerReadySet.contains(userId);
    }

    // Return whether the player has joined the game or not.
    public boolean playerIsIn(String userId) {
        return playerInSet.contains(userId);
    }

    // The number of people currently in the game.
    public int getPlayerInNumber() {
        return playerInSet.size();
    }

    // Is it a number bomb
    public boolean isHitBomb() {
        return isHitBomb;
    }

    // destroy the game
    public void destroyMG() {
        captainUserId = null;
        mgCommonGameStateModel = null;
        isHitBomb = false;
        playerInSet.clear();
        playerReadySet.clear();
        playerPlayingMap.clear();
    }

    /** Get the userId of the team leader. */
    public String getCaptainUserId() {
        return captainUserId;
    }

    /** Get the collection of players who have joined the game. */
    public HashSet<String> getPlayerInSet() {
        return new HashSet<>(playerInSet);
    }

    /** Get the collection of players who are currently ready. */
    public HashSet<String> getPlayerReadySet() {
        return new HashSet<>(playerReadySet);
    }

    /** "Get collection of player game states */
    public HashMap<String, SudMGPMGState.MGCommonPlayerPlaying> getPlayerPlayingMap() {
        return new HashMap<>(playerPlayingMap);
    }

    /**
     * Return the current state of the game, numeric parameter {@link SudMGPMGState.MGCommonGameState}.
     */
    public int getGameState() {
        if (mgCommonGameStateModel != null) {
            return mgCommonGameStateModel.gameState;
        }
        return SudMGPMGState.MGCommonGameState.UNKNOW;
    }

}
