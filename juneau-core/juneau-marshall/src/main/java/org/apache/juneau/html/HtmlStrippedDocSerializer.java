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
import org.apache.juneau.serializer.*;

/**
 * Serializes POJOs to HTTP responses as stripped HTML.
 *
 * <h5 class='topic'>Media types</h5>
 *
 * Handles <c>Accept</c> types:  <bc>text/html+stripped</bc>
 * <p>
 * Produces <c>Content-Type</c> types:  <bc>text/html</bc>
 *
 * <h5 class='topic'>Description</h5>
 *
 * Produces the same output as {@link HtmlDocSerializer}, but without the header and body tags and page title and
 * description.
 * Used primarily for JUnit testing the {@link HtmlDocSerializer} class.
 */
@ConfigurableContext
public class HtmlStrippedDocSerializer extends HtmlSerializer {

	//-------------------------------------------------------------------------------------------------------------------
	// Predefined instances
	//-------------------------------------------------------------------------------------------------------------------

	/** Default serializer, all default settings. */
	public static final HtmlStrippedDocSerializer DEFAULT = new HtmlStrippedDocSerializer(create());

	//-------------------------------------------------------------------------------------------------------------------
	// Configurable properties
	//-------------------------------------------------------------------------------------------------------------------

	static final String PREFIX = "HtmlStrippedDocSerializer";

	/**
	 * Constructor.
	 *
	 * @param builder The builder for this object.
	 */
	protected HtmlStrippedDocSerializer(HtmlStrippedDocSerializerBuilder builder) {
		super(builder);
	}

	@Override /* Context */
	public HtmlStrippedDocSerializerBuilder copy() {
		return new HtmlStrippedDocSerializerBuilder(this);
	}

	/**
	 * Instantiates a new clean-slate {@link HtmlStrippedDocSerializerBuilder} object.
	 *
	 * <p>
	 * This is equivalent to simply calling <code><jk>new</jk> HtmlStrippedDocSerializerBuilder()</code>.
	 *
	 * <p>
	 * Note that this method creates a builder initialized to all default settings, whereas {@link #copy()} copies
	 * the settings of the object called on.
	 *
	 * @return A new {@link HtmlStrippedDocSerializerBuilder} object.
	 */
	public static HtmlStrippedDocSerializerBuilder create() {
		return new HtmlStrippedDocSerializerBuilder();
	}

	@Override /* Serializer */
	public HtmlSerializerSession createSession() {
		return createSession(createDefaultSessionArgs());
	}

	@Override /* Serializer */
	public HtmlSerializerSession createSession(SerializerSessionArgs args) {
		return new HtmlStrippedDocSerializerSession(this, args);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Other methods
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* Context */
	public OMap toMap() {
		return super.toMap()
			.a(
				"HtmlStrippedDocSerializer",
				OMap
					.create()
					.filtered()
			);
	}
}
