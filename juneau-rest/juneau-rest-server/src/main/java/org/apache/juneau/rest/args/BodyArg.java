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

import java.lang.reflect.*;

import org.apache.juneau.http.annotation.*;
import org.apache.juneau.httppart.*;
import org.apache.juneau.reflect.*;
import org.apache.juneau.rest.*;
import org.apache.juneau.rest.annotation.*;

/**
 * Resolves method parameters and parameter types annotated with {@link Body} on {@link RestOp}-annotated Java methods.
 *
 * <p>
 * The parameter value is resolved using <c><jv>call</jv>.{@link RestCall#getRestRequest() getRestRequest}().{@link RestRequest#getBody() getBody}().{@link RequestBody#schema(HttpPartSchema) schema}(<jv>schema</jv>).{@link RequestBody#asType(Type,Type...) asType}(<jv>type</jv>)</c>.
 * with a {@link HttpPartSchema schema} derived from the {@link Body} annotation.
 */
public class BodyArg implements RestOpArg {

	private final HttpPartSchema schema;
	private final Type type;

	/**
	 * Static creator.
	 *
	 * @param paramInfo The Java method parameter being resolved.
	 * @return A new {@link BodyArg}, or <jk>null</jk> if the parameter is not annotated with {@link Body}.
	 */
	public static BodyArg create(ParamInfo paramInfo) {
		if (paramInfo.hasAnnotation(Body.class) || paramInfo.getParameterType().hasAnnotation(Body.class))
			return new BodyArg(paramInfo);
		return null;
	}

	/**
	 * Constructor.
	 *
	 * @param paramInfo The Java method parameter being resolved.
	 */
	protected BodyArg(ParamInfo paramInfo) {
		this.type = paramInfo.getParameterType().innerType();
		this.schema = HttpPartSchema.create(Body.class, paramInfo);
	}

	@Override /* RestOpArg */
	public Object resolve(RestCall call) throws Exception {
		return call.getRestRequest().getBody().schema(schema).asType(type);
	}
}
