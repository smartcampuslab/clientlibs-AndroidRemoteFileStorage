/**
 *    Copyright 2012-2013 Trento RISE
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
 */

package eu.trentorise.smartcampus.storage.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * User storage account informations
 * 
 * @author mirko perillo
 * 
 */
public class UserAccount implements Parcelable {
	/**
	 * id of the account
	 */
	private String id;
	/**
	 * id of the user
	 */
	private long userId;
	/**
	 * type of the storage
	 */
	private String appAccountId;

	private String appName;

	private StorageType storageType;

	private String accountName;
	/**
	 * list of the configurations of the account storage
	 */
	private List<Configuration> configurations;

	public UserAccount() {
		configurations = new ArrayList<Configuration>();
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public StorageType getStorageType() {
		return storageType;
	}

	public void setStorageType(StorageType storage) {
		this.storageType = storage;
	}

	public List<Configuration> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(List<Configuration> configurations) {
		this.configurations = configurations;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAppAccountId() {
		return appAccountId;
	}

	public void setAppAccountId(String appAccountId) {
		this.appAccountId = appAccountId;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(configurations);
		dest.writeString(accountName);
		dest.writeString(appAccountId);
		dest.writeString(appName);
		dest.writeString(id);
		dest.writeValue(storageType);
		dest.writeLong(userId);

	}

	public UserAccount(Parcel p) {
		configurations = new ArrayList<Configuration>();
		readFromParcel(p);
	}

	private void readFromParcel(Parcel in) {
		in.readTypedList(configurations, Configuration.CREATOR);
		accountName = in.readString();
		appAccountId = in.readString();
		appName = in.readString();
		id = in.readString();
		storageType = (StorageType) in.readValue(getClass().getClassLoader());
		userId = in.readLong();

	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public UserAccount createFromParcel(Parcel in) {
			return new UserAccount(in);
		}

		public UserAccount[] newArray(int size) {
			return new UserAccount[size];
		}
	};

}
