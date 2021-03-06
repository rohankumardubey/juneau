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

import static org.apache.juneau.Enablement.*;
import static org.apache.juneau.rest.HttpRuntimeException.*;

import java.util.function.*;

import javax.servlet.http.*;

import org.apache.juneau.*;
import org.apache.juneau.cp.*;
import org.apache.juneau.http.response.*;
import org.apache.juneau.rest.annotation.*;
import org.apache.juneau.utils.*;

/**
 * Interface used for selectively turning on debug per request.
 */
public interface DebugEnablement {

	//-----------------------------------------------------------------------------------------------------------------
	// Static
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Represents no DebugEnablement.
	 */
	public abstract class Null implements DebugEnablement {};

	/**
	 * Creator.
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
	public class Builder {

		ReflectionMap.Builder<Enablement> mapBuilder;
		Enablement defaultEnablement = NEVER;
		Predicate<HttpServletRequest> conditionalPredicate;
		BeanCreator<DebugEnablement> creator = BeanCreator.of(DebugEnablement.class).type(BasicDebugEnablement.class).builder(this);

		/**
		 * Constructor.
		 */
		protected Builder() {
			mapBuilder = ReflectionMap.create(Enablement.class);
			defaultEnablement = NEVER;
			conditionalPredicate = x -> "true".equalsIgnoreCase(x.getHeader("Debug"));
		}

		/**
		 * Copy constructor.
		 *
		 * @param copyFrom The builder being copied.
		 */
		protected Builder(Builder copyFrom) {
			mapBuilder = copyFrom.mapBuilder.copy();
			creator = copyFrom.creator.copy();
			defaultEnablement = copyFrom.defaultEnablement;
			conditionalPredicate = copyFrom.conditionalPredicate;
		}

		/**
		 * Creates a new {@link DebugEnablement} object from this builder.
		 *s
		 * <p>
		 * Instantiates an instance of the {@link #type(Class) implementation class} or
		 * else {@link BasicDebugEnablement} if implementation class was not specified.
		 *
		 * @return A new {@link DebugEnablement} object.
		 */
		public DebugEnablement build() {
			try {
				return creator.run();
			} catch (Exception e) {
				throw toHttpException(e, InternalServerError.class);
			}
		}

		/**
		 * Specifies the bean store to use for instantiating the {@link DebugEnablement} object.
		 *
		 * @param value The new value for this setting.
		 * @return  This object.
		 */
		public Builder beanStore(BeanStore value) {
			creator.store(value);
			return this;
		}

		/**
		 * Specifies a subclass of {@link DebugEnablement} to create when the {@link #build()} method is called.
		 *
		 * @param value The new value for this setting.
		 * @return  This object.
		 */
		public Builder type(Class<? extends DebugEnablement> value) {
			creator.type(value == null ? BasicDebugEnablement.class : value);
			return this;
		}

		/**
		 * Specifies an already-instantiated bean for the {@link #build()} method to return.
		 *
		 * @param value The setting value.
		 * @return This object.
		 */
		public Builder impl(DebugEnablement value) {
			creator.impl(value);
			return this;
		}

		/**
		 * Enables or disables debug on the specified classes and/or methods.
		 *
		 * <p>
		 * Allows you to target specified debug enablement on specified classes and/or methods.
		 *
		 * @param enablement
		 * 	The debug enablement setting to set on the specified classes/methods.
		 * 	<br>Can be any of the following:
		 * 	<ul>
		 * 		<li>{@link Enablement#ALWAYS ALWAYS} - Debug is always enabled.
		 * 		<li>{@link Enablement#NEVER NEVER} - Debug is always disabled.
		 * 		<li>{@link Enablement#CONDITIONAL CONDITIONAL} - Debug is enabled when the {@link #conditionalPredicate(Predicate)} conditional predicate test} passes.
		 * 	</ul>
		 * @param keys
		 * 	The mapping keys.
		 * 	<br>Can be any of the following:
		 * 	<ul>
		 * 		<li>Full class name (e.g. <js>"com.foo.MyClass"</js>).
		 * 		<li>Simple class name (e.g. <js>"MyClass"</js>).
		 * 		<li>All classes (e.g. <js>"*"</js>).
		 * 		<li>Full method name (e.g. <js>"com.foo.MyClass.myMethod"</js>).
		 * 		<li>Simple method name (e.g. <js>"MyClass.myMethod"</js>).
		 * 		<li>A comma-delimited list of anything on this list.
		 * 	</ul>
		 * @return This object.
		 */
		public Builder enable(Enablement enablement, String...keys) {
			for (String k : keys)
				mapBuilder.append(k, enablement);
			return this;
		}

