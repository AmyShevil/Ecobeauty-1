package com.example.ecobeauty.mycosmetics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import com.example.ecobeauty.R;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import java.util.ArrayList;
import static com.example.ecobeauty.mycosmetics.DatabaseHelper2.*;
import static com.example.ecobeauty.mydeparture.Constants.BASIC_NAME_USER_WISH;
import static java.lang.String.*;


public class WishList extends AppCompatActivity {

    private FloatingActionMenu fam;
    ListView wishList;
    DatabaseHelper2 databaseHelper;
    SQLiteDatabase db;
    Cursor cursor;
    SimpleCursorAdapter adapter;
    ArrayList<Long> idCheckedProduct;
    ArrayList<String> nameCheckedProduct;
    int numberCountNameUserWish, nameIndex;
    String nameUserWish, stringCountNameUserWish, uid, nameWish, nameCheck, nameMove;
    DatabaseReference myRef;
    boolean containsWord;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        fam = (FloatingActionMenu) findViewById(R.id.fab_menu);
        wishList = (ListView) findViewById(R.id.wishlist);
        wishList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);


        fam.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (fam.isOpened()) {
                    fam.close(true);
                }
            }
        });


        databaseHelper = new DatabaseHelper2(getApplicationContext());
        wishList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                      containsWord = idCheckedProduct.contains(id);
                        if (containsWord){
                            idCheckedProduct.remove(id);
                        }
                        else{
                            idCheckedProduct.add(id);
                        }
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance(getString(R.string.dataBase));
        myRef = database.getReference(uid);
        stringCountNameUserWish = Integer.toString(numberCountNameUserWish);
        nameUserWish = BASIC_NAME_USER_WISH + stringCountNameUserWish;
    }


   @Override
   public void onResume() {
       super.onResume();
       db = databaseHelper.getReadableDatabase();
       cursor =  db.rawQuery("select * from "+ TABLE_W + " order by "+ DatabaseHelper2.COLUMN_NAME2, null);

       if (cursor.getCount()>=1) {
           numberCountNameUserWish =0;
           for (int i = 0; i < cursor.getCount(); i++) {
               while (cursor.moveToNext()) {
                   numberCountNameUserWish++;
                   stringCountNameUserWish = Integer.toString(numberCountNameUserWish);
                   nameUserWish = BASIC_NAME_USER_WISH + stringCountNameUserWish;
                   nameIndex = cursor.getColumnIndex(COLUMN_NAME2);
                   nameWish = cursor.getString(nameIndex);
                   myRef.child("UserWishes").child(nameUserWish).setValue(new UserWish(nameWish));
                   }
               }
           }

       String[] headers = new String[] {COLUMN_NAME2};
       adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_multiple_choice,
               cursor, headers, new int[]{android.R.id.text1});
       wishList.setAdapter(adapter);
   }

     public void add(View view) {
         EditText nameEditText = (EditText) findViewById(R.id.entername);
         db = databaseHelper.getWritableDatabase();
         String name = nameEditText.getText().toString();
         ContentValues cv = new ContentValues();
         cv.put(COLUMN_NAME2, name);
         db.insert(TABLE_W, null, cv);
         nameEditText.setText("");
         onResume();
     }


   public void remove(View view) {

       nameCheckedProduct = new ArrayList<String>();
       DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
       nameCheck ="";

       for(int i=0;i<idCheckedProduct.size();i++) {

           cursor = db.rawQuery("select * from " + DatabaseHelper2.TABLE_W +
                           " where " + DatabaseHelper2.COLUMN_ID2 + " like ?" ,
                   new String[]{"%" + idCheckedProduct.get(i)} );
           if(cursor.getCount() >= 1) {

               while (cursor.moveToNext()) {

                   int nameIndex = cursor.getColumnIndex(COLUMN_NAME2);
                   nameCheck = cursor.getString(nameIndex);
                   nameCheckedProduct.add(nameCheck);
               }
           }
       }

       for(int i = 0; i< nameCheckedProduct.size(); i++){
       Query query = rootRef
               .child(uid)
               .child("UserWishes")
               .orderByChild("name")
               .equalTo(nameCheck);
       query.getRef().removeValue();

       }

       db = databaseHelper.getWritableDatabase();
      for(int i=0;i<idCheckedProduct.size();i++){
          db.delete(TABLE_W, "_id = ?", new String[]{valueOf(idCheckedProduct.get(i))});
      }
      onResume();
   }

    public void move(View view) {

        db = databaseHelper.getWritableDatabase();
        nameCheckedProduct = new ArrayList<>();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        for(int i=0;i<idCheckedProduct.size();i++) {
            cursor = db.rawQuery("select * from " + DatabaseHelper2.TABLE_W +
                            " where " + DatabaseHelper2.COLUMN_ID2 + " like ?" ,
                    new String[]{"%" + idCheckedProduct.get(i)} );
            if(cursor.getCount() >= 1) {
                while (cursor.moveToNext()) {
                    nameIndex = cursor.getColumnIndex(COLUMN_NAME2);
                    nameMove = cursor.getString(nameIndex);
                    nameCheckedProduct.add(nameMove);
                }
            }
        }

        for(int i = 0; i< nameCheckedProduct.size(); i++){
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper2.COLUMN_NAME, nameCheckedProduct.get(i));
            db.insert(TABLE, null, cv);
        }

        for(int i = 0; i< nameCheckedProduct.size(); i++){
            Query query = rootRef
                    .child(uid)
                    .child("UserWishes")
                    .orderByChild("name")
                    .equalTo(nameMove);
            query.getRef().removeValue();
        }

        for(int i=0;i<idCheckedProduct.size();i++) {
            db.delete(TABLE_W, "_id = ?", new String[]{valueOf(idCheckedProduct.get(i))});
        }
        onResume();
     }

    public void homeCosmetics(View view) {

        Intent intent = new Intent(this, MyCosmeticsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        db.close();
        cursor.close();
    }
}
