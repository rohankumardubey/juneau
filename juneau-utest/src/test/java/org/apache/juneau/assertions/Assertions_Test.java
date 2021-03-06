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

import static org.apache.juneau.assertions.Assertions.*;
import static org.apache.juneau.testutils.StreamUtils.*;
import static org.junit.runners.MethodSorters.*;

import java.io.*;
import java.time.*;
import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.testutils.pojos.*;
import org.junit.*;

@FixMethodOrder(NAME_ASCENDING)
public class Assertions_Test {

	@SafeVarargs
	private static <T> List<T> list(T...objects) {
		return AList.of(objects);
	}

	//------------------------------------------------------------------------------------------------------------------
	// Basic tests
	//------------------------------------------------------------------------------------------------------------------

	@Test
	public void a00_basic() {
		new Assertions();
	}

	@Test
	public void a01_assertDate() throws Exception {
		assertDate(new Date()).isAfter(new Date(0));
	}

	@Test
	public void a02_assertInteger() throws Exception {
		assertInteger(2).isGt(1);
	}

	@Test
	public void a03_assertLong() throws Exception {
		assertLong(2l).isGt(1l);
	}

	@Test
	public void a04_assertObject() throws Exception {
		assertObject("foo").asJson().is("'foo'");
	}

	@Test
	public void a05_assertString() throws Exception {
		assertString("foo").is("foo");
		assertString(Optional.of("foo")).is("foo");
		assertString(Optional.empty()).isNull();
	}

	@Test
	public void a06_assertThrowable() throws Exception {
		assertThrowable(null).isNull();
	}

	@Test
	public void a07_assertArray() throws Exception {
		assertArray(new String[0]).isEmpty();
	}

	@Test
	public void a08_assertCollection() throws Exception {
		assertCollection(list()).isEmpty();
	}

	@Test
	public void a09_assertList() throws Exception {
		assertList(list()).isEmpty();
	}

	@Test
	public void a10_assertStream() throws Exception {
		assertStream(inputStream("foo")).asString().is("foo");
		assertStream((InputStream)null).asString().isNull();
	}

	@Test
	public void a11_assertBytes() throws Exception {
		assertBytes("foo".getBytes()).asString().is("foo");
		assertBytes((byte[])null).asString().isNull();
	}

	@Test
	public void a12_assertReader() throws Exception {
		assertReader(reader("foo")).is("foo");
		assertReader((Reader)null).isNull();
	}

	@Test
	public void a13_assertThrown() throws Exception {
		assertThrown(()->{throw new RuntimeException("foo");}).message().is("foo");
		assertThrown(()->{}).isNull();
	}

	@Test
	public void a14_assertZonedDateTime() throws Exception {
		assertZonedDateTime(ZonedDateTime.now()).exists();
	}

	@Test
	public void a15_assertBean() throws Exception {
		assertBean("123").exists();
	}

	@Test
	public void a16_assertBoolean() throws Exception {
		assertBoolean(true).isTrue();
	}

	@Test
	public void a17_assertVersion() throws Exception {
		assertVersion(Version.of("2")).isGt(Version.of("1"));
	}

	@Test
	public void a18_assertComparable() throws Exception {
		assertComparable(2).isGt(1);
	}

	@Test
	public void a19_assertBeanList() throws Exception {
		assertBeanList(list(ABean.get())).asJson().is("[{a:1,b:'foo'}]");
	}

	@Test
	public void a20a_assertIntArray() throws Exception {
		assertIntArray(new int[]{1}).length().is(1);
	}

	@Test
	public void a20b_assertLongArray() throws Exception {
		assertLongArray(new long[]{1l}).length().is(1);
	}

	@Test
	public void a20c_assertShortArray() throws Exception {
		assertShortArray(new short[]{1}).length().is(1);
	}

	@Test
	public void a20d_assertFloatArray() throws Exception {
		assertFloatArray(new float[]{1}).length().is(1);
	}

	@Test
	public void a20e_assertDoubleArray() throws Exception {
		assertDoubleArray(new double[]{1}).length().is(1);
	}

	@Test
	public void a20f_assertBooleanArray() throws Exception {
		assertBooleanArray(new boolean[]{true}).length().is(1);
	}

	@Test
	public void a20g_assertCharArray() throws Exception {
		assertCharArray(new char[]{'a'}).length().is(1);
	}

	@Test
	public void a20h_assertByteArray() throws Exception {
		assertByteArray(new byte[]{1}).length().is(1);
	}

	@Test
	public void a21_assertMap() throws Exception {
		assertMap(AMap.of(1,2)).size().is(1);
	}

	@Test
	public void a22_assertArgNotNull() throws Exception {
		assertArgNotNull("foo", 123);
		assertThrown(()->assertArgNotNull("foo", null)).message().is("Argument 'foo' cannot be null.");
	}

	@Test
	public void a23_assertArg() throws Exception {
		assertArg(true, "foo {0}", 1);
		assertThrown(()->assertArg(false, "foo {0}", 1)).message().is("foo 1");
	}

	@Test
	public void a24_assertOptional() throws Exception {
		assertOptional(Optional.empty()).isNull();
		assertOptional(Optional.of(1)).isNotNull();
	}

	@Test
	public void a25_assertStringList() throws Exception {
		assertStringList(list()).isNotNull();
		assertStringList(null).isNull();
	}
}
