package edu.ucsb.cs184.moments.moments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

public class CreateGroupActivity extends AppCompatActivity {

    private ImageView icon;
    private ImageButton camera;
    private ImageButton cancel;
    private ImageButton finishButton;
    private EditText name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        icon = findViewById(R.id.cg_group_icon);
        camera = findViewById(R.id.cg_camera);
        cancel = findViewById(R.id.cg_cancel);
        finishButton = findViewById(R.id.cg_finish);
        name = findViewById(R.id.cg_groupname);

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadIcon();
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadIcon();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
            }
        });
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!finishClickable()) return;
                Group group = new Group(name.getText().toString(), User.user);
                FirebaseHelper.setAfterGroupInsertionListener(new FirebaseHelper.AfterGroupInsertedListener() {
                    @Override
                    public void afterGroupInserted(Group group) {
                        User.user.createGroup(group.getId());
                        FirebaseHelper.uploadIcon(((BitmapDrawable) icon.getDrawable()).getBitmap(), FirebaseHelper.GROUP_ICON, group.getId());
                        Intent intent = new Intent(getApplicationContext(), GroupsFragment.class);
                        intent.putExtra(GroupPostsActivity.GROUP, group);
                        setResult(RESULT_OK, intent);
                        overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
                        finish();
                    }
                });
                FirebaseHelper.insertGroup(group);
            }
        });
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Boolean clickable = finishClickable();
                finishButton.setClickable(clickable);
                finishButton.setImageResource(clickable ? R.drawable.ic_tick : R.drawable.ic_tick_unclickable);
            }
        });
    }

    private Boolean finishClickable(){
        return (name.getText().toString().trim().length() != 0) && (name.getText().toString().length() <= 30);
    }

    private void uploadIcon(){
        Intent uu = new Intent(CreateGroupActivity.this, UploadIconActivity.class);
        uu.putExtra(UploadIconActivity.ICON, ((BitmapDrawable) icon.getDrawable()).getBitmap());
        startActivityForResult(uu, UploadIconActivity.ICON_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == UploadIconActivity.ICON_CODE){
            icon.setImageBitmap((Bitmap) data.getParcelableExtra(UploadIconActivity.ICON));
        }
    }
}
