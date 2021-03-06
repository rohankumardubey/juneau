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
package org.apache.juneau.httppart;

import static org.apache.juneau.internal.ExceptionUtils.*;

import org.apache.juneau.*;
import org.apache.juneau.parser.*;

/**
 * An implementation of {@link HttpPartParser} that takes in the strings and tries to convert them to POJOs using constructors and static create methods.
 *
 * <p>
 * The class being created must be one of the following in order to convert it from a string:
 *
 * <ul>
 * 	<li>
 * 		An <jk>enum</jk>.
 * 	<li>
 * 		Have a public constructor with a single <c>String</c> parameter.
 * 	<li>
 * 		Have one of the following public static methods that takes in a single <c>String</c> parameter:
 * 		<ul>
 * 			<li><c>fromString</c>
 * 			<li><c>fromValue</c>
 * 			<li><c>valueOf</c>
 * 			<li><c>parse</c>
 * 			<li><c>parseString</c>
 * 			<li><c>forName</c>
 * 			<li><c>forString</c>
 * 	</ul>
 * </ul>
 */
public class SimplePartParser extends BaseHttpPartParser {

	//-----------------------------------------------------------------------------------------------------------------
	// Static
	//-----------------------------------------------------------------------------------------------------------------

	/** Reusable instance of {@link SimplePartParser}, all default settings. */
	public static final SimplePartParser DEFAULT = create().build();

	/** Reusable instance of {@link SimplePartParser}, all default settings. */
	public static final SimplePartParserSession DEFAULT_SESSION = DEFAULT.createPartSession(null);

	/**
	 * Static creator.
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
	public static class Builder extends BaseHttpPartParser.Builder {

		Builder() {
			super();
		}

		Builder(Builder builder) {
			super(builder);
		}

		@Override
		public SimplePartParser build() {
			return new SimplePartParser(this);
		}

		@Override
		public BeanContextableBuilder copy() {
			return new Builder(this);
		}
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Instance
	//-------------------------------------------------------------------------------------------------------------------

	/**
	 * Constructor
	 *
	 * @param builder The builder for this object.
	 */
	protected SimplePartParser(Builder builder) {
		super(builder);
	}

	@Override
	public SimplePartParserSession createPartSession(ParserSessionArgs args) {
		return new SimplePartParserSession();
	}

	@Override
	public Builder copy() {
		throw unsupportedOperationException("Not implemented.");
	}
}
