package com.example.ivana.trainapptfg;

import android.graphics.PorterDuff;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.example.ivana.trainapptfg.fragments.FourFragment;
import com.example.ivana.trainapptfg.fragments.ReconocerActividadFragment;
import com.example.ivana.trainapptfg.fragments.ThreeFragment;
import com.example.ivana.trainapptfg.fragments.TwoFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private int[] icons = {
            R.drawable.ic_home_black_24dp,
            R.drawable.ic_directions_run_black_24dp,
            R.drawable.ic_today_black_24dp,
            R.drawable.ic_history_black_24dp,
            R.drawable.ic_group_black_24dp
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_dehaze_black_24dp);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_list);






        viewPager = (ViewPager) findViewById(R.id.viewpager);
        initViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorWhite));
        initTabsWithIcons();

        //TODO INVOCAR A REMOVEONTABSLECTEDLISTENER
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ReconocerActividadFragment(), "ACTIVIDADES");
        adapter.addFragment(new TwoFragment(), "CALENDARIO");
        adapter.addFragment(new ThreeFragment(), "HISTORIAL");
        adapter.addFragment(new FourFragment(), "TWITAPP");

        viewPager.setAdapter(adapter);
    }

    private void initTabsWithIcons(){
        tabLayout.getTabAt(0).setIcon(icons[1]);
        tabLayout.getTabAt(0).getIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN);

        tabLayout.getTabAt(1).setIcon(icons[2]);
        tabLayout.getTabAt(2).setIcon(icons[3]);
        tabLayout.getTabAt(3).setIcon(icons[4]);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //return mFragmentTitleList.get(position);
            return null;
        }
    }

}
