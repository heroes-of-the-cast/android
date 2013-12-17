package com.timvdalen.hotc;

import java.io.IOException;

import com.timvdalen.hotc.apihandler.APIHandler;
import com.timvdalen.hotc.apihandler.NoNetworkException;
import com.timvdalen.hotc.apihandler.error.APIException;
import com.timvdalen.hotc.apihandler.error.LoginException;
import com.timvdalen.hotc.apihandler.error.ReservedNameException;
import com.timvdalen.hotc.apihandler.error.UserExistsException;
import com.timvdalen.hotc.data.Session;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity{
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for Username and password at the time of the login attempt.
	private String mUsername;
	private String mPassword;

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Set up the login form.
		mUsernameView = (EditText) findViewById(R.id.username);
		mUsernameView.setText(mUsername);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent){
				if(id == R.id.login || id == EditorInfo.IME_NULL){
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view){
				attemptLogin();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid Username, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin(){
		if(mAuthTask != null){
			return;
		}

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if(TextUtils.isEmpty(mPassword)){
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid Username address.
		if(TextUtils.isEmpty(mUsername)){
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}

		if(cancel){
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		}else{
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	private void showProgress(final boolean show){
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

		mLoginStatusView.setVisibility(View.VISIBLE);
		mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation){
				mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			}
		});

		mLoginFormView.setVisibility(View.VISIBLE);
		mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation){
				mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			}
		});

	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean>{
		private Exception e;
		
		@Override
		protected Boolean doInBackground(Void... params){
			// Check if an account with this username exists
			try{
				if(!APIHandler.userexists(mUsername, LoginActivity.this)){
					APIHandler.usercreate(mUsername, mPassword, LoginActivity.this);
				}
				Session s = APIHandler.userlogin(mUsername, mPassword, LoginActivity.this);
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				preferences.edit().putString("session_key", s.getKey()).commit();
				
				return true;
			}catch(NoNetworkException e){
				//Notify user that there is no connection
				this.e = e;
			}catch(ReservedNameException e){
				//The user tried to register with a reserved name
				this.e = e;
			}catch(UserExistsException e){
				//The user tried to register with a registered name
				//This can only happen if someone registers this name between the userexists and usercreate calls
				this.e = e;
			}catch(LoginException e){
				//Credentials are wrong
				this.e = e;
			}catch(APIException e){
				// /user/:name/exists can't throw errors according to the doc
				this.e = e;
			}catch(IOException e){
				//TODO: Figure out if there's something that can fix this
				this.e = e;
			}

			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success){
			mAuthTask = null;
			showProgress(false);

			if(success){
				//Succesfully logged in, get back to the main activity
				Intent main = new Intent(LoginActivity.this, HomeActivity.class);
	            startActivityForResult(main, 0);
				finish();
			}else{
				if(this.e != null){
					//Figure out what went wrong
					if(e instanceof NoNetworkException){
						Toast.makeText(LoginActivity.this, getResources().getString(R.string.error_no_network), Toast.LENGTH_SHORT).show();
					}else if(e instanceof ReservedNameException){
						mUsernameView.setError(getString(R.string.error_reserved_name));
						mUsernameView.requestFocus();
					}else if(e instanceof UserExistsException){
						mUsernameView.setError(getString(R.string.error_name_taken));
						mUsernameView.requestFocus();
					}else if(e instanceof LoginException){
						mPasswordView.setError(getString(R.string.error_incorrect_password));
						mPasswordView.requestFocus();
					}else{
						//No idea
						Toast.makeText(LoginActivity.this, getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
					}
				}
			}
		}

		@Override
		protected void onCancelled(){
			mAuthTask = null;
			showProgress(false);
		}
	}
}
