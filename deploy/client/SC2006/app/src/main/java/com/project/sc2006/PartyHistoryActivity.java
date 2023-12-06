package com.project.sc2006;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListView;

import com.project.sc2006.controllers.DeleteButtonClickListener;
import com.project.sc2006.controllers.PartyHistoryRecyclerAdapter;
import com.project.sc2006.controllers.PartyHistorySwipeHelper;
import com.project.sc2006.controllers.PartyModeController;
import com.project.sc2006.entities.Party;
import com.project.sc2006.entities.PartyHistory;

import java.util.ArrayList;
import java.util.List;

public class PartyHistoryActivity extends AppCompatActivity {
    ListView listView;

    ArrayList<String> listItems = new ArrayList<String>();

    PartyHistoryRecyclerAdapter adapter;
    LinearLayoutManager layoutManager;
    RecyclerView recyclerView;

    String session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_history);

        Bundle extras = getIntent().getExtras();

        session = extras.getString("session");
        recyclerView = findViewById(R.id.listParties);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        PartyHistorySwipeHelper swipeHelper = new PartyHistorySwipeHelper(this, recyclerView, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<PartyHistorySwipeHelper.MyButton> buffer) {
                buffer.add(new MyButton(PartyHistoryActivity.this,
                        "Delete",
                        30,
                        0,
                        Color.parseColor("#FF3C30"),
                        new DeleteButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                adapter.removeAt(pos);
                            }
                        }));
            }
        };

        List<PartyHistory> items = new ArrayList<>();
        PartyModeController controller = MainActivity.partyModeController;
        List<Party> parties = controller.getParticipatedParties();
        for (Party party : parties) {
            items.add(new PartyHistory(party.getPartyEta(), party.getDestinationName(), 0, party.getPartyID()));
        }
        adapter = new PartyHistoryRecyclerAdapter(this, items, session, PartyHistoryActivity.this);
        recyclerView.setAdapter(adapter);

    }
    public void joinPartyFromHistory(PartyHistory item){
//        Toast.makeText(PartyHistoryActivity.this, "Clicked ID" + item.getPartyID(), Toast.LENGTH_SHORT).show();
        MainActivity.partyModeController.joinParty(session, String.valueOf(item.getPartyID()), PartyHistoryActivity.this);
        Intent data = new Intent();
        String text = "123";
        data.setData(Uri.parse(text));
        setResult(RESULT_OK, data);
        finish();
    }
//    public void end(){
//        MainActivity.partyModeController.joinParty(session, code, JoinPartyActivity.this);
//        Intent data = new Intent();
//        String text = "123";
//        data.setData(Uri.parse(text));
//        setResult(RESULT_OK, data);
//        finish();
//    }

}