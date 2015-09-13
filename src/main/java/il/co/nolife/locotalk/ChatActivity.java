package il.co.nolife.locotalk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;
import com.google.api.client.util.DateTime;

import java.util.Date;
import java.util.List;

/**
 * Created by NirLapTop on 9/10/2015.
 */
public class ChatActivity extends Activity {

    ChatListAdapter adapter;
    ListView list;
    TextView title;
    EditText content;
    Button sendButton, addFriend;
    ImageView profile;
    List<Message> contentList;

    Intent intent;

    IApiCallback<Message> messageCallback;
    IApiCallback<Long> forumMessageCallback;
    IApiCallback<Long> eventMessageCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.basic_chat_activity);

        list = (ListView) findViewById(R.id.messagesList);

        intent = getIntent();
        int ex = intent.getIntExtra("type", -1);

        title = (TextView)findViewById(R.id.chat_title);
        content = (EditText)findViewById(R.id.message_content);
        sendButton = (Button)findViewById(R.id.sendbutton);

        if(ex > -1) {
            EChatType type = EChatType.values()[ex];

            switch (type) {
                case PRIVATE:
                    PrivateChat();
                    break;

                case EVENT:
                    EventChat(intent);
                    break;

                case FORUM:
                    ForumChat(intent);
                    break;

            }
        }


    }

    void PrivateChat() {

        final DataAccessObject dao = new DataAccessObject(getApplicationContext());
        String mail = intent.getStringExtra("from");
        final User user = AppController.GetUserFromCache(mail);
        Log.i(getClass().toString(), user.toString());
        String tempurl;
        if(user.getImageUrl().indexOf("sz=50")!= -1){
            tempurl = user.getImageUrl().replace("sz=50","sz=400");
        }else{
            tempurl = user.getImageUrl().replace("sz=150","sz=400");
        }

        Log.i(getClass().toString(),tempurl);
        title.setText(user.getFullName());
        final ImageView imageView = (ImageView)findViewById(R.id.profile_image);

        AppController.GetImage(tempurl, new IApiCallback<Bitmap>() {
            @Override
            public void Invoke(final Bitmap result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(result);
                    }
                });
            }
        });
        contentList = dao.GetMessagesFromDirectConversation(mail);
        adapter = new ChatListAdapter(getApplicationContext(), contentList, this);
        list.setAdapter(adapter);

        list.setSelection(adapter.getCount() - 1);

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
                adapter.notifyDataSetChanged();

            }
        });

        messageCallback = new IApiCallback<Message>() {
            @Override
            public void Invoke(Message result) {
                if(result.getFrom().compareTo(user.getMail()) == 0) {
                    contentList.add(result);
                    adapter.notifyDataSetChanged();
                }
            }
        };

        AppController.AddPrivateMessageListener(messageCallback);

    }

    void EventChat(Intent intent) {

        DataAccessObject dao = new DataAccessObject(getApplicationContext());
        long eventId = intent.getLongExtra("eventId", -1);
        List<Message> messages = dao.GetAllMessagesFromEvent(eventId);
        adapter = new ChatListAdapter(getApplicationContext(), messages, this);
        list.setAdapter(adapter);
        Log.i(getClass().toString(), "Event Chat");
        profile = (ImageView)findViewById(R.id.profile_image);
        profile.setVisibility(View.GONE);

    }

    void ForumChat(Intent intent) {

        DataAccessObject dao = new DataAccessObject(getApplicationContext());
        long forumId = intent.getLongExtra("forumId", -1);
        List<Message> messages = dao.GetAllMessagesFromEvent(forumId);
        adapter = new ChatListAdapter(getApplicationContext(), messages, this);
        list.setAdapter(adapter);
        Log.i(getClass().toString(), "Forum Chat");
    }

    protected void onStop() {
        super.onStop();
        if(messageCallback != null) {
            AppController.RemovePrivateMessageListener(messageCallback);
        }
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
