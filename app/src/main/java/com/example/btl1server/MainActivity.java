package com.example.btl1server;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.btl1server.Common.Common;
import com.example.btl1server.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import info.hoang8f.widget.FButton;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    Button btndangnhap;
    TextView txtChao,txtApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Ánh xạ
        btndangnhap= (FButton)findViewById(R.id.btndangnhap);
        txtChao = (TextView) findViewById(R.id.txtChao);
        txtApp = (TextView)findViewById(R.id.txtApp);

        Paper.init(this);

        Typeface typeface = (Typeface) Typeface.createFromAsset(getAssets(),"fonts/NABILA.TTF");
        txtChao.setTypeface(typeface);
        txtApp.setTypeface(typeface);
        btndangnhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dangnhap = new Intent(MainActivity.this,Dangnhap.class);
                startActivity(dangnhap);
            }
        });

        // check remember
        String phone = Paper.book().read(Common.USER_KEY);
        String pwd = Paper.book().read(Common.PWD_KEY);
        if(phone != null && pwd != null) {
            if(!phone.isEmpty() && ! pwd.isEmpty()) {
                login(phone, pwd);
            }
        }
    }

    private void login(String phone, String pwd) {

        if (phone.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(MainActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
        }
        else
            signInUser(phone, pwd);
        
    }

    private void signInUser(String phone, String password) {
        FirebaseDatabase database;
        DatabaseReference users;
        database = FirebaseDatabase.getInstance();
        users = database.getReference("User");
        final ProgressDialog process = new ProgressDialog(MainActivity.this);
        process.setMessage("Vui lòng đợi");
        process.show();
        final String localPhone = phone;
        final String localPassword = password;
        users.addValueEventListener(new ValueEventListener() {//Đọc dữ liệu từ database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(localPhone).exists()){
                    process.dismiss();
                    User user = dataSnapshot.child(localPhone).getValue(User.class);
                    user.setPhone(localPhone);
                    if(Boolean.parseBoolean(user.getIsStaff())){
                        if(user.getPassword().equals(localPassword)){
                            //login ok
                            Intent dangnhap = new Intent(MainActivity.this, Home.class);
                            Common.currentUser = user;
                            startActivity(dangnhap);
                            finish();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Vui lòng đăng nhập với tài khoản quán ăn", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    process.dismiss();
                    Toast.makeText(MainActivity.this,"Người dùng không tồn tại",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
