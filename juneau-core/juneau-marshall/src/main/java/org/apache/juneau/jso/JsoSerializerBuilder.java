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
package org.apache.juneau.jso;

import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.http.*;
import org.apache.juneau.serializer.*;

/**
 * Builder class for building instances of Java Serialized Object serializers.
 */
public class JsoSerializerBuilder extends SerializerBuilder {

	/**
	 * Constructor, default settings.
	 */
	public JsoSerializerBuilder() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param ps The initial configuration settings for this builder.
	 */
	public JsoSerializerBuilder(PropertyStore ps) {
		super(ps);
	}

	@Override /* ContextBuilder */
	public JsoSerializer build() {
		return build(JsoSerializer.class);
	}


	//--------------------------------------------------------------------------------
	// Properties
	//--------------------------------------------------------------------------------

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder maxDepth(int value) {
		super.maxDepth(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder initialDepth(int value) {
		super.initialDepth(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder detectRecursions(boolean value) {
		super.detectRecursions(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder ignoreRecursions(boolean value) {
		super.ignoreRecursions(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder useWhitespace(boolean value) {
		super.useWhitespace(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder ws() {
		super.ws();
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder maxIndent(int value) {
		super.maxIndent(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder addBeanTypeProperties(boolean value) {
		super.addBeanTypeProperties(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder quoteChar(char value) {
		super.quoteChar(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder sq() {
		super.sq();
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder trimNullProperties(boolean value) {
		super.trimNullProperties(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder trimEmptyCollections(boolean value) {
		super.trimEmptyCollections(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder trimEmptyMaps(boolean value) {
		super.trimEmptyMaps(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder trimStrings(boolean value) {
		super.trimStrings(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder uriContext(UriContext value) {
		super.uriContext(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder uriResolution(UriResolution value) {
		super.uriResolution(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder uriRelativity(UriRelativity value) {
		super.uriRelativity(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder sortCollections(boolean value) {
		super.sortCollections(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder sortMaps(boolean value) {
		super.sortMaps(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder abridged(boolean value) {
		super.abridged(value);
		return this;
	}

	@Override /* SerializerBuilder */
	public JsoSerializerBuilder listener(Class<? extends SerializerListener> value) {
		super.listener(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder beansRequireDefaultConstructor(boolean value) {
		super.beansRequireDefaultConstructor(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder beansRequireSerializable(boolean value) {
		super.beansRequireSerializable(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder beansRequireSettersForGetters(boolean value) {
		super.beansRequireSettersForGetters(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder beansRequireSomeProperties(boolean value) {
		super.beansRequireSomeProperties(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder beanMapPutReturnsOldValue(boolean value) {
		super.beanMapPutReturnsOldValue(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder beanConstructorVisibility(Visibility value) {
		super.beanConstructorVisibility(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder beanClassVisibility(Visibility value) {
		super.beanClassVisibility(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder beanFieldVisibility(Visibility value) {
		super.beanFieldVisibility(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder methodVisibility(Visibility value) {
		super.methodVisibility(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder useJavaBeanIntrospector(boolean value) {
		super.useJavaBeanIntrospector(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder useInterfaceProxies(boolean value) {
		super.useInterfaceProxies(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder ignoreUnknownBeanProperties(boolean value) {
		super.ignoreUnknownBeanProperties(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder ignoreUnknownNullBeanProperties(boolean value) {
		super.ignoreUnknownNullBeanProperties(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder ignorePropertiesWithoutSetters(boolean value) {
		super.ignorePropertiesWithoutSetters(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder ignoreInvocationExceptionsOnGetters(boolean value) {
		super.ignoreInvocationExceptionsOnGetters(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder ignoreInvocationExceptionsOnSetters(boolean value) {
		super.ignoreInvocationExceptionsOnSetters(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder sortProperties(boolean value) {
		super.sortProperties(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder notBeanPackages(String...values) {
		super.notBeanPackages(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder notBeanPackages(Collection<String> values) {
		super.notBeanPackages(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder setNotBeanPackages(String...values) {
		super.setNotBeanPackages(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder setNotBeanPackages(Collection<String> values) {
		super.setNotBeanPackages(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder removeNotBeanPackages(String...values) {
		super.removeNotBeanPackages(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder removeNotBeanPackages(Collection<String> values) {
		super.removeNotBeanPackages(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder notBeanClasses(Class<?>...values) {
		super.notBeanClasses(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder notBeanClasses(Collection<Class<?>> values) {
		super.notBeanClasses(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder setNotBeanClasses(Class<?>...values) {
		super.setNotBeanClasses(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder setNotBeanClasses(Collection<Class<?>> values) {
		super.setNotBeanClasses(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder removeNotBeanClasses(Class<?>...values) {
		super.removeNotBeanClasses(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder removeNotBeanClasses(Collection<Class<?>> values) {
		super.removeNotBeanClasses(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder beanFilters(Class<?>...values) {
		super.beanFilters(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder beanFilters(Collection<Class<?>> values) {
		super.beanFilters(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder setBeanFilters(Class<?>...values) {
		super.setBeanFilters(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder setBeanFilters(Collection<Class<?>> values) {
		super.setBeanFilters(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder removeBeanFilters(Class<?>...values) {
		super.removeBeanFilters(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder removeBeanFilters(Collection<Class<?>> values) {
		super.removeBeanFilters(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder pojoSwaps(Class<?>...values) {
		super.pojoSwaps(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder pojoSwaps(Collection<Class<?>> values) {
		super.pojoSwaps(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder setPojoSwaps(Class<?>...values) {
		super.setPojoSwaps(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder setPojoSwaps(Collection<Class<?>> values) {
		super.setPojoSwaps(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder removePojoSwaps(Class<?>...values) {
		super.removePojoSwaps(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder removePojoSwaps(Collection<Class<?>> values) {
		super.removePojoSwaps(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder implClasses(Map<String,Class<?>> values) {
		super.implClasses(values);
		return this;
	}

	@Override /* ContextBuilder */
	public <T> JsoSerializerBuilder implClass(Class<T> interfaceClass, Class<? extends T> implClass) {
		super.implClass(interfaceClass, implClass);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder includeProperties(Map<String,String> values) {
		super.includeProperties(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder includeProperties(String beanClassName, String properties) {
		super.includeProperties(beanClassName, properties);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder includeProperties(Class<?> beanClass, String properties) {
		super.includeProperties(beanClass, properties);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder excludeProperties(Map<String,String> values) {
		super.excludeProperties(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder excludeProperties(String beanClassName, String properties) {
		super.excludeProperties(beanClassName, properties);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder excludeProperties(Class<?> beanClass, String properties) {
		super.excludeProperties(beanClass, properties);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder beanDictionary(Class<?>...values) {
		super.beanDictionary(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder beanDictionary(Collection<Class<?>> values) {
		super.beanDictionary(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder setBeanDictionary(Class<?>...values) {
		super.setBeanDictionary(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder setBeanDictionary(Collection<Class<?>> values) {
		super.setBeanDictionary(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder removeFromBeanDictionary(Class<?>...values) {
		super.removeFromBeanDictionary(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder removeFromBeanDictionary(Collection<Class<?>> values) {
		super.removeFromBeanDictionary(values);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder beanTypePropertyName(String value) {
		super.beanTypePropertyName(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder defaultParser(Class<?> value) {
		super.defaultParser(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder locale(Locale value) {
		super.locale(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder timeZone(TimeZone value) {
		super.timeZone(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder mediaType(MediaType value) {
		super.mediaType(value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder debug() {
		super.debug();
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder set(String name, Object value) {
		super.set(name, value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder set(Map<String,Object> properties) {
		super.set(properties);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder add(Map<String,Object> properties) {
		super.add(properties);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder addTo(String name, Object value) {
		super.addTo(name, value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder addTo(String name, String key, Object value) {
		super.addTo(name, key, value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder removeFrom(String name, Object value) {
		super.removeFrom(name, value);
		return this;
	}

	@Override /* ContextBuilder */
	public JsoSerializerBuilder apply(PropertyStore copyFrom) {
		super.apply(copyFrom);
		return this;
	}
}