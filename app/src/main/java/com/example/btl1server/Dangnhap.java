package com.example.btl1server;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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

public class Dangnhap extends AppCompatActivity {
    EditText edtvenphone,edtvenPass;
    Button btndangnhap1;
    CheckBox ckbRemember;
    FirebaseDatabase database;
    DatabaseReference users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangnhap);
        btndangnhap1=(FButton)findViewById(R.id.btndangnhap1);
        edtvenphone=(EditText)findViewById(R.id.edtvenphone);
        edtvenPass=(EditText)findViewById(R.id.edtvenPass);
        ckbRemember = (CheckBox)findViewById(R.id.ckbRemember);
        //

        Paper.init(this);

        database = FirebaseDatabase.getInstance();
        users = database.getReference("User");
        btndangnhap1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save user name and password
                if (ckbRemember.isChecked()) {
                    Paper.book().write(Common.USER_KEY, edtvenphone.getText().toString());
                    Paper.book().write(Common.PWD_KEY, edtvenPass.getText().toString());
                }
                if(edtvenphone.getText().toString().isEmpty() || edtvenPass.getText().toString().isEmpty()){
                    Toast.makeText(Dangnhap.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                }
                signInUser(edtvenphone.getText().toString(),edtvenPass.getText().toString());
            }
        });
    }
    private void signInUser(String phone, String password) {
        final ProgressDialog process = new ProgressDialog(Dangnhap.this);
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
                            Intent dangnhap = new Intent(Dangnhap.this, Home.class);
                            Common.currentUser = user;
                            startActivity(dangnhap);
                            finish();
                        }
                        else{
                            Toast.makeText(Dangnhap.this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(Dangnhap.this, "Vui lòng đăng nhập với tài khoản quán ăn", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    process.dismiss();
                    Toast.makeText(Dangnhap.this,"Tài khoản chủ quán không tồn tại",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
