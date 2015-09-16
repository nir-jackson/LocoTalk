package il.co.nolife.locotalk.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import il.co.nolife.locotalk.AppController;
import il.co.nolife.locotalk.Callback;
import il.co.nolife.locotalk.DataTypes.LocoUser;
import il.co.nolife.locotalk.R;

/**
 * Created by Victor Belski on 9/16/2015.
 */
public class FriendSelectActivity extends Activity {

    class FriendsListAdapter extends ArrayAdapter<LocoUser> {

        class ViewHolder {
            ImageView image;
            TextView text;
        }

        List<LocoUser> users;
        List<LocoUser> allUsers;
        LayoutInflater inflater;
        Context context;
        Callback<LocoUser> onClickCallback;

        public FriendsListAdapter(Context context, List<LocoUser> availableUsers, Callback<LocoUser> onClickCallback) {
            super(context, R.layout.friend_item_layout, availableUsers);

            this.context = context;
            users = availableUsers;
            allUsers = availableUsers;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.onClickCallback = onClickCallback;

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if(convertView == null) {
                convertView = inflater.inflate(R.layout.friend_item_layout, parent);
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickCallback.Invoke(users.get(position));
                }
            });

            ViewHolder holder = (ViewHolder) convertView.getTag();
            if(holder == null) {

                holder = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.friend_item_image);
                holder.text = (TextView) convertView.findViewById(R.id.friend_item_text);

            }

            holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.question_man));
            holder.text.setText(users.get(position).getName());

            convertView.setTag(holder);

            return convertView;

        }



    }

    EditText searchField;
    ListView friendsList;
    List<LocoUser> all;
    List<LocoUser> filtered;
    FriendsListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchField = (EditText) findViewById(R.id.get_friend_search);

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

                String str = s.toString().toLowerCase();
                filtered.clear();
                for (LocoUser u : all) {
                    if(u.getName().toLowerCase().contains(str)) {
                        filtered.add(u);
                    }
                }
                adapter.notifyDataSetChanged();

            }

        });

        friendsList = (ListView) findViewById(R.id.get_friend_list);

        String[] exclude = getIntent().getStringArrayExtra("exclude");

        all = AppController.GetSafeFriends();
        if(exclude != null) {
            for (String s : exclude) {
                for (int i = 0; i < all.size(); ++i) {
                    if (s.compareTo(all.get(i).getMail()) == 0) {
                        all.remove(i);
                        --i;
                    }
                }
            }
        }

        filtered = all;

        adapter = new FriendsListAdapter(this, filtered, new Callback<LocoUser>() {
            @Override
            public void Invoke(LocoUser result) {

                Intent returnIntent = new Intent();
                returnIntent.putExtra("mail", result.getMail());
                setResult(RESULT_OK, returnIntent);
                finish();

            }
        });

    }

}
