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
package org.apache.juneau.svl;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.juneau.collections.*;
import org.apache.juneau.cp.*;
import org.apache.juneau.svl.vars.*;

/**
 * Utility class for resolving variables of the form <js>"$X{key}"</js> in strings.
 *
 * <p>
 * Variables are of the form <c>$X{key}</c>, where <c>X</c> can consist of zero or more ASCII characters.
 * <br>The variable key can contain anything, even nested variables that get recursively resolved.
 *
 * <p>
 * Variables are defined through the {@link Builder#vars(Class[])} method.
 *
 * <p>
 * The {@link Var} interface defines how variables are converted to values.
 *
 * <h5 class='section'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jk>public class</jk> SystemPropertiesVar <jk>extends</jk> SimpleVar {
 *
 * 		<jc>// Must have a no-arg constructor!</jc>
 * 		<jk>public</jk> SystemPropertiesVar() {
 * 			<jk>super</jk>(<js>"S"</js>);
 * 		}
 *
 * 		<ja>@Override</ja>
 * 		<jk>public</jk> String resolve(VarResolverSession session, String varVal) {
 * 			<jk>return</jk> System.<jsm>getProperty</jsm>(varVal);
 * 		}
 * 	}
 *
 * 	<jc>// Create a variable resolver that resolves system properties (e.g. "$S{java.home}")</jc>
 * 	VarResolver r = VarResolver.<jsm>create</jsm>().vars(SystemPropertiesVar.<jk>class</jk>).build();
 *
 * 	<jc>// Use it!</jc>
 * 	System.<jsf>out</jsf>.println(r.resolve(<js>"java.home is set to $S{java.home}"</js>));
 * </p>
 *
 * <ul class='seealso'>
 * 	<li class='link'>{@doc VarResolvers}
 * </ul>
 */
public class VarResolver {

	/**
	 * Default string variable resolver with support for system properties and environment variables:
	 *
	 * <ul>
	 * 	<li><c>$S{key[,default]}</c> - {@link SystemPropertiesVar}
	 * 	<li><c>$E{key[,default]}</c> - {@link EnvVariablesVar}
	 * 	<li><c>$A{key[,default]}</c> - {@link ArgsVar}
	 * 	<li><c>$MF{key[,default]}</c> - {@link ManifestFileVar}
	 * 	<li><c>$SW{stringArg,pattern:thenValue[,pattern:thenValue...]}</c> - {@link SwitchVar}
	 * 	<li><c>$IF{arg,then[,else]}</c> - {@link IfVar}
	 * 	<li><c>$CO{arg[,arg2...]}</c> - {@link CoalesceVar}
	 * 	<li><c>$PM{arg,pattern}</c> - {@link PatternMatchVar}
	 * 	<li><c>$PR{stringArg,pattern,replace}</c>- {@link PatternReplaceVar}
	 * 	<li><c>$PE{arg,pattern,groupIndex}</c> - {@link PatternExtractVar}
	 * 	<li><c>$UC{arg}</c> - {@link UpperCaseVar}
	 * 	<li><c>$LC{arg}</c> - {@link LowerCaseVar}
	 * 	<li><c>$NE{arg}</c> - {@link NotEmptyVar}
	 * 	<li><c>$LN{arg[,delimiter]}</c> - {@link LenVar}
	 * 	<li><c>$ST{arg,start[,end]}</c> - {@link SubstringVar}
	 * </ul>
	 */
	public static final VarResolver DEFAULT = create().defaultVars().build();

	final Var[] vars;
	private final Map<String,Var> varMap;
	final BeanStore beanStore;

	/**
	 * Instantiates a new clean-slate {@link Builder} object.
	 *
	 * @return A new {@link Builder} object.
	 */
	public static Builder create() {
		return new Builder();
	}

	/**
	 * Constructor.
	 *
	 * @param builder The builder for this object.
	 */
	protected VarResolver(Builder builder) {
		this.vars = builder.vars.stream().map(x -> toVar(builder.beanStore,x)).toArray(Var[]::new);

		Map<String,Var> m = new ConcurrentSkipListMap<>();
		for (Var v : vars)
			m.put(v.getName(), v);

		this.varMap = AMap.unmodifiable(m);
		this.beanStore = BeanStore.of(builder.beanStore);
	}

	@SuppressWarnings("unchecked")
	private static Var toVar(BeanStore bs, Object o) {
		if (o instanceof Class)
			return bs.createBean(Var.class, (Class<? extends Var>)o);
		return (Var)o;
	}

	/**
	 * Returns a new builder object using the settings in this resolver as a base.
	 *
	 * @return A new var resolver builder.
	 */
	public Builder copy() {
		return new Builder(this);
	}

	/**
	 * Builder class for building instances of {@link VarResolver}.
	 */
	public static class Builder {

		final VarList vars;
		BeanStore beanStore;
		VarResolver impl;

		/**
		 * Constructor.
		 */
		protected Builder() {
			vars = VarList.create();
			beanStore = BeanStore.create().build();
		}

		/**
		 * Copy constructor.
		 *
		 * @param copyFrom The bean to copy from.
		 */
		protected Builder(VarResolver copyFrom) {
			vars = VarList.of(copyFrom.vars);
			beanStore = BeanStore.of(copyFrom.beanStore);
		}

		/**
		 * Copy constructor.
		 *
		 * @param copyFrom The builder to copy from.
		 */
		protected Builder(Builder copyFrom) {
			vars = copyFrom.vars.copy();
			beanStore = BeanStore.of(copyFrom.beanStore);
			impl = copyFrom.impl;
		}

