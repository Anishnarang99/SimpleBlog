package com.pencilbox.simpleblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static android.R.attr.data;

public class PostActivity extends AppCompatActivity {


     private ImageButton mSelectImage;
    private EditText mPostTitle;
    private  EditText mPostDesc;
    private Button mSubmitBtn;
    private Uri mImageUri=null;
    private StorageReference mStorageReference;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mDatabaseReference;

    private static final int GALLERY_REQUEST=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mPostTitle=(EditText)findViewById(R.id.titleField);
        mPostDesc=(EditText)findViewById(R.id.descField);
        mSelectImage=(ImageButton)findViewById(R.id.imageSelect);
        mSubmitBtn=(Button)findViewById(R.id.submitBtn);
        mStorageReference= FirebaseStorage.getInstance().getReference();
        mProgressDialog= new ProgressDialog(this);
        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Blog");
        
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }

            private void startPosting() {

                mProgressDialog.setMessage("Posting...");
                mProgressDialog.show();

                final String title_val=mPostTitle.getText().toString().trim();
                final String desc_val=mPostDesc.getText().toString().trim();
                if(!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && mImageUri!=null)
                {
                    StorageReference filepath= mStorageReference.child("Blog_Images").child(mImageUri.getLastPathSegment());
                    filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl= taskSnapshot.getDownloadUrl();
                            DatabaseReference newPost= mDatabaseReference.push();
                            newPost.child("title").setValue(title_val);
                            newPost.child("description").setValue(desc_val);
                            newPost.child("image").setValue(downloadUrl.toString());


                            mProgressDialog.dismiss();

                            startActivity(PostActivity.this,MainActivity.class);
                        }
                    });



                }
            }
        });

        mSelectImage = (ImageButton) findViewById(R.id.imageSelect);
        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });
    }
    


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK)

        {
            mImageUri = data.getData();
            mSelectImage.setImageURI(mImageUri);
        }
    }

}
