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
package org.apache.juneau.parser;

import static org.apache.juneau.assertions.Assertions.*;
import static org.junit.runners.MethodSorters.*;

import org.apache.juneau.json.*;
import org.junit.*;

@FixMethodOrder(NAME_ASCENDING)
public class ParserGroupTest {

	//====================================================================================================
	// Test parser group matching
	//====================================================================================================
	@Test
	public void testParserGroupMatching() throws Exception {

		ParserGroup g = ParserGroup.create().add(Parser1.class, Parser2.class, Parser3.class).build();
		assertObject(g.getParser("text/foo")).isType(Parser1.class);
		assertObject(g.getParser("text/foo_a")).isType(Parser1.class);
		assertObject(g.getParser("text/foo_a+xxx")).isType(Parser1.class);
		assertObject(g.getParser("text/xxx+foo_a")).isType(Parser1.class);
		assertObject(g.getParser("text/foo+bar")).isType(Parser2.class);
		assertObject(g.getParser("text/foo+bar_a")).isType(Parser2.class);
		assertObject(g.getParser("text/bar+foo")).isType(Parser2.class);
		assertObject(g.getParser("text/bar+foo+xxx")).isType(Parser2.class);
		assertObject(g.getParser("text/baz")).isType(Parser3.class);
		assertObject(g.getParser("text/baz_a")).isType(Parser3.class);
		assertObject(g.getParser("text/baz+yyy")).isType(Parser3.class);
		assertObject(g.getParser("text/baz_a+yyy")).isType(Parser3.class);
		assertObject(g.getParser("text/yyy+baz")).isType(Parser3.class);
		assertObject(g.getParser("text/yyy+baz_a")).isType(Parser3.class);
	}


	public static class Parser1 extends JsonParser { protected Parser1(JsonParserBuilder b) { super(b.consumes("text/foo,text/foo_a")); }}
	public static class Parser2 extends JsonParser { protected Parser2(JsonParserBuilder b) { super(b.consumes("text/foo+bar,text/foo+bar_a")); }}
	public static class Parser3 extends JsonParser { protected Parser3(JsonParserBuilder b) { super(b.consumes("text/baz,text/baz_a")); }}

	//====================================================================================================
	// Test inheritence
	//====================================================================================================
	@Test
	public void testInheritence() throws Exception {
		ParserGroup.Builder gb = null;
		ParserGroup g = null;

		gb = ParserGroup.create().add(P1.class, P2.class);
		g = gb.build();
		assertObject(g.getSupportedMediaTypes()).asJson().is("['text/1','text/2','text/2a']");

		gb = ParserGroup.create().add(P1.class, P2.class).add(P3.class, P4.class);
		g = gb.build();
		assertObject(g.getSupportedMediaTypes()).asJson().is("['text/3','text/4','text/4a','text/1','text/2','text/2a']");

		gb = ParserGroup.create().add(P1.class, P2.class).add(P3.class, P4.class).add(P5.class);
		g = gb.build();
		assertObject(g.getSupportedMediaTypes()).asJson().is("['text/5','text/3','text/4','text/4a','text/1','text/2','text/2a']");
	}

	public static class P1 extends JsonParser { protected P1(JsonParserBuilder b) { super(b.consumes("text/1")); }}
	public static class P2 extends JsonParser { protected P2(JsonParserBuilder b) { super(b.consumes("text/2,text/2a")); }}
	public static class P3 extends JsonParser { protected P3(JsonParserBuilder b) { super(b.consumes("text/3")); }}
	public static class P4 extends JsonParser { protected P4(JsonParserBuilder b) { super(b.consumes("text/4,text/4a"));} }
	public static class P5 extends JsonParser { protected P5(JsonParserBuilder b) { super(b.consumes("text/5"));}}
}