		/**
		 * Register new variables with this resolver.
		 *
		 * @param values
		 * 	The variable resolver classes.
		 * 	These classes must subclass from {@link Var} and have no-arg constructors.
		 * @return This object .
		 */
		@SafeVarargs
		public final Builder vars(Class<? extends Var>...values) {
			vars.append(values);
			return this;
		}

		/**
		 * Register new variables with this resolver.
		 *
		 * @param values
		 * 	The variable resolver classes.
		 * 	These classes must subclass from {@link Var} and have no-arg constructors.
		 * @return This object .
		 */
		public Builder vars(Var...values) {
			vars.append(values);
			return this;
		}

		/**
		 * Register new variables with this resolver.
		 *
		 * @param values
		 * 	The variable resolver classes.
		 * 	These classes must subclass from {@link Var} and have no-arg constructors.
		 * @return This object .
		 */
		public Builder vars(VarList values) {
			vars.append(values);
			return this;
		}

		/**
		 * Specify a pre-instantiated bean for the {@link #build()} method to return.
		 *
		 * @param value The pre-instantiated bean.
		 * @return This object.
		 */
		public Builder impl(VarResolver value) {
			this.impl = value;
			return this;
		}

		/**
		 * Adds the default variables to this builder.
		 *
		 * <p>
		 * The default variables are:
		 * <ul>
		 * 	<li>{@link SystemPropertiesVar}
		 * 	<li>{@link EnvVariablesVar}
		 * 	<li>{@link ArgsVar}
		 * 	<li>{@link ManifestFileVar}
		 * 	<li>{@link SwitchVar}
		 * 	<li>{@link IfVar}
		 * 	<li>{@link CoalesceVar}
		 * 	<li>{@link PatternMatchVar}
		 * 	<li>{@link PatternReplaceVar}
		 * 	<li>{@link PatternExtractVar}
		 * 	<li>{@link UpperCaseVar}
		 * 	<li>{@link LowerCaseVar}
		 * 	<li>{@link NotEmptyVar}
		 * 	<li>{@link LenVar}
		 * 	<li>{@link SubstringVar}
		 * </ul>
		 *
		 * @return This object .
		 */
		public Builder defaultVars() {
			vars.addDefault();
			return this;
		}

		/**
		 * Associates a bean store with this builder.
		 *
		 * @param value The bean store to associate with this var resolver.
		 * @return This object .
		 */
		public Builder beanStore(BeanStore value) {
			this.beanStore = BeanStore.of(value);
			return this;
		}

		/**
		 * Adds a bean to the bean store in this session.
		 *
		 * @param <T> The bean type.
		 * @param c The bean type.
		 * @param value The bean.
		 * @return This object .
		 */
		public <T> Builder bean(Class<T> c, T value) {
			beanStore.addBean(c, value);
			return this;
		}

		/**
		 * Create a new var resolver using the settings in this builder.
		 *
		 * @return A new var resolver.
		 */
		public VarResolver build() {
			return impl != null ? impl : new VarResolver(this);
		}

		/**
		 * Creates a copy of this builder.
		 *
		 * @return A new copy of this builder.
		 */
		public Builder copy() {
			return new Builder(this);
		}
	}

	/**
	 * Returns an unmodifiable map of {@link Var Vars} associated with this context.
	 *
	 * @return A map whose keys are var names (e.g. <js>"S"</js>) and values are {@link Var} instances.
	 */
	protected Map<String,Var> getVarMap() {
		return varMap;
	}

	/**
	 * Returns an array of variables define in this variable resolver context.
	 *
	 * @return A new array containing the variables in this context.
	 */
	protected Var[] getVars() {
		return Arrays.copyOf(vars, vars.length);
	}

	/**
	 * Adds a bean to this session.
	 *
	 * @param <T> The bean type.
	 * @param c The bean type.
	 * @param value The bean.
	 * @return This object .
	 */
	public <T> VarResolver addBean(Class<T> c, T value) {
		beanStore.addBean(c, value);
		return this;
	}

	/**
	 * Creates a new resolver session with no session objects.
	 *
	 * @return A new resolver session.
	 */
	public VarResolverSession createSession() {
		return new VarResolverSession(this, null);
	}

	/**
	 * Same as {@link #createSession()} except allows you to specify a bean store for resolving beans.
	 *
	 * @param beanStore The bean store to associate with this session.
	 * @return A new resolver session.
	 */
	public VarResolverSession createSession(BeanStore beanStore) {
		return new VarResolverSession(this, beanStore);
	}

	/**
	 * Resolve variables in the specified string.
	 *
	 * <p>
	 * This is a shortcut for calling <code>createSession(<jk>null</jk>).resolve(s);</code>.
	 * <br>This method can only be used if the string doesn't contain variables that rely on the existence of session
	 * variables.
	 *
	 * @param s The input string.
	 * @return The string with variables resolved, or the same string if it doesn't contain any variables to resolve.
	 */
	public String resolve(String s) {
		return createSession(null).resolve(s);
	}

	/**
	 * Resolve variables in the specified string and sends the results to the specified writer.
	 *
	 * <p>
	 * This is a shortcut for calling <code>createSession(<jk>null</jk>).resolveTo(s, w);</code>.
	 * <br>This method can only be used if the string doesn't contain variables that rely on the existence of session
	 * variables.
	 *
	 * @param s The input string.
	 * @param w The writer to send the result to.
	 * @throws IOException Thrown by underlying stream.
	 */
	public void resolveTo(String s, Writer w) throws IOException {
		createSession(null).resolveTo(s, w);
	}
}