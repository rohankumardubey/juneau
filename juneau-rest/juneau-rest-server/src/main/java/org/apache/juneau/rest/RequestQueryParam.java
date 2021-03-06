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
package org.apache.juneau.rest;

import static org.apache.juneau.httppart.HttpPartType.*;
import static org.apache.juneau.internal.ClassUtils.*;
import static org.apache.juneau.internal.ExceptionUtils.*;

import java.time.*;
import java.util.*;

import org.apache.http.*;
import org.apache.juneau.*;
import org.apache.juneau.assertions.*;
import org.apache.juneau.http.part.*;
import org.apache.juneau.httppart.*;
import org.apache.juneau.reflect.*;

/**
 * Represents a single query parameter on an HTTP request.
 */
public class RequestQueryParam extends RequestHttpPart implements NameValuePair {

	private final String value;

	/**
	 * Constructor.
	 *
	 * @param request The request object.
	 * @param name The parameter name.
	 * @param value The parameter value.
	 */
	public RequestQueryParam(RestRequest request, String name, String value) {
		super(QUERY, request, name);
		this.value = value;
	}

	//------------------------------------------------------------------------------------------------------------------
	// Retrievers
	//------------------------------------------------------------------------------------------------------------------

	@Override /* RequestHttpPart */
	public String getValue() {
		return value;
	}

	/**
	 * Returns the value of this parameter as an integer.
	 *
	 * @return The value of this parameter as an integer, or {@link Optional#empty()} if the parameter was not present.
	 */
	public Optional<Integer> asInteger() {
		return asIntegerPart().asInteger();
	}

	/**
	 * Returns the value of this parameter as a boolean.
	 *
	 * @return The value of this parameter as a boolean, or {@link Optional#empty()} if the parameter was not present.
	 */
	public Optional<Boolean> asBoolean() {
		return asBooleanPart().asBoolean();
	}

	/**
	 * Returns the value of this parameter as a long.
	 *
	 * @return The value of this parameter as a long, or {@link Optional#empty()} if the parameter was not present.
	 */
	public Optional<Long> asLong() {
		return asLongPart().asLong();
	}

	/**
	 * Returns the value of this parameter as a date.
	 *
	 * @return The value of this parameter as a date, or {@link Optional#empty()} if the parameter was not present.
	 */
	public Optional<ZonedDateTime> asDate() {
		return asDatePart().asZonedDateTime();
	}

	/**
	 * Returns the value of this parameter as a list from a comma-delimited string.
	 *
	 * @return The value of this parameter as a list from a comma-delimited string, or {@link Optional#empty()} if the parameter was not present.
	 */
	public Optional<List<String>> asCsvArray() {
		return asCsvArrayPart().asList();
	}

	/**
	 * Returns the value of this parameter as a {@link BasicPart}.
	 *
	 * @param c The subclass of {@link BasicPart} to instantiate.
	 * @param <T> The subclass of {@link BasicPart} to instantiate.
	 * @return The value of this parameter as a string, never <jk>null</jk>.
	 */
	public <T extends BasicPart> T asPart(Class<T> c) {
		try {
			ClassInfo ci = ClassInfo.of(c);
			ConstructorInfo cc = ci.getConstructor(Visibility.PUBLIC, String.class);
			if (cc != null)
				return cc.invoke(orElse(null));
			cc = ci.getConstructor(Visibility.PUBLIC, String.class, String.class);
			if (cc != null)
				return cc.invoke(getName(), orElse(null));
		} catch (Exception e) {
			throw runtimeException(e);
		}
		throw runtimeException("Could not determine a method to construct type {0}", className(c));
	}

	/**
	 * Returns the value of this parameter as a {@link BasicCsvArrayPart}.
	 *
	 * @return The value of this parameter as a {@link BasicCsvArrayPart}, never <jk>null</jk>.
	 */
	public BasicCsvArrayPart asCsvArrayPart() {
		return new BasicCsvArrayPart(getName(), getValue());
	}

	/**
	 * Returns the value of this parameter as a {@link BasicDatePart}.
	 *
	 * @return The value of this parameter as a {@link BasicDatePart}, never <jk>null</jk>.
	 */
	public BasicDatePart asDatePart() {
		return new BasicDatePart(getName(), getValue());
	}

	/**
	 * Returns the value of this parameter as a {@link BasicIntegerPart}.
	 *
	 * @return The value of this parameter as a {@link BasicIntegerPart}, never <jk>null</jk>.
	 */
	public BasicIntegerPart asIntegerPart() {
		return new BasicIntegerPart(getName(), getValue());
	}

	/**
	 * Returns the value of this parameter as a {@link BasicBooleanPart}.
	 *
	 * @return The value of this parameter as a {@link BasicBooleanPart}, never <jk>null</jk>.
	 */
	public BasicBooleanPart asBooleanPart() {
		return new BasicBooleanPart(getName(), getValue());
	}

