package com.example.myquizadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

import static com.example.myquizadmin.CategoryActivity.catList;
import static com.example.myquizadmin.CategoryActivity.selected_cat_index;
import static com.example.myquizadmin.QuestionsActivity.quesList;
import static com.example.myquizadmin.SetsActivity.selected_set_index;
import static com.example.myquizadmin.SetsActivity.setsIDs;

public class QuestionDetailsActivity extends AppCompatActivity {

    private EditText ques ,optionA,optionB,optionC,optionD,answer;
    private Button addQB;
    private String qStr,aStr,bStr,cStr,dStr,ansStr;
    private Dialog loadingdialog;
    private FirebaseFirestore firestore;
    private String action;
    private int qID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_details);

        Toolbar toolbar = findViewById(R.id.qdetails_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        ques = findViewById(R.id.question);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        answer = findViewById(R.id.answer);
        addQB = findViewById(R.id.addQB);

        loadingdialog = new Dialog(QuestionDetailsActivity.this);
        loadingdialog.setContentView(R.layout.loading_progressbar);
        loadingdialog.setCancelable(false);
        loadingdialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingdialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        firestore = FirebaseFirestore.getInstance();

        action = getIntent().getStringExtra("ACTION");

        if(action.compareTo("EDIT")==0){

            qID = getIntent().getIntExtra("Q_ID",0);
            loadData(qID);
            getSupportActionBar().setTitle("Question "+String.valueOf(qID+1));
            addQB.setText("UPDATE");
        }
        else{

            getSupportActionBar().setTitle("Question "+String.valueOf(quesList.size()+1));
            addQB.setText("ADD");

        }
        addQB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                qStr = ques.getText().toString();
                aStr = optionA.getText().toString();
                bStr = optionB.getText().toString();
                cStr = optionC.getText().toString();
                dStr = optionD.getText().toString();
                ansStr = answer.getText().toString();

                if(qStr.isEmpty()){
                    ques.setError("Enter Question");
                    return;
                }else if(aStr.isEmpty()){
                    optionA.setError("Enter Option A");
                    return;
                }else if(bStr.isEmpty()){
                    optionB.setError("Enter Option B");
                    return;
                }else if(cStr.isEmpty()){
                    optionC.setError("Enter Option C");
                    return;
                }else if(dStr.isEmpty()){
                    optionD.setError("Enter Option D");
                    return;
                }else if(aStr.isEmpty()){
                    answer.setError("Enter Answer Number");
                    return;
                }

                if(!answer.getText().toString().matches("[1-4]{1}")){

                    answer.setError("Enter Answer Number Between 1 and 4");
                    return;

                }

                if(action.compareTo("EDIT") == 0) {

                    editQuestion();
                }
                else {
                    addNewQuestion();
                }

            }
        });

    }

    private void addNewQuestion(){

        loadingdialog.show();

        Map<String,Object> quesData = new ArrayMap<>();

        quesData.put("QUESTION",qStr);
        quesData.put("A",aStr);
        quesData.put("B",bStr);
        quesData.put("C",cStr);
        quesData.put("D",dStr);
        quesData.put("ANSWER",ansStr);

        final String doc_id = firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .collection(setsIDs.get(selected_set_index)).document().getId();

        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .collection(setsIDs.get(selected_set_index)).document(doc_id)
                .set(quesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String , Object> quesDoc = new ArrayMap<>();
                        quesDoc.put("Q" + String.valueOf(quesList.size()+1)+"_ID",doc_id);
                        quesDoc.put("COUNT" , String.valueOf(quesList.size()+1));

                        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                                .collection(setsIDs.get(selected_set_index)).document("QUESTIONS_LIST")
                                .update(quesDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(QuestionDetailsActivity.this,"Question Added Successfully",Toast.LENGTH_SHORT).show();
                                        quesList.add(new QuestionModel(doc_id,qStr,aStr,bStr,cStr,dStr,Integer.valueOf(ansStr)));

                                        loadingdialog.dismiss();
                                        QuestionDetailsActivity.this.finish();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(QuestionDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                loadingdialog.dismiss();

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(QuestionDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                loadingdialog.dismiss();

            }
        });

    }

    private void loadData(int id){

        ques.setText(quesList.get(id).getQuestion());
        optionA.setText(quesList.get(id).getOptionA());
        optionB.setText(quesList.get(id).getOptionB());
        optionC.setText(quesList.get(id).getOptionC());
        optionD.setText(quesList.get(id).getOptionD());
        answer.setText(String.valueOf(quesList.get(id).getCorrectAns()));

    }

    private void editQuestion(){

        loadingdialog.show();

        Map<String ,Object> quesData = new ArrayMap<>();
        quesData.put("QUESTION", qStr);
        quesData.put("A",aStr);
        quesData.put("B",bStr);
        quesData.put("C",cStr);
        quesData.put("D",dStr);
        quesData.put("ANSWER",ansStr);

        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .collection(setsIDs.get(selected_set_index)).document(quesList.get(qID).getQuesID())
                .set(quesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(QuestionDetailsActivity.this,"Question Updated Successfully",Toast.LENGTH_SHORT).show();
                        quesList.get(qID).setQuestion(qStr);
                        quesList.get(qID).setOptionA(aStr);
                        quesList.get(qID).setOptionB(bStr);
                        quesList.get(qID).setOptionC(cStr);
                        quesList.get(qID).setOptionD(dStr);
                        quesList.get(qID).setCorrectAns(Integer.valueOf(ansStr));

                        loadingdialog.dismiss();
                        QuestionDetailsActivity.this.finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(QuestionDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
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