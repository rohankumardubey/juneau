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
package org.apache.juneau.http.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import static org.apache.juneau.internal.ArrayUtils.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.httppart.*;
import org.apache.juneau.jsonschema.annotation.*;
import org.apache.juneau.reflect.*;
import org.apache.juneau.svl.*;

/**
 * Utility classes and methods for the {@link Response @Response} annotation.
 */
public class ResponseAnnotation {

	//-----------------------------------------------------------------------------------------------------------------
	// Static
	//-----------------------------------------------------------------------------------------------------------------

	/** Default value */
	public static final Response DEFAULT = create().build();

	/**
	 * Instantiates a new builder for this class.
	 *
	 * @return A new builder object.
	 */
	public static Builder create() {
		return new Builder();
	}

	/**
	 * Instantiates a new builder for this class.
	 *
	 * @param on The targets this annotation applies to.
	 * @return A new builder object.
	 */
	public static Builder create(Class<?>...on) {
		return create().on(on);
	}

	/**
	 * Instantiates a new builder for this class.
	 *
	 * @param on The targets this annotation applies to.
	 * @return A new builder object.
	 */
	public static Builder create(String...on) {
		return create().on(on);
	}

	/**
	 * Creates a copy of the specified annotation.
	 *
	 * @param a The annotation to copy.
	 * @param r The var resolver for resolving any variables.
	 * @return A copy of the specified annotation.
	 */
	public static Response copy(Response a, VarResolverSession r) {
		return
			create()
			.api(r.resolve(a.api()))
			.code(a.code())
			.d(r.resolve(a.d()))
			.description(r.resolve(a.description()))
			.ex(r.resolve(a.ex()))
			.example(r.resolve(a.example()))
			.examples(r.resolve(a.examples()))
			.exs(r.resolve(a.exs()))
			.headers(ResponseHeaderAnnotation.copy(a.headers(), r))
			.on(r.resolve(a.on()))
			.onClass(a.onClass())
			.parser(a.parser())
			.schema(SchemaAnnotation.copy(a.schema(), r))
			.serializer(a.serializer())
			.value(a.value())
			.build();
	}

