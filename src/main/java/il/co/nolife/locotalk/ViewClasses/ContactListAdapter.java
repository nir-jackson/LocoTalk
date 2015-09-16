package il.co.nolife.locotalk.ViewClasses;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import il.co.nolife.locotalk.AppController;
import il.co.nolife.locotalk.DataTypes.LocoUser;
import il.co.nolife.locotalk.Callback;
import il.co.nolife.locotalk.R;

/**
 * Created by NirLapTop on 9/12/2015.
 */
public class ContactListAdapter extends ArrayAdapter<LocoUser> {

    class ViewHolder{
        ImageView image;
        TextView name;
        int pos;
    }


    class ThumbnailTask implements Callback<Bitmap> {

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
    Callback<LocoUser> onClickCallback;

    public ContactListAdapter(Context context, List<LocoUser> objects, Activity activity, Callback<LocoUser> onClick) {
        super(context, R.layout.contact_item, objects);

        this.context = context;
        users = objects;
        myActivity = activity;
        onClickCallback = onClick;

    }


    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chat_item, parent, false);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        if(holder == null) {

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.contact_name);
            holder.image = (ImageView) convertView.findViewById(R.id.contact_image);

        }

        holder.name.setText(users.get(position).getName());
        holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.question_man));
        holder.pos = position;
        AppController.GetImage(users.get(position).getIcon(), new ThumbnailTask(holder, position));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCallback.Invoke(users.get(position));
            }
        });

        return convertView;

    }

}
