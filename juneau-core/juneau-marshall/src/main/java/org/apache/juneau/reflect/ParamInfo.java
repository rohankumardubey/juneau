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
package org.apache.juneau.reflect;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;

/**
 * Lightweight utility class for introspecting information about a method parameter.
 */
public final class ParamInfo {

	private final ExecutableInfo eInfo;
	private final Parameter p;
	private final int index;
	private volatile Map<Class<?>,Optional<Annotation>> annotationMap;

	//-----------------------------------------------------------------------------------------------------------------
	// Instantiation
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param eInfo The constructor or method wrapper.
	 * @param p The parameter being wrapped.
	 * @param index The parameter index.
	 */
	protected ParamInfo(ExecutableInfo eInfo, Parameter p, int index) {
		this.eInfo = eInfo;
		this.p = p;
		this.index = index;
	}

	/**
	 * Returns the index position of this parameter.
	 *
	 * @return The index position of this parameter.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Returns the method that this parameter belongs to.
	 *
	 * @return The method that this parameter belongs to, or <jk>null</jk> if it belongs to a constructor.
	 */
	public MethodInfo getMethod() {
		return eInfo.isConstructor() ? null : (MethodInfo)eInfo;
	}

	/**
	 * Returns the constructor that this parameter belongs to.
	 *
	 * @return The constructor that this parameter belongs to, or <jk>null</jk> if it belongs to a method.
	 */
	public ConstructorInfo getConstructor() {
		return eInfo.isConstructor() ? (ConstructorInfo)eInfo : null;
	}

