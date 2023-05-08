package com.zegocloud.demo.liveaudioroom.components;

import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOCLOUDUser;
import java.util.Objects;

public class ZEGOLiveAudioRoomSeat {

    public int seatIndex = 0;
    public int rowIndex = 0;
    public int columnIndex = 0;
    private ZEGOCLOUDUser lastUser;
    private ZEGOCLOUDUser currentUser;

    public boolean isNotEmpty() {
        return currentUser != null;
    }

    public boolean isEmpty() {
        return currentUser == null;
    }

    public boolean isSeatChanged() {
        return !Objects.equals(currentUser, lastUser);
    }

    public boolean isTakenByUser(ZEGOCLOUDUser user) {
        return user.equals(currentUser);
    }

    public ZEGOCLOUDUser getUser() {
        return currentUser;
    }

    public ZEGOCLOUDUser getLastUser() {
        return lastUser;
    }

    public void setUser(ZEGOCLOUDUser user) {
        lastUser = currentUser;
        currentUser = user;
    }

    @Override
    public String toString() {
        return "ZEGOLiveAudioRoomSeat{" +
            "seatIndex=" + seatIndex +
            ", rowIndex=" + rowIndex +
            ", columnIndex=" + columnIndex +
            ", lastUser=" + lastUser +
            ", currentUser=" + currentUser +
            '}';
    }
}
