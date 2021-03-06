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
package org.apache.juneau.urlencoding.annotation;

import org.apache.juneau.*;
import org.apache.juneau.reflect.*;
import org.apache.juneau.svl.*;
import org.apache.juneau.urlencoding.*;

/**
 * Utility classes and methods for the {@link UrlEncodingConfig @UrlEncodingConfig} annotation.
 */
public class UrlEncodingConfigAnnotation {

	/**
	 * Applies {@link UrlEncodingConfig} annotations to a {@link ContextPropertiesBuilder}.
	 */
	public static class Apply extends AnnotationApplier<UrlEncodingConfig,ContextPropertiesBuilder> {

		/**
		 * Constructor.
		 *
		 * @param vr The resolver for resolving values in annotations.
		 */
		public Apply(VarResolverSession vr) {
			super(UrlEncodingConfig.class, ContextPropertiesBuilder.class, vr);
		}

		@Override
		public void apply(AnnotationInfo<UrlEncodingConfig> ai, ContextPropertiesBuilder b) {
			UrlEncodingConfig a = ai.getAnnotation();

			bool(a.expandedParams()).ifPresent(x -> b.set(UrlEncodingSerializer.URLENC_expandedParams, x));
			bool(a.expandedParams()).ifPresent(x -> b.set(UrlEncodingParser.URLENC_expandedParams, x));
		}
	}
}