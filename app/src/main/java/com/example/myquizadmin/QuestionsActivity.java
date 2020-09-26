package com.example.myquizadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.myquizadmin.CategoryActivity.catList;
import static com.example.myquizadmin.CategoryActivity.selected_cat_index;
import static com.example.myquizadmin.SetsActivity.selected_set_index;
import static com.example.myquizadmin.SetsActivity.setsIDs;

public class QuestionsActivity extends AppCompatActivity {

    private RecyclerView quesView;
    private Button addQB;
    public static List<QuestionModel> quesList = new ArrayList<>();
    private QuestionAdapter adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingdialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        Toolbar toolbar = findViewById(R.id.q_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Questions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        quesView = findViewById(R.id.questi_recycler);
        addQB = findViewById(R.id.addQuesB);

        loadingdialog = new Dialog(QuestionsActivity.this);
        loadingdialog.setContentView(R.layout.loading_progressbar);
        loadingdialog.setCancelable(false);
        loadingdialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingdialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


        addQB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(QuestionsActivity.this,QuestionDetailsActivity.class);
                intent.putExtra("ACTION", "ADD");
                startActivity(intent);

            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        quesView.setLayoutManager(layoutManager);

        firestore = FirebaseFirestore.getInstance();

        loadQuestion();

    }

    private void loadQuestion(){

        quesList.clear();

        loadingdialog.show();

        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .collection(setsIDs.get(selected_set_index)).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Map<String,QueryDocumentSnapshot> docList = new ArrayMap<>();

                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots){

                            docList.put(doc.getId(),doc);
                        }

                        QueryDocumentSnapshot quesListDoc = docList.get("QUESTIONS_LIST");

                        String count = quesListDoc.getString("COUNT");

                        for(int i=0; i < Integer.valueOf(count);i++){

                            String quesID = quesListDoc.getString("Q" + String.valueOf(i+1)+"_ID");

                            QueryDocumentSnapshot quesDoc = docList.get(quesID);

                            quesList.add(new QuestionModel(
                                    quesID,
                                    quesDoc.getString("QUESTION"),
                                    quesDoc.getString("A"),
                                    quesDoc.getString("B"),
                                    quesDoc.getString("C"),
                                    quesDoc.getString("D"),
                                    Integer.valueOf(quesDoc.getString("ANSWER"))));

                        }

                        adapter = new QuestionAdapter(quesList);
                        quesView.setAdapter(adapter);
                        loadingdialog.dismiss();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(QuestionsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                loadingdialog.dismiss();

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}