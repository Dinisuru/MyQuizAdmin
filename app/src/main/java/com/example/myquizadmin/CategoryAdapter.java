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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {


    private List<CategoryModel> cat_List;

    public CategoryAdapter(List<CategoryModel> cat_List) {
        this.cat_List = cat_List;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_item_layout,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {

        String title = cat_List.get(position).getName();

        holder.setData(title, position,this);

    }

    @Override
    public int getItemCount() {
        return cat_List.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView catName;
        private ImageView deleteB;
        private Dialog loadingdialog;
        private Dialog editDialog;
        private EditText tv_editName;
        private Button updatecatButton;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            catName = itemView.findViewById(R.id.catName);
            deleteB = itemView.findViewById(R.id.catDeleteB);

            loadingdialog = new Dialog(itemView.getContext());
            loadingdialog.setContentView(R.layout.loading_progressbar);
            loadingdialog.setCancelable(false);
            loadingdialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingdialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

            editDialog = new Dialog(itemView.getContext());
            editDialog.setContentView(R.layout.edit_category_dialog);
            editDialog.setCancelable(true);
            editDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

            tv_editName = editDialog.findViewById(R.id.addcat_cat_name);
            updatecatButton = editDialog.findViewById(R.id.addcat_add_button);

        }
        private void setData(String title, final int position , final CategoryAdapter adapter){

            catName.setText(title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    CategoryActivity.selected_cat_index = position;
                    Intent intent =new Intent(itemView.getContext(), SetsActivity.class);
                    itemView.getContext().startActivity(intent);

                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    tv_editName.setText(cat_List.get(position).getName());
                    editDialog.show();

                    return false;
                }
            });

            updatecatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(tv_editName.getText().toString().isEmpty()){
                        tv_editName.setError("Enter Category Name");
                        return;
                    }

                    updateCategory(tv_editName.getText().toString(), position, itemView.getContext(), adapter);
                }
            });




            deleteB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Category")
                            .setMessage("Do you want to delete this category ?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    deleteCategory(position , itemView.getContext() , adapter);

                                }
                            }).setNegativeButton("Cancel" , null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                    dialog.getButton(dialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0,0,50,0);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setLayoutParams(params);

                }
            });

        }

        private void deleteCategory(final int id , final Context context, final CategoryAdapter adapter){

            loadingdialog.show();

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();


            Map<String,Object> catDoc = new ArrayMap<>();

            int index = 1;

            for(int  i = 0; i<cat_List.size(); i++ ){

                if(i != id){
                    catDoc.put("CAT"+String.valueOf(index) + "_ID",cat_List.get(i).getId());
                    catDoc.put("CAT"+String.valueOf(index) + "_NAME",cat_List.get(i).getName());
                    index++;
                }

            }

            catDoc.put("COUNT", index-1);
            firestore.collection("QUIZ").document("Category")
                    .set(catDoc)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(context,"Category deleted sucessfully",Toast.LENGTH_SHORT).show();
                            CategoryActivity.catList.remove(id);

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

        private void updateCategory(final String catNewName, final int position, final Context context, final CategoryAdapter adapter){

            editDialog.dismiss();
            loadingdialog.show();

            Map<String , Object> catData = new ArrayMap<>();
            catData.put("NAME",catNewName);

            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("QUIZ").document(cat_List.get(position).getId())
                    .update((catData))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Map<String,Object> catDoc = new ArrayMap<>();
                            catDoc.put("CAT"+ String.valueOf(position + 1) + "_NAME",catNewName);

                            firestore.collection("QUIZ").document("Category")
                                    .update(catDoc)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Toast.makeText(context,"Category name changed Successfully",Toast.LENGTH_SHORT).show();
                                            CategoryActivity.catList.get(position).setName(catNewName);
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
