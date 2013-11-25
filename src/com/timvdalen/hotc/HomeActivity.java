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

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class HomeActivity extends ActionBarActivity implements MediaRouteAdapter{
	private ApplicationSession mSession;
	private SessionListener mSessionListener;

	private CastContext mCastContext;
	private CastDevice mSelectedDevice;
	private MediaRouter mMediaRouter;
	private MediaRouteSelector mMediaRouteSelector;
	private MediaRouter.Callback mMediaRouterCallback;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		mSessionListener = new SessionListener();

		mCastContext = new CastContext(getApplicationContext());
		MediaRouteHelper.registerMinimalMediaRouteProvider(mCastContext, this);
		mMediaRouter = MediaRouter.getInstance(getApplicationContext());
		mMediaRouteSelector = MediaRouteHelper.buildMediaRouteSelector(
				MediaRouteHelper.CATEGORY_CAST,
				getResources().getString(R.string.CAST_APP_NAME), null);
		mMediaRouterCallback = new MediaRouterCallback();
	}

	@Override
	protected void onStart(){
		super.onStart();
		mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
				MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
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
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.home, menu);

		MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
		MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
		Log.v("CAST", Boolean.toString(mediaRouteActionProvider == null));
		mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
		return true;
	}

	private void setSelectedDevice(CastDevice device){
		mSelectedDevice = device;

		if(mSelectedDevice != null){
			mSession = new ApplicationSession(mCastContext, mSelectedDevice);
			mSession.setListener(mSessionListener);

			try{
				mSession.startSession(getResources().getString(
						R.string.CAST_APP_NAME));
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
	public void onDeviceAvailable(CastDevice device, String routeId,
			MediaRouteStateChangeListener listener){
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

}
