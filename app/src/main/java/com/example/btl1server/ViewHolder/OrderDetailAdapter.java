package com.example.btl1server.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.btl1server.Model.Order;
import com.example.btl1server.R;

import java.util.List;

class MyViewHolder extends RecyclerView.ViewHolder {

    public TextView name, quantity, price, note;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        name = (TextView) itemView.findViewById(R.id.product_name);
        quantity = (TextView) itemView.findViewById(R.id.product_quantity);
        price = (TextView) itemView.findViewById(R.id.product_price);
        note = (TextView) itemView.findViewById(R.id.product_note);
    }
}
public class OrderDetailAdapter extends RecyclerView.Adapter<MyViewHolder> {

    List<Order> myOrders;

    public OrderDetailAdapter(List<Order> myOrders) {
        this.myOrders = myOrders;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.order_detail_layout, viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Order order = myOrders.get(i);
        myViewHolder.name.setText(String.format("Tên món ăn: %s", order.getProductName()));
        myViewHolder.quantity.setText(String.format("Số lượng: %s", order.getQuantity()));
        myViewHolder.price.setText(String.format("Giá: %s", order.getPrice()));
        myViewHolder.note.setText(String.format("Note: %s", order.getComment()));
    }

    @Override
    public int getItemCount() {
        return myOrders.size();
    }
}
