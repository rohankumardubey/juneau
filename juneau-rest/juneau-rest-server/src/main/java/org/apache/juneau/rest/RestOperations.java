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

import static org.apache.juneau.internal.ClassUtils.*;
import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.cp.*;
import org.apache.juneau.http.response.*;
import org.apache.juneau.rest.annotation.*;

/**
 * Encapsulates the set of {@link RestOp}-annotated methods within a single {@link Rest}-annotated object.
 */
public class RestOperations {

	//-----------------------------------------------------------------------------------------------------------------
	// Static
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Represents a null value for the {@link Rest#restOperationsClass()} annotation.
	 */
	@SuppressWarnings("javadoc")
	public final class Null extends RestOperations {
		public Null(Builder builder) throws Exception {
			super(builder);
		}
	}

	/**
	 * Creates a new builder.
	 *
	 * @return A new builder.
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
	public static class Builder extends BeanBuilder<RestOperations> {

		TreeMap<String,TreeSet<RestOpContext>> map;
		Set<RestOpContext> set;

		/**
		 * Constructor.
		 */
		protected Builder() {
			super(RestOperations.class);
			map = new TreeMap<>();
			set = ASet.of();
		}

		/**
		 * Copy constructor.
		 *
		 * @param copyFrom The builder being copied.
		 */
		protected Builder(Builder copyFrom) {
			super(copyFrom);
			map = new TreeMap<>(copyFrom.map);
			set = ASet.of(copyFrom.set);
		}

		@Override /* BeanBuilder */
		protected RestOperations buildDefault() {
			return new RestOperations(this);
		}

		@Override /* BeanBuilder */
		public Builder copy() {
			return new Builder(this);
		}

		//-------------------------------------------------------------------------------------------------------------
		// Properties
		//-------------------------------------------------------------------------------------------------------------

		/**
		 * Adds a method context to this builder.
		 *
		 * @param value The REST method context to add.
		 * @return Adds a method context to this builder.
		 */
		public Builder add(RestOpContext value) {
			return add(value.getHttpMethod(), value);
		}

		/**
		 * Adds a method context to this builder.
		 *
		 * @param httpMethodName The HTTP method name.
		 * @param value The REST method context to add.
		 * @return Adds a method context to this builder.
		 */
		public Builder add(String httpMethodName, RestOpContext value) {
			httpMethodName = httpMethodName.toUpperCase();
			if (! map.containsKey(httpMethodName))
				map.put(httpMethodName, new TreeSet<>());
			map.get(httpMethodName).add(value);
			set.add(value);
			return this;
		}

		// <FluentSetters>

		@Override /* BeanBuilder */
		public Builder type(Class<? extends RestOperations> value) {
			super.type(value);
			return this;
		}

		@Override /* BeanBuilder */
		public Builder impl(RestOperations value) {
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

	private final Map<String,List<RestOpContext>> map;
	private List<RestOpContext> list;

	/**
	 * Constructor.
	 *
	 * @param builder The builder containing the settings for this object.
	 */
	public RestOperations(Builder builder) {
		AMap<String,List<RestOpContext>> m = AMap.create();
		for (Map.Entry<String,TreeSet<RestOpContext>> e : builder.map.entrySet())
			m.put(e.getKey(), AList.of(e.getValue()));
		this.map = m;
		this.list = AList.of(builder.set);
	}

	/**
	 * Finds the method that should handle the specified call.
	 *
	 * @param call The HTTP call.
	 * @return The method that should handle the specified call.
	 * @throws MethodNotAllowed If no methods implement the requested HTTP method.
	 * @throws PreconditionFailed At least one method was found but it didn't match one or more matchers.
	 * @throws NotFound HTTP method match was found but matching path was not.
	 */
	public RestOpContext findOperation(RestCall call) throws MethodNotAllowed, PreconditionFailed, NotFound {
		String m = call.getMethod();

		int rc = 0;
		if (map.containsKey(m)) {
			for (RestOpContext oc : map.get(m)) {
				int mrc = oc.match(call);
				if (mrc == 2)
					return oc;
				rc = Math.max(rc, mrc);
			}
		}

		if (map.containsKey("*")) {
			for (RestOpContext oc : map.get("*")) {
				int mrc = oc.match(call);
				if (mrc == 2)
					return oc;
				rc = Math.max(rc, mrc);
			}
		}

		// If no paths matched, see if the path matches any other methods.
		// Note that we don't want to match against "/*" patterns such as getOptions().
		if (rc == 0) {
			for (RestOpContext oc : list) {
				if (! oc.getPathPattern().endsWith("/*")) {
					int orc = oc.match(call);
					if (orc == 2)
						throw new MethodNotAllowed();
				}
			}
		}

		if (rc == 1)
			throw new PreconditionFailed("Method ''{0}'' not found on resource on path ''{1}'' with matching matcher.", m, call.getPathInfo());

		throw new NotFound("Java method matching path ''{0}'' not found on resource ''{1}''.", call.getPathInfo(), className(call.getResource()));
	}


	/**
	 * Returns the list of method contexts in this object.
	 *
	 * @return An unmodifiable list of method contexts in this object.
	 */
	public List<RestOpContext> getOpContexts() {
		return Collections.unmodifiableList(list);
	}
}
