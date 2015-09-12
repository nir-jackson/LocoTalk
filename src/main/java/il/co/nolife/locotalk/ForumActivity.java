package il.co.nolife.locotalk;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

public class ForumActivity extends Activity {
    boolean isPublic = false;
    List<LocoUser> contactList, friendslist;
    EditText to,content,eName;
    ContactListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.new_forum);
        DataAccessObject dao = new DataAccessObject(getApplicationContext());
        friendslist = dao.GetAllFriends();
        ListView listV = (ListView)findViewById(R.id.to_list);
        adapter = new ContactListAdapter(this,contactList,this);
        listV.setAdapter(adapter);
        Button b1 = (Button)findViewById(R.id.add_contact_button);
        Button b2 = (Button)findViewById(R.id.forum_send);
        to = (EditText)findViewById(R.id.mail_temp);
        content = (EditText)findViewById(R.id.content_forum);
        eName = (EditText)findViewById(R.id.name);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (LocoUser lu: friendslist
                     ) {
                    if (lu.getMail().compareTo(to.getText().toString()) == 0){
                        contactList.add(lu);
                        adapter.notifyDataSetChanged();
                    }
                }
                to.setText("");
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_forum, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
