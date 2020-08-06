package com.example.btl1server;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.btl1server.Common.Common;
import com.example.btl1server.Interface.ItemClickListener;
import com.example.btl1server.Model.Category;
import com.example.btl1server.Model.Food;
import com.example.btl1server.Model.Token;
import com.example.btl1server.ViewHolder.FoodViewHolder;
import com.example.btl1server.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import info.hoang8f.widget.FButton;
import io.paperdb.Paper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    TextView txtFullName;
    //Firebase
    DatabaseReference category;
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter ;
    RecyclerView list_menu;
    MaterialEditText edtName;
    FButton btnSelect,btnUpload;
    Category newCategory;
    Uri saveUri;

    DrawerLayout drawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);
        //khoi tao firebase
        category= FirebaseDatabase.getInstance().getReference("Category");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Paper.init(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               showDialog();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Hiển thị tên người dùng
        View headerView  = navigationView.getHeaderView(0);
        txtFullName = (TextView)headerView.findViewById(R.id.txtuserName);
        txtFullName.setText(Common.currentUser.getName());

        list_menu = (RecyclerView)findViewById(R.id.recycler_menu);
        list_menu.setHasFixedSize(true);
        list_menu.setLayoutManager(new GridLayoutManager(this, 2));
        loadMenu();

        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void updateToken(String token) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Token data = new Token(token, true);
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }

    private void showDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Thêm loại món ăn");
        alertDialog.setMessage("Vui lòng điền đầy đủ thông tin");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu_layout, null);

        edtName = add_menu_layout.findViewById(R.id.edtName);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();// Người dùng chọn ảnh từ thư viện
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.add);
        //Thiết lập button
        alertDialog.setPositiveButton("Hoàn tất", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // Tạo một loại món ăn mới trên database
                if (newCategory != null){
                    category.push().setValue(newCategory);
                    Snackbar.make(drawer, newCategory.getName()+" đã được thêm", Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });
        alertDialog.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void uploadImage() {
        if(saveUri != null){
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Đang tải lên");
            mDialog.show();
            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.dismiss();
                    Toast.makeText(Home.this,"Đã tải lên",Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Thiết lập giá trị cho loại mới nếu ảnh đc tải lên và ta có thể download link
                            newCategory = new Category(edtName.getText().toString(),uri.toString());

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(Home.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.setMessage("Đang tải");
                }
            });
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
        && data!=null && data.getData() !=null)
        {
            saveUri = data.getData();
            btnSelect.setText("Ảnh đã chọn");

        }
    }
    //Chọn một hình ảnh từ bộ sưu tập.
    //Nhận URL hình ảnh từ thư viện
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Chọn ảnh"),Common.PICK_IMAGE_REQUEST);
    }

    private void loadMenu() {

        FirebaseRecyclerOptions<Category> options = new  FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category, Category.class).build();


        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options
             //   Category.class,
             //   R.layout.menu_item,
             //   MenuViewHolder.class,
             //   categories
        ) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewholder, int position, @NonNull Category model) {
                viewholder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewholder.imageView);

                //final Category clickItem = model; sửa lại
                viewholder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Lấy CategoryId và gửi nó đến Activity mới
                        Intent foodList = new Intent(Home.this, FoodList.class);
                        //Vì Category là key nên ta lấy key của dữ liệu trên
                        foodList.putExtra("CategoryId", adapter.getRef(position).getKey());//Dữ liệu "CategoryId" lấy cùng của Intent
                        startActivity(foodList);
                    }
                });
            }

            @Override
            public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item, parent, false);

                return new MenuViewHolder(itemView);
            }

        };
        adapter.startListening();

        adapter.notifyDataSetChanged();//Refresh dữ liệu khi dữ liệu thay đổi
        list_menu.setAdapter(adapter);
    }

    protected void onResume() {
        super.onResume();
        if(adapter!= null)
            adapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_orders) {
            Intent orders = new Intent(Home.this, OrderStatus.class);
            startActivity(orders);
        }
        else if (id == R.id.nav_banner) {
            Intent banner = new Intent(Home.this, BannerActivity.class);
            startActivity(banner);
        }
        else if (id == R.id.nav_menu) {
            Intent menu = new Intent(Home.this, Home.class);
            startActivity(menu);
        }
        else if (id == R.id.nav_logout) {
            // delete remember user & pwd
            Paper.book().destroy();

            Intent signIn = new Intent(Home.this, Dangnhap.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);
        }
        else if (id == R.id.nav_change_pwd){
            showChangePasswordDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Đổi mật khẩu");
        alertDialog.setMessage("Vui lòng điền đầy đủ thông tin");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_pwd = inflater.inflate(R.layout.change_password_layout,null);

        final MaterialEditText edtPassOld = (MaterialEditText)layout_pwd.findViewById(R.id.edtPassOld);
        final MaterialEditText edtNhapPass = (MaterialEditText)layout_pwd.findViewById(R.id.edtNhapPass);
        final MaterialEditText edtNhaplaiPass = (MaterialEditText)layout_pwd.findViewById(R.id.edtNhaplaiPass);

        alertDialog.setView(layout_pwd);

        //Button
        alertDialog.setPositiveButton("ĐỔI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();

                if (edtPassOld.getText().toString().equals(Common.currentUser.getPassword())) {
                    if (edtNhapPass.getText().toString().equals(edtNhaplaiPass.getText().toString())) {
                        Map<String, Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put("Password", edtNhapPass.getText().toString());

                        //Make update
                        DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                        user.child(Common.currentUser.getPhone())
                                .updateChildren(passwordUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        waitingDialog.dismiss();
                                        Toast.makeText(Home.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        waitingDialog.dismiss();
                        Toast.makeText(Home.this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    waitingDialog.dismiss();
                    Toast.makeText(Home.this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                }

            }
        });
        alertDialog.setNegativeButton("ĐÓNG", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    //update/delete
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if(item.getTitle().equals(Common.DELETE)){
            deleteCategory(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteCategory(String key) {

        DatabaseReference foods = FirebaseDatabase.getInstance().getReference("Food");
        Query foodInCategory = foods.orderByChild("menuId").equalTo(key);

        foodInCategory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren()) {
                    postSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        category.child(key).removeValue();
        Toast.makeText(this,"Đã xóa !",Toast.LENGTH_SHORT).show();
    }


    private void showUpdateDialog(final String key, final Category item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Cập nhật loại món ăn");
        alertDialog.setMessage("Vui lòng điền đầy đủ thông tin");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu_layout, null);

        edtName = add_menu_layout.findViewById(R.id.edtName);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);
        //Đặt tên mặt định
        edtName.setText(item.getName());
        //Tạo sự kiện cho Button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();// Người dùng chọn ảnh từ thư viện và lưu Uri của ảnh này
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });
        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.add);
        //Thiết lập button
        alertDialog.setPositiveButton("Hoàn tất", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // Tạo một loại món ăn mới trên database
                item.setName(edtName.getText().toString());
                category.child(key).setValue(item);
            }
        });
        alertDialog.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
    private void changeImage(final Category item) {
        if(saveUri != null){
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Đang tải lên");
            mDialog.show();
            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.dismiss();
                    Toast.makeText(Home.this,"Đã tải lên",Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Thiết lập giá trị cho loại mới nếu ảnh đc tải lên và ta có thể download link
                            item.setImage(uri.toString());

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(Home.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    // double process = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    mDialog.setMessage("Đang tải");
                }
            });
        }
    }
}

