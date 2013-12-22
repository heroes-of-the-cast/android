package com.timvdalen.hotc;

import java.io.IOException;

import com.google.cast.ApplicationChannel;
import com.google.cast.ApplicationMetadata;
import com.google.cast.ApplicationSession;
import com.google.cast.CastContext;
import com.google.cast.CastDevice;
import com.google.cast.MediaRouteAdapter;
import com.google.cast.MediaRouteHelper;
import com.google.cast.MediaRouteStateChangeListener;
import com.google.cast.SessionError;
import com.timvdalen.hotc.ui.MainFragment;
import com.timvdalen.hotc.ui.adventures.AdventuresFragment;
import com.timvdalen.hotc.ui.characters.AddCharActivity;
import com.timvdalen.hotc.ui.characters.CharactersFragment;
import com.timvdalen.hotc.ui.home.HomeFragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HomeActivity extends ActionBarActivity implements MediaRouteAdapter{
	private String[] mMenuItems; // TODO: Write an adapter for this
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private String mTitle;
	
	private MainFragment openFragment;

	private ApplicationSession mSession;
	private SessionListener mSessionListener;

	private CastContext mCastContext;
	private CastDevice mSelectedDevice;
	private MediaRouter mMediaRouter;
	private MediaRouteSelector mMediaRouteSelector;
	private MediaRouter.Callback mMediaRouterCallback;
	
	private static final int ADD_CHAR_RESULT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		mTitle = getResources().getString(R.string.app_name);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String session_key = preferences.getString("session_key", null);
		if(session_key == null){
			Intent login = new Intent(this, LoginActivity.class);
			startActivityForResult(login, 0);
			finish();
		}

		setContentView(R.layout.activity_home);

		mMenuItems = new String[] { "Home", "Characters", "Adventures" };
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mMenuItems));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
			public void onDrawerClosed(View view){
				invalidateOptionsMenu();
				getActionBar().setTitle(mTitle);
			}

			public void onDrawerOpened(View drawerView){
				invalidateOptionsMenu();
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mSessionListener = new SessionListener();

		mCastContext = new CastContext(getApplicationContext());
		MediaRouteHelper.registerMinimalMediaRouteProvider(mCastContext, this);
		mMediaRouter = MediaRouter.getInstance(getApplicationContext());
		mMediaRouteSelector = MediaRouteHelper.buildMediaRouteSelector(MediaRouteHelper.CATEGORY_CAST, getResources().getString(R.string.CAST_APP_NAME), null);
		mMediaRouterCallback = new MediaRouterCallback();

		// Open default fragment
		Fragment fragment = new HomeFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
		
		//Set visibility
		invalidateOptionsMenu();
	}

	@Override
	protected void onStart(){
		super.onStart();
		mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
	}

	@Override
	protected void onStop(){
		endSession();
		mMediaRouter.removeCallback(mMediaRouterCallback);
		super.onStop();
	}

	@Override
	public void onDestroy(){
		MediaRouteHelper.unregisterMediaRouteProvider(mCastContext);
		mCastContext.dispose();
		mCastContext = null;
		if(mSession != null){
			try{
				if(!mSession.hasStopped()){
					mSession.endSession();
				}
			}catch(IOException e){
				// Failed to end session
			}
		}
		mSession = null;
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.home, menu);

		MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
		MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
		mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		// Hide and show actions based on fragment/drawer
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

		if(drawerOpen){
			// Hide all
			menu.findItem(R.id.media_route_menu_item).setVisible(false);
			menu.findItem(R.id.char_add).setVisible(false);
			menu.findItem(R.id.refresh).setVisible(false);
		}else{
			// TODO: Better switch
			if(mTitle.equals("Heroes of the Cast")){
				menu.findItem(R.id.media_route_menu_item).setVisible(true);
			}else if(mTitle.equals("Characters")){
				menu.findItem(R.id.char_add).setVisible(true);
				menu.findItem(R.id.refresh).setVisible(true);
			}else if(mTitle.equals("Adventures")){

			}
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if(mDrawerToggle.onOptionsItemSelected(item)){
			return true;
		}
		// Handle the rest of the actions

		switch(item.getItemId()){
		case R.id.char_add:
			Intent addchar = new Intent(this, AddCharActivity.class);
            startActivityForResult(addchar, ADD_CHAR_RESULT);
			break;
		case R.id.refresh:
			openFragment.refresh();
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		switch(requestCode){
		case ADD_CHAR_RESULT:
			if(resultCode == AddCharActivity.RESULT_ADDED){
				//TODO: Can there be a case where the open fragment is not the character fragment? And does that matter?
				openFragment.refresh();
			}
			break;
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState){
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private void setSelectedDevice(CastDevice device){
		mSelectedDevice = device;

		if(mSelectedDevice != null){
			mSession = new ApplicationSession(mCastContext, mSelectedDevice);
			mSession.setListener(mSessionListener);

			try{
				mSession.startSession(getResources().getString(R.string.CAST_APP_NAME));
			}catch(IOException e){

			}
		}else{
			endSession();
		}
	}

	private void endSession(){
		if((mSession != null) && (mSession.hasStarted())){
			try{
				if(mSession.hasChannel()){
					// Send goodbyes
				}
				mSession.endSession();
			}catch(IOException e){

			}catch(IllegalStateException e){

			}finally{
				mSession = null;
			}
		}
	}

	@Override
	public void onDeviceAvailable(CastDevice device, String routeId, MediaRouteStateChangeListener listener){
		setSelectedDevice(device);
	}

	@Override
	public void onSetVolume(double arg0){
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpdateVolume(double arg0){
		// TODO Auto-generated method stub

	}

	private class MediaRouterCallback extends MediaRouter.Callback{
		@Override
		public void onRouteSelected(MediaRouter router, RouteInfo route){
			MediaRouteHelper.requestCastDeviceForRoute(route);
		}

		@Override
		public void onRouteUnselected(MediaRouter router, RouteInfo route){
			setSelectedDevice(null);
		}
	}

	private class SessionListener implements ApplicationSession.Listener{
		@Override
		public void onSessionStarted(ApplicationMetadata appMetadata){
			ApplicationChannel channel = mSession.getChannel();
			if(channel == null){
				return;
			}
		}

		@Override
		public void onSessionStartFailed(SessionError error){
		}

		@Override
		public void onSessionEnded(SessionError error){
		}
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener{
		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id){
			selectItem(position);
		}
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position){
		// Create a new fragment and specify the planet to show based on
		// position

		//TODO: Should I manually clean up the old one?
		
		switch(position){
		case 0:
			openFragment = new HomeFragment();
			break;
		case 1:
			openFragment = new CharactersFragment();
			break;
		case 2:
			openFragment = new AdventuresFragment();
			break;
		default:
			// Java is stupid
			openFragment = new HomeFragment();
		}

		//TODO: Much, much better way of handling open fragment
		if(position == 0){
			mTitle = "Heroes of the Cast";
		}else{
			mTitle = mMenuItems[position];
		}

		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, openFragment).commit();

		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

}
