package com.example.ebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BooksList extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference database;
    MyAdapter myAdapter;
    ArrayList<Book> list;
    ArrayList<String> matches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books_list);

        // Initialize the recyclerView and get the database reference of the right path (Books)
        recyclerView = findViewById(R.id.bookList);
        database = FirebaseDatabase.getInstance().getReference("Books");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        matches = new ArrayList<>();
        list = new ArrayList<>(); // this list contains all the data from database (array list of type Book)
        // Set the adapter
        myAdapter = new MyAdapter(this, list);
        recyclerView.setAdapter(myAdapter);

        // Read data from database
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // For every book (child)
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    // Create a Book object and set it's attributes, according to database values
                    Book book = dataSnapshot.getValue(Book.class);
                    list.add(book);
                }
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showMessage("Error", error.getMessage());
            }
        });
    }

    // Search
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        // Get reference to menu item
        MenuItem searchItem = menu.findItem(R.id.action_search);
        // Get reference to search menu
        SearchView searchView = (SearchView) searchItem.getActionView();
        // Set keyboard icon
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // There is no use for this method because the search happens on real time
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the results according to the newText
                myAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    // Voice recognition method
    public void talk(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a title");
        startActivityForResult(intent,123);
    }

    // Results of voice recognition
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            // Search for the best match of voice recognition
            myAdapter.getFilter().filter(matches.get(0));
        }
    }

    // Method used for showing a message
    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}