package com.example.todo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.snackbar.Snackbar;

import com.example.todo.R;
import com.example.todo.databinding.ActivityDetailviewBinding;

import com.example.model.ToDo;
import com.example.todo.R;

public class DetailviewActivity extends AppCompatActivity {

    public static final String ARG_ITEM = "item";
    public static final int CALL_CONTACT_PICKER = 0;

    private ToDo item;
    private ActivityDetailviewBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detailview);

        this.item = (ToDo)getIntent().getSerializableExtra( ARG_ITEM );
        if (this.item == null) {
            this.item = new ToDo( );
        }

        binding.setController( this );

    }

    public void onSaveItem(View view) {
        Intent returnData = new Intent();

        returnData.putExtra( ARG_ITEM,this.item);

        this.setResult( Activity.RESULT_OK,returnData );
        finish();
    }

    public ToDo getItem() {
        return item;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addContact:selectAndAddContact();return true;
            case R.id.nochwas:
                Toast.makeText(this,
                        "Nochwas wurde ausgewählt",
                        Toast.LENGTH_SHORT).show();return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectAndAddContact() {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(pickContactIntent, CALL_CONTACT_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CALL_CONTACT_PICKER && resultCode == Activity.RESULT_OK) {
            addSelectedContactToContacts(data.getData());
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addSelectedContactToContacts(Uri contactId) {

        Cursor cursor = getContentResolver().query(contactId, null, null, null, null);
        if (cursor.moveToFirst()) {
            String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String internalContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

            showFeedbackMessage("Kontakt übernommen: " + contactName + ", ID: " + internalContactId);

        }
    }

    private void showFeedbackMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}