	/**
	 * Returns the class type of this parameter.
	 *
	 * @return The class type of this parameter.
	 */
	public ClassInfo getParameterType() {
		return eInfo.getParamType(index);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Annotations
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the parameter annotations declared on this parameter.
	 *
	 * @return The parameter annotations declared on this parameter, or an empty array if none found.
	 */
	public Annotation[] getDeclaredAnnotations() {
		return eInfo.getParameterAnnotations(index);
	}

	/**
	 * Returns the specified parameter annotation declared on this parameter.
	 *
	 * @param a
	 * 	The annotation to search for.
	 * @param <T>
	 * 	The annotation type.
	 * @return The specified parameter annotation declared on this parameter, or <jk>null</jk> if not found.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getDeclaredAnnotation(Class<T> a) {
		if (a != null)
			for (Annotation aa : eInfo.getParameterAnnotations(index))
				if (a.isInstance(aa))
					return (T)aa;
		return null;
	}

	/**
	 * Finds the annotation of the specified type defined on this method parameter.
	 *
	 * <p>
	 * If the annotation cannot be found on the immediate method, searches methods with the same
	 * signature on the parent classes or interfaces.
	 * <br>The search is performed in child-to-parent order.
	 *
	 * <p>
	 * If still not found, searches for the annotation on the return type of the method.
	 *
	 * @param a
	 * 	The annotation to search for.
	 * @return
	 * 	The annotation if found, or <jk>null</jk> if not.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getLastAnnotation(Class<T> a) {
		Optional<Annotation> o = annotationMap().get(a);
		if (o == null) {
			o = Optional.ofNullable(findAnnotation(a));
			annotationMap().put(a, o);
		}
		return o.isPresent() ? (T)o.get() : null;
	}

	/**
	 * Returns <jk>true</jk> if this parameter has the specified annotation.
	 *
	 * @param a
	 * 	The annotation to search for.
	 * @return
	 * 	The <jk>true</jk> if annotation if found.
	 */
	public boolean hasAnnotation(Class<? extends Annotation> a) {
		return getLastAnnotation(a) != null;
	}

	@SuppressWarnings("unchecked")
	private <T extends Annotation> T findAnnotation(Class<T> a) {
		if (eInfo.isConstructor()) {
			for (Annotation a2 : eInfo.getParameterAnnotations(index))
				if (a.isInstance(a2))
					return (T)a2;
			return eInfo.getParamType(index).unwrap(Value.class,Optional.class).getLastAnnotation(a);
		}
		MethodInfo mi = (MethodInfo)eInfo;
		for (Method m2 : mi.getMatching())
			for (Annotation a2 :  m2.getParameterAnnotations()[index])
				if (a.isInstance(a2))
					return (T)a2;
		return eInfo.getParamType(index).unwrap(Value.class,Optional.class).getLastAnnotation(a);
	}

	/**
	 * Returns all annotations of the specified type defined on this method parameter.
	 *
	 * <p>
	 * Searches all methods with the same signature on the parent classes or interfaces
	 * and the return type on the method.
	 * <p>
	 * Results are in parent-to-child order.
	 *
	 * @param a
	 * 	The annotation to search for.
	 * @return
	 * 	A list of all matching annotations found or an empty list if none found.
	 */
	public <T extends Annotation> List<T> getAnnotations(Class<T> a) {
		return appendAnnotations(new ArrayList<>(), a, true);
	}

	@SuppressWarnings("unchecked")
	private <T extends Annotation> List<T> appendAnnotations(List<T> l, Class<T> a, boolean parentFirst) {
		if (eInfo.isConstructor) {
			ClassInfo ci = eInfo.getParamType(index).unwrap(Value.class,Optional.class);
			Annotation[] annotations = eInfo.getParameterAnnotations(index);
			if (parentFirst) {
				ci.appendAnnotations(l, a);
				for (Annotation a2 : annotations)
					if (a.isInstance(a2))
						l.add((T)a2);
			} else {
				for (Annotation a2 : annotations)
					if (a.isInstance(a2))
						l.add((T)a2);
				ci.appendAnnotations(l, a);
			}
		} else {
			MethodInfo mi = (MethodInfo)eInfo;
			ClassInfo ci = eInfo.getParamType(index).unwrap(Value.class,Optional.class);
			if (parentFirst) {
				ci.appendAnnotations(l, a);
				for (Method m2 : mi.getMatchingParentFirst())
					for (Annotation a2 :  m2.getParameterAnnotations()[index])
						if (a.isInstance(a2))
							l.add((T)a2);
			} else {
				for (Method m2 : mi.getMatching())
					for (Annotation a2 :  m2.getParameterAnnotations()[index])
						if (a.isInstance(a2))
							l.add((T)a2);
				ci.appendAnnotations(l, a);
			}
		}
		return l;
	}

	private synchronized Map<Class<?>,Optional<Annotation>> annotationMap() {
		if (annotationMap == null)
			annotationMap = new ConcurrentHashMap<>();
		return annotationMap;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Other methods
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Returns <jk>true</jk> if the parameter type is an exact match for the specified class.
	 *
	 * @param c The type to check.
	 * @return <jk>true</jk> if the parameter type is an exact match for the specified class.
	 */
	public boolean isType(Class<?> c) {
		return getParameterType().is(c);
	}

	/**
	 * Returns <jk>true</jk> if the parameter has a name provided by the class file.
	 *
	 * @return <jk>true</jk> if the parameter has a name provided by the class file.
	 */
	public boolean hasName() {
		return p.isNamePresent() || p.isAnnotationPresent(Name.class);
	}

	/**
	 * Returns the name of the parameter.
	 *
	 * <p>
	 * If the parameter's name is present, then this method returns the name provided by the class file.
	 * Otherwise, this method synthesizes a name of the form argN, where N is the index of the parameter in the descriptor of the method which declares the parameter.
	 *
	 * @return The name of the parameter.
	 * @see Parameter#getName()
	 */
	public String getName() {
		Name n = p.getAnnotation(Name.class);
		if (n != null)
			return n.value();
		if (p.isNamePresent())
			return p.getName();
		return null;
	}

	@Override
	public String toString() {
		return (eInfo.getSimpleName()) + "[" + index + "]";
	}
}
