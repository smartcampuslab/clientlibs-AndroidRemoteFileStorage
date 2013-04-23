/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.storage.dropbox;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.AuthActivity;
import eu.trentorise.smartcampus.storage.Constants;
import eu.trentorise.smartcampus.storage.Filestorage;
import eu.trentorise.smartcampus.storage.model.AppAccount;
import eu.trentorise.smartcampus.storage.model.Configuration;
import eu.trentorise.smartcampus.storage.model.StorageType;
import eu.trentorise.smartcampus.storage.model.UserAccount;

/**
 * Implements the DROPBOX authentication to create user account.
 * @see AuthActivity
 * @author raman
 *
 */
public class DropboxAuth extends AuthActivity {

	DropboxAPI<AndroidAuthSession> dApi;

	private boolean externalAuthenticationStarted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new SessionAsyncTask().execute();
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		// set to true because this step starts before external dropbox
		// authentication
		externalAuthenticationStarted = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (dApi == null) return;
		
		AndroidAuthSession session = dApi.getSession();
		if (session != null && externalAuthenticationStarted) {
			if (session.authenticationSuccessful()) {
				try {
					// Mandatory call to complete the auth
					session.finishAuthentication();
					TokenPair tokens = session.getAccessTokenPair();
					onAccountAcquired(createUserAccount(tokens));
				} catch (IllegalStateException e) {
					Log.e(getClass().getName(),
							"Exception during dropbox authentication", e);
				}
			} else {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		}

	}

	private UserAccount createUserAccount(TokenPair token) {
		UserAccount userAccount = new UserAccount();
		userAccount.setAppAccountId(appAccountId);
		userAccount.setAppName(appName);
		userAccount.setStorageType(StorageType.DROPBOX);

		List<Configuration> confs = Arrays.asList(new Configuration(
				Constants.USER_KEY_CONF, token.key), new Configuration(
				Constants.USER_SECRET_CONF, token.secret));
		userAccount.setConfigurations(confs);
		return userAccount;
	}


	private class SessionAsyncTask extends AsyncTask<Void, Void, AndroidAuthSession> {
		
		private Constants.RESULT result = Constants.RESULT.OK;
		
		@Override
		protected AndroidAuthSession doInBackground(Void... arg0) {
			try {
				List<AppAccount> appAccounts = fileStorage.getAppAccounts(authToken);
				String appKey = null;
				String appSecret = null;
				for (AppAccount appAccount : appAccounts) {
					if (appAccount.getId().equals(appAccountId)) {
						for (Configuration conf : appAccount.getConfigurations()) {
							if (conf.getName().equals(Constants.APP_KEY_CONF)) {
								appKey = conf.getValue();
							}

							if (conf.getName().equals(Constants.APP_SECRET_CONF)) {
								appSecret = conf.getValue();
							}
						}
						break;
					}
				}

				if (appKey == null || appSecret == null) {
					throw new IllegalArgumentException("bad appAccount configurations");
				}

				AppKeyPair appKeyPair = new AppKeyPair(appKey, appSecret);
				AndroidAuthSession session = new AndroidAuthSession(appKeyPair,
						AccessType.APP_FOLDER);
				return session;
			} catch (ProtocolException e) {
				result = Constants.RESULT.PROTOCOL;
			} catch (ConnectionException e) {
				result = Constants.RESULT.CONNECTION;
			} catch (SecurityException e) {
				result = Constants.RESULT.SECURITY;
			}
			return null;
		}

		@Override
		protected void onPostExecute(AndroidAuthSession session) {
			if (session != null) {
				dApi = new DropboxAPI<AndroidAuthSession>(session);
				// start request of permission to the user
				dApi.getSession().startAuthentication(DropboxAuth.this);
			} else {
				if (result == Constants.RESULT.SECURITY) {
					onNegativeResult(Filestorage.RESULT_SC_SECURITY_ERROR);
				} else if (result == Constants.RESULT.CONNECTION) {
					onNegativeResult(Filestorage.RESULT_SC_CONNECTION_ERROR);
				} else if (result == Constants.RESULT.PROTOCOL) {
					onNegativeResult(Filestorage.RESULT_SC_PROTOCOL_ERROR);
				}
			}
		}
		
	}
}
