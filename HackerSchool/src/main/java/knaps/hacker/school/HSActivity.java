package knaps.hacker.school;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.SearchView;

import knaps.hacker.school.networking.HSOAuthService;

public class HSActivity extends BaseFragmentActivity {

    private SearchView mSearchView;

    private static final String LIST_FRAGMENT = "list";
    private static final String LOGIN_FRAGMENT = "login";
    private ViewPager mViewPager;
    private HSPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mPagerAdapter = new HSPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(mPagerListener);

        Fragment fragment;
        String name;
        if (needsToShowLogin()) {
            fragment = new LoginFragment();
            name = LOGIN_FRAGMENT;

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(android.R.id.content, fragment, name);
            transaction.addToBackStack(name);
            transaction.commit();
        }


        handleIntent(getIntent());
        setupActionBar();
    }

    private void handleIntent(Intent intent) {
        if (intent != null && Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String filter = intent.getStringExtra(SearchManager.QUERY);
            mPagerAdapter.performSearch(filter);
        }
    }

    private boolean needsToShowLogin() {
        return !HSOAuthService.getService().isAuthorized();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.TabListener listener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(final ActionBar.Tab tab, final FragmentTransaction ft) {
                int position = tab.getPosition();
                closeSearchBox(position);
                mViewPager.setCurrentItem(position);
            }

            @Override
            public void onTabUnselected(final ActionBar.Tab tab, final FragmentTransaction ft) {
                // hide the tab ??
            }

            @Override
            public void onTabReselected(final ActionBar.Tab tab, final FragmentTransaction ft) { }
        };

        actionBar.addTab(actionBar.newTab()
                                  .setText("$ Directory")
                                  .setTabListener(listener));

        actionBar.addTab(actionBar.newTab()
                                  .setText("Play_")
                                  .setTabListener(listener));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null && Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mPagerAdapter.performSearch(intent.getStringExtra(SearchManager.QUERY).toLowerCase().trim());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list, menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                mPagerAdapter.performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                mPagerAdapter.performSearch(newText);
                return true;
            }
        });
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchItem.collapseActionView();
                return true;
            }
        });

        return true;
    }

    private void closeSearchBox(int position) {
        if (mSearchView != null && position != HSPagerAdapter.LIST_PAGE && !mSearchView.isIconified()) {
            mSearchView.setIconified(true);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        menu.findItem(R.id.search).collapseActionView();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                //NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.search:
                // navigate to the list fragment
                mViewPager.setCurrentItem(HSPagerAdapter.LIST_PAGE);
                mSearchView.setIconified(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ViewPager.SimpleOnPageChangeListener mPagerListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(final int position) {
            closeSearchBox(position);
            getActionBar().setSelectedNavigationItem(position);
        }
    };

    public class HSPagerAdapter extends FragmentPagerAdapter {

        public static final int LIST_PAGE = 0;
        public static final int GAME_PAGE = 1;

        HSListFragment mListFragment;
        Fragment mGameFragment;

        public HSPagerAdapter(final FragmentManager fm) {
            super(fm);
            mListFragment = new HSListFragment();
            mGameFragment = new HSListFragment();
        }

        @Override
        public Fragment getItem(final int i) {
            switch (i) {
                case LIST_PAGE:
                    return mListFragment;
                case GAME_PAGE:
                    return mGameFragment;
                default:
                    return mListFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(final int position) {
            return position == LIST_PAGE ? "$ Directory" : "Play_";
        }

        public void performSearch(String searchTerm) {
            mListFragment.filterSearch(searchTerm);
        }
    }
}
