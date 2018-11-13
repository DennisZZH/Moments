package edu.ucsb.cs184.moments.moments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NotificationsFragment extends Fragment {

    private Context context;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabPagerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications,container,false);
        context = getContext();
        mViewPager = view.findViewById(R.id.n_viewpager);
        mTabLayout = view.findViewById(R.id.nTablayout);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        adapter = new TabPagerAdapter(getFragmentManager());
        adapter.addFragment(new AtMeFragment(), getString(R.string.atme));
        adapter.addFragment(new CommentsFragment(), getString(R.string.comments));
        adapter.addFragment(new RatingsFragment(), getString(R.string.ratings));
        adapter.addFragment(new MessagesFragment(), getString(R.string.messages));
        mViewPager.setAdapter(adapter);
        return view;
    }
}