package com.example.timerecording;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.database.Cursor;

import com.example.timerecording.db.TimeDataContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

public class TimeTrackingActivity extends AppCompatActivity {
    private EditText _startDateTime;
    private EditText _endDateTime;
    private Button _startCommand;
    private Button _endCommand;

    private final DateFormat _dateTimeFormatter =
            DateFormat.getDateTimeInstance(
                    DateFormat.SHORT, // Datum-Formatierung
                    DateFormat.SHORT); // Uhrzeit-Formatierung

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_tracking);

        // Suchen der Views
        _startDateTime = findViewById(R.id.StartDateTime);
        _endDateTime = findViewById(R.id.EndDateTime);
        _startCommand = findViewById(R.id.StartCommand);
        _endCommand = findViewById(R.id.EndCommand);

        // Tastatureingaben verhindern
        _startDateTime.setKeyListener(null);
        _endDateTime.setKeyListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Listener registrieren
        _startCommand.setOnClickListener(new StartButtonClicked());

        _endCommand.setOnClickListener(new EndButtonClicked());
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Listener deregistrieren
        _startCommand.setOnClickListener(null);
        _endCommand.setOnClickListener(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initFromDb();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_time_tracking, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ListDataMenuItem:
                // Expliziter Intent
                Intent listDataIntent = new Intent(this, ListDataActivity.class);
                startActivity(listDataIntent);
                return true;

            case R.id.InfoMenuItem:
                Intent InfoIntent = new Intent(this, InfoActivity.class);
                startActivity(InfoIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void initFromDb() {
        // Deaktivieren der Buttons
        _startCommand.setEnabled(false);
        _endCommand.setEnabled(false);

        // Laden des offenen Datensatzes, falls vorhanden
        Cursor data = getContentResolver().query(
                TimeDataContract.TimeData.NOT_FINISHED_CONTENT_URI,
                new String[]{TimeDataContract.TimeData.Columns.START_TIME},
                null, // Keine Bedingungen
                null, // Keine Argumente
                null); // Keine Sortierung

        // Prüfen, ob Daten vorhanden sind
        if (data.moveToFirst()) {
            try {
                Calendar startTime = TimeDataContract.Converter.parse(data.getString(0));
                _startDateTime.setText(_dateTimeFormatter.format(startTime.getTime()));
            } catch (ParseException e) {
                // Fehler bei der Konvertierung der Startzeit
                _startDateTime.setText("Falscher Datumsformat in der Datenbank");
            }
            // Beenden Button aktivieren
            _endDateTime.setText("");
            _endCommand.setEnabled(true);
        } else {
            // Start Button aktivieren
            _startDateTime.setText("");
            _endDateTime.setText("");
            _startCommand.setEnabled(true);
        }

        // Schließen, da die Daten nicht weiter benutzt werden
        data.close();
    }

    class StartButtonClicked implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // Startbutton deaktivieren
            _startCommand.setEnabled(false);
            // Aktuelle Zeit
            Calendar currentTime = Calendar.getInstance();
            // Konvertierung für die Datenbank
            String dbTime = TimeDataContract.Converter.format(currentTime);
            // Zeit für Datenbank
            ContentValues values = new ContentValues();
            values.put(TimeDataContract.TimeData.Columns.START_TIME, dbTime);
            // In die Datenbank speichern
            getContentResolver().insert(TimeDataContract.TimeData.CONTENT_URI, values);
            // Ausgabe für UI
            _startDateTime.setText(_dateTimeFormatter.format(currentTime.getTime()));
            // Beeenden Button aktivieren
            _endCommand.setEnabled(true);
        }
    }

    class EndButtonClicked implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // End Button deaktivieren
            _endCommand.setEnabled(false);
            // Aktuelle Zeit
            Calendar currentTime = Calendar.getInstance();
            // Konvertierung für die Datenbank
            String dbTime = TimeDataContract.Converter.format(currentTime);
            // Zeit für Datenbank
            ContentValues values = new ContentValues();
            values.put(TimeDataContract.TimeData.Columns.END_TIME, dbTime);
            // In die Datenbank speichern
            getContentResolver().update(TimeDataContract.TimeData.NOT_FINISHED_CONTENT_URI, values, null, null);
            // Ausgabe für UI
            _endDateTime.setText(_dateTimeFormatter.format(currentTime.getTime()));
            // Start Button aktivieren
            _startCommand.setEnabled(true);
        }
    }
}
