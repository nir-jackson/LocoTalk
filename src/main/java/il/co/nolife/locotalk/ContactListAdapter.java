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

import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;

import java.util.List;

/**
 * Created by NirLapTop on 9/12/2015.
 */
public class ContactListAdapter extends ArrayAdapter<LocoUser> {
    class ViewHolder{
        ImageView image;
        TextView name;
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
    List<LocoUser> users;
    Activity myActivity;
    public ContactListAdapter(Context context, List<LocoUser> objects, Activity activity) {
        super(context, R.layout.contact_item, objects);
        this.context = context;
        users = objects;
        myActivity = activity;

    }


    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chat_item, parent, false);
        }
        ViewHolder meta = (ViewHolder) convertView.getTag();

        if(meta == null) {

            meta = new ViewHolder();
            meta.name = (TextView) convertView.findViewById(R.id.contact_name);
            meta.image = (ImageView) convertView.findViewById(R.id.contact_image);

        }

        meta.name.setText(users.get(position).getName());
        meta.image.setImageDrawable(context.getResources().getDrawable(R.drawable.question_man));
        meta.pos = position;
        //User u = AppController.GetUserFromCache(users.get(position).getMail());
        AppController.GetImage(users.get(position).getIcon(), new ThumbnailTask(meta, position));


        return convertView;

    }

}
