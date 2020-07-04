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
package org.apache.juneau.rest.client.remote;

import static org.apache.juneau.internal.StringUtils.*;
import java.lang.reflect.*;
import java.util.*;

import org.apache.http.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.http.*;
import org.apache.juneau.http.header.*;
import org.apache.juneau.http.remote.*;
import org.apache.juneau.reflect.*;

/**
 * Contains the meta-data about a REST proxy class.
 *
 * <p>
 * Captures the information in {@link org.apache.juneau.http.remote.Remote @Remote} and {@link org.apache.juneau.http.remote.RemoteMethod @RemoteMethod} annotations for
 * caching and reuse.
 *
 * <ul class='seealso'>
 * 	<li class='link'>{@doc juneau-rest-client.RestProxies}
 * </ul>
 */
public class RemoteMeta {

	private final Map<Method,RemoteMethodMeta> methods;
	private final HeaderSupplier headerSupplier = HeaderSupplier.create();

	/**
	 * Constructor.
	 *
	 * @param c The interface class annotated with a {@link org.apache.juneau.http.remote.Remote @Remote} annotation (optional).
	 */
	@SuppressWarnings("deprecation")
	public RemoteMeta(Class<?> c) {
		String path = "";

		ClassInfo ci = ClassInfo.of(c);
		for (RemoteResource r : ci.getAnnotations(RemoteResource.class))
			if (! r.path().isEmpty())
				path = trimSlashes(r.path());
		for (org.apache.juneau.http.remote.RemoteResource r : ci.getAnnotations(org.apache.juneau.http.remote.RemoteResource.class))
			if (! r.path().isEmpty())
				path = trimSlashes(r.path());
		for (Remote r : ci.getAnnotations(Remote.class)) {
			if (! r.path().isEmpty())
				path = trimSlashes(r.path());
			for (String h : r.headers())
				headerSupplier.add(BasicHeader.ofPair(h));
			if (! r.version().isEmpty())
				headerSupplier.add(ClientVersion.of(r.version()));
			if (r.headerSupplier() != HeaderSupplier.Null.class) {
				try {
					headerSupplier.add(r.headerSupplier().newInstance());
				} catch (Exception e) {
					throw new RuntimeException("Could not instantiate HeaderSupplier class.", e);
				}
			}
		}

		AMap<Method,RemoteMethodMeta> methods = AMap.of();
		for (MethodInfo m : ci.getPublicMethods())
			methods.put(m.inner(), new RemoteMethodMeta(path, m.inner(), "GET"));

		this.methods = methods.unmodifiable();
	}

	/**
	 * Returns the metadata about the specified method on this resource proxy.
	 *
	 * @param m The method to look up.
	 * @return Metadata about the method or <jk>null</jk> if no metadata was found.
	 */
	public RemoteMethodMeta getMethodMeta(Method m) {
		return methods.get(m);
	}

	/**
	 * Returns the headers to set on all requests.
	 *
	 * @return The headers to set on all requests.
	 */
	public Iterable<Header> getHeaders() {
		return headerSupplier;
	}
}
