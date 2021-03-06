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
package org.apache.juneau.httppart.bean;

import static org.apache.juneau.httppart.bean.Utils.*;
import static org.apache.juneau.httppart.HttpPartType.*;
import static org.apache.juneau.annotation.InvalidAnnotationException.*;
import static java.util.Optional.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.http.annotation.*;
import org.apache.juneau.httppart.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.reflect.*;

/**
 * Represents the metadata gathered from a parameter or class annotated with {@link Response}.
 */
public class ResponseBeanMeta {

	/**
	 * Represents a non-existent meta object.
	 */
	public static ResponseBeanMeta NULL = new ResponseBeanMeta(new Builder(new AnnotationWorkList()));

	/**
	 * Create metadata from specified class.
	 *
	 * @param t The class annotated with {@link Response}.
	 * @param annotations The annotations to apply to any new part serializers or parsers.
	 * @return Metadata about the class, or <jk>null</jk> if class not annotated with {@link Response}.
	 */
	public static ResponseBeanMeta create(Type t, AnnotationWorkList annotations) {
		ClassInfo ci = ClassInfo.of(t).unwrap(Value.class, Optional.class);
		if (! ci.hasAnnotation(Response.class))
			return null;
		Builder b = new Builder(annotations);
		b.apply(ci.innerType());
		for (Response r : ci.getAnnotations(Response.class))
			b.apply(r);
		return b.build();
	}

	/**
	 * Create metadata from specified method return.
	 *
	 * @param m The method annotated with {@link Response}.
	 * @param annotations The annotations to apply to any new part serializers or parsers.
	 * @return Metadata about the class, or <jk>null</jk> if class not annotated with {@link Response}.
	 */
	public static ResponseBeanMeta create(MethodInfo m, AnnotationWorkList annotations) {
		if (! (m.hasAnnotation(Response.class) || m.getReturnType().unwrap(Value.class,Optional.class).hasAnnotation(Response.class)))
			return null;
		Builder b = new Builder(annotations);
		b.apply(m.getReturnType().unwrap(Value.class, Optional.class).innerType());
		for (Response r : m.getAnnotations(Response.class))
			b.apply(r);
		return b.build();
	}

