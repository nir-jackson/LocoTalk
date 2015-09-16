package il.co.nolife.locotalk.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import il.co.nolife.locotalk.ApiHandler;
import il.co.nolife.locotalk.AppController;
import il.co.nolife.locotalk.DataAccessObject;
import il.co.nolife.locotalk.DataTypes.LocoEvent;
import il.co.nolife.locotalk.DataTypes.LocoForum;
import il.co.nolife.locotalk.DataTypes.LocoUser;
import il.co.nolife.locotalk.R;
import il.co.nolife.locotalk.ViewClasses.SimpleDialog;

/**
 * Created by NirLapTop on 9/13/2015.
 */
public class EventOrForumActivity extends Activity {

    class RemovalbleFriendListAdapter extends ArrayAdapter<LocoUser> {

        class ViewHolder {
            ImageView image;
            TextView text;
            Button remove;
        }

        List<LocoUser> users;
        Context context;
        LayoutInflater inflater;

        public RemovalbleFriendListAdapter(Context context, List<LocoUser> users) {
            super(context, R.layout.forum_friend_list_item, users);

            this.users = users;
            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public View getView(final int pos, View convertView, ViewGroup parent) {

            if(convertView == null) {
                convertView = inflater.inflate(R.layout.forum_friend_list_item, parent, false);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();

            if(holder == null) {

                holder = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.friend_item_image);
                holder.text = (TextView) convertView.findViewById(R.id.friend_item_text);
                holder.remove = (Button) convertView.findViewById(R.id.remove_button);

            }

            holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.question_man));
            holder.text.setText(users.get(pos).getName());
            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    users.remove(pos);
                    notifyDataSetChanged();
                }
            });

            convertView.setTag(holder);

            return convertView;

        }

    }

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

    List<LocoUser> users;
    RemovalbleFriendListAdapter adapter;

    boolean forumSelected;

    GeoPt position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.event_or_forum);

        users = new ArrayList<>();

        forum = (Button) findViewById(R.id.picker_forum);
        forum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForumClicked();
            }
        });
        event = (Button) findViewById(R.id.picker_event);
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

                Intent intent = new Intent(getApplicationContext(), FriendSelectActivity.class);
                ArrayList<String> exclude = new ArrayList<String>();
                for (LocoUser u : users) {
                    exclude.add(u.getMail());
                }
                intent.putStringArrayListExtra("exclude", exclude);

                startActivityForResult(intent, 0);

            }
        });

        radiusPanel = (LinearLayout) findViewById(R.id.picker_number_holder);
        seekBar = (SeekBar) findViewById(R.id.picker_seek_bar);
        seekBar.setMax(LocoTalkMain.MAX_RANGE);
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
        adapter = new RemovalbleFriendListAdapter(this, users);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {

            String mail = data.getStringExtra("mail");
            LocoUser user = AppController.GetUser(mail);
            users.add(user);
            adapter.notifyDataSetChanged();

        }

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


                users.add(AppController.GetMyUser());

                if(users.size() > 1) {

                    LocoForum newForum = new LocoForum();
                    newForum.setLocation(position);
                    newForum.setName(name);
                    newForum.setOwner(AppController.GetMyUser().getMail());

                    newForum.setUsers(users);

                    DataAccessObject dao = new DataAccessObject(getApplicationContext());

                    boolean ret = false;
                    do {

                        SecureRandom random = new SecureRandom();
                        long forumId = random.nextLong();
                        if(forumId != -1) {
                            newForum.setId(forumId);
                            ret = dao.CreateOwnedForum(users, position, name, AppController.GetMyUser().getMail(), forumId);
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

                    boolean ret = false;

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

}
