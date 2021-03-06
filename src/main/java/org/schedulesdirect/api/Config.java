/*
 *      Copyright 2012-2014 Battams, Derek
 *       
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */
package org.schedulesdirect.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;

/**
 * Provides various configuration options to the API.
 * 
 * <p>Only some constants are made public from this class</p>
 * 
 * @author Derek Battams &lt;derek@battams.ca&gt;
 *
 */
public final class Config {
	static private final Log LOG = LogFactory.getLog(Config.class);
	
	/**
	 * The version of this API build being used; this is NOT the version of the Schedules Direct service feed
	 */
	static public final String API_VERSION = initApiVersion();
	static private String initApiVersion() {
		try(InputStream apiProps = Config.class.getResourceAsStream("/sdjson-api-versioning.properties")) {
			if(apiProps != null) {
				Properties p = new Properties();
				p.load(apiProps);
				return p.getProperty("VERSION_DISPLAY");
			}			
		} catch(IOException e) {
			LOG.error("IOError", e);
			return "unknown";
		}
		LOG.warn("Unable to determine API version!  Setting to 'unknown'.");
		return "unknown";
	}

	/**
	 * The default URL for contacting the Schedules Direct JSON data feed server
	 */
	static public final String DEFAULT_BASE_URL = "https://json.schedulesdirect.org";
	static private Config INSTANCE = null;
	/**
	 * Obtain the singleton instance of the Config class
	 * @return The lone, global instance of the Config class
	 */
	static public Config get() {
		if(INSTANCE == null)
			INSTANCE = new Config();
		return INSTANCE;
	}
	
	private String dateTimeFmt;
	private ObjectMapper mapper;
	
	private Config() {
		dateTimeFmt = "yyyy-MM-dd'T'HH:mm:ss'Z'";
		mapper = new ObjectMapper();
		mapper.registerModule(new JsonOrgModule());
	}

	/**
	 * Return the ObjectMapper configured for this app
	 * @return A properly configured ObjectMapper
	 */
	public ObjectMapper getObjectMapper() { return mapper; }
	
	/**
	 * Return the expected format string for all date/time values in the upstream JSON
	 * @return The format string suitable for use in SimpleDateFormat constructor
	 */
	public String getDateTimeFormatString() { return dateTimeFmt; }
	
	/**
	 * Get a SimpleDateFormat instance for the configured date/time format string
	 * <p>This cannot be overridden at runtime.</p>
	 * @return The SimpleDateFormat instance
	 */
	public SimpleDateFormat getDateTimeFormat() {
		SimpleDateFormat fmt = new SimpleDateFormat(dateTimeFmt);
		fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		return fmt;
	}
	
	/**
	 * <p>
	 * 	When true, the api will capture and write all JSON parsing errors it
	 *  encounters to disk; useful for debugging and bug reporting.
	 * </p>
	 * <p>
	 *  Default is false, to enable set the JVM system property:
	 *  
	 *  <code>sdjson.capture.json-errors</code>
	 * </p>
	 * @return
	 */
	public boolean captureJsonParseErrors() {
		return System.getProperty("sdjson.capture.json-errors") != null;
	}

	/**
	 * <p>
	 * 	When true, the api will capture and write all JSON encoding errors it
	 *  encounters to disk; useful for debugging and bug reporting.
	 * </p>
	 * <p>
	 *  Default is false, to enable set the JVM system property:
	 *  
	 *  <code>sdjson.capture.encode-errors</code>
	 * </p>
	 * @return True if the option is enabled, false otherwise
	 */
	public boolean captureJsonEncodingErrors() {
		return System.getProperty("sdjson.capture.encode-errors") != null;
	}
	
	/**
	 * <p>
	 *  Specifies the root directory that all capture logs are written to.
	 * </p>
	 * <p>
	 *  Default is <code>${user.home}/.sdjson/capture</code>; can be overriden via system property:
	 *  
	 *  <code>sdjson.fs.capture=/path/to/root/dir</code>
	 * </p>
	 * @return The location to be used as the root sdjson capture logs
	 */
	public File captureRoot() {
		String capRoot = System.getProperty("sdjson.fs.capture");
		return new File(capRoot != null && capRoot.length() > 0 ? capRoot : String.format("%s/%s", System.getProperty("user.home"), ".sdjson/capture"));
	}

	/**
	 * <p>
	 * 	When true, the api will capture and write all HTTP communication with
	 *  the JSON service to a log file.
	 * </p>
	 * <p>
	 *  Default is false, to enable set the JVM system property:
	 *  
	 *  <code>sdjson.capture.http</code>
	 * </p>
	 * @return True if the option is enabled, false otherwise
	 */
	public boolean captureHttpComm() {
		return System.getProperty("sdjson.capture.http") != null;
	}

	/**
	 * <p>
	 * 	When true, the api will capture the content of all HTTP communication
	 *  with the JSON service.  This setting only has an affect if
	 *  <code>captureHttpComm() == true</code>.
	 * </p>
	 * <p>
	 *  Default is false, to enable set the JVM system property:
	 *  
	 *  <code>sdjson.capture.http.content</code>
	 * </p>
	 * @return True if the option is enabled, false otherwise
	 */
	public boolean captureHttpContent() {
		return captureHttpComm() && System.getProperty("sdjson.capture.http.content") != null;
	}
}
