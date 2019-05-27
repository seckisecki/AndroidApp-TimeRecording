package com.example.timerecording;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.timerecording.adapter.TimeDataAdapter;

import com.example.timerecording. db.TimeDataContract;
import com.example.timerecording.dialogs.DeleteTimeDataDialog;
import com.example.timerecording.dialogs.IConfirmDeleteListener;
import com.example.timerecording.dialogs.IDeleteItemListener;
import com.example.timerecording.dialogs.IItemActionListener;
import com.example.timerecording.services.CsvExportService;


public class ListDataActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, IItemActionListener, IConfirmDeleteListener {
    private static final int _LOADER_ID = 100;
    private TimeDataAdapter _adapter = null;
    private RecyclerView _list = null;

    // ID für SAF Dateiabfrage
    private final static int _SAF_CREATE_EXPORT_FILE = 200;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);

        // Views suchen
        _list = findViewById(R.id.DataList);
        _list.setLayoutManager(new LinearLayoutManager(this));
        _list.setHasFixedSize(true);

        // Adpater initialisieren
        _adapter = new TimeDataAdapter(this, null);
        _list.setAdapter(_adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportLoaderManager().restartLoader(_LOADER_ID, null, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getSupportLoaderManager().destroyLoader(_LOADER_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_data, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ExportMenuItem:
                selectFileForExport();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == _SAF_CREATE_EXPORT_FILE) {
            // Pfad zur Datei (als Content Provider URI)
            Uri fileUri = data.getData();
            exportData(fileUri);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void exportData(Uri fileUri) {
        Intent exportService = new Intent(this, CsvExportService.class);
        // Datei URI an den Service übergeben
        exportService.setData(fileUri);
        startService(exportService);
    }

    private void selectFileForExport() {
        // Aktion, eine neue Datei anzulegen
        Intent fileIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        // Kategorie, um diese Datei dann auch öffnen zu können
        fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        // Datentyp (Mime Type) setzen, als Filter
        fileIntent.setType("text/csv");
        // Vorschlag für den Namen der Datei
        fileIntent.putExtra(Intent.EXTRA_TITLE, "export.csv");
        // Starten des Intents
        startActivityForResult(fileIntent, _SAF_CREATE_EXPORT_FILE);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle bundle) {
        CursorLoader loader = null;

        // Unterscheidung zwischen den Loadern
        switch (loaderId) {
            case _LOADER_ID:
                loader = new CursorLoader(
                        this, // Context
                        TimeDataContract.TimeData.CONTENT_URI, // Daten-URI
                        null, // alle Spalten
                        null, //Filter
                        null, // Filter Argumente
                        TimeDataContract.TimeData.Columns.START_TIME + " DESC" // Sortierung
                );
                break;
        }

        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        // Unterscheidung zwischen den Loadern
        switch (loader.getId()) {
            case _LOADER_ID:
                _adapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Unterscheidung zwischen den Loadern
        switch (loader.getId()) {
            case _LOADER_ID:
                _adapter.swapCursor(null); // Daten freigeben
                break;
        }
    }

    @Override
    public void deleteItem(long id, int position) {
        // Uri zum löschen erzeugen
        Uri deleteUri = ContentUris.withAppendedId(TimeDataContract.TimeData.CONTENT_URI, id);
        // Löschen
        getContentResolver().delete(deleteUri, null, null);
        // Benachrichtigen, dass ein Datensatz gelöscht wurde (Recycler View)
        _adapter.notifyItemRemoved(position);
    }

    @Override
    public void editItem(long id, int position) {
        Intent editIntent = new Intent(this, EditDataActivity.class);
        // ID des Datensatzen übergeben
        editIntent.putExtra(EditDataActivity.ID_KEY, id);
        startActivity(editIntent);
    }

    @Override
    public void confirmDelete(long id, int position) {
        // Argumente initialisieren
        Bundle arguments = new Bundle();
        arguments.putLong(DeleteTimeDataDialog.ID_KEY, id);
        arguments.putInt(DeleteTimeDataDialog.POSITION_KEY, position);

        // Dialog initialisieren
        DeleteTimeDataDialog dialog = new DeleteTimeDataDialog();
        dialog.setArguments(arguments);

        // Dialog anzeigen
        dialog.show(getSupportFragmentManager(), "DeleteDialog");
    }
}
