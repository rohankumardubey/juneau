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
package org.apache.juneau.html;

import org.apache.juneau.annotation.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.jsonschema.*;
import org.apache.juneau.serializer.*;

/**
 * Serializes POJO metamodels to HTML.
 *
 * <h5 class='topic'>Media types</h5>
 *
 * Handles <c>Accept</c> types:  <bc>text/html+schema</bc>
 * <p>
 * Produces <c>Content-Type</c> types:  <bc>text/html</bc>
 *
 * <h5 class='topic'>Description</h5>
 *
 * Essentially the same as {@link HtmlSerializer}, except serializes the POJO metamodel instead of the model itself.
 *
 * <p>
 * Produces output that describes the POJO metamodel similar to an XML schema document.
 *
 * <p>
 * The easiest way to create instances of this class is through the {@link HtmlSerializer#getSchemaSerializer()},
 * which will create a schema serializer with the same settings as the originating serializer.
 */
@ConfigurableContext
public class HtmlSchemaSerializer extends HtmlSerializer {

	//-------------------------------------------------------------------------------------------------------------------
	// Configurable properties
	//-------------------------------------------------------------------------------------------------------------------

	static final String PREFIX = "HtmlSchemaSerializer";

	//-------------------------------------------------------------------------------------------------------------------
	// Predefined instances
	//-------------------------------------------------------------------------------------------------------------------

	/** Default serializer, all default settings.*/
	public static final HtmlSchemaSerializer DEFAULT = new HtmlSchemaSerializer(create());

	/** Default serializer, all default settings.*/
	public static final HtmlSchemaSerializer DEFAULT_READABLE = new Readable(create());

	/** Default serializer, single quotes, simple mode. */
	public static final HtmlSchemaSerializer DEFAULT_SIMPLE = new Simple(create());

	/** Default serializer, single quotes, simple mode, with whitespace. */
	public static final HtmlSchemaSerializer DEFAULT_SIMPLE_READABLE = new SimpleReadable(create());

	//-------------------------------------------------------------------------------------------------------------------
	// Predefined subclasses
	//-------------------------------------------------------------------------------------------------------------------

	/** Default serializer, with whitespace. */
	public static class Readable extends HtmlSchemaSerializer {

		/**
		 * Constructor.
		 *
		 * @param builder The builder for this object.
		 */
		protected Readable(HtmlSchemaSerializerBuilder builder) {
			super(builder.useWhitespace());
		}
	}

	/** Default serializer, single quotes, simple mode. */
	public static class Simple extends HtmlSchemaSerializer {

		/**
		 * Constructor.
		 *
		 * @param builder The builder for this object.
		 */
		protected Simple(HtmlSchemaSerializerBuilder builder) {
			super(builder.quoteChar('\''));
		}
	}

	/** Default serializer, single quotes, simple mode, with whitespace. */
	public static class SimpleReadable extends HtmlSchemaSerializer {

		/**
		 * Constructor.
		 *
		 * @param builder The builder for this object.
		 */
		protected SimpleReadable(HtmlSchemaSerializerBuilder builder) {
			super(builder.quoteChar('\'').useWhitespace());
		}
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Instance
	//-------------------------------------------------------------------------------------------------------------------

	private final JsonSchemaGenerator generator;

	/**
	 * Constructor.
	 *
	 * @param builder The builder for this serializer.
	 */
	protected HtmlSchemaSerializer(HtmlSchemaSerializerBuilder builder) {
		super(builder.detectRecursions().ignoreRecursions());

		generator = JsonSchemaGenerator.create().apply(getContextProperties()).build();
	}

	/**
	 * Instantiates a new clean-slate {@link HtmlSchemaSerializerBuilder} object.
	 *
	 * <p>
	 * This is equivalent to simply calling <code><jk>new</jk> HtmlSchemaSerializerBuilder()</code>.
	 *
	 * <p>
	 * Note that this method creates a builder initialized to all default settings, whereas {@link #copy()} copies
	 * the settings of the object called on.
	 *
	 * @return A new {@link HtmlSerializerBuilder} object.
	 */
	public static HtmlSchemaSerializerBuilder create() {
		return new HtmlSchemaSerializerBuilder();
	}

	@Override /* Context */
	public HtmlSchemaSerializerBuilder copy() {
		return new HtmlSchemaSerializerBuilder(this);
	}

	@Override /* Context */
	public HtmlSchemaSerializerSession createSession() {
		return createSession(createDefaultSessionArgs());
	}

	@Override /* Serializer */
	public HtmlSchemaSerializerSession createSession(SerializerSessionArgs args) {
		return new HtmlSchemaSerializerSession(this, args);
	}

	JsonSchemaGenerator getGenerator() {
		return generator;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Other methods
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* Context */
	public OMap toMap() {
		return super.toMap()
			.a(
				"HtmlSchemaSerializer",
				OMap
					.create()
					.filtered()
					.a("generator", generator)
			);
	}
}
