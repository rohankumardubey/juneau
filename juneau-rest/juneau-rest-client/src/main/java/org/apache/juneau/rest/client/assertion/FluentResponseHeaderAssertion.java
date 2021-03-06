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
package org.apache.juneau.rest.client.assertion;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

import org.apache.juneau.assertions.*;
import org.apache.juneau.http.response.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.rest.client.*;
import org.apache.juneau.serializer.*;

/**
 * Used for fluent assertion calls against {@link ResponseHeader} objects.
 * {@review}
 *
 *
 * <h5 class='topic'>Test Methods</h5>
 * 	<ul>
 * 		<li class='jm'>{@link FluentStringAssertion#is(String)}
 * 		<li class='jm'>{@link FluentStringAssertion#isNot(String)}
 * 		<li class='jm'>{@link FluentStringAssertion#isLines(String...)}
 * 		<li class='jm'>{@link FluentStringAssertion#isSortedLines(String...)}
 * 		<li class='jm'>{@link FluentStringAssertion#isIc(String)}
 * 		<li class='jm'>{@link FluentStringAssertion#isNotIc(String)}
 * 		<li class='jm'>{@link FluentStringAssertion#contains(String...)}
 * 		<li class='jm'>{@link FluentStringAssertion#doesNotContain(String...)}
 * 		<li class='jm'>{@link FluentStringAssertion#isEmpty()}
 * 		<li class='jm'>{@link FluentStringAssertion#isNotEmpty()}
 * 		<li class='jm'>{@link FluentStringAssertion#matches(String)}
 * 		<li class='jm'>{@link FluentStringAssertion#regex(String)}
 * 		<li class='jm'>{@link FluentStringAssertion#regex(String,int)}
 * 		<li class='jm'>{@link FluentStringAssertion#regex(Pattern)}
 * 		<li class='jm'>{@link FluentStringAssertion#startsWith(String)}
 * 		<li class='jm'>{@link FluentStringAssertion#endsWith(String)}
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
 * 		<li class='jm'>{@link FluentResponseHeaderAssertion#asBoolean()}
 * 		<li class='jm'>{@link FluentResponseHeaderAssertion#asInteger()}
 * 		<li class='jm'>{@link FluentResponseHeaderAssertion#asLong()}
 * 		<li class='jm'>{@link FluentResponseHeaderAssertion#asZonedDateTime()}
 * 		<li class='jm'>{@link FluentResponseHeaderAssertion#asType(Class)}
 * 		<li class='jm'>{@link FluentResponseHeaderAssertion#asType(Type,Type...)}
 * 		<li class='jm'>{@link FluentStringAssertion#replaceAll(String,String)}
 * 		<li class='jm'>{@link FluentStringAssertion#replace(String,String)}
 * 		<li class='jm'>{@link FluentStringAssertion#urlDecode()}
 * 		<li class='jm'>{@link FluentStringAssertion#lc()}
 * 		<li class='jm'>{@link FluentStringAssertion#uc()}
 * 		<li class='jm'>{@link FluentStringAssertion#lines()}
 * 		<li class='jm'>{@link FluentStringAssertion#split(String)}
 * 		<li class='jm'>{@link FluentStringAssertion#length()}
 * 		<li class='jm'>{@link FluentStringAssertion#oneLine()}
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
@FluentSetters(returns="FluentResponseHeaderAssertion<R>")
public class FluentResponseHeaderAssertion<R> extends FluentStringAssertion<R> {

	private final ResponseHeader value;

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
	public FluentResponseHeaderAssertion(ResponseHeader value, R returns) {
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
	public FluentResponseHeaderAssertion(Assertion creator, ResponseHeader value, R returns) {
		super(creator, value.asString().orElse(null), returns);
		this.value = value;
		throwable(BadRequest.class);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Transform methods
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Converts this object assertion into a boolean assertion.
	 *
	 * @return A new assertion.
	 * @throws AssertionError If object is not a boolean.
	 */
	public FluentBooleanAssertion<R> asBoolean() {
		return new FluentBooleanAssertion<>(this, value.asBoolean().orElse(null), returns());
	}

	/**
	 * Converts this object assertion into an integer assertion.
	 *
	 * @return A new assertion.
	 * @throws AssertionError If object is not an integer.
	 */
	public FluentIntegerAssertion<R> asInteger() {
		return new FluentIntegerAssertion<>(this, value.asInteger().orElse(null), returns());
	}

	/**
	 * Converts this object assertion into a long assertion.
	 *
	 * @return A new assertion.
	 * @throws AssertionError If object is not a long.
	 */
	public FluentLongAssertion<R> asLong() {
		return new FluentLongAssertion<>(this, value.asLong().orElse(null), returns());
	}

	/**
	 * Converts this object assertion into a zoned-datetime assertion.
	 *
	 * @return A new assertion.
	 * @throws AssertionError If object is not a zoned-datetime.
	 */
	public FluentZonedDateTimeAssertion<R> asZonedDateTime() {
		return new FluentZonedDateTimeAssertion<>(this, value.asDateHeader().asZonedDateTime().orElse(null), returns());
	}

	/**
	 * Converts the parameter value to a type using {@link ResponseHeader#asType(Class)} and then returns the value as an any-object assertion.
	 *
	 * @param type The object type to create.
	 * @return A new fluent assertion object.
	 * @throws RestCallException If value could not be parsed.
	 */
	public <V> FluentAnyAssertion<V,R> asType(Class<V> type) throws RestCallException {
		return new FluentAnyAssertion<>(value.asType(type).orElse(null), returns());
	}

	/**
	 * Converts the parameter value to a type using {@link ResponseHeader#asType(Type,Type...)} and then returns the value as an any-object assertion.
	 *
	 * <p>
	 * See {@doc Generics Generics} for information on defining complex generic types of {@link Map Maps} and {@link Collection Collections}.
	 *
	 * @param type The object type to create.
	 * @param args Optional type arguments.
	 * @return A new fluent assertion object.
	 * @throws RestCallException If value could not be parsed.
	 */
	public FluentAnyAssertion<Object,R> asType(Type type, Type...args) throws RestCallException {
		return new FluentAnyAssertion<>(value.asType(type, args).orElse(null), returns());
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Fluent setters
	//-----------------------------------------------------------------------------------------------------------------

	// <FluentSetters>

	@Override /* GENERATED - Assertion */
	public FluentResponseHeaderAssertion<R> msg(String msg, Object...args) {
		super.msg(msg, args);
		return this;
	}

	@Override /* GENERATED - Assertion */
	public FluentResponseHeaderAssertion<R> out(PrintStream value) {
		super.out(value);
		return this;
	}

	@Override /* GENERATED - Assertion */
	public FluentResponseHeaderAssertion<R> silent() {
		super.silent();
		return this;
	}

	@Override /* GENERATED - Assertion */
	public FluentResponseHeaderAssertion<R> stdout() {
		super.stdout();
		return this;
	}

	@Override /* GENERATED - Assertion */
	public FluentResponseHeaderAssertion<R> throwable(Class<? extends java.lang.RuntimeException> value) {
		super.throwable(value);
		return this;
	}

	// </FluentSetters>
}
