package il.co.nolife.locotalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NirLapTop on 9/10/2015.
 */
public class ChatActivity extends Activity {

    ChatListAdapter adapter;
    ListView list;
    EditText mail,content;
    List<Message> listm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.basic_chat_activity);

        list = (ListView) findViewById(R.id.messagesList);

        Intent intent = getIntent();
        int ex = intent.getIntExtra("type", -1);
        listm = new ArrayList<>();
        adapter = new ChatListAdapter(this,listm,this);
        list.setAdapter(adapter);

        mail = (EditText)findViewById(R.id.to_email);
        content = (EditText)findViewById(R.id.message_content);
        Button b = (Button)findViewById(R.id.sendbutton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message m = new Message();
                m.setContnet(content.getText().toString());
                m.setTo(mail.getText().toString());
                m.setFrom(ApiHandler.GetUser().getMail());
                Log.i(getClass().toString(), m.toString());
                ApiHandler.SendMessageToUser(m, null);
                listm.add(m);
                adapter.notifyDataSetChanged();
            }
        });
//        if(ex > -1) {
//            EChatType type = EChatType.values()[ex];
//
//            switch (type) {
//                case PRIVATE:
//                    PrivateChat(intent);
//                    break;
//
//                case EVENT:
//                    EventChat(intent);
//                    break;
//
//                case FORUM:
//                    ForumChat(intent);
//                    break;
//
//            }
//        }


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
