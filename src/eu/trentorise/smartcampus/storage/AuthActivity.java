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
import eu.trentorise.smartcampus.filestorage.client.FilestorageException;
import eu.trentorise.smartcampus.filestorage.client.model.Account;
import eu.trentorise.smartcampus.storage.Constants.RESULT;
import eu.trentorise.smartcampus.storage.dropbox.DropboxAuth;
import eu.trentorise.smartcampus.storage.model.AccountWrapper;

/**
 * Parent activity to start the storage user authentication: retrieval of the
 * User Account information. Provides the facilities to store the user account
 * information upon completion. Currently, the only subclass is the
 * {@link DropboxAuth} that supports the DROPBOX storage and activates the
 * Dropbox library functionality for the authentication.
 * 
 * @author raman
 * 
 */
public class AuthActivity extends Activity {

	protected AndroidFilestorage fileStorage;
	protected String authToken;
	protected String accountName;
	protected String appAccountId;
	protected String appId;
	protected String appToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		accountName = intent
				.getStringExtra(AndroidFilestorage.EXTRA_INPUT_ACCOUNTNAME);
		appId = intent.getStringExtra(AndroidFilestorage.EXTRA_INPUT_APPNAME);
		appAccountId = intent
				.getStringExtra(AndroidFilestorage.EXTRA_INPUT_APPACCOUNTID);
		authToken = intent
				.getStringExtra(AndroidFilestorage.EXTRA_INPUT_AUTHTOKEN);
		appToken = intent
				.getStringExtra(AndroidFilestorage.EXTRA_INPUT_APPTOKEN);
		String service = intent
				.getStringExtra(AndroidFilestorage.EXTRA_INPUT_SERVICE);
		String host = intent
				.getStringExtra(AndroidFilestorage.EXTRA_INPUT_HOST);
		fileStorage = new AndroidFilestorage(host, appId, appToken);
	}

	protected void onAccountAcquired(Account account) {
		account = completeUserAccountData(account);
		new StoreAccountTask().execute(account);
	}

	protected void onNegativeResult(int result) {
		setResult(result);
		finish();
	}

	private Account completeUserAccountData(Account account) {
		account.setName(accountName);
		return account;
	}

	protected Account createAccount(Account account)
			throws FilestorageException {
		return fileStorage.createAccountByUser(authToken, account);
	}

	protected class StoreAccountTask extends AsyncTask<Account, Void, Account> {
		private RESULT result = RESULT.OK;

		@Override
		protected Account doInBackground(Account... params) {
			// try {
			try {
				return createAccount(params[0]);
			} catch (FilestorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// } catch (ProtocolException e) {
			// result = Constants.RESULT.PROTOCOL;
			// } catch (ConnectionException e) {
			// result = Constants.RESULT.CONNECTION;
			// } catch (SecurityException e) {
			// result = Constants.RESULT.SECURITY;
			// }
			return null;
		}

		@Override
		protected void onPostExecute(Account account) {
			Intent intent = new Intent();
			if (account != null) {
				intent.putExtra(AndroidFilestorage.EXTRA_OUTPUT_USERACCOUNT,
						new AccountWrapper(account));
				setResult(Activity.RESULT_OK, intent);
			} else {
				if (result == Constants.RESULT.SECURITY) {
					setResult(AndroidFilestorage.RESULT_SC_SECURITY_ERROR,
							intent);
				} else if (result == Constants.RESULT.CONNECTION) {
					setResult(AndroidFilestorage.RESULT_SC_CONNECTION_ERROR,
							intent);
				} else if (result == Constants.RESULT.PROTOCOL) {
					setResult(AndroidFilestorage.RESULT_SC_PROTOCOL_ERROR,
							intent);
				}
			}
			finish();
		}
	}
}
