package il.co.nolife.locotalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;

import java.util.List;

/**
 * Created by NirLapTop on 9/10/2015.
 */
public class ChatActivity extends Activity {

    ChatListAdapter adapter;
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_chat_activity);

        list = (ListView) findViewById(R.id.messagesList);

        Intent intent = getIntent();
        int ex = intent.getIntExtra("type", -1);


        if(ex > -1) {
            EChatType type = EChatType.values()[ex];

            switch (type) {
                case PRIVATE:
                    PrivateChat(intent);
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

    void PrivateChat(Intent intent) {

        DataAccessObject dao = new DataAccessObject(getApplicationContext());
        String mail = intent.getStringExtra("from");
        List<Message> messages = dao.GetMessagesFromDirectConversation(mail);
        adapter = new ChatListAdapter(getApplicationContext(), messages, this);
        list.setAdapter(adapter);

    }

    void EventChat(Intent intent) {

        DataAccessObject dao = new DataAccessObject(getApplicationContext());
        long eventId = intent.getLongExtra("eventId", -1);
        List<Message> messages = dao.GetAllMessagesFromEvent(eventId);
        adapter = new ChatListAdapter(getApplicationContext(), messages, this);
        list.setAdapter(adapter);

    }

    void ForumChat(Intent intent) {

        DataAccessObject dao = new DataAccessObject(getApplicationContext());
        long forumId = intent.getLongExtra("forumId", -1);
        List<Message> messages = dao.GetAllMessagesFromEvent(forumId);
        adapter = new ChatListAdapter(getApplicationContext(), messages, this);
        list.setAdapter(adapter);

    }


}
