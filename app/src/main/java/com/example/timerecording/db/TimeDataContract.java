package com.example.timerecording.db;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class TimeDataContract {
    /**
     * Eindeutiger Name des Providers innerhalb des Betriebssystems
     */
    public static final String AUTHORITY = "com.example.timerecording.provider";

    /**
     * Basis URI zu dem Content Provider
     */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * Kontrakt für zeiten
     */
    public static final class TimeData {
        /**
         * Unterverzeichnis für die Dateb
         */
        public static final String CONTENT_DIRECTORY = "time";

        /**
         * Unterverzeichnis für offenen Datensatz
         */
        public static final String NOT_FINISHED_CONTENT_DIRECTORY =
                CONTENT_DIRECTORY + "/not_finished";

        /**
         * URI zu den Daten
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_DIRECTORY);

        /**
         * Uri zu dem offenen Datensatz
         */
        public static final Uri NOT_FINISHED_CONTENT_URI =
                Uri.withAppendedPath(AUTHORITY_URI, NOT_FINISHED_CONTENT_DIRECTORY);

        /**
         * Datentyp für die Auflistung der Daten
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_DIRECTORY;

        /**
         * Datentyp für einen einzelnen Datensatz
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_DIRECTORY;

        /**
         * Verfügbare Splaten
         */
        public interface Columns extends BaseColumns {
            /**
             * Start der Aufzeichnung in ISO-8601 Format (z.B.: 2016-11-23T17:34)
             */
            String START_TIME = "start_time";

            /**
             * Ende der Aufzeichnung in ISO-8601 Format (z.B.: 2016-11-23T17:34)
             */
            String END_TIME = "end_time";

            /**
             * Pausendauer in Minuten (Integer, Standard: 0
             */
            String PAUSE = "pause";

            /**
             * Kommentar zu der Aufzeichnung
             */
            String COMMENT = "comment";
        }
    }

    /**
     * Datenkonverter
     */
    public static final class Converter {
        private static final String _ISO_8601_PATTERN = "yyyy-MM-dd'T'HH:mm";

        /**
         * Standatd Formatter für Datum und Uhrzeit in der Datenbank (ISO 8601)
         */
        public static final DateFormat DB_DATE_TIME_FORMATTER =
                new SimpleDateFormat(_ISO_8601_PATTERN, Locale.GERMANY);

        /**
         * Parse Datum und Uhrzeit aus der Datenbank als Calenar
         *
         * @param dbTime Datum aus der Datenbank
         * @return neues Calendar objekt mit der Zeit aus der Datenbank
         * @throws ParseException
         */
        public static Calendar parse(String dbTime) throws ParseException {
            Calendar date = Calendar.getInstance();
            date.setTime(DB_DATE_TIME_FORMATTER.parse(dbTime));
            return date;
        }

        /**
         * Formatieren des Datums in String für die Datenbank
         *
         * @param dateTime Datum und Uhrzeit
         * @return Datum in ISO 8601 Format
         */
        public static String format(Calendar dateTime) {
            return DB_DATE_TIME_FORMATTER.format(dateTime.getTime());
        }
    }
}
