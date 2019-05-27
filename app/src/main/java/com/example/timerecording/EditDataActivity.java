package com.example.timerecording;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import com.example.timerecording.db.TimeDataContract;
import com.example.timerecording.dialogs.ChangeDateDialog;
import com.example.timerecording.dialogs.ChangeTimeDialog;
import com.example.timerecording.dialogs.IChangeDateTimeListener;

public class EditDataActivity extends AppCompatActivity implements IChangeDateTimeListener {
    public static final String ID_KEY = "TimeDataId";

    private static final long _NO_DATA = -1L;
    private long _timeDataId = _NO_DATA;

    // UI Elemente
    private EditText _startDate;
    private EditText _startTime;
    private EditText _endDate;
    private EditText _endTime;
    private EditText _pause;
    private EditText _comment;

    // Formatter
    DateFormat _dateFormatter = null;
    DateFormat _timeFormatter = null;

    // Konstanten für die Zustandssicherung
    private static final String _START_DATE_TIME_KEY = "Key_StartTime";
    private static final String _END_DATE_TIME_KEY = "Key_EndTime";
    private static final String _PAUSE_KEY = "Key_Pause";
    private static final String _COMMENT_KEY = "Key_Comment";

    // Datumsfelder
    private Calendar _startDateTime = Calendar.getInstance();
    private Calendar _endDateTime = Calendar.getInstance();

    // Sicherungsfeld
    private boolean _isRestored = false;

