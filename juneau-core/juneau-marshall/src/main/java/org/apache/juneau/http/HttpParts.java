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
package org.apache.juneau.http;

import static org.apache.juneau.internal.ExceptionUtils.*;
import static org.apache.juneau.internal.StringUtils.*;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.apache.http.*;
import org.apache.juneau.http.header.*;
import org.apache.juneau.http.part.*;
import org.apache.juneau.reflect.*;

/**
 * Standard predefined HTTP headers.
 */
public class HttpParts {

	/**
	 * Creates a new {@link BasicBooleanPart} part.
	 *
	 * @param name The part name.
	 * @param value
	 * 	The part value.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li>{@link Boolean} - As-is.
	 * 		<li>{@link String} - Parsed using {@link Boolean#parseBoolean(String)}.
	 * 		<li>Anything else - Converted to <c>String</c> and then parsed.
	 * 	</ul>
	 * @return A new {@link BasicBooleanPart} object, or <jk>null</jk> if the name or value is <jk>null</jk>.
	 */
	public static final BasicBooleanPart booleanPart(String name, Object value) {
		return BasicBooleanPart.of(name, value);
	}

	/**
	 * Creates a new {@link BasicBooleanPart} part with a delayed value.
	 *
	 * <p>
	 * Part value is re-evaluated on each call to {@link NameValuePair#getValue()}.
	 *
	 * @param name The part name.
	 * @param value
	 * 	The part value supplier.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li>{@link Boolean} - As-is.
	 * 		<li>{@link String} - Parsed using {@link Boolean#parseBoolean(String)}.
	 * 		<li>Anything else - Converted to <c>String</c> and then parsed.
	 * 	</ul>
	 * @return A new {@link BasicBooleanPart} object, or <jk>null</jk> if the name or value is <jk>null</jk>.
	 */
	public static final BasicBooleanPart booleanPart(String name, Supplier<?> value) {
		return BasicBooleanPart.of(name, value);
	}

	/**
	 * Creates a new {@link BasicCsvArrayPart} part.
	 *
	 * @param name The part name.
	 * @param value
	 * 	The part value.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li><c>String</c> - A comma-delimited string.
	 * 		<li><c>String[]</c> - A pre-parsed value.
	 * 		<li>Any other array type - Converted to <c>String[]</c>.
	 * 		<li>Any {@link Collection} - Converted to <c>String[]</c>.
	 * 		<li>Anything else - Converted to <c>String</c>.
	 * 	</ul>
	 * @return A new {@link BasicCsvArrayPart} object, or <jk>null</jk> if the name or value is <jk>null</jk>.
	 */
	public static final BasicCsvArrayPart csvArrayPart(String name, Object value) {
		return BasicCsvArrayPart.of(name, value);
	}

	/**
	 * Creates a new {@link BasicCsvArrayPart} part with a delayed value.
	 *
	 * <p>
	 * Part value is re-evaluated on each call to {@link NameValuePair#getValue()}.
	 *
	 * @param name The part name.
	 * @param value
	 * 	The part value supplier.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li><c>String</c> - A comma-delimited string.
	 * 		<li><c>String[]</c> - A pre-parsed value.
	 * 		<li>Any other array type - Converted to <c>String[]</c>.
	 * 		<li>Any {@link Collection} - Converted to <c>String[]</c>.
	 * 		<li>Anything else - Converted to <c>String</c>.
	 * 	</ul>
	 * @return A new {@link BasicCsvArrayPart} object, or <jk>null</jk> if the name or value is <jk>null</jk>.
	 */
	public static final BasicCsvArrayPart basicCsvArrayPart(String name, Supplier<?> value) {
		return BasicCsvArrayPart.of(name, value);
	}

	/**
	 * Creates a new {@link BasicDatePart} part.
	 *
	 * @param name The part name.
	 * @param value
	 * 	The part value.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li><c>String</c> - An ISO-8601 formated string (e.g. <js>"1994-10-29T19:43:31Z"</js>).
	 * 		<li>{@link ZonedDateTime}
	 * 		<li>{@link Calendar}
	 * 		<li>Anything else - Converted to <c>String</c>.
	 * 	</ul>
	 * @return A new {@link BasicDatePart} object, or <jk>null</jk> if the name or value is <jk>null</jk>.
	 */
	public static final BasicDatePart datePart(String name, Object value) {
		return BasicDatePart.of(name, value);
	}

	/**
	 * Creates a new {@link BasicDatePart} part with a delayed value.
	 *
	 * <p>
	 * Part value is re-evaluated on each call to {@link NameValuePair#getValue()}.
	 *
	 * @param name The part name.
	 * @param value
	 * 	The part value supplier.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li><c>String</c> - An ISO-8601 formated string (e.g. <js>"1994-10-29T19:43:31Z"</js>).
	 * 		<li>{@link ZonedDateTime}
	 * 		<li>{@link Calendar}
	 * 		<li>Anything else - Converted to <c>String</c>.
	 * 	</ul>
	 * @return A new {@link BasicDatePart} object, or <jk>null</jk> if the name or value is <jk>null</jk>.
	 */
	public static final BasicDatePart datePart(String name, Supplier<?> value) {
		return BasicDatePart.of(name, value);
	}

