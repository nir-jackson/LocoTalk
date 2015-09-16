package il.co.nolife.locotalk.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;

import java.util.List;

/**
 * Created by Victor Belski on 9/16/2015.
 */
public abstract class BaseChatActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected abstract ListView getListView();

    protected abstract List<Message> getList();

}