    // Focus
    private boolean _isFirstFocus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_data);

        // Auslesen der übergebenen Metainformationen, flass welche da sind
        _timeDataId = getIntent().getLongExtra(
                ID_KEY, // Key für die Daten
                _NO_DATA // Standardwert, falls nichts übergeben wurde
        );

        // Initialisieren der UI
        _startDate = findViewById(R.id.StartDateValue);
        _startTime = findViewById(R.id.StartTimeValue);
        _endDate = findViewById(R.id.EndDateValue);
        _endTime = findViewById(R.id.EndTimeValue);
        _pause = findViewById(R.id.PauseValue);
        _comment = findViewById(R.id.CommentValue);

        // Tastatureingaben verhindern
        _startDate.setKeyListener(null);
        _startTime.setKeyListener(null);
        _endDate.setKeyListener(null);
        _endTime.setKeyListener(null);

        // Initialisieren der Formatter
        _dateFormatter = android.text.format.DateFormat.getDateFormat(this);
        _timeFormatter = android.text.format.DateFormat.getTimeFormat(this);

        // Wiederherstellen der Daten
        if (savedInstanceState != null) {
            _isRestored = savedInstanceState.containsKey(_START_DATE_TIME_KEY);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Datumsfelder als Anzahl der Millisekunden merken
        outState.putLong(_START_DATE_TIME_KEY, _startDateTime.getTimeInMillis());
        outState.putLong(_END_DATE_TIME_KEY, _endDateTime.getTimeInMillis());

        // Pause nach der Konvertierung in Integer merken
        String pauseValue = _pause.getText().toString();
        if (pauseValue != null && !pauseValue.isEmpty()) {
            int pause = Integer.parseInt(pauseValue);
            outState.putInt(_PAUSE_KEY, pause);
        }

        // Kommentar sichern
        outState.putString(_COMMENT_KEY, _comment.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Startdatum
        long milliseconds = savedInstanceState.getLong(_START_DATE_TIME_KEY, 0L);
        if (milliseconds > 0) {
            _startDateTime.setTimeInMillis(milliseconds);
        }

        // Enddatum
        milliseconds = savedInstanceState.getLong(_END_DATE_TIME_KEY, 0L);
        if (milliseconds > 0) {
            _endDateTime.setTimeInMillis(milliseconds);
        }

        // Pause
        int pause = savedInstanceState.getInt(_PAUSE_KEY, 0);
        _pause.setText(String.valueOf(pause));

        // Comment
        _comment.setText(savedInstanceState.getString(_COMMENT_KEY, ""));

        updateDateTime();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Zurücksetzen des ersten Focus
        _isFirstFocus = true;

        // Laden der Daten, falls diese nicht weiderhergestellt wurden
        if (_isRestored) {
            return;
        }

        // Laden der Daten, falls diese vorhanden sind
        if (_timeDataId == _NO_DATA) {
            return;
        }

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Listener registrieren
        _startDate.setOnClickListener(new OnDateClicked(START_DATE));
        _startDate.setOnFocusChangeListener(new OnDateClicked(START_DATE));

        _endDate.setOnClickListener(new OnDateClicked(END_DATE));
        _endDate.setOnFocusChangeListener(new OnDateClicked(END_DATE));

        _startTime.setOnClickListener(new OnTimeClicked(START_TIME));
        _startTime.setOnFocusChangeListener(new OnDateClicked(START_TIME));

        _endTime.setOnClickListener(new OnTimeClicked(END_TIME));
        _endTime.setOnFocusChangeListener(new OnTimeClicked(END_TIME));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Listener deregistrieren
        _startDate.setOnClickListener(null);
        _startDate.setOnFocusChangeListener(null);

        _endDate.setOnClickListener(null);
        _endDate.setOnFocusChangeListener(null);

        _startTime.setOnClickListener(null);
        _startTime.setOnFocusChangeListener(null);

        _endTime.setOnClickListener(null);
        _endTime.setOnFocusChangeListener(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Zurück Button in der Action Bar
            case android.R.id.home:
                saveData();
                // Kein break oder return hier, da das Menü von Android noch verarbeitet werden soll

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // Speichern der Daten beim Verlassen der Activity
        saveData();
    }

    private void loadData() {
        // URI für ein Datensatz
        Uri dataUri = ContentUris.withAppendedId(TimeDataContract.TimeData.CONTENT_URI, _timeDataId);

        Cursor data = getContentResolver().query(dataUri,
                null, // Alle Spalten
                null, //Filter
                null, // Filter Argumente
                null); // Sortierung

        try {
            // Prüfen, ob ein Datensatz da ist
            if (!data.moveToFirst()) {
                return;
            }

            // Startzeit
            int columnIndex = data.getColumnIndex(TimeDataContract.TimeData.Columns.START_TIME);
            String startDateTimeString = data.getString(columnIndex);
            try {
                _startDateTime = TimeDataContract.Converter.parse(startDateTimeString);
            } catch (ParseException e) {
                // Datum konnte nicht umgewandelt werden
                e.printStackTrace();
            }

            // Endzeit
            columnIndex = data.getColumnIndex(TimeDataContract.TimeData.Columns.END_TIME);
            if (!data.isNull(columnIndex)) {

                String endDateTimeString = data.getString(columnIndex);
                try {
                    _endDateTime = TimeDataContract.Converter.parse(endDateTimeString);
                } catch (ParseException e) {
                    // Datum konnte nicht umgewandelt werden
                    e.printStackTrace();
                }
            }

            // Auslesen der Pause
            columnIndex = data.getColumnIndex(TimeDataContract.TimeData.Columns.PAUSE);
            int pause = data.getInt(columnIndex);
            _pause.setText(String.valueOf(pause));

            // Aslesen des Kommentares
            columnIndex = data.getColumnIndex(TimeDataContract.TimeData.Columns.COMMENT);
            if (data.isNull(columnIndex)) {
                _comment.setText("");
            } else {
                _comment.setText(data.getString(columnIndex));
            }

            updateDateTime();
        } finally {
            // Ergebnismenge freigeben
            if (data != null) {
                data.close();
            }
        }
    }

    private void updateDateTime() {
        // Ausgabe der Startzeit
        _startDate.setText(_dateFormatter.format(_startDateTime.getTime()));
        _startTime.setText(_timeFormatter.format(_startDateTime.getTime()));

        // Ausgabe der Endzeit
        _endDate.setText(_dateFormatter.format(_endDateTime.getTime()));
        _endTime.setText(_timeFormatter.format(_endDateTime.getTime()));
    }

    private void saveData() {
        // Pause auslesen
        String pauseValue = _pause.getText().toString();
        int pause = 0;
        if (pauseValue != null && !pauseValue.isEmpty()) {
            pause = Integer.parseInt(pauseValue);
        }

        // Spaltenwerte zurodnen
        ContentValues values = new ContentValues();
        values.put(TimeDataContract.TimeData.Columns.START_TIME,
                TimeDataContract.Converter.format(_startDateTime));
        values.put(TimeDataContract.TimeData.Columns.END_TIME,
                TimeDataContract.Converter.format(_endDateTime));
        values.put(TimeDataContract.TimeData.Columns.PAUSE, pause);
        values.put(TimeDataContract.TimeData.Columns.COMMENT,
                _comment.getText().toString());

        // Speichern der neuen Daten
        Uri updateUri = ContentUris.withAppendedId(TimeDataContract.TimeData.CONTENT_URI, _timeDataId);
        getContentResolver().update(updateUri, values, null, null);
    }

    @Override
    public Calendar getDate(String dialogType) {
        switch (dialogType) {
            case IChangeDateTimeListener.START_DATE:
            case IChangeDateTimeListener.START_TIME:
                return _startDateTime;

            case IChangeDateTimeListener.END_DATE:
            case IChangeDateTimeListener.END_TIME:
                return _endDateTime;

            default:
                return null;
        }
    }

    @Override
    public void updateView(String dialogType) {
        updateDateTime();
    }

    public abstract class OnClickedOrFocused implements View.OnClickListener, View.OnFocusChangeListener {
        protected final String _dialogType;

        protected OnClickedOrFocused(String dialogType) {
            _dialogType = dialogType;
        }

        // Methode, die beim Fokuswechsel aufgerufen wird
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            // Erstes Focus verwerfen
            if (_isFirstFocus) {
                _isFirstFocus = false;
                return;
            }

            // Dialog anzeigen, falls Focus gesetzt wurde
            if (hasFocus) {
                showDialog();
            }
        }

        // Methode, die beim Click aufgerufen wird
        @Override
        public void onClick(View v) {
            showDialog();
        }

        // Implementierung, wie der Dialog angezeigt wird
        protected abstract void showDialog();
    }

    public class OnDateClicked extends OnClickedOrFocused {

        public OnDateClicked(String dialogType) {
            super(dialogType);
        }

        // Anzeige des Datumdialoges
        @Override
        protected void showDialog() {
            ChangeDateDialog dialog = new ChangeDateDialog();
            dialog.show(getSupportFragmentManager(), _dialogType);
        }
    }

    public class OnTimeClicked extends OnClickedOrFocused {

        public OnTimeClicked(String dialogType) {
            super(dialogType);
        }

        // Anzeige des Zeitdialoges
        @Override
        protected void showDialog() {
            ChangeTimeDialog dialog = new ChangeTimeDialog();
            dialog.show(getSupportFragmentManager(), _dialogType);
        }
    }
}
