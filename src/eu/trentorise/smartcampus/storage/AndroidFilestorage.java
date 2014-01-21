package eu.trentorise.smartcampus.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;

import eu.trentorise.smartcampus.filestorage.client.Filestorage;
import eu.trentorise.smartcampus.filestorage.client.FilestorageException;
import eu.trentorise.smartcampus.filestorage.client.model.Account;
import eu.trentorise.smartcampus.filestorage.client.model.Configuration;
import eu.trentorise.smartcampus.filestorage.client.model.Metadata;
import eu.trentorise.smartcampus.filestorage.client.model.Resource;
import eu.trentorise.smartcampus.filestorage.client.model.StorageType;

public class AndroidFilestorage extends Filestorage {

	private static final String TAG = "AndroidFilestorage";

	private String appId;
	private String serverUrl;

	private AccessTokenPair token = null;
	private AppKeyPair app = null;

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
	/** Output parameter: account ID */
	public static final String EXTRA_OUTPUT_ACCOUNT_ID = "eu.trentorise.smartcampus.storage.ACCOUNT_ID";

	public AndroidFilestorage(String serverUrl, String appId) {
		super(serverUrl, appId);
		this.appId = appId;
		this.serverUrl = serverUrl;
	}

	/**
	 * @param type
	 * @return true if the specified storage type requires explicit user
	 *         authorization. In this case the account acquisition is performed
	 *         using
	 *         {@link #startAuthActivityForResult(Activity, String, String, String, StorageType, int)}
	 *         method.
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
	 * Start the storage authorization to create the user account. Returns the
	 * intent containing the {@link AndroidFilestorage#EXTRA_OUTPUT_ACCOUNT_ID}
	 * parameter with account ID in case of successful authorization or one of
	 * {@link Activity#RESULT_CANCELED},
	 * {@link AndroidFilestorage#RESULT_SC_CONNECTION_ERROR},
	 * {@link AndroidFilestorage#RESULT_SC_PROTOCOL_ERROR},
	 * {@link AndroidFilestorage#RESULT_SC_SECURITY_ERROR} response codes.
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
			StorageType storageType, int requestCode) {
		if (appId == null || storageType == null || authToken == null)
			throw new IllegalArgumentException(
					"Intent MUST have setted authToken, appname, accountName, storageType extras");

		Intent intent = createIntent(activity, appId, authToken, serverUrl);
		intent.setClass(activity, AuthActivity.class);
		intent.putExtras(intent);
		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * Create an account directly using the specified parameters. Should never
	 * be called from the UI thread.
	 * 
	 * @param token
	 *            user access token
	 * @param appId
	 *            application ID.
	 * @param name
	 *            account name
	 * @param type
	 *            {@link StorageType}
	 * @param configurations
	 *            list of {@link Configuration} parameters specific to the
	 *            storage (if any).
	 * @return {@link Account} created
	 * @throws FilestorageException
	 */
	public Account createAccount(String token, String appId, String name,
			StorageType type, List<Configuration> configurations)
			throws FilestorageException {
		if (isAuthenticationRequired(type)) {
			throw new FilestorageException("Type " + type
					+ " requires explicit authorization.");
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

	// public void startDropboxAuth(Activity a) {
	// Intent intent = new Intent();
	// intent.setClass(a, DropboxAuthActivity.class);
	// a.startActivity(intent);
	//
	// }

	public Metadata storeOnDropbox(File resource, String authToken,
			String accountId, boolean createSocialData, Context ctx) {
		// store

		try {
			DropboxUtils utils = new DropboxUtils(ctx);
			AppKeyPair appKeys = utils.getAppKey();
			if (appKeys == null) {
				return null;
			}
			AccessTokenPair userKeys = getUserKeys(ctx, authToken);
			if (userKeys == null) {
				return null;
			}
			AndroidAuthSession sourceSession = new AndroidAuthSession(appKeys,
					Session.AccessType.APP_FOLDER, userKeys);
			DropboxAPI<AndroidAuthSession> sourceClient = new DropboxAPI<AndroidAuthSession>(
					sourceSession);
			InputStream in = new FileInputStream(resource);
			Entry entry = sourceClient.putFile(resource.getName(), in,
					resource.length(), null, null);
			sourceSession.unlink();
			in.close();

			// create metadata
			return createMetadataByUser(authToken, toResource(entry),
					accountId, createSocialData);
		} catch (IOException e) {
		} catch (DropboxException e) {
			Log.e(TAG,
					String.format(
							"DropboxException storing resource %s:"
									+ e.getMessage(),
							resource.getAbsolutePath()));
		} catch (SecurityException e) {
			Log.e(TAG,
					String.format(
							"SecurityException storing resource %s:"
									+ e.getMessage(),
							resource.getAbsolutePath()));
		} catch (FilestorageException e) {
			Log.e(TAG,
					String.format("FilestorageException storing resource %s:"
							+ e.getMessage(), resource.getAbsolutePath()));
		}
		return null;
	}

	private Resource toResource(Entry dropboxEntry) {
		Resource res = new Resource();
		res.setName(dropboxEntry.fileName());
		res.setContentType(dropboxEntry.mimeType);
		res.setSize(dropboxEntry.bytes);
		return res;
	}

	private static Intent createIntent(Context ctx, String appId,
			String authToken, String service) {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_INPUT_APPID, appId);
		intent.putExtra(EXTRA_INPUT_AUTHTOKEN, authToken);
		intent.putExtra(EXTRA_INPUT_SERVICE, service);
		return intent;
	}

	private AccessTokenPair getUserKeys(Context ctx, String authToken) {
		DropboxUtils utils = new DropboxUtils(ctx);
		AccessTokenPair userKeys = utils.getUserKeys();
		if (userKeys == null) {
			Account account = null;
			try {
				account = getAccountByUser(authToken);
			} catch (SecurityException e) {
				Log.i(TAG,
						"SecurityException getting user account:"
								+ e.getMessage());
			} catch (FilestorageException e) {
				Log.i(TAG,
						"SecurityException getting user account:"
								+ e.getMessage());
			}
			if (account != null) {
				utils.storeUserKeys(account);
				userKeys = utils.getUserKeys();
			}
		}
		return userKeys;
	}
}
