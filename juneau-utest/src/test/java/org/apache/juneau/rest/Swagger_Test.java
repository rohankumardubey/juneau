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
package org.apache.juneau.rest;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.runners.MethodSorters.*;
import static org.apache.juneau.assertions.Assertions.*;
import java.io.*;
import java.util.*;

import org.apache.juneau.json.*;
import org.apache.juneau.jsonschema.annotation.*;
import org.apache.juneau.jsonschema.annotation.Tag;
import org.apache.juneau.xml.*;
import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.cp.*;
import org.apache.juneau.dto.swagger.*;
import org.apache.juneau.http.annotation.*;
import org.apache.juneau.http.annotation.Contact;
import org.apache.juneau.http.annotation.License;
import org.apache.juneau.rest.annotation.*;
import org.apache.juneau.rest.annotation.Swagger;
import org.apache.juneau.rest.mock.*;
import org.junit.*;

@FixMethodOrder(NAME_ASCENDING)
public class Swagger_Test {

	//------------------------------------------------------------------------------------------------------------------
	// Setup
	//------------------------------------------------------------------------------------------------------------------

	public void testMethod() {}

	private org.apache.juneau.dto.swagger.Swagger getSwaggerWithFile(Object resource) throws Exception {
		RestContext rc = RestContext.create(resource.getClass(),null,null).init(()->resource).defaultClasses(TestClasspathFileFinder.class).build();
		RestOpContext roc = RestOpContext.create(Swagger_Test.class.getMethod("testMethod"), rc).build();
		RestRequest req = rc.createRequest(new RestCall(resource, rc, new MockServletRequest(), new MockServletResponse()).restOpContext(roc));
		SwaggerProvider ip = rc.getSwaggerProvider();
		return ip.getSwagger(rc, req.getLocale());
	}

	private static org.apache.juneau.dto.swagger.Swagger getSwagger(Object resource) throws Exception {
		RestContext rc = RestContext.create(resource.getClass(),null,null).init(()->resource).build();
		RestOpContext roc = RestOpContext.create(Swagger_Test.class.getMethod("testMethod"), rc).build();
		RestRequest req = rc.createRequest(new RestCall(resource, rc, new MockServletRequest(), new MockServletResponse()).restOpContext(roc));
		SwaggerProvider ip = rc.getSwaggerProvider();
		return ip.getSwagger(rc, req.getLocale());
	}

	public static class TestClasspathFileFinder extends BasicFileFinder {

		public TestClasspathFileFinder() {
			super(FileFinder.create().cp(Swagger_Test.class, null, false));
		}

		@Override
		public Optional<InputStream> find(String name, Locale locale) throws IOException {
			if (name.endsWith(".json"))
				return Optional.of(SwaggerProvider.class.getResourceAsStream("BasicRestInfoProviderTest_swagger.json"));
			return super.find(name, locale);
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	// /<root>
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class A1 {}

	@Test
	public void a01_swagger_default() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwagger(new A1());
		assertEquals("2.0", x.getSwagger());
		assertEquals(null, x.getHost());
		assertEquals(null, x.getBasePath());
		assertEquals(null, x.getSchemes());
	}
	@Test
	public void a01_swagger_default_withFile() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwaggerWithFile(new A1());
		assertEquals("0.0", x.getSwagger());
		assertEquals("s-host", x.getHost());
		assertEquals("s-basePath", x.getBasePath());
		assertObject(x.getSchemes()).asJson().is("['s-scheme']");
	}


	@Rest(swagger=@Swagger("{swagger:'3.0',host:'a-host',basePath:'a-basePath',schemes:['a-scheme']}"))
	public static class A2 {}

