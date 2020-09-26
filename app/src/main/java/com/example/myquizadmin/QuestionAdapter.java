package com.example.myquizadmin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import static com.example.myquizadmin.CategoryActivity.catList;
import static com.example.myquizadmin.CategoryActivity.selected_cat_index;
import static com.example.myquizadmin.QuestionsActivity.quesList;
import static com.example.myquizadmin.SetsActivity.selected_set_index;
import static com.example.myquizadmin.SetsActivity.setsIDs;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {

    private List<QuestionModel> ques_List;

    public QuestionAdapter(List<QuestionModel> ques_List) {
        this.ques_List = ques_List;
    }

    @NonNull
    @Override
    public QuestionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_item_layout,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionAdapter.ViewHolder holder, int position) {

        holder.setData(position , this);
    }
    @Override
    public int getItemCount() {
        return ques_List.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private ImageView deleteB;
        private Dialog loadingdialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

        title = itemView.findViewById(R.id.catName);
        deleteB = itemView.findViewById(R.id.catDeleteB);

            loadingdialog = new Dialog(itemView.getContext());
            loadingdialog.setContentView(R.layout.loading_progressbar);
            loadingdialog.setCancelable(false);
            loadingdialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingdialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        }

        private void setData(final int position , final QuestionAdapter adapter){

            title.setText("QUESTION "+ String.valueOf(position+1));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(itemView.getContext(),QuestionDetailsActivity.class);
                    intent.putExtra("ACTION","EDIT");
                    intent.putExtra("Q_ID",position);
                    itemView.getContext().startActivity(intent);

                }
            });

            deleteB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Question")
                            .setMessage("Do you want to delete this Quiz ?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    deleteQuestion(position , itemView.getContext() , adapter);

                                }
                            }).setNegativeButton("Cancel", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                    dialog.getButton(dialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 50, 0);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setLayoutParams(params);


                }
            });


        }

        private void deleteQuestion(final int position, final Context context , final QuestionAdapter adapter){

        loadingdialog.show();

            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                    .collection(setsIDs.get(selected_set_index)).document(quesList.get(position).getQuesID())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Map<String ,Object> quesDoc = new ArrayMap<>();
                            int index = 1;

                            for(int i = 0; i < quesList.size();i++) {

                                if (i != position) {
                                    // change question id's except the question that we want to delete
                                    quesDoc.put("Q" + String.valueOf(index) + "_ID", quesList.get(i).getQuesID());
                                    index++;

                                }
                            }

                                quesDoc.put("COUNT" , String.valueOf(index-1));

                                firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                                        .collection(setsIDs.get(selected_set_index)).document("QUESTIONS_LIST")
                                        .set(quesDoc)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Toast.makeText(context ,"Question Dealeted Successfully ", Toast.LENGTH_SHORT).show();

                                                quesList.remove(position);
                                                adapter.notifyDataSetChanged();

                                                loadingdialog.dismiss();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                                        loadingdialog.dismiss();

                                    }
                                });



                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                    loadingdialog.dismiss();


                }
            });

        }
    }
}
