package eu.trentorise.smartcampus.storage;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import eu.trentorise.smartcampus.filestorage.client.model.Account;
import eu.trentorise.smartcampus.filestorage.client.model.Configuration;

public class DropboxUtils {

	protected static String ACCOUNT_PREFS_NAME = "lifelog_dropbox";
	protected static String ACCESS_KEY_NAME = "dropbox_user_key";
	protected static String ACCESS_SECRET_NAME = "dropbox_user_secret";

	protected static String META_APP_KEY = "dropbox_api_key";
	protected static String META_APP_SECRET = "dropbox_api_secret";

	private static final String TAG = "DropboxUtils";
	Context ctx;

	public DropboxUtils(Context ctx) {
		this.ctx = ctx;
	}

	public AppKeyPair getAppKey() {
		try {
			ApplicationInfo ai = ctx.getPackageManager().getApplicationInfo(
					ctx.getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			String appKey = bundle.getString(META_APP_KEY);
			String appSecret = bundle.getString(META_APP_SECRET);
			if (appKey != null && appSecret != null) {
				return new AppKeyPair(appKey, appSecret);
			} else {
				return null;
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG,
					"Failed to load meta-data, NameNotFound: " + e.getMessage());
		} catch (NullPointerException e) {
			Log.e(TAG,
					"Failed to load meta-data, NullPointer: " + e.getMessage());
		}
		return null;
	}

	public AccessTokenPair getUserKeys() {
		SharedPreferences prefs = ctx.getSharedPreferences(ACCOUNT_PREFS_NAME,
				0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key != null && secret != null) {
			return new AccessTokenPair(key, secret);
		} else {
			return null;
		}
	}

	public void storeUserKeys(Account account) {
		List<Configuration> confs = account.getConfigurations();
		String key = null, secret = null;
		for (Configuration conf : confs) {
			if (conf.getName().equals("USER_KEY")) {
				key = conf.getValue();
			}

			if (conf.getName().equals("USER_SECRET")) {
				secret = conf.getValue();
			}
		}

		storeUserKeys(key, secret);
	}

	public void storeUserKeys(String key, String secret) {
		// Save the access key for later
		SharedPreferences prefs = ctx.getSharedPreferences(ACCOUNT_PREFS_NAME,
				0);
		Editor edit = prefs.edit();
		edit.putString(ACCESS_KEY_NAME, key);
		edit.putString(ACCESS_SECRET_NAME, secret);
		edit.commit();
	}

	public void clearUserKeys() {
		SharedPreferences prefs = ctx.getSharedPreferences(ACCOUNT_PREFS_NAME,
				0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}
}
