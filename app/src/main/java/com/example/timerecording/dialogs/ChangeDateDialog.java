package com.example.timerecording.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

public class ChangeDateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Interface für Änderungen bestimmen
        if (getActivity() instanceof IChangeDateTimeListener == false) {
            // Interface nicht umgesetzt, keine Nutzung des Dialoges möglich
            throw new UnsupportedOperationException("Please implement IChangeDateTimeListener in your calling activity");
        }

        // Interface initialisieren
        IChangeDateTimeListener listener = (IChangeDateTimeListener) getActivity();
        Calendar date = listener.getDate(getTag());

        // Initialisieren des Dialoges
        DatePickerDialog dialog = new DatePickerDialog(getContext(),
                this, // Änderungscallback
                date.get(Calendar.YEAR), // Jahr
                date.get(Calendar.MONTH), // Monat
                date.get(Calendar.DAY_OF_MONTH)); // Tag

        return dialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // Interface für Änderungen bestimmen
        if (getActivity() instanceof IChangeDateTimeListener == false) {
            // Interface nicht umgesetzt, keine Nutzung des Dialoges möglich
            throw new UnsupportedOperationException("Please implement IChangeDateTimeListener in your calling activity");
        }

        // Interface initialisieren
        IChangeDateTimeListener listener = (IChangeDateTimeListener) getActivity();

        // Datum ändern
        Calendar date = listener.getDate(getTag());
        date.set(year, month, dayOfMonth);

        // Ausgabe anpassen
        listener.updateView(getTag());
    }
}