	/**
	 * Creates a new {@link BasicIntegerPart} part.
	 *
	 * @param name The part name.
	 * @param value
	 * 	The part value.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li>{@link Number} - Converted to an integer using {@link Number#intValue()}.
	 * 		<li>{@link String} - Parsed using {@link Integer#parseInt(String)}.
	 * 		<li>Anything else - Converted to <c>String</c>.
	 * 	</ul>
	 * @return A new {@link BasicIntegerPart} object, or <jk>null</jk> if the name or value is <jk>null</jk>.
	 */
	public static final BasicIntegerPart integerPart(String name, Object value) {
		return BasicIntegerPart.of(name, value);
	}

	/**
	 * Creates a new {@link BasicIntegerPart} part with a delayed value.
	 *
	 * <p>
	 * Part value is re-evaluated on each call to {@link NameValuePair#getValue()}.
	 *
	 * @param name The part name.
	 * @param value
	 * 	The part value supplier.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li>{@link Number} - Converted to an integer using {@link Number#intValue()}.
	 * 		<li>{@link String} - Parsed using {@link Integer#parseInt(String)}.
	 * 		<li>Anything else - Converted to <c>String</c>.
	 * 	</ul>
	 * @return A new {@link BasicIntegerPart} object, or <jk>null</jk> if the name or value is <jk>null</jk>.
	 */
	public static final BasicIntegerPart integerPart(String name, Supplier<?> value) {
		return BasicIntegerPart.of(name, value);
	}

	/**
	 * Creates a new {@link BasicLongPart} part.
	 *
	 * @param name The part name.
	 * @param value
	 * 	The part value.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li>{@link Number} - Converted to a long using {@link Number#longValue()}.
	 * 		<li>{@link String} - Parsed using {@link Long#parseLong(String)}.
	 * 		<li>Anything else - Converted to <c>String</c>.
	 * 	</ul>
	 * @return A new {@link BasicLongPart} object, or <jk>null</jk> if the name or value is <jk>null</jk>.
	 */
	public static final BasicLongPart longPart(String name, Object value) {
		return BasicLongPart.of(name, value);
	}

	/**
	 * Creates a new {@link BasicLongPart} part with a delayed value.
	 *
	 * <p>
	 * Part value is re-evaluated on each call to {@link NameValuePair#getValue()}.
	 *
	 * @param name The part name.
	 * @param value
	 * 	The part value supplier.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li>{@link Number} - Converted to a long using {@link Number#longValue()}.
	 * 		<li>{@link String} - Parsed using {@link Long#parseLong(String)}.
	 * 		<li>Anything else - Converted to <c>String</c>.
	 * 	</ul>
	 * @return A new {@link BasicLongPart} object, or <jk>null</jk> if the name or value is <jk>null</jk>.
	 */
	public static final BasicLongPart longPart(String name, Supplier<?> value) {
		return BasicLongPart.of(name, value);
	}

	/**
	 * Creates a new {@link BasicUriPart} part.
	 *
	 * @param name The header name.
	 * @param value
	 * 	The header value.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li>{@link String}
	 * 		<li>Anything else - Converted to <c>String</c> then parsed.
	 * 	</ul>
	 * @return A new {@link BasicUriPart} object, or <jk>null</jk> if the name or value is <jk>null</jk>.
	 */
	public static final BasicUriPart uriPart(String name, Object value) {
		return BasicUriPart.of(name, value);
	}

	/**
	 * Creates a new {@link BasicUriPart} part with a delayed value.
	 *
	 * <p>
	 * Part value is re-evaluated on each call to {@link NameValuePair#getValue()}.
	 *
	 * @param name The header name.
	 * @param value
	 * 	The header value supplier.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li>{@link String}
	 * 		<li>Anything else - Converted to <c>String</c> then parsed.
	 * 	</ul>
	 * @return A new {@link BasicUriPart} object, or <jk>null</jk> if the name or value is <jk>null</jk>.
	 */
	public static final BasicUriPart uriPart(String name, Supplier<?> value) {
		return BasicUriPart.of(name, value);
	}

	/**
	 * Creates a {@link BasicPart} from a name/value pair string (e.g. <js>"Foo: bar"</js>)
	 *
	 * @param pair The pair string.
	 * @return A new {@link BasicPart} object.
	 */
	public static final BasicPart basicPart(String pair) {
		return BasicPart.ofPair(pair);
	}

	/**
	 * Creates a new {@link BasicPart} part.
	 *
	 * @param name The part name.
	 * @param value The part value.
	 * @return A new {@link BasicPart} object.
	 */
	public static final BasicPart basicPart(String name, Object value) {
		return BasicPart.of(name, value);
	}

