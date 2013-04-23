/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.storage;

public class Constants {

	public static final String MULTIPART_FILE_PARAM = "file";

	public static final String APP_KEY_CONF = "APP_KEY";
	public static final String APP_SECRET_CONF = "APP_SECRET";

	public static final String USER_KEY_CONF = "USER_KEY";
	public static final String USER_SECRET_CONF = "USER_SECRET";

	public static final int AUTH_REQUEST_CODE = 1000;

	public enum RESULT {OK, PROTOCOL, CONNECTION, SECURITY};

}
