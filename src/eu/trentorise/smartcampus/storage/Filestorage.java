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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
import eu.trentorise.smartcampus.protocolcarrier.custom.FileRequestParam;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;
import eu.trentorise.smartcampus.protocolcarrier.custom.RequestParam;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.dropbox.DropboxAuth;
import eu.trentorise.smartcampus.storage.impl.HttpResourceRetriever;
import eu.trentorise.smartcampus.storage.model.AppAccount;
import eu.trentorise.smartcampus.storage.model.ListAppAccount;
import eu.trentorise.smartcampus.storage.model.ListUserAccount;
import eu.trentorise.smartcampus.storage.model.Metadata;
import eu.trentorise.smartcampus.storage.model.Resource;
import eu.trentorise.smartcampus.storage.model.StorageType;
import eu.trentorise.smartcampus.storage.model.Token;
import eu.trentorise.smartcampus.storage.model.UserAccount;

/**
 * This class provides an interface to access the remote file storage
 * functionaty: CRUD on resources (files), CRD on user account and reading the
 * app accounts.
 * 
 * @author raman
 * 
 */
public class Filestorage {

	private Context mCtx;
	private ProtocolCarrier mProtocolCarrier;
	private String appName;
	private String appToken;
	private String host;
	private String service;

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

	/**
	 * Create a new Filestorage service connector, given the execution context,
	 * app name, app token, remote service host and service context path
	 * 
	 * @param ctx
	 * @param appName
	 * @param appToken
	 * @param host
	 * @param service
	 */
	public Filestorage(Context ctx, String appName, String appToken,
			String host, String service) {
		mCtx = ctx;
		mProtocolCarrier = new ProtocolCarrier(mCtx, appToken);
		this.appName = appName;
		this.appToken = appToken;
		this.host = host;
		this.service = service;
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
	 * @param appAccountId
	 * @param storageType
	 *            storage type
	 * @param requestCode
	 * 
	 */
	public void startAuthActivityForResult(Activity activity, String authToken,
			String appAccountName, String appAccountId,
			StorageType storageType, int requestCode) {
		if (appName == null || appAccountName == null || appAccountId == null
				|| storageType == null || authToken == null)
			throw new IllegalArgumentException(
					"Intent MUST have setted authToken, appname, accountName, appAccountId,storageType extras");

		Intent intent = createIntent(activity, appName, appToken, authToken,
				appAccountName, appAccountId, storageType, host, service);

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

	/**
	 * Store resource remotely.
	 * 
	 * @param content
	 *            byte array of the file content
	 * @param contentType
	 *            file MIME type
	 * @param resourceName
	 *            name of the file
	 * @param authToken
	 * @param userAccountId
	 *            user account ID
	 * @param createSocialData
	 *            true to create social entity associated to the resource
	 * @return information about created resource
	 * @throws ProtocolException
	 * @throws IOException
	 * @throws ConnectionException
	 * @throws SecurityException
	 * @throws URISyntaxException
	 */
	public Metadata storeResource(byte[] content, String contentType,
			String resourceName, String authToken, String userAccountId,
			boolean createSocialData) throws ProtocolException, IOException,
			ConnectionException, SecurityException, URISyntaxException {
		MessageRequest request = new MessageRequest(host, service
				+ "/resource/" + appName + "/" + userAccountId);
		request.setMethod(Method.POST);
		request.setQuery("createSocialData=" + createSocialData);
		FileRequestParam fileParam = new FileRequestParam();

		fileParam.setContent(content);
		fileParam.setContentType(contentType);
		fileParam.setFilename(resourceName);
		fileParam.setParamName("file");

		List<RequestParam> params = new ArrayList<RequestParam>();
		params.add(fileParam);
		request.setRequestParams(params);

		MessageResponse response = mProtocolCarrier.invokeSync(request,
				appToken, authToken);
		if (response.getHttpStatus() == 200) {
			return Utils
					.convertJSONToObject(response.getBody(), Metadata.class);
		} else {
			return null;
		}
	}

	/**
	 * Delete the specified resource
	 * 
	 * @param authToken
	 * @param userAccountId
	 *            user account ID
	 * @param resourceId
	 *            ID of the resource to be deleted.
	 * @throws ProtocolException
	 * @throws ConnectionException
	 * @throws SecurityException
	 */
	public void deleteResource(String authToken, String userAccountId,
			String resourceId) throws ProtocolException, ConnectionException,
			SecurityException {
		MessageRequest request = new MessageRequest(host, service
				+ "/resource/" + appName + "/" + userAccountId + "/"
				+ resourceId);
		request.setMethod(Method.DELETE);

		mProtocolCarrier.invokeSync(request, appToken, authToken);
	}

	/**
	 * 
	 * @param authToken
	 * @param userAccountId
	 *            user account ID
	 * @param resourceId
	 *            ID of the resource to be deleted.
	 * @param content
	 *            byte array of the file content
	 * @throws ProtocolException
	 * @throws ConnectionException
	 * @throws SecurityException
	 */
	public void updateResource(String authToken, String userAccountId,
			String resourceId, byte[] content) throws ProtocolException,
			ConnectionException, SecurityException {

		Metadata meta = getResourceMetadata(authToken, resourceId);
		MessageRequest request = new MessageRequest(host, service
				+ "/resource/" + appName + "/" + userAccountId + "/"
				+ resourceId);
		request.setMethod(Method.POST);
		FileRequestParam fileParam = new FileRequestParam();
		fileParam.setContent(content);
		fileParam.setContentType(meta.getContentType());
		fileParam.setParamName("file");
		fileParam.setFilename("");
		List<RequestParam> params = new ArrayList<RequestParam>();
		params.add(fileParam);
		request.setRequestParams(params);

		mProtocolCarrier.invokeSync(request, appToken, authToken);
	}

	/**
	 * Read all the accounts associated to the specified user in the current
	 * app.
	 * 
	 * @param authToken
	 * @return
	 * @throws ProtocolException
	 * @throws ConnectionException
	 * @throws SecurityException
	 */
	public List<UserAccount> getUserAccounts(String authToken)
			throws ProtocolException, ConnectionException, SecurityException {

		MessageRequest request = new MessageRequest(host, service
				+ "/useraccount/" + appName);
		request.setMethod(Method.GET);

		MessageResponse response = mProtocolCarrier.invokeSync(request,
				appToken, authToken);
		if (response.getHttpStatus() == 200) {
			return Utils.convertJSONToObject(response.getBody(),
					ListUserAccount.class).getUserAccounts();
		} else {
			return null;
		}
	}

	/**
	 * Return all the accounts associated with the current app
	 * 
	 * @param authToken
	 * @return
	 * @throws ProtocolException
	 * @throws ConnectionException
	 * @throws SecurityException
	 */
	public List<AppAccount> getAppAccounts(String authToken)
			throws ProtocolException, ConnectionException, SecurityException {
		MessageRequest request = new MessageRequest(host, service
				+ "/appaccount/" + appName);
		request.setMethod(Method.GET);
		MessageResponse response = mProtocolCarrier.invokeSync(request,
				appToken, authToken);
		if (response.getHttpStatus() == 200) {
			return Utils.convertJSONToObject(response.getBody(),
					ListAppAccount.class).getAppAccounts();
		} else {
			return null;
		}
	}

	/**
	 * Store the user account remotely
	 * 
	 * @param authToken
	 * @param userAccount
	 * @return
	 * @throws ProtocolException
	 * @throws ConnectionException
	 * @throws SecurityException
	 */
	public UserAccount storeUserAccount(String authToken,
			UserAccount userAccount) throws ProtocolException,
			ConnectionException, SecurityException {
		MessageRequest request = new MessageRequest(host, service
				+ "/useraccount/" + appName);
		request.setMethod(Method.POST);
		request.setBody(Utils.convertToJSON(userAccount));
		MessageResponse response = mProtocolCarrier.invokeSync(request,
				appToken, authToken);
		if (response.getHttpStatus() == 200) {
			return Utils.convertJSONToObject(response.getBody(),
					UserAccount.class);
		} else {
			return null;
		}

	}

	/**
	 * Read the shared file with the specified ID
	 * 
	 * @param authToken
	 * @param resourceId
	 * @return the {@link Resource} object describing the remote file.
	 * @throws ProtocolException
	 * @throws ConnectionException
	 * @throws SecurityException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public Resource getSharedResource(String authToken, String resourceId)
			throws ProtocolException, ConnectionException, SecurityException,
			ClientProtocolException, IOException {
		return getResource(authToken, resourceId, false);
	}

	/**
	 * Reads a owned resource with specified ID
	 * 
	 * @param authToken
	 * @param resourceId
	 * @return the {@link Resource} object describing the remote file.
	 * @throws ClientProtocolException
	 * @throws ConnectionException
	 * @throws ProtocolException
	 * @throws SecurityException
	 * @throws IOException
	 */
	public Resource getMyResource(String authToken, String resourceId)
			throws ClientProtocolException, ConnectionException,
			ProtocolException, SecurityException, IOException {
		return getResource(authToken, resourceId, true);
	}

	private Resource getResource(String authToken, String resourceId,
			boolean owned) throws ConnectionException, ProtocolException,
			SecurityException, ClientProtocolException, IOException {
		String functionality = owned ? "myresource" : "resource";
		MessageRequest request = new MessageRequest(host, service + "/"
				+ functionality + "/" + appName + "/" + resourceId);
		request.setMethod(Method.GET);
		MessageResponse response = mProtocolCarrier.invokeSync(request,
				appToken, authToken);
		String body = response.getBody();
		Token token = Utils.convertJSONToObject(body, Token.class);

		ResourceRetriever retriever = resourceRetrieverFactory(token);
		return retriever.getResource(authToken, resourceId, token);
	}

	private ResourceRetriever resourceRetrieverFactory(Token token) {
		ResourceRetriever retriever = null;
		switch (token.getStorageType()) {
		case DROPBOX:
			retriever = new HttpResourceRetriever(mCtx, appName, appToken,
					host, service);
			break;

		default:
			throw new IllegalArgumentException(
					"StorageType requested doesn't exist");
		}

		return retriever;
	}

	/**
	 * Read the resource metadata.
	 * 
	 * @param authToken
	 * @param resourceId
	 * @return
	 * @throws ProtocolException
	 * @throws ConnectionException
	 * @throws SecurityException
	 */
	public Metadata getResourceMetadata(String authToken, String resourceId)
			throws ProtocolException, ConnectionException, SecurityException {
		MessageRequest request = new MessageRequest(host, service
				+ "/metadata/" + appName + "/" + resourceId);
		request.setMethod(Method.GET);
		MessageResponse response = mProtocolCarrier.invokeSync(request,
				appToken, authToken);
		if (response.getHttpStatus() == 200) {
			return Utils
					.convertJSONToObject(response.getBody(), Metadata.class);
		} else {
			return null;
		}
	}

	/**
	 * update the social data associated to the resource
	 * 
	 * @param authToken
	 * @param rid
	 * @param resourceId
	 * @param entityId
	 *            social entity id to associated to the resource
	 * @return the updated information about resource
	 * @throws ConnectionException
	 * @throws ProtocolException
	 * @throws SecurityException
	 */
	public Metadata updateSocialData(String authToken, String rid,
			String resourceId, String entityId) throws ConnectionException,
			ProtocolException, SecurityException {
		MessageRequest request = new MessageRequest(host, service
				+ "/updatesocial/" + appName + "/" + resourceId + "/"
				+ entityId);
		request.setMethod(Method.PUT);
		MessageResponse response = mProtocolCarrier.invokeSync(request,
				appToken, authToken);
		if (response.getHttpStatus() == 200) {
			return Utils
					.convertJSONToObject(response.getBody(), Metadata.class);
		} else {
			return null;
		}
	}

	private static Intent createIntent(Context ctx, String appName,
			String appToken, String authToken, String accountName,
			String appAccountId, StorageType storageType, String host,
			String service) {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_INPUT_APPNAME, appName);
		intent.putExtra(EXTRA_INPUT_AUTHTOKEN, authToken);
		intent.putExtra(EXTRA_INPUT_ACCOUNTNAME, accountName);
		intent.putExtra(EXTRA_INPUT_APPACCOUNTID, appAccountId);
		intent.putExtra(EXTRA_INPUT_STORAGETYPE, storageType);
		intent.putExtra(EXTRA_INPUT_APPTOKEN, appToken);
		intent.putExtra(EXTRA_INPUT_HOST, host);
		intent.putExtra(EXTRA_INPUT_SERVICE, service);
		return intent;
	}

}