	/**
	 * Returns the value of this parameter as a {@link BasicLongPart}.
	 *
	 * @return The value of this parameter as a {@link BasicLongPart}, never <jk>null</jk>.
	 */
	public BasicLongPart asLongPart() {
		return new BasicLongPart(getName(), getValue());
	}

	/**
	 * Returns the value of this parameter as a {@link BasicStringPart}.
	 *
	 * @return The value of this parameter as a {@link BasicStringPart}, never <jk>null</jk>.
	 */
	public BasicStringPart asStringPart() {
		return new BasicStringPart(getName(), getValue());
	}

	/**
	 * Returns the value of this parameter as a {@link BasicUriPart}.
	 *
	 * @return The value of this parameter as a {@link BasicUriPart}, never <jk>null</jk>.
	 */
	public BasicUriPart asUriPart() {
		return new BasicUriPart(getName(), getValue());
	}

	//------------------------------------------------------------------------------------------------------------------
	// Assertions
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Provides the ability to perform fluent-style assertions on this parameter.
	 *
	 * <h5 class='section'>Examples:</h5>
	 * <p class='bcode w800'>
	 * 	<jv>request</jv>
	 * 		.getQueryParam(<js>"foo"</js>)
	 * 		.assertString().contains(<js>"bar"</js>);
	 * </p>
	 *
	 * <p>
	 * The assertion test returns the original object allowing you to chain multiple requests like so:
	 * <p class='bcode w800'>
	 * 	String <jv>foo</jv> = <jv>request</jv>
	 * 		.getQueryParam(<js>"foo"</js>)
	 * 		.assertString().contains(<js>"bar"</js>)
	 * 		.asString().get();
	 * </p>
	 *
	 * @return A new fluent assertion object.
	 */
	public FluentStringAssertion<RequestQueryParam> assertString() {
		return new FluentStringAssertion<>(orElse(null), this);
	}

	/**
	 * Provides the ability to perform fluent-style assertions on an integer parameter.
	 *
	 * <h5 class='section'>Examples:</h5>
	 * <p class='bcode w800'>
	 * 	<jv>request</jv>
	 * 		.getQueryParam(<js>"age"</js>)
	 * 		.assertInteger().isGreaterThan(1);
	 * </p>
	 *
	 * @return A new fluent assertion object.
	 */
	public FluentIntegerAssertion<RequestQueryParam> assertInteger() {
		return new FluentIntegerAssertion<>(asIntegerPart().asInteger().orElse(null), this);
	}

	/**
	 * Provides the ability to perform fluent-style assertions on a long parameter.
	 *
	 * <h5 class='section'>Examples:</h5>
	 * <p class='bcode w800'>
	 * 	<jv>request</jv>
	 * 		.getQueryParam(<js>"length"</js>)
	 * 		.assertLong().isLessThan(100000);
	 * </p>
	 *
	 * @return A new fluent assertion object.
	 */
	public FluentLongAssertion<RequestQueryParam> assertLong() {
		return new FluentLongAssertion<>(asLongPart().asLong().orElse(null), this);
	}

	/**
	 * Provides the ability to perform fluent-style assertions on a date parameter.
	 *
	 * <h5 class='section'>Examples:</h5>
	 * <p class='bcode w800'>
	 * 	<jv>request</jv>
	 * 		.getQueryParam(<js>"time"</js>)
	 * 		.assertDate().isAfterNow();
	 * </p>
	 *
	 * @return A new fluent assertion object.
	 */
	public FluentZonedDateTimeAssertion<RequestQueryParam> assertDate() {
		return new FluentZonedDateTimeAssertion<>(asDatePart().asZonedDateTime().orElse(null), this);
	}

	/**
	 * Provides the ability to perform fluent-style assertions on comma-separated string parameters.
	 *
	 * <h5 class='section'>Examples:</h5>
	 * <p class='bcode w800'>
	 * 	<jv>request</jv>
	 * 		.getQueryParam(<js>"allow"</js>)
	 * 		.assertCsvArray().contains(<js>"GET"</js>);
	 * </p>
	 *
	 * @return A new fluent assertion object.
	 */
	public FluentListAssertion<String,RequestQueryParam> assertCsvArray() {
		return new FluentListAssertion<>(asCsvArrayPart().asList().orElse(null), this);
	}

	//------------------------------------------------------------------------------------------------------------------
	// Header passthrough methods.
	//------------------------------------------------------------------------------------------------------------------

	@Override /* Object */
	public String toString() {
		return getName() + "=" + getValue();
	}

	// <FluentSetters>

	@Override /* GENERATED */
	public RequestQueryParam schema(HttpPartSchema value) {
		super.schema(value);
		return this;
	}

	@Override /* GENERATED */
	public RequestQueryParam parser(HttpPartParserSession value) {
		super.parser(value);
		return this;
	}
	// </FluentSetters>
}
