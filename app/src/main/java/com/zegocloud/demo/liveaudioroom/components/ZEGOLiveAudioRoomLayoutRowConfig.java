package com.zegocloud.demo.liveaudioroom.components;

public class ZEGOLiveAudioRoomLayoutRowConfig {

    public int count;
    public int seatSpacing;
    public ZEGOLiveAudioRoomLayoutAlignment alignment;

    public ZEGOLiveAudioRoomLayoutRowConfig(int count, ZEGOLiveAudioRoomLayoutAlignment alignment) {
        this.count = count;
        this.alignment = alignment;
    }

    public ZEGOLiveAudioRoomLayoutRowConfig(int count, int seatSpacing, ZEGOLiveAudioRoomLayoutAlignment alignment) {
        this.count = count;
        this.seatSpacing = seatSpacing;
        this.alignment = alignment;
    }

    public ZEGOLiveAudioRoomLayoutRowConfig() {
        this.alignment = ZEGOLiveAudioRoomLayoutAlignment.SPACE_AROUND;
    }
}
