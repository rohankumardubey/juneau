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
package org.apache.juneau.rest.assertions;

import java.io.*;
import java.util.function.*;

import org.apache.http.*;
import org.apache.juneau.assertions.*;
import org.apache.juneau.http.response.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.serializer.*;

/**
 * Used for fluent assertion calls against {@link RequestLine} objects.
 * {@review}
 *
 *
 * <h5 class='topic'>Test Methods</h5>
 * 	<ul>
 * 		<li class='jm'>{@link FluentObjectAssertion#exists()}
 * 		<li class='jm'>{@link FluentObjectAssertion#is(Object)}
 * 		<li class='jm'>{@link FluentObjectAssertion#is(Predicate)}
 * 		<li class='jm'>{@link FluentObjectAssertion#isNot(Object)}
 * 		<li class='jm'>{@link FluentObjectAssertion#isAny(Object...)}
 * 		<li class='jm'>{@link FluentObjectAssertion#isNotAny(Object...)}
 * 		<li class='jm'>{@link FluentObjectAssertion#isNull()}
 * 		<li class='jm'>{@link FluentObjectAssertion#isNotNull()}
 * 		<li class='jm'>{@link FluentObjectAssertion#isString(String)}
 * 		<li class='jm'>{@link FluentObjectAssertion#isJson(String)}
 * 		<li class='jm'>{@link FluentObjectAssertion#isSame(Object)}
 * 		<li class='jm'>{@link FluentObjectAssertion#isSameJsonAs(Object)}
 * 		<li class='jm'>{@link FluentObjectAssertion#isSameSortedJsonAs(Object)}
 * 		<li class='jm'>{@link FluentObjectAssertion#isSameSerializedAs(Object, WriterSerializer)}
 * 		<li class='jm'>{@link FluentObjectAssertion#isType(Class)}
 * 		<li class='jm'>{@link FluentObjectAssertion#isExactType(Class)}
 * 	</ul>
 *
 * <h5 class='topic'>Transform Methods</h5>
 * 	<ul>
 * 		<li class='jm'>{@link FluentRequestLineAssertion#method()}
 * 		<li class='jm'>{@link FluentRequestLineAssertion#uri()}
 * 		<li class='jm'>{@link FluentRequestLineAssertion#protocolVersion()}
 * 		<li class='jm'>{@link FluentObjectAssertion#asString()}
 * 		<li class='jm'>{@link FluentObjectAssertion#asString(WriterSerializer)}
 * 		<li class='jm'>{@link FluentObjectAssertion#asString(Function)}
 * 		<li class='jm'>{@link FluentObjectAssertion#asJson()}
 * 		<li class='jm'>{@link FluentObjectAssertion#asJsonSorted()}
 * 		<li class='jm'>{@link FluentObjectAssertion#apply(Function)}
 * 		<li class='jm'>{@link FluentObjectAssertion#asAny()}
 *	</ul>
 *
 * <h5 class='topic'>Configuration Methods</h5>
 * 	<ul>
 * 		<li class='jm'>{@link Assertion#msg(String, Object...)}
 * 		<li class='jm'>{@link Assertion#out(PrintStream)}
 * 		<li class='jm'>{@link Assertion#silent()}
 * 		<li class='jm'>{@link Assertion#stdout()}
 * 		<li class='jm'>{@link Assertion#throwable(Class)}
 * 	</ul>
 *
 * <ul class='seealso'>
 * 	<li class='link'>{@doc FluentAssertions}
 * </ul>
 *
 * @param <R> The return type.
 */
@FluentSetters(returns="FluentRequestLineAssertion<R>")
public class FluentRequestLineAssertion<R> extends FluentObjectAssertion<RequestLine,R> {

	//-----------------------------------------------------------------------------------------------------------------
	// Constructors
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param value
	 * 	The object being tested.
	 * 	<br>Can be <jk>null</jk>.
	 * @param returns
	 * 	The object to return after a test method is called.
	 * 	<br>If <jk>null</jk>, the test method returns this object allowing multiple test method calls to be
	 * used on the same assertion.
	 */
	public FluentRequestLineAssertion(RequestLine value, R returns) {
		this(null, value, returns);
	}

	/**
	 * Chained constructor.
	 *
	 * <p>
	 * Used when transforming one assertion into another so that the assertion config can be used by the new assertion.
	 *
	 * @param creator
	 * 	The assertion that created this assertion.
	 * 	<br>Should be <jk>null</jk> if this is the top-level assertion.
	 * @param value
	 * 	The object being tested.
	 * 	<br>Can be <jk>null</jk>.
	 * @param returns
	 * 	The object to return after a test method is called.
	 * 	<br>If <jk>null</jk>, the test method returns this object allowing multiple test method calls to be
	 * used on the same assertion.
	 */
	public FluentRequestLineAssertion(Assertion creator, RequestLine value, R returns) {
		super(creator, value, returns);
		throwable(BadRequest.class);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Transform methods
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the request line method string as a new assertion.
	 *
	 * @return A new assertion.
	 */
	public FluentStringAssertion<R> method() {
		return new FluentStringAssertion<>(value().getMethod(), returns());
	}

	/**
	 * Returns the request line uri string as a new assertion.
	 *
	 * @return A new assertion.
	 */
	public FluentStringAssertion<R> uri() {
		return new FluentStringAssertion<>(value().getUri(), returns());
	}

	/**
	 * Returns the request line protocol version as a new assertion.
	 *
	 * @return A new assertion.
	 */
	public FluentProtocolVersionAssertion<R> protocolVersion() {
		return new FluentProtocolVersionAssertion<>(value().getProtocolVersion(), returns());
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Fluent setters
	//-----------------------------------------------------------------------------------------------------------------

	// <FluentSetters>

	@Override /* GENERATED - Assertion */
	public FluentRequestLineAssertion<R> msg(String msg, Object...args) {
		super.msg(msg, args);
		return this;
	}

	@Override /* GENERATED - Assertion */
	public FluentRequestLineAssertion<R> out(PrintStream value) {
		super.out(value);
		return this;
	}

	@Override /* GENERATED - Assertion */
	public FluentRequestLineAssertion<R> silent() {
		super.silent();
		return this;
	}

	@Override /* GENERATED - Assertion */
	public FluentRequestLineAssertion<R> stdout() {
		super.stdout();
		return this;
	}

	@Override /* GENERATED - Assertion */
	public FluentRequestLineAssertion<R> throwable(Class<? extends java.lang.RuntimeException> value) {
		super.throwable(value);
		return this;
	}

	// </FluentSetters>
}
