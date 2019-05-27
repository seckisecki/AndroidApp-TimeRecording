package com.example.timerecording.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import java.util.Calendar;

public class ChangeTimeDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Interface für Änderungen bestimmen
        if (getActivity() instanceof IChangeDateTimeListener == false) {
            // Interface nicht umgesetzt, keine Nutzung des Dialoges möglich
            throw new UnsupportedOperationException("Please implement IChangeDateTimeListener in your calling activity");
        }

        // Interface initialisieren
        IChangeDateTimeListener listener = (IChangeDateTimeListener) getActivity();
        Calendar time = listener.getDate(getTag());

        // Anzeige 12 / 24h
        boolean is24 = android.text.format.DateFormat.is24HourFormat(getContext());

        TimePickerDialog dialog = new TimePickerDialog(getContext(),
                this,
                time.get(Calendar.HOUR_OF_DAY),
                time.get(Calendar.MINUTE),
                is24);

        return dialog;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Interface für Änderungen bestimmen
        if (getActivity() instanceof IChangeDateTimeListener == false) {
            // Interface nicht umgesetzt, keine Nutzung des Dialoges möglich
            throw new UnsupportedOperationException("Please implement IChangeDateTimeListener in your calling activity");
        }

        // Interface initialisieren
        IChangeDateTimeListener listener = (IChangeDateTimeListener) getActivity();

        Calendar time = listener.getDate(getTag());
        time.set(Calendar.HOUR_OF_DAY, hourOfDay);
        time.set(Calendar.MINUTE, minute);

        // Ausgabe anpassen
        listener.updateView(getTag());
    }
}
