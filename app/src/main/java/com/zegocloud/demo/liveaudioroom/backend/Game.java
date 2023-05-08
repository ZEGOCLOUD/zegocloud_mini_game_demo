package com.zegocloud.demo.liveaudioroom.backend;

public class Game {

    public String name;
    public int coin;
    public Long gameID;


    public Game(Long gameID, String name, int coin) {
        this.name = name;
        this.coin = coin;
        this.gameID = gameID;
    }
}
