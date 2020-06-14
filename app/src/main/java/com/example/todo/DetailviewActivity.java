package com.example.todo;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import com.example.todo.databinding.ActivityDetailviewBinding;
import com.example.model.ITodoCRUDOperationAsync;
import com.example.model.Todo;
import com.example.view.DetailviewActions;

public class DetailviewActivity extends AppCompatActivity implements DetailviewActions{

    public static final String ARG_ITEM_ID = "itemId";
    private static final int CALL_PICK_CONTACT = 2;
    private static final int REQUEST_CONTACT_PERMISSIONS = 3;
    private ITodoCRUDOperationAsync crudOperation;
    private Todo todo;
    private ViewGroup contactLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityDetailviewBinding bindingMediator = DataBindingUtil.setContentView(this, R.layout.activity_detailview);

        this.crudOperation = ((TodoApplication)getApplication()).getCRUDOperation();

        contactLayout = findViewById(R.id.contactsLayout);

        long todoId = getIntent().getLongExtra(ARG_ITEM_ID, -1);
        if (todoId != -1){
            crudOperation.readTodo(todoId, new ITodoCRUDOperationAsync.ResultCallback<Todo>() {
                @Override
                public void onResult(Todo result) {
                    todo = result;
                    for (String  id : todo.getContacts()){
                        addContactToScrollview(id);
                    }
                    doDatabinding(bindingMediator);
                }
            });
        }
        else {
            this.todo = new Todo();
            doDatabinding(bindingMediator);
        }
    }

    private void doDatabinding(ActivityDetailviewBinding bindingMediator){
        bindingMediator.setTodo(todo);
        bindingMediator.setActions(DetailviewActivity.this);
    }

    public void saveTodo(){

        if (this.todo.getId() == -1){
            crudOperation.createTodo(this.todo, new ITodoCRUDOperationAsync.ResultCallback<Long>() {
                @Override
                public void onResult(Long result) {
                    todo.setId(result);
                    returnToOverview(-1, false);
                }
            });
        }
        else {
            crudOperation.updateTodo(this.todo.getId(), this.todo, new ITodoCRUDOperationAsync.ResultCallback<Boolean>() {
                @Override
                public void onResult(Boolean result) {
                    int overviewListPosition = getIntent().getIntExtra("LIST_POSITION", -1);
                    returnToOverview(overviewListPosition, false);
                }
            });
        }
    }

    public void deleteTodo(){
        if (this.todo.getId() == -1) return;

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == DialogInterface.BUTTON_POSITIVE) {
                    crudOperation.deleteTodo(todo.getId(), new ITodoCRUDOperationAsync.ResultCallback<Boolean>() {
                        @Override
                        public void onResult(Boolean result) {
                            int overviewListPosition = getIntent().getIntExtra("LIST_POSITION", -1);
                            returnToOverview(overviewListPosition, true);
                        }
                    });
                }
                else if (i == DialogInterface.BUTTON_NEGATIVE){
                    Toast.makeText(DetailviewActivity.this, "Todo \"" + todo.getName() +"\" not deleted", Toast.LENGTH_SHORT).show();
                }
            }
        };

        new AlertDialog.Builder(this)
                .setMessage("Delete Todo \"" + todo.getName() + "\"")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    private void returnToOverview(int listPosition, boolean delete){
        Intent returnIntent = new Intent();
        returnIntent.putExtra(ARG_ITEM_ID, this.todo.getId());
        returnIntent.putExtra("DELETE_TODO", delete);

        if (listPosition > -1){
            returnIntent.putExtra("LIST_POSITION", listPosition);
        }
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    public void toggleDone() {
        this.todo.setDone(!this.todo.isDone());
    }

    public void toggleFavourite() {
        this.todo.setFavourite(!this.todo.isFavourite());
    }

    public void setDate(){

        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

                        calendar.set(i, i1 ,i2);

                        new TimePickerDialog(
                                DetailviewActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                        calendar.set(Calendar.HOUR_OF_DAY, i);
                                        calendar.set(Calendar.MINUTE, i1);

                                        todo.setExpiry(calendar.getTimeInMillis());
                                        TextView dateText = findViewById(R.id.todoDateText);
                                        dateText.setText(todo.getDateString());
                                    }
                                },
                                0,
                                0,
                                true
                        ).show();
                    }
                },
                year,
                month,
                day
        ).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detailview_optionsmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.addContact){
            this.pickContact();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void addContactToScrollview(final String id){
        final ViewGroup listitemLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_detailview_contactlistitem, null);
        final TextView contactNameText = listitemLayout.findViewById(R.id.contactName);
        contactNameText.setText(getContactName(id));

        TextView contactMailText = listitemLayout.findViewById(R.id.contactMail);
        contactMailText.setText(getContactEmail(id));

        final TextView contactPhoneText = listitemLayout.findViewById(R.id.contactPhone);
        contactPhoneText.setText(getContactPhone(id));

        listitemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                PopupMenu popup = new PopupMenu(DetailviewActivity.this, contactNameText);
                popup.getMenuInflater().inflate(R.menu.detailview_popupmenu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.contactDelete:
                                deleteContact(id, view);
                                break;
                            case R.id.contactEmail:
                                String email = getContactEmail(id);
                                if (email != null) {
                                    composeEmail(DetailviewActivity.this, "TODO: " + todo.getName(),  " " + "DESCRIPTION: " + todo.getDescription(), "bla@aol.com", email);
                                }
                                else {
                                    Toast.makeText(DetailviewActivity.this,"no email" , Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case R.id.contactSms:
                                String phoneNumber = getContactPhone(id);
                                if (phoneNumber != null) {
                                    composeSMS(phoneNumber, "TODO: " + todo.getName() + " " + "DESCRIPTION: " + todo.getDescription());
                                }
                                else {
                                    Toast.makeText(DetailviewActivity.this,"no mobile phonenumber" , Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        contactLayout.addView(listitemLayout);
    }

    private void deleteContact(String id, View view){
        this.todo.deleteContact(id);
        this.contactLayout.removeView(view);
    }

    private String getContactName(String id){
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Contactables.CONTENT_URI, null, ContactsContract.CommonDataKinds.Contactables.CONTACT_ID+ "=?", new String[]{id}, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
        return null;
    }

    private String getContactPhone(String id){
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id}, null);
        while (cursor.moveToNext()) {
            int currentNumberType =cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA2));
            if (currentNumberType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE){
                return cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
        }
        return null;
    }

    private String getContactEmail(String id){
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?", new String[]{id}, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
        }
        return null;
    }

    private void addContact(Uri contactUri){
        Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
        if (cursor.moveToFirst()){
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

            if (!todo.getContacts().contains(id)){
                todo.addContact(id);
                addContactToScrollview(id);
            }
            else {
                Toast.makeText(DetailviewActivity.this, "Contact already exists", Toast.LENGTH_SHORT).show();
            }
            for (String s : todo.getContacts()){
                Log.i("addcontact",s);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CONTACT_PERMISSIONS) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                pickContact();
            }
        }
    }

    @Override
    public void pickContact() {

        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT_PERMISSIONS);
            Log.i("Detailtview", "no permissions for contacts");
            return;
        }
        else {
            Log.i("Detailtview", "permissions for contacts granted");
            Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(pickContactIntent, CALL_PICK_CONTACT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CALL_PICK_CONTACT){
            if (resultCode == RESULT_OK){
                addContact(data.getData());
            }
        }
    }


    /**
     * compose an sms
     */
    // http://snipt.net/Martin/android-intent-usage/
    protected void composeSMS(String receiver, String message) {

        // the sms compose is identified by the tel: uri and the specified
        // action ACTION_SENDTO
        Uri smsUri = Uri.parse("smsto:" + receiver);
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, smsUri);
        smsIntent.putExtra("sms_body", message);

        startActivity(smsIntent);
    }

    // see
    // http://www.anddev.org/email_send_intent_intentchooser-t3295.html
    public static void composeEmail(Context context, String subject, String body, String sender, String recipients) {

        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients.split(","));
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

        // determine the activity to use for sending
        Intent chosenIntent = Intent.createChooser(emailIntent, "Sending Email...");

        context.startActivity(chosenIntent);
    }
}