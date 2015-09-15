package il.co.nolife.locotalk.ViewClasses;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;

import java.util.List;

import il.co.nolife.locotalk.AppController;
import il.co.nolife.locotalk.DataTypes.LocoUser;
import il.co.nolife.locotalk.IApiCallback;
import il.co.nolife.locotalk.R;

/**
 * Created by Victor Belski on 9/11/2015.
 */
public class ChatListAdapter extends ArrayAdapter<Message> {

    class ViewHolder {

        TextView content;
        ImageView image;
        int pos;

    }

    class ThumbnailTask implements IApiCallback<Bitmap> {

        ViewHolder holder;
        int pos;
        Activity activity;

        public ThumbnailTask(ViewHolder holder, int pos) {
            this.holder = holder;
            this.pos = pos;
            activity = myActivity;
        }

        @Override
        public void Invoke(final Bitmap result) {
            if(holder.pos == pos) {
                if(result != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            holder.image.setImageBitmap(result);
                        }
                    });
                }
            }
        }

    }

    Context context;
    List<Message> messages;
    Activity myActivity;
    int myColor;
    int whiteColor;
    boolean showImages;

    public ChatListAdapter(Context context, List<Message> objects, Activity activity, boolean showImages) {
        super(context, R.layout.chat_item, objects);

        this.context = context;
        messages = objects;
        myActivity = activity;
        this.showImages = showImages;
        myColor = Color.parseColor("#a4f9ff");
        whiteColor = Color.WHITE;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chat_item, parent, false);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        if(holder == null) {

            holder = new ViewHolder();
            holder.content = (TextView) convertView.findViewById(R.id.chatItemContent);
            holder.image = (ImageView) convertView.findViewById(R.id.chatItemIcon);

        }

        holder.content.setText(messages.get(position).getContnet());
        holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.question_man));
        holder.pos = position;
        if(AppController.GetMyUser().getMail().compareTo(messages.get(position).getFrom()) == 0) {
            convertView.setBackgroundColor(myColor);
            holder.content.setGravity(Gravity.RIGHT);
        } else {
            convertView.setBackgroundColor(whiteColor);
            holder.content.setGravity(Gravity.LEFT);
        }

        if(showImages) {
            LocoUser u = AppController.GetUser(messages.get(position).getFrom());
            if (u != null) {
                AppController.GetImage(u.getIcon(), new ThumbnailTask(holder, position));
            }
        } else {
            holder.image.setVisibility(View.GONE);
        }

        return convertView;

    }

}
