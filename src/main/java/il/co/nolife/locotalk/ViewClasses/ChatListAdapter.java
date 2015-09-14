package il.co.nolife.locotalk.ViewClasses;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
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
    ColorStateList myColor;
    ColorStateList whiteColor;
    Boolean showImages;

    public ChatListAdapter(Context context, List<Message> objects, Activity activity, Boolean showImages) {
        super(context, R.layout.chat_item, objects);

        this.context = context;
        messages = objects;
        myActivity = activity;
        this.showImages = showImages;
        myColor = ColorStateList.valueOf(Color.parseColor("#e3e3f3"));
        whiteColor = ColorStateList.valueOf(Color.WHITE);

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
            holder.content.setBackgroundTintList(myColor);
            holder.content.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        } else {

            holder.content.setBackgroundTintList(whiteColor);
            holder.content.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);


        }

        if(showImages) {
            LocoUser u = AppController.GetUserFromCache(messages.get(position).getFrom());
            if (u != null) {
                AppController.GetImage(u.getIcon(), new ThumbnailTask(holder, position));
            }
        } else {
            holder.image.setVisibility(View.GONE);
        }

        return convertView;

    }

}