	/**
	 * Creates a new {@link BasicPart} part with a delayed value.
	 *
	 * <p>
	 * Part value is re-evaluated on each call to {@link NameValuePair#getValue()}.
	 *
	 * @param name The part name.
	 * @param value The part value supplier.
	 * @return A new {@link BasicPart} object.
	 */
	public static final BasicPart basicPart(String name, Supplier<?> value) {
		return BasicPart.of(name, value);
	}

	/**
	 * Creates a new {@link BasicStringPart} part.
	 *
	 * @param name The part name.
	 * @param value
	 * 	The part value.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li>{@link String}
	 * 		<li>Anything else - Converted to <c>String</c> then parsed.
	 * 	</ul>
	 * @return A new {@link BasicStringPart} object, or <jk>null</jk> if the name or value is <jk>null</jk>.
	 */
	public static final BasicStringPart stringPart(String name, Object value) {
		return BasicStringPart.of(name, value);
	}

	/**
	 * Creates a new {@link BasicStringPart} part with a delayed value.
	 *
	 * <p>
	 * Part value is re-evaluated on each call to {@link NameValuePair#getValue()}.
	 *
	 * @param name The part name.
	 * @param value
	 * 	The part value supplier.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li>{@link String}
	 * 		<li>Anything else - Converted to <c>String</c> then parsed.
	 * 	</ul>
	 * @return A new {@link BasicStringPart} object, or <jk>null</jk> if the name or value is <jk>null</jk>.
	 */
	public static final BasicStringPart stringPart(String name, Supplier<?> value) {
		return BasicStringPart.of(name, value);
	}

	/**
	 * Creates a new {@link SerializedPart} part.
	 *
	 * @param name The part name.
	 * @param value
	 * 	The part value.
	 * 	<br>Can be any POJO.
	 * @return A new {@link SerializedPart} object, never <jk>null</jk>.
	 */
	public static final SerializedPart serializedPart(String name, Object value) {
		return SerializedPart.of(name, value);
	}

	/**
	 * Creates a new {@link SerializedPart} part with a delayed value.
	 *
	 * @param name The part name.
	 * @param value
	 * 	The part value supplier.
	 * 	<br>Can be a supplier of any POJO.
	 * @return A new {@link SerializedPart} object, never <jk>null</jk>.
	 */
	public static final SerializedPart serializedPart(String name, Supplier<?> value) {
		return SerializedPart.of(name, value);
	}

	/**
	 * Instantiates a new {@link org.apache.juneau.http.part.PartList.Builder}.
	 *
	 * @return A new empty builder.
	 */
	public static final PartList.Builder partListBuilder() {
		return PartList.create();
	}

	/**
	 * Creates a new {@link PartList} initialized with the specified parts.
	 *
	 * @param parts The parts to add to the list.  Can be <jk>null</jk>.  <jk>null</jk> entries are ignored.
	 * @return A new unmodifiable instance, never <jk>null</jk>.
	 */
	public static final PartList partList(List<NameValuePair> parts) {
		return PartList.of(parts);
	}

	/**
	 * Creates a new {@link PartList} initialized with the specified parts.
	 *
	 * @param parts The parts to add to the list.  <jk>null</jk> entries are ignored.
	 * @return A new unmodifiable instance, never <jk>null</jk>.
	 */
	public static final PartList partList(NameValuePair...parts) {
		return PartList.of(parts);
	}

	/**
	 * Creates a new {@link PartList} initialized with the specified name/value pairs.
	 *
	 * @param pairs
	 * 	Initial list of pairs.
	 * 	<br>Must be an even number of parameters representing key/value pairs.
	 * @throws RuntimeException If odd number of parameters were specified.
	 * @return A new instance.
	 */
	public static PartList partList(Object...pairs) {
		return PartList.ofPairs(pairs);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Utility methods
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Utility method for converting an arbitrary object to a {@link NameValuePair}.
	 *
	 * @param o
	 * 	The object to cast or convert to a {@link NameValuePair}.
	 * @return Either the same object cast as a {@link NameValuePair} or converted to a {@link NameValuePair}.
	 */
	@SuppressWarnings("rawtypes")
	public static NameValuePair cast(Object o) {
		if (o instanceof NameValuePair)
			return (NameValuePair)o;
		if (o instanceof Headerable) {
			Header x = ((Headerable)o).asHeader();
			return BasicPart.of(x.getName(), x.getValue());
		}
		if (o instanceof Map.Entry) {
			Map.Entry e = (Map.Entry)o;
			return BasicPart.of(stringify(e.getKey()), e.getValue());
		}
		throw runtimeException("Object of type {0} could not be converted to a Part.", o == null ? null : o.getClass().getName());
	}

	/**
	 * Returns <jk>true</jk> if the {@link #cast(Object)} method can be used on the specified object.
	 *
	 * @param o The object to check.
	 * @return <jk>true</jk> if the {@link #cast(Object)} method can be used on the specified object.
	 */
	public static boolean canCast(Object o) {
		ClassInfo ci = ClassInfo.of(o);
		return ci != null && ci.isChildOfAny(Headerable.class, NameValuePair.class, NameValuePairable.class, Map.Entry.class);
	}
}
