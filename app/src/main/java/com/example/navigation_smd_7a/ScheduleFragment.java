package com.example.navigation_smd_7a;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

public class ScheduleFragment extends Fragment implements OnProductStatusChangeListener  {

    Context context;
    private ProductAdapter adapter;
    private ArrayList<Product> products;
    private ListView lvNewOrderList;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_order, container, false);
        lvNewOrderList = view.findViewById(R.id.lvNewOrdersList);
        loadProducts();
        return view;
    }

    private void loadProducts() {
        ProductDB productDB = new ProductDB(context);
        productDB.open();
        products = productDB.fetchProducts("Scheduled");
        productDB.close();

        adapter = new ProductAdapter(context, R.layout.product_item_design, products, this);
        lvNewOrderList.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        onProductStatusChanged();  // Reload data when fragment resumes
    }

    @Override
    public void onProductStatusChanged() {
        loadProducts();
        adapter.notifyDataSetChanged();
    }
}
