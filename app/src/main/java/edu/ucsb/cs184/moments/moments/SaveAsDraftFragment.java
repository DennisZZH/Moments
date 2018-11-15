package edu.ucsb.cs184.moments.moments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Date;

public class SaveAsDraftFragment extends DialogFragment {

    private AppCompatActivity activity;
    private Post post;
    private Button yes;
    private Button no;

    public void setActivity(AppCompatActivity activity) { this.activity = activity; }
    public void setPost(Post post) { this.post = post; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_save_as_draft,container,false);
        yes = view.findViewById(R.id.draft_yes);
        no = view.findViewById(R.id.draft_no);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAsDraft();
                close();
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
        return view;
    }

    private void close(){
        SaveAsDraftFragment.this.dismiss();
        activity.finish();
    }

    private void saveAsDraft(){

    }
}