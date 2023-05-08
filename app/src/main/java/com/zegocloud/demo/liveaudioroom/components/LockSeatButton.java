package com.zegocloud.demo.liveaudioroom.components;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.demo.liveaudioroom.R;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager.SeatLockChangeListener;
import com.zegocloud.demo.liveaudioroom.internal.components.ZEGOImageButton;

public class LockSeatButton extends ZEGOImageButton {

    public LockSeatButton(@NonNull Context context) {
        super(context);
    }

    public LockSeatButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LockSeatButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
        setImageResource(R.drawable.liveaudioroom_btn_lock_seat, R.drawable.liveaudioroom_btn_unlock_seat);

        ZEGOLiveAudioRoomManager.getInstance().addSeatLockChangeListener(new SeatLockChangeListener() {
            @Override
            public void onSeatLockChanged(boolean lock) {
                updateState(lock);
            }
        });
    }

    @Override
    public void open() {
        super.open();
        ZEGOLiveAudioRoomManager.getInstance().lockSeat(true);
    }

    @Override
    public void close() {
        super.close();
        ZEGOLiveAudioRoomManager.getInstance().lockSeat(false);
    }
}
