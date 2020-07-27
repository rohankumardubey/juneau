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
package org.apache.juneau.rest.annotation;

import static org.junit.runners.MethodSorters.*;

import java.util.*;

import org.apache.juneau.collections.*;
import org.apache.juneau.cp.*;
import org.apache.juneau.http.annotation.*;
import org.apache.juneau.rest.mock2.*;
import org.junit.*;

@FixMethodOrder(NAME_ASCENDING)
public class Rest_Messages_Test {

	//------------------------------------------------------------------------------------------------------------------
	// Basic tests
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class A1 {
		@RestMethod
		public OMap a(ResourceBundle rb) {
			return asMap(rb);
		}
		@RestMethod
		public OMap b(Messages m) {
			return asMap(m);
		}
		@RestMethod
		public String c(Messages m, @Query("name") String name) {
			return m.getString(name);
		}
	}
	static MockRestClient a1 = MockRestClient.build(A1.class);

	@Test
	public void a01_default() throws Exception {
		a1.get("/a").run().assertBody().is("{'A1.key2':'A1.value2a',key1:'value1a',key2:'A1.value2a'}");
		a1.get("/b").run().assertBody().is("{'A1.key2':'A1.value2a',key1:'value1a',key2:'A1.value2a'}");
		a1.get("/c?name=key1").run().assertBody().is("value1a");
		a1.get("/c?name=key2").run().assertBody().is("A1.value2a");
		a1.get("/c?name=key3").run().assertBody().is("{!key3}");
	}

	@Rest
	public static class A2 extends A1 {}
	static MockRestClient a2 = MockRestClient.build(A2.class);

	@Test
	public void a02_subclassed() throws Exception {
		a2.get("/a").run().assertBody().is("{'A1.key2':'A1.value2a','A2.key3':'A2.value3b',key1:'value1a',key2:'value2b',key3:'A2.value3b'}");
		a2.get("/b").run().assertBody().is("{'A1.key2':'A1.value2a','A2.key3':'A2.value3b',key1:'value1a',key2:'value2b',key3:'A2.value3b'}");
		a2.get("/c?name=key1").run().assertBody().is("value1a");
		a2.get("/c?name=key2").run().assertBody().is("value2b");
		a2.get("/c?name=key3").run().assertBody().is("A2.value3b");
		a2.get("/c?name=key4").run().assertBody().is("{!key4}");
	}

	//------------------------------------------------------------------------------------------------------------------
	// Overridden on subclass.
	//------------------------------------------------------------------------------------------------------------------

	@Rest(messages="B1x")
	public static class B1 {
		@RestMethod
		public OMap a(ResourceBundle rb) {
			return asMap(rb);
		}
		@RestMethod
		public OMap b(Messages m) {
			return asMap(m);
		}
		@RestMethod
		public String c(Messages m, @Query("name") String name) {
			return m.getString(name);
		}
	}
	static MockRestClient b1 = MockRestClient.build(B1.class);

	@Test
	public void b01_customName() throws Exception {
		b1.get("/a").run().assertBody().is("{'B1.key2':'B1.value2a',key1:'value1a',key2:'B1.value2a'}");
		b1.get("/b").run().assertBody().is("{'B1.key2':'B1.value2a',key1:'value1a',key2:'B1.value2a'}");
		b1.get("/c?name=key1").run().assertBody().is("value1a");
		b1.get("/c?name=key2").run().assertBody().is("B1.value2a");
		b1.get("/c?name=key3").run().assertBody().is("{!key3}");
	}

	@Rest(messages="B2x")
	public static class B2 extends B1 {}
	static MockRestClient b2 = MockRestClient.build(B2.class);

	@Test
	public void b02_subclassed_customName() throws Exception {
		b2.get("/a").run().assertBody().stderr().is("{'B1.key2':'B1.value2a','B2.key3':'B2.value3b',key1:'value1a',key2:'value2b',key3:'B2.value3b'}");
		b2.get("/b").run().assertBody().is("{'B1.key2':'B1.value2a','B2.key3':'B2.value3b',key1:'value1a',key2:'value2b',key3:'B2.value3b'}");
		b2.get("/c?name=key1").run().assertBody().is("value1a");
		b2.get("/c?name=key2").run().assertBody().is("value2b");
		b2.get("/c?name=key3").run().assertBody().is("B2.value3b");
		b2.get("/c?name=key4").run().assertBody().is("{!key4}");
	}

	//------------------------------------------------------------------------------------------------------------------
	// Helper methods.
	//------------------------------------------------------------------------------------------------------------------

	private static OMap asMap(ResourceBundle rb) {
		OMap m = new OMap();
		for (String k : new TreeSet<>(rb.keySet()))
			m.put(k, rb.getString(k));
		return m;
	}
}
