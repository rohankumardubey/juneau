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

import static java.util.logging.Level.*;
import static org.apache.juneau.rest.logging.RestLoggingDetail.*;

import java.util.concurrent.atomic.*;
import java.util.logging.*;

import org.apache.juneau.assertions.*;

/**
 *
 * Implementation of a {@link RestLogger} that captures log entries for testing logging itself.
 *
 * <p>
 * Instead of logging messages to a log file, messages are simply kept in an internal atomic string reference.
 * Once a message has been logged, you can use the {@link #getMessage()} or {@link #assertMessage()} methods
 * to access and evaluate it.
 *
 * <p>
 * The following example shows how the capture logger can be associated with a REST class so that logged messages can
 * be examined.
 *
 * <p class='bcode w800'>
 * 	<jk>public class</jk> MyTests {
 *
 * 		<jk>private static final</jk> CaptureLogger <jsf>LOGGER</jsf> = <jk>new</jk> CaptureLogger();
 *
 * 		<jk>public static class</jk> CaptureLogger <jk>extends</jk> BasicTestCaptureRestLogger {
 * 			<jc>// How our REST class will get the logger.</jc>
 * 			<jk>public static</jk> CaptureLogger <jsm>getInstance</jsm>() {
 * 				<jk>return</jk> <jsf>LOGGER</jsf>;
 * 			}
 * 		}
 *
 * 		<ja>@Rest</ja>(callLogger=CaptureLogger.<jk>class</jk>)
 * 		<jk>public static class</jk> TestRest <jk>implements</jk> BasicUniversalRest {
 * 			<ja>@RestGet</ja>
 * 			<jk>public boolean</jk> bad() <jk>throws</jk> InternalServerError {
 * 				<jk>throw new</jk> InternalServerError(<js>"foo"</js>);
 * 			}
 * 		}
 *
 * 		<ja>@Test</ja>
 * 		<jk>public void</jk> testBadRequestLogging() <jk>throws</jk> Exception {
 * 			<jc>// Create client that won't throw exceptions.</jc>
 * 			RestClient <jv>client</jv> = MockRestClient.<jsm>create</jsm>(TestRest.<jk>class</jk>).ignoreErrors().build();
 * 			<jc>// Make the REST call.</jc>
 * 			<jv>client</jv>.get(<js>"/bad"</js>).run().assertStatusCode(500).assertBody().contains(<js>"foo"</js>);
 * 			<jc>// Make sure the message was logged in our expected format.</jc>
 * 			<jsf>LOGGER</jsf>.assertMessageAndReset().contains(<js>"[500] HTTP GET /bad"</js>);
 * 		}
 * }
 * </p>
 */
public class BasicTestCaptureRestLogger extends BasicRestLogger {

	private AtomicReference<LogRecord> lastRecord = new AtomicReference<>();

	/**
	 * Constructor using specific settings.
	 *
	 * @param builder The settings to use for this logger.
	 */
	public BasicTestCaptureRestLogger(RestLogger.Builder builder) {
		super(builder);
	}

	/**
	 * Constructor using default settings.
	 * <p>
	 * Uses the same settings as {@link BasicRestLogger}.
	 */
	public BasicTestCaptureRestLogger() {
		super(builder());
	}

	private static RestLogger.Builder builder() {
		return RestLogger.create()
			.normalRules(  // Rules when debugging is not enabled.
				RestLoggerRule.create()  // Log 500+ errors with status-line and header information.
					.statusFilter(x -> x >= 500)
					.level(SEVERE)
					.requestDetail(HEADER)
					.responseDetail(HEADER)
					.build(),
				RestLoggerRule.create()  // Log 400-500 errors with just status-line information.
					.statusFilter(x -> x >= 400)
					.level(WARNING)
					.requestDetail(STATUS_LINE)
					.responseDetail(STATUS_LINE)
					.build()
			)
			.debugRules(  // Rules when debugging is enabled.
				RestLoggerRule.create()  // Log everything with full details.
					.level(SEVERE)
					.requestDetail(ENTITY)
					.responseDetail(ENTITY)
					.build()
			)
		;
	}

	@Override
	protected void log(Level level, String msg, Throwable e) {
		LogRecord r = new LogRecord(level, msg);
		r.setThrown(e);
		this.lastRecord.set(r);
	}

	/**
	 * Returns the last logged message.
	 *
	 * @return The last logged message, or <jk>null</jk> if nothing was logged.
	 */
	public String getMessage() {
		LogRecord r = lastRecord.get();
		return r == null ? null : r.getMessage();
	}

	/**
	 * Returns the last logged message and then deletes it internally.
	 *
	 * @return The last logged message.
	 */
	public String getMessageAndReset() {
		String msg = getMessage();
		reset();
		return msg;
	}

	/**
	 * Returns an assertion of the last logged message.
	 *
	 * @return The last logged message as an assertion object.  Never <jk>null</jk>.
	 */
	public StringAssertion assertMessage() {
		return new StringAssertion(getMessage());
	}

	/**
	 * Returns an assertion of the last logged message and then deletes it internally.
	 *
	 * @return The last logged message as an assertion object.  Never <jk>null</jk>.
	 */
	public StringAssertion assertMessageAndReset() {
		return new StringAssertion(getMessageAndReset());
	}

	/**
	 * Returns the last logged message level.
	 *
	 * @return The last logged message level, or <jk>null</jk> if nothing was logged.
	 */
	public Level getLevel() {
		LogRecord r = lastRecord.get();
		return r == null ? null : r.getLevel();
	}

	/**
	 * Returns the last logged message level.
	 *
	 * @return The last logged message level, or <jk>null</jk> if nothing was logged.
	 */
	public Throwable getThrown() {
		LogRecord r = lastRecord.get();
		return r == null ? null : r.getThrown();
	}

	/**
	 * Returns the last logged message level.
	 *
	 * @return The last logged message level, or <jk>null</jk> if nothing was logged.
	 */
	public ThrowableAssertion<Throwable> assertThrown() {
		return new ThrowableAssertion<>(getThrown());
	}

	/**
	 * Resets the internal message buffer.
	 *
	 * @return This object (for method chaining).
	 */
	public BasicTestCaptureRestLogger reset() {
		this.lastRecord.set(null);
		return this;
	}
}
