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
package org.apache.juneau;

import java.text.*;

import org.apache.juneau.internal.*;

/**
 * Subclass of runtime exceptions that take in a message and zero or more arguments.
 */
public abstract class BasicRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	final boolean unmodifiable;

	/**
	 * Creates a new builder for this bean.
	 *
	 * @return A new builder for this bean.
	 */
	public static BasicRuntimeExceptionBuilder create() {
		return new BasicRuntimeExceptionBuilder();
	}

	/**
	 * Constructor.
	 *
	 * @param builder The builder containing the settings for this exception.
	 */
	public BasicRuntimeException(BasicRuntimeExceptionBuilder builder) {
		super(builder.message, builder.causedBy);
		this.unmodifiable = builder.unmodifiable;
	}

	/**
	 * Constructor.
	 *
	 * @param cause The cause of this exception.
	 * @param message The {@link MessageFormat}-style message.
	 * @param args Optional {@link MessageFormat}-style arguments.
	 */
	public BasicRuntimeException(Throwable cause, String message, Object...args) {
		this(create().causedBy(cause).message(message, args));
	}

	/**
	 * Constructor.
	 *
	 * @param message The {@link MessageFormat}-style message.
	 * @param args Optional {@link MessageFormat}-style arguments.
	 */
	public BasicRuntimeException(String message, Object...args) {
		this(create().message(message, args));
	}

	/**
	 * Constructor.
	 *
	 * @param cause The cause of this exception.
	 */
	public BasicRuntimeException(Throwable cause) {
		this(create().causedBy(cause));
	}

	/**
	 * Same as {@link #getCause()} but searches the throwable chain for an exception of the specified type.
	 *
	 * @param c The throwable type to search for.
	 * @param <T> The throwable type to search for.
	 * @return The exception, or <jk>null</jk> if not found.
	 */
	public <T extends Throwable> T getCause(Class<T> c) {
		return ThrowableUtils.getCause(c, this);
	}

	@Override /* Throwable */
	public String getMessage() {
		String m = super.getMessage();
		if (m == null && getCause() != null)
			m = getCause().getMessage();
		return m;
	}

	@Override /* Throwable */
	public synchronized Throwable fillInStackTrace() {
		assertModifiable();
		return super.fillInStackTrace();
	}

	@Override /* Throwable */
	public synchronized Throwable initCause(Throwable cause) {
		assertModifiable();
		return super.initCause(cause);
	}

	@Override /* Throwable */
	public void setStackTrace(StackTraceElement[] stackTrace) {
		assertModifiable();
		super.setStackTrace(stackTrace);
	}

	/**
	 * Returns the caused-by exception if there is one.
	 *
	 * @return The caused-by exception if there is one, or this exception if there isn't.
	 */
	public Throwable unwrap() {
		Throwable t = getCause();
		return t == null ? this : t;
	}

	/**
	 * Throws an {@link UnsupportedOperationException} if the unmodifiable flag is set on this bean.
	 */
	protected final void assertModifiable() {
		if (unmodifiable)
			throw new UnsupportedOperationException("Bean is read-only");
	}
}