	/**
	 * Returns <jk>true</jk> if the specified annotation contains all default values.
	 *
	 * @param a The annotation to check.
	 * @return <jk>true</jk> if the specified annotation contains all default values.
	 */
	public static boolean empty(Response a) {
		return a == null || DEFAULT.equals(a);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Builder
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Builder class.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link BeanContextBuilder#annotations(Annotation...)}
	 * </ul>
	 */
	public static class Builder extends TargetedAnnotationTMBuilder {

		Class<? extends HttpPartParser> parser = HttpPartParser.Null.class;
		Class<? extends HttpPartSerializer> serializer = HttpPartSerializer.Null.class;
		int[] code={}, value={};
		ResponseHeader[] headers={};
		Schema schema = SchemaAnnotation.DEFAULT;
		String[] api={}, d={}, description={}, ex={}, example={}, examples={}, exs={};

		/**
		 * Constructor.
		 */
		public Builder() {
			super(Response.class);
		}

		/**
		 * Instantiates a new {@link Response @Response} object initialized with this builder.
		 *
		 * @return A new {@link Response @Response} object.
		 */
		public Response build() {
			return new Impl(this);
		}

		/**
		 * Sets the {@link Response#api} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder api(String...value) {
			this.api = value;
			return this;
		}

		/**
		 * Sets the {@link Response#code} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder code(int...value) {
			this.code = value;
			return this;
		}

		/**
		 * Sets the {@link Response#d} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder d(String...value) {
			this.d = value;
			return this;
		}

		/**
		 * Sets the {@link Response#description} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder description(String...value) {
			this.description = value;
			return this;
		}

		/**
		 * Sets the {@link Response#ex} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder ex(String...value) {
			this.ex = value;
			return this;
		}

		/**
		 * Sets the {@link Response#example} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder example(String...value) {
			this.example = value;
			return this;
		}

		/**
		 * Sets the {@link Response#examples} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder examples(String...value) {
			this.examples = value;
			return this;
		}

		/**
		 * Sets the {@link Response#exs} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder exs(String...value) {
			this.exs = value;
			return this;
		}

		/**
		 * Sets the {@link Response#headers} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder headers(ResponseHeader...value) {
			this.headers = value;
			return this;
		}

		/**
		 * Sets the {@link Response#parser} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder parser(Class<? extends HttpPartParser> value) {
			this.parser = value;
			return this;
		}

		/**
		 * Sets the {@link Response#schema} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder schema(Schema value) {
			this.schema = value;
			return this;
		}

		/**
		 * Sets the {@link Response#serializer} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder serializer(Class<? extends HttpPartSerializer> value) {
			this.serializer = value;
			return this;
		}

		/**
		 * Sets the {@link Response#value} property on this annotation.
		 *
		 * @param value The new value for this property.
		 * @return This object (for method chaining).
		 */
		public Builder value(int...value) {
			this.value = value;
			return this;
		}

		// <FluentSetters>

		@Override /* GENERATED - TargetedAnnotationBuilder */
		public Builder on(String...values) {
			super.on(values);
			return this;
		}

		@Override /* GENERATED - TargetedAnnotationTBuilder */
		public Builder on(java.lang.Class<?>...value) {
			super.on(value);
			return this;
		}

		@Override /* GENERATED - TargetedAnnotationTBuilder */
		public Builder onClass(java.lang.Class<?>...value) {
			super.onClass(value);
			return this;
		}

		@Override /* GENERATED - TargetedAnnotationTMBuilder */
		public Builder on(Method...value) {
			super.on(value);
			return this;
		}

		// </FluentSetters>
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Implementation
	//-----------------------------------------------------------------------------------------------------------------

	private static class Impl extends TargetedAnnotationTImpl implements Response {

		private final Class<? extends HttpPartParser> parser;
		private final Class<? extends HttpPartSerializer> serializer;
		private final int[] code, value;
		private final ResponseHeader[] headers;
		private final Schema schema;
		private final String[] api, d, description, ex, example, examples, exs;

		Impl(Builder b) {
			super(b);
			this.api = copyOf(b.api);
			this.code = Arrays.copyOf(b.code, b.code.length);
			this.d = copyOf(b.d);
			this.description = copyOf(b.description);
			this.ex = copyOf(b.ex);
			this.example = copyOf(b.example);
			this.examples = copyOf(b.examples);
			this.exs = copyOf(b.exs);
			this.headers = copyOf(b.headers);
			this.parser = b.parser;
			this.schema = b.schema;
			this.serializer = b.serializer;
			this.value = Arrays.copyOf(b.value, b.value.length);
			postConstruct();
		}

		@Override /* Response */
		public String[] api() {
			return api;
		}

		@Override /* Response */
		public int[] code() {
			return code;
		}

		@Override /* Response */
		public String[] d() {
			return d;
		}

		@Override /* Response */
		public String[] description() {
			return description;
		}

		@Override /* Response */
		public String[] ex() {
			return ex;
		}

		@Override /* Response */
		public String[] example() {
			return example;
		}

		@Override /* Response */
		public String[] examples() {
			return examples;
		}

		@Override /* Response */
		public String[] exs() {
			return exs;
		}

		@Override /* Response */
		public ResponseHeader[] headers() {
			return headers;
		}

		@Override /* Response */
		public Class<? extends HttpPartParser> parser() {
			return parser;
		}

		@Override /* Response */
		public Schema schema() {
			return schema;
		}

		@Override /* Response */
		public Class<? extends HttpPartSerializer> serializer() {
			return serializer;
		}

		@Override /* Response */
		public int[] value() {
			return value;
		}
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Appliers
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Applies targeted {@link Response} annotations to a {@link BeanContextBuilder}.
	 */
	public static class Applier extends AnnotationApplier<Response,BeanContextBuilder> {

		/**
		 * Constructor.
		 *
		 * @param vr The resolver for resolving values in annotations.
		 */
		public Applier(VarResolverSession vr) {
			super(Response.class, BeanContextBuilder.class, vr);
		}

		@Override
		public void apply(AnnotationInfo<Response> ai, BeanContextBuilder b) {
			Response a = ai.getAnnotation();

			if (isEmpty(a.on()) && isEmpty(a.onClass()))
				return;

			b.annotations(copy(a, vr()));
		}
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Other
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * A collection of {@link Response @Response annotations}.
	 */
	@Documented
	@Target({METHOD,TYPE})
	@Retention(RUNTIME)
	@Inherited
	public static @interface Array {

		/**
		 * The child annotations.
		 */
		Response[] value();
	}
}