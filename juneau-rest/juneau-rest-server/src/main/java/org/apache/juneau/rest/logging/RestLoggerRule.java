// ***************************************************************************************************************************
// * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file *
// * distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file        *
// * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance            *
// * with the License.  You may obtain a copy of the License at                                                              *
// *                                                                                                                         *
// *  http://www.apache.org/licenses/LICENSE-2.0                                                                             *
// *                                                                                                                         *
// * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an  *
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the        *
// * specific language governing permissions and limitations under the License.                                              *
// ***************************************************************************************************************************
package org.apache.juneau.rest.logging;

import java.util.function.*;
import java.util.logging.*;

import javax.servlet.http.*;

import org.apache.juneau.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.cp.*;
import org.apache.juneau.json.*;

/**
 * Represents a logging rule used by {@link RestLogger}.
 */
public class RestLoggerRule {

	//-----------------------------------------------------------------------------------------------------------------
	// Static
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new builder for this object.
	 *
	 * @return A new builder for this object.
	 */
	public static Builder create() {
		return new Builder();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Builder
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Builder class.
	 */
	public static class Builder extends BeanBuilder<RestLoggerRule> {

		Predicate<Integer> statusFilter;
		Predicate<HttpServletRequest> requestFilter;
		Predicate<HttpServletResponse> responseFilter;
		Predicate<Throwable> exceptionFilter;
		Enablement enabled;
		Predicate<HttpServletRequest> enabledTest;
		Level level;
		RestLoggingDetail requestDetail, responseDetail;
		boolean logStackTrace;

		/**
		 * Constructor.
		 */
		protected Builder() {
			super(RestLoggerRule.class);
		}

		/**
		 * Copy constructor.
		 *
		 * @param copyFrom The builder being copied.
		 */
		protected Builder(Builder copyFrom) {
			super(copyFrom);
			statusFilter = copyFrom.statusFilter;
			requestFilter = copyFrom.requestFilter;
			responseFilter = copyFrom.responseFilter;
			exceptionFilter = copyFrom.exceptionFilter;
			enabled = copyFrom.enabled;
			enabledTest = copyFrom.enabledTest;
			level = copyFrom.level;
			requestDetail = copyFrom.requestDetail;
			responseDetail = copyFrom.responseDetail;
			logStackTrace = copyFrom.logStackTrace;
		}

		@Override /* BeanBuilder */
		protected RestLoggerRule buildDefault() {
			return new RestLoggerRule(this);
		}

		@Override /* BeanBuilder */
		public Builder copy() {
			return new Builder(this);
		}

		//-------------------------------------------------------------------------------------------------------------
		// Properties
		//-------------------------------------------------------------------------------------------------------------

		/**
		 * Apply a status-based predicate check for this rule to match against.
		 *
		 * <h5 class='section'>Example:</h5>
		 * <p class='bcode w800'>
		 * 	<jc>// Create a logger rule that only matches if the status code is greater than or equal to 500.</jc>
		 * 	RestLogger
		 * 		.<jsm>createRule</jsm>()
		 * 		.statusFilter(<jv>x</jv> -&gt; <jv>x</jv> &gt;= 500)
		 * 		.build();
		 * </p>
		 *
		 * @param value
		 * 	The predicate check, or <jk>null</jk> to not use any filtering based on status code.
		 * @return This object (for method chaining).
		 */
		public Builder statusFilter(Predicate<Integer> value) {
			this.statusFilter = value;
			return this;
		}

		/**
		 * Apply a throwable-based predicate check for this rule to match against.
		 *
		 * <h5 class='section'>Example:</h5>
		 * <p class='bcode w800'>
		 * 	<jc>// Create a logger rule that only matches if a NotFound exception was thrown.</jc>
		 * 	RestLogger
		 * 		.<jsm>createRule</jsm>()
		 * 		.exceptionFilter(<jv>x</jv> -&gt; <jv>x</jv> <jk>instanceof</jk> NotFound)
		 * 		.build();
		 * </p>
		 *
		 * <p>
		 * This check is only performed if an actual throwable was thrown.  Therefore it's not necessary to perform a
		 * null check in the predicate.
		 *
		 * @param value
		 * 	The predicate check, or <jk>null</jk> to not use any filtering based on exceptions.
		 * @return This object (for method chaining).
		 */
		public Builder exceptionFilter(Predicate<Throwable> value) {
			this.exceptionFilter = value;
			return this;
		}

		/**
		 * Apply a request-based predicate check for this rule to match against.
		 *
		 * <h5 class='section'>Example:</h5>
		 * <p class='bcode w800'>
		 * 	<jc>// Create a logger rule that only matches if the servlet path contains "foobar".</jc>
		 * 	RestLogger
		 * 		.<jsm>createRule</jsm>()
		 * 		.requestFilter(<jv>x</jv> -&gt; <jv>x</jv>.getServletPath().contains(<js>"foobar"</js>))
		 * 		.build();
		 * </p>
		 *
		 * @param value
		 * 	The predicate check, or <jk>null</jk> to not use any filtering based on the request.
		 * @return This object (for method chaining).
		 */
		public Builder requestFilter(Predicate<HttpServletRequest> value) {
			this.requestFilter = value;
			return this;
		}

		/**
		 * Apply a response-based predicate check for this rule to match against.
		 *
		 * <h5 class='section'>Example:</h5>
		 * <p class='bcode w800'>
		 * 	<jc>// Create a logger rule that only matches if the servlet path contains "foobar".</jc>
		 * 	RestLogger
		 * 		.<jsm>createRule</jsm>()
		 * 		.responseFilter(<jv>x</jv> -&gt; <jv>x</jv>.getStatus() &gt;= 500)
		 * 		.build();
		 * </p>
		 *
		 * <p>
		 * Note that the {@link #statusFilter(Predicate)} and {@link #exceptionFilter(Predicate)} methods are simply
		 * convenience response filters.
		 *
		 * @param value
		 * 	The predicate check, or <jk>null</jk> to not use any filtering based on the response.
		 * @return This object (for method chaining).
		 */
		public Builder responseFilter(Predicate<HttpServletResponse> value) {
			this.responseFilter = value;
			return this;
		}

		/**
		 * Specifies whether logging is enabled when using this rule.
		 *
		 * <p>
		 * The possible values are:
		 * <ul>
		 * 	<li>{@link Enablement#ALWAYS ALWAYS} - Logging is enabled.
		 * 	<li>{@link Enablement#NEVER NEVER} - Logging is disabled.
		 * 	<li>{@link Enablement#CONDITIONAL CONDITIONALLY} - Logging is enabled if it passes the {@link #enabledTest(Predicate)} test.
		 * </ul>
		 *
		 * <h5 class='section'>Example:</h5>
		 * <p class='bcode w800'>
		 * 	<jc>// Create a logger rule where logging is only enabled if the query string contains "foobar".</jc>
		 * 	RestLogger
		 * 		.<jsm>createRule</jsm>()
		 * 		.enabled(<jsf>CONDITIONALLY</jsf>)
		 * 		.enabledTest(<jv>x</jv> -> <jv>x</jv>.getQueryString().contains(<js>"foobar"</js>))
		 * 		.build();
		 * </p>
		 *
		 * @param value
		 * 	The enablement flag value, or <jk>null</jk> to inherit from the call logger whose default value is {@link Enablement#ALWAYS ALWAYS}
		 * 	unless overridden via a <js>"juneau.restCallLogger.enabled"</js> system property or <js>"JUNEAU_RESTCALLLOGGER_ENABLED"</js> environment variable.
		 * @return This object (for method chaining).
		 */
		public Builder enabled(Enablement value) {
			this.enabled = value;
			return this;
		}

		/**
		 * Specifies the predicate test to use when the enabled setting is set to {@link Enablement#CONDITIONAL CONDITIONALLY}.
		 *
		 * <p>
		 * This setting has no effect if the enablement value is not {@link Enablement#CONDITIONAL CONDITIONALLY}.
		 *
		 * <h5 class='section'>Example:</h5>
		 * <p class='bcode w800'>
		 * 	<jc>// Create a logger rule where logging is only enabled if the query string contains "foobar".</jc>
		 * 	RestLogger
		 * 		.<jsm>createRule</jsm>()
		 * 		.enabled(<jsf>CONDITIONALLY</jsf>)
		 * 		.enabledTest(<jv>x</jv> -> <jv>x</jv>.getQueryString().contains(<js>"foobar"</js>))
		 * 		.build();
		 * </p>
		 *
		 * @param value
		 * 	The enablement predicate test, or <jk>null</jk> to inherit from the call logger whose default value is <c><jv>x</jv> -&gt; <jk>false</jk></c>.
		 * @return This object (for method chaining).
		 */
		public Builder enabledTest(Predicate<HttpServletRequest> value) {
			this.enabledTest = value;
			return this;
		}

		/**
		 * Shortcut for calling <c>enabled(<jsf>NEVER</jsf>)</c>.
		 *
		 * @return This object (for method chaining).
		 */
		public Builder disabled() {
			return this.enabled(Enablement.NEVER);
		}

		/**
		 * The level of detail to log on a request.
		 *
		 * <p>
		 * The possible values are:
		 * <ul>
		 * 	<li>{@link RestLoggingDetail#STATUS_LINE STATUS_LINE} - Log only the status line.
		 * 	<li>{@link RestLoggingDetail#HEADER HEADER} - Log the status line and headers.
		 * 	<li>{@link RestLoggingDetail#ENTITY ENTITY} - Log the status line and headers and body if available.
		 * </ul>
		 *
		 * @param value
		 * 	The new value for this property, or <jk>null</jk> to inherit from the call logger.
		 * @return This object (for method chaining).
		 */
		public Builder requestDetail(RestLoggingDetail value) {
			this.requestDetail = value;
			return this;
		}

		/**
		 * The level of detail to log on a response.
		 *
		 * <p>
		 * The possible values are:
		 * <ul>
		 * 	<li>{@link RestLoggingDetail#STATUS_LINE STATUS_LINE} - Log only the status line.
		 * 	<li>{@link RestLoggingDetail#HEADER HEADER} - Log the status line and headers.
		 * 	<li>{@link RestLoggingDetail#ENTITY ENTITY} - Log the status line and headers and body if available.
		 * </ul>
		 *
		 * @param value
		 * 	The new value for this property, or <jk>null</jk> to inherit from the call logger.
		 * @return This object (for method chaining).
		 */
		public Builder responseDetail(RestLoggingDetail value) {
			this.responseDetail = value;
			return this;
		}

		/**
		 * The logging level to use for logging the request/response.
		 *
		 * <p>
		 * The default value is {@link Level#INFO}.
		 *
		 * @param value
		 * 	The new value for this property, or <jk>null</jk> to inherit from the call logger.
		 * @return This object (for method chaining).
		 */
		public Builder level(Level value) {
			this.level = value;
			return this;
		}

		/**
		 * Log a stack trace as part of the log entry.
		 *
		 * <p>
		 * The default value is <jk>false</jk>.
		 *
		 * @return This object (for method chaining).
		 */
		public Builder logStackTrace() {
			this.logStackTrace = true;
			return this;
		}

		// <FluentSetters>

		@Override /* BeanBuilder */
		public Builder type(Class<? extends RestLoggerRule> value) {
			super.type(value);
			return this;
		}

		@Override /* BeanBuilder */
		public Builder impl(RestLoggerRule value) {
			super.impl(value);
			return this;
		}

		@Override /* BeanBuilder */
		public Builder outer(Object value) {
			super.outer(value);
			return this;
		}

		@Override /* BeanBuilder */
		public Builder beanStore(BeanStore value) {
			super.beanStore(value);
			return this;
		}

		// </FluentSetters>
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Instance
	//-----------------------------------------------------------------------------------------------------------------

	private final Predicate<Integer> statusFilter;
	private final Predicate<HttpServletRequest> requestFilter;
	private final Predicate<HttpServletResponse> responseFilter;
	private final Predicate<Throwable> exceptionFilter;
	private final Level level;
	private final Enablement enabled;
	private final Predicate<HttpServletRequest> enabledTest;
	private final RestLoggingDetail requestDetail, responseDetail;
	private final boolean logStackTrace;

	/**
	 * Constructor.
	 *
	 * @param b Builder
	 */
	RestLoggerRule(Builder b) {
		this.statusFilter = b.statusFilter;
		this.exceptionFilter = b.exceptionFilter;
		this.requestFilter = b.requestFilter;
		this.responseFilter = b.responseFilter;
		this.level = b.level;
		this.enabled = b.enabled;
		this.enabledTest = b.enabledTest;
		this.requestDetail = b.requestDetail;
		this.responseDetail = b.responseDetail;
		this.logStackTrace = b.logStackTrace;
	}

	/**
	 * Returns <jk>true</jk> if this rule matches the specified parameters.
	 *
	 * @param req The HTTP request being logged.  Never <jk>null</jk>.
	 * @param res The HTTP response being logged.  Never <jk>null</jk>.
	 * @return <jk>true</jk> if this rule matches the specified parameters.
	 */
	public boolean matches(HttpServletRequest req, HttpServletResponse res) {

		if (requestFilter != null && ! requestFilter.test(req))
			return false;

		if (responseFilter != null && ! responseFilter.test(res))
			return false;

		if (statusFilter != null && ! statusFilter.test(res.getStatus()))
			return false;

		Throwable e = (Throwable)req.getAttribute("Exception");
		if (e != null && exceptionFilter != null && ! exceptionFilter.test(e))
			return false;

		return true;
	}

	/**
	 * Returns the detail level for HTTP requests.
	 *
	 * @return the detail level for HTTP requests, or <jk>null</jk> if it's not set.
	 */
	public RestLoggingDetail getRequestDetail() {
		return requestDetail;
	}

	/**
	 * Returns the detail level for HTTP responses.
	 *
	 * @return the detail level for HTTP responses, or <jk>null</jk> if it's not set.
	 */
	public RestLoggingDetail getResponseDetail() {
		return responseDetail;
	}

	/**
	 * Returns the log level on this rule.
	 *
	 * @return The log level on this rule, or <jk>null</jk> if it's not set.
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * Returns the enablement flag value on this rule.
	 *
	 * @return The enablement flag value on this rule, or <jk>null</jk> if it's not set.
	 */
	public Enablement getEnabled() {
		return enabled;
	}

	/**
	 * Returns the enablement predicate test on this rule.
	 *
	 * @return The enablement predicate test on this rule, or <jk>null</jk> if it's not set.
	 */
	public Predicate<HttpServletRequest> getEnabledTest() {
		return enabledTest;
	}

	/**
	 * Returns <jk>true</jk> if a stack trace should be logged.
	 *
	 * @return <jk>true</jk> if a stack trace should be logged.
	 */
	public boolean isLogStackTrace() {
		return logStackTrace;
	}

	private OMap toMap() {
		return OMap.create()
			.a("codeFilter", statusFilter)
			.a("exceptionFilter", exceptionFilter)
			.a("requestFilter", requestFilter)
			.a("responseFilter", responseFilter)
			.a("level", level)
			.a("requestDetail", requestDetail)
			.a("responseDetail", responseDetail)
			.a("enabled", enabled)
			.a("enabledTest", enabledTest);
	}

	@Override /* Object */
	public String toString() {
		return SimpleJsonSerializer.DEFAULT.toString(toMap());
	}
}
