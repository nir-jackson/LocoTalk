package il.co.nolife.locotalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Victor Belski on 9/8/2015.
 */
public class LocoTalkMessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        ApiHandler.Initialize(context);
        final DataAccessObject dao = new DataAccessObject(context);

        if(intent.hasExtra("newMessage")) {

            String messageId = intent.getStringExtra("newMessage");
            try {
                ApiHandler.RetrieveMessage(Long.parseLong(messageId), new Callback<Message>() {
                    @Override
                    public void Invoke(Message result) {
                        dao.WriteMessageToUserConversation(result, false);
                        NotificationHandler.SendNotification(result.getFrom(),result.getContnet(),context);
                    }
                });
            } catch (NumberFormatException e) {
                Log.e(getClass().toString(), "Could not parse messageId as long: " + e.getMessage());
            }

        } else if(intent.hasExtra("message")) {
            try {
                String m = intent.getStringExtra("message");
                JSONObject json = new JSONObject(m);
                GcmMessageHandler.HandleMessage(context, json, m);
            } catch (JSONException e) {
                Log.e(getClass().toString(), e.getMessage());
            }
        }

    }

}
