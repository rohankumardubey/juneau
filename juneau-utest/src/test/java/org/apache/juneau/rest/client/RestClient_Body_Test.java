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
package org.apache.juneau.rest.client;

import static org.apache.juneau.http.HttpHeaders.*;
import static org.apache.juneau.http.HttpEntities.*;
import static org.apache.juneau.http.HttpResources.*;
import static org.junit.runners.MethodSorters.*;
import static org.apache.juneau.testutils.StreamUtils.*;

import java.io.*;
import java.util.*;

import org.apache.http.*;
import org.apache.juneau.http.entity.*;
import org.apache.juneau.http.resource.*;
import org.apache.juneau.json.*;
import org.apache.juneau.rest.*;
import org.apache.juneau.rest.annotation.*;
import org.apache.juneau.rest.mock.*;
import org.apache.juneau.serializer.*;
import org.apache.juneau.testutils.pojos.*;
import org.junit.*;

@FixMethodOrder(NAME_ASCENDING)
public class RestClient_Body_Test {

	@Rest
	public static class A extends BasicRestObject {
		@RestPost
		public Reader post(org.apache.juneau.rest.RestRequest req, org.apache.juneau.rest.RestResponse res) throws IOException {
			for (RequestHeader e : req.getHeaders().getAll())
				res.addHeader("X-" + e.getName(), e.getValue());
			return req.getReader();
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	// Method tests
	//------------------------------------------------------------------------------------------------------------------

	@Test
	public void a01_BasicHttpResource() throws Exception {
		HttpResource x1 = stringResource("foo").build();
		client().build().post("/", x1).run()
			.assertHeader("X-Content-Length").asInteger().is(3)
			.assertHeader("X-Content-Encoding").isNull()
			.assertHeader("X-Content-Type").isNull()
			.assertHeader("X-Transfer-Encoding").isNull()
		;

		HttpResource x2 = stringResource("foo").contentType("text/plain").contentEncoding("identity").build();
		client().build().post("/",x2).run()
			.assertHeader("X-Content-Length").asInteger().is(3)
			.assertHeader("X-Content-Encoding").is("identity")
			.assertHeader("X-Content-Type").is("text/plain")
			.assertHeader("X-Transfer-Encoding").isNull()
		;

		HttpResource x3 = stringResource("foo").contentType(contentType("text/plain")).contentEncoding(contentEncoding("identity")).chunked().build();
		client().build().post("/",x3).run()
			.assertHeader("X-Content-Length").isNull()  // Missing when chunked.
			.assertHeader("X-Content-Encoding").is("identity")
			.assertHeader("X-Content-Type").is("text/plain")
			.assertHeader("X-Transfer-Encoding").is("chunked")
		;

		HttpResource x4 = stringResource("foo", contentType("text/plain")).contentEncoding("identity").build();
		client().build().post("/",x4).run()
			.assertHeader("X-Content-Length").asInteger().is(3)
			.assertHeader("X-Content-Encoding").is("identity")
			.assertHeader("X-Content-Type").is("text/plain")
			.assertHeader("X-Transfer-Encoding").isNull()
		;

		HttpResource x5 = stringResource("foo").header("Foo","bar").header(header("Baz","qux")).build();
		client().build().post("/",x5).run()
			.assertHeader("X-Foo").is("bar")
			.assertHeader("X-Baz").is("qux")
		;

		HttpResource x6 = stringResource("foo").headers(Arrays.asList(header("Foo","bar"),header("Baz","qux"))).build();
		client().build().post("/",x6).run()
			.assertHeader("X-Foo").is("bar")
			.assertHeader("X-Baz").is("qux")
		;

		HttpResource x7 = readerResource(reader("foo")).build();
		client().build().post("/",x7).run().assertBody().is("foo");

		HttpResource x8 = readerResource(reader("foo")).cached().build();
		client().build().post("/",x8).run().assertBody().is("foo");
		client().build().post("/",x8).run().assertBody().is("foo");

		HttpResource x9 = readerResource(null).build();
		client().build().post("/",x9).run().assertBody().isEmpty();
	}

	@Test
	public void a02_StringEntity() throws Exception {
		HttpEntity x1 = stringEntity("foo").build();
		client().build().post("/", x1).run()
			.assertHeader("X-Content-Length").asInteger().is(3)
			.assertHeader("X-Content-Encoding").isNull()
			.assertHeader("X-Content-Type").isNull()
			.assertHeader("X-Transfer-Encoding").isNull()
		;

		HttpEntity x2 = stringEntity("foo").contentType("text/plain").contentEncoding("identity").build();
		client().build().post("/",x2).run()
			.assertHeader("X-Content-Length").asInteger().is(3)
			.assertHeader("X-Content-Encoding").is("identity")
			.assertHeader("X-Content-Type").is("text/plain")
			.assertHeader("X-Transfer-Encoding").isNull()
		;

		HttpEntity x3 = stringEntity("foo").contentType(contentType("text/plain")).contentEncoding(contentEncoding("identity")).chunked().build();
		client().build().post("/",x3).run()
			.assertHeader("X-Content-Length").isNull()  // Missing when chunked.
			.assertHeader("X-Content-Encoding").is("identity")
			.assertHeader("X-Content-Type").is("text/plain")
			.assertHeader("X-Transfer-Encoding").is("chunked")
		;

		HttpEntity x4 = stringEntity("foo", contentType("text/plain")).contentEncoding("identity").build();
		client().build().post("/",x4).run()
			.assertHeader("X-Content-Length").asInteger().is(3)
			.assertHeader("X-Content-Encoding").is("identity")
			.assertHeader("X-Content-Type").is("text/plain")
			.assertHeader("X-Transfer-Encoding").isNull()
		;

		HttpEntity x7 = readerEntity(reader("foo")).build();
		client().build().post("/",x7).run().assertBody().is("foo");

		HttpEntity x8 = readerEntity(reader("foo")).cached().build();
		client().build().post("/",x8).run().assertBody().is("foo");
		client().build().post("/",x8).run().assertBody().is("foo");

		HttpEntity x9 = readerEntity(null).build();
		client().build().post("/",x9).run().assertBody().isEmpty();

		BasicHttpEntity x12 = stringEntity("foo").build();
		x12.assertString().is("foo");
		x12.assertBytes().asString().is("foo");
	}

	@Test
	public void a03_SerializedHttpEntity() throws Exception {
		Serializer js = JsonSerializer.DEFAULT;

		SerializedEntity x1 = serializedEntity(ABean.get(),null,null).build();
		client().build().post("/",x1).run()
			.assertHeader("X-Content-Length").isNull()
			.assertHeader("X-Content-Encoding").isNull()
			.assertHeader("X-Content-Type").is("application/json+simple")
			.assertHeader("X-Transfer-Encoding").is("chunked")  // Because content length is -1.
		;

		SerializedEntity x2 = serializedEntity(ABean.get(),js,null).build();
		client().build().post("/",x2).run()
			.assertHeader("X-Content-Length").isNull()
			.assertHeader("X-Content-Encoding").isNull()
			.assertHeader("X-Content-Type").is("application/json")
			.assertBody().asType(ABean.class).asJson().is("{a:1,b:'foo'}");

		SerializedEntity x3 = serializedEntity(()->ABean.get(),js,null).build();
		client().build().post("/",x3).run()
			.assertHeader("X-Content-Length").isNull()
			.assertHeader("X-Content-Encoding").isNull()
			.assertHeader("X-Content-Type").is("application/json")
			.assertBody().asType(ABean.class).asJson().is("{a:1,b:'foo'}");
	}

	//------------------------------------------------------------------------------------------------------------------
	// Helper methods.
	//------------------------------------------------------------------------------------------------------------------

	private static RestClientBuilder client() {
		return MockRestClient.create(A.class).simpleJson();
	}

	private static Header header(String name, Object val) {
		return basicHeader(name, val);
	}
}
