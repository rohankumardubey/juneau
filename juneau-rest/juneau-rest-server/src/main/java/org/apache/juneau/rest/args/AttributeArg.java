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

import org.apache.juneau.reflect.*;
import org.apache.juneau.rest.*;
import org.apache.juneau.rest.annotation.*;

/**
 * Resolves method parameters annotated with {@link Attr} on {@link RestOp}-annotated Java methods.
 *
 * <p>
 * The parameter value is resolved using <c><jv>call</jv>.{@link RestCall#getRestRequest() getRestRequest}().{@link RestRequest#getAttributes() getAttributes}().{@link RequestAttributes#get(String) get}(<jv>name</jv>).{@link RequestAttribute#asType(Class) asType}(<jv>type</jv>.
 */
public class AttributeArg implements RestOpArg {

	private final String name;
	private final Class<?> type;

	/**
	 * Static creator.
	 *
	 * @param paramInfo The Java method parameter being resolved.
	 * @return A new {@link AttributeArg}, or <jk>null</jk> if the parameter is not annotated with {@link Attr}.
	 */
	public static AttributeArg create(ParamInfo paramInfo) {
		if (paramInfo.hasAnnotation(Attr.class) || paramInfo.getParameterType().hasAnnotation(Attr.class))
			return new AttributeArg(paramInfo);
		return null;
	}

	/**
	 * Constructor.
	 *
	 * @param paramInfo The Java method parameter being resolved.
	 */
	protected AttributeArg(ParamInfo paramInfo) {
		this.name = getName(paramInfo);
		this.type = paramInfo.getParameterType().inner();
	}

	private String getName(ParamInfo paramInfo) {
		String n = null;
		for (Attr h : paramInfo.getAnnotations(Attr.class))
			n = firstNonEmpty(h.name(), h.value(), n);
		for (Attr h : paramInfo.getParameterType().getAnnotations(Attr.class))
			n = firstNonEmpty(h.name(), h.value(), n);
		if (n == null)
			throw new ArgException(paramInfo, "@Attr used without name or value");
		return n;
	}

	@Override /* RestOpArg */
	public Object resolve(RestCall call) throws Exception {
		return call.getRestRequest().getAttribute(name).asType(type).orElse(null);
	}
}
