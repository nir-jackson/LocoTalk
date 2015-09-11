package il.co.nolife.locotalk;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;

import java.util.List;

/**
 * Created by Victor Belski on 9/11/2015.
 */
public class ChatListAdapter extends ArrayAdapter<Message> {

    class ViewMetadata {

        TextView content;
        ImageView image;
        int pos;

    }

    class ThumbnailTask implements IApiCallback<Bitmap> {

        ViewMetadata holder;
        int pos;
        Activity activity;

        public ThumbnailTask(ViewMetadata holder, int pos) {
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



    public ChatListAdapter(Context context, List<Message> objects, Activity activity) {
        super(context, R.layout.chat_item, objects);

        this.context = context;
        messages = objects;
        myActivity = activity;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chat_item, parent, false);
        }
        ViewMetadata meta = (ViewMetadata) convertView.getTag();

        if(meta == null) {

            meta = new ViewMetadata();
            meta.content = (TextView) convertView.findViewById(R.id.chatItemContent);
            meta.image = (ImageView) convertView.findViewById(R.id.chatItemIcon);

        }

        meta.content.setText(messages.get(position).getContnet());
        meta.image.setImageDrawable(context.getResources().getDrawable(R.drawable.question_man));
        meta.pos = position;
        User u = AppController.GetUserFromCache(messages.get(position).getFrom());
        if(u != null) {
            AppController.GetImage(u.getImageUrl(), new ThumbnailTask(meta, position));
        }

        return convertView;

    }

}
