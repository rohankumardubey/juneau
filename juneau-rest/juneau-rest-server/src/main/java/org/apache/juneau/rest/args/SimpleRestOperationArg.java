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

import java.util.function.*;

import org.apache.juneau.rest.*;
import org.apache.juneau.utils.*;

/**
 * An implementation of a {@link RestOpArg} that takes in a {@link ThrowingFunction} for resolving a parameter value.
 */
public class SimpleRestOperationArg implements RestOpArg {

	private final Function<RestCall,Object> function;

	/**
	 * Constructor.
	 *
	 * @param function The function to use to retrieve the parameter value from the {@link RestCall}.
	 */
	@SuppressWarnings("unchecked")
	protected <T> SimpleRestOperationArg(ThrowingFunction<RestCall,T> function) {
		this.function = (Function<RestCall,Object>)function;
	}

	@Override /* RestOpArg */
	public Object resolve(RestCall call) throws Exception {
		return function.apply(call);
	}
}
