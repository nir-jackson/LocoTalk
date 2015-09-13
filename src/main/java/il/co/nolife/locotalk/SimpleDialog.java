package il.co.nolife.locotalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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

        builder.setView(layout)
                .setPositiveButton(args.getString("positive"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onPositive();
                    }
                })
                .setNegativeButton(args.getString("negative"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onNegative();
                    }
                });

        return builder.create();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (DialogClickListener) activity;
        } catch(ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogClickListener");

        }

    }



}
