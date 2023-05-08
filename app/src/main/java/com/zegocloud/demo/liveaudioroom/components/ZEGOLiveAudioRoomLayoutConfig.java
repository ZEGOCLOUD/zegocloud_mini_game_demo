package com.zegocloud.demo.liveaudioroom.components;

import java.util.Arrays;
import java.util.List;

public class ZEGOLiveAudioRoomLayoutConfig {

    public List<ZEGOLiveAudioRoomLayoutRowConfig> rowConfigs = Arrays.asList(
        new ZEGOLiveAudioRoomLayoutRowConfig(4, ZEGOLiveAudioRoomLayoutAlignment.SPACE_AROUND),
        new ZEGOLiveAudioRoomLayoutRowConfig(4, ZEGOLiveAudioRoomLayoutAlignment.SPACE_AROUND));
    public int rowSpacing = 0;
}
