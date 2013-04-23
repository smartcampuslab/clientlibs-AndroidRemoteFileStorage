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
package eu.trentorise.smartcampus.storage;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.Constants.RESULT;
import eu.trentorise.smartcampus.storage.dropbox.DropboxAuth;
import eu.trentorise.smartcampus.storage.model.UserAccount;

/**
 * Parent activity to start the storage user authentication: retrieval of the User Account information.
 * Provides the facilities to store the user account information upon completion.
 * Currently, the only subclass is the {@link DropboxAuth} that supports the DROPBOX storage and activates the Dropbox library functionality for 
 * the authentication.
 * @author raman
 *
 */
public class AuthActivity extends Activity {

	
	protected Filestorage fileStorage;
	protected String authToken;
	protected String accountName;
	protected String appAccountId;
	protected String appName;
	protected String appToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		accountName = intent.getStringExtra(Filestorage.EXTRA_INPUT_ACCOUNTNAME);
		appName = intent.getStringExtra(Filestorage.EXTRA_INPUT_APPNAME);
		appAccountId = intent.getStringExtra(Filestorage.EXTRA_INPUT_APPACCOUNTID);
		authToken = intent.getStringExtra(Filestorage.EXTRA_INPUT_AUTHTOKEN);
		appToken = intent.getStringExtra(Filestorage.EXTRA_INPUT_APPTOKEN);
		String service = intent.getStringExtra(Filestorage.EXTRA_INPUT_SERVICE);
		String host = intent.getStringExtra(Filestorage.EXTRA_INPUT_HOST);
		fileStorage = new Filestorage(getApplicationContext(), appName, appToken, host, service);
	}

	protected void onAccountAcquired(UserAccount u) {
		u = completeUserAccountData(u);
		new StoreAccountTask().execute(u);
	}
	
	protected void onNegativeResult(int result) {
		setResult(result);
		finish();
	}
	
	private UserAccount completeUserAccountData(UserAccount u) {
		u.setAccountName(accountName);
		return u;
	}

	protected UserAccount storeUserAccount(UserAccount userAccount)
			throws ProtocolException, ConnectionException, SecurityException {
		return fileStorage.storeUserAccount(authToken, userAccount);
	}

	protected class StoreAccountTask extends AsyncTask<UserAccount, Void, UserAccount> {
		private RESULT result = RESULT.OK;
		@Override
		protected UserAccount doInBackground(UserAccount... params) {
			try {
				return storeUserAccount(params[0]);
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
		protected void onPostExecute(UserAccount ua) {
			Intent intent = new Intent();
			if (ua != null) {
				intent.putExtra(Filestorage.EXTRA_OUTPUT_USERACCOUNT, ua);
				setResult(Activity.RESULT_OK, intent);
			} else {
				if (result == Constants.RESULT.SECURITY) {
					setResult(Filestorage.RESULT_SC_SECURITY_ERROR, intent);
				} else if (result == Constants.RESULT.CONNECTION) {
					setResult(Filestorage.RESULT_SC_CONNECTION_ERROR, intent);
				} else if (result == Constants.RESULT.PROTOCOL) {
					setResult(Filestorage.RESULT_SC_PROTOCOL_ERROR, intent);
				}
			}
			finish();
		}
	}
}