	@Test
	public void a02_swagger_Swagger_value() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwagger(new A2());
		assertEquals("3.0", x.getSwagger());
		assertEquals("a-host", x.getHost());
		assertEquals("a-basePath", x.getBasePath());
		assertObject(x.getSchemes()).asJson().is("['a-scheme']");
	}
	@Test
	public void a02_swagger_Swagger_value_withFile() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwaggerWithFile(new A2());
		assertEquals("3.0", x.getSwagger());
		assertEquals("a-host", x.getHost());
		assertEquals("a-basePath", x.getBasePath());
		assertObject(x.getSchemes()).asJson().is("['a-scheme']");
	}

	//------------------------------------------------------------------------------------------------------------------
	// /info
	//------------------------------------------------------------------------------------------------------------------

	@Rest(
		title="a-title",
		description="a-description"
	)
	public static class B1 {}

	@Test
	public void b01a_info_Rest() throws Exception {
		Info x = getSwagger(new B1()).getInfo();
		assertEquals("a-title", x.getTitle());
		assertEquals("a-description", x.getDescription());
		assertEquals(null, x.getVersion());
		assertEquals(null, x.getTermsOfService());
		assertEquals(null, x.getContact());
		assertEquals(null, x.getLicense());
	}
	@Test
	public void b01b_info_Rest_withFile() throws Exception {
		Info x = getSwaggerWithFile(new B1()).getInfo();
		assertEquals("s-title", x.getTitle());
		assertEquals("s-description", x.getDescription());
		assertEquals("0.0.0", x.getVersion());
		assertEquals("s-termsOfService", x.getTermsOfService());
		assertObject(x.getContact()).asJson().is("{name:'s-name',url:'s-url',email:'s-email'}");
		assertObject(x.getLicense()).asJson().is("{name:'s-name',url:'s-url'}");
	}

	@Rest(
		messages="BasicRestInfoProviderTest",
		title="$L{foo}",
		description="$L{foo}"
	)
	public static class B2 {}

	@Test
	public void b02a_info_Rest_localized() throws Exception {
		Info x = getSwagger(new B2()).getInfo();
		assertEquals("l-foo", x.getTitle());
		assertEquals("l-foo", x.getDescription());
	}
	@Test
	public void b02b_info_Rest_localized_withFile() throws Exception {
		Info x = getSwaggerWithFile(new B2()).getInfo();
		assertEquals("s-title", x.getTitle());
		assertEquals("s-description", x.getDescription());
	}

	@Rest(
		title="a-title",
		description="a-description",
		swagger=@Swagger(
			{
				"info:{",
					"title:'b-title',",
					"description:'b-description',",
					"version:'2.0.0',",
					"termsOfService:'a-termsOfService',",
					"contact:{name:'a-name',url:'a-url',email:'a-email'},",
					"license:{name:'a-name',url:'a-url'}",
				"}"
			}
		)
	)
	public static class B3 {}

	@Test
	public void b03a_info_Swagger_value() throws Exception {
		Info x = getSwagger(new B3()).getInfo();
		assertEquals("b-title", x.getTitle());
		assertEquals("b-description", x.getDescription());
		assertEquals("2.0.0", x.getVersion());
		assertEquals("a-termsOfService", x.getTermsOfService());
		assertObject(x.getContact()).asJson().is("{name:'a-name',url:'a-url',email:'a-email'}");
		assertObject(x.getLicense()).asJson().is("{name:'a-name',url:'a-url'}");
	}
	@Test
	public void b03b_info_Swagger_value_withFile() throws Exception {
		Info x = getSwaggerWithFile(new B3()).getInfo();
		assertEquals("b-title", x.getTitle());
		assertEquals("b-description", x.getDescription());
		assertEquals("2.0.0", x.getVersion());
		assertEquals("a-termsOfService", x.getTermsOfService());
		assertObject(x.getContact()).asJson().is("{name:'a-name',url:'a-url',email:'a-email'}");
		assertObject(x.getLicense()).asJson().is("{name:'a-name',url:'a-url'}");
	}

	@Rest(
		messages="BasicRestInfoProviderTest",
		title="a-title",
		description="a-description",
		swagger=@Swagger("{info:{title:'$L{bar}',description:'$L{bar}'}}")
	)
	public static class B4 {}

	@Test
	public void b04_info_Swagger_value_localised() throws Exception {
		assertEquals("l-bar", getSwagger(new B4()).getInfo().getTitle());
		assertEquals("l-bar", getSwaggerWithFile(new B4()).getInfo().getTitle());
		assertEquals("l-bar", getSwagger(new B4()).getInfo().getDescription());
		assertEquals("l-bar", getSwaggerWithFile(new B4()).getInfo().getDescription());
	}

	@Rest(
		title="a-title",
		description="a-description",
		swagger=@Swagger(
			value= {
				"info:{",
					"title:'b-title',",
					"description:'b-description',",
					"version:'2.0.0',",
					"termsOfService:'a-termsOfService',",
					"contact:{name:'a-name',url:'a-url',email:'a-email'},",
					"license:{name:'a-name',url:'a-url'}",
				"}"
			},
			title="c-title",
			description="c-description",
			version="3.0.0",
			termsOfService="b-termsOfService",
			contact=@Contact(name="b-name",url="b-url",email="b-email"),
			license=@License(name="b-name",url="b-url")
		)
	)
	public static class B5 {}

	@Test
	public void b05a_info_Swagger_title() throws Exception {
		Info x = getSwagger(new B5()).getInfo();
		assertEquals("c-title", x.getTitle());
		assertEquals("c-description", x.getDescription());
		assertEquals("3.0.0", x.getVersion());
		assertEquals("b-termsOfService", x.getTermsOfService());
		assertObject(x.getContact()).asJson().is("{name:'b-name',url:'b-url',email:'b-email'}");
		assertObject(x.getLicense()).asJson().is("{name:'b-name',url:'b-url'}");
	}
	@Test
	public void b05b_info_Swagger_title_withFile() throws Exception {
		Info x = getSwaggerWithFile(new B5()).getInfo();
		assertEquals("c-title", x.getTitle());
		assertEquals("c-description", x.getDescription());
		assertEquals("3.0.0", x.getVersion());
		assertEquals("b-termsOfService", x.getTermsOfService());
		assertObject(x.getContact()).asJson().is("{name:'b-name',url:'b-url',email:'b-email'}");
		assertObject(x.getLicense()).asJson().is("{name:'b-name',url:'b-url'}");
	}

	@Rest(
		title="a-title",
		description="a-description",
		swagger=@Swagger(
			value= {
				"info:{",
					"title:'b-title',",
					"description:'b-description',",
					"version:'2.0.0',",
					"termsOfService:'a-termsOfService',",
					"contact:{name:'a-name',url:'a-url',email:'a-email'},",
					"license:{name:'a-name',url:'a-url'}",
				"}"
			},
			title="$L{baz}",
			description="$L{baz}",
			version="$L{foo}",
			termsOfService="$L{foo}",
			contact=@Contact("{name:'$L{foo}',url:'$L{bar}',email:'$L{baz}'}"),
			license=@License("{name:'$L{foo}',url:'$L{bar}'}")
		),
		messages="BasicRestInfoProviderTest"
	)
	public static class B6 {}

	@Test
	public void b06a_info_Swagger_title_localized() throws Exception {
		Info x = getSwagger(new B6()).getInfo();
		assertEquals("l-baz", x.getTitle());
		assertEquals("l-baz", x.getDescription());
		assertEquals("l-foo", x.getVersion());
		assertEquals("l-foo", x.getTermsOfService());
		assertObject(x.getContact()).asJson().is("{name:'l-foo',url:'l-bar',email:'l-baz'}");
		assertObject(x.getLicense()).asJson().is("{name:'l-foo',url:'l-bar'}");
	}
	@Test
	public void b06b_info_Swagger_title_localized_withFile() throws Exception {
		Info x = getSwaggerWithFile(new B6()).getInfo();
		assertEquals("l-baz", x.getTitle());
		assertEquals("l-baz", x.getDescription());
		assertEquals("l-foo", x.getVersion());
		assertEquals("l-foo", x.getTermsOfService());
		assertObject(x.getContact()).asJson().is("{name:'l-foo',url:'l-bar',email:'l-baz'}");
		assertObject(x.getLicense()).asJson().is("{name:'l-foo',url:'l-bar'}");
	}

	@Rest(
		swagger=@Swagger(
			title="c-title",
			description="c-description"
		)
	)
	public static class B07 {}

	@Test
	public void b07a_title_Swagger_title_only() throws Exception {
		Info x = getSwagger(new B07()).getInfo();
		assertEquals("c-title", x.getTitle());
		assertEquals("c-description", x.getDescription());
	}
	@Test
	public void b07b_title_Swagger_title_only_withFile() throws Exception {
		Info x = getSwaggerWithFile(new B07()).getInfo();
		assertEquals("c-title", x.getTitle());
		assertEquals("c-description", x.getDescription());
	}

	//------------------------------------------------------------------------------------------------------------------
	// /tags
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class C1 {}

	@Test
	public void c01a_tags_default() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwagger(new C1());
		assertEquals(null, x.getTags());
	}
	@Test
	public void c01b_tags_default_withFile() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwaggerWithFile(new C1());
		assertObject(x.getTags()).asJson().is("[{name:'s-name',description:'s-description',externalDocs:{description:'s-description',url:'s-url'}}]");
	}

	// Tags in @ResourceSwagger(value) should override file.
	@Rest(
		swagger=@Swagger(
			"{tags:[{name:'a-name',description:'a-description',externalDocs:{description:'a-description',url:'a-url'}}]}"
		)
	)
	public static class C2 {}

	@Test
	public void c02a_tags_Swagger_value() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwagger(new C2());
		assertObject(x.getTags()).asJson().is("[{name:'a-name',description:'a-description',externalDocs:{description:'a-description',url:'a-url'}}]");
	}
	@Test
	public void c02b_tags_Swagger_value_withFile() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwaggerWithFile(new C2());
		assertObject(x.getTags()).asJson().is("[{name:'a-name',description:'a-description',externalDocs:{description:'a-description',url:'a-url'}}]");
	}

	// Tags in both @ResourceSwagger(value) and @ResourceSwagger(tags) should accumulate.
	@Rest(
		swagger=@Swagger(
			value="{tags:[{name:'a-name',description:'a-description',externalDocs:{description:'a-description',url:'a-url'}}]}",
			tags=@Tag(name="b-name",description="b-description",externalDocs=@ExternalDocs(description="b-description",url="b-url"))
		)
	)
	public static class C3 {}

	@Test
	public void c03a_tags_Swagger_tags() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwagger(new C3());
		assertObject(x.getTags()).asJson().is("[{name:'a-name',description:'a-description',externalDocs:{description:'a-description',url:'a-url'}},{name:'b-name',description:'b-description',externalDocs:{description:'b-description',url:'b-url'}}]");
	}
	@Test
	public void c03b_tags_Swagger_tags_withFile() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwaggerWithFile(new C3());
		assertObject(x.getTags()).asJson().is("[{name:'a-name',description:'a-description',externalDocs:{description:'a-description',url:'a-url'}},{name:'b-name',description:'b-description',externalDocs:{description:'b-description',url:'b-url'}}]");
	}

	// Same as above but without [] outer characters.
	@Rest(
		swagger=@Swagger(
			value="{tags:[{name:'a-name',description:'a-description',externalDocs:{description:'a-description',url:'a-url'}}]}",
			tags=@Tag(name="b-name",value=" { description:'b-description', externalDocs: { description:'b-description', url:'b-url' } } ")
		)
	)
	public static class C4 {}

	@Test
	public void c04a_tags_Swagger_tags() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwagger(new C4());
		assertObject(x.getTags()).asJson().is("[{name:'a-name',description:'a-description',externalDocs:{description:'a-description',url:'a-url'}},{name:'b-name',description:'b-description',externalDocs:{description:'b-description',url:'b-url'}}]");
	}
	@Test
	public void c04b_tags_Swagger_tags_withFile() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwaggerWithFile(new C4());
		assertObject(x.getTags()).asJson().is("[{name:'a-name',description:'a-description',externalDocs:{description:'a-description',url:'a-url'}},{name:'b-name',description:'b-description',externalDocs:{description:'b-description',url:'b-url'}}]");
	}

	// Tags in both Swagger.json and @ResourceSwagger(tags) should accumulate.
	@Rest(
		swagger=@Swagger(
			tags=@Tag(name="b-name",description="b-description",externalDocs=@ExternalDocs(description="b-description",url="b-url"))
		)
	)
	public static class C5 {}

	@Test
	public void c05a_tags_Swagger_tags_only() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwagger(new C5());
		assertObject(x.getTags()).asJson().is("[{name:'b-name',description:'b-description',externalDocs:{description:'b-description',url:'b-url'}}]");
	}
	@Test
	public void c05b_tags_Swagger_tags_only_witFile() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwaggerWithFile(new C5());
		assertObject(x.getTags()).asJson().is("[{name:'s-name',description:'s-description',externalDocs:{description:'s-description',url:'s-url'}},{name:'b-name',description:'b-description',externalDocs:{description:'b-description',url:'b-url'}}]");
	}

	// Dup tag names should be overwritten
	@Rest(
		swagger=@Swagger(
			tags={
				@Tag(name="s-name",description="b-description",externalDocs=@ExternalDocs(description="b-description",url="b-url")),
				@Tag(name="s-name",value="{description:'c-description',externalDocs:{description:'c-description',url:'c-url'}}")
			}
		)
	)
	public static class C6 {}

	@Test
	public void c06a_tags_Swagger_tags_dups() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwagger(new C6());
		assertObject(x.getTags()).asJson().is("[{name:'s-name',description:'c-description',externalDocs:{description:'c-description',url:'c-url'}}]");
	}
	@Test
	public void c06b_tags_Swagger_tags_dups_withFile() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwaggerWithFile(new C6());
		assertObject(x.getTags()).asJson().is("[{name:'s-name',description:'c-description',externalDocs:{description:'c-description',url:'c-url'}}]");
	}

	@Rest(
		swagger=@Swagger(
			value="{tags:[{name:'$L{foo}',description:'$L{foo}',externalDocs:{description:'$L{foo}',url:'$L{foo}'}}]}",
			tags=@Tag(name="$L{foo}",description="$L{foo}",externalDocs=@ExternalDocs(description="$L{foo}",url="$L{foo}"))
		),
		messages="BasicRestInfoProviderTest"
	)
	public static class C7 {}

	@Test
	public void c07a_tags_Swagger_tags_localised() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwagger(new C7());
		assertObject(x.getTags()).asJson().is("[{name:'l-foo',description:'l-foo',externalDocs:{description:'l-foo',url:'l-foo'}}]");
	}
	@Test
	public void c07b_tags_Swagger_tags_localised_withFile() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwaggerWithFile(new C7());
		assertObject(x.getTags()).asJson().is("[{name:'l-foo',description:'l-foo',externalDocs:{description:'l-foo',url:'l-foo'}}]");
	}

	// Auto-detect tags defined on methods.
	@Rest
	public static class C8 {
		@RestOp(swagger=@OpSwagger(tags="foo"))
		public void a() {}
	}

	@Test
	public void c08a_tags_Swagger_tags_loose() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwagger(new C8());
		assertObject(x.getTags()).asJson().is("[{name:'foo'}]");
	}
	@Test
	public void c08b_tags_Swagger_tags_loose_withFile() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwaggerWithFile(new C8());
		assertObject(x.getTags()).asJson().is("[{name:'s-name',description:'s-description',externalDocs:{description:'s-description',url:'s-url'}},{name:'foo'}]");
	}

	// Comma-delimited list
	@Rest
	public static class C9 {
		@RestOp(swagger=@OpSwagger(tags=" foo, bar "))
		public void a() {}
	}

	@Test
	public void c09a_tags_Swagger_tags_loose_cdl() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwagger(new C9());
		assertObject(x.getTags()).asJson().is("[{name:'foo'},{name:'bar'}]");
	}
	@Test
	public void c09b_tags_Swagger_tags_loose_cdl_withFile() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwaggerWithFile(new C9());
		assertObject(x.getTags()).asJson().is("[{name:'s-name',description:'s-description',externalDocs:{description:'s-description',url:'s-url'}},{name:'foo'},{name:'bar'}]");
	}

	// OList
	@Rest
	public static class C10 {
		@RestGet(swagger=@OpSwagger(tags="['foo', 'bar']"))
		public void a() {}
	}

	@Test
	public void c10a_tags_Swagger_tags_loose_olist() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwagger(new C10());
		assertObject(x.getTags()).asJson().is("[{name:'foo'},{name:'bar'}]");
	}
	@Test
	public void c10b_tags_Swagger_tags_loose_olist_withFile() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwaggerWithFile(new C10());
		assertObject(x.getTags()).asJson().is("[{name:'s-name',description:'s-description',externalDocs:{description:'s-description',url:'s-url'}},{name:'foo'},{name:'bar'}]");
	}

	// OList localized
	@Rest(messages="BasicRestInfoProviderTest")
	public static class C11 {
		@RestGet(swagger=@OpSwagger(tags="['$L{foo}', '$L{bar}']"))
		public void a() {}
	}

	@Test
	public void c11a_tags_Swagger_tags_loose_olist_localized() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwagger(new C11());
		assertObject(x.getTags()).asJson().is("[{name:'l-foo'},{name:'l-bar'}]");
	}
	@Test
	public void c11b_tags_Swagger_tags_loose_olist_localized_withFile() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwaggerWithFile(new C11());
		assertObject(x.getTags()).asJson().is("[{name:'s-name',description:'s-description',externalDocs:{description:'s-description',url:'s-url'}},{name:'l-foo'},{name:'l-bar'}]");
	}

	// Comma-delimited list localized
	@Rest(messages="BasicRestInfoProviderTest")
	public static class C12 {
		@RestGet(swagger=@OpSwagger(tags=" $L{foo}, $L{bar} "))
		public void a() {}
	}

	@Test
	public void c12a_tags_Swagger_tags_loose_cdl_localized() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwagger(new C12());
		assertObject(x.getTags()).asJson().is("[{name:'l-foo'},{name:'l-bar'}]");
	}
	@Test
	public void c12b_tags_Swagger_tags_loose_cdl_localized_withFile() throws Exception {
		org.apache.juneau.dto.swagger.Swagger x = getSwaggerWithFile(new C12());
		assertObject(x.getTags()).asJson().is("[{name:'s-name',description:'s-description',externalDocs:{description:'s-description',url:'s-url'}},{name:'l-foo'},{name:'l-bar'}]");
	}

	//------------------------------------------------------------------------------------------------------------------
	// /externalDocs
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class D1 {}

	@Test
	public void d01a_externalDocs_default() throws Exception {
		ExternalDocumentation x = getSwagger(new D1()).getExternalDocs();
		assertEquals(null, x);
	}
	@Test
	public void d01b_externalDocs_default_withFile() throws Exception {
		ExternalDocumentation x = getSwaggerWithFile(new D1()).getExternalDocs();
		assertObject(x).asJson().is("{description:'s-description',url:'s-url'}");
	}


	@Rest(
		swagger=@Swagger("{externalDocs:{description:'a-description',url:'a-url'}}")
	)
	public static class D2 {}

	@Test
	public void d02a_externalDocs_Swagger_value() throws Exception {
		ExternalDocumentation x = getSwagger(new D2()).getExternalDocs();
		assertObject(x).asJson().is("{description:'a-description',url:'a-url'}");
	}
	@Test
	public void d02b_externalDocs_Swagger_value_withFile() throws Exception {
		ExternalDocumentation x = getSwaggerWithFile(new D2()).getExternalDocs();
		assertObject(x).asJson().is("{description:'a-description',url:'a-url'}");
	}


	@Rest(
		swagger=@Swagger(
			value="{externalDocs:{description:'a-description',url:'a-url'}}",
			externalDocs=@ExternalDocs(description="b-description",url="b-url")
		)
	)
	public static class D3 {}

	@Test
	public void d03a_externalDocs_Swagger_externalDocs() throws Exception {
		ExternalDocumentation x = getSwagger(new D3()).getExternalDocs();
		assertObject(x).asJson().is("{description:'b-description',url:'b-url'}");
	}
	@Test
	public void d03b_externalDocs_Swagger_externalDocs_withFile() throws Exception {
		ExternalDocumentation x = getSwaggerWithFile(new D3()).getExternalDocs();
		assertObject(x).asJson().is("{description:'b-description',url:'b-url'}");
	}

	@Rest(
		swagger=@Swagger(
			value="{info:{externalDocs:{description:'a-description',url:'a-url'}}}",
			externalDocs=@ExternalDocs(" description:'b-description', url:'b-url' ")
			)
	)
	public static class D4 {}

	@Test
	public void d04a_externalDocs_Swagger_externalDocs() throws Exception {
		ExternalDocumentation x = getSwagger(new D4()).getExternalDocs();
		assertObject(x).asJson().is("{description:'b-description',url:'b-url'}");
	}
	@Test
	public void d04b_externalDocs_Swagger_externalDocs_withFile() throws Exception {
		ExternalDocumentation x = getSwaggerWithFile(new D4()).getExternalDocs();
		assertObject(x).asJson().is("{description:'b-description',url:'b-url'}");
	}

	@Rest(
		swagger=@Swagger(
			value="{externalDocs:{description:'a-description',url:'a-url'}}",
			externalDocs=@ExternalDocs("{description:'$L{foo}',url:'$L{bar}'}")
		),
		messages="BasicRestInfoProviderTest"
	)
	public static class D5 {}

	@Test
	public void d05a_externalDocs_Swagger_externalDocs_localised() throws Exception {
		ExternalDocumentation x = getSwagger(new D5()).getExternalDocs();
		assertObject(x).asJson().is("{description:'l-foo',url:'l-bar'}");
	}
	@Test
	public void d05b_externalDocs_Swagger_externalDocs_localised_withFile() throws Exception {
		ExternalDocumentation x = getSwaggerWithFile(new D5()).getExternalDocs();
		assertObject(x).asJson().is("{description:'l-foo',url:'l-bar'}");
	}

	//------------------------------------------------------------------------------------------------------------------
	// /paths/<path>/<method>
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class E1 {
		@RestGet(path="/path/{foo}")
		public X a() {
			return null;
		}
	}

	@Test
	public void e01a_operation_summary_default() throws Exception {
		Operation x = getSwagger(new E1()).getPaths().get("/path/{foo}").get("get");
		assertEquals("a", x.getOperationId());
		assertEquals(null, x.getSummary());
		assertEquals(null, x.getDescription());
		assertEquals(null, x.getDeprecated());
		assertEquals(null, x.getSchemes());
	}
	@Test
	public void e01b_operation_summary_default_withFile() throws Exception {
		Operation x = getSwaggerWithFile(new E1()).getPaths().get("/path/{foo}").get("get");
		assertEquals("s-operationId", x.getOperationId());
		assertEquals("s-summary", x.getSummary());
		assertEquals("s-description", x.getDescription());
		assertObject(x.getDeprecated()).asJson().is("true");
		assertObject(x.getSchemes()).asJson().is("['s-scheme']");
	}

	@Rest(
		swagger=@Swagger(
			"paths:{'/path/{foo}':{get:{operationId:'a-operationId',summary:'a-summary',description:'a-description',deprecated:false,schemes:['a-scheme']}}}"
		)
	)
	public static class E2 {
		@RestGet(path="/path/{foo}")
		public X a() {
			return null;
		}
	}

	@Test
	public void e02a_operation_summary_swaggerOnClass() throws Exception {
		Operation x = getSwagger(new E2()).getPaths().get("/path/{foo}").get("get");
		assertEquals("a-operationId", x.getOperationId());
		assertEquals("a-summary", x.getSummary());
		assertEquals("a-description", x.getDescription());
		assertObject(x.getDeprecated()).asJson().is("false");
		assertObject(x.getSchemes()).asJson().is("['a-scheme']");
	}
	@Test
	public void e02b_operation_summary_swaggerOnClass_withFile() throws Exception {
		Operation x = getSwaggerWithFile(new E2()).getPaths().get("/path/{foo}").get("get");
		assertEquals("a-operationId", x.getOperationId());
		assertEquals("a-summary", x.getSummary());
		assertEquals("a-description", x.getDescription());
		assertObject(x.getDeprecated()).asJson().is("false");
		assertObject(x.getSchemes()).asJson().is("['a-scheme']");
	}

	@Rest(
		swagger=@Swagger(
			"paths:{'/path/{foo}':{get:{operationId:'a-operationId',summary:'a-summary',description:'a-description',deprecated:false,schemes:['a-scheme']}}}"
		)
	)
	public static class E3 {
		@RestGet(path="/path/{foo}",
			swagger=@OpSwagger("operationId:'b-operationId',summary:'b-summary',description:'b-description',deprecated:false,schemes:['b-scheme']")
		)
		public X a() {
			return null;
		}
	}

	@Test
	public void e03a_operation_summary_swaggerOnMethod() throws Exception {
		Operation x = getSwagger(new E3()).getPaths().get("/path/{foo}").get("get");
		assertEquals("b-operationId", x.getOperationId());
		assertEquals("b-summary", x.getSummary());
		assertEquals("b-description", x.getDescription());
		assertObject(x.getDeprecated()).asJson().is("false");
		assertObject(x.getSchemes()).asJson().is("['b-scheme']");
	}
	@Test
	public void e03b_operation_summary_swaggerOnMethod_withFile() throws Exception {
		Operation x = getSwaggerWithFile(new E3()).getPaths().get("/path/{foo}").get("get");
		assertEquals("b-operationId", x.getOperationId());
		assertEquals("b-summary", x.getSummary());
		assertEquals("b-description", x.getDescription());
		assertObject(x.getDeprecated()).asJson().is("false");
		assertObject(x.getSchemes()).asJson().is("['b-scheme']");
	}

	@Rest(
		swagger=@Swagger(
			"paths:{'/path/{foo}':{get:{operationId:'a-operationId',summary:'a-summary',description:'a-description',deprecated:false,schemes:['a-scheme']}}}"
		)
	)
	public static class E4 {
		@RestGet(path="/path/{foo}",
			swagger=@OpSwagger(
				operationId="c-operationId",
				summary="c-summary",
				description="c-description",
				deprecated="false",
				schemes="d-scheme-1, d-scheme-2"
			)
		)
		public X a() {
			return null;
		}
	}

	@Test
	public void e04a_operation_summary_swaggerOnAnnotation() throws Exception {
		Operation x = getSwagger(new E4()).getPaths().get("/path/{foo}").get("get");
		assertEquals("c-operationId", x.getOperationId());
		assertEquals("c-summary", x.getSummary());
		assertEquals("c-description", x.getDescription());
		assertObject(x.getSchemes()).asJson().is("['d-scheme-1','d-scheme-2']");
	}
	@Test
	public void e04b_operation_summary_swaggerOnAnnotation_withFile() throws Exception {
		Operation x = getSwaggerWithFile(new E4()).getPaths().get("/path/{foo}").get("get");
		assertEquals("c-operationId", x.getOperationId());
		assertEquals("c-summary", x.getSummary());
		assertEquals("c-description", x.getDescription());
		assertObject(x.getSchemes()).asJson().is("['d-scheme-1','d-scheme-2']");
	}

	@Rest(
		messages="BasicRestInfoProviderTest",
		swagger=@Swagger(
			"paths:{'/path/{foo}':{get:{operationId:'a-operationId',summary:'a-summary',description:'a-description',deprecated:false,schemes:['a-scheme']}}}"
		)
	)
	public static class E5 {
		@RestGet(path="/path/{foo}",
			swagger=@OpSwagger(
				summary="$L{foo}",
				operationId="$L{foo}",
				description="$L{foo}",
				deprecated="$L{false}",
				schemes="$L{foo}"
			)
		)
		public X a() {
			return null;
		}
	}

	@Test
	public void e05a_operation_summary_swaggerOnAnnotation_localized() throws Exception {
		Operation x = getSwagger(new E5()).getPaths().get("/path/{foo}").get("get");
		assertEquals("l-foo", x.getOperationId());
		assertEquals("l-foo", x.getSummary());
		assertEquals("l-foo", x.getDescription());
		assertObject(x.getDeprecated()).asJson().is("false");
		assertObject(x.getSchemes()).asJson().is("['l-foo']");
	}
	@Test
	public void e05b_operation_summary_swaggerOnAnnotation_localized_withFile() throws Exception {
		Operation x = getSwaggerWithFile(new E5()).getPaths().get("/path/{foo}").get("get");
		assertEquals("l-foo", x.getOperationId());
		assertEquals("l-foo", x.getSummary());
		assertEquals("l-foo", x.getDescription());
		assertObject(x.getDeprecated()).asJson().is("false");
		assertObject(x.getSchemes()).asJson().is("['l-foo']");
	}

	@Rest(
		swagger=@Swagger(
			"paths:{'/path/{foo}':{get:{summary:'a-summary',description:'a-description'}}}"
		)
	)
	public static class E6 {
		@RestGet(path="/path/{foo}",
			summary="d-summary",
			description="d-description"
		)
		public X a() {
			return null;
		}
	}

	@Test
	public void e06a_operation_summary_RestOp() throws Exception {
		Operation x = getSwagger(new E6()).getPaths().get("/path/{foo}").get("get");
		assertEquals("a-summary", x.getSummary());
		assertEquals("a-description", x.getDescription());
	}
	@Test
	public void e06b_operation_summary_RestOp_withFile() throws Exception {
		Operation x = getSwaggerWithFile(new E6()).getPaths().get("/path/{foo}").get("get");
		assertEquals("a-summary", x.getSummary());
		assertEquals("a-description", x.getDescription());
	}

	@Rest(
		swagger=@Swagger(
			"paths:{'/path/{foo}':{get:{}}}"
		)
	)
	public static class E7 {
		@RestGet(path="/path/{foo}",
			summary="d-summary",
			description="d-description"
		)
		public X a() {
			return null;
		}
	}

	@Test
	public void e07a_operation_summary_RestOp() throws Exception {
		Operation x = getSwagger(new E7()).getPaths().get("/path/{foo}").get("get");
		assertEquals("d-summary", x.getSummary());
		assertEquals("d-description", x.getDescription());
	}
	@Test
	public void e07b_operation_summary_RestOp_withFile() throws Exception {
		Operation x = getSwaggerWithFile(new E7()).getPaths().get("/path/{foo}").get("get");
		assertEquals("d-summary", x.getSummary());
		assertEquals("d-description", x.getDescription());
	}

	//------------------------------------------------------------------------------------------------------------------
	// /paths/<path>/<method>/tags
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class F1 {

		@RestGet(path="/path/{foo}")
		public X a() {
			return null;
		}
	}

	@Test
	public void f01_operation_tags_default() throws Exception {
		assertObject(getSwagger(new F1()).getPaths().get("/path/{foo}").get("get").getTags()).asJson().is("null");
		assertObject(getSwaggerWithFile(new F1()).getPaths().get("/path/{foo}").get("get").getTags()).asJson().is("['s-tag']");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{tags:['a-tag']}}}"))
	public static class F2 {
		@RestGet(path="/path/{foo}")
		public X a() {
			return null;
		}
	}

	@Test
	public void f02_operation_tags_swaggerOnClass() throws Exception {
		assertObject(getSwagger(new F2()).getPaths().get("/path/{foo}").get("get").getTags()).asJson().is("['a-tag']");
		assertObject(getSwaggerWithFile(new F2()).getPaths().get("/path/{foo}").get("get").getTags()).asJson().is("['a-tag']");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{tags:['a-tag']}}}"))
	public static class F3 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger("tags:['b-tag']"))
		public X a() {
			return null;
		}
	}

	@Test
	public void f03_operation_tags_swaggerOnMethod() throws Exception {
		assertObject(getSwagger(new F3()).getPaths().get("/path/{foo}").get("get").getTags()).asJson().is("['b-tag']");
		assertObject(getSwaggerWithFile(new F3()).getPaths().get("/path/{foo}").get("get").getTags()).asJson().is("['b-tag']");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{tags:['a-tag']}}}"))
	public static class F4 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger(tags="['c-tag-1','c-tag-2']"))
		public X a() {
			return null;
		}
	}

	@Test
	public void f04_operation_tags_swaggerOnAnnotation() throws Exception {
		assertObject(getSwagger(new F4()).getPaths().get("/path/{foo}").get("get").getTags()).asJson().is("['c-tag-1','c-tag-2']");
		assertObject(getSwaggerWithFile(new F4()).getPaths().get("/path/{foo}").get("get").getTags()).asJson().is("['c-tag-1','c-tag-2']");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{tags:['a-tag']}}}"))
	public static class F5 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger(tags="c-tag-1, c-tag-2"))
		public X a() {
			return null;
		}
	}

	@Test
	public void f05_operation_tags_swaggerOnAnnotation() throws Exception {
		assertObject(getSwagger(new F5()).getPaths().get("/path/{foo}").get("get").getTags()).asJson().is("['c-tag-1','c-tag-2']");
		assertObject(getSwaggerWithFile(new F5()).getPaths().get("/path/{foo}").get("get").getTags()).asJson().is("['c-tag-1','c-tag-2']");
	}

	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}':{get:{tags:'a-tags'}}}"))
	public static class F6 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger(tags="$L{foo}"))
		public X a() {
			return null;
		}
	}

	@Test
	public void f06_operation_tags_swaggerOnAnnotation_localized() throws Exception {
		assertObject(getSwagger(new F6()).getPaths().get("/path/{foo}").get("get").getTags()).asJson().is("['l-foo']");
		assertObject(getSwaggerWithFile(new F6()).getPaths().get("/path/{foo}").get("get").getTags()).asJson().is("['l-foo']");
	}

	//------------------------------------------------------------------------------------------------------------------
	// /paths/<path>/<method>/externalDocs
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class G1 {

		@RestGet(path="/path/{foo}")
		public X a() {
			return null;
		}
	}

	@Test
	public void g01_operation_externalDocs_default() throws Exception {
		assertObject(getSwagger(new G1()).getPaths().get("/path/{foo}").get("get").getExternalDocs()).asJson().is("null");
		assertObject(getSwaggerWithFile(new G1()).getPaths().get("/path/{foo}").get("get").getExternalDocs()).asJson().is("{description:'s-description',url:'s-url'}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{externalDocs:{description:'a-description',url:'a-url'}}}}"))
	public static class G2 {
		@RestGet(path="/path/{foo}")
		public X a() {
			return null;
		}
	}

	@Test
	public void g02_operation_externalDocs_swaggerOnClass() throws Exception {
		assertObject(getSwagger(new G2()).getPaths().get("/path/{foo}").get("get").getExternalDocs()).asJson().is("{description:'a-description',url:'a-url'}");
		assertObject(getSwaggerWithFile(new G2()).getPaths().get("/path/{foo}").get("get").getExternalDocs()).asJson().is("{description:'a-description',url:'a-url'}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{externalDocs:{description:'a-description',url:'a-url'}}}}"))
	public static class G3 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger("externalDocs:{description:'b-description',url:'b-url'}"))
		public X a() {
			return null;
		}
	}

	@Test
	public void g03_operation_externalDocs_swaggerOnMethod() throws Exception {
		assertObject(getSwagger(new G3()).getPaths().get("/path/{foo}").get("get").getExternalDocs()).asJson().is("{description:'b-description',url:'b-url'}");
		assertObject(getSwaggerWithFile(new G3()).getPaths().get("/path/{foo}").get("get").getExternalDocs()).asJson().is("{description:'b-description',url:'b-url'}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{externalDocs:{description:'a-description',url:'a-url'}}}}"))
	public static class G4 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger(externalDocs=@ExternalDocs(description="c-description",url="c-url")))
		public X a() {
			return null;
		}
	}

	@Test
	public void g04_operation_externalDocs_swaggerOnAnnotation() throws Exception {
		assertObject(getSwagger(new G4()).getPaths().get("/path/{foo}").get("get").getExternalDocs()).asJson().is("{description:'c-description',url:'c-url'}");
		assertObject(getSwaggerWithFile(new G4()).getPaths().get("/path/{foo}").get("get").getExternalDocs()).asJson().is("{description:'c-description',url:'c-url'}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{externalDocs:{description:'a-description',url:'a-url'}}}}"))
	public static class G5 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger(externalDocs=@ExternalDocs("{description:'d-description',url:'d-url'}")))
		public X a() {
			return null;
		}
	}

	@Test
	public void g05_operation_externalDocs_swaggerOnAnnotation() throws Exception {
		assertObject(getSwagger(new G5()).getPaths().get("/path/{foo}").get("get").getExternalDocs()).asJson().is("{description:'d-description',url:'d-url'}");
		assertObject(getSwaggerWithFile(new G5()).getPaths().get("/path/{foo}").get("get").getExternalDocs()).asJson().is("{description:'d-description',url:'d-url'}");
	}

	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}':{get:{externalDocs:{description:'a-description',url:'a-url'}}}}"))
	public static class G6 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger(externalDocs=@ExternalDocs("{description:'$L{foo}',url:'$L{foo}'}")))
		public X a() {
			return null;
		}
	}

	@Test
	public void g06_operation_externalDocs_swaggerOnAnnotation_localized() throws Exception {
		assertObject(getSwagger(new G6()).getPaths().get("/path/{foo}").get("get").getExternalDocs()).asJson().is("{description:'l-foo',url:'l-foo'}");
		assertObject(getSwaggerWithFile(new G6()).getPaths().get("/path/{foo}").get("get").getExternalDocs()).asJson().is("{description:'l-foo',url:'l-foo'}");
	}

	//------------------------------------------------------------------------------------------------------------------
	// /paths/<path>/<method>/consumes
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class H1 {

		@RestGet(path="/path/{foo}")
		public X a() {
			return null;
		}
	}

	@Test
	public void h01_operation_consumes_default() throws Exception {
		assertObject(getSwagger(new H1()).getPaths().get("/path/{foo}").get("get").getConsumes()).asJson().is("null");
		assertObject(getSwaggerWithFile(new H1()).getPaths().get("/path/{foo}").get("get").getConsumes()).asJson().is("['s-consumes']");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{consumes:['a-consumes']}}}"))
	public static class H2 {
		@RestGet(path="/path/{foo}")
		public X a() {
			return null;
		}
	}

	@Test
	public void h02_operation_consumes_swaggerOnClass() throws Exception {
		assertObject(getSwagger(new H2()).getPaths().get("/path/{foo}").get("get").getConsumes()).asJson().is("['a-consumes']");
		assertObject(getSwaggerWithFile(new H2()).getPaths().get("/path/{foo}").get("get").getConsumes()).asJson().is("['a-consumes']");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{consumes:['a-consumes']}}}"))
	public static class H3 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger("consumes:['b-consumes']"))
		public X a() {
			return null;
		}
	}

	@Test
	public void h03_operation_consumes_swaggerOnMethod() throws Exception {
		assertObject(getSwagger(new H3()).getPaths().get("/path/{foo}").get("get").getConsumes()).asJson().is("['b-consumes']");
		assertObject(getSwaggerWithFile(new H3()).getPaths().get("/path/{foo}").get("get").getConsumes()).asJson().is("['b-consumes']");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{consumes:['a-consumes']}}}"))
	public static class H4 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger(consumes="['c-consumes-1','c-consumes-2']"))
		public X a() {
			return null;
		}
	}

	@Test
	public void h04_operation_consumes_swaggerOnAnnotation() throws Exception {
		assertObject(getSwagger(new H4()).getPaths().get("/path/{foo}").get("get").getConsumes()).asJson().is("['c-consumes-1','c-consumes-2']");
		assertObject(getSwaggerWithFile(new H4()).getPaths().get("/path/{foo}").get("get").getConsumes()).asJson().is("['c-consumes-1','c-consumes-2']");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{consumes:['a-consumes']}}}"))
	public static class H5 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger(consumes="c-consumes-1, c-consumes-2"))
		public X a() {
			return null;
		}
	}

	@Test
	public void h05_operation_consumes_swaggerOnAnnotation() throws Exception {
		assertObject(getSwagger(new H5()).getPaths().get("/path/{foo}").get("get").getConsumes()).asJson().is("['c-consumes-1','c-consumes-2']");
		assertObject(getSwaggerWithFile(new H5()).getPaths().get("/path/{foo}").get("get").getConsumes()).asJson().is("['c-consumes-1','c-consumes-2']");
	}

	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}':{get:{consumes:['a-consumes']}}}"))
	public static class H6 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger(consumes="['$L{foo}']"))
		public X a() {
			return null;
		}
	}

	@Test
	public void h06_operation_consumes_swaggerOnAnnotation_localized() throws Exception {
		assertObject(getSwagger(new H6()).getPaths().get("/path/{foo}").get("get").getConsumes()).asJson().is("['l-foo']");
		assertObject(getSwaggerWithFile(new H6()).getPaths().get("/path/{foo}").get("get").getConsumes()).asJson().is("['l-foo']");
	}

	@Rest(parsers={JsonParser.class})
	public static class H7 {
		@RestPut(path="/path2/{foo}")
		public X a() {
			return null;
		}
	}

	@Test
	public void h07_operation_consumes_parsersOnClass() throws Exception {
		assertObject(getSwagger(new H7()).getPaths().get("/path2/{foo}").get("put").getConsumes()).asJson().is("null");
		assertObject(getSwaggerWithFile(new H7()).getPaths().get("/path2/{foo}").get("put").getConsumes()).asJson().is("null");
	}

	@Rest(parsers={JsonParser.class})
	public static class H8 {
		@RestPut(path="/path2/{foo}",parsers={XmlParser.class})
		public X a() {
			return null;
		}
		@RestPut(path="/b")
		public X b() {
			return null;
		}
	}

	@Test
	public void h08_operation_consumes_parsersOnClassAndMethod() throws Exception {
		assertObject(getSwagger(new H8()).getPaths().get("/path2/{foo}").get("put").getConsumes()).asJson().is("['text/xml','application/xml']");
		assertObject(getSwaggerWithFile(new H8()).getPaths().get("/path2/{foo}").get("put").getConsumes()).asJson().is("['text/xml','application/xml']");
	}

	@Rest(parsers={JsonParser.class},swagger=@Swagger("paths:{'/path2/{foo}':{put:{consumes:['a-consumes']}}}"))
	public static class H9 {
		@RestPut(path="/path2/{foo}",parsers={XmlParser.class})
		public X a() {
			return null;
		}
	}

	@Test
	public void h09_operation_consumes_parsersOnClassAndMethodWithSwagger() throws Exception {
		assertObject(getSwagger(new H9()).getPaths().get("/path2/{foo}").get("put").getConsumes()).asJson().is("['a-consumes']");
		assertObject(getSwaggerWithFile(new H9()).getPaths().get("/path2/{foo}").get("put").getConsumes()).asJson().is("['a-consumes']");
	}

	//------------------------------------------------------------------------------------------------------------------
	// /paths/<path>/<method>/produces
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class I1 {

		@RestGet(path="/path/{foo}")
		public X a() {
			return null;
		}
	}

	@Test
	public void i01_operation_produces_default() throws Exception {
		assertObject(getSwagger(new I1()).getPaths().get("/path/{foo}").get("get").getProduces()).asJson().is("null");
		assertObject(getSwaggerWithFile(new I1()).getPaths().get("/path/{foo}").get("get").getProduces()).asJson().is("['s-produces']");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{produces:['a-produces']}}}"))
	public static class I2 {
		@RestGet(path="/path/{foo}")
		public X a() {
			return null;
		}
	}

	@Test
	public void i02_operation_produces_swaggerOnClass() throws Exception {
		assertObject(getSwagger(new I2()).getPaths().get("/path/{foo}").get("get").getProduces()).asJson().is("['a-produces']");
		assertObject(getSwaggerWithFile(new I2()).getPaths().get("/path/{foo}").get("get").getProduces()).asJson().is("['a-produces']");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{produces:['a-produces']}}}"))
	public static class I3 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger("produces:['b-produces']"))
		public X a() {
			return null;
		}
	}

	@Test
	public void i03_operation_produces_swaggerOnMethod() throws Exception {
		assertObject(getSwagger(new I3()).getPaths().get("/path/{foo}").get("get").getProduces()).asJson().is("['b-produces']");
		assertObject(getSwaggerWithFile(new I3()).getPaths().get("/path/{foo}").get("get").getProduces()).asJson().is("['b-produces']");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{produces:['a-produces']}}}"))
	public static class I4 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger(produces="['c-produces-1','c-produces-2']"))
		public X a() {
			return null;
		}
	}

	@Test
	public void i04_operation_produces_swaggerOnAnnotation() throws Exception {
		assertObject(getSwagger(new I4()).getPaths().get("/path/{foo}").get("get").getProduces()).asJson().is("['c-produces-1','c-produces-2']");
		assertObject(getSwaggerWithFile(new I4()).getPaths().get("/path/{foo}").get("get").getProduces()).asJson().is("['c-produces-1','c-produces-2']");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}':{get:{produces:['a-produces']}}}"))
	public static class I5 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger(produces="c-produces-1, c-produces-2"))
		public X a() {
			return null;
		}
	}

	@Test
	public void i05_operation_produces_swaggerOnAnnotation() throws Exception {
		assertObject(getSwagger(new I5()).getPaths().get("/path/{foo}").get("get").getProduces()).asJson().is("['c-produces-1','c-produces-2']");
		assertObject(getSwaggerWithFile(new I5()).getPaths().get("/path/{foo}").get("get").getProduces()).asJson().is("['c-produces-1','c-produces-2']");
	}

	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}':{get:{produces:['a-produces']}}}"))
	public static class I6 {
		@RestGet(path="/path/{foo}",swagger=@OpSwagger(produces="['$L{foo}']"))
		public X a() {
			return null;
		}
	}

	@Test
	public void i06_operation_produces_swaggerOnAnnotation_localized() throws Exception {
		assertObject(getSwagger(new I6()).getPaths().get("/path/{foo}").get("get").getProduces()).asJson().is("['l-foo']");
		assertObject(getSwaggerWithFile(new I6()).getPaths().get("/path/{foo}").get("get").getProduces()).asJson().is("['l-foo']");
	}

	@Rest(serializers={JsonSerializer.class})
	public static class I7 {
		@RestPut(path="/path2/{foo}")
		public X a() {
			return null;
		}
	}

	@Test
	public void i07_operation_produces_serializersOnClass() throws Exception {
		assertObject(getSwagger(new I7()).getPaths().get("/path2/{foo}").get("put").getProduces()).asJson().is("null");
		assertObject(getSwaggerWithFile(new I7()).getPaths().get("/path2/{foo}").get("put").getProduces()).asJson().is("null");
	}

	@Rest(serializers={JsonSerializer.class})
	public static class I8 {
		@RestPut(path="/path2/{foo}",serializers={XmlSerializer.class})
		public X a() {
			return null;
		}
		@RestGet(path="/b")
		public X b() {
			return null;
		}
	}

	@Test
	public void i08_operation_produces_serializersOnClassAndMethod() throws Exception {
		assertObject(getSwagger(new I8()).getPaths().get("/path2/{foo}").get("put").getProduces()).asJson().is("['text/xml']");
		assertObject(getSwaggerWithFile(new I8()).getPaths().get("/path2/{foo}").get("put").getProduces()).asJson().is("['text/xml']");
	}

	@Rest(serializers={JsonSerializer.class},swagger=@Swagger("paths:{'/path2/{foo}':{put:{produces:['a-produces']}}}"))
	public static class I9 {
		@RestPut(path="/path2/{foo}",serializers={XmlSerializer.class})
		public X a() {
			return null;
		}
	}

	@Test
	public void i09_operation_produces_serializersOnClassAndMethodWithSwagger() throws Exception {
		assertObject(getSwagger(new I9()).getPaths().get("/path2/{foo}").get("put").getProduces()).asJson().is("['a-produces']");
		assertObject(getSwaggerWithFile(new I9()).getPaths().get("/path2/{foo}").get("put").getProduces()).asJson().is("['a-produces']");
	}

	//------------------------------------------------------------------------------------------------------------------
	// /paths/<path>/<method>/deprecated
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class J1 {
		@RestGet(path="/path2/{foo}")
		@Deprecated
		public X a() {
			return null;
		}
	}

	@Test
	public void j01_operation_deprecated_Deprecated() throws Exception {
		assertObject(getSwagger(new J1()).getPaths().get("/path2/{foo}").get("get").getDeprecated()).asJson().is("true");
		assertObject(getSwaggerWithFile(new J1()).getPaths().get("/path2/{foo}").get("get").getDeprecated()).asJson().is("true");
	}

	@Rest
	@Deprecated
	public static class J2 {
		@RestGet(path="/path2/{foo}")
		public X a() {
			return null;
		}
	}

	@Test
	public void j02_operation_deprecated_Deprecated() throws Exception {
		assertObject(getSwagger(new J2()).getPaths().get("/path2/{foo}").get("get").getDeprecated()).asJson().is("true");
		assertObject(getSwaggerWithFile(new J2()).getPaths().get("/path2/{foo}").get("get").getDeprecated()).asJson().is("true");
	}

	//------------------------------------------------------------------------------------------------------------------
	// /paths/<path>/<method>/parameters/query
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class K1 {
		@RestGet(path="/path/{foo}/query")
		public X a(@Query("foo") X foo) {
			return null;
		}
	}

	@Test
	public void k01a_query_type_default() throws Exception {
		ParameterInfo x = getSwagger(new K1()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo");
		assertEquals("object", x.getType());
		assertEquals(null, x.getDescription());
		assertEquals(null, x.getRequired());
		assertEquals(null, x.getAllowEmptyValue());
		assertEquals(null, x.getExclusiveMaximum());
		assertEquals(null, x.getExclusiveMinimum());
		assertEquals(null, x.getUniqueItems());
		assertEquals(null, x.getFormat());
		assertEquals(null, x.getCollectionFormat());
		assertEquals(null, x.getPattern());
		assertEquals(null, x.getMaximum());
		assertEquals(null, x.getMinimum());
		assertEquals(null, x.getMultipleOf());
		assertEquals(null, x.getMaxLength());
		assertEquals(null, x.getMinLength());
		assertEquals(null, x.getMaxItems());
		assertEquals(null, x.getMinItems());
	}
	@Test
	public void k01b_query_type_default_withFile() throws Exception {
		ParameterInfo x = getSwaggerWithFile(new K1()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo");
		assertEquals("string", x.getType());
		assertEquals("s-description", x.getDescription());
		assertObject(x.getRequired()).asJson().is("true");
		assertObject(x.getAllowEmptyValue()).asJson().is("true");
		assertObject(x.getExclusiveMaximum()).asJson().is("true");
		assertObject(x.getExclusiveMinimum()).asJson().is("true");
		assertObject(x.getUniqueItems()).asJson().is("true");
		assertEquals("s-format", x.getFormat());
		assertEquals("s-collectionFormat", x.getCollectionFormat());
		assertEquals("s-pattern", x.getPattern());
		assertObject(x.getMaximum()).asJson().is("1.0");
		assertObject(x.getMinimum()).asJson().is("1.0");
		assertObject(x.getMultipleOf()).asJson().is("1.0");
		assertObject(x.getMaxLength()).asJson().is("1");
		assertObject(x.getMinLength()).asJson().is("1");
		assertObject(x.getMaxItems()).asJson().is("1");
		assertObject(x.getMinItems()).asJson().is("1");
	}

	@Rest(
		swagger=@Swagger({
			"paths:{'/path/{foo}/query':{get:{parameters:[{'in':'query',",
				"name:'foo',",
				"type:'int32',",
				"description:'a-description',",
				"required:false,",
				"allowEmptyValue:false,",
				"exclusiveMaximum:false,",
				"exclusiveMinimum:false,",
				"uniqueItems:false,",
				"format:'a-format',",
				"collectionFormat:'a-collectionFormat',",
				"pattern:'a-pattern',",
				"maximum:2.0,",
				"minimum:2.0,",
				"multipleOf:2.0,",
				"maxLength:2,",
				"minLength:2,",
				"maxItems:2,",
				"minItems:2",
			"}]}}}"
		})
	)
	public static class K2 {
		@RestGet(path="/path/{foo}/query")
		public X a(@Query("foo") X foo) {
			return null;
		}
	}

	@Test
	public void k02a_query_type_swaggerOnClass() throws Exception {
		ParameterInfo x = getSwagger(new K2()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo");
		assertEquals("int32", x.getType());
		assertEquals("a-description", x.getDescription());
		assertObject(x.getRequired()).asJson().is("false");
		assertObject(x.getAllowEmptyValue()).asJson().is("false");
		assertObject(x.getExclusiveMaximum()).asJson().is("false");
		assertObject(x.getExclusiveMinimum()).asJson().is("false");
		assertObject(x.getUniqueItems()).asJson().is("false");
		assertEquals("a-format", x.getFormat());
		assertEquals("a-collectionFormat", x.getCollectionFormat());
		assertEquals("a-pattern", x.getPattern());
		assertObject(x.getMaximum()).asJson().is("2.0");
		assertObject(x.getMinimum()).asJson().is("2.0");
		assertObject(x.getMultipleOf()).asJson().is("2.0");
		assertObject(x.getMaxLength()).asJson().is("2");
		assertObject(x.getMinLength()).asJson().is("2");
		assertObject(x.getMaxItems()).asJson().is("2");
		assertObject(x.getMinItems()).asJson().is("2");
	}
	@Test
	public void k02b_query_type_swaggerOnClass_withFile() throws Exception {
		ParameterInfo x = getSwaggerWithFile(new K2()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo");
		assertEquals("int32", x.getType());
		assertEquals("a-description", x.getDescription());
		assertObject(x.getRequired()).asJson().is("false");
		assertObject(x.getAllowEmptyValue()).asJson().is("false");
		assertObject(x.getExclusiveMaximum()).asJson().is("false");
		assertObject(x.getExclusiveMinimum()).asJson().is("false");
		assertObject(x.getUniqueItems()).asJson().is("false");
		assertEquals("a-format", x.getFormat());
		assertEquals("a-collectionFormat", x.getCollectionFormat());
		assertEquals("a-pattern", x.getPattern());
		assertObject(x.getMaximum()).asJson().is("2.0");
		assertObject(x.getMinimum()).asJson().is("2.0");
		assertObject(x.getMultipleOf()).asJson().is("2.0");
		assertObject(x.getMaxLength()).asJson().is("2");
		assertObject(x.getMinLength()).asJson().is("2");
		assertObject(x.getMaxItems()).asJson().is("2");
		assertObject(x.getMinItems()).asJson().is("2");
	}

	@Rest(
		swagger=@Swagger({
			"paths:{'/path/{foo}/query':{get:{parameters:[{",
				"'in':'query',",
				"name:'foo',",
				"type:'int32',",
				"description:'a-description',",
				"required:false,",
				"allowEmptyValue:false,",
				"exclusiveMaximum:false,",
				"exclusiveMinimum:false,",
				"uniqueItems:false,",
				"format:'a-format',",
				"collectionFormat:'a-collectionFormat',",
				"pattern:'a-pattern',",
				"maximum:2.0,",
				"minimum:2.0,",
				"multipleOf:2.0,",
				"maxLength:2,",
				"minLength:2,",
				"maxItems:2,",
				"minItems:2",
			"}]}}}"
		})
	)
	public static class K3 {
		@RestGet(path="/path/{foo}/query",
			swagger=@OpSwagger({
				"parameters:[{",
					"'in':'query',",
					"name:'foo',",
					"type:'int64',",
					"description:'b-description',",
					"required:'true',",
					"allowEmptyValue:'true',",
					"exclusiveMaximum:'true',",
					"exclusiveMinimum:'true',",
					"uniqueItems:'true',",
					"format:'b-format',",
					"collectionFormat:'b-collectionFormat',",
					"pattern:'b-pattern',",
					"maximum:3.0,",
					"minimum:3.0,",
					"multipleOf:3.0,",
					"maxLength:3,",
					"minLength:3,",
					"maxItems:3,",
					"minItems:3",
				"}]"
			}))
		public X a() {
			return null;
		}
	}

	@Test
	public void k03a_query_type_swaggerOnMethod() throws Exception {
		ParameterInfo x = getSwagger(new K3()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo");
		assertEquals("int64", x.getType());
		assertEquals("b-description", x.getDescription());
		assertObject(x.getRequired()).asJson().is("true");
		assertObject(x.getAllowEmptyValue()).asJson().is("true");
		assertObject(x.getExclusiveMaximum()).asJson().is("true");
		assertObject(x.getExclusiveMinimum()).asJson().is("true");
		assertObject(x.getUniqueItems()).asJson().is("true");
		assertEquals("b-format", x.getFormat());
		assertEquals("b-collectionFormat", x.getCollectionFormat());
		assertEquals("b-pattern", x.getPattern());
		assertObject(x.getMaximum()).asJson().is("3.0");
		assertObject(x.getMinimum()).asJson().is("3.0");
		assertObject(x.getMultipleOf()).asJson().is("3.0");
		assertObject(x.getMaxLength()).asJson().is("3");
		assertObject(x.getMinLength()).asJson().is("3");
		assertObject(x.getMaxItems()).asJson().is("3");
		assertObject(x.getMinItems()).asJson().is("3");
	}
	@Test
	public void k03b_query_type_swaggerOnMethod_withFile() throws Exception {
		ParameterInfo x = getSwaggerWithFile(new K3()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo");
		assertEquals("int64", x.getType());
		assertEquals("b-description", x.getDescription());
		assertObject(x.getRequired()).asJson().is("true");
		assertObject(x.getAllowEmptyValue()).asJson().is("true");
		assertObject(x.getExclusiveMaximum()).asJson().is("true");
		assertObject(x.getExclusiveMinimum()).asJson().is("true");
		assertObject(x.getUniqueItems()).asJson().is("true");
		assertEquals("b-format", x.getFormat());
		assertEquals("b-collectionFormat", x.getCollectionFormat());
		assertEquals("b-pattern", x.getPattern());
		assertObject(x.getMaximum()).asJson().is("3.0");
		assertObject(x.getMinimum()).asJson().is("3.0");
		assertObject(x.getMultipleOf()).asJson().is("3.0");
		assertObject(x.getMaxLength()).asJson().is("3");
		assertObject(x.getMinLength()).asJson().is("3");
		assertObject(x.getMaxItems()).asJson().is("3");
		assertObject(x.getMinItems()).asJson().is("3");
	}

	//------------------------------------------------------------------------------------------------------------------
	// /paths/<path>/<method>/parameters/query/example
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class L1 {
		@RestGet(path="/path/{foo}/query")
		public X a(@Query("foo") X foo) {
			return null;
		}
	}

	@Test
	public void l01_query_example_default() throws Exception {
		assertEquals(null, getSwagger(new L1()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo").getExample());
		assertEquals("{id:1}", getSwaggerWithFile(new L1()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo").getExample());
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/query':{get:{parameters:[{'in':'query',name:'foo',example:'{id:2}'}]}}}"))
	public static class L2 {
		@RestGet(path="/path/{foo}/query")
		public X a(@Query("foo") X foo) {
			return null;
		}
	}

	@Test
	public void l02_query_example_swaggerOnClass() throws Exception {
		assertEquals("{id:2}", getSwagger(new L2()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo").getExample());
		assertEquals("{id:2}", getSwaggerWithFile(new L2()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo").getExample());
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/query':{get:{parameters:[{'in':'query',name:'foo',example:'{id:2}'}]}}}"))
	public static class L3 {
		@RestGet(path="/path/{foo}/query",swagger=@OpSwagger("parameters:[{'in':'query',name:'foo',example:'{id:3}'}]"))
		public X a() {
			return null;
		}
	}

	@Test
	public void l03_query_example_swaggerOnMethod() throws Exception {
		assertEquals("{id:3}", getSwagger(new L3()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo").getExample());
		assertEquals("{id:3}", getSwaggerWithFile(new L3()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo").getExample());
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/query':{get:{parameters:[{'in':'query',name:'foo',example:'{id:2}'}]}}}"))
	public static class L4 {
		@RestGet(path="/path/{foo}/query")
		public X a(@Query(n="foo",ex="{id:4}") X foo) {
			return null;
		}
	}

	@Test
	public void l04_query_example_swaggerOnAnnotation() throws Exception {
		assertEquals("{id:4}", getSwagger(new L4()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo").getExample());
		assertEquals("{id:4}", getSwaggerWithFile(new L4()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo").getExample());
	}

	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}/query':{get:{parameters:[{'in':'query',name:'foo',example:'{id:2}'}]}}}"))
	public static class L5 {
		@RestGet(path="/path/{foo}/query")
		public X a(@Query(n="foo",ex="{id:$L{5}}") X foo) {
			return null;
		}
	}

	@Test
	public void l05_query_example_swaggerOnAnnotation_localized() throws Exception {
		assertEquals("{id:5}", getSwagger(new L5()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo").getExample());
		assertEquals("{id:5}", getSwaggerWithFile(new L5()).getPaths().get("/path/{foo}/query").get("get").getParameter("query", "foo").getExample());
	}

	//------------------------------------------------------------------------------------------------------------------
	// /paths/<path>/<method>/parameters/body/examples
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class M1 {
		@RestGet(path="/path/{foo}/body")
		public X a(@Body X foo) {
			return null;
		}
	}

	@Test
	public void m01_body_examples_default() throws Exception {
		assertEquals(null, getSwagger(new M1()).getPaths().get("/path/{foo}/body").get("get").getParameter("body",null).getExamples());
		assertObject(getSwaggerWithFile(new M1()).getPaths().get("/path/{foo}/body").get("get").getParameter("body",null).getExamples()).asJson().is("{foo:'a'}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/body':{get:{parameters:[{'in':'body',examples:{foo:'b'}}]}}}"))
	public static class M2 {
		@RestGet(path="/path/{foo}/body")
		public X a(@Body X foo) {
			return null;
		}
	}

	@Test
	public void m02_body_examples_swaggerOnClass() throws Exception {
		assertObject(getSwagger(new M2()).getPaths().get("/path/{foo}/body").get("get").getParameter("body",null).getExamples()).asJson().is("{foo:'b'}");
		assertObject(getSwaggerWithFile(new M2()).getPaths().get("/path/{foo}/body").get("get").getParameter("body",null).getExamples()).asJson().is("{foo:'b'}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/body':{get:{parameters:[{'in':'body',examples:{foo:'b'}}]}}}"))
	public static class M3 {
		@RestGet(path="/path/{foo}/body",swagger=@OpSwagger("parameters:[{'in':'body',examples:{foo:'c'}}]"))
		public X a() {
			return null;
		}
	}

	@Test
	public void m03_body_examples_swaggerOnMethods() throws Exception {
		assertObject(getSwagger(new M3()).getPaths().get("/path/{foo}/body").get("get").getParameter("body",null).getExamples()).asJson().is("{foo:'c'}");
		assertObject(getSwaggerWithFile(new M3()).getPaths().get("/path/{foo}/body").get("get").getParameter("body",null).getExamples()).asJson().is("{foo:'c'}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/body':{get:{parameters:[{'in':'body',examples:{foo:'b'}}]}}}"))
	public static class M4 {
		@RestGet(path="/path/{foo}/body")
		public X a(@Body(exs="{foo:'d'}") X foo) {
			return null;
		}
	}

	@Test
	public void m04_body_examples_swaggerOnAnnotation() throws Exception {
		assertObject(getSwagger(new M4()).getPaths().get("/path/{foo}/body").get("get").getParameter("body",null).getExamples()).asJson().is("{foo:'d'}");
		assertObject(getSwaggerWithFile(new M4()).getPaths().get("/path/{foo}/body").get("get").getParameter("body",null).getExamples()).asJson().is("{foo:'d'}");
	}

	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}/body':{get:{parameters:[{'in':'body',examples:{foo:'b'}}]}}}"))
	public static class M5 {
		@RestGet(path="/path/{foo}/body")
		public X a(@Body(exs="{foo:'$L{foo}'}") X foo) {
			return null;
		}
	}

	@Test
	public void m05_body_examples_swaggerOnAnnotation_localized() throws Exception {
		assertObject(getSwagger(new M5()).getPaths().get("/path/{foo}/body").get("get").getParameter("body",null).getExamples()).asJson().is("{foo:'l-foo'}");
		assertObject(getSwaggerWithFile(new M5()).getPaths().get("/path/{foo}/body").get("get").getParameter("body",null).getExamples()).asJson().is("{foo:'l-foo'}");
	}

	//------------------------------------------------------------------------------------------------------------------
	// /paths/<path>/<method>/parameters/query/schema
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class N1 {
		@RestGet(path="/path/{foo}/query")
		public X a(@Query("foo") X foo) {
			return null;
		}
	}

	@Test
	public void n01_query_schema_default() throws Exception {
		assertObject(getSwagger(new N1()).getPaths().get("/path/{foo}/query").get("get").getParameter("query","foo").getSchema()).asJson().is("{properties:{a:{format:'int32',type:'integer'}}}");
		assertObject(getSwaggerWithFile(new N1()).getPaths().get("/path/{foo}/query").get("get").getParameter("query","foo").getSchema()).asJson().is("{'$ref':'#/definitions/Foo'}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/query':{get:{parameters:[{in:'query',name:'foo',schema:{$ref:'b'}}]}}}"))
	public static class N2 {
		@RestGet(path="/path/{foo}/query")
		public X a(@Query("foo") X foo) {
			return null;
		}
	}

	@Test
	public void n02_query_schema_swaggerOnClass() throws Exception {
		assertObject(getSwagger(new N2()).getPaths().get("/path/{foo}/query").get("get").getParameter("query","foo").getSchema()).asJson().is("{'$ref':'b'}");
		assertObject(getSwaggerWithFile(new N2()).getPaths().get("/path/{foo}/query").get("get").getParameter("query","foo").getSchema()).asJson().is("{'$ref':'b'}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/query':{get:{parameters:[{in:'query',name:'foo',schema:{$ref:'b'}}]}}}"))
	public static class N3 {

		@RestGet(path="/path/{foo}/query",swagger=@OpSwagger("parameters:[{'in':'query',name:'foo',schema:{$ref:'c'}}]"))
		public X a() {
			return null;
		}
	}

	@Test
	public void n03_query_schema_swaggerOnMethnt() throws Exception {
		assertObject(getSwagger(new N3()).getPaths().get("/path/{foo}/query").get("get").getParameter("query","foo").getSchema()).asJson().is("{'$ref':'c'}");
		assertObject(getSwaggerWithFile(new N3()).getPaths().get("/path/{foo}/query").get("get").getParameter("query","foo").getSchema()).asJson().is("{'$ref':'c'}");
	}

	//------------------------------------------------------------------------------------------------------------------
	// /paths/<path>/<method>/responses/<response>/description
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class O1a {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(Value<O1c> foo) {}
	}
	@Rest
	public static class O1b {
		@RestGet(path="/path/{foo}/responses/100")
		public O1c a() { return null;}
	}
	@Response(code=100)
	public static class O1c {
		public String a;
	}

	@Test
	public void o01a_responses_100_description_default() throws Exception {
		assertEquals("Continue", getSwagger(new O1a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
		assertEquals("s-100-description", getSwaggerWithFile(new O1a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
	}
	@Test
	public void o01b_responses_100_description_default() throws Exception {
		assertEquals("Continue", getSwagger(new O1b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
		assertEquals("s-100-description", getSwaggerWithFile(new O1b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{description:'a-100-description'}}}}}"))
	public static class O2 {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(@ResponseStatus Value<Integer> foo) {}
	}

	@Test
	public void o02_response_100_description_swaggerOnClass() throws Exception {
		assertEquals("a-100-description", getSwagger(new O2()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
		assertEquals("a-100-description", getSwaggerWithFile(new O2()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{description:'a-100-description'}}}}}"))
	public static class O3 {
		@RestGet(path="/path/{foo}/responses/100",swagger=@OpSwagger("responses:{100:{description:'b-100-description'}}"))
		public void a(@ResponseStatus Value<Integer> foo) {}
	}

	@Test
	public void o03_response_100_description_swaggerOnMethod() throws Exception {
		assertEquals("b-100-description", getSwagger(new O3()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
		assertEquals("b-100-description", getSwaggerWithFile(new O3()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{description:'a-100-description'}}}}}"))
	public static class O4a {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(Value<O4c> foo) {}
	}
	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{description:'a-100-description'}}}}}"))
	public static class O4b {
		@RestGet(path="/path/{foo}/responses/100")
		public O4c a() {return null;}
	}
	@Response(code=100,description="c-100-description")
	public static class O4c {}

	@Test
	public void o04a_response_100_description_swaggerOnAnnotation() throws Exception {
		assertEquals("c-100-description", getSwagger(new O4a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
		assertEquals("c-100-description", getSwaggerWithFile(new O4a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
	}
	@Test
	public void o04b_response_100_description_swaggerOnAnnotation() throws Exception {
		assertEquals("c-100-description", getSwagger(new O4b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
		assertEquals("c-100-description", getSwaggerWithFile(new O4b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
	}

	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{description:'a-100-description'}}}}}"))
	public static class O5a {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(Value<O5c> foo) {}
	}
	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{description:'a-100-description'}}}}}"))
	public static class O5b {
		@RestGet(path="/path/{foo}/responses/100")
		public O5c a() {return null;}
	}
	@Response(code=100,description="$L{foo}")
	public static class O5c {}

	@Test
	public void o05a_response_100_description_swaggerOnAnnotation_localized() throws Exception {
		assertEquals("l-foo", getSwagger(new O5a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
		assertEquals("l-foo", getSwaggerWithFile(new O5a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
	}
	@Test
	public void o05b_response_100_description_swaggerOnAnnotation_localized() throws Exception {
		assertEquals("l-foo", getSwagger(new O5b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
		assertEquals("l-foo", getSwaggerWithFile(new O5b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getDescription());
	}

	//------------------------------------------------------------------------------------------------------------------
	// /paths/<path>/<method>/responses/<response>/headers
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class P1a {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(Value<P1c> foo) {}
	}
	@Rest
	public static class P1b {
		@RestGet(path="/path/{foo}/responses/100")
		public P1c a() {return null;}
	}
	@Response(code=100)
	public static class P1c {
		public String a;
	}

	@Test
	public void p01a_responses_100_headers_default() throws Exception {
		assertEquals(null, getSwagger(new P1a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders());
		assertObject(getSwaggerWithFile(new P1a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders()).asJson().is("{'X-Foo':{description:'s-description',type:'integer',format:'int32'}}");
	}
	@Test
	public void p01b_responses_100_headers_default() throws Exception {
		assertEquals(null, getSwagger(new P1b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders());
		assertObject(getSwaggerWithFile(new P1b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders()).asJson().is("{'X-Foo':{description:'s-description',type:'integer',format:'int32'}}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{headers:{'X-Foo':{description:'b-description',type:'integer',format:'int32'}}}}}}}"))
	public static class P2 {
		@RestGet(path="/path/{foo}/responses/100")
		public X a(@ResponseStatus Value<Integer> foo) {
			return null;
		}
	}

	@Test
	public void p02_response_100_headers_swaggerOnClass() throws Exception {
		assertObject(getSwagger(new P2()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders()).asJson().is("{'X-Foo':{description:'b-description',type:'integer',format:'int32'}}");
		assertObject(getSwaggerWithFile(new P2()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders()).asJson().is("{'X-Foo':{description:'b-description',type:'integer',format:'int32'}}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{headers:{'X-Foo':{description:'b-description',type:'integer',format:'int32'}}}}}}}"))
	public static class P3 {
		@RestGet(path="/path/{foo}/responses/100",swagger=@OpSwagger("responses:{100:{headers:{'X-Foo':{description:'c-description',type:'integer',format:'int32'}}}}"))
		public X a(@ResponseStatus Value<Integer> foo) {
			return null;
		}
	}

	@Test
	public void p03_response_100_headers_swaggerOnMethod() throws Exception {
		assertObject(getSwagger(new P3()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders()).asJson().is("{'X-Foo':{description:'c-description',type:'integer',format:'int32'}}");
		assertObject(getSwaggerWithFile(new P3()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders()).asJson().is("{'X-Foo':{description:'c-description',type:'integer',format:'int32'}}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{headers:{'X-Foo':{description:'b-description',type:'integer',format:'int32'}}}}}}}"))
	public static class P4a {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(Value<P4c> foo) {}
	}
	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{headers:{'X-Foo':{description:'b-description',type:'integer',format:'int32'}}}}}}}"))
	public static class P4b {
		@RestGet(path="/path/{foo}/responses/100")
		public P4c a() {return null;}
	}
	@Response(code=100,headers=@ResponseHeader(name="X-Foo",description="d-description",type="integer",format="int32"))
	public static class P4c {}

	@Test
	public void p04a_response_100_headers_swaggerOnAnnotation() throws Exception {
		assertObject(getSwagger(new P4a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders()).asJson().is("{'X-Foo':{description:'d-description',type:'integer',format:'int32'}}");
		assertObject(getSwaggerWithFile(new P4a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders()).asJson().is("{'X-Foo':{description:'d-description',type:'integer',format:'int32'}}");
	}
	@Test
	public void p04b_response_100_headers_swaggerOnAnnotation() throws Exception {
		assertObject(getSwagger(new P4b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders()).asJson().is("{'X-Foo':{description:'d-description',type:'integer',format:'int32'}}");
		assertObject(getSwaggerWithFile(new P4b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders()).asJson().is("{'X-Foo':{description:'d-description',type:'integer',format:'int32'}}");
	}

	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{headers:{'X-Foo':{description:'b-description',type:'integer',format:'int32'}}}}}}}"))
	public static class P5a {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(Value<P5c> foo) {}
	}
	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{headers:{'X-Foo':{description:'b-description',type:'integer',format:'int32'}}}}}}}"))
	public static class P5b {
		@RestGet(path="/path/{foo}/responses/100")
		public P5c a() {return null;}
	}
	@Response(code=100,headers=@ResponseHeader(name="X-Foo",description="$L{foo}",type="integer",format="int32"))
	public static class P5c {}

	@Test
	public void p05a_response_100_headers_swaggerOnAnnotation_localized() throws Exception {
		assertObject(getSwagger(new P5a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders()).asJson().is("{'X-Foo':{description:'l-foo',type:'integer',format:'int32'}}");
		assertObject(getSwaggerWithFile(new P5a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders()).asJson().is("{'X-Foo':{description:'l-foo',type:'integer',format:'int32'}}");
	}
	@Test
	public void p05b_response_100_headers_swaggerOnAnnotation_localized() throws Exception {
		assertObject(getSwagger(new P5b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders()).asJson().is("{'X-Foo':{description:'l-foo',type:'integer',format:'int32'}}");
		assertObject(getSwaggerWithFile(new P5b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getHeaders()).asJson().is("{'X-Foo':{description:'l-foo',type:'integer',format:'int32'}}");
	}

	//------------------------------------------------------------------------------------------------------------------
	// /paths/<path>/<method>/responses/<response>/example
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class Q1a {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(Value<Q1c> foo) {}
	}
	@Rest
	public static class Q1b {
		@RestGet(path="/path/{foo}/responses/100")
		public Q1c a() {return null;}
	}
	@Response(code=100)
	public static class Q1c {
		public String a;
	}

	@Test
	public void q01a_responses_100_example_default() throws Exception {
		assertEquals(null, getSwagger(new Q1a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample());
		assertEquals("{foo:'a'}", getSwaggerWithFile(new Q1a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample());
	}
	@Test
	public void q01b_responses_100_example_default() throws Exception {
		assertEquals(null, getSwagger(new Q1b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample());
		assertEquals("{foo:'a'}", getSwaggerWithFile(new Q1b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample());
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{example:{foo:'b'}}}}}}"))
	public static class Q2 {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(@ResponseStatus Value<Integer> foo) {}
	}

	@Test
	public void q02_response_100_example_swaggerOnClass() throws Exception {
		assertObject(getSwagger(new Q2()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample()).asJson().is("{foo:'b'}");
		assertObject(getSwaggerWithFile(new Q2()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample()).asJson().is("{foo:'b'}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{example:{foo:'b'}}}}}}"))
	public static class Q3 {
		@RestGet(path="/path/{foo}/responses/100",swagger=@OpSwagger("responses:{100:{example:{foo:'c'}}}"))
		public void a(@ResponseStatus Value<Integer> foo) {}
	}

	@Test
	public void q03_response_100_example_swaggerOnMethod() throws Exception {
		assertObject(getSwagger(new Q3()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample()).asJson().is("{foo:'c'}");
		assertObject(getSwaggerWithFile(new Q3()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample()).asJson().is("{foo:'c'}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{example:{foo:'b'}}}}}}"))
	public static class Q4a {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(Value<Q4c> foo) {}
	}
	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{example:{foo:'b'}}}}}}"))
	public static class Q4b {
		@RestGet(path="/path/{foo}/responses/100")
		public Q4c a() {return null;}
	}
	@Response(code=100,example="{foo:'d'}")
	public static class Q4c {
		public String a;
	}

	@Test
	public void q04a_response_100_example_swaggerOnAnnotation() throws Exception {
		assertEquals("{foo:'d'}", getSwagger(new Q4a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample());
		assertEquals("{foo:'d'}", getSwaggerWithFile(new Q4a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample());
	}
	@Test
	public void q04b_response_100_example_swaggerOnAnnotation() throws Exception {
		assertEquals("{foo:'d'}", getSwagger(new Q4b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample());
		assertEquals("{foo:'d'}", getSwaggerWithFile(new Q4b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample());
	}

	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{example:{foo:'b'}}}}}}"))
	public static class Q5a {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(Value<Q5c> foo) {}
	}
	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{example:{foo:'b'}}}}}}"))
	public static class Q5b {
		@RestGet(path="/path/{foo}/responses/100")
		public Q5c a() {return null;}
	}
	@Response(code=100,example="{foo:'$L{foo}'}")
	public static class Q5c {
		public String a;
	}

	@Test
	public void q05a_response_100_example_swaggerOnAnnotation_localized() throws Exception {
		assertEquals("{foo:'l-foo'}", getSwagger(new Q5a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample());
		assertEquals("{foo:'l-foo'}", getSwaggerWithFile(new Q5a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample());
	}
	@Test
	public void q05b_response_100_example_swaggerOnAnnotation_localized() throws Exception {
		assertEquals("{foo:'l-foo'}", getSwagger(new Q5b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample());
		assertEquals("{foo:'l-foo'}", getSwaggerWithFile(new Q5b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExample());
	}

	//------------------------------------------------------------------------------------------------------------------
	// /paths/<path>/<method>/responses/<response>/examples
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class R1a {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(Value<R1c> foo) {}
	}
	@Rest
	public static class R1b {
		@RestGet(path="/path/{foo}/responses/100")
		public R1c a() {return null;}
	}
	@Response(code=100)
	public static class R1c {
		public String a;
	}

	@Test
	public void r01a_responses_100_examples_default() throws Exception {
		assertEquals(null, getSwagger(new R1a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples());
		assertObject(getSwaggerWithFile(new R1a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples()).asJson().is("{foo:'a'}");
	}
	@Test
	public void r01b_responses_100_examples_default() throws Exception {
		assertEquals(null, getSwagger(new R1b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples());
		assertObject(getSwaggerWithFile(new R1b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples()).asJson().is("{foo:'a'}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{examples:{foo:{bar:'b'}}}}}}}"))
	public static class R2 {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(@ResponseStatus Value<Integer> foo) {}
	}

	@Test
	public void r02_response_100_examples_swaggerOnClass() throws Exception {
		assertObject(getSwagger(new R2()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples()).asJson().is("{foo:{bar:'b'}}");
		assertObject(getSwaggerWithFile(new R2()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples()).asJson().is("{foo:{bar:'b'}}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{examples:{foo:{bar:'b'}}}}}}}"))
	public static class R3 {
		@RestGet(path="/path/{foo}/responses/100",swagger=@OpSwagger("responses:{100:{examples:{foo:{bar:'c'}}}}"))
		public void a(@ResponseStatus Value<Integer> foo) {}
	}

	@Test
	public void r03_response_100_examples_swaggerOnMethod() throws Exception {
		assertObject(getSwagger(new R3()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples()).asJson().is("{foo:{bar:'c'}}");
		assertObject(getSwaggerWithFile(new R3()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples()).asJson().is("{foo:{bar:'c'}}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{examples:{foo:{bar:'b'}}}}}}}"))
	public static class R4a {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(Value<R4c> foo) {}
	}
	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{examples:{foo:{bar:'b'}}}}}}}"))
	public static class R4b {
		@RestGet(path="/path/{foo}/responses/100")
		public R4c a() {return null;}
	}
	@Response(code=100,examples="{foo:{bar:'d'}}")
	public static class R4c {}

	@Test
	public void r04a_response_100_examples_swaggerOnAnnotation() throws Exception {
		assertObject(getSwagger(new R4a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples()).asJson().is("{foo:{bar:'d'}}");
		assertObject(getSwaggerWithFile(new R4a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples()).asJson().is("{foo:{bar:'d'}}");
	}
	@Test
	public void r04b_response_100_examples_swaggerOnAnnotation() throws Exception {
		assertObject(getSwagger(new R4b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples()).asJson().is("{foo:{bar:'d'}}");
		assertObject(getSwaggerWithFile(new R4b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples()).asJson().is("{foo:{bar:'d'}}");
	}

	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{examples:{foo:{bar:'b'}}}}}}}"))
	public static class R5a {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(Value<R5c> foo) {}
	}
	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{examples:{foo:{bar:'b'}}}}}}}"))
	public static class R5b {
		@RestGet(path="/path/{foo}/responses/100")
		public R5c a() {return null;}
	}
	@Response(code=100,examples="{foo:{bar:'$L{foo}'}}")
	public static class R5c {}

	@Test
	public void r05a_response_100_examples_swaggerOnAnnotation_lodalized() throws Exception {
		assertObject(getSwagger(new R5a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples()).asJson().is("{foo:{bar:'l-foo'}}");
		assertObject(getSwaggerWithFile(new R5a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples()).asJson().is("{foo:{bar:'l-foo'}}");
	}
	@Test
	public void r05b_response_100_examples_swaggerOnAnnotation_lodalized() throws Exception {
		assertObject(getSwagger(new R5b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples()).asJson().is("{foo:{bar:'l-foo'}}");
		assertObject(getSwaggerWithFile(new R5b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getExamples()).asJson().is("{foo:{bar:'l-foo'}}");
	}

	//------------------------------------------------------------------------------------------------------------------
	// /paths/<path>/<method>/responses/<response>/schema
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class S1a {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(Value<S1c> foo) {}
	}
	@Rest
	public static class S1b {
		@RestGet(path="/path/{foo}/responses/100")
		public S1c a() {return null;}
	}
	@Response(code=100)
	public static class S1c extends X {}

	@Test
	public void s01a_responses_100_schema_default() throws Exception {
		assertObject(getSwagger(new S1a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{type:'object',properties:{a:{format:'int32',type:'integer'}}}");
		assertObject(getSwaggerWithFile(new S1a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{type:'array',items:{'$ref':'#/definitions/Foo'}}");
	}
	@Test
	public void s01b_responses_100_schema_default() throws Exception {
		assertObject(getSwagger(new S1b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{type:'object',properties:{a:{format:'int32',type:'integer'}}}");
		assertObject(getSwaggerWithFile(new S1b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{type:'array',items:{'$ref':'#/definitions/Foo'}}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{schema:{$ref:'b'}}}}}}"))
	public static class S2 {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(@ResponseStatus Value<Integer> foo) {}
	}

	@Test
	public void s02_response_100_schema_swaggerOnClass() throws Exception {
		assertObject(getSwagger(new S2()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{'$ref':'b'}");
		assertObject(getSwaggerWithFile(new S2()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{'$ref':'b'}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{schema:{$ref:'b'}}}}}}"))
	public static class S3 {
		@RestGet(path="/path/{foo}/responses/100",swagger=@OpSwagger("responses:{100:{schema:{$ref:'c'}}}}"))
		public void a(@ResponseStatus Value<Integer> foo) {}
	}

	@Test
	public void s03_response_100_schema_swaggerOnMethoe() throws Exception {
		assertObject(getSwagger(new S3()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{'$ref':'c'}");
		assertObject(getSwaggerWithFile(new S3()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{'$ref':'c'}");
	}

	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{schema:{$ref:'b'}}}}}}"))
	public static class S4a {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(Value<S4c> foo) {}
	}
	@Rest(swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{schema:{$ref:'b'}}}}}}"))
	public static class S4b {
		@RestGet(path="/path/{foo}/responses/100")
		public S4c a() {return null;}
	}
	@Response(code=100,schema=@Schema($ref="d"))
	public static class S4c extends X {}

	@Test
	public void s04a_response_100_schema_swaggerOnAnnotation() throws Exception {
		assertObject(getSwagger(new S4a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{'$ref':'d'}");
		assertObject(getSwaggerWithFile(new S4a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{'$ref':'d'}");
	}
	@Test
	public void s04b_response_100_schema_swaggerOnAnnotation() throws Exception {
		assertObject(getSwagger(new S4b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{'$ref':'d'}");
		assertObject(getSwaggerWithFile(new S4b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{'$ref':'d'}");
	}

	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{schema:{$ref:'b'}}}}}}"))
	public static class S5a {
		@RestGet(path="/path/{foo}/responses/100")
		public void a(Value<S5c> foo) {}
	}
	@Rest(messages="BasicRestInfoProviderTest", swagger=@Swagger("paths:{'/path/{foo}/responses/100':{get:{responses:{100:{schema:{$ref:'b'}}}}}}"))
	public static class S5b {
		@RestGet(path="/path/{foo}/responses/100")
		public S5c a() {return null;}
	}
	@Response(code=100,schema=@Schema("{$ref:'$L{foo}'}"))
	public static class S5c extends X {}

	@Test
	public void s05a_response_100_schema_swaggerOnAnnotation_loealized() throws Exception {
		assertObject(getSwagger(new S5a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{'$ref':'l-foo'}");
		assertObject(getSwaggerWithFile(new S5a()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{'$ref':'l-foo'}");
	}
	@Test
	public void s05b_response_100_schema_swaggerOnAnnotation_loealized() throws Exception {
		assertObject(getSwagger(new S5b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{'$ref':'l-foo'}");
		assertObject(getSwaggerWithFile(new S5b()).getPaths().get("/path/{foo}/responses/100").get("get").getResponse(100).getSchema()).asJson().is("{'$ref':'l-foo'}");
	}

	@Bean(typeName="Foo")
	public static class X {
		public int a;
	}

	//------------------------------------------------------------------------------------------------------------------
	// Example bean with getter-only property.
	//------------------------------------------------------------------------------------------------------------------

	@Rest
	public static class T1 extends BasicRestServlet {
		private static final long serialVersionUID = 1L;

		@RestGet(path="/")
		public T2 a(@Body T2 body) {
			return null;
		}
	}

	@Bean(sort=true)
	public static class T2 {
		private int f1;

		public T2 setF1(int f1) {
			this.f1 = f1;
			return this;
		}

		public int getF1() {
			return f1;
		}

		public int getF2() {
			return 2;
		}

		@Example
		public static T2 example() {
			return new T2().setF1(1);
		}
	}


	@Test
	public void t01_bodyWithReadOnlyProperty() throws Exception {
		MockRestClient p = MockRestClient.build(T1.class);
		org.apache.juneau.dto.swagger.Swagger s = JsonParser.DEFAULT.parse(p.get("/api").accept("application/json").run().getBody().asString(), org.apache.juneau.dto.swagger.Swagger.class);
		Operation o = s.getOperation("/", "get");
		ParameterInfo pi = o.getParameter("body", null);

		assertEquals("{\n\tf1: 1,\n\tf2: 2\n}", pi.getExamples().get("application/json+simple"));
		ResponseInfo ri = o.getResponse("200");
		assertEquals("{\n\tf1: 1,\n\tf2: 2\n}", ri.getExamples().get("application/json+simple"));
	}
}
