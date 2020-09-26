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
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.Map;

import static com.example.myquizadmin.CategoryActivity.catList;
import static com.example.myquizadmin.CategoryActivity.selected_cat_index;
import static com.example.myquizadmin.SetsActivity.selected_set_index;
import static com.example.myquizadmin.SetsActivity.selected_set_index;

public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.ViewHolder> {

    private List<String> setIDs;

    public SetsAdapter(List<String> setIDs) {
        this.setIDs = setIDs;
    }

    @NonNull
    @Override
    public SetsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_item_layout,parent,false);
        //used category item layout on top line
        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetsAdapter.ViewHolder holder, int position) {

        String setID = setIDs.get(position);
        holder.setData(position , setID , this);

    }

    @Override
    public int getItemCount() {
        return setIDs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView setName;
        private ImageView deleteSetB;
        private Dialog loadingdialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            setName = itemView.findViewById(R.id.catName);
            deleteSetB = itemView.findViewById(R.id.catDeleteB);

            loadingdialog = new Dialog(itemView.getContext());
            loadingdialog.setContentView(R.layout.loading_progressbar);
            loadingdialog.setCancelable(false);
            loadingdialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingdialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


        }

        private void setData(final int position , final String setID , final SetsAdapter adapter) {

            //display position as set1 , set2
            setName.setText("Quiz " + String.valueOf(position + 1));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected_set_index = position;
                    Intent intent = new Intent(itemView.getContext(),QuestionsActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });


            deleteSetB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Quiz")
                            .setMessage("Do you want to delete this Quiz ?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    deleteQuiz(position , setID ,  itemView.getContext() , adapter);

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
        private void deleteQuiz(final int position , final String setID , final Context context , final SetsAdapter adapter){

            loadingdialog.show();

            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                    .collection(setID).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            WriteBatch batch = firestore.batch();
                            for(QueryDocumentSnapshot doc : queryDocumentSnapshots){

                                batch.delete(doc.getReference());
                            }
                            //deleting documents
                            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Map<String,Object> catDoc = new ArrayMap<>();
                                    int index = 1;
                                    for(int i = 0 ; i<setIDs.size();i++){

                                        if(i != position){

                                            catDoc.put("SET"+String.valueOf(index)+"_ID",setIDs.get(i));
                                            index++;
                                        }
                                    }

                                    catDoc.put("SETS", index-1 );

                                    firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                                            .update(catDoc)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    Toast.makeText(context,"Quiz Deleated Successfully",Toast.LENGTH_SHORT).show();

                                                    SetsActivity.setsIDs.remove(position);

                                                    catList.get(selected_cat_index).setNoOfSets(String.valueOf(SetsActivity.setsIDs.size()));

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
