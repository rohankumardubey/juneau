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
package org.apache.juneau.rest.args;

import static org.apache.juneau.internal.StringUtils.*;
import static java.util.Optional.*;

import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.http.annotation.*;
import org.apache.juneau.httppart.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.reflect.*;
import org.apache.juneau.rest.*;
import org.apache.juneau.rest.annotation.*;

/**
 * Resolves method parameters and parameter types annotated with {@link Header} on {@link RestOp}-annotated Java methods.
 *
 * <p>
 * The parameter value is resolved using <c><jv>call</jv>.{@link RestCall#getRestRequest() getRestRequest}().{@link RestRequest#getHeaders() getHeaders}()./c>
 * with a {@link HttpPartSchema schema} derived from the {@link Header} annotation.
 *
 * <p>
 * If the {@link Header#multi()} flag is set, then the data type can be a {@link Collection} or array.
 */
public class HeaderArg implements RestOpArg {
	private final HttpPartParser partParser;
	private final HttpPartSchema schema;
	private final boolean multi;
	private final String name;
	private final ClassInfo type;

	/**
	 * Static creator.
	 *
	 * @param paramInfo The Java method parameter being resolved.
	 * @param annotations The annotations to apply to any new part parsers.
	 * @return A new {@link HeaderArg}, or <jk>null</jk> if the parameter is not annotated with {@link Header}.
	 */
	public static HeaderArg create(ParamInfo paramInfo, AnnotationWorkList annotations) {
		if (paramInfo.hasAnnotation(Header.class) || paramInfo.getParameterType().hasAnnotation(Header.class))
			return new HeaderArg(paramInfo, annotations);
		return null;
	}

	/**
	 * Constructor.
	 *
	 * @param paramInfo The Java method parameter being resolved.
	 * @param annotations The annotations to apply to any new part parsers.
	 */
	protected HeaderArg(ParamInfo paramInfo, AnnotationWorkList annotations) {
		this.name = getName(paramInfo);
		this.type = paramInfo.getParameterType();
		this.schema = HttpPartSchema.create(Header.class, paramInfo);
		this.partParser = ofNullable(schema.getParser()).map(x -> HttpPartParser.creator().type(x).apply(annotations).create()).orElse(null);
		this.multi = getMulti(paramInfo);

		if (multi && ! type.isCollectionOrArray())
			throw new ArgException(paramInfo, "Use of multipart flag on @Header parameter that is not an array or Collection");
	}

	private String getName(ParamInfo paramInfo) {
		String n = null;
		for (Header h : paramInfo.getAnnotations(Header.class))
			n = firstNonEmpty(h.name(), h.n(), h.value(), n);
		for (Header h : paramInfo.getParameterType().getAnnotations(Header.class))
			n = firstNonEmpty(h.name(), h.n(), h.value(), n);
		if (n == null)
			throw new ArgException(paramInfo, "@Header used without name or value");
		return n;
	}

	private boolean getMulti(ParamInfo paramInfo) {
		for (Header h : paramInfo.getAnnotations(Header.class))
			if (h.multi())
				return true;
		for (Header h : paramInfo.getParameterType().getAnnotations(Header.class))
			if (h.multi())
				return true;
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override /* RestOpArg */
	public Object resolve(RestCall call) throws Exception {
		RestRequest req = call.getRestRequest();
		HttpPartParserSession ps = partParser == null ? req.getPartParserSession() : partParser.createPartSession(req.getParserSessionArgs());
		RequestHeaders rh = call.getRestRequest().getHeaders();
		BeanSession bs = call.getRestRequest().getBeanSession();
		ClassMeta<?> cm = bs.getClassMeta(type.innerType());

		if (multi) {
			Collection c = cm.isArray() ? new ArrayList<>() : (Collection)(cm.canCreateNewInstance() ? cm.newInstance() : new OList());
			rh.getAll(name).stream().map(x -> x.parser(ps).schema(schema).asType(cm.getElementType()).orElse(null)).forEach(x -> c.add(x));
			return cm.isArray() ? ArrayUtils.toArray(c, cm.getElementType().getInnerClass()) : c;
		}

		if (cm.isMapOrBean() && isOneOf(name, "*", "")) {
			OMap m = new OMap();
			for (RequestHeader e : rh.getAll())
				m.put(e.getName(), e.parser(ps).schema(schema == null ? null : schema.getProperty(e.getName())).asType(cm.getValueType()).orElse(null));
			return req.getBeanSession().convertToType(m, cm);
		}

		return rh.getLast(name).parser(ps).schema(schema).asType(type.innerType()).orElse(null);
	}
}
