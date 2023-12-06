package com.project.sc2006.controllers;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.sc2006.MainActivity;
import com.project.sc2006.PartyHistoryActivity;
import com.project.sc2006.R;
import com.project.sc2006.entities.PartyHistory;

import java.util.List;

public class PartyHistoryRecyclerAdapter extends RecyclerView.Adapter<PartyHistoryViewHolder> {
    private Context mContext;
    List<PartyHistory> partyList;
    String session;
    Activity runningActivity;

    public PartyHistoryRecyclerAdapter(Context context, List<PartyHistory> items, String session, Activity runningActivity) {
        this.partyList = items;
        this.mContext = context;
        this.session = session;
        this.runningActivity = runningActivity;
    }

    @NonNull
    @Override
    public PartyHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View partyView = LayoutInflater.from(parent.getContext()).inflate(R.layout.party_history_one_party, parent, false);
        return new PartyHistoryViewHolder(partyView);
    }

    @Override
    public void onBindViewHolder(@NonNull PartyHistoryViewHolder holder, int position) {
        holder.tt1.setText(partyList.get(position).getDestinationName());
        holder.tt2.setText(partyList.get(position).getArrivalTime());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PartyHistory item = partyList.get(position);
                ((PartyHistoryActivity)mContext).joinPartyFromHistory(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return partyList.size();
    }

    public void removeAt(int position) {
        PartyHistory partyToQuit = partyList.get(position);
        MainActivity.getPartyModeController().quitParty(session, String.valueOf(partyToQuit.getPartyID()), runningActivity);
        partyList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, partyList.size());
    }
}
