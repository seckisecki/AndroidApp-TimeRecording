package com.example.timerecording.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;

public class TimeDataProvider extends ContentProvider {
    private static final UriMatcher _URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // Lookup für die Auflistung
        _URI_MATCHER.addURI(
                TimeDataContract.AUTHORITY, // Basis-Uri
                TimeDataContract.TimeData.CONTENT_DIRECTORY, // Unterverzeichnis der Daten
                TimeDataTable.ITEM_LIST_ID); // Eindeutige ID

        // Lookup für ein Datensatz
        _URI_MATCHER.addURI(
                TimeDataContract.AUTHORITY, // Basis-Uri
                TimeDataContract.TimeData.CONTENT_DIRECTORY + "/#", // Unterverzeichnis mit ID des Datensatzes
                TimeDataTable.ITEM_ID); // Eindeutige ID

        // Lookup für den offenen Datensatz
        _URI_MATCHER.addURI(
                TimeDataContract.AUTHORITY, // Basis Uri
                TimeDataContract.TimeData.NOT_FINISHED_CONTENT_DIRECTORY, // Unterverzeichnis
                TimeDataTable.NOT_FINISHED_ITEM_ID); // ID für offenen Datensatz
    }

    private DbHelper _dbHelper = null;
    private static final String _ID_WHERE = BaseColumns._ID + "=?";
    private static final String _NOT_FINISHED_WHERE = "IFNULL("
            + TimeDataContract.TimeData.Columns.END_TIME
            + ",'')=''";

    @Override
    public boolean onCreate() {
        _dbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final int uriType = _URI_MATCHER.match(uri);
        Cursor data;
        SQLiteDatabase db = _dbHelper.getReadableDatabase();

        switch (uriType) {
            case TimeDataTable.ITEM_LIST_ID:
                data = db.query(TimeDataTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case TimeDataTable.ITEM_ID:
                final long id = ContentUris.parseId(uri);
                data = db.query(TimeDataTable.TABLE_NAME, projection, _ID_WHERE, idAsArray(id), null, null, null);
                break;

            case TimeDataTable.NOT_FINISHED_ITEM_ID:
                data = db.query(TimeDataTable.TABLE_NAME, projection, _NOT_FINISHED_WHERE, null, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException(String.format(Locale.GERMANY, "Unbekannte URI: %s", uri));
        }

        if (data != null) {
            data.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return data;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // Auflösen der Uri
        final int uriType = _URI_MATCHER.match(uri);
        String type = null;

        // Bestimmen des Datentyps
        switch (uriType) {
            case TimeDataTable.ITEM_LIST_ID:
                type = TimeDataContract.TimeData.CONTENT_TYPE;
                break;

            case TimeDataTable.ITEM_ID:
            case TimeDataTable.NOT_FINISHED_ITEM_ID:
                type = TimeDataContract.TimeData.CONTENT_ITEM_TYPE;
                break;
        }

        return type;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // Auflösen der Uri
        final int uriType = _URI_MATCHER.match(uri);

        // Initialisieren der Werte
        Uri insertUri = null;
        long newItemId = -1;

        // Bestimmen der Aktionen für die gefundene Uri
        switch (uriType) {
            case TimeDataTable.ITEM_LIST_ID:
            case TimeDataTable.ITEM_ID:
            case TimeDataTable.NOT_FINISHED_ITEM_ID:
                SQLiteDatabase db = _dbHelper.getWritableDatabase();
                newItemId = db.insert(TimeDataTable.TABLE_NAME, null, values);
                db.close();
                break;

            default:
                // Ausnahme erzeugen, da wir die Uri nicht kennen
                throw new IllegalArgumentException(String.format(Locale.GERMANY, "Unbekannte URI: %s", uri));
        }

        // Datensatz erfogreich hinzugefügt
        if (newItemId > 0) {
            // Erstellen der Uri
            insertUri = ContentUris.withAppendedId(
                    TimeDataContract.TimeData.CONTENT_URI, // Basis-Uri für die Daten
                    newItemId); // ID des Datensatzes

            // Benachrichtigen über die Änderungen der Daten
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return insertUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int uriType = _URI_MATCHER.match(uri);
        int deletedItems = 0;
        SQLiteDatabase db = _dbHelper.getWritableDatabase();

        // Bestimmen der Aktionen, abhängig vom Typ
        switch (uriType) {
            case TimeDataTable.ITEM_LIST_ID:
                deletedItems = db.delete(TimeDataTable.TABLE_NAME, selection, selectionArgs);
                // Datenbak freigeben
                db.close();
                break;

            case TimeDataTable.ITEM_ID:
                final long id = ContentUris.parseId(uri);
                deletedItems = db.delete(TimeDataTable.TABLE_NAME, _ID_WHERE, idAsArray(id));
                // Datenbak freigeben
                db.close();
                break;

            case TimeDataTable.NOT_FINISHED_ITEM_ID:
                deletedItems = db.delete(TimeDataTable.TABLE_NAME, _NOT_FINISHED_WHERE, null);
                db.close();
                break;

            default:
                // Ausnahme erzeugen, da wir die Uri nicht kennen
                throw new IllegalArgumentException(String.format(Locale.GERMANY, "Unbekannte URI: %s", uri));
        }

        // Datensätze erfolgreich gelöscht
        if (deletedItems > 0) {
            // Benachrichtigung über die Änderungen der Daten
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deletedItems;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int uriType = _URI_MATCHER.match(uri);
        int updateItems = 0;
        SQLiteDatabase db = _dbHelper.getWritableDatabase();

        switch (uriType) {
            case TimeDataTable.ITEM_LIST_ID:
                updateItems = db.update(TimeDataTable.TABLE_NAME, values, selection, selectionArgs);
                db.close();
                break;

            case TimeDataTable.ITEM_ID:
                final long id = ContentUris.parseId(uri);
                updateItems = db.update(TimeDataTable.TABLE_NAME, values, _ID_WHERE, idAsArray(id));
                db.close();
                break;

            case TimeDataTable.NOT_FINISHED_ITEM_ID:
                updateItems = db.update(TimeDataTable.TABLE_NAME, values, _NOT_FINISHED_WHERE, null);
                db.close();
                break;

            default:
                throw new IllegalArgumentException(String.format(Locale.GERMANY, "Unbekannte URI: %s", uri));
        }

        if (updateItems > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updateItems;
    }

    /**
     * Konvertierung einer long ID in ein String Array
     *
     * @param id ID
     * @return ID als String Array
     */
    private String[] idAsArray(long id) {
        return new String[]{String.valueOf(id)};
    }
}
