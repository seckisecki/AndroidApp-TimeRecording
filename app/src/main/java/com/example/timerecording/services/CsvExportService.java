package com.example.timerecording.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.app.NotificationChannel;

import com.example.timerecording.R;
import com.example.timerecording.db.TimeDataContract;

public class CsvExportService extends IntentService {
    private static final String _NOTIFICATION_CHANNEL = "Export";
    private static final int _NOTIFICATION_ID = 500;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public CsvExportService() {
        super("CSVExporter");
    }


    private void createChannel() {
        // Versionsweiche
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // OS Service für Benachrichtigungen holen
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // Gruppe definieren
            NotificationChannel channel = new NotificationChannel(
                    _NOTIFICATION_CHANNEL, // Eindeutiger Name der Gruppe
                    getString(R.string.ExportNotificationChannel), // Titel der gruppe
                    NotificationManager.IMPORTANCE_DEFAULT); // Wichtigkeit
            // Beschreibung für die Grußße
            channel.setDescription(getString(R.string.ExportNotificationChannelDescription));
            // Sichtbarkeit der Gruppe auf dem Sperrbildschirm
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
            // Gruppe erzeugen
            manager.createNotificationChannel(channel);
        }
    }

    private NotificationCompat.Builder createNotification() {
        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), _NOTIFICATION_CHANNEL)
                .setContentTitle(getString(R.string.ExportNotificationTitle))
                .setContentText(getString(R.string.ExportNotificationMessage))
                .setSmallIcon(R.drawable.ic_file_download)
                .setAutoCancel(true);

        return builder;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //Datei für Export auslesen
        Uri exportFile = intent.getData();
        //Falls Dateiname nicht gesetzt ist, kein Export
        if(exportFile == null){
            return;
        }
        // System Service für Benachrichtigungen abfragen
        NotificationManagerCompat notifyManager = NotificationManagerCompat.from(getApplicationContext());
        // Gruppe anlegen
        createChannel();
        // Benachrichtigung vorfüllen
        NotificationCompat.Builder builder = createNotification();

        Cursor data = null;

        try {
            // Daten über Content Provider abfragen
            data = getBaseContext().getContentResolver()
                    .query(TimeDataContract.TimeData.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

            int dataCount = data == null ? 0 : data.getCount();

            if (dataCount == 0) {
                // Nichts weiter machen, wenn keine Daten vorhanden
                return;
            }

            // Export starten, mit richtigen Max-Wert
            builder.setProgress(dataCount + 1, 0, false);
            // Benachrichtigung veröffentlichen
            notifyManager.notify(_NOTIFICATION_ID, builder.build());


            // Klasse zum Schreiben der Daten
            BufferedWriter writer = null;
            // Stream der Datei
            OutputStream os = null;
            try {
                // Initialisierung des Streams zum Schreiben aus dem Content Provider
                os = getContentResolver().openOutputStream(exportFile);

                writer = new BufferedWriter(new OutputStreamWriter(os));

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

                // Ausgabe der Spaltennamen
                builder.setProgress(dataCount + 1, 1, false);
                notifyManager.notify(_NOTIFICATION_ID, builder.build());

                // Zeilen mit Daten ausgeben
                while (data.moveToNext()) {
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

                    // Ausgabe der Zeilen.
                    builder.setProgress(dataCount + 1, data.getPosition() + 2, false);
                    notifyManager.notify(_NOTIFICATION_ID, builder.build());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (writer != null) {
                        writer.flush();

                        writer.close();
                    }

                    if (os != null){
                        os.flush();
                        os.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            if (data != null) {
                data.close();
            }

            // Export abgeschlossen
            builder.setProgress(0, 0, false)
                    .setContentText(getString(R.string.ExportNotificationFinischMessage));
            notifyManager.notify(_NOTIFICATION_ID, builder.build());
        }
    }
}
