package com.example.ebook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements Filterable {

    Context context;
    ArrayList<Book> list;
    ArrayList<Book> listFull; // List copy used for search

    // Constructor
    public MyAdapter(Context context, ArrayList<Book> list) {
        this.context = context;
        this.list = list;
        // New list containing the same items, for independent usage
        listFull = new ArrayList<>(list);
    }

    // THIS IS NOT A CONSTRUCTOR!!!
    // listFull does not fill up in the constructor method, because the list is still empty...
    // So fill up the list here
    public void MyAdapter1(){listFull = new ArrayList<>(list);}

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new MyViewHolder(v);
    }

    // Method to bind the view in Card view with data in Book class
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Here the list is full, so go to MyAdapter1 method to fill up the listFull
        if (listFull.size() == 0){
            MyAdapter1();
        }
        // Add attribute values from model class to appropriate view in Card view
        Book book = list.get(position);
        holder.title.setText(book.getTitle());
        holder.author.setText(book.getAuthor());
        holder.pages.setText(String.valueOf(book.getPages()));
        holder.price.setText(String.valueOf(book.getPrice()) + "€");
        // Get book cover images from Firebase Storage
        StorageReference imageRef2 = holder.storageReference.child(book.getCover());
        imageRef2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            // Use Glide to get the image through url
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(holder.cover);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.getLocalizedMessage();
            }
        });

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When user clicks on a card view, go to the next activity and pass the book object
                Intent intent = new Intent(context, More.class);
                intent.putExtra("object", book);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    // Class to create references of the views in Card view
    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView author, pages, price, title;
        ImageView cover;
        FirebaseStorage firebaseStorage;
        StorageReference storageReference;
        ConstraintLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            firebaseStorage = FirebaseStorage.getInstance();
            storageReference = firebaseStorage.getReference();
            cover = itemView.findViewById(R.id.cover);
            author = itemView.findViewById(R.id.author);
            pages = itemView.findViewById(R.id.nPages);
            price = itemView.findViewById(R.id.nPrice);
            title = itemView.findViewById(R.id.title);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }

    @Override
    public Filter getFilter() {
        return listFilter;
    }

    // Return the filter results
    private Filter listFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Book> filteredList = new ArrayList<>();
            // constraint variable is the input of the search bar
            // If constraint is null, return all the results
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(listFull);
            } else { // Create a string with the filter pattern (case insensitive)
                String filterPattern = constraint.toString().toLowerCase().trim();
                // Iterate all books from list and find those that match the filter pattern
                for (Book item : listFull) { // Filter for book titles
                    if (item.getTitle().toLowerCase().startsWith(filterPattern)){
                        filteredList.add(item); // add it to the filtered list
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            // Returns the filtered list to the publishResults method
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Delete all items from list and add only those that match the filter pattern
            list.clear();
            list.addAll((ArrayList) results.values);
            notifyDataSetChanged();
        }
    };
}
