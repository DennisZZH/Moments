package edu.ucsb.cs184.moments.moments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.jude.swipbackhelper.SwipeBackHelper;

public class GroupSettingsActivity extends AppCompatActivity {

    public static final String GROUP = "Group", QUIT = "Quit";
    private static final String SETTINGS = "Settings";
    public static Group group;
    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SwipeBackHelper.onCreate(this);
        setContentView(R.layout.activity_group_settings);
        Intent intent = getIntent();
        group = intent.getParcelableExtra(GroupPostsActivity.GROUP);

        back = findViewById(R.id.gs_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
            }
        });

        Fragment fragment = new SettingsFragment();
        ((SettingsFragment) fragment).setData(group);
        // this fragment must be from android.app.Fragment,
        // if you use support fragment, it will not work

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (savedInstanceState == null) {
            // when saved instance state is null, that means
            // activity is created for the first time, so basically
            // add the fragment to activity if and only if activity is new
            // when activity rotates, do nothing
            transaction.add(R.id.gs_content, fragment, SETTINGS);
        }
        transaction.commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SwipeBackHelper.onPostCreate(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SwipeBackHelper.onDestroy(this);
    }

    public static class SettingsFragment extends PreferenceFragment {
        private Preference icon, number, member, sortby, quit;
        private EditTextPreference name, intro;
        private boolean init = false;
        private Group group;
        private Activity activity;
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // here we should call settings ui
            addPreferencesFromResource(R.xml.group_preferences);
            icon = findPreference("Group Icon");
            name = (EditTextPreference) findPreference("Group Name");
            name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String data = newValue.toString();
                    group.modifyName(data);
                    name.setSummary(data);
                    return true;
                }
            });
            number = findPreference("Group Number");
            member = findPreference("Group Members");
            intro = (EditTextPreference) findPreference("Group Intro");
            sortby = findPreference(getString(R.string.sort_by));
            quit = findPreference(getString(R.string.quit_group));
            icon.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getContext(), UploadIconActivity.class);
                    intent.putExtra(UploadIconActivity.CALLER, UploadIconActivity.GROUP);
                    intent.putExtra(UploadIconActivity.GROUP, group);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
                    return true;
                }
            });
            intro.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String data = newValue.toString();
                    group.modifyIntro(data);
                    intro.setSummary(data);
                    return true;
                }
            });
            activity = getActivity();
            init = true;
            if (group != null) setData(group);
        }

        public void setData(Group data){
            group = data;
            if (!init) return;
            name.setSummary(group.getName());
            name.setText(group.getName());
            number.setSummary(group.getNumber() + "");
            member.setSummary(group.getMemberSize() + "");
            member.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(activity, SearchListActivity.class);
                    intent.putExtra(SearchListActivity.ADAPTER, SearchListActivity.MEMBER);
                    intent.putExtra(SearchListActivity.DATA, group.getMembers());
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
                    return true;
                }
            });
            // TODO: save group settings
            sortby.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    sortby.setSummary(newValue.toString());
                    return true;
                }
            });
            if (!group.getIntro().isEmpty()) intro.setSummary(group.getIntro());
            intro.setText(group.getIntro());
            quit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    User.user.quitGroup(group.getId());
                    activity.finish();
                    activity.overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
                    Intent intent = new Intent();
                    intent.putExtra(QUIT, QUIT);
                    activity.setResult(RESULT_OK, intent);
                    return true;
                }
            });
        }
    }
}