		/**
		 * Enables or disables debug on the specified classes.
		 *
		 * <p>
		 * Identical to {@link #enable(Enablement, String...)} but allows you to specify specific classes.
		 *
		 * @param enablement
		 * 	The debug enablement setting to set on the specified classes/methods.
		 * 	<br>Can be any of the following:
		 * 	<ul>
		 * 		<li>{@link Enablement#ALWAYS ALWAYS} - Debug is always enabled.
		 * 		<li>{@link Enablement#NEVER NEVER} - Debug is always disabled.
		 * 		<li>{@link Enablement#CONDITIONAL CONDITIONAL} - Debug is enabled when the {@link #conditionalPredicate(Predicate)} conditional predicate test} passes.
		 * 	</ul>
		 * @param classes
		 * 	The classes to set the debug enablement setting on.
		 * @return This object.
		 */
		public Builder enable(Enablement enablement, Class<?>...classes) {
			for (Class<?> c : classes)
				mapBuilder.append(c.getName(), enablement);
			return this;
		}

		/**
		 * Specifies the default debug enablement setting if not overridden per class/method.
		 *
		 * <p>
		 * The default value for this setting is {@link Enablement#NEVER NEVER}.
		 *
		 * @param value The default debug enablement setting if not overridden per class/method.
		 * @return This object.
		 */
		public Builder defaultEnable(Enablement value) {
			defaultEnablement = value;
			return this;
		}

		/**
		 * Specifies the predicate to use for conditional debug enablement.
		 *
		 * <p>
		 * Specifies the predicate to use to determine whether debug is enabled when the resolved enablement value
		 * is {@link Enablement#CONDITIONAL CONDITIONAL}.
		 *
		 * <p>
		 * The default value for this setting is <c>(<jv>x</jv>)-><js>"true"</js>.equalsIgnoreCase(<jv>x</jv>.getHeader(<js>"Debug"</js>))</c>.
		 *
		 * @param value The predicate.
		 * @return This object.
		 */
		public Builder conditionalPredicate(Predicate<HttpServletRequest> value) {
			conditionalPredicate = value;
			return this;
		}

		/**
		 * Creates a copy of this builder.
		 *
		 * @return A copy of this builder.
		 */
		public Builder copy() {
			return new Builder(this);
		}
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Instance
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Returns <jk>true</jk> if debug is enabled on the specified class and request.
	 *
	 * <p>
	 * This enables debug mode on requests once the matched class is found and before the
	 * Java method is found.
	 *
	 * @param context The context of the {@link Rest}-annotated class.
	 * @param req The HTTP request.
	 * @return <jk>true</jk> if debug is enabled on the specified method and request.
	 */
	public boolean isDebug(RestContext context, HttpServletRequest req);

	/**
	 * Returns <jk>true</jk> if debug is enabled on the specified method and request.
	 *
	 * <p>
	 * This enables debug mode after the Java method is found and allows you to enable
	 * debug on individual Java methods instead of the entire class.
	 *
	 * @param context The context of the {@link RestOp}-annotated method.
	 * @param req The HTTP request.
	 * @return <jk>true</jk> if debug is enabled on the specified method and request.
	 */
	public boolean isDebug(RestOpContext context, HttpServletRequest req);
}
