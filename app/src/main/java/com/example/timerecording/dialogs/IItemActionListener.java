package com.example.timerecording.dialogs;

public interface IItemActionListener {
    void deleteItem(long id, int position);
    void editItem(long id, int position);
}
