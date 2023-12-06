package com.project.sc2006.controllers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.sc2006.R;
import com.project.sc2006.entities.PartyHistory;

import java.util.List;

public class PartyHistoryViewHolder extends RecyclerView.ViewHolder {
    TextView tt1;
    TextView tt2;

    public PartyHistoryViewHolder(@NonNull View partyView) {
        super(partyView);
        this.tt1 = (TextView) partyView.findViewById(R.id.party_line1);
        this.tt2 = (TextView) partyView.findViewById(R.id.party_line2);
    }
}
