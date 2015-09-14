package il.co.nolife.locotalk;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import il.co.nolife.locotalk.DataTypes.LocoEvent;
import il.co.nolife.locotalk.DataTypes.LocoForum;
import il.co.nolife.locotalk.DataTypes.LocoUser;
import il.co.nolife.locotalk.ViewClasses.FriendsAddedListAdapter;
import il.co.nolife.locotalk.ViewClasses.SimpleDialog;

/**
 * Created by NirLapTop on 9/13/2015.
 */
public class EventOrForumActivity extends Activity {

    public static final int min = 0;
    public static final int max = 300000;

    Button forum, event;

    Button addFriend;
    LinearLayout radiusPanel;
    ListView participantsList;
    EditText nameView;
    SeekBar seekBar;
    TextView radiusText;
    TextView radius;
    Button finish;

    Intent thisIntent;

    static List<String> participants;
    List<String> friendMails;
    FriendsAddedListAdapter adapter;

    Boolean forumSelected;

    GeoPt position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.event_or_forum);

        if(participants == null) {
            participants = new ArrayList<String>();
        }

        List<LocoUser> friends = AppController.GetSafeFriends();
        friendMails = new ArrayList<>();

        for (LocoUser u : friends) {
            friendMails.add(u.getMail());
        }


        forum = (Button)findViewById(R.id.picker_forum);
        forum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForumClicked();
            }
        });
        event = (Button)findViewById(R.id.picker_event);
        event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventClicked();
            }
        });

        addFriend = (Button) findViewById(R.id.picker_add_friend);
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                participants.add("");
                adapter.notifyDataSetChanged();
            }
        });

        radiusPanel = (LinearLayout) findViewById(R.id.picker_number_holder);
        seekBar = (SeekBar) findViewById(R.id.picker_seek_bar);
        seekBar.setMax(max);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress > 0) {
                        radius.setText(Integer.toString(progress));
                    } else {
                        radius.setText(Integer.toString(0));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        radius = (TextView) findViewById(R.id.picker_radius);

        radiusText = (TextView) findViewById(R.id.picker_range_text);

        nameView = (EditText) findViewById(R.id.picker_name_text);
        participantsList = (ListView) findViewById(R.id.picker_participants);
        adapter = new FriendsAddedListAdapter(this, participants, friendMails);
        participantsList.setAdapter(adapter);

        finish = (Button) findViewById(R.id.picker_create);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FinishClicked();
            }
        });

        thisIntent = getIntent();
        position = new GeoPt();
        position.setLatitude((float)thisIntent.getDoubleExtra("lat",0));
        position.setLongitude((float) thisIntent.getDoubleExtra("lon", 0));

        ForumClicked();

        finish.setBackgroundTintList(null);

    }


    void ForumClicked() {

        forumSelected = true;
        radiusPanel.setVisibility(View.GONE);
        radiusText.setVisibility(View.GONE);
        seekBar.setVisibility(View.GONE);
        addFriend.setVisibility(View.VISIBLE);
        participantsList.setVisibility(View.VISIBLE);
        forum.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#99aaee")));
        event.setBackgroundTintList(null);
        nameView.setHint("Forum Name");

    }

    void EventClicked() {

        forumSelected = false;
        radiusPanel.setVisibility(View.VISIBLE);
        radiusText.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
        addFriend.setVisibility(View.GONE);
        participantsList.setVisibility(View.GONE);
        event.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#99aaee")));
        forum.setBackgroundTintList(null);
        nameView.setHint("Event Name");

    }

    void FinishClicked() {

        String name = nameView.getText().toString();

        if(name.isEmpty()) {

            Bundle bundle = new Bundle();
            bundle.putString("title", "Name Missing");
            bundle.putString("content", "Name wasn't set, please use the box on the top of this window to set the name");

            SimpleDialog dialog = new SimpleDialog();
            dialog.setArguments(bundle);
            dialog.show(getFragmentManager(), "SimpleDialog");

        } else {

            if (forumSelected) {

                List<String> actual = new ArrayList<>();

                for (String p : participants) {
                    if(friendMails.contains(p)) {
                        actual.add(p);
                    }
                }

                actual.add(AppController.GetMyUser().getMail());

                if(actual.size() > 1) {

                    LocoForum newForum = new LocoForum();
                    newForum.setLocation(position);
                    newForum.setName(name);
                    newForum.setOwner(AppController.GetMyUser().getMail());

                    HashMap<String, LocoUser> friendsMap = AppController.GetFriends();
                    List<LocoUser> finalParticipands = new ArrayList<>();

                    for (String m : actual) {
                        LocoUser user = friendsMap.get(m);
                        if (!finalParticipands.contains(user)) {
                            finalParticipands.add(user);
                        }
                    }

                    newForum.setUsers(finalParticipands);

                    DataAccessObject dao = new DataAccessObject(getApplicationContext());

                    Boolean ret = false;
                    do {

                        SecureRandom random = new SecureRandom();
                        long forumId = random.nextLong();
                        if(forumId != -1) {
                            newForum.setId(forumId);
                            ret = dao.CreateOwnedForum(finalParticipands, position, name, AppController.GetMyUser().getMail(), forumId);
                        }

                    } while(!ret);

                    ApiHandler.CreateForum(newForum);

                    finish();

                } else {

                    Bundle bundle = new Bundle();
                    bundle.putString("title", "No Participants");
                    bundle.putString("content", "Forum without any participants, add some friends to the forum");

                    SimpleDialog dialog = new SimpleDialog();
                    dialog.setArguments(bundle);
                    dialog.show(getFragmentManager(), "SimpleDialog");

                }

            } else {

                try {

                    int radiusInt = Integer.parseInt(radius.getText().toString());
                    LocoEvent newEvent = new LocoEvent();
                    newEvent.setName(name);
                    newEvent.setOwner(AppController.GetMyUser().getMail());
                    newEvent.setLocation(position);
                    newEvent.setRadius(radiusInt);

                    DataAccessObject dao = new DataAccessObject(getApplicationContext());

                    Boolean ret = false;

                    do {

                        SecureRandom random = new SecureRandom();
                        long eventId = random.nextLong();
                        if(eventId != -1) {
                            newEvent.setId(eventId);
                            ret = dao.CreateOwnedEvent(position, name, AppController.GetMyUser().getMail(), radiusInt, eventId);
                        }

                    } while (!ret);

                    finish();

                } catch(NumberFormatException e) {
                    Log.e(getClass().toString(), e.getMessage());
                }

            }

        }

    }

    public static void Reset() {
        participants = new ArrayList<>();
    }

}
