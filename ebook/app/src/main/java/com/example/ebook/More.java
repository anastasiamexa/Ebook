package com.example.ebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class More extends AppCompatActivity {

    ImageView mCover;
    TextView mTitle, mAuthor2, mPages2, mQuantity2, mPrice2, mDescription2, mNumber;
    String title, author, pages, quantity, price, description, cover;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    MyTTS tts; // Text to Speech variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        mCover = findViewById(R.id.mCover);
        mTitle = findViewById(R.id.mTitle);
        mAuthor2 = findViewById(R.id.mAuthor2);
        mPages2 = findViewById(R.id.mPages2);
        mQuantity2 = findViewById(R.id.mQuantity2);
        mPrice2 = findViewById(R.id.mPrice2);
        mDescription2 = findViewById(R.id.mDescription2);
        mNumber = findViewById(R.id.mNumber);
        tts = new MyTTS(this);

        getData();
        setData();
    }

    // Method used to get the data from previous activity, using the getters of book object
    private void getData() {
        if(getIntent().hasExtra("object")) {
            Book book  = getIntent().getParcelableExtra("object");
            title = book.getTitle();
            author = book.getAuthor();
            pages = String.valueOf(book.getPages());
            quantity = String.valueOf(book.getQuantity());
            price = String.valueOf(book.getPrice());
            description = book.getDescription();
            cover = book.getCover();
        } else {
            Toast.makeText(this,"No data", Toast.LENGTH_SHORT).show();
        }
    }

    // Method used to set the data to the appropriate views
    private void setData() {
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        mTitle.setText(title);
        mAuthor2.setText(author);
        mPages2.setText(pages);
        mQuantity2.setText(quantity);
        mPrice2.setText(price + "€");
        mDescription2.setText(description);

        // Get book cover images from Firebase Storage
        StorageReference imageRef = storageReference.child(cover);
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            // Use Glide to get the image through url
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(More.this).load(uri).into(mCover);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.getLocalizedMessage();
            }
        });
    }

    // Commence Text to Speech
    public void speak(View view) {
        tts.speak(title);
        tts.speak(description);
    }

    public void buy(View view) {
        if (mNumber.getText().toString().isEmpty()){
            showMessage("Error", "Quantity cannot be null.");
        } else {
            int available = Integer.parseInt(mQuantity2.getText().toString()); // How many books are available
            int purchase = Integer.parseInt(mNumber.getText().toString()); // How many books the users wants to buy
            // Purchased books must be more than 0 and less or equal than the available books
            if (purchase >= 1 && purchase <= available){
                // Start new activity
                Intent intent = new Intent(this, Buy.class);
                intent.putExtra("quantity", mNumber.getText().toString());
                intent.putExtra("available", mQuantity2.getText().toString());
                intent.putExtra("price", price);
                intent.putExtra("title", mTitle.getText().toString());
                startActivity(intent);
            } else if (purchase == 0) {
                showMessage("Error", "Quantity must be more than 0.");
            } else {
                showMessage("Error", "There are only " + available + " books.");
            }
        }
    }

    // Method used for showing a message
    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }

    // On Destroy stop the Text to Speech
    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.stop();
    }
}