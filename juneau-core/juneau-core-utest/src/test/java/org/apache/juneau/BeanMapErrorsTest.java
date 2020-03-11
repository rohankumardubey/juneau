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
package org.apache.juneau;

import static org.junit.Assert.*;

import java.util.stream.*;

import org.apache.juneau.annotation.*;
import org.junit.*;

/**
 * Tests various error conditions when defining beans.
 */
public class BeanMapErrorsTest {

	//-----------------------------------------------------------------------------------------------------------------
	// @Beanp(name) on method not in @Bean(properties)
	// Shouldn't be found in keySet()/entrySet() but should be found in containsKey()/get()
	//-----------------------------------------------------------------------------------------------------------------
	@Test
	public void beanPropertyMethodNotInBeanProperties() {
		BeanContext bc = BeanContext.DEFAULT;

		BeanMap<A1> bm = bc.createBeanSession().newBeanMap(A1.class);
		assertTrue(bm.containsKey("f2"));
		assertEquals(-1, bm.get("f2"));
		bm.put("f2", -2);
		assertEquals(-2, bm.get("f2"));
		assertFalse(bm.keySet().contains("f2"));
		assertFalse(bm.entrySet().stream().map(x -> x.getKey()).collect(Collectors.toList()).contains("f2"));
	}

	@Bean(bpi="f1")
	public static class A1 {
		public int f1;
		private int f2 = -1;

		@Beanp("f2")
		public int f2() {
			return f2;
		};

		public void setF2(int f2) {
			this.f2 = f2;
		}
	}

	@Test
	public void beanPropertyMethodNotInBeanProperties_usingConfig() {
		BeanContext bc = BeanContext.create().applyAnnotations(B1.class).build();

		BeanMap<B1> bm = bc.createBeanSession().newBeanMap(B1.class);
		assertTrue(bm.containsKey("f2"));
		assertEquals(-1, bm.get("f2"));
		bm.put("f2", -2);
		assertEquals(-2, bm.get("f2"));
		assertFalse(bm.keySet().contains("f2"));
		assertFalse(bm.entrySet().stream().map(x -> x.getKey()).collect(Collectors.toList()).contains("f2"));
	}

	@BeanConfig(
		applyBean={
			@Bean(on="B1", bpi="f1"),
		},
		applyBeanp={
			@Beanp(on="B1.f2", value="f2")
		}
	)
	public static class B1 {
		public int f1;
		private int f2 = -1;

		@Beanp("f2")
		public int f2() {
			return f2;
		};

		public void setF2(int f2) {
			this.f2 = f2;
		}
	}

	//-----------------------------------------------------------------------------------------------------------------
	// @Beanp(name) on field not in @Bean(properties)
	//-----------------------------------------------------------------------------------------------------------------
	@Test
	public void beanPropertyFieldNotInBeanProperties() {
		BeanContext bc = BeanContext.DEFAULT;

		BeanMap<A2> bm = bc.createBeanSession().newBeanMap(A2.class);
		assertTrue(bm.containsKey("f2"));
		assertEquals(-1, bm.get("f2"));
		bm.put("f2", -2);
		assertEquals(-2, bm.get("f2"));
		assertFalse(bm.keySet().contains("f2"));
		assertFalse(bm.entrySet().stream().map(x -> x.getKey()).collect(Collectors.toList()).contains("f2"));
	}

	@Bean(bpi="f1")
	public static class A2 {
		public int f1;

		@Beanp("f2")
		public int f2 = -1;
	}

	@Test
	public void beanPropertyFieldNotInBeanProperties_usingBeanConfig() {
		BeanContext bc = BeanContext.create().applyAnnotations(B2.class).build();

		BeanMap<B2> bm = bc.createBeanSession().newBeanMap(B2.class);
		assertTrue(bm.containsKey("f2"));
		assertEquals(-1, bm.get("f2"));
		bm.put("f2", -2);
		assertEquals(-2, bm.get("f2"));
		assertFalse(bm.keySet().contains("f2"));
		assertFalse(bm.entrySet().stream().map(x -> x.getKey()).collect(Collectors.toList()).contains("f2"));
	}

	@BeanConfig(
		applyBean={
			@Bean(on="B2", bpi="f1")
		},
		applyBeanp={
			@Beanp(on="B2.f2", value="f2")
		}
	)
	public static class B2 {
		public int f1;

		public int f2 = -1;
	}
}
