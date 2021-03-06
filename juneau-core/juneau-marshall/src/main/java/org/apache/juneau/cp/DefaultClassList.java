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
package org.apache.juneau.cp;

import static java.util.Arrays.*;
import static org.apache.juneau.assertions.Assertions.*;
import static java.util.Optional.*;

import java.util.*;

import org.apache.juneau.annotation.*;

/**
 * A list of default implementation classes.
 */
@NotThreadSafe
public class DefaultClassList {

	//-----------------------------------------------------------------------------------------------------------------
	// Static
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Static creator.
	 *
	 * @return A new object.
	 */
	public static DefaultClassList create() {
		return new DefaultClassList();
	}

	/**
	 * Static creator.
	 *
	 * @param values Initial entries in this list.
	 * @return A new object initialized with the specified values.
	 */
	public static DefaultClassList of(Class<?>...values) {
		return new DefaultClassList().add(values);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Instance
	//-----------------------------------------------------------------------------------------------------------------

	private final List<Class<?>> entries;

	/**
	 * Constructor.
	 */
	protected DefaultClassList() {
		entries = new ArrayList<>();
	}

	/**
	 * Copy constructor
	 *
	 * @param value The object to copy.
	 */
	public DefaultClassList(DefaultClassList value) {
		entries = new ArrayList<>(value.entries);
	}

	/**
	 * Prepends the specified values to the beginning of this list.
	 *
	 * @param values The values to prepend to this list.
	 * @return This object.
	 */
	public DefaultClassList add(Class<?>...values) {
		entries.addAll(0, asList(values));
		return this;
	}

	/**
	 * Returns the first class in this list which is a subclass of (or same as) the specified type.
	 *
	 * @param type The parent type to check for.
	 * @return The first class in this list which is a subclass of the specified type.
	 */
	@SuppressWarnings("unchecked")
	public <T> Optional<Class<? extends T>> get(Class<T> type) {
		assertArgNotNull("type", type);
		for (Class<?> e : entries)
			if (e != null && type.isAssignableFrom(e))
				return Optional.of((Class<? extends T>)e);
		return empty();
	}

	/**
	 * Creates a copy of this list.
	 *
	 * @return A copy of this list.
	 */
	public DefaultClassList copy() {
		return new DefaultClassList(this);
	}
}
