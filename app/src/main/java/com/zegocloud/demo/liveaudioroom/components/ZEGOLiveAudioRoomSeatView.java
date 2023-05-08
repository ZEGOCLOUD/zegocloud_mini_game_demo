package com.zegocloud.demo.liveaudioroom.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.flexbox.FlexboxLayout;
import com.zegocloud.demo.liveaudioroom.R;
import com.zegocloud.demo.liveaudioroom.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOCLOUDUser;
import com.zegocloud.demo.liveaudioroom.utils.Utils;

public class ZEGOLiveAudioRoomSeatView extends FrameLayout {

    private TextView textView;
    private ImageView seatIcon;
    private ImageView userCustomAvatarView;
    private FrameLayout userDefaultAvatarView;
    private ImageFilterView imageFilterView;
    private TextView nameTextView;

    public ZEGOLiveAudioRoomSeatView(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZEGOLiveAudioRoomSeatView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZEGOLiveAudioRoomSeatView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int width = Utils.dp2px(80, displayMetrics);
        int height = Utils.dp2px(80, displayMetrics);
        FlexboxLayout.LayoutParams flexChildParams = new FlexboxLayout.LayoutParams(width, height);
        setLayoutParams(flexChildParams);

        seatIcon = new ImageView(getContext());
        seatIcon.setBackgroundResource(R.drawable.audioroom_icon_seat);
        addView(seatIcon);

        userDefaultAvatarView = new FrameLayout(getContext());
        addView(userDefaultAvatarView);

        imageFilterView = new ImageFilterView(getContext());
        imageFilterView.setRoundPercent(1.0f);
        userDefaultAvatarView.addView(imageFilterView);

        textView = new TextView(getContext());
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        textView.setTextColor(Color.parseColor("#222222"));
        userDefaultAvatarView.addView(textView);

        userCustomAvatarView = new ImageView(getContext());
        addView(userCustomAvatarView);

        nameTextView = new TextView(getContext());
        nameTextView.setSingleLine();
        nameTextView.setEllipsize(TruncateAt.END);
        addView(nameTextView);

        onEnterNormalMode();
    }

    public void onEnterGameMode() {
        float scaleFactor = 0.8f;

        FlexboxLayout.LayoutParams flexChildParams = (FlexboxLayout.LayoutParams) getLayoutParams();
        flexChildParams.width = (int) (flexChildParams.width * scaleFactor);
        flexChildParams.height = (int) (flexChildParams.height * scaleFactor);

        LayoutParams params = (LayoutParams) seatIcon.getLayoutParams();
        params.width = (int) (params.width * scaleFactor);
        params.height = (int) (params.height * scaleFactor);
        seatIcon.setLayoutParams(params);

        LayoutParams avatarParams = (LayoutParams) userDefaultAvatarView.getLayoutParams();
        avatarParams.width = (int) (avatarParams.width * scaleFactor);
        avatarParams.height = (int) (avatarParams.height * scaleFactor);
        userDefaultAvatarView.setLayoutParams(avatarParams);
        userCustomAvatarView.setLayoutParams(avatarParams);

        nameTextView.setTextColor(Color.WHITE);
        nameTextView.setTextSize(nameTextView.getTextSize() / 2);
        LayoutParams nameParams = (LayoutParams) nameTextView.getLayoutParams();
        nameParams.bottomMargin = (int) (nameParams.bottomMargin * scaleFactor);
        nameTextView.setLayoutParams(nameParams);
    }

    public void onEnterNormalMode() {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int width = Utils.dp2px(80, displayMetrics);
        int height = Utils.dp2px(80, displayMetrics);
        FlexboxLayout.LayoutParams flexChildParams = new FlexboxLayout.LayoutParams(width, height);
        setLayoutParams(flexChildParams);

        LayoutParams params = new LayoutParams(Utils.dp2px(54, displayMetrics), Utils.dp2px(54, displayMetrics));
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        seatIcon.setLayoutParams(params);

        LayoutParams avatarParams = new LayoutParams(Utils.dp2px(54, displayMetrics), Utils.dp2px(54, displayMetrics));
        avatarParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        userDefaultAvatarView.setLayoutParams(avatarParams);
        userCustomAvatarView.setLayoutParams(avatarParams);

        nameTextView.setTextColor(Color.BLACK);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        LayoutParams nameParams = new LayoutParams(-2, -2);
        nameParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        nameParams.bottomMargin = Utils.dp2px(4, displayMetrics);
        nameTextView.setLayoutParams(nameParams);
    }

    private static final String TAG = "ZEGOLiveAudioRoomSeatVi";

    public void onUserUpdate(ZEGOCLOUDUser zegocloudUser) {
        Log.d(TAG, "onUserUpdate() called with: zegocloudUser = [" + zegocloudUser + "]");
        if (zegocloudUser != null) {
            addUserToSeat(zegocloudUser);
        } else {
            removeUserFromSeat();
        }
    }

    private void removeUserFromSeat() {
        imageFilterView.setImageDrawable(null);
        textView.setText("");
        nameTextView.setText("");
        userCustomAvatarView.setImageDrawable(null);
    }

    private void addUserToSeat(ZEGOCLOUDUser zegocloudUser) {
        imageFilterView.setImageDrawable(new ColorDrawable(Color.parseColor("#DBDDE3")));
        textView.setText(String.valueOf(zegocloudUser.userName.toUpperCase().charAt(0)));
        nameTextView.setText(zegocloudUser.userName);
        String userAvatar = ZEGOLiveAudioRoomManager.getInstance().getUserAvatar(zegocloudUser.userID);
        onUserAvatarUpdated(userAvatar);
    }

    public void onLockChanged(boolean lock) {
        if (lock) {
            seatIcon.setBackgroundResource(R.drawable.audioroom_icon_lock_seat);
        } else {
            seatIcon.setBackgroundResource(R.drawable.audioroom_icon_seat);
        }
    }

    public void onUserAvatarUpdated(String url) {
        if (TextUtils.isEmpty(url)) {
            userCustomAvatarView.setImageDrawable(null);
        } else {
            RequestOptions requestOptions = new RequestOptions().circleCrop();
            Glide.with(getContext()).load(url).apply(requestOptions).into(userCustomAvatarView);
        }
    }
}
