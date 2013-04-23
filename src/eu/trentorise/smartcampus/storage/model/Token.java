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

import java.util.Map;

public class Token {
	/* Token data */
	/**
	 * the set of security information to access the resource
	 */
	private Map<String, Object> metadata;

	/* parameters for REST invocation */

	/**
	 * HTTP method to access the resource
	 */
	private String methodREST;
	/**
	 * direct URL to access the resource
	 */
	private String url;
	/**
	 * optional HTTP headers to access the resource
	 */
	private Map<String, String> httpHeaders;

	/**
	 * storage which stores the resource
	 */
	private StorageType storageType;

	public Map<String, Object> getMetadata() {
		return metadata;

	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public String getMethodREST() {
		return methodREST;
	}

	public void setMethodREST(String methodREST) {
		this.methodREST = methodREST;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, String> getHttpHeaders() {
		return httpHeaders;
	}

	public void setHttpHeaders(Map<String, String> httpHeaders) {
		this.httpHeaders = httpHeaders;
	}

	public StorageType getStorageType() {
		return storageType;
	}

	public void setStorageType(StorageType storageType) {
		this.storageType = storageType;
	}
}
