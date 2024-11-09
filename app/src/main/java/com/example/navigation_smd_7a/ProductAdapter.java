package com.example.navigation_smd_7a;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.concurrent.Executors;


public class ProductAdapter extends ArrayAdapter<Product> {
    Context context;
    int resource;
    private final OnProductStatusChangeListener statusChangeListener;

    public ProductAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Product> objects, OnProductStatusChangeListener listener) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.statusChangeListener = listener;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        TextView tvTitle = v.findViewById(R.id.tvProductTitle);
        ImageView ivEdit = v.findViewById(R.id.ivEdit);
        ImageView ivDelete = v.findViewById(R.id.ivDelete);

        Product p = getItem(position);
        tvTitle.setText(p.getPrice() + " : " + p.getTitle()+ ":" + p.getStatus());

        ivEdit.setOnClickListener(view -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setTitle("Product");

            View dialogView = LayoutInflater.from(context).inflate(R.layout.add_new_product_dialog_design, null, false);
            dialog.setView(dialogView);

            TextView tvHeading = dialogView.findViewById(R.id.heading);
            tvHeading.setText("Update Product");
            EditText etTitle = dialogView.findViewById(R.id.etTitle);
            EditText etDate = dialogView.findViewById(R.id.etDate);
            EditText etPrice = dialogView.findViewById(R.id.etPrice);
            AutoCompleteTextView etStatus = dialogView.findViewById(R.id.actvStatus);

            // Initialize dropdown for status options
            String[] options = getContext().getResources().getStringArray(R.array.autocomplete_options);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, options);
            etStatus.setAdapter(adapter);

            // Load existing product values into fields
            etTitle.setText(p.getTitle());
            etDate.setText(p.getDate());
            etPrice.setText(String.valueOf(p.getPrice()));
            etStatus.setText(p.getStatus(), false);

            dialog.setPositiveButton("Save", (dialogInterface, i) -> {
                String title = etTitle.getText().toString().trim();
                String date = etDate.getText().toString().trim();
                String price = etPrice.getText().toString().trim();
                String status = etStatus.getText().toString().trim();

                if (title.isEmpty() || date.isEmpty() || price.isEmpty() || status.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int priceInt = Integer.parseInt(price);

                    // Update the product in the database off the main thread
                    Executors.newSingleThreadExecutor().execute(() -> {
                        ProductDB productDB = new ProductDB(getContext());
                        productDB.open();
                        productDB.updateAttr(p.getId(), title, date, priceInt, status);
                        productDB.close();

                        // Update product in the adapter's list and refresh UI on main thread
                        p.setTitle(title);
                        p.setDate(date);
                        p.setPrice(priceInt);
                        p.setStatus(status);

                        ((Activity) context).runOnUiThread(() -> {
                            notifyDataSetChanged();
                            Toast.makeText(getContext(), "Product Updated", Toast.LENGTH_SHORT).show();
                            statusChangeListener.onProductStatusChanged();
                        });

                    });

                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                }
            });

            dialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
            dialog.show();
        });


        ivDelete.setOnClickListener(view -> {
            ProductDB db = new ProductDB(context);
            db.open();
            db.remove(p.getId());
            db.close();
            remove(p);
            notifyDataSetChanged();
        });

        return v;
    }
}
