package il.co.nolife.locotalk.ViewClasses;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import il.co.nolife.locotalk.R;

/**
 * Created by Victor Belski on 9/13/2015.
 */
public class FriendsAddedListAdapter extends ArrayAdapter<String> {

    class ViewHolder {
        AutoCompleteTextView text;
        Button remove;
    }

    class MyTextWatcher implements TextWatcher {

        AutoCompleteTextView view;
        Boolean wasActual;
        String prev;

        public MyTextWatcher(AutoCompleteTextView view) {
            this.view = view;
            wasActual = false;
        }

        @Override
        public void afterTextChanged(Editable s) {

            String str = s.toString();

            if(friends.contains(str)) {

                view.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                if(remaining.contains(str)) {
                    remaining.remove(str);
                    autoCompleteAdapter.notifyDataSetChanged();
                }
                wasActual = true;
                prev = str;

            } else {

                view.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff88aa")));
                if(wasActual) {

                    remaining.add(prev);
                    wasActual = false;
                    prev = "";
                    autoCompleteAdapter.notifyDataSetChanged();

                }

            }

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }

    class RemoveClickListener implements View.OnClickListener {

        int index;

        public RemoveClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {

            String str = mails.get(index);
            if(friends.contains(str)) {
                remaining.add(str);
                autoCompleteAdapter.notifyDataSetChanged();
            }
            mails.remove(index);
            notifyDataSetChanged();

        }

    }

    List<String> mails;
    List<String> friends;
    List<String> remaining;
    Context context;
    LayoutInflater inflater;
    ArrayAdapter<String> autoCompleteAdapter;

    public FriendsAddedListAdapter(Context context, List<String> objects, List<String> allFriends) {
        super(context, R.layout.forum_friend_list_item, objects);

        mails = objects;
        friends = allFriends;
        remaining = new ArrayList<>(allFriends);
        autoCompleteAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, remaining);

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.forum_friend_list_item, parent, false);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();

        if(holder == null) {

            holder = new ViewHolder();
            holder.text = (AutoCompleteTextView) convertView.findViewById(R.id.auto_complete_friend_item);
            holder.text.setAdapter(autoCompleteAdapter);
            holder.text.addTextChangedListener(new MyTextWatcher(holder.text));

            holder.remove = (Button) convertView.findViewById(R.id.remove_button);

        }

        holder.text.setText(mails.get(position));
        if(friends.contains(mails.get(position))) {
            holder.text.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        } else {
            holder.text.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff88aa")));
        }
        holder.remove.setOnClickListener(new RemoveClickListener(position));

        return convertView;

    }
}
