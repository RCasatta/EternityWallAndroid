package it.eternitywall.eternitywall;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.eternitywall.eternitywall.fragments.AccountFragment;
import it.eternitywall.eternitywall.fragments.CreateFragment;
import it.eternitywall.eternitywall.fragments.HelloFragment;
import it.eternitywall.eternitywall.fragments.ListFragment;
import it.eternitywall.eternitywall.fragments.RecoverPassphraseFragment;
import it.eternitywall.eternitywall.fragments.WalletFragment;


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

    // Container and Fragments
    ViewPager viewPager;
    ListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Specify that tabs should be displayed in the action bar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        changeTabsFont();

        /*
        String passphrase = Bitcoin.getNewMnemonicPassphrase();
        EWWallet ewWallet = new EWWallet( passphrase ,  getApplicationContext());
        Thread thread = new Thread(ewWallet);
        thread.start();
        */
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
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Get the root inflator.
        LayoutInflater baseInflater = (LayoutInflater)getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        TextView txtOrder=new TextView(MainActivity.this);
        txtOrder.setPadding(0, 0, (int) getResources().getDimension(R.dimen.activity_horizontal_margin), 0);
        txtOrder.setText(getResources().getString(R.string.action_order));
        txtOrder.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Large);
        txtOrder.setTypeface(font);
        menu.findItem(R.id.action_order).setActionView(txtOrder);
        menu.findItem(R.id.action_order).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, item.getActionView());
                popupMenu.setOnMenuItemClickListener(MainActivity.this);
                popupMenu.inflate(R.menu.menu_order);
                popupMenu.show();
                return true;
            }
        });
        txtOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.setOnMenuItemClickListener(MainActivity.this);
                popupMenu.inflate(R.menu.menu_order);
                popupMenu.show();
            }
        });

        /*TextView txtCloud=new TextView(MainActivity.this);
        txtCloud.setPadding(0,0,(int) getResources().getDimension(R.dimen.activity_horizontal_margin),0);
        txtCloud.setText(getResources().getString(R.string.action_cloud));
        txtCloud.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Large);
        txtCloud.setTypeface(font);
        menu.findItem(R.id.action_cloud).setActionView(txtCloud);
*/

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
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, item.getActionView());
            //popupMenu.setOnMenuItemClickListener(MainActivity.this);
            popupMenu.inflate(R.menu.menu_order);
            popupMenu.show();
            return true;
        }else if (id == R.id.action_search) {
            //searchable element
            return true;
        }/*else if (id == R.id.action_cloud) {
            return true;
        }*/

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
        listFragment.clear();
        listFragment.loadMoreData();
        return false;
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
