package com.carstenbrauer.todo;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.carstenbrauer.model.ITodoCRUDOperationAsync;
import com.carstenbrauer.model.Todo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OverviewActivity extends AppCompatActivity {

    private ITodoCRUDOperationAsync crudOperation;

    private ViewGroup listView;
    private ArrayAdapter<Todo> listViewAdapter;
    private FloatingActionButton createItemButton;
    private ProgressBar progressBar;

    private static final  int CALL_EDIT_ITEM = 0;
    private static final  int CALL_CREATE_ITEM = 1;

    private List<Todo> arrayAdapterList = new ArrayList<>();

    public enum SortMode{
        SORT_BY_NAME,
        SORT_BY_ID,
        SORT_BY_DATE,
        SORT_BY_RELEVANCE,
        SORT_BY_DONE
    }

    private SortMode activeSortMode = SortMode.SORT_BY_DATE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        initialise();
    }

    private void initialise(){

        this.crudOperation = ((TodoApplication)getApplication()).getCRUDOperation();

        listView = findViewById(R.id.listView);
        createItemButton = findViewById(R.id.createTodoButton);
        progressBar = findViewById(R.id.progressBar);

        boolean onlineStatus = getIntent().getBooleanExtra("ONLINE_STATUS", false);

        if (!onlineStatus){
            Snackbar.make(findViewById(R.id.contentView), "Der Server ist offline!" , Snackbar.LENGTH_INDEFINITE).show();
        }

        listViewAdapter = new ArrayAdapter<Todo>(this, R.layout.activity_overview_listitem, arrayAdapterList){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

                Todo todo = this.getItem(position);
                View itemView = convertView;
                ListitemViewHolder viewHolder;

                if (itemView == null){
                    itemView = getLayoutInflater().inflate(R.layout.activity_overview_listitem, null);

                    viewHolder = new ListitemViewHolder((ViewGroup) itemView);
                    itemView.setTag(viewHolder);
                }
                else {
                    viewHolder = (ListitemViewHolder) itemView.getTag();
                }

                viewHolder.unbind();
                viewHolder.bind(todo);
                return itemView;
            }
        };

        listViewAdapter.setNotifyOnChange(true);
        ((ListView)listView).setAdapter(listViewAdapter);

        progressBar.setVisibility(View.VISIBLE);
        crudOperation.readAllTodos(new ITodoCRUDOperationAsync.ResultCallback<List<Todo>>() {
            @Override
            public void onResult(List<Todo> result) {
                listViewAdapter.addAll(result);
                sortTodos();
                progressBar.setVisibility(View.GONE);
            }
        });

        ((ListView)listView).setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Todo selectedTodo = listViewAdapter.getItem(i);
                showDetailViewForEdit(selectedTodo, i);
            }
        });

        createItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeteilViewForCreate();
            }
        });
    }

    private void showDeteilViewForCreate() {
        Intent callDetailviewForCreateTodo = new Intent( this, DetailviewActivity.class);
        startActivityForResult(callDetailviewForCreateTodo, CALL_CREATE_ITEM);
    }


    private void showDetailViewForEdit(Todo item, int listPosition){
//        Toast.makeText(OverviewActivity.this, String.format(getApplicationContext().getResources().getString(R.string.toast_message), item), Toast.LENGTH_LONG).show();
        Intent callDetailviewIntent = new Intent(this, DetailviewActivity.class);
        callDetailviewIntent.putExtra(DetailviewActivity.ARG_ITEM_ID, item.getId());
        callDetailviewIntent.putExtra("LIST_POSITION", listPosition);
        startActivityForResult(callDetailviewIntent, CALL_EDIT_ITEM);
    }


    private void addTodoToList(final Todo todo){
        this.listViewAdapter.add(todo);
        this.sortTodos();
        ((ListView)this.listView).setSelection(this.listViewAdapter.getPosition(todo));
    }

    private void updateTodoList(Todo todo, int position) {

        this.listViewAdapter.remove(listViewAdapter.getItem(position));
        this.listViewAdapter.insert(todo, position);
        this.sortTodos();
        ((ListView)this.listView).setSelection(this.listViewAdapter.getPosition(todo));
    }

    private void deleteTodoInList(int position) {
        this.listViewAdapter.remove(listViewAdapter.getItem(position));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CALL_EDIT_ITEM){
            if (resultCode == RESULT_OK){
                long todoId = data.getLongExtra(DetailviewActivity.ARG_ITEM_ID, -1);
                final int overviewListPosition = data.getIntExtra("LIST_POSITION", -1);
                if (data.getBooleanExtra("DELETE_TODO", false)){
                    deleteTodoInList(overviewListPosition);
                }
                else {
                    crudOperation.readTodo(todoId, new ITodoCRUDOperationAsync.ResultCallback<Todo>() {
                        @Override
                        public void onResult(Todo result) {
                            updateTodoList(result, overviewListPosition);
                        }
                    });
                }
            }
            else {
//                Toast.makeText(OverviewActivity.this  , "no item received from DetailView ", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == CALL_CREATE_ITEM){
            if (resultCode == RESULT_OK){
                long todoId = data.getLongExtra(DetailviewActivity.ARG_ITEM_ID, -1);
                crudOperation.readTodo(todoId, new ITodoCRUDOperationAsync.ResultCallback<Todo>() {
                    @Override
                    public void onResult(Todo result) {
                        addTodoToList(result);
                    }
                });
            }
            else {
//                Toast.makeText(OverviewActivity.this, "no item received from DetailView ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overview_optionsmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.sortTodos){
            this.activeSortMode = this.activeSortMode == SortMode.SORT_BY_DATE ? SortMode.SORT_BY_RELEVANCE : SortMode.SORT_BY_DATE;
            Toast.makeText(OverviewActivity.this, activeSortMode.toString(), Toast.LENGTH_SHORT).show();
            this.sortTodos();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void sortTodos() {

        if (this.activeSortMode == SortMode.SORT_BY_DATE){
            listViewAdapter.sort(Todo.SORT_BY_RELEVANCE);
            listViewAdapter.sort(Todo.SORT_BY_DATE);
        }
        else if(this.activeSortMode == SortMode.SORT_BY_RELEVANCE) {
            listViewAdapter.sort(Todo.SORT_BY_DATE);
            listViewAdapter.sort(Todo.SORT_BY_RELEVANCE);
        }

        listViewAdapter.sort(Todo.SORT_BY_DONE);
    }

    public class ListitemViewHolder{

        private Todo todo;

        private TextView todoName;
        private TextView todoExpiry;
        private TextView todoId;
        private CheckBox todoDone;
        private Switch todoFavourite;

        private ViewGroup layout;

        public ListitemViewHolder(ViewGroup layout){

            this.layout = layout;
            this.todoName = layout.findViewById(R.id.itemName);
//            this.todoId = layout.findViewById(R.id.itemId);
            this.todoExpiry = layout.findViewById(R.id.itemExpiry);
            this.todoDone  = layout.findViewById(R.id.todoDone);
            this.todoFavourite  = layout.findViewById(R.id.todoFavourite);

            this.todoDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (todo != null) {
                        todo.setDone(b);
                        crudOperation.updateTodo(todo.getId(), todo, new ITodoCRUDOperationAsync.ResultCallback<Boolean>() {
                            @Override
                            public void onResult(Boolean result) {
                                Toast.makeText(OverviewActivity.this, "Status des Todo mit der ID " + todo.getId() + " wurde geändert.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });

            this.todoFavourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (todo != null) {
                        todo.setFavourite(b);
                        crudOperation.updateTodo(todo.getId(), todo, new ITodoCRUDOperationAsync.ResultCallback<Boolean>() {
                            @Override
                            public void onResult(Boolean result) {
                                Toast.makeText(OverviewActivity.this, "Favoritenstatus des Todo mit der ID " + todo.getId() + " wurde geändert.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }

        public void unbind(){
            this.todo = null;
        }

        public void bind (Todo todo){

            this.todoName.setText(todo.getName());
//            this.todoId.setText(String.valueOf(todo.getId()));
            this.todoExpiry.setText(todo.getDateString());
            this.todoDone.setChecked(todo.isDone());
            this.todoFavourite.setChecked(todo.isFavourite());

            Calendar now = Calendar.getInstance();
            this.layout.setBackgroundResource(now.getTimeInMillis() > todo.getExpiry() ? R.color.outDatedTodoBackground : R.color.todoBackground);

            this.todo = todo;
        }
    }
}
