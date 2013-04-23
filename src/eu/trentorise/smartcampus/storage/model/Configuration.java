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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <i>Configuration</i> is the representation of a configuration in a user
 * storage account
 * 
 * @author mirko perillo
 * 
 */
public class Configuration implements Parcelable {
	/**
	 * name of configuration
	 */
	private String name;
	/**
	 * value of configuration
	 */
	private String value;

	public Configuration() {

	}

	public Configuration(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(value);

	}

	public Configuration(Parcel p) {
		readFromParcel(p);
	}

	private void readFromParcel(Parcel in) {
		name = in.readString();
		value = in.readString();

	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public Configuration createFromParcel(Parcel in) {
			return new Configuration(in);
		}

		public Configuration[] newArray(int size) {
			return new Configuration[size];
		}
	};

}
