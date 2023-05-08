package com.zegocloud.demo.liveaudioroom.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.zegocloud.demo.liveaudioroom.R;
import com.zegocloud.demo.liveaudioroom.internal.ZEGOSDKManager;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.AcceptInvitationCallback;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.RejectInvitationCallback;
import com.zegocloud.demo.liveaudioroom.internal.invitation.common.ZEGOInvitation;
import com.zegocloud.demo.liveaudioroom.internal.rtc.ZEGOCLOUDUser;
import com.zegocloud.demo.liveaudioroom.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class TakeSeatRequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> userIDList = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_seatrequest, parent, false);
        int height = Utils.dp2px(70, parent.getContext().getResources().getDisplayMetrics());
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        return new ViewHolder(view) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView memberName = holder.itemView.findViewById(R.id.live_member_item_name);
        TextView agree = holder.itemView.findViewById(R.id.live_member_item_agree);
        TextView disagree = holder.itemView.findViewById(R.id.live_member_item_disagree);

        ZEGOCLOUDUser cloudUser = ZEGOSDKManager.getInstance().rtcService.getUser(userIDList.get(position));
        ZEGOInvitation invitation = ZEGOSDKManager.getInstance().zimService.getUserInvitation(cloudUser.userID);
        memberName.setText(cloudUser.userName);

        agree.setOnClickListener(v -> {
            ZEGOSDKManager.getInstance().zimService.acceptInvite(invitation, new AcceptInvitationCallback() {
                @Override
                public void onResult(int errorCode, String invitationID) {

                }
            });
        });

        disagree.setOnClickListener(v -> {
            ZEGOSDKManager.getInstance().zimService.rejectInvite(invitation, new RejectInvitationCallback() {
                @Override
                public void onResult(int errorCode, String invitationID) {

                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return userIDList.size();
    }

    public void addItem(String userID) {
        int position = userIDList.size();
        userIDList.add(userID);
        notifyItemInserted(position);
    }

    public void removeItem(String userID) {
        int position = userIDList.size();
        userIDList.remove(userID);
        notifyItemRemoved(position);
    }

    public void setItems(List<String> userIDList) {
        this.userIDList.clear();
        this.userIDList.addAll(userIDList);
        notifyDataSetChanged();
    }
}
