package edu.ucsb.cs184.moments.moments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Calendar;

public class AddCommentDialog extends DialogFragment {

    private EditText edit_comment, parent_edit_comment;
    private FullPostCommentsFragment fragment;
    private ImageButton camera, gallery, at, hashtag, send;
    private Post post;
    private Comment comment; // Parent Comment
    private View.OnClickListener onSendListener;

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_comment,container,false);
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
        edit_comment = view.findViewById(R.id.ac_editcomment);
        camera = view.findViewById(R.id.ac_camera);
        gallery = view.findViewById(R.id.ac_gallery);
        at = view.findViewById(R.id.ac_at);
        hashtag = view.findViewById(R.id.ac_hashtag);
        send = view.findViewById(R.id.ac_send);

        edit_comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Boolean hasText = s.toString().trim().length() != 0;
                send.setClickable(hasText);
                send.setImageResource(hasText ? R.drawable.ic_add_comment : R.drawable.ic_add_comment_unclickable);
                if (parent_edit_comment != null) parent_edit_comment.setText(s);
            }
        });
        at.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = edit_comment.getText() + "@";
                edit_comment.setText(text);
                edit_comment.setSelection(text.length());
            }
        });
        hashtag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = edit_comment.getText() + "##";
                edit_comment.setText(text);
                edit_comment.setSelection(text.length() - 1);
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = edit_comment.getText().toString();
                if (content.trim().length() == 0) return;
                Comment new_comment = null;
                if (post != null){
                    new_comment = new Comment(User.user.getId(), content, Calendar.getInstance().getTimeInMillis(), post.GetKey());
                    post.addComment(new_comment);
                }
//                else if (comment != null){
//                    new_comment = new Comment(User.user.getId(), content, Calendar.getInstance().getTimeInMillis(), comment.GetKey());
//                    comment.addReply(new_comment);
//                }
                if (!User.user.isUser(new_comment.GetPosterId()))
                    User.findUser(new_comment.GetPosterId()).CommentNotification(new_comment, false);
                fragment.addElement(new_comment);
                if (onSendListener != null) onSendListener.onClick(v);
                edit_comment.setText("");
                dismiss();
            }
        });
        return view;
    }

    public void setPost(Post post) { this.post = post; }
    public void setComment(Comment comment) { this.comment = comment; }

    public void setCaller(EditText editText, FullPostCommentsFragment fragment){
        parent_edit_comment = editText;
        this.fragment = fragment;
    }

    public void setOnSendListener(View.OnClickListener onSendListener){
        this.onSendListener = onSendListener;
    }
}
