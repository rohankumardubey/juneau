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
package org.apache.juneau.utils;

import static org.apache.juneau.internal.ObjectUtils.*;
import static org.apache.juneau.internal.StringUtils.*;

import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.Bean;
import org.apache.juneau.collections.*;

/**
 * Utility class for comparing two versions of a POJO.
 *
 * <p>
 *
 * <p class='bcode w800'>
 * 	<jc>// Two beans to compare.</jc>
 * 	MyBean bean1, bean2;
 *
 *	<jc>// Get differences.</jc>
 * 	BeanDiff bf = BeanDiff.<jsm>create</jsm>(bean1, bean2).exclude(<js>"fooProperty"</js>).build();
 *
 * 	<jc>// Check for differences.</jc>
 * 	<jk>boolean</jk> b = bf.hasDiffs();
 *
 * 	OMap v1 = bf.getV1();  <jc>// Get version 1 differences.</jc>
 * 	OMap v2 = bf.getV2();  <jc>// Get version 2 differences.</jc>
 *
 * 	<jc>// Display differences.</jc>
 * 	System.<jsf>err</jsf>.println(bf);
 * </p>
 */
@Bean(properties="v1,v2")
public class BeanDiff {

	//-----------------------------------------------------------------------------------------------------------------
	// Static
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Create a new builder for this class.
	 *
	 * @param first The first bean to compare.
	 * @param second The second bean to compare.
	 * @return A new builder.
	 */
	public static <T> Builder<T> create(T first, T second) {
		return new Builder<T>().first(first).second(second);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Builder
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Builder class.
	 *
	 * @param <T> The bean type.
	 */
	public static class Builder<T> {
		T first, second;
		BeanContext beanContext = BeanContext.DEFAULT;
		Set<String> include, exclude;

		/**
		 * Specifies the first bean to compare.
		 *
		 * @param value The first bean to compare.
		 * @return This object (for method chaining).
		 */
		public Builder<T> first(T value) {
			this.first = value;
			return this;
		}

		/**
		 * Specifies the second bean to compare.
		 *
		 * @param value The first bean to compare.
		 * @return This object (for method chaining).
		 */
		public Builder<T> second(T value) {
			this.second = value;
			return this;
		}

		/**
		 * Specifies the bean context to use for introspecting beans.
		 *
		 * <p>
		 * If not specified, uses {@link BeanContext#DEFAULT}.
		 *
		 * @param value The bean context to use for introspecting beans.
		 * @return This object (for method chaining).
		 */
		public Builder<T> beanContext(BeanContext value) {
			this.beanContext = value;
			return this;
		}

		/**
		 * Specifies the properties to include in the comparison.
		 *
		 * <p>
		 * If not specified, compares all properties.
		 *
		 * @param properties The properties to include in the comparison.
		 * @return This object (for method chaining).
		 */
		public Builder<T> include(String...properties) {
			include = ASet.of(properties);
			return this;
		}

		/**
		 * Specifies the properties to include in the comparison.
		 *
		 * <p>
		 * If not specified, compares all properties.
		 *
		 * @param properties The properties to include in the comparison.
		 * @return This object (for method chaining).
		 */
		public Builder<T> include(Set<String> properties) {
			include = properties;
			return this;
		}

		/**
		 * Specifies the properties to exclude from the comparison.
		 *
		 * @param properties The properties to exclude from the comparison.
		 * @return This object (for method chaining).
		 */
		public Builder<T> exclude(String...properties) {
			exclude = ASet.of(properties);
			return this;
		}

		/**
		 * Specifies the properties to exclude from the comparison.
		 *
		 * @param properties The properties to exclude from the comparison.
		 * @return This object (for method chaining).
		 */
		public Builder<T> exclude(Set<String> properties) {
			exclude = properties;
			return this;
		}

		/**
		 * Build the differences.
		 *
		 * @return A new {@link BeanDiff} object.
		 */
		public BeanDiff build() {
			return new BeanDiff(beanContext, first, second, include, exclude);
		}

	}

	//-----------------------------------------------------------------------------------------------------------------
	// Instance
	//-----------------------------------------------------------------------------------------------------------------

	private OMap v1 = new OMap(), v2 = new OMap();

	/**
	 * Constructor.
	 *
	 * @param bc The bean context to use for comparing beans.
	 * @param first The first bean to compare.
	 * @param second The second bean to compare.
	 * @param include
	 * 	Optional properties to include in the comparison.
	 * 	<br>If <jk>null</jk>, all properties are included.
	 * @param exclude
	 * 	Optional properties to exclude in the comparison.
	 * 	<br>If <jk>null</jk>, no properties are excluded.
	 */
	@SuppressWarnings("null")
	public <T> BeanDiff(BeanContext bc, T first, T second, Set<String> include, Set<String> exclude) {
		if (first == null && second == null)
			return;
		BeanSession bs = bc.createBeanSession();
		BeanMap<?> bm1 = first == null ? null : bs.toBeanMap(first);
		BeanMap<?> bm2 = second == null ? null : bs.toBeanMap(second);
		Set<String> keys = bm1 != null ? bm1.keySet() : bm2.keySet();
		for (String k : keys) {
			if ((include == null || include.contains(k)) && (exclude == null || ! exclude.contains(k))) {
				Object o1 = bm1 == null ? null : bm1.get(k);
				Object o2 = bm2 == null ? null : bm2.get(k);
				if (ne(o1, o2)) {
					if (o1 != null)
						v1.put(k, o1);
					if (o2 != null)
						v2.put(k, o2);
				}
			}
		}
	}

	/**
	 * Returns <jk>true</jk> if the beans had differences.
	 *
	 * @return <jk>true</jk> if the beans had differences.
	 */
	public boolean hasDiffs() {
		return v1.size() > 0 || v2.size() > 0;
	}

	/**
	 * Returns the differences in the first bean.
	 *
	 * @return The differences in the first bean.
	 */
	public OMap getV1() {
		return v1;
	}

	/**
	 * Returns the differences in the second bean.
	 *
	 * @return The differences in the second bean.
	 */
	public OMap getV2() {
		return v2;
	}

	@Override
	public String toString() {
		return json(this);
	}
}
