package com.example.timerecording.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;

import com.example.timerecording.R;
import com.example.timerecording.db.TimeDataContract;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CsvExporter extends AsyncTask<Void, Integer, Void> {
    private final Context _context;
    private ProgressDialog _dialog = null;

    public CsvExporter(Context context) {
        _context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Dialog initialisieren
        _dialog = new ProgressDialog(_context);
        // Dialog Title
        _dialog.setTitle(R.string.DialogTitleExport);
        // Dialog Text
        _dialog.setMessage(_context.getString(R.string.DialogMessageExport));
        // Schließen durch "daneben" Tippen, verhindern
        _dialog.setCanceledOnTouchOutside(false);
        // Abbrechen durch den Zurückbutton
        _dialog.setCancelable(true);
        // Typ des Dialoges festlegen (Allgemein oder Fortschritt)
        _dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // Abbrechen Button hinzufügen
        _dialog.setButton(Dialog.BUTTON_NEGATIVE,
                _context.getString(R.string.ButtonCancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // AsncTask mitteielen, dass die Aktion abgebrochen werden soll
                        cancel(false);
                    }
                });

        // Dialog anzeigen
        _dialog.show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        // Prüfen, ob Dialog angezeigt wird
        if (_dialog != null && _dialog.isShowing()) {
            // Schließen des Dialoges
            _dialog.dismiss();
            _dialog = null;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        // Prüfen auf den Inhalt
        if (_dialog != null && values != null && values.length == 1) {
            // Weitergabe des aktuellen Standes an das Dialog
            _dialog.setProgress(values[0]);
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Cursor data = null;

        try {
            // Daten über Content Provider abfragen
            data = _context.getContentResolver()
                    .query(TimeDataContract.TimeData.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

            int dataCount = data == null ? 0 : data.getCount();

            if (dataCount == 0 || isCancelled()) {
                // Nichts weiter machen, wenn keine Daten vorhanden
                return null;
            }

            // Maximalen Wert für den Dialog setzen
            if (_dialog != null) {
                _dialog.setMax(dataCount + 1); // +1 für die Spaltenzeile in CSV
            }

            // Ordner für externe Daten
            File externalStorage = Environment.getExternalStorageDirectory();

            // Prüfen, ob externe Daten geschrieben werden können (SD Karte nur Read Only oder voll)
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                return null;
            }

            // Unterordner für unser Export
            File exportPath = new File(externalStorage, "export");

            // Dateiname für Export
            File exportFile = new File(exportPath, "TimeDataLog.csv");

            // Erzeugen der ordner, falls noch nicht vorhanden
            if (!exportFile.exists()) {
                exportPath.mkdirs();
            }

            // Klasse zum Schreiben der Daten
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(exportFile));

                // Asulesen der Spaltennamen
                String[] columnList = data.getColumnNames();

                StringBuilder line = new StringBuilder();

                // Befüllen der ersten Zeile mit Spaltennamen
                for (String columnName : columnList) {
                    if (line.length() > 0) {
                        line.append(';');
                    }

                    line.append(columnName);
                }

                writer.append(line);

                // Zeilen mit Daten ausgeben
                while (data.moveToNext() && !isCancelled()) {
                    // Neue Zeile
                    writer.newLine();

                    // Zeilenvariable leeren
                    line.delete(0, line.length());

                    // Ausgabe aller Spaltenwerte
                    for (int columnIndex = 0; columnIndex < columnList.length; columnIndex++) {
                        if (line.length() > 0) {
                            line.append(';');
                        }

                        // Prüfen auf NULL (Datenbank) des Spalteninhaltes
                        if (data.isNull(columnIndex)) {
                            line.append("<NULL>");
                        } else {
                            line.append(data.getString(columnIndex));
                        }
                    }

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    writer.append(line);

                    // Fortschritt melden
                    publishProgress(data.getPosition() + 2); // +1 für '0' basierte Position + 1 für Überschriften
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (writer != null) {
                        writer.flush();

                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Datei löschen, falls Benutzerabbruch
                if (isCancelled() && exportFile.exists()) {
                    exportFile.delete();
                }
            }

            return null;
        } finally {
            if (data != null) {
                data.close();
            }
        }
    }
}