	/**
	 * Create metadata from specified method parameter.
	 *
	 * @param mpi The method parameter.
	 * @param annotations The annotations to apply to any new part serializers or parsers.
	 * @return Metadata about the class, or <jk>null</jk> if class not annotated with {@link Response}.
	 */
	public static ResponseBeanMeta create(ParamInfo mpi, AnnotationWorkList annotations) {
		if (! mpi.hasAnnotation(Response.class))
			return null;
		Builder b = new Builder(annotations);
		b.apply(mpi.getParameterType().unwrap(Value.class, Optional.class).innerType());
		for (Response r : mpi.getAnnotations(Response.class))
			b.apply(r);
		return b.build();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Instance
	//-----------------------------------------------------------------------------------------------------------------

	private final ClassMeta<?> cm;
	private final Map<String,ResponseBeanPropertyMeta> properties;
	private final int code;
	private final Map<String,ResponseBeanPropertyMeta> headerMethods;
	private final ResponseBeanPropertyMeta statusMethod, bodyMethod;
	private final Optional<HttpPartSerializer> partSerializer;
	private final Optional<HttpPartParser> partParser;
	private final HttpPartSchema schema;

	ResponseBeanMeta(Builder b) {
		this.cm = b.cm;
		this.code = b.code;
		this.partSerializer = ofNullable(b.partSerializer).map(x -> HttpPartSerializer.creator().type(x).apply(b.annotations).create());
		this.partParser = ofNullable(b.partParser).map(x -> HttpPartParser.creator().type(x).apply(b.annotations).create());
		this.schema = b.schema.build();

		Map<String,ResponseBeanPropertyMeta> properties = new LinkedHashMap<>();

		Map<String,ResponseBeanPropertyMeta> hm = new LinkedHashMap<>();
		for (Map.Entry<String,ResponseBeanPropertyMeta.Builder> e : b.headerMethods.entrySet()) {
			ResponseBeanPropertyMeta pm = e.getValue().build(partSerializer, partParser);
			hm.put(e.getKey(), pm);
			properties.put(pm.getGetter().getName(), pm);
		}
		this.headerMethods = Collections.unmodifiableMap(hm);

		this.bodyMethod = b.bodyMethod == null ? null : b.bodyMethod.schema(schema).build(partSerializer, partParser);
		this.statusMethod = b.statusMethod == null ? null : b.statusMethod.build(empty(), empty());

		if (bodyMethod != null)
			properties.put(bodyMethod.getGetter().getName(), bodyMethod);
		if (statusMethod != null)
			properties.put(statusMethod.getGetter().getName(), statusMethod);

		this.properties = Collections.unmodifiableMap(properties);
	}

	static class Builder {
		ClassMeta<?> cm;
		int code;
		AnnotationWorkList annotations;
		Class<? extends HttpPartSerializer> partSerializer;
		Class<? extends HttpPartParser> partParser;
		HttpPartSchemaBuilder schema = HttpPartSchema.create();

		Map<String,ResponseBeanPropertyMeta.Builder> headerMethods = new LinkedHashMap<>();
		ResponseBeanPropertyMeta.Builder bodyMethod;
		ResponseBeanPropertyMeta.Builder statusMethod;

		Builder(AnnotationWorkList annotations) {
			this.annotations = annotations;
		}

		Builder apply(Type t) {
			Class<?> c = ClassUtils.toClass(t);
			this.cm = BeanContext.DEFAULT.getClassMeta(c);
			ClassInfo ci = cm.getInfo();
			for (MethodInfo m : ci.getAllMethods()) {
				if (m.isPublic()) {
					assertNoInvalidAnnotations(m, Header.class, Query.class, FormData.class, Path.class);
					if (m.hasAnnotation(ResponseHeader.class)) {
						assertNoArgs(m, ResponseHeader.class);
						assertReturnNotVoid(m, ResponseHeader.class);
						HttpPartSchema s = HttpPartSchema.create(m.getLastAnnotation(ResponseHeader.class), m.getPropertyName());
						headerMethods.put(s.getName(), ResponseBeanPropertyMeta.create(RESPONSE_HEADER, s, m));
					} else if (m.hasAnnotation(ResponseStatus.class)) {
						assertNoArgs(m, ResponseHeader.class);
						assertReturnType(m, ResponseHeader.class, int.class, Integer.class);
						statusMethod = ResponseBeanPropertyMeta.create(RESPONSE_STATUS, m);
					} else if (m.hasAnnotation(ResponseBody.class)) {
						if (m.getParamCount() == 0)
							assertReturnNotVoid(m, ResponseHeader.class);
						else
							assertArgType(m, ResponseHeader.class, OutputStream.class, Writer.class);
						bodyMethod = ResponseBeanPropertyMeta.create(RESPONSE_BODY, m);
					}
				}
			}
			return this;
		}

		Builder apply(Response a) {
			if (a != null) {
				if (a.serializer() != HttpPartSerializer.Null.class)
					partSerializer = a.serializer();
				if (a.parser() != HttpPartParser.Null.class)
					partParser = a.parser();
				if (a.value().length > 0)
					code = a.value()[0];
				if (a.code().length > 0)
					code = a.code()[0];
				schema.apply(a.schema());
			}
			return this;
		}

		ResponseBeanMeta build() {
			return new ResponseBeanMeta(this);
		}
	}

	/**
	 * Returns the HTTP status code.
	 *
	 * @return The HTTP status code.
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Returns the schema information about the response object.
	 *
	 * @return The schema information about the response object.
	 */
	public HttpPartSchema getSchema() {
		return schema;
	}

	/**
	 * Returns metadata about the <ja>@ResponseHeader</ja>-annotated methods.
	 *
	 * @return Metadata about the <ja>@ResponseHeader</ja>-annotated methods, or an empty collection if none exist.
	 */
	public Collection<ResponseBeanPropertyMeta> getHeaderMethods() {
		return headerMethods.values();
	}

	/**
	 * Returns the <ja>@ResponseBody</ja>-annotated method.
	 *
	 * @return The <ja>@ResponseBody</ja>-annotated method, or <jk>null</jk> if it doesn't exist.
	 */
	public ResponseBeanPropertyMeta getBodyMethod() {
		return bodyMethod;
	}

	/**
	 * Returns the <ja>@ResponseStatus</ja>-annotated method.
	 *
	 * @return The <ja>@ResponseStatus</ja>-annotated method, or <jk>null</jk> if it doesn't exist.
	 */
	public ResponseBeanPropertyMeta getStatusMethod() {
		return statusMethod;
	}

	/**
	 * Returns the part serializer to use to serialize this response.
	 *
	 * @return The part serializer to use to serialize this response.
	 */
	public Optional<HttpPartSerializer> getPartSerializer() {
		return partSerializer;
	}

	/**
	 * Returns metadata about the class.
	 *
	 * @return Metadata about the class.
	 */
	public ClassMeta<?> getClassMeta() {
		return cm;
	}

	/**
	 * Returns metadata about the bean property with the specified method getter name.
	 *
	 * @param name The bean method getter name.
	 * @return Metadata about the bean property, or <jk>null</jk> if none found.
	 */
	public ResponseBeanPropertyMeta getProperty(String name) {
		return properties.get(name);
	}

	/**
	 * Returns all the annotated methods on this bean.
	 *
	 * @return All the annotated methods on this bean.
	 */
	public Collection<ResponseBeanPropertyMeta> getProperties() {
		return properties.values();
	}
}
