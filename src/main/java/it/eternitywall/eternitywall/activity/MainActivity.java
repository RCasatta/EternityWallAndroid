package it.eternitywall.eternitywall.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.util.ArrayList;
import java.util.List;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.TimedStopper;
import it.eternitywall.eternitywall.fragments.AccountFragment;
import it.eternitywall.eternitywall.fragments.CreateFragment;
import it.eternitywall.eternitywall.fragments.HelloFragment;
import it.eternitywall.eternitywall.fragments.ListFragment;
import it.eternitywall.eternitywall.fragments.RecoverPassphraseFragment;
import it.eternitywall.eternitywall.fragments.WalletFragment;
import it.eternitywall.eternitywall.wallet.WalletObservable;


public class MainActivity extends ActionBarActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener, PopupMenu.OnMenuItemClickListener,
ListFragment.OnFragmentInteractionListener, AccountFragment.OnFragmentInteractionListener, RecoverPassphraseFragment.OnFragmentInteractionListener,
        CreateFragment.OnFragmentInteractionListener,HelloFragment.OnFragmentInteractionListener,WalletFragment.OnFragmentInteractionListener{

    private static final int REQUEST_CODE = 8274;
    private static final String TAG = "MainActivity";

    // ActionBar and Toolbar items
    private SearchView searchView;
    private MenuItem searchMenuItem;
    TabLayout tabLayout;
    Toolbar toolbar;
    int page_num=0;



    // Container and Fragments
    ViewPager viewPager;
    ListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");

        setContentView(R.layout.activity_main);
        Iconify.with(new FontAwesomeModule());


        // Specify that tabs should be displayed in the action bar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        // Set Table Layout and Pager with fragments
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        changeTabsFont();

        //TODO DEBUG for density
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.i(TAG, "Density:" + metrics.densityDpi);
        Log.i(TAG, "WidthPixels:" + metrics.widthPixels);




        // Show / Hide write button
        /*SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String passphrase=sharedPref.getString(Preferences.PASSPHRASE, null);
        if (passphrase==null){
            // Hide write button on activity if there is no account
            findViewById(R.id.payButton).setVisibility(View.GONE);
        } else {
            // Show write button on activity if there is one account
            findViewById(R.id.payButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MainActivity.this, WriteActivity.class);
                    startActivity(i);
                }
            });
        }*/
    }

    private void changeTabsFont() {
        // Set typeface font inside toolbar for support font-awesome
        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTextAppearance(this,R.style.TextAppearance_AppCompat_Large);
                    ((TextView) tabViewChild).setTypeface(font);
                }
            }
        }
    }



    private void setupViewPager(ViewPager viewPager) {
        // put the fragments into container ViewPager
        listFragment=new ListFragment();

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(listFragment, getResources().getString(R.string.list_fragment));
        adapter.addFragment(new AccountFragment(), getResources().getString(R.string.account_fragment));
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                page_num = position;
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        page_num=0;
        supportInvalidateOptionsMenu();
        viewPager.setCurrentItem(page_num);

    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
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

        public void delFragment( String title) {
            int position=mFragmentTitleList.indexOf(title);
            mFragmentTitleList.remove(position);
            mFragmentList.remove(position);

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return  mFragmentTitleList.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if (page_num == 0) {
            getMenuInflater().inflate(R.menu.menu_main, menu);


            menu.findItem(R.id.action_order).setIcon(
                    new IconDrawable(this, FontAwesomeIcons.fa_sort_amount_desc)
                            .colorRes(android.R.color.white)
                            .actionBarSize());

            menu.findItem(R.id.action_preferences).setIcon(
                    new IconDrawable(this, FontAwesomeIcons.fa_gear)
                            .colorRes(android.R.color.white)
                            .actionBarSize());

            //SearchManager searchManager = (SearchManager)         getSystemService(Context.SEARCH_SERVICE);
            searchMenuItem = menu.findItem(R.id.action_search);
            searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
            searchView.setOnQueryTextListener(this);
            searchView.setOnCloseListener(this);

            //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            //searchView.setSubmitButtonEnabled(true);
            //searchView.setOnQueryTextListener(this);
            MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    // Return true to expand action view
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    // Write your code here
                    onClose();
                    // Return true to collapse action view
                    return true;
                }
            });

        } else if (page_num == 1) {
            getMenuInflater().inflate(R.menu.menu_profile, menu);


            menu.findItem(R.id.action_share).setIcon(
                    new IconDrawable(this, FontAwesomeIcons.fa_share_alt)
                            .colorRes(android.R.color.white)
                            .actionBarSize());

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String passphrase=sharedPref.getString(Preferences.PASSPHRASE, null);
            if (passphrase==null){
                menu.findItem(R.id.action_share).setVisible(false);
            } else {
                menu.findItem(R.id.action_share).setVisible(true);
            }

            // show sharing button only if wallet is synced or pending
            EWApplication ewApplication = (EWApplication) this.getApplication();
            if(ewApplication!=null && ewApplication.getWalletObservable()!=null && ewApplication.getWalletObservable().isSyncedOrPending()) {
                menu.findItem(R.id.action_share).setVisible(true);
            }else {
                menu.findItem(R.id.action_share).setVisible(false);
            }


            menu.findItem(R.id.action_preferences).setIcon(
                    new IconDrawable(this, FontAwesomeIcons.fa_gear)
                            .colorRes(android.R.color.white)
                            .actionBarSize());


        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_order) {
            View menuItemView = findViewById(R.id.action_order);
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, menuItemView);
            popupMenu.setOnMenuItemClickListener(MainActivity.this);
            popupMenu.inflate(R.menu.menu_order);
            popupMenu.show();
            return true;
        }else if (id == R.id.action_search) {
            //searchable element
            return true;
        }else if (id == R.id.action_share) {
            final WalletObservable walletObservable = ((EWApplication) getApplication()).getWalletObservable();
            if (walletObservable != null && walletObservable.getState() == WalletObservable.State.SYNCED) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, walletObservable.getCurrent().toString());
                intent.setType("text/plain");
                startActivity(intent);
            }
            return true;

        }else if (id == R.id.action_preferences) {
            startActivity(new Intent(MainActivity.this,PreferencesActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(this, "requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data , Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        // OnQueryTextSubmit was called twice, so clean
        searchView.setIconified(true);
        searchView.clearFocus();
        listFragment.clear();
        listFragment.setSearch(query);
        listFragment.loadMoreData();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onClose() {
        Log.i(TAG,"onClose");
        listFragment.clear();
        listFragment.loadMoreData();
        return false;
    }


    @Override
    protected void onPause() {
        super.onPause();
        ((EWApplication)getApplication()).onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        ((EWApplication)getApplication()).onResume();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        listFragment.clear();
        switch (item.getItemId()) {
            case R.id.item_main:
                ;
                break;
            case R.id.item_views:
                listFragment.setSortby("alltimeviews");
                break;
            case R.id.item_ranking:
                listFragment.setSortby("ranking");
                break;
            case R.id.item_likes:
                listFragment.setSortby("likes");
                break;
            case R.id.item_replies:
                listFragment.setSortby("replies");
                break;
            case R.id.item_viewslast7days:
                listFragment.setSortby("last7daysviews");
                break;
        }
        listFragment.loadMoreData();
        return false;
    }

    @Override
    public void onBackPressed() {
        if (listFragment.getSearch()!= null){
            if(searchMenuItem.isActionViewExpanded())
                searchMenuItem.collapseActionView();
            listFragment.clear();
            listFragment.loadMoreData();
        } else if(searchMenuItem.isActionViewExpanded()){
                searchMenuItem.collapseActionView();
            listFragment.clear();
            listFragment.loadMoreData();
        } else if (listFragment.getSortby()!= null){
            listFragment.clear();
            listFragment.loadMoreData();
        } else {
            super.onBackPressed();
        }
    }
}
