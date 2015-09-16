package il.co.nolife.locotalk.ViewClasses;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

import il.co.nolife.locotalk.Callback;

/**
 * Created by Victor Belski on 9/16/2015.
 */
public class ThumbnailRetriever implements Callback<Bitmap> {

    ImageView view;
    Activity activity;
    boolean canceled;

    public ThumbnailRetriever(ImageView view, Activity activity) {
        this.view = view;
        this.activity = activity;
        canceled = false;
    }

    @Override
    public void Invoke(final Bitmap result) {
        if(!canceled) {
            if(result != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setImageBitmap(result);
                    }
                });
            }
        }
    }

    public void Cancel() {
        canceled = true;
    }

}
