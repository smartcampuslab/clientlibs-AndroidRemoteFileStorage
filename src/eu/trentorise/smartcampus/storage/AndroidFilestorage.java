package eu.trentorise.smartcampus.storage;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import eu.trentorise.smartcampus.filestorage.client.Filestorage;
import eu.trentorise.smartcampus.filestorage.client.FilestorageException;
import eu.trentorise.smartcampus.filestorage.client.model.Account;
import eu.trentorise.smartcampus.filestorage.client.model.Configuration;
import eu.trentorise.smartcampus.filestorage.client.model.StorageType;

public class AndroidFilestorage extends Filestorage {

	private String appId;
	private String serverUrl;

	/** Input parameter key: appname */
	public static final String EXTRA_INPUT_APPID = "eu.trentorise.smartcampus.storage.APPNAME";
	/** Input parameter: Platform user auth token */
	public static final String EXTRA_INPUT_AUTHTOKEN = "eu.trentorise.smartcampus.storage.AUTHTOKEN";
	/** Input parameter: service context path */
	public static final String EXTRA_INPUT_SERVICE = "eu.trentorise.smartcampus.storage.SERVICE";

	/** Result code: protocol exception */
	public static final int RESULT_SC_PROTOCOL_ERROR = 1001;
	/** Result code: connector exception */
	public static final int RESULT_SC_CONNECTION_ERROR = 1002;
	/** Result code: security exception */
	public static final int RESULT_SC_SECURITY_ERROR = 1003;
	/** Output parameter: account ID*/
	public static final String EXTRA_OUTPUT_ACCOUNT_ID = "eu.trentorise.smartcampus.storage.ACCOUNT_ID";

	public AndroidFilestorage(String serverUrl, String appId) {
		super(serverUrl, appId);
		this.appId = appId;
		this.serverUrl = serverUrl;
	}

	/**
	 * @param type
	 * @return true if the specified storage type requires explicit user authorization.
	 * In this case the account acquisition is performed using {@link #startAuthActivityForResult(Activity, String, String, String, StorageType, int)} method.
	 */
	public boolean isAuthenticationRequired(StorageType type) {
		switch (type) {
		case DROPBOX:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * Start the storage authentication to create the user account. Returns the
	 * intent containing the {@link AuthActivity#EXTRA_OUTPUT_USERACCOUNT}
	 * parameter with {@link UserAccount} object in case of successful
	 * authentication or one of {@link Activity#RESULT_CANCELED},
	 * {@link AuthActivity#RESULT_SC_CONNECTION_ERROR},
	 * {@link AuthActivity#RESULT_SC_PROTOCOL_ERROR},
	 * {@link AuthActivity#RESULT_SC_SECURITY_ERROR} response codes.
	 * 
	 * @param activity
	 * @param authToken
	 * @param appAccountName
	 * @param storageId
	 * @param storageType
	 *            storage type
	 * @param requestCode
	 * 
	 */
	public void startAuthActivityForResult(Activity activity, String authToken, StorageType storageType, int requestCode) {
		if (appId == null || storageType == null || authToken == null)
			throw new IllegalArgumentException(
					"Intent MUST have setted authToken, appname, accountName, storageType extras");

		Intent intent = createIntent(activity, appId, authToken, serverUrl);
		intent.setClass(activity, AuthActivity.class);
		intent.putExtras(intent);
		activity.startActivityForResult(intent, requestCode);
	}

	public Account createAccount(String token, String appId, String name, StorageType type, List<Configuration> configurations) throws FilestorageException {
		if (isAuthenticationRequired(type)) {
			throw new FilestorageException("Type "+ type + " requires explicit authorization.");
		}
		
		Filestorage storage = new Filestorage(serverUrl, appId);
		
		Account a = new Account();
		a.setAppId(appId);
		a.setConfigurations(configurations);
		a.setName(name);
		a.setStorageType(type);
		a = storage.createAccountByUser(token, a);
		return a;
	}
	
	private static Intent createIntent(Context ctx, String appId, String authToken, String service) {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_INPUT_APPID, appId);
		intent.putExtra(EXTRA_INPUT_AUTHTOKEN, authToken);
		intent.putExtra(EXTRA_INPUT_SERVICE, service);
		return intent;
	}
}
