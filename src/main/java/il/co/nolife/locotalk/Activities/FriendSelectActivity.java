package il.co.nolife.locotalk.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import il.co.nolife.locotalk.AppController;
import il.co.nolife.locotalk.Callback;
import il.co.nolife.locotalk.DataTypes.LocoUser;
import il.co.nolife.locotalk.R;
import il.co.nolife.locotalk.ViewClasses.FriendsListAdapter;

/**
 * Created by Victor Belski on 9/16/2015.
 */
public class FriendSelectActivity extends Activity {

    EditText searchField;
    ListView friendsList;
    List<LocoUser> all;
    List<LocoUser> filtered;
    FriendsListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.get_friend_layout);

        searchField = (EditText) findViewById(R.id.get_friend_search);

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                String str = s.toString().toLowerCase();
                filtered.clear();
                for (LocoUser u : all) {
                    Log.i(getClass().toString(), u.getName() + " contains " + str + " = " + u.getName().toLowerCase().contains(str));
                    if (u.getName().toLowerCase().contains(str)) {
                        filtered.add(u);
                    }
                }
                adapter.notifyDataSetChanged();

            }

        });

        friendsList = (ListView) findViewById(R.id.get_friend_list);

        String[] exclude = getIntent().getStringArrayExtra("exclude");

        all = AppController.GetSafeFriends();
        Log.i(getClass().toString(), "Before:");
        PrintList(all);
        if(exclude != null) {
            for (String s : exclude) {
                Log.i(getClass().toString(), s);
                for (int i = 0; i < all.size(); ++i) {
                    if (s.compareTo(all.get(i).getMail()) == 0) {
                        all.remove(i);
                        --i;
                    }
                }
            }
        }
        Log.i(getClass().toString(), "After:");
        PrintList(all);

        filtered = new ArrayList<>(all);

        adapter = new FriendsListAdapter(this, R.layout.get_friend_layout, filtered, new Callback<LocoUser>() {
            @Override
            public void Invoke(LocoUser result) {

                Intent returnIntent = new Intent();
                returnIntent.putExtra("mail", result.getMail());
                setResult(RESULT_OK, returnIntent);
                finish();

            }
        });

        friendsList.setAdapter(adapter);

    }

    void PrintList(List<LocoUser> l) {

        for (LocoUser u : l) {
            Log.i(getClass().toString(), u.getMail());
        }

    }

}
