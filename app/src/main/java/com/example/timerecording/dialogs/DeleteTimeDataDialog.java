package com.example.timerecording.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.example.timerecording.R;

public class DeleteTimeDataDialog extends AppCompatDialogFragment {
    public static final String ID_KEY = "Key_TimeDataId";
    public static final String POSITION_KEY = "Key_TimeDataPosition";
    private long _id = -1L;
    private int _position = -1;

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);

        // ID aus Argumente auslesen
        _id = args.getLong(ID_KEY, -1L);
        _position = args.getInt(POSITION_KEY, -1);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // ID für die Drehung zwischenspeichern
        outState.putLong(ID_KEY, _id);
        outState.putInt(POSITION_KEY, _position);

        super.onSaveInstanceState(outState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // ID wiederherstellen, falls Drehung passiert ist
        if (savedInstanceState != null && savedInstanceState.containsKey(ID_KEY)) {
            _id = savedInstanceState.getLong(ID_KEY);
        }

        // Position wiederherstellen, falls Drehung passiert ist
        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY)) {
            _position = savedInstanceState.getInt(POSITION_KEY);
        }

        // Fehlermeldung ausgeben, falls ID nicht gesetzt wurde
        if (_id == -1L) {
            throw new IllegalArgumentException("Please set id with 'setArguments' method and key'" + ID_KEY + "'");
        }

        // Fehlermeldung ausgeben, falls Position nicht gesetzt wurde
        if (_id == -1L) {
            throw new IllegalArgumentException("Please set position with 'setArguments' method and key'" + POSITION_KEY + "'");
        }

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.DialogTitleDeleteItem)
                .setMessage(R.string.DialogMessageDeleteItem)
                .setNegativeButton(R.string.ButtonCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ButtonDelete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Löschfunktion in Activity aufrufen
                        if (getActivity() instanceof IItemActionListener) {
                            ((IItemActionListener) getActivity()).deleteItem(_id, _position);
                        }

                        // Dialog schließen
                        dialog.dismiss();
                    }
                })
                .create();
    }
}
