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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import eu.trentorise.smartcampus.filestorage.client.Filestorage;
import eu.trentorise.smartcampus.filestorage.client.FilestorageException;
import eu.trentorise.smartcampus.filestorage.client.model.Account;
import eu.trentorise.smartcampus.fs.R;

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

	private Filestorage fileStorage;
	private String authToken;
	private String appId;

	private WebView mWebView;
    private ProgressDialog mSpinner; 
    private ImageView mCrossImage; 
    private FrameLayout mContent;

    static final FrameLayout.LayoutParams FILL =
            new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                             ViewGroup.LayoutParams.MATCH_PARENT);

    private AsyncTask<Void, Void, String> initTask = new AsyncTask<Void, Void, String>(){
    	private int result = -1;
    	@Override
    	protected void onPreExecute() {
    		mSpinner.show(); 
    	}
    	
		@Override
		protected String doInBackground(Void... params) {
			try {
				return fileStorage.getAuthorizationURL(authToken);
			} catch (FilestorageException e) {
				result = AndroidFilestorage.RESULT_SC_PROTOCOL_ERROR;
				return null;
			} catch (SecurityException e) {
				result = AndroidFilestorage.RESULT_SC_PROTOCOL_ERROR;
				return null;
			} 
		}
    	
		protected void onPostExecute(String uri) {
			if (uri == null) {
				mSpinner.dismiss();
				onNegativeResult(result);
			} else {
				startWebView(uri);
			}
		}
    };
    
    private AsyncTask<String, Void, Account> readAccountTask = new AsyncTask<String, Void, Account>(){
    	private int result = -1;
		@Override
		protected Account doInBackground(String ...params) {
			try {
				return fileStorage.getAccountByUser(authToken);
			} catch (FilestorageException e) {
				result = AndroidFilestorage.RESULT_SC_PROTOCOL_ERROR;
				return null;
			} catch (SecurityException e) {
				result = AndroidFilestorage.RESULT_SC_PROTOCOL_ERROR;
				return null;
			} 
		}
    	
		protected void onPostExecute(Account a) {
			if (a != null) {
				onAccountAcquired(a);
			} else {
				onNegativeResult(result);
			}
		};
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		appId = intent.getStringExtra(AndroidFilestorage.EXTRA_INPUT_APPID);
		authToken = intent
				.getStringExtra(AndroidFilestorage.EXTRA_INPUT_AUTHTOKEN);
		String service = intent
				.getStringExtra(AndroidFilestorage.EXTRA_INPUT_SERVICE);
		fileStorage = new Filestorage(service, appId);
		setUp();
	}
	
    protected void setUp() {
        mSpinner = new ProgressDialog(this);
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading..."); 
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        mContent = new FrameLayout(this); 

        createCrossImage(); 
        int crossWidth = mCrossImage.getDrawable().getIntrinsicWidth();
        setUpWebView(crossWidth / 2); 
        
        mContent.addView(mCrossImage, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addContentView(mContent, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)); 
    }

    @SuppressLint("SetJavaScriptEnabled")
	private void setUpWebView(int margin) {
        LinearLayout webViewContainer = new LinearLayout(this);
        mWebView = new WebView(this);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        CookieSyncManager.createInstance(getApplicationContext());
        CookieManager cookieManager = CookieManager.getInstance(); 
        cookieManager.removeAllCookie();
        
        initTask.execute();
        
        mWebView.setLayoutParams(FILL);
        mWebView.setVisibility(View.INVISIBLE);
        
        webViewContainer.setPadding(margin, margin, margin, margin);
        webViewContainer.addView(mWebView);
        mContent.addView(webViewContainer);
    } 

    private void createCrossImage() {
        mCrossImage = new ImageView(this);
        // Dismiss the dialog when user click on the 'x'
        mCrossImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	onNegativeResult(Activity.RESULT_CANCELED);
            }
        });
        Drawable crossDrawable = getResources().getDrawable(R.drawable.close);
        mCrossImage.setImageDrawable(crossDrawable);
        /* 'x' should not be visible while webview is loading
         * make it visible only after webview has fully loaded
        */
        mCrossImage.setVisibility(View.INVISIBLE);
    } 

    
    
	private void startWebView(String url) {
		mWebView.setWebViewClient(new AuthWebViewClient());
	    mWebView.loadUrl(url);

	}
    
	protected void onAccountAcquired(Account account) {
		Intent i = new Intent();
		i.putExtra(AndroidFilestorage.EXTRA_OUTPUT_ACCOUNT_ID, account.getId());
		setResult(Activity.RESULT_OK, i);
		finish();
	}

	protected void onNegativeResult(int result) {
		setResult(result);
		finish();
	}

	public class AuthWebViewClient extends WebViewClient {
		
		private boolean verified = false;

		public AuthWebViewClient() {
			super();
		}

		private boolean verifyUrl(String url) throws NameNotFoundException {
			if (isOkUrl(url)){
				String accountId = Uri.parse(url).getQueryParameter("accountId");
				readAccountTask.execute(accountId);
				return true;
			} 
			if (isFailureUrl(url)) {
				onNegativeResult(AndroidFilestorage.RESULT_SC_PROTOCOL_ERROR);
				return true;
			}
			return false;
		}

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if (!verified) {
				try {
					verified  = verifyUrl(url);
				} catch (NameNotFoundException e) {
					onNegativeResult(AndroidFilestorage.RESULT_SC_PROTOCOL_ERROR);
				}
			} 
			if (!verified) {
	            super.onPageStarted(view, url, favicon);
	            mSpinner.show();
			} else {
	            mWebView.setVisibility(View.INVISIBLE);
	            mCrossImage.setVisibility(View.INVISIBLE);
			}
        }  
		
		@Override
		public void onPageFinished(WebView view, String url) {
			if (!verified) {
				super.onPageFinished(view, url);
	            mSpinner.dismiss();
	            /* 
	             * Once webview is fully loaded, set the mContent background to be transparent
	             * and make visible the 'x' image. 
	             */
	            mContent.setBackgroundColor(Color.TRANSPARENT);
	            mWebView.setVisibility(View.VISIBLE);
	            mCrossImage.setVisibility(View.VISIBLE);
			}
        }

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) 
		{
			handler.proceed();
		}
	}
	
	/**
	 * @param url
	 * @return true if the url is the correct redirect url with code request parameter
	 */
	public boolean isOkUrl(String url) {
		Uri uri = Uri.parse(url);
		return "ok".equals(uri.getQueryParameter("status")) && uri.getQueryParameter("accountId") != null;
	}

	/**
	 * @param url
	 * @return true if the url is the correct redirect url with code request parameter
	 */
	public boolean isFailureUrl(String url) {
		Uri uri = Uri.parse(url);
		return "error".equals(uri.getQueryParameter("status")) && uri.getQueryParameter("error_message") != null;
	}

}
