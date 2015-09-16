package il.co.nolife.locotalk.ViewClasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import il.co.nolife.locotalk.Callback;
import il.co.nolife.locotalk.DataTypes.LocoUser;
import il.co.nolife.locotalk.R;

/**
 * Created by Victor Belski on 9/16/2015.
 */
public class FriendsListAdapter extends ArrayAdapter<LocoUser> {

    class ViewHolder {
        ImageView image;
        TextView text;
    }

    List<LocoUser> users;
    List<LocoUser> allUsers;
    LayoutInflater inflater;
    Context context;
    Callback<LocoUser> onClickCallback;

    public FriendsListAdapter(Context context, int resource, List<LocoUser> availableUsers, Callback<LocoUser> onClickCallback) {
        super(context, resource, availableUsers);

        this.context = context;
        users = availableUsers;
        allUsers = availableUsers;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.onClickCallback = onClickCallback;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.friend_item_layout, parent, false);
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
