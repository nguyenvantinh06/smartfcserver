package com.example.btl1server;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.example.btl1server.Common.Common;
import com.example.btl1server.ViewHolder.OrderDetailAdapter;

public class OrderDetail extends AppCompatActivity {

    TextView order_id, order_phone, order_address, order_total, order_comment;
    String order_id_value = "";
    RecyclerView lstFoods;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        order_id = (TextView) findViewById(R.id.order_id);
        order_phone = (TextView) findViewById(R.id.order_phone);
        order_address = (TextView) findViewById(R.id.order_address);
        order_total = (TextView) findViewById(R.id.order_total);
        order_comment = (TextView) findViewById(R.id.order_comment);

        lstFoods = (RecyclerView) findViewById(R.id.lstFoods);
        lstFoods.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        lstFoods.setLayoutManager(layoutManager);

        if (getIntent() != null) {
            order_id_value = getIntent().getStringExtra("OrderId");
        }

        // set value
        order_id.setText(order_id_value);
        order_phone.setText(String.format("Người dùng: %s", Common.currentRequest.getPhone()));
        order_total.setText(String.format("Tổng tiền: %s", Common.currentRequest.getTotal()));
        order_address.setText(String.format("Địa chỉ: %s", Common.currentRequest.getAddress()));
        String note = Common.currentRequest.getComment().toString();
        if (note.isEmpty())
            order_comment.setText("Lời dặn: Không có");
        else
            order_comment.setText(String.format("Lời dặn: %s", Common.currentRequest.getComment()));
        OrderDetailAdapter adapter = new OrderDetailAdapter(Common.currentRequest.getFoods());
        adapter.notifyDataSetChanged();
        lstFoods.setAdapter(adapter);
    }
}
