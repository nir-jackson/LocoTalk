package il.co.nolife.locotalk;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Victor Belski on 9/12/2015.
 */
public class LocoInfoWindowAdapter implements GoogleMap.InfoWindowAdapter  {

    View myContentsView;
    Context context;
    IMarkerLocoUserGetter userGetter;
    TextView defaultView;

    public LocoInfoWindowAdapter(Context context, IMarkerLocoUserGetter userGetter){
        this.context = context;
        this.userGetter = userGetter;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myContentsView = inflater.inflate(R.layout.info_window_layout, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {

        final LocoUser user = userGetter.GetUser(marker);

        if(user != null) {
            TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.info_name));
            tvTitle.setText(user.getName());
            Button addFriend = (Button) myContentsView.findViewById(R.id.add_friend_button);

            if(AppController.GetFriends().containsKey(user.getMail())) {
                addFriend.setVisibility(View.GONE);
            } else {
                addFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DataAccessObject dao = new DataAccessObject(context);
                        dao.AddFriend(user);
                    }
                });
            }
            Button chat = (Button) myContentsView.findViewById(R.id.send_message_button);
            chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent chatIntent = new Intent(context, ChatActivity.class);
                    chatIntent.putExtra("type", EChatType.PRIVATE);
                    chatIntent.putExtra("from", user.getMail());
                    context.startActivity(chatIntent);
                }
            });

            return myContentsView;
        } else {
            return defaultView;
        }
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

}
