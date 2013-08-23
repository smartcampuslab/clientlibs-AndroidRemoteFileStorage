package eu.trentorise.smartcampus.storage.model;

import android.os.Parcel;
import android.os.Parcelable;
import eu.trentorise.smartcampus.filestorage.client.model.Account;

public class AccountWrapper implements Parcelable {

	private Account account;

	public AccountWrapper(Account account) {
		this.account = account;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(ConfigurationWrapper.toWrapper(account
				.getConfigurations()));
		dest.writeString(account.getName());
		dest.writeString(account.getStorageId());
		dest.writeString(account.getAppId());
		dest.writeString(account.getId());
		dest.writeValue(account.getStorageType());
		dest.writeString(account.getUserId());

	}

	public AccountWrapper(Parcel p) {
		account = new Account();
		readFromParcel(p);
	}

	private void readFromParcel(Parcel in) {
		in.readTypedList(account.getConfigurations(), Configuration.CREATOR);
		account.setName(in.readString());
		account.setStorageId(in.readString());
		account.setAppId(in.readString());
		account.setId(in.readString());
		account.setStorageType((eu.trentorise.smartcampus.filestorage.client.model.StorageType) in
				.readValue(getClass().getClassLoader()));
		account.setUserId(in.readString());

	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public AccountWrapper createFromParcel(Parcel in) {
			return new AccountWrapper(in);
		}

		public AccountWrapper[] newArray(int size) {
			return new AccountWrapper[size];
		}
	};

}
