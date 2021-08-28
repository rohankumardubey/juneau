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

import org.apache.juneau.*;
import org.apache.juneau.http.annotation.*;
import org.apache.juneau.reflect.*;
import org.apache.juneau.rest.*;
import org.apache.juneau.rest.annotation.*;

/**
 * Resolves method parameters annotated with {@link ResponseStatus} on {@link RestOp}-annotated Java methods.
 */
public class ResponseStatusArg implements RestOpArg {

	private final Type type;

	/**
	 * Static creator.
	 *
	 * @param paramInfo The Java method parameter being resolved.
	 * @return A new {@link ResponseStatusArg}, or <jk>null</jk> if the parameter is not annotated with {@link ResponseStatus}.
	 */
	public static ResponseStatusArg create(ParamInfo paramInfo) {
		if (paramInfo.hasAnnotation(ResponseStatus.class) || paramInfo.getParameterType().hasAnnotation(ResponseStatus.class))
			return new ResponseStatusArg(paramInfo);
		return null;
	}

	/**
	 * Constructor.
	 *
	 * @param paramInfo The Java method parameter being resolved.
	 */
	protected ResponseStatusArg(ParamInfo paramInfo) {
		this.type = paramInfo.getParameterType().innerType();
		Class<?> c = type instanceof Class ? (Class<?>)type : type instanceof ParameterizedType ? (Class<?>)((ParameterizedType)type).getRawType() : null;
		if (c != Value.class || Value.getParameterType(type) != Integer.class)
			throw new ArgException(paramInfo, "Type must be Value<Integer> on parameter annotated with @ResponseStatus annotation");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override /* RestOpArg */
	public Object resolve(final RestCall call) throws Exception {
		Value<Object> v = new Value();
		v.listener(new ValueListener() {
			@Override
			public void onSet(Object o) {
				call.getRestResponse().setStatus(Integer.parseInt(o.toString()));
			}
		});
		return v;
	}
}
