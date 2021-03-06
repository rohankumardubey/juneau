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
package org.apache.juneau.assertions;

import static org.apache.juneau.assertions.AssertionPredicates.*;
import static org.apache.juneau.assertions.Assertions.*;
import static org.junit.runners.MethodSorters.*;

import java.util.*;

import org.apache.juneau.collections.*;
import org.apache.juneau.json.*;
import org.apache.juneau.serializer.*;
import org.junit.*;

@FixMethodOrder(NAME_ASCENDING)
public class MapAssertion_Test {

	//------------------------------------------------------------------------------------------------------------------
	// Helpers
	//------------------------------------------------------------------------------------------------------------------

	private <K,V> MapAssertion<K,V> test(Map<K,V> value) {
		return assertMap(value).silent();
	}

	@SafeVarargs
	private static <K,V> Map<K,V> map(Object...objects) {
		return AMap.ofPairs(objects);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Basic tests
	//-----------------------------------------------------------------------------------------------------------------

	@Test
	public void a01_msg() throws Exception {
		assertThrown(()->test(null).msg("Foo {0}", 1).exists()).message().is("Foo 1");
		assertThrown(()->test(null).msg("Foo {0}", 1).throwable(RuntimeException.class).exists()).isExactType(RuntimeException.class).message().is("Foo 1");
	}

	@Test
	public void a02_stdout() throws Exception {
		test(null).stdout();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Transform tests
	//-----------------------------------------------------------------------------------------------------------------

	@Test
	public void ba01a_asString() throws Exception {
		Map<String,Integer> x = map("a",1), nil = null;
		test(x).asString().is("{a=1}");
		test(nil).asString().isNull();
	}

	@Test
	public void ba01b_asString_wSerializer() throws Exception {
		Map<String,Integer> x = map("a",1), nil = null;
		WriterSerializer s = SimpleJsonSerializer.DEFAULT;
		test(x).asString(s).is("{a:1}");
		test(nil).asString(s).is("null");
	}

	@Test
	public void ba01c_asString_wPredicate() throws Exception {
		Map<String,Integer> x1 = map("a",1);
		test(x1).asString(x -> "foo").is("foo");
	}

	@Test
	public void ba02_asJson() throws Exception {
		Map<String,Integer> x = map("a",1), nil = null;
		test(x).asJson().is("{a:1}");
		test(nil).asJson().is("null");
	}

	@Test
	public void ba03_asJsonSorted() throws Exception {
		Map<String,Integer> x = map("b",2,"a",1), nil = null;
		test(x).asJsonSorted().is("{a:1,b:2}");
		test(nil).asJsonSorted().is("null");
	}

	@Test
	public void ba04_apply() throws Exception {
		Map<String,Integer> x1 = map("a",1), x2 = map("b",2);
		test(x1).apply(x -> x2).is(x2);
	}

	@Test
	public void bb01_value() throws Exception {
		Map<String,Integer> x = map("a",1,"b",2), nil = null;
		test(x).value("a").asInteger().is(1);
		test(x).value("z").asInteger().isNull();
		test(nil).value("a").asInteger().isNull();
	}

	@Test
	public void bb02_values() throws Exception {
		Map<String,Integer> x = map("a",1,"b",2), nil = null;
		test(x).values("b","a").has(2,1);
		test(x).values((String)null).has((Integer)null);
		test(nil).values("a","b").isNull();
	}

	@Test
	public void bb03_extract() throws Exception {
		Map<String,Integer> x = map("a",1,"b",2), nil = null;
		test(x).extract("a").isString("{a=1}");
		test(x).extract((String)null).isString("{null=null}");
		test(nil).extract("a").isNull();
	}

	@Test
	public void bb04_size() throws Exception {
		Map<String,Integer> x = map("a",1,"b",2), nil = null;
		test(x).size().is(2);
		test(nil).size().isNull();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Test tests
	//-----------------------------------------------------------------------------------------------------------------

	@Test
	public void ca01_exists() throws Exception {
		Map<Integer,Integer> x = map(), nil = null;
		test(x).exists().exists();
		assertThrown(()->test(nil).exists()).message().is("Value was null.");
	}

	@Test
	public void ca02_isNull() throws Exception {
		Map<Integer,Integer> x = map(), nil = null;
		assertMap(nil).isNull();
		assertThrown(()->test(x).isNull()).message().is("Value was not null.");
	}

	@Test
	public void ca03_isNotNull() throws Exception {
		Map<Integer,Integer> x = map(), nil = null;
		test(x).isNotNull();
		assertThrown(()->test(nil).isNotNull()).message().is("Value was null.");
	}

	@Test
	public void ca04a_is_T() throws Exception {
		Map<Integer,Integer> x1 = map(1,2), x1a = map(1,2), x2 = map(2,3), nil = null;
		test(x1).is(x1);
		test(x1).is(x1a);
		test(nil).is(nil);
		assertThrown(()->test(x1).is(x2)).message().oneLine().is("Unexpected value.  Expect='{2=3}'.  Actual='{1=2}'.");
		assertThrown(()->test(x1).is(nil)).message().oneLine().is("Unexpected value.  Expect='null'.  Actual='{1=2}'.");
		assertThrown(()->test(nil).is(x2)).message().oneLine().is("Unexpected value.  Expect='{2=3}'.  Actual='null'.");
	}

	@Test
	public void ca04b_is_predicate() throws Exception {
		Map<Integer,Integer> x1 = map(1,2);
		test(x1).is(x->x.size()==1);
		assertThrown(()->test(x1).is(x->x.size()==2)).message().oneLine().is("Unexpected value: '{1=2}'.");
		assertThrown(()->test(x1).is(ne(x1))).message().oneLine().is("Value unexpectedly matched.  Value='{1=2}'.");
	}

	@Test
	public void ca05_isNot() throws Exception {
		Map<Integer,Integer> x1 = map(1,2), x1a = map(1,2), x2 = map(3,4), nil = null;
		test(x1).isNot(x2);
		test(x1).isNot(nil);
		test(nil).isNot(x1);
		assertThrown(()->test(x1).isNot(x1a)).message().oneLine().is("Unexpected value.  Did not expect='{1=2}'.  Actual='{1=2}'.");
		assertThrown(()->test(nil).isNot(nil)).message().oneLine().is("Unexpected value.  Did not expect='null'.  Actual='null'.");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void ca06_isAny() throws Exception {
		Map<Integer,Integer> x1 = map(1,2), x1a = map(1,2), x2 = map(3,4), nil = null;
		test(x1).isAny(x1a, x2);
		assertThrown(()->test(x1).isAny(x2)).message().oneLine().is("Expected value not found.  Expect='[{3=4}]'.  Actual='{1=2}'.");
		assertThrown(()->test(x1).isAny()).message().oneLine().is("Expected value not found.  Expect='[]'.  Actual='{1=2}'.");
		assertThrown(()->test(nil).isAny(x2)).message().oneLine().is("Expected value not found.  Expect='[{3=4}]'.  Actual='null'.");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void ca07_isNotAny() throws Exception {
		Map<Integer,Integer> x1 = map(1,2), x1a = map(1,2), x2 = map(3,4), nil = null;
		test(x1).isNotAny(x2);
		test(x1).isNotAny();
		test(nil).isNotAny(x2);
		assertThrown(()->test(x1).isNotAny(x1a)).message().oneLine().is("Unexpected value found.  Unexpected='{1=2}'.  Actual='{1=2}'.");
		assertThrown(()->test(nil).isNotAny(nil)).message().oneLine().is("Unexpected value found.  Unexpected='null'.  Actual='null'.");
	}

	@Test
	public void ca08_isSame() throws Exception {
		Map<Integer,Integer> x1 = map(1,2), x1a = map(1,2), nil = null;
		test(x1).isSame(x1);
		test(nil).isSame(nil);
		assertThrown(()->test(x1).isSame(x1a)).message().oneLine().matches("Not the same value.  Expect='{1=2}(AMap@*)'.  Actual='{1=2}(AMap@*)'.");
		assertThrown(()->test(nil).isSame(x1a)).message().oneLine().matches("Not the same value.  Expect='{1=2}(AMap@*)'.  Actual='null(null)'.");
		assertThrown(()->test(x1).isSame(nil)).message().oneLine().matches("Not the same value.  Expect='null(null)'.  Actual='{1=2}(AMap@*)'.");
	}

	@Test
	public void ca09_isSameJsonAs() throws Exception {
		Map<Integer,Integer> x1 = map(1,2), x1a = map(1,2), x2 = map(3,4), nil = null;
		test(x1).isSameJsonAs(x1a);
		test(nil).isSameJsonAs(nil);
		assertThrown(()->test(x1a).isSameJsonAs(x2)).message().oneLine().is("Unexpected comparison.  Expect='{'3':4}'.  Actual='{'1':2}'.");
		assertThrown(()->test(nil).isSameJsonAs(x2)).message().oneLine().is("Unexpected comparison.  Expect='{'3':4}'.  Actual='null'.");
		assertThrown(()->test(x1).isSameJsonAs(nil)).message().oneLine().is("Unexpected comparison.  Expect='null'.  Actual='{'1':2}'.");
	}

	@Test
	public void ca10_isSameSortedJsonAs() throws Exception {
		Map<Integer,Integer> x1 = map(1,2), x1a = map(1,2), x2 = map(3,4), nil = null;
		test(x1).isSameSortedJsonAs(x1a);
		test(nil).isSameSortedJsonAs(nil);
		assertThrown(()->test(x1a).isSameSortedJsonAs(x2)).message().oneLine().is("Unexpected comparison.  Expect='{'3':4}'.  Actual='{'1':2}'.");
		assertThrown(()->test(nil).isSameSortedJsonAs(x2)).message().oneLine().is("Unexpected comparison.  Expect='{'3':4}'.  Actual='null'.");
		assertThrown(()->test(x1).isSameSortedJsonAs(nil)).message().oneLine().is("Unexpected comparison.  Expect='null'.  Actual='{'1':2}'.");
	}

	@Test
	public void ca11_isSameSerializedAs() throws Exception {
		Map<Integer,Integer> x1 = map(1,2), x1a = map(1,2), x2 = map(3,4), nil = null;
		WriterSerializer s = SimpleJsonSerializer.DEFAULT;
		test(x1).isSameSerializedAs(x1a, s);
		test(nil).isSameSerializedAs(nil, s);
		assertThrown(()->test(x1a).isSameSerializedAs(x2, s)).message().oneLine().is("Unexpected comparison.  Expect='{'3':4}'.  Actual='{'1':2}'.");
		assertThrown(()->test(nil).isSameSerializedAs(x2, s)).message().oneLine().is("Unexpected comparison.  Expect='{'3':4}'.  Actual='null'.");
		assertThrown(()->test(x1).isSameSerializedAs(nil, s)).message().oneLine().is("Unexpected comparison.  Expect='null'.  Actual='{'1':2}'.");
	}

	@Test
	public void ca12_isType() throws Exception {
		Map<Integer,Integer> x = map(1,2), nil = null;
		test(x).isType(Map.class);
		test(x).isType(Object.class);
		assertThrown(()->test(x).isType(String.class)).message().oneLine().is("Unexpected type.  Expect='java.lang.String'.  Actual='org.apache.juneau.collections.AMap'.");
		assertThrown(()->test(nil).isType(String.class)).message().oneLine().is("Value was null.");
		assertThrown(()->test(x).isType(null)).message().oneLine().is("Argument 'parent' cannot be null.");
	}

	@Test
	public void ca13_isExactType() throws Exception {
		Map<Integer,Integer> x = map(1,2), nil = null;
		test(x).isExactType(AMap.class);
		assertThrown(()->test(x).isExactType(Object.class)).message().oneLine().is("Unexpected type.  Expect='java.lang.Object'.  Actual='org.apache.juneau.collections.AMap'.");
		assertThrown(()->test(x).isExactType(String.class)).message().oneLine().is("Unexpected type.  Expect='java.lang.String'.  Actual='org.apache.juneau.collections.AMap'.");
		assertThrown(()->test(nil).isExactType(String.class)).message().oneLine().is("Value was null.");
		assertThrown(()->test(x).isExactType(null)).message().oneLine().is("Argument 'parent' cannot be null.");
	}

	@Test
	public void ca14_isString() throws Exception {
		Map<Integer,Integer> x = map(1,2), nil = null;
		test(x).isString("{1=2}");
		test(nil).isString(null);
		assertThrown(()->test(x).isString("bad")).message().oneLine().is("String differed at position 0.  Expect='bad'.  Actual='{1=2}'.");
		assertThrown(()->test(x).isString(null)).message().oneLine().is("String differed at position 0.  Expect='null'.  Actual='{1=2}'.");
		assertThrown(()->test(nil).isString("bad")).message().oneLine().is("String differed at position 0.  Expect='bad'.  Actual='null'.");
	}

	@Test
	public void ca15_isJson() throws Exception {
		Map<Integer,Integer> x = map(1,2), nil = null;
		test(x).isJson("{'1':2}");
		test(nil).isJson("null");
		assertThrown(()->test(x).isJson("bad")).message().oneLine().is("String differed at position 0.  Expect='bad'.  Actual='{'1':2}'.");
		assertThrown(()->test(x).isJson(null)).message().oneLine().is("String differed at position 0.  Expect='null'.  Actual='{'1':2}'.");
		assertThrown(()->test(nil).isJson("bad")).message().oneLine().is("String differed at position 0.  Expect='bad'.  Actual='null'.");
	}

	@Test
	public void cb01_isEmpty() throws Exception {
		Map<String,Integer> x1 = map(), x2 = map("a",1,"b",2), nil = null;
		test(x1).isEmpty();
		assertThrown(()->test(x2).isEmpty()).message().is("Map was not empty.");
		assertThrown(()->test(nil).isEmpty()).message().is("Value was null.");
	}

	@Test
	public void cb02_isNotEmpty() throws Exception {
		Map<String,Integer> x1 = map(), x2 = map("a",1,"b",2), nil = null;
		test(x2).isNotEmpty();
		assertThrown(()->test(x1).isNotEmpty()).message().is("Map was empty.");
		assertThrown(()->test(nil).isNotEmpty()).message().is("Value was null.");
	}

	@Test
	public void cb03_containsKey() throws Exception {
		Map<String,Integer> x = map("a",1,"b",2), nil = null;
		test(x).containsKey("a");
		assertThrown(()->test(x).containsKey("x")).message().oneLine().is("Map did not contain expected key.  Expected key='x'.  Value='{a=1, b=2}'.");
		assertThrown(()->test(nil).containsKey("x")).message().is("Value was null.");
	}

	@Test
	public void cb04_doesNotContainKey() throws Exception {
		Map<String,Integer> x = map("a",1,"b",2), nil = null;
		test(x).doesNotContainKey("x");
		assertThrown(()->test(x).doesNotContainKey("a")).message().oneLine().is("Map contained unexpected key.  Unexpected key='a'.  Value='{a=1, b=2}'.");
		assertThrown(()->test(nil).containsKey("x")).message().is("Value was null.");
	}

	@Test
	public void cb05_isSize() throws Exception {
		Map<String,Integer> x = map("a",1,"b",2), nil = null;
		test(x).isSize(2);
		assertThrown(()->test(x).isSize(1)).message().oneLine().is("Map did not have the expected size.  Expect=1.  Actual=2.");
		assertThrown(()->test(nil).isSize(0)).message().is("Value was null.");
	}
}
