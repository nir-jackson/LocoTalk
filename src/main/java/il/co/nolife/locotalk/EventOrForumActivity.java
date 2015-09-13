package il.co.nolife.locotalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/**
 * Created by NirLapTop on 9/13/2015.
 */
public class EventOrForumActivity extends Activity {
    Button forum, event;
    Intent thisIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.event_or_forum);
        forum = (Button)findViewById(R.id.new_forum_button);
        event = (Button)findViewById(R.id.new_event_button);
        thisIntent = getIntent();

        forum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newForumActivityStart();
            }
        });

        event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newEventActivityStart();
            }
        });
    }

    void newForumActivityStart(){
        Intent intent = new Intent(this,ChatActivity.class);

        intent.putExtra("type", EChatType.FORUM.ordinal());
        intent.putExtra("longitude",thisIntent.getDoubleExtra("longitude", 0.0));
        intent.putExtra("latitude",thisIntent.getDoubleExtra("latitude",0.0));
        startActivity(intent);
    }
    void newEventActivityStart() {
        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtra("type", EChatType.EVENT.ordinal());
        intent.putExtra("longitude",thisIntent.getDoubleExtra("longitude",0.0));
        intent.putExtra("latitude",thisIntent.getDoubleExtra("latitude",0.0));
        startActivity(intent);
    }
}
