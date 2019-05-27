package com.example.timerecording.db;

import android.database.sqlite.SQLiteDatabase;
import android.icu.text.CurrencyPluralInfo;

import static com.example.timerecording.db.TimeDataContract.TimeData.Columns;

final class TimeDataTable {
    /**
     * ID für eine Auflistung
     */
    public static final int ITEM_LIST_ID = 100;

    /**
     * ID für einen Datensatz
     */
    public static final int ITEM_ID = 101;

    /**
     * ID für einen Datensatz, das noch nicht beendet wurde
     */
    public static final int NOT_FINISHED_ITEM_ID = 102;

    /**
     * Name der Tabelle
     */
    public static final String TABLE_NAME = "time_data";

    /**
     * Skript für die Erzeugung der Tabelle
     */
    private static final String _CREATE_TABLE =
            "CREATE TABLE \"" + TABLE_NAME
                    + "\" (`" + Columns._ID + "` INTEGER PRIMARY KEY AUTOINCREMENT,"
            + " `" + Columns.START_TIME + "` TEXT NOT NULL,"
            + " `" + Columns.END_TIME + "` TEXT,"
            + " `" + Columns.PAUSE + "` INTEGER NOT NULL DEFAULT 0,"
            + " `" + Columns.COMMENT + "` TEXT )";

    private static final String _MIGRATION_1_TO_2 =
            "ALTER TABLE \"" + TABLE_NAME + "\" "
                    + "ADD COLUMN `" + Columns.PAUSE + "` INTEGER NOT NULL DEFAULT 0";

    private static final String _MIGRATION_2_TO_3 =
            "ALTER TABLE \"" + TABLE_NAME + "\" "
                    + "ADD COLUMN `" + Columns.COMMENT + "` TEXT";

    static void createTable(SQLiteDatabase db) {
        db.execSQL(_CREATE_TABLE);
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion) {
        switch (oldVersion) {
            case 1:
                // Migration von Version 1 auf 2
                db.execSQL(_MIGRATION_1_TO_2);

            case 2:
                // Migration von Version 2 auf 3
                db.execSQL(_MIGRATION_2_TO_3);
        }
    }
}
