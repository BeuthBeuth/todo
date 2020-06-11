package com.example.todo;

import android.app.Activity;

import android.content.Intent;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.model.ToDo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import com.example.model.IToDoCRUDOperations;

import com.example.model.RetrofitToDoCRUDOperationsImpl;
import com.example.todo.databinding.ActivityMainListitemBinding;
import com.example.tasks.CreateToDoTask;
import com.example.tasks.ReadAllToDosTask;
import com.example.tasks.UpdateToDoTaskWithFuture;


public class MainActivity extends AppCompatActivity {

    private static final String logger = "MainActivity";

    public static final int CALL_DETAILVIEW_FOR_NEW_ITEM = 0;

    public static final int CALL_DETAILVIEW_FOR_EXISTING_ITEM = 1;

    private ViewGroup listView;

    private ArrayAdapter<ToDo> listViewAdapter;

    private FloatingActionButton fab;

    private ProgressBar progressBar;

    private IToDoCRUDOperations crudOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((ToDoApplication) getApplication())
                .verifyWebappAvailable(available -> {
                    this.initialiseView();
                }
        );

    }

    private void initialiseView() {

        this.crudOperations = ((ToDoApplication)getApplication()).getCrudOperations();//new RetrofitToDoCRUDOperationsImpl();//new RoomToDoCRUDOperationsImpl(this); //SimpleDataItemCRUDOperationsImpl();

        this.listView = this.findViewById( R.id.listView );
        this.fab = this.findViewById( R.id.fab );
        this.progressBar = findViewById(R.id.progressBar);

        this.listViewAdapter = new ArrayAdapter<ToDo>( this, R.layout.activity_main_listitem, R.id.itemName ) {

            @NonNull
            @Override
            public View getView(int position, @Nullable View existingView, @NonNull ViewGroup parent) {
                Log.i(logger, "using existingView for position: " + position + ": "+  existingView);

                ActivityMainListitemBinding binding = null;
                View currentView = null;

                if (existingView != null) {
                    currentView = existingView;
                    binding = (ActivityMainListitemBinding)existingView.getTag();
                }
                else {
                    binding = DataBindingUtil.inflate( getLayoutInflater(),R.layout.activity_main_listitem,null,false );
                    currentView = binding.getRoot();
                    currentView.setTag(binding);
                }

                ToDo item = getItem( position );
                binding.setItem( item );
                binding.setController(MainActivity.this);

                return currentView;
            }

        };

        ((ListView)this.listView).setAdapter(this.listViewAdapter);

        ((ListView)this.listView).setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ToDo item = listViewAdapter.getItem( i );
                onListitemSelected(item);

            }
        } );

        this.fab.setOnClickListener( (view) -> {
            this.onAddNewListitem();
        } );

        new ReadAllToDosTask(progressBar,
                crudOperations,
                items -> listViewAdapter.addAll(items)
        ).execute();
    }

    private void onListitemSelected(ToDo item) {

        Intent callDetailviewIntent = new Intent(this, DetailviewActivity.class);
        callDetailviewIntent.putExtra( DetailviewActivity.ARG_ITEM, item );
        startActivityForResult( callDetailviewIntent, CALL_DETAILVIEW_FOR_EXISTING_ITEM );

    }

    private void onAddNewListitem() {

        Intent callDetailviewIntentForReturnValue = new Intent(this, DetailviewActivity.class);
        startActivityForResult( callDetailviewIntentForReturnValue, CALL_DETAILVIEW_FOR_NEW_ITEM );

    }

    private void createItemAndAddItToList(ToDo item) {
        new CreateToDoTask(
                progressBar,
                crudOperations,
                createdItem -> {
                    this.listViewAdapter.add(createdItem);
                    ((ListView)this.listView).smoothScrollToPosition(
                            this.listViewAdapter.getPosition(createdItem));
                }
        ).execute(item);
    }

    private void updateToDoandUpdateList(ToDo changedItem) {

//        // Variante A
//        new UpdateToDoTask(this.crudOperations, updated -> {
//            handleResultFromUpdateTask(changedItem, updated);
//        }).execute(changedItem);

        //Variante B
        new UpdateToDoTaskWithFuture(this, this.crudOperations)
                .execute(changedItem)
                .thenAccept(updated -> {
                    handleResultFromUpdateTask(changedItem, updated);
                });

    }

    private void handleResultFromUpdateTask(ToDo changedItem, boolean updated) {
        if (updated) {
            int existingItemInListPos = this.listViewAdapter.getPosition(changedItem);
            if (existingItemInListPos > -1) {
                ToDo existingItem = this.listViewAdapter.getItem(existingItemInListPos);
                existingItem.setName(changedItem.getName());
                existingItem.setDone(changedItem.isDone());
                existingItem.setDescription(changedItem.getDescription());
                existingItem.setContacts(changedItem.getContacts());
                this.listViewAdapter.notifyDataSetChanged();
            }
            else {
                showFeedbackMessage("Aktualisiert: " + changedItem.getName() + " - Kann laufende Nummer nicht finden! ");
            }
        }
        else {
            showFeedbackMessage("Aktualisiert: " + changedItem.getName() + " - Das ToDo konnte nicht in der Datenbank aktualisiert werden ");
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CALL_DETAILVIEW_FOR_NEW_ITEM) {
            if (resultCode == Activity.RESULT_OK) {
                ToDo item = (ToDo)data.getSerializableExtra( DetailviewActivity.ARG_ITEM );
//                showFeedbackMessage( "got new item: " + item);
                createItemAndAddItToList( item );
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                showFeedbackMessage( "cancelled." );
            }
            else {
                showFeedbackMessage( "no item name received and no cancellation." );
            }
        }
        else if (requestCode == CALL_DETAILVIEW_FOR_EXISTING_ITEM) {
            if (resultCode == Activity.RESULT_OK) {
                ToDo item = (ToDo)data.getSerializableExtra(DetailviewActivity.ARG_ITEM);
                updateToDoandUpdateList(item);
            }
        }
        else {
            super.onActivityResult( requestCode, resultCode, data );
        }
    }

    private void showFeedbackMessage(String msg) {
        Snackbar.make(findViewById(R.id.viewRoot),msg, Snackbar.LENGTH_SHORT).show();
    }

    public void onListItemChangedInList(ToDo item) {
        new UpdateToDoTaskWithFuture(this, this.crudOperations)
                .execute(item)
                .thenAccept((updated) ->
                        showFeedbackMessage("ToDo " + item.getName() + " wurde aktualisiert."));
    }

}
