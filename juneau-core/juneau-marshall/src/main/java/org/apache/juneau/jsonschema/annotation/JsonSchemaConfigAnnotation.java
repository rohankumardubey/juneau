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
package org.apache.juneau.jsonschema.annotation;

import static org.apache.juneau.BeanTraverseContext.*;
import static org.apache.juneau.jsonschema.JsonSchemaGenerator.*;

import org.apache.juneau.*;
import org.apache.juneau.reflect.*;
import org.apache.juneau.svl.*;

/**
 * Utility classes and methods for the {@link JsonSchemaConfig @JsonSchemaConfig} annotation.
 */
public class JsonSchemaConfigAnnotation {

	/**
	 * Applies {@link JsonSchemaConfig} annotations to a {@link ContextPropertiesBuilder}.
	 */
	public static class Apply extends AnnotationApplier<JsonSchemaConfig,ContextPropertiesBuilder> {

		/**
		 * Constructor.
		 *
		 * @param vr The resolver for resolving values in annotations.
		 */
		public Apply(VarResolverSession vr) {
			super(JsonSchemaConfig.class, ContextPropertiesBuilder.class, vr);
		}

		@Override
		public void apply(AnnotationInfo<JsonSchemaConfig> ai, ContextPropertiesBuilder b) {
			JsonSchemaConfig a = ai.getAnnotation();

			string(a.addDescriptionsTo()).ifPresent(x -> b.set(JSONSCHEMA_addDescriptionsTo, x));
			string(a.addExamplesTo()).ifPresent(x -> b.set(JSONSCHEMA_addExamplesTo, x));
			bool(a.allowNestedDescriptions()).ifPresent(x -> b.set(JSONSCHEMA_allowNestedDescriptions, x));
			bool(a.allowNestedExamples()).ifPresent(x -> b.set(JSONSCHEMA_allowNestedExamples, x));
			type(a.beanDefMapper()).ifPresent(x -> b.set(JSONSCHEMA_beanDefMapper, x));
			string(a.ignoreTypes()).ifPresent(x -> b.set(JSONSCHEMA_ignoreTypes, x));
			bool(a.useBeanDefs()).ifPresent(x -> b.set(JSONSCHEMA_useBeanDefs, x));
			bool(a.detectRecursions()).ifPresent(x -> b.set(BEANTRAVERSE_detectRecursions, x));
			bool(a.ignoreRecursions()).ifPresent(x -> b.set(BEANTRAVERSE_ignoreRecursions, x));
			integer(a.initialDepth(), "initialDepth").ifPresent(x -> b.set(BEANTRAVERSE_initialDepth, x));
			integer(a.maxDepth(), "maxDepth").ifPresent(x -> b.set(BEANTRAVERSE_maxDepth, x));
		}
	}
}