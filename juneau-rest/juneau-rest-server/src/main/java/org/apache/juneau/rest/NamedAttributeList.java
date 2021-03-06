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

import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.cp.*;

/**
 * A list of {@link NamedAttribute} objects.
 */
public class NamedAttributeList {

	//-----------------------------------------------------------------------------------------------------------------
	// Static
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Static creator.
	 *
	 * @return An empty list.
	 */
	public static Builder create() {
		return new Builder();
	}

	/**
	 * Static creator.
	 *
	 * @param values The initial contents of this list.
	 * @return An empty list.
	 */
	public static NamedAttributeList of(NamedAttribute...values) {
		return create().add(values).build();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Builder
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Builder class.
	 */
	public static class Builder extends BeanBuilder<NamedAttributeList> {

		LinkedHashMap<String,NamedAttribute> entries;

		/**
		 * Constructor.
		 */
		protected Builder() {
			super(NamedAttributeList.class);
			entries = new LinkedHashMap<>();
		}

		/**
		 * Copy constructor.
		 *
		 * @param copyFrom The builder being copied.
		 */
		protected Builder(Builder copyFrom) {
			super(copyFrom);
			entries = new LinkedHashMap<>(copyFrom.entries);
		}

		@Override /* BeanBuilder */
		protected NamedAttributeList buildDefault() {
			return new NamedAttributeList(this);
		}

		@Override /* BeanBuilder */
		public Builder copy() {
			return new Builder(this);
		}

		//-------------------------------------------------------------------------------------------------------------
		// Properties
		//-------------------------------------------------------------------------------------------------------------

		/**
		 * Appends the specified rest matcher classes to the list.
		 *
		 * @param values The values to add.
		 * @return This object (for method chaining).
		 */
		public Builder add(NamedAttribute...values) {
			for (NamedAttribute v : values)
				entries.put(v.getName(), v);
			return this;
		}

		// <FluentSetters>

		@Override /* BeanBuilder */
		public Builder type(Class<? extends NamedAttributeList> value) {
			super.type(value);
			return this;
		}

		@Override /* BeanBuilder */
		public Builder impl(NamedAttributeList value) {
			super.impl(value);
			return this;
		}

		@Override /* BeanBuilder */
		public Builder outer(Object value) {
			super.outer(value);
			return this;
		}

		@Override /* BeanBuilder */
		public Builder beanStore(BeanStore value) {
			super.beanStore(value);
			return this;
		}

		// </FluentSetters>
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Instance
	//-----------------------------------------------------------------------------------------------------------------

	final NamedAttribute[] entries;

	/**
	 * Constructor.
	 *
	 * @param b The builder of this object.
	 */
	public NamedAttributeList(Builder b) {
		entries = b.entries.values().toArray(new NamedAttribute[b.entries.size()]);
	}

	/**
	 * Returns a copy of this list.
	 *
	 * @return A new copy of this list.
	 */
	public Builder copy() {
		return copy().add(entries);
	}
}
