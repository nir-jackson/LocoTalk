package il.co.nolife.locotalk.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.google.api.client.util.DateTime;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import il.co.nolife.locotalk.ApiHandler;
import il.co.nolife.locotalk.AppController;
import il.co.nolife.locotalk.DataAccessObject;
import il.co.nolife.locotalk.DataTypes.EChatType;
import il.co.nolife.locotalk.DataTypes.LocoEvent;
import il.co.nolife.locotalk.DataTypes.LocoForum;
import il.co.nolife.locotalk.DataTypes.LocoUser;
import il.co.nolife.locotalk.Callback;
import il.co.nolife.locotalk.R;
import il.co.nolife.locotalk.ViewClasses.ChatListAdapter;

/**
 * Created by NirLapTop on 9/10/2015.
 */
public class ChatActivity extends Activity {

    ChatListAdapter adapter;
    ListView list;
    TextView title;
    EditText content;
    Button sendButton, centerButton;
    ImageView profileImage;
    List<Message> contentList;

    Intent intent;

    Callback<Message> messageCallback;
    Callback<Long> forumMessageCallback;
    Callback<Long> eventMessageCallback;

    DataAccessObject dao;

    Context appContext;

    LocoUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.basic_chat_activity);

        appContext = getApplicationContext();

        dao = new DataAccessObject(appContext);

        list = (ListView) findViewById(R.id.chat_message_list);

        intent = getIntent();
        int ex = intent.getIntExtra("type", -1);

        title = (TextView)findViewById(R.id.chat_title);
        content = (EditText)findViewById(R.id.chat_message_content);
        sendButton = (Button)findViewById(R.id.chat_send_button);
        profileImage = (ImageView) findViewById(R.id.chat_profile_image);
        centerButton = (Button) findViewById(R.id.chat_center_button);

        if(ex > -1) {
            EChatType type = EChatType.values()[ex];

            switch (type) {
                case PRIVATE:
                    PrivateChat();
                    break;

                case EVENT:
                    EventChat();
                    break;

                case FORUM:
                    ForumChat();
                    break;

            }
        }

        adapter.sort(new Comparator<Message>() {
            @Override
            public int compare(Message lhs, Message rhs) {

                Date left = new Date(lhs.getTimestamp().getValue());
                Date right = new Date(rhs.getTimestamp().getValue());

                if (left.after(right)) {
                    return 1;
                } else if (left.before(right)) {
                    return -1;
                } else {
                    return 0;
                }

            }
        });
        adapter.notifyDataSetChanged();

        list.setSelection(adapter.getCount() - 1);

    }

    void PrivateChat() {

        String mail = intent.getStringExtra("from");
        user = AppController.GetUser(mail);
        Log.i(getClass().toString(), user.toString());
        String tempurl = user.getIcon().replace("sz=50", "sz=150");

        Log.i(getClass().toString(), tempurl);
        title.setText(user.getName());

        AppController.GetImage(tempurl, new Callback<Bitmap>() {
            @Override
            public void Invoke(final Bitmap result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        profileImage.setImageBitmap(result);
                    }
                });
            }
        });

        contentList = dao.GetMessagesFromDirectConversation(mail);
        adapter = new ChatListAdapter(getApplicationContext(), contentList, this, false);
        list.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Message newMessage = new Message();
                newMessage.setTo(user.getMail());
                newMessage.setContnet(content.getText().toString());
                newMessage.setFrom(AppController.GetMyUser().getMail());
                ApiHandler.SendMessageToUser(newMessage, null);
                newMessage.setTimestamp(new DateTime(new Date()));
                dao.WriteMessageToUserConversation(newMessage, true);
                contentList.add(newMessage);
                content.setText("");
                list.setSelection(adapter.getCount() - 1);

            }
        });

        messageCallback = new Callback<Message>() {
            @Override
            public void Invoke(final Message result) {
                if(result.getFrom().compareTo(user.getMail()) == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            contentList.add(result);
                            adapter.notifyDataSetChanged();
                            list.setSelection(adapter.getCount() - 1);
                        }
                    });
                }
            }
        };
        Log.i("SequenceCheck", "after message callback created");

        centerButton = (Button) findViewById(R.id.chat_center_button);

        if(AppController.CheckIfFriend(user.getMail())) {
            UserIsFriend();
        } else {
            UserIsNotFriend();
        }

        AppController.AddPrivateMessageListener(messageCallback);

    }

    void EventChat() {

        final LocoEvent event = dao.GetEvent(intent.getLongExtra("eventId", -1));

        if(event != null) {

            ViewGroup.LayoutParams params = profileImage.getLayoutParams();
            params.width = 0;
            profileImage.setLayoutParams(params);
            centerButton.setText("Remove");
            centerButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dd4465")));
            centerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dao.RemoveEvent(event);
                    finish();

                }
            });

            title.setText(event.getName());

            contentList = dao.GetAllMessagesFromEvent(event.getId());
            adapter = new ChatListAdapter(getApplicationContext(), contentList, this, true);
            list.setAdapter(adapter);
            Log.i(getClass().toString(), "Event Chat");

            eventMessageCallback = new Callback<Long>() {
                @Override
                public void Invoke(Long result) {

                if(result == event.getId()) {

                    contentList.clear();
                    contentList.addAll(dao.GetAllMessagesFromEvent(event.getId()));
                    adapter.notifyDataSetChanged();

                }

                }
            };

            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!content.getText().toString().isEmpty()) {

                        Log.i("Check event", event.toString());
                        ApiHandler.SendEventMessage(event, content.getText().toString());
                        Message newMessage = new Message();
                        newMessage.setContnet(content.getText().toString());
                        newMessage.setFrom(AppController.GetMyUser().getMail());
                        newMessage.setTimestamp(new DateTime(new Date()));
                        dao.WriteMessageToEvent(event, newMessage);
                        content.setText("");
                        list.setSelection(adapter.getCount() - 1);

                    }

                }
            });

        } else {
            Log.e(getClass().toString(), "Could not find the event:" + intent.getLongExtra("eventId", -1));
            finish();
        }

    }

    void ForumChat() {

        final LocoForum forum = dao.GetForum(intent.getLongExtra("forumId", -1));

        if(forum != null) {

            ViewGroup.LayoutParams params = profileImage.getLayoutParams();
            params.width = 0;
            profileImage.setLayoutParams(params);
            centerButton.setText("Remove");
            centerButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dd4465")));
            centerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dao.RemoveForum(forum);
                    finish();

                }
            });

            title.setText(forum.getName());

            contentList = dao.GetAllMessagesFromForum(forum.getId());
            adapter = new ChatListAdapter(getApplicationContext(), contentList, this, true);
            list.setAdapter(adapter);
            Log.i(getClass().toString(), "Forum Chat");

            eventMessageCallback = new Callback<Long>() {
                @Override
                public void Invoke(Long result) {

                    if(result == forum.getId()) {

                        contentList.clear();
                        contentList.addAll(dao.GetAllMessagesFromForum(forum.getId()));
                        adapter.notifyDataSetChanged();

                    }

                }
            };

            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!content.getText().toString().isEmpty()) {

                        ApiHandler.SendForumMessage(forum, content.getText().toString());
                        Message newMessage = new Message();
                        newMessage.setContnet(content.getText().toString());
                        newMessage.setFrom(AppController.GetMyUser().getMail());
                        newMessage.setTimestamp(new DateTime(new Date()));
                        dao.WriteMessageToForum(forum, newMessage);
                        content.setText("");
                        list.setSelection(adapter.getCount() - 1);

                    }

                }
            });

        } else {
            Log.e(getClass().toString(), "Could not find the forum:" + intent.getLongExtra("forumId", -1));
            finish();
        }

    }

    void UserIsFriend() {

        centerButton.setText("Remove Friend");
        centerButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff032d")));
        centerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DataAccessObject dao = new DataAccessObject(getApplicationContext());
                dao.RemoveUserFromFriends(user.getMail());
                UserIsNotFriend();

            }
        });

    }

    void UserIsNotFriend() {

        centerButton.setText("Add as Friend");
        centerButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#339dff")));
        centerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DataAccessObject dao = new DataAccessObject(getApplicationContext());
                dao.AddUserToFriends(user.getMail());
                UserIsFriend();

            }
        });

    }

    protected void onStop() {
        super.onStop();
        if(messageCallback != null) {
            AppController.RemovePrivateMessageListener(messageCallback);
        }
        Log.i("SequenceCheck", "after message callback attached");
        if(forumMessageCallback != null) {
            AppController.RemoveNewForumMessageListener(forumMessageCallback);
        }
        if(eventMessageCallback != null) {
            AppController.RemoveNewEventMessageListener(eventMessageCallback);
        }

    }

    protected void onStart() {
        super.onStart();
        if(messageCallback != null) {
            AppController.AddPrivateMessageListener(messageCallback);
        }
        if(forumMessageCallback != null) {
            AppController.AddNewForumMessageListener(forumMessageCallback);
        }
        if(eventMessageCallback != null) {
            AppController.AddNewEventMessageListener(eventMessageCallback);
        }

    }

}
