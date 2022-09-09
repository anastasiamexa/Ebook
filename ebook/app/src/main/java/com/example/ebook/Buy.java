package com.example.ebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.cardform.view.CardForm;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.HashMap;

public class Buy extends AppCompatActivity {
    TextView Title, Total, Quantity;
    DatabaseReference database, databasePayment;
    String title;
    int quantity, available;
    float price;
    CardForm cardForm;
    Button pay;
    AlertDialog.Builder alertBuilder;
    // Format to show only 2 decimals of float
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        // Database references
        database = FirebaseDatabase.getInstance().getReference().child("Books");
        databasePayment = FirebaseDatabase.getInstance().getReference().child("Payments");

        Title = findViewById(R.id.Book);
        Total = findViewById(R.id.Total);
        Quantity = findViewById(R.id.tQuantity);

        // Variables from previous intent
        quantity = Integer.parseInt (getIntent().getStringExtra("quantity"));
        available = Integer.parseInt (getIntent().getStringExtra("available"));
        price = Float.parseFloat(getIntent().getStringExtra("price"));
        title = getIntent().getStringExtra("title");
        // Set values to TextViews
        Title.setText(title);
        Total.setText("Total: " + df.format(price * quantity) + "€");
        Quantity.setText("Quantity: " + quantity);

        cardForm = findViewById(R.id.card_form);
        pay = findViewById(R.id.btnPay);
        // Set fields as required
        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .cardholderName(CardForm.FIELD_REQUIRED)
                .postalCodeRequired(true)
                .setup(this); // show the card view in this activity
        // Make CVV field like a password (not visible)
        cardForm.getCvvEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        // When the Confirm Purchase button is pressed
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the form is completed
                if (cardForm.isValid()) {
                    // Create a message
                    alertBuilder = new AlertDialog.Builder(Buy.this);
                    alertBuilder.setTitle("Confirm purchase details");
                    alertBuilder.setMessage("Card number: " + cardForm.getCardNumber() + "\n" +
                            "Card expiration date: " + cardForm.getExpirationDateEditText().getText().toString() + "\n" +
                            "Card CVV: " + cardForm.getCvv() + "\n" +
                            "Postal code: " + cardForm.getPostalCode() + "\n" +
                            "Name: " + cardForm.getCardholderName());
                    // If user agrees to the purchase
                    alertBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Save payment to database
                            HashMap paymentMap = new HashMap();
                            paymentMap.put("Book", title);
                            paymentMap.put("Quantity", quantity);
                            paymentMap.put("Total", df.format(price * quantity));
                            paymentMap.put("Card Number", cardForm.getCardNumber());
                            paymentMap.put("Expiration Date", cardForm.getExpirationDateEditText().getText().toString());
                            paymentMap.put("Card CVV", cardForm.getCvv());
                            paymentMap.put("Postal Code", cardForm.getPostalCode());
                            paymentMap.put("Name", cardForm.getCardholderName());

                            databasePayment.push().setValue(paymentMap);

                            // Update the quantity of the book on database
                            HashMap hashMap = new HashMap();
                            hashMap.put("Quantity", available - quantity);

                            database.child(title).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
                                    dialog.dismiss();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Buy.this);
                                    builder.setMessage("Thank you for your purchase!")
                                            .setCancelable(false)
                                            .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    // Go back to BooksList activity
                                                    Intent intent = new Intent(Buy.this, BooksList.class);
                                                    startActivity(intent);
                                                }
                                            });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Buy.this,"Problem with database", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                    // If user cancels the purchase
                    alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    // Show message
                    AlertDialog alertDialog = alertBuilder.create();
                    alertDialog.show();
                } else { // If the form is not completed
                    Toast.makeText(Buy.this,"Please complete the form", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}