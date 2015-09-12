package il.co.nolife.locotalk;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Victor Belski on 9/12/2015.
 * Does all the background operations for LocoTalk
 */
public class LocoBackground extends IntentService {

    public LocoBackground(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

}
