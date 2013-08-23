package eu.trentorise.smartcampus.storage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import eu.trentorise.smartcampus.filestorage.client.Filestorage;
import eu.trentorise.smartcampus.storage.dropbox.DropboxAuth;
import eu.trentorise.smartcampus.storage.model.StorageType;
import eu.trentorise.smartcampus.storage.model.UserAccount;

public class AndroidFilestorage extends Filestorage {

	private String appId;
	private String serverUrl;
	private String appToken;

	/** Input parameter key: appname */
	public static final String EXTRA_INPUT_APPNAME = "eu.trentorise.smartcampus.storage.APPNAME";
	/** Input parameter: apptoken */
	public static final String EXTRA_INPUT_APPTOKEN = "eu.trentorise.smartcampus.storage.APPTOKEN";
	/** Input parameter: app account id */
	public static final String EXTRA_INPUT_APPACCOUNTID = "eu.trentorise.smartcampus.storage.APPACCOUNTID";
	/** Input parameter: account name */
	public static final String EXTRA_INPUT_ACCOUNTNAME = "eu.trentorise.smartcampus.storage.ACCOUNTNAME";
	/** Input parameter: Platform user auth token */
	public static final String EXTRA_INPUT_AUTHTOKEN = "eu.trentorise.smartcampus.storage.AUTHTOKEN";
	/** Input parameter: storage type (e.g., DROPBOX) */
	public static final String EXTRA_INPUT_STORAGETYPE = "eu.trentorise.smartcampus.storage.STORAGETYPE";
	/** Input parameter: service host address */
	public static final String EXTRA_INPUT_HOST = "eu.trentorise.smartcampus.storage.HOST";
	/** Input parameter: service context path */
	public static final String EXTRA_INPUT_SERVICE = "eu.trentorise.smartcampus.storage.SERVICE";

	/** Output parameter: user account object */
	public static final String EXTRA_OUTPUT_USERACCOUNT = "eu.trentorise.smartcampus.storage.USERACCOUNT";

	/** Result code: protocol exception */
	public static final int RESULT_SC_PROTOCOL_ERROR = 1001;
	/** Result code: connector exception */
	public static final int RESULT_SC_CONNECTION_ERROR = 1002;
	/** Result code: security exception */
	public static final int RESULT_SC_SECURITY_ERROR = 1003;

	public AndroidFilestorage(String serverUrl, String appId, String appToken) {
		super(serverUrl, appId);
		this.appId = appId;
		this.serverUrl = serverUrl;
		this.appToken = appToken;
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
	public void startAuthActivityForResult(Activity activity, String authToken,
			String appAccountName, String storageId, StorageType storageType,
			int requestCode) {
		if (appId == null || appAccountName == null || storageId == null
				|| storageType == null || authToken == null)
			throw new IllegalArgumentException(
					"Intent MUST have setted authToken, appname, accountName, storageId,storageType extras");

		Intent intent = createIntent(activity, appId, appToken, authToken,
				appAccountName, storageId, storageType, serverUrl, SERVICE);

		switch (storageType) {
		case DROPBOX:
			intent.setClass(activity, DropboxAuth.class);
			intent.putExtras(intent);
			activity.startActivityForResult(intent, requestCode);
			break;
		default:
			throw new UnsupportedOperationException(
					"Unsupported storage type: " + storageType);
		}
	}

	private static Intent createIntent(Context ctx, String appId,
			String appToken, String authToken, String accountName,
			String storageId, StorageType storageType, String host,
			String service) {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_INPUT_APPNAME, appId);
		intent.putExtra(EXTRA_INPUT_AUTHTOKEN, authToken);
		intent.putExtra(EXTRA_INPUT_ACCOUNTNAME, accountName);
		intent.putExtra(EXTRA_INPUT_APPACCOUNTID, storageId);
		intent.putExtra(EXTRA_INPUT_STORAGETYPE, storageType);
		intent.putExtra(EXTRA_INPUT_APPTOKEN, appToken);
		intent.putExtra(EXTRA_INPUT_HOST, host);
		intent.putExtra(EXTRA_INPUT_SERVICE, service);
		return intent;
	}
}
