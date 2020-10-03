package com.example.myquizadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class CategoryActivity extends AppCompatActivity {
//supun
    private RecyclerView cat_recycle_view;
    private Button addCatButton;
    public static List<CategoryModel> catList = new ArrayList<>();
    public static int selected_cat_index = 0;
    private FirebaseFirestore firestore;
    private Dialog loadingdialog , addCatDialog;
    private EditText dialogCatName;
    private Button dialogAddButton;
    private CategoryAdapter adapter;
    private FirebaseAuth firebaseAuth;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categoryies");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //back button

        cat_recycle_view = findViewById(R.id.cat_recycler);
        addCatButton = findViewById(R.id.addCatB);

        loadingdialog = new Dialog(CategoryActivity.this);
        loadingdialog.setContentView(R.layout.loading_progressbar);
        loadingdialog.setCancelable(false);
        loadingdialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingdialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        addCatDialog = new Dialog(CategoryActivity.this);
        addCatDialog.setContentView(R.layout.add_category_dialog);
        addCatDialog.setCancelable(true);
        addCatDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogCatName = addCatDialog.findViewById(R.id.addcat_cat_name);
        dialogAddButton = addCatDialog.findViewById(R.id.addcat_add_button);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();



        addCatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialogCatName.getText().clear();
                addCatDialog.show();

            }
        });

        dialogAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(dialogCatName.getText().toString().isEmpty()){

                    dialogCatName.setError("Enter Category Name");
                    return;

                }

                addNewCategory(dialogCatName.getText().toString());
            }
        });


        LinearLayoutManager layoutManager =new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        cat_recycle_view.setLayoutManager(layoutManager);

        loadData();

    }

    private void loadData(){

        loadingdialog.show();
        catList.clear();

        firestore.collection("QUIZ").document("Category").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    DocumentSnapshot doc = task.getResult();

                    if(doc.exists()){

                        long count = (long)doc.get("COUNT");

                        for(int i=1 ; i <= count ; i++){

                            String catName = doc.getString("CAT" + String.valueOf(i)+"_NAME");
                            String catid = doc.getString("CAT" + String.valueOf(i)+"_ID");

                            catList.add(new CategoryModel(catid,catName,"0","1"));

                        }

                        adapter = new CategoryAdapter(catList);
                        cat_recycle_view.setAdapter(adapter);


                    }
                    else{

                        Toast.makeText(CategoryActivity.this,"No Category Document Exist",Toast.LENGTH_SHORT).show();
                        finish();

                    }

                }
                else{

                    Toast.makeText(CategoryActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();

                }

                loadingdialog.dismiss();

            }
        });

    }


    private void addNewCategory(final String title){

        addCatDialog.dismiss();
        loadingdialog.show();

        Map<String,Object> catData = new ArrayMap<>();
        catData.put("NAME",title);
        catData.put("SETS",0);
        catData.put("COUNTER", "1");

        final String doc_id = firestore.collection("QUIZ").document().getId();

        firestore.collection("QUIZ").document(doc_id).set(catData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Map<String,Object> catDoc = new ArrayMap<>();
                catDoc.put("CAT"+String.valueOf(catList.size()+1) + "_NAME" , title);
                catDoc.put("CAT"+String.valueOf(catList.size()+1) + "_ID" , doc_id);
                catDoc.put("COUNT", catList.size()+1);

                firestore.collection("QUIZ").document("Category").update(catDoc)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(CategoryActivity.this,"Category added Successfully",Toast.LENGTH_SHORT).show();

                        catList.add(new CategoryModel(doc_id,title,"0","1"));
                        adapter.notifyItemInserted(catList.size());
                        loadingdialog.dismiss();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(CategoryActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        loadingdialog.dismiss();

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(CategoryActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                loadingdialog.dismiss();

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadingdialog.show();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser!= null){
            Intent intent = new Intent(this, CategoryActivity.class);
            Toast.makeText(CategoryActivity.this,"Welcome again!",Toast.LENGTH_SHORT).show();
            loadingdialog.dismiss();

        }else{
            startActivity(new Intent(this,MainActivity.class));
            loadingdialog.dismiss();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId()){
            case R.id.logout:
                firebaseAuth.signOut();
                Intent intent = new Intent(CategoryActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                return true;
        }


        //if(item.getItemId() == android.R.id.home){
        //    CategoryActivity2.this.finish();
        // }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            finish();
        } else {
            Toast.makeText(this, "Press back again to Exit", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }


}