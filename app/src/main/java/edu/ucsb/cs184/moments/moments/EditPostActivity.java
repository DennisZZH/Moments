package edu.ucsb.cs184.moments.moments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.Calendar;

public class EditPostActivity extends AppCompatActivity {

    public static final String POST = "Post", GROUP = "Group", DRAFT = "Draft";
    public static final int EDIT_POST = 0;

    private ImageButton back, send;
    private ImageButton hashtag, at;
    private ImageView usericon;
    private EditText edit_content;
    private Intent intent;
    private Group group;
    private Post draft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        intent = getIntent();

        edit_content = findViewById(R.id.edit_content);
        usericon = findViewById(R.id.edit_usericon);
        back = findViewById(R.id.edit_cancel);
        send = findViewById(R.id.edit_send);
        at = findViewById(R.id.edit_at);
        hashtag = findViewById(R.id.edit_hashtag);
        group = intent.getParcelableExtra(GROUP);
        draft = intent.getParcelableExtra(DRAFT);

        edit_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Boolean hasText = hasText();
                send.setClickable(hasText);
                send.setImageResource(hasText ? R.drawable.ic_send : R.drawable.ic_send_unclickable);
            }
        });
        FirebaseHelper.setIcon(User.user.GetIcon(), this, usericon);
        if (draft != null) edit_content.setText(draft.getContent());
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = edit_content.getText().toString();
                if (content.isEmpty()){
                    close();
                }else {
                    // ask save to draft box
                    String message = getString(draft == null ? R.string.save_as_draft : R.string.update_draft);
                    final AskYesNoDialog askYesNoDialog = new AskYesNoDialog();
                    askYesNoDialog.showNow(getSupportFragmentManager(), message);
                    askYesNoDialog.setMessage(message);
                    askYesNoDialog.setOnYesListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (draft != null) User.user.removeDraft(draft);
                            User.user.addDraft(getPost());
                            close();
                        }
                    });
                    askYesNoDialog.setOnNoListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            close();
                        }
                    });
                }
            }
        });
        at.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = edit_content.getText() + "@";
                edit_content.setText(text);
                edit_content.setSelection(text.length());
            }
        });
        hashtag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = edit_content.getText() + "##";
                edit_content.setText(text);
                edit_content.setSelection(text.length() - 1);
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = edit_content.getText().toString();
                if (content.trim().isEmpty()) return;
                Intent callBack = new Intent();
                Post post = getPost();
                for (String name: post.findAtUsers()){
                    User user = User.findUserWithName(name);
                    if (user != null)
                        if (post.postedInGroup())
                            if (group.hasMember(user.getId()))
                                user.atMe(post);
                        else
                            user.atMe(post);
                }
                callBack.putExtra(POST, post);
                if (group == null) User.user.addPost(post);
                else group.addPost(post);
                if (draft != null) User.user.removeDraft(draft);
                setResult(RESULT_OK, callBack);
                close();
            }
        });
    }

    private Post getPost(){
        if (group == null)
            return new Post(User.user.getId(), edit_content.getText().toString(), Calendar.getInstance().getTimeInMillis());
        else
            return new Post(User.user.getId(), edit_content.getText().toString(), Calendar.getInstance().getTimeInMillis(), group.getId());
    }

    private boolean hasText(){
        return !edit_content.toString().trim().isEmpty();
    }

    private void close(){
        finish();
        overridePendingTransition(R.anim.push_down_in,R.anim.push_down_out);
    }
}
