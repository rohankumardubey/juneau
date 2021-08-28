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

import static java.util.stream.Collectors.*;
import static java.util.Collections.*;

import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.cp.*;

/**
 * A list of {@link RestMatcher} objects.
 */
public class RestMatcherList {

	private final List<RestMatcher> matchers;

	/**
	 * Static creator.
	 *
	 * @return An empty list.
	 */
	public static RestMatcherListBuilder create() {
		return new RestMatcherListBuilder();
	}

	/**
	 * Constructor.
	 *
	 * @param builder The builder containing the contents for this list.
	 */
	protected RestMatcherList(RestMatcherListBuilder builder) {
		matchers = unmodifiableList(
			builder
				.matchers
				.stream()
				.map(x -> instantiate(x, builder.beanStore))
				.collect(toList())
		);
	}

	private static RestMatcher instantiate(Object o, BeanStore bs) {
		if (o instanceof RestMatcher)
			return (RestMatcher)o;
		try {
			return (RestMatcher)bs.createBean((Class<?>)o);
		} catch (ExecutableException e) {
			throw new ConfigException(e, "Could not instantiate class {0}", o);
		}
	}

	/**
	 * Returns the matchers in this list.
	 *
	 * @return The matchers in this list.  The list is unmodifiable.
	 */
	public List<RestMatcher> getMatchers() {
		return matchers;
	}
}
