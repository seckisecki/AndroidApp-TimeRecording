package com.example.timerecording.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import com.example.timerecording.R;
import com.example.timerecording.db.TimeDataContract;
import com.example.timerecording.dialogs.DeleteTimeDataDialog;
import com.example.timerecording.dialogs.IConfirmDeleteListener;
import com.example.timerecording.dialogs.IItemActionListener;

public class TimeDataAdapter extends RecyclerView.Adapter<TimeDataAdapter.TimeDataViewHolder> {
    private final Context _context;
    private Cursor _data = null;
    private DateFormat _dateTimeFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public TimeDataAdapter(Context context, Cursor data) {
        _context = context;
        _data = data;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public TimeDataViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(_context);
        View view = inflater.inflate(R.layout.item_time_data, viewGroup, false);
        return new TimeDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TimeDataViewHolder timeDataViewHolder, int position) {
        // Keine daten vorhanden
        if (_data == null) {
            return;
        }

        // Keine Daten an der Position
        if (!_data.moveToPosition(position)) {
            return;
        }

        // Daten auslesen
        int columnIndex =
                _data.getColumnIndex(TimeDataContract.TimeData.Columns.START_TIME);
        String startTimeString =
                _data.getString(columnIndex);

        // Konvertieren und formatiren
        try {
            Calendar start = TimeDataContract.Converter.parse(startTimeString);
            startTimeString = _dateTimeFormatter.format(start.getTime());
        } catch (ParseException e) {
            // Fehler bei Konvertierung
            startTimeString = "PARSE ERROR";
        }

        String endTimeString = "---";
        columnIndex =
                _data.getColumnIndex(TimeDataContract.TimeData.Columns.END_TIME);
        if (!_data.isNull(columnIndex)) {
            endTimeString = _data.getString(columnIndex);

            // Konvertieren und formatiren
            try {
                Calendar end = TimeDataContract.Converter.parse(endTimeString);
                endTimeString = _dateTimeFormatter.format(end.getTime());
            } catch (ParseException e) {
                // Fehler bei Konvertierung
                endTimeString = "PARSE ERROR";
            }
        }

        // Daten ins View schreiben
        timeDataViewHolder.StartTime.setText(startTimeString);
        timeDataViewHolder.EndTime.setText(endTimeString);

        // Menü erstellen
        timeDataViewHolder.MoreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(_context, timeDataViewHolder.MoreIcon);
                // Layout des Menüs entfalten (deserialisieren)
                menu.inflate(R.menu.ctx_menu_data_list);
                // Listener setzen
                menu.setOnMenuItemClickListener(new OnMenuItemClicked(timeDataViewHolder.getItemId(), timeDataViewHolder.getAdapterPosition()));
                // Anzeigen
                menu.show();
            }
        });

        // Barbeiten auf Klick
        timeDataViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_context instanceof IItemActionListener) {
                    ((IItemActionListener) _context).editItem(timeDataViewHolder.getItemId(), timeDataViewHolder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (_data == null) {
            return 0;
        }

        return _data.getCount();
    }

    @Override
    public long getItemId(int position) {
        if (_data == null) {
            return -1L;
        }

        if (_data.moveToPosition(position)) {
            return _data.getLong(_data.getColumnIndex(BaseColumns._ID));
        }

        return -1L;
    }

    public void swapCursor(Cursor newData) {
        if (_data != null) {
            // Cursor schließen
            _data.close();
        }

        _data = newData;
        notifyDataSetChanged();
    }

    class OnMenuItemClicked implements PopupMenu.OnMenuItemClickListener {
        private final long _id;
        private final int _position;

        OnMenuItemClicked(long id, int position) {
            _id = id;
            _position = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.MenuItemDelete:
                    deleteItem(_id, _position);
                    return true;

                case R.id.MenuItemEdit:
                    editItem(_id, _position);
                    return true;

                default:
                    return false;
            }
        }

        private void editItem(long id, int position) {
            if (_context instanceof IItemActionListener) {
                ((IItemActionListener) _context).editItem(id, position);
            }
        }

        private void deleteItem(long id, int position) {
            if (_context instanceof IConfirmDeleteListener) {
                ((IConfirmDeleteListener) _context).confirmDelete(id, position);
            }
        }
    }

    class TimeDataViewHolder extends RecyclerView.ViewHolder {
        final TextView StartTime;
        final TextView EndTime;
        final TextView MoreIcon;

        TimeDataViewHolder(@NonNull final View itemView) {
            super(itemView);

            // Views suchen
            StartTime = itemView.findViewById(R.id.StartTimeValue);
            EndTime = itemView.findViewById(R.id.EndTimeValue);
            MoreIcon = itemView.findViewById(R.id.MoreIconText);
        }
    }
}