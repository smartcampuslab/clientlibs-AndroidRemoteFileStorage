package eu.trentorise.smartcampus.storage.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class ConfigurationWrapper implements Parcelable {

	private eu.trentorise.smartcampus.filestorage.client.model.Configuration configuration;

	public ConfigurationWrapper(
			eu.trentorise.smartcampus.filestorage.client.model.Configuration conf) {
		configuration = conf;
	}

	public ConfigurationWrapper(Parcel p) {
		configuration = new eu.trentorise.smartcampus.filestorage.client.model.Configuration();
		readFromParcel(p);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(configuration.getName());
		dest.writeString(configuration.getValue());

	}

	private void readFromParcel(Parcel in) {
		configuration.setName(in.readString());
		configuration.setValue(in.readString());

	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public ConfigurationWrapper createFromParcel(Parcel in) {
			return new ConfigurationWrapper(in);
		}

		public ConfigurationWrapper[] newArray(int size) {
			return new ConfigurationWrapper[size];
		}
	};

	public static List<ConfigurationWrapper> toWrapper(
			List<eu.trentorise.smartcampus.filestorage.client.model.Configuration> confs) {
		List<ConfigurationWrapper> wrappedList = new ArrayList<ConfigurationWrapper>();
		for (eu.trentorise.smartcampus.filestorage.client.model.Configuration conf : confs) {
			wrappedList.add(new ConfigurationWrapper(conf));
		}
		return wrappedList;
	}
}
