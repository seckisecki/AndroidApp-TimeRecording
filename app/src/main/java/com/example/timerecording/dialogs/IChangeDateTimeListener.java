package com.example.timerecording.dialogs;

import android.widget.TextView;

import java.util.Calendar;

public interface IChangeDateTimeListener {
    String START_DATE = "StartDate";
    String END_DATE = "EndDate";
    String START_TIME = "StartTime";
    String END_TIME = "EndTime";

    /**
     * Instanz für die Bearbeitung
     *
     * @param dialogType Typ des Dialoges (siehe Konstanten in diesem Interface)
     * @return
     */
    Calendar getDate(String dialogType);

    /**
     * Trigger, um die Oberfläche mit dem neuen Wertt zu aktualisieren
     *
     * @param dialogType Typ des Dialoges (siehe Konstanten in diesem Interface)
     * @return
     */
    void updateView(String dialogType);
}