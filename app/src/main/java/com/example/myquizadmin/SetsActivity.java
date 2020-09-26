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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.myquizadmin.CategoryActivity.catList;
import static com.example.myquizadmin.CategoryActivity.selected_cat_index;

public class SetsActivity extends AppCompatActivity {

    private RecyclerView setsView;
    private Button addSetB;
    private SetsAdapter adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingdialog;
    public static List<String> setsIDs = new ArrayList<>();
    public static int selected_set_index =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);

        Toolbar toolbar = findViewById(R.id.sa_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Quizzes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setsView = findViewById(R.id.sets_recycler);
        addSetB = findViewById(R.id.addSetB);

        addSetB.setText("Add New Quiz");

        loadingdialog = new Dialog(SetsActivity.this);
        loadingdialog.setContentView(R.layout.loading_progressbar);
        loadingdialog.setCancelable(false);
        loadingdialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingdialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


        addSetB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            addNewSet();

            }
        });

        firestore = FirebaseFirestore.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setsView.setLayoutManager(layoutManager);

        loadSets();

    }

    private void loadSets(){

        setsIDs.clear();

        loadingdialog.show();

        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                long noOfSets = (long)documentSnapshot.get("SETS");

                for(int i = 1; i<= noOfSets; i++){

                    setsIDs.add(documentSnapshot.getString("SET"+String.valueOf(i)+"_ID"));
                }

                catList.get(selected_cat_index).setSetCounter(documentSnapshot.getString("COUNTER"));
                catList.get(selected_cat_index).getNoOfSets(String.valueOf(noOfSets));

                adapter = new SetsAdapter(setsIDs);
                setsView.setAdapter(adapter);

                loadingdialog.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(SetsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                loadingdialog.dismiss();

            }
        });

    }

    private void addNewSet(){

        loadingdialog.show();

        final String current_cat_id = catList.get(selected_cat_index).getId();
        final String current_counter = catList.get(selected_cat_index).getSetCounter();
        //question data
        Map<String,Object> qData = new ArrayMap<>();
        qData.put("COUNT","0");

        firestore.collection("QUIZ").document(current_cat_id)
                .collection(current_counter).document("QUESTIONS_LIST")
                .set(qData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Map<String,Object> catDoc = new ArrayMap<>();
                        catDoc.put("COUNTER", String.valueOf(Integer.valueOf(current_counter)+1));
                        catDoc.put("SET" + String.valueOf(setsIDs.size() + 1)+"_ID", current_counter);
                        catDoc.put("SETS", setsIDs.size()+1);

                        firestore.collection("QUIZ").document(current_cat_id)
                                .update(catDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(SetsActivity.this,"Quiz Added Successfully",Toast.LENGTH_SHORT).show();

                                        setsIDs.add(current_counter);
                                        catList.get(selected_cat_index).setNoOfSets(String.valueOf(setsIDs.size()));
                                        catList.get(selected_cat_index).setSetCounter(String.valueOf(Integer.valueOf(current_counter)+1));

                                        adapter.notifyItemInserted(setsIDs.size());
                                        loadingdialog.dismiss();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(SetsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                loadingdialog.dismiss();

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(SetsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                loadingdialog.dismiss();

            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            finish();
         }

        return super.onOptionsItemSelected(item);
    }

}