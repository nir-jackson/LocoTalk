package il.co.nolife.locotalk.ViewClasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import il.co.nolife.locotalk.R;

/**
 * Created by Victor Belski on 9/12/2015.
 * A simple dialog for asking the user simple things
 */
public class SimpleDialog extends DialogFragment {

    public interface DialogClickListener {
        void onPositive();
        void onNegative();
    }

    DialogClickListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View layout = inflater.inflate(R.layout.basic_dialog, null);
        TextView contentView = (TextView) layout.findViewById(R.id.dialog_content);
        TextView titleView = (TextView) layout.findViewById(R.id.dialog_title);
        contentView.setText(args.getString("content"));
        titleView.setText(args.getString("title"));

        String positive = args.getString("positive");
        String negative = args.getString("negative");

        boolean writeSingle = true;
        builder.setView(layout);
        if((positive != null) && (negative != null)) {
            if ((!positive.isEmpty()) && (!negative.isEmpty())) {
                writeSingle = false;
                builder
                        .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listener.onPositive();
                            }
                        })
                        .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listener.onNegative();
                            }
                        });

            }
        }

        if(writeSingle) {
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            });
        }

        return builder.create();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (DialogClickListener) activity;
        } catch(ClassCastException e) {
            Log.e(getClass().toString(), activity.toString() + " does not implement DialogClickListener");

        }

    }



}
