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

import static org.apache.juneau.internal.StringUtils.*;
import static org.apache.juneau.httppart.HttpPartType.*;
import static org.apache.juneau.http.HttpMethod.*;
import static org.apache.juneau.http.HttpHeaders.*;
import static org.apache.juneau.http.HttpParts.*;
import static org.apache.juneau.http.HttpEntities.*;
import static org.apache.juneau.rest.client.RestOperation.*;
import static java.util.logging.Level.*;
import static org.apache.juneau.internal.ExceptionUtils.*;
import static org.apache.juneau.internal.StateMachineState.*;
import static java.lang.Character.*;
import static java.util.Optional.*;

import java.io.*;
import java.lang.reflect.*;
import java.lang.reflect.Proxy;
import java.net.*;
import java.net.URI;
import java.nio.charset.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.regex.*;

import org.apache.http.*;
import org.apache.http.Header;
import org.apache.http.client.*;
import org.apache.http.client.config.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.*;
import org.apache.http.conn.*;
import org.apache.http.impl.client.*;
import org.apache.http.params.*;
import org.apache.http.protocol.*;
import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.cp.*;
import org.apache.juneau.http.remote.RemoteReturn;
import org.apache.juneau.http.resource.*;
import org.apache.juneau.http.entity.*;
import org.apache.juneau.http.header.*;
import org.apache.juneau.http.part.*;
import org.apache.juneau.http.remote.*;
import org.apache.juneau.httppart.*;
import org.apache.juneau.httppart.bean.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.marshall.*;
import org.apache.juneau.oapi.*;
import org.apache.juneau.parser.*;
import org.apache.juneau.parser.ParseException;
import org.apache.juneau.reflect.*;
import org.apache.juneau.rest.client.assertion.*;
import org.apache.juneau.rest.client.remote.*;
import org.apache.juneau.serializer.*;
import org.apache.juneau.urlencoding.*;
import org.apache.juneau.utils.*;

/**
 * Utility class for interfacing with remote REST interfaces.
 * {@review}
 *
 * <p class='w900'>
 * Built upon the feature-rich Apache HttpClient library, the Juneau RestClient API adds support for fluent-style
 * REST calls and the ability to perform marshalling of POJOs to and from HTTP parts.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Create a basic REST client with JSON support and download a bean.</jc>
 * 	MyBean <jv>bean</jv> = RestClient.<jsm>create</jsm>()
 * 		.simpleJson()
 * 		.build()
 * 		.get(<jsf>URI</jsf>)
 * 		.run()
 * 		.assertStatus().code().is(200)
 * 		.assertHeader(<js>"Content-Type"</js>).matchesSimple(<js>"application/json*"</js>)
 * 		.getBody().as(MyBean.<jk>class</jk>);
 * </p>
 *
 * <p class='w900'>
 * Breaking apart the fluent call, we can see the classes being used:
 * <p class='bcode w800'>
 * 	RestClientBuilder <jv>builder</jv> = RestClient.<jsm>create</jsm>().simpleJson();
 * 	RestClient <jv>client</jv> = <jv>builder</jv>.build();
 * 	RestRequest <jv>req</jv> = <jv>client</jv>.get(<jsf>URI</jsf>);
 * 	RestResponse <jv>res</jv> = <jv>req</jv>.run();
 * 	RestResponseStatusLineAssertion <jv>statusLineAssertion</jv> = <jv>res</jv>.assertStatus();
 * 	FluentIntegerAssertion&lt;RestResponse&gt; <jv>codeAssertion</jv> = <jv>statusLineAssertion</jv>.code();
 * 	<jv>res</jv> = <jv>codeAssertion</jv>.is(200);
 * 	FluentStringAssertion&lt;RestResponse&gt; <jv>headerAssertion</jv> = <jv>res</jv>.assertHeader(<js>"Content-Type"</js>);
 * 	<jv>res</jv> = <jv>headerAssertion</jv>.matchesSimple(<js>"application/json*"</js>);
 * 	RestResponseBody <jv>body</jv> = <jv>res</jv>.getBody();
 * 	MyBean <jv>bean</jv> = <jv>body</jv>.as(MyBean.<jk>class</jk>);
 * </p>
 *
 * <p class='w900'>
 * It additionally provides support for creating remote proxy interfaces using REST as the transport medium.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Define a Remote proxy for interacting with a REST interface.</jc>
 * 	<ja>@Remote</ja>(path=<js>"/petstore"</js>)
 * 	<jk>public interface</jk> PetStore {
 *
 * 		<ja>@RemotePost</ja>(<js>"/pets"</js>)
 * 		Pet addPet(
 * 			<ja>@Body</ja> CreatePet <jv>pet</jv>,
 * 			<ja>@Header</ja>(<js>"E-Tag"</js>) UUID <jv>etag</jv>,
 * 			<ja>@Query</ja>(<js>"debug"</js>) <jk>boolean</jk> <jv>debug</jv>
 * 		);
 * 	}
 *
 * 	<jc>// Use a RestClient with default Simple JSON support.</jc>
 * 	RestClient <jv>client</jv> = RestClient.<jsm>create</jsm>().simpleJson().build();
 *
 * 	PetStore <jv>store</jv> = <jv>client</jv>.getRemote(PetStore.<jk>class</jk>, <js>"http://localhost:10000"</js>);
 * 	CreatePet <jv>createPet</jv> = <jk>new</jk> CreatePet(<js>"Fluffy"</js>, 9.99);
 * 	Pet <jv>pet</jv> = <jv>store</jv>.addPet(<jv>createPet</jv>, UUID.<jsm>randomUUID</jsm>(), <jk>true</jk>);
 * </p>
 *
 * <p class='w900'>
 * The classes are closely tied to Apache HttpClient, yet provide lots of additional functionality:
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestClient} <jk>extends</jk> {@link HttpClient}, creates {@link RestRequest} objects.
 * 	<li class='jc'>{@link RestRequest} <jk>extends</jk> {@link HttpUriRequest}, creates {@link RestResponse} objects.
 * 	<li class='jc'>{@link RestResponse} creates {@link ResponseBody} and {@link ResponseHeader} objects.
 * 	<li class='jc'>{@link ResponseBody} <jk>extends</jk> {@link HttpEntity}
 * 	<li class='jc'>{@link ResponseHeader} <jk>extends</jk> {@link Header}
 * </ul>
 *
 *
 * <p class='w900'>
 * Instances of this class are built using the {@link RestClientBuilder} class which can be constructed using
 * the {@link #create() RestClient.create()} method as shown above.
 *
 * <p class='w900'>
 * Clients are typically created with a root URI so that relative URIs can be used when making requests.
 * This is done using the {@link RestClientBuilder#rootUri(Object)} method.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Create a client where all URIs are relative to localhost.</jc>
 * 	RestClient <jv>client</jv> = RestClient.<jsm>create</jsm>().json().rootUri(<js>"http://localhost:5000"</js>).build();
 *
 * 	<jc>// Use relative paths.</jc>
 * 	String <jv>body</jv> = <jv>client</jv>.get(<js>"/subpath"</js>).run().getBody().asString();
 * </p>
 *
 * <p class='w900'>
 * The {@link RestClient} class creates {@link RestRequest} objects using the following methods:
 *
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestClient}
 * 	<ul>
 * 		<li class='jm'>{@link RestClient#get(Object) get(uri)} / {@link RestClient#get() get()}
 * 		<li class='jm'>{@link RestClient#put(Object,Object) put(uri,body)} / {@link RestClient#put(Object) put(uri)}
 * 		<li class='jm'>{@link RestClient#post(Object) post(uri,body)} / {@link RestClient#post(Object) post(uri)}
 * 		<li class='jm'>{@link RestClient#patch(Object,Object) patch(uri,body)} / {@link RestClient#patch(Object) patch(uri)}
 * 		<li class='jm'>{@link RestClient#delete(Object) delete(uri)}
 * 		<li class='jm'>{@link RestClient#head(Object) head(uri)}
 * 		<li class='jm'>{@link RestClient#options(Object) options(uri)}
 * 		<li class='jm'>{@link RestClient#formPost(Object,Object) formPost(uri,body)} / {@link RestClient#formPost(Object) formPost(uri)}
 * 		<li class='jm'>{@link RestClient#formPostPairs(Object,Object...) formPostPairs(uri,parameters...)}
 * 		<li class='jm'>{@link RestClient#request(String,Object,Object) request(method,uri,body)}
 * 	</ul>
 * </ul>
 *
 * <p class='w900'>
 * The {@link RestRequest} class creates {@link RestResponse} objects using the following methods:
 *
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestRequest}
 * 	<ul>
 * 		<li class='jm'>{@link RestRequest#run() run()}
 * 		<li class='jm'>{@link RestRequest#complete() complete()}
 * 	</ul>
 * </ul>
 *
 * <p class='w900'>
 * The distinction between the two methods is that {@link RestRequest#complete() complete()} automatically consumes the response body and
 * {@link RestRequest#run() run()} does not.  Note that you must consume response bodies in order for HTTP connections to be freed up
 * for reuse!  The {@link InputStream InputStreams} returned by the {@link ResponseBody} object are auto-closing once
 * they are exhausted, so it is often not necessary to explicitly close them.
 *
 * <p class='w900'>
 * The following examples show the distinction between the two calls:
 *
 * <p class='bcode w800'>
 * 	<jc>// Consuming the response, so use run().</jc>
 * 	String <jv>body</jv> = <jv>client</jv>.get(<jsf>URI</jsf>).run().getBody().asString();
 *
 * 	<jc>// Only interested in response status code, so use complete().</jc>
 *   <jk>int</jk> <jv>status</jv> = <jv>client</jv>.get(<jsf>URI</jsf>).complete().getStatusCode();
 * </p>
 *
 *
 * <h4 class='topic'>POJO Marshalling</h4>
 *
 * <p class='w900'>
 * By default, JSON support is provided for HTTP request and response bodies.
 * Other languages can be specified using any of the following builder methods:
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestClientBuilder}
 * 	<ul>
 * 		<li class='jm'>{@link RestClientBuilder#json() json()}
 * 		<li class='jm'>{@link RestClientBuilder#simpleJson() simpleJson()}
 * 		<li class='jm'>{@link RestClientBuilder#xml() xml()}
 * 		<li class='jm'>{@link RestClientBuilder#html() html()}
 * 		<li class='jm'>{@link RestClientBuilder#plainText() plainText()}
 * 		<li class='jm'>{@link RestClientBuilder#msgPack() msgPack()}
 * 		<li class='jm'>{@link RestClientBuilder#uon() uon()}
 * 		<li class='jm'>{@link RestClientBuilder#urlEnc() urlEnc()}
 * 		<li class='jm'>{@link RestClientBuilder#openApi() openApi()}
 * 	</ul>
 * </ul>
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Create a basic REST client with Simplified-JSON support.</jc>
 * 	<jc>// Typically easier to use when performing unit tests.</jc>
 * 	RestClient <jv>client</jv> = RestClient.<jsm>create</jsm>().simpleJson().build();
 * </p>
 *
 * <p>
 * Clients can also support multiple languages:
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Create a REST client with support for multiple languages.</jc>
 * 	RestClient <jv>client1</jv> = RestClient.<jsm>create</jsm>().json().xml().openApi().build();
 *
 * 	<jc>// Create a REST client with support for all supported languages.</jc>
 * 	RestClient <jv>client2</jv> = RestClient.<jsm>create</jsm>().universal().build();
 * </p>
 *
 * <p class='w900'>
 * When using clients with multiple language support, you must specify the <c>Content-Type</c> header on requests
 * with bodies to specify which serializer should be selected.
 *
 * <p class='bcode w800'>
 * 	<jc>// Create a REST client with support for multiple languages.</jc>
 * 	RestClient <jv>client</jv> = RestClient.<jsm>create</jsm>().universal().build();
 *
 * 	<jv>client</jv>
 * 		.post(<jsf>URI</jsf>, <jv>myBean</jv>)
 * 		.contentType(<js>"application/json"</js>)
 * 		.complete()
 * 		.assertStatus().is(200);
 * </p>
 *
 * <p>
 * Languages can also be specified per-request.
 *
 * <p class='bcode w800'>
 * 	<jc>// Create a REST client with no default languages supported.</jc>
 * 	RestClient <jv>client</jv> = RestClient.<jsm>create</jsm>().build();
 *
 * 	<jc>// Use JSON for this request.</jc>
 * 	<jv>client</jv>
 * 		.post(<jsf>URI</jsf>, <jv>myBean</jv>)
 * 		.json()
 * 		.complete()
 * 		.assertStatus().is(200);
 * </p>
 *
 *
 * <p class='w900'>
 * The {@link RestClientBuilder} class provides convenience methods for setting common serializer and parser
 * settings.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Create a basic REST client with JSON support.</jc>
 * 	<jc>// Use single-quotes and whitespace.</jc>
 * 	RestClient <jv>client</jv> = RestClient.<jsm>create</jsm>().json().sq().ws().build();
 *
 * 	<jc>// Same, but using properties.</jc>
 * 	RestClient <jv>client2</jv> = RestClient
 * 		.<jsm>create</jsm>()
 * 		.json()
 * 		.set(<jsf>WSERIALIZER_quoteChar</jsf>, <js>'\''</js>)
 * 		.set(<jsf>WSERIALIZER_useWhitespace</jsf>)
 * 		.build();
 * </p>
 *
 * <p class='w900'>
 * 	Other methods are also provided for specifying the serializers and parsers used for lower-level marshalling support:
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestClientBuilder}
 * 	<ul>
 * 		<li class='jm'>{@link RestClientBuilder#serializer(Serializer) serializer(Serializer)}
 * 		<li class='jm'>{@link RestClientBuilder#parser(Parser) parser(Parser)}
 * 		<li class='jm'>{@link RestClientBuilder#marshall(Marshall) marshall(Marshall)}
 * 	</ul>
 * </ul>
 *
 * <p class='w900'>
 * HTTP parts (headers, query parameters, form data...) are serialized and parsed using the {@link HttpPartSerializer}
 * and {@link HttpPartParser} APIs.  By default, clients are configured to use {@link OpenApiSerializer} and
 * {@link OpenApiParser}.  These can be overridden using the following methods:
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestClientBuilder}
 * 	<ul>
 * 		<li class='jm'>{@link RestClientBuilder#partSerializer(Class) partSerializer(Class&lt;? extends HttpPartSerializer>)}
 * 		<li class='jm'>{@link RestClientBuilder#partParser(Class) partParser(Class&lt;? extends HttpPartParser>)}
 * 	</ul>
 * </ul>
 *
 *
 * <h4 class='topic'>Request Headers</h4>
 * <p class='w900'>
 * Per-client or per-request headers can be specified using the following methods:
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestClientBuilder}
 * 	<ul>
 * 		<li class='jm'>{@link RestClientBuilder#headerData() headerData()}
 * 		<li class='jm'>{@link RestClientBuilder#header(String,String) header(String,Object)}
 * 		<li class='jm'>{@link RestClientBuilder#header(String,Supplier) header(String,Supplier&lt;?&gt;)}
 * 		<li class='jm'>{@link RestClientBuilder#headers(Header...) headers(Header...)}
 * 		<li class='jm'>{@link RestClientBuilder#headersDefault(Header...) defaultHeaders(Header...)}
 * 	</ul>
 * 	<li class='jc'>{@link RestRequest}
 * 	<ul>
 * 		<li class='jm'>{@link RestRequest#header(String,Object) header(String,Object)}
 * 		<li class='jm'>{@link RestRequest#headers(Header...) headers(Header...)}
 * 		<li class='jm'>{@link RestRequest#headers(ListOperation,Header...) headers(ListOperation,Header...)}
 * 		<li class='jm'>{@link RestRequest#headersBean(Object) headersBean(Object)}
 * 		<li class='jm'>{@link RestRequest#headerPairs(String...) headerPairs(String...)}
 * 	</ul>
 * </ul>
 *
 * <p class='w900'>
 * The supplier methods are particularly useful for header values whose values may change over time (such as <c>Authorization</c> headers
 * which may need to change every few minutes).
 * </p>
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Create a client that adds a dynamic Authorization header to every request.</jc>
 * 	RestClient <jv>client</jv> = RestClient.<jsm>create</jsm>().header(<js>"Authorization"</js>, ()-&gt;getMyAuthToken()).build();
 * </p>
 *
 * <p>
 * The {@link HttpPartSchema} API allows you to define OpenAPI schemas to POJO data structures on both requests
 * and responses.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Create a client that adds a header "Foo: bar|baz" to every request.</jc>
 * 	RestClient <jv>client</jv> = RestClient.<jsm>create</jsm>()
 * 		.header(<js>"Foo"</js>, AList.<jsm>of</jsm>(<js>"bar"</js>,<js>"baz"</js>), <jsf>T_ARRAY_PIPES</jsf>)
 * 		.build();
 * </p>
 *
 * <p>
 * The methods with {@link ListOperation} parameters allow you to control whether new headers get appended, prepended, or
 * replace existing headers with the same name.
 *
 * <ul class='notes'>
 * 	<li>Methods that pass in POJOs convert values to strings using the part serializers.  Methods that pass in <c>Header</c> or
 * 		<c>NameValuePair</c> objects use the values returned by that bean directly.
 * </ul>
 *
 *
 * <h4 class='topic'>Request Query Parameters</h4>
 * <p>
 * Per-client or per-request query parameters can be specified using the following methods:
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestClientBuilder}
 * 	<ul>
 * 		<li class='jm'>{@link RestClientBuilder#queryData() queryData()}
 * 		<li class='jm'>{@link RestClientBuilder#queryData(String,String) queryData(String,String)}
 * 		<li class='jm'>{@link RestClientBuilder#queryData(String,Supplier) queryData(String,Supplier&lt;?&gt;)}
 * 		<li class='jm'>{@link RestClientBuilder#queryData(NameValuePair...) queryData(NameValuePair...)}
 * 		<li class='jm'>{@link RestClientBuilder#queryDataDefault(NameValuePair...) defaultQueryData(NameValuePair...)}
 * 	</ul>
 * 	<li class='jc'>{@link RestRequest}
 * 	<ul>
 * 		<li class='jm'>{@link RestRequest#queryData(String,Object) queryData(String,Object)}
 * 		<li class='jm'>{@link RestRequest#queryData(NameValuePair...) queryData(NameValuePair...)}
 * 		<li class='jm'>{@link RestRequest#queryData(ListOperation,NameValuePair...) queryData(ListOperation,NameValuePair...)}
 * 		<li class='jm'>{@link RestRequest#queryDataBean(Object) queryDataBean(Object)}
 * 		<li class='jm'>{@link RestRequest#queryCustom(Object) queryCustom(Object)}
 * 		<li class='jm'>{@link RestRequest#queryDataPairs(String...) queryDataPairs(String...)}
 * 	</ul>
 * </ul>
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Create a client that adds a ?foo=bar query parameter to every request.</jc>
 * 	RestClient <jv>client</jv> = RestClient.<jsm>create</jsm>().query(<js>"foo"</js>, <js>"bar"</js>).build();
 *
 * 	<jc>// Or do it on every request.</jc>
 * 	String <jv>response</jv> = <jv>client</jv>.get(<jsf>URI</jsf>).query(<js>"foo"</js>, <js>"bar"</js>).run().getBody().asString();
 * </p>
 *
 * <ul class='notes'>
 * 	<li>Like header values, dynamic values and OpenAPI schemas are supported.
 * 	<li>Methods that pass in POJOs convert values to strings using the part serializers.  Methods that pass in <c>NameValuePair</c>
 * 		objects use the values returned by that bean directly.
 * </ul>
 *
 *
 * <h4 class='topic'>Request Form Data</h4>
 *
 * <p class='w900'>
 * Per-client or per-request form-data parameters can be specified using the following methods:
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestClientBuilder}
 * 	<ul>
 * 		<li class='jm'>{@link RestClientBuilder#formData() formData()}
 * 		<li class='jm'>{@link RestClientBuilder#formData(String,String) formData(String,String)}
 * 		<li class='jm'>{@link RestClientBuilder#formData(String,Supplier) formData(String,Supplier&lt;?&gt;)}
 * 		<li class='jm'>{@link RestClientBuilder#formData(NameValuePair...) formDatas(NameValuePair...)}
 * 		<li class='jm'>{@link RestClientBuilder#formDataDefault(NameValuePair...) defaultFormData(NameValuePair...)}
 * 	</ul>
 * 	<li class='jc'>{@link RestRequest}
 * 	<ul>
 * 		<li class='jm'>{@link RestRequest#formData(String,Object) formData(String,Object)}
 * 		<li class='jm'>{@link RestRequest#formData(NameValuePair...) formData(NameValuePair...)}
 * 		<li class='jm'>{@link RestRequest#formData(ListOperation,NameValuePair...) formData(ListOperation,NameValuePair...)}
 * 		<li class='jm'>{@link RestRequest#formDataBean(Object) formDataBean(Object)}
 * 		<li class='jm'>{@link RestRequest#formDataCustom(Object) formDataCustom(Object)}
 * 		<li class='jm'>{@link RestRequest#formDataPairs(String...) formDataPairs(String...)}
 * 	</ul>
 * </ul>
 *
 * <ul class='notes'>
 * 	<li>Like header values, dynamic values and OpenAPI schemas are supported.
 * 	<li>Methods that pass in POJOs convert values to strings using the part serializers.  Methods that pass in <c>NameValuePair</c>
 * 		objects use the values returned by that bean directly.
 * </ul>
 *
 *
 * <h4 class='topic'>Request Body</h4>
 *
 * <p class='w900'>
 * The request body can either be passed in with the client creator method (e.g. {@link RestClient#post(Object,Object) post(uri,body)}),
 * or can be specified via the following methods:
 *
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestRequest}
 * 	<ul>
 * 		<li class='jm'>{@link RestRequest#body(Object) body(Object)}
 * 		<li class='jm'>{@link RestRequest#body(Object,HttpPartSchema) body(Object,HttpPartSchema)}
 * 	</ul>
 * </ul>
 *
 * <p class='w900'>
 * The request body can be any of the following types:
 * <ul class='javatree'>
 * 		<li class='jc'>
 * 			{@link Object} - POJO to be converted to text using the {@link Serializer} defined on the client or request.
 * 		<li class='jc'>
 * 			{@link Reader} - Raw contents of {@code Reader} will be serialized to remote resource.
 * 		<li class='jc'>
 * 			{@link InputStream} - Raw contents of {@code InputStream} will be serialized to remote resource.
 * 		<li class='jc'>
 * 			{@link HttpResource} - Raw contents will be serialized to remote resource.  Additional headers and media type will be set on request.
 * 		<li class='jc'>
 * 			{@link HttpEntity} - Bypass Juneau serialization and pass HttpEntity directly to HttpClient.
 * 		<li class='jc'>
 * 			{@link PartList} - Converted to a URL-encoded FORM post.
 * 		<li class='jc'>
 * 			{@link Supplier} - A supplier of anything on this list.
 * 	</ul>
 *
 * <ul class='notes'>
 * 	<li>If the serializer on the client or request is explicitly set to <jk>null</jk>, POJOs will be converted to strings
 * 		using the registered part serializer as content type <js>"text/plain</js>.  If the part serializer is also <jk>null</jk>,
 * 		POJOs will be converted to strings using {@link ClassMeta#toString(Object)} which typically just calls {@link Object#toString()}.
 * </ul>
 *
 *
 * <h4 class='topic'>Response Status</h4>
 *
 * <p class='w900'>
 * After execution using {@link RestRequest#run()} or {@link RestRequest#complete()}, the following methods can be used
 * to get the response status:
 *
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestResponse}
 * 	<ul>
 * 		<li class='jm'><c>{@link RestResponse#getStatusLine() getStatusLine()} <jk>returns</jk> {@link StatusLine}</c>
 * 		<li class='jm'><c>{@link RestResponse#getStatusCode() getStatusCode()} <jk>returns</jk> <jk>int</jk></c>
 * 		<li class='jm'><c>{@link RestResponse#getReasonPhrase() getReasonPhrase()} <jk>returns</jk> String</c>
 * 		<li class='jm'><c>{@link RestResponse#assertStatus() assertStatus()} <jk>returns</jk> {@link FluentResponseStatusLineAssertion}</c>
 * 	</ul>
 * </ul>
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Only interested in status code.</jc>
 * 	<jk>int</jk> <jv>statusCode</jv> = <jv>client</jv>.get(<jsf>URI</jsf>).complete().getStatusCode();
 * </p>
 *
 * <p class='w900'>
 * Equivalent methods with mutable parameters are provided to allow access to status values without breaking fluent call chains.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Interested in multiple values.</jc>
 * 	Mutable&lt;Integer&gt; <jv>statusCode</jv> = Mutable.<jsm>create</jsm>();
 * 	Mutable&lt;String&gt; <jv>reasonPhrase</jv> = Mutable.<jsm>create</jsm>();
 *
 * 	<jv>client</jv>.get(<jsf>URI</jsf>).complete().getStatusCode(<jv>statusCode</jv>).getReasonPhrase(<jv>reasonPhrase</jv>);
 * 	System.<jsf>err</jsf>.println(<js>"statusCode="</js>+<jv>statusCode</jv>.get()+<js>", reasonPhrase="</js>+<jv>reasonPhrase</jv>.get());
 * </p>
 *
 * <ul class='notes'>
 * 	<li>If you are only interested in the response status and not the response body, be sure to use {@link RestRequest#complete()} instead
 * 		of {@link RestRequest#run()} to make sure the response body gets automatically cleaned up.  Otherwise you must
 * 		consume the response yourself.
 * </ul>
 *
 * <p class='w900'>
 * The assertion method is provided for quickly asserting status codes in fluent calls.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Status assertion using a static value.</jc>
 * 	String <jv>body</jv> = <jv>client</jv>.get(<jsf>URI</jsf>)
 * 		.run()
 * 		.assertStatus().code().isBetween(200,399)
 * 		.getBody().asString();
 *
 * 	<jc>// Status assertion using a predicate.</jc>
 * 	String <jv>body</jv> = <jv>client</jv>.get(<jsf>URI</jsf>)
 * 		.run()
 * 		.assertStatus().code().passes(<jv>x</jv> -&gt; <jv>x</jv>&lt;400)
 * 		.getBody().asString();
 * </p>
 *
 *
 * <h4 class='topic'>Response Headers</h4>
 *
 * <p class='w900'>
 * Response headers are accessed through the following methods:
 *
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestResponse}
 * 	<ul>
 * 		<li class='jm'><c>{@link RestResponse#getHeaders(String) getHeaders(String)} <jk>returns</jk> {@link ResponseHeader}[]</c>
 * 		<li class='jm'><c>{@link RestResponse#getFirstHeader(String) getFirstHeader(String)} <jk>returns</jk> {@link ResponseHeader}</c>
 * 		<li class='jm'><c>{@link RestResponse#getLastHeader(String) getLastHeader(String)} <jk>returns</jk> {@link ResponseHeader}</c>
 * 		<li class='jm'><c>{@link RestResponse#getAllHeaders() getAllHeaders()} <jk>returns</jk> {@link ResponseHeader}[]</c>
 * 		<li class='jm'><c>{@link RestResponse#getStringHeader(String) getStringHeader(String)} <jk>returns</jk> String</c>
 * 		<li class='jm'><c>{@link RestResponse#containsHeader(String) containsHeader(String)} <jk>returns</jk> <jk>boolean</jk></c>
 * 	</ul>
 * </ul>
 *
 * <p class='w900'>
 * The {@link RestResponse#getFirstHeader(String)} and {@link RestResponse#getLastHeader(String)} methods return an empty {@link ResponseHeader} object instead of<jk>null</jk>.
 * This allows it to be used more easily in fluent calls.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// See if response contains Location header.</jc>
 * 	<jk>boolean</jk> <jv>hasLocationHeader</jv> = client.get(<jsf>URI</jsf>).complete().getLastHeader(<js>"Location"</js>).exists();
 * </p>
 *
 * <p class='w900'>
 * The {@link ResponseHeader} class extends from the HttpClient {@link Header} class and provides several convenience
 * methods:
 *
 * <ul class='javatree'>
 * 	<li class='jc'>{@link ResponseHeader}
 * 	<ul>
 * 		<li class='jm'><c>{@link ResponseHeader#isPresent() isPresent()} <jk>returns</jk> <jk>boolean</jk></c>
 * 		<li class='jm'><c>{@link ResponseHeader#asString() asString()} <jk>returns</jk> String</c>
 * 		<li class='jm'><c>{@link ResponseHeader#asType(Type,Type...) asType(Type,Type...)} <jk>returns</jk> T</c>
 * 		<li class='jm'><c>{@link ResponseHeader#asType(Class) asType(Class&lt;T&gt;)} <jk>returns</jk> T</c>
 * 		<li class='jm'><c>{@link ResponseHeader#asMatcher(Pattern) asMatcher(Pattern)} <jk>returns</jk> {@link Matcher}</c>
 * 		<li class='jm'><c>{@link ResponseHeader#asMatcher(String) asMatcher(String)} <jk>returns</jk> {@link Matcher}</c>
 * 		<li class='jm'><c>{@link ResponseHeader#asHeader(Class) asHeader(Class&lt;T <jk>extends</jk> BasicHeader&gt; c)} <jk>returns</jk> {@link BasicHeader}</c>
 * 		<li class='jm'><c>{@link ResponseHeader#asStringHeader() asStringHeader()} <jk>returns</jk> {@link BasicStringHeader}</c>
 * 		<li class='jm'><c>{@link ResponseHeader#asIntegerHeader() asIntegerHeader()} <jk>returns</jk> {@link BasicIntegerHeader}</c>
 * 		<li class='jm'><c>{@link ResponseHeader#asLongHeader() asLongHeader()} <jk>returns</jk> {@link BasicLongHeader}</c>
 * 		<li class='jm'><c>{@link ResponseHeader#asDateHeader() asDateHeader()} <jk>returns</jk> {@link BasicDateHeader}</c>
 * 		<li class='jm'><c>{@link ResponseHeader#asCsvArrayHeader() asCsvArrayHeader()} <jk>returns</jk> {@link BasicCsvArrayHeader}</c>
 * 		<li class='jm'><c>{@link ResponseHeader#asEntityTagArrayHeader() asEntityValidatorArrayHeader()} <jk>returns</jk> {@link BasicEntityTagArrayHeader}</c>
 * 		<li class='jm'><c>{@link ResponseHeader#asStringRangeArrayHeader() asRangeArrayHeader()} <jk>returns</jk> {@link BasicStringRangeArrayHeader}</c>
 * 		<li class='jm'><c>{@link ResponseHeader#asUriHeader() asUriHeader()} <jk>returns</jk> {@link BasicUriHeader}</c>
 * 	</ul>
 * </ul>
 *
 * <p>
 * The {@link ResponseHeader#schema(HttpPartSchema)} method allows you to perform parsing of OpenAPI formats for
 * header parts.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Parse the header "Foo: bar|baz".</jc>
 * 	List&lt;String&gt; <jv>fooHeader</jv> = <jv>client</jv>
 * 		.get(<jsf>URI</jsf>)
 * 		.complete()
 * 		.getHeader(<js>"Foo"</js>).schema(<jsf>T_ARRAY_PIPES</jsf>).as(List.<jk>class</jk>, String.<jk>class</jk>);
 * </p>
 *
 * <p>
 * Assertion methods are also provided for fluent-style calls:
 *
 * <ul class='javatree'>
 * 	<li class='jc'>{@link ResponseHeader}
 * 	<ul>
 * 		<li class='jm'><c>{@link ResponseHeader#assertValue() assertValue()} <jk>returns</jk> {@link FluentResponseHeaderAssertion}</c>
 * 	</ul>
 * </ul>
 *
 * <p>
 * Note how in the following example, the fluent assertion returns control to the {@link RestResponse} object after
 * the assertion has been completed:
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Assert the response content type is any sort of JSON.</jc>
 * 	String <jv>body</jv> = <jv>client</jv>.get(<jsf>URI</jsf>)
 * 		.run()
 * 		.getHeader(<js>"Content-Type"</js>).assertValue().matchesSimple(<js>"application/json*"</js>)
 * 		.getBody().asString();
 * </p>
 *
 *
 * <h4 class='topic'>Response Body</h4>
 *
 * <p class='w900'>
 * The response body is accessed through the following method:
 *
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestResponse}
 * 	<ul>
 * 		<li class='jm'><c>{@link RestResponse#getBody() getBody()} <jk>returns</jk> {@link ResponseBody}</c>
 * 	</ul>
 * </ul>
 *
 * <p class='w900'>
 * The {@link ResponseBody} class extends from the HttpClient {@link HttpEntity} class and provides several convenience
 * methods:
 *
 * <ul class='javatree'>
 * 	<li class='jc'>{@link ResponseBody}
 * 	<ul>
 * 		<li class='jm'><c>{@link ResponseBody#asInputStream() asInputStream()} <jk>returns</jk> InputStream</c>
 * 		<li class='jm'><c>{@link ResponseBody#asReader() asReader()} <jk>returns</jk> Reader</c>
 * 		<li class='jm'><c>{@link ResponseBody#asReader(Charset) asReader(Charset)} <jk>returns</jk> Reader</c>
 * 		<li class='jm'><c>{@link ResponseBody#pipeTo(OutputStream) pipeTo(OutputStream)} <jk>returns</jk> {@link RestResponse}</c>
 * 		<li class='jm'><c>{@link ResponseBody#pipeTo(Writer) pipeTo(Writer)} <jk>returns</jk> {@link RestResponse}</c>
 * 		<li class='jm'><c>{@link ResponseBody#asType(Type,Type...) asType(Type,Type...)} <jk>returns</jk> T</c>
 * 		<li class='jm'><c>{@link ResponseBody#asType(Class) asType(Class&lt;T&gt;)} <jk>returns</jk> T</c>
 * 		<li class='jm'><c>{@link ResponseBody#asFuture(Class) asFuture(Class&lt;T&gt;)} <jk>returns</jk> Future&lt;T&gt;</c>
 * 		<li class='jm'><c>{@link ResponseBody#asFuture(Type,Type...) asFuture(Type,Type...)} <jk>returns</jk> Future&lt;T&gt;</c>
 * 		<li class='jm'><c>{@link ResponseBody#asString() asString()} <jk>returns</jk> String</c>
 * 		<li class='jm'><c>{@link ResponseBody#asStringFuture() asStringFuture()} <jk>returns</jk> Future&lt;String&gt;</c>
 * 		<li class='jm'><c>{@link ResponseBody#asAbbreviatedString(int) asAbbreviatedString(int)} <jk>returns</jk> String</c>
 * 		<li class='jm'><c>{@link ResponseBody#asPojoRest(Class) asPojoRest(Class&lt;?&gt;)} <jk>returns</jk> {@link PojoRest}</c>
 * 		<li class='jm'><c>{@link ResponseBody#asPojoRest() asPojoRest()} <jk>returns</jk> {@link PojoRest}</c>
 * 		<li class='jm'><c>{@link ResponseBody#asMatcher(Pattern) asMatcher(Pattern)} <jk>returns</jk> {@link Matcher}</c>
 * 		<li class='jm'><c>{@link ResponseBody#asMatcher(String) asMatcher(String)} <jk>returns</jk> {@link Matcher}</c>
 * 	</ul>
 * </ul>
 *
 * <br>
 *
 * <h5 class='figure'>Examples:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Parse into a linked-list of strings.</jc>
 * 	List&lt;String&gt; <jv>l1</jv> = <jv>client</jv>
 * 		.get(<jsf>URI</jsf>)
 * 		.run()
 * 		.getBody().as(LinkedList.<jk>class</jk>, String.<jk>class</jk>);
 *
 * 	<jc>// Parse into a linked-list of beans.</jc>
 * 	List&lt;MyBean&gt; <jv>l2</jv> = <jv>client</jv>
 * 		.get(<jsf>URI</jsf>)
 * 		.run()
 * 		.getBody().as(LinkedList.<jk>class</jk>, MyBean.<jk>class</jk>);
 *
 * 	<jc>// Parse into a linked-list of linked-lists of strings.</jc>
 * 	List&lt;List&lt;String&gt;&gt; <jv>l3</jv> = <jv>client</jv>
 * 		.get(<jsf>URI</jsf>)
 * 		.run()
 * 		.getBody().as(LinkedList.<jk>class</jk>, LinkedList.<jk>class</jk>, String.<jk>class</jk>);
 *
 * 	<jc>// Parse into a map of string keys/values.</jc>
 * 	Map&lt;String,String&gt; <jv>m1</jv> = <jv>client</jv>
 * 		.get(<jsf>URI</jsf>)
 * 		.run()
 * 		.getBody().as(TreeMap.<jk>class</jk>, String.<jk>class</jk>, String.<jk>class</jk>);
 *
 * 	<jc>// Parse into a map containing string keys and values of lists containing beans.</jc>
 * 	Map&lt;String,List&lt;MyBean&gt;&gt; <jv>m2<jv> = <jv>client</jv>
 * 		.get(<jsf>URI</jsf>)
 * 		.run()
 * 		.getBody().as(TreeMap.<jk>class</jk>, String.<jk>class</jk>, List.<jk>class</jk>, MyBean.<jk>class</jk>);
 * </p>
 *
 * <p class='w900'>
 * The response body can only be consumed once unless it has been cached into memory.  In many cases, the body is
 * automatically cached when using the assertions methods or methods such as {@link ResponseBody#asString()}.
 * However, methods that involve reading directly from the input stream cannot be called twice.
 * In these cases, the {@link RestResponse#cacheBody()} and {@link ResponseBody#cache()} methods are provided
 * to cache the response body in memory so that you can perform several operations against it.
 *
 * <p class='bcode w800'>
 * 	<jc>// Cache the response body so we can access it twice.</jc>
 * 	InputStream <jv>inputStream</jv> = <jv>client</jv>
 * 		.get(<jsf>URI</jsf>)
 * 		.run()
 * 		.cacheBody()
 * 		.getBody().pipeTo(<jv>someOtherStream</jv>)
 * 		.getBody().asInputStream();
 * </p>
 *
 * <p>
 * Assertion methods are also provided for fluent-style calls:
 *
 * <ul class='javatree'>
 * 	<li class='jc'>{@link ResponseBody}
 * 	<ul>
 * 		<li class='jm'><c>{@link ResponseBody#assertValue() assertValue()} <jk>returns</jk> {@link FluentResponseBodyAssertion}</c>
 * 	</ul>
 * </ul>
 *
 * <br>
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Assert that the body contains the string "Success".</jc>
 * 	String <jv>body</jv> = <jv>client</jv>
 * 		.get(<jsf>URI</jsf>)
 * 		.run()
 * 		.getBody().assertString().contains(<js>"Success"</js>)
 * 		.getBody().asString();
 * </p>
 *
 * <p>
 * Object assertions allow you to parse the response body into a POJO and then perform various tests on that resulting
 * POJO.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Parse bean into POJO and then validate that it was parsed correctly.</jc>
 * 	MyBean <jv>bean</jv> = <jv>client</jv>.get(<jsf>URI</jsf>)
 * 		.run()
 * 		.getBody().assertObject(MyBean.<jk>class</jk>).json().is(<js>"{foo:'bar'}"</js>)
 * 		.getBody().as(MyBean.<jk>class</jk>);
 * </p>
 *
 *
 * <h4 class='topic'>Custom Call Handlers</h4>
 *
 * <p class='w900'>
 * The {@link RestCallHandler} interface provides the ability to provide custom handling of requests.
 *
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestClientBuilder}
 * 	<ul>
 * 		<li class='jm'>{@link RestClientBuilder#callHandler() callHandler()}
 * 	</ul>
 * 	<li class='jic'>{@link RestCallHandler}
 * 	<ul>
 * 		<li class='jm'><c>{@link RestCallHandler#run(HttpHost,HttpRequest,HttpContext) run(HttpHost,HttpRequest,HttpContext)} <jk>returns</jk> HttpResponse</c>
 * 	</ul>
 * </ul>
 *
 * <p class='w900'>
 * Note that there are other ways of accomplishing this such as extending the {@link RestClient} class and overriding
 * the {@link #run(HttpHost,HttpRequest,HttpContext)} method
 * or by defining your own {@link HttpRequestExecutor}.  Using this interface is often simpler though.
 *
 *
 * <h4 class='topic'>Interceptors</h4>
 *
 * <p class='w900'>
 * The {@link RestCallInterceptor} API provides a quick way of intercepting and manipulating requests and responses beyond
 * the existing {@link HttpRequestInterceptor} and {@link HttpResponseInterceptor} APIs.
 *
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestClientBuilder}
 * 	<ul>
 * 		<li class='jm'>{@link RestClientBuilder#interceptors(Object...) interceptors(Object...)}
 * 	</ul>
 * 	<li class='jc'>{@link RestRequest}
 * 	<ul>
 * 		<li class='jm'>{@link RestRequest#interceptors(RestCallInterceptor...) interceptors(RestCallInterceptor...)}
 * 	</ul>
 * 	<li class='jic'>{@link RestCallInterceptor}
 * 	<ul>
 * 		<li class='jm'>{@link RestCallInterceptor#onInit(RestRequest) onInit(RestRequest)}
 * 		<li class='jm'>{@link RestCallInterceptor#onConnect(RestRequest,RestResponse) onConnect(RestRequest,RestResponse)}
 * 		<li class='jm'>{@link RestCallInterceptor#onClose(RestRequest,RestResponse) onClose(RestRequest,RestResponse)}
 * 	</ul>
 * </ul>
 *
 *
 * <h4 class='topic'>Logging / Debugging</h4>
 *
 * <p class='w900'>
 * The following methods provide logging of requests and responses:
 *
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestClientBuilder}
 * 	<ul>
 * 		<li class='jm'>{@link RestClientBuilder#logger(Logger) logger(Logger)}
 * 		<li class='jm'>{@link RestClientBuilder#logToConsole() logToConsole()}
 * 		<li class='jm'>{@link RestClientBuilder#logRequests(DetailLevel,Level,BiPredicate) logRequests(DetailLevel,Level,BiPredicate)}
 * 	</ul>
 * </ul>
 *
 * <p>
 * The following example shows the results of logging all requests that end with <c>/bean</c>.
 *
 * <h5 class='figure'>Examples:</h5>
 * <p class='bcode w800'>
 * 	MyBean <jv>bean</jv> = RestClient
 * 		.<jsm>create</jsm>()
 * 		.simpleJson()
 * 		.logRequests(DetailLevel.<jsf>FULL</jsf>, Level.<jsf>SEVERE</jsf>, (<jv>req</jv>,<jv>res</jv>)-&gt;<jv>req</jv>.getUri().endsWith(<js>"/bean"</js>))
 * 		.logToConsole()
 * 		.build()
 * 		.post(<js>"http://localhost/bean"</js>, <jv>anotherBean</jv>)
 * 		.run()
 * 		.getBody().as(MyBean.<jk>class</jk>);
 * </p>
 *
 * <p>
 * This produces the following console output:
 *
 * <p class='bcode w800 console'>
 * 	=== HTTP Call (outgoing) ======================================================
 * 	=== REQUEST ===
 * 	POST http://localhost/bean
 * 	---request headers---
 * 		Accept: application/json+simple
 * 	---request entity---
 * 	Content-Type: application/json+simple
 * 	---request content---
 * 	{f:1}
 * 	=== RESPONSE ===
 * 	HTTP/1.1 200
 * 	---response headers---
 * 		Content-Type: application/json
 * 	---response content---
 * 	{f:1}
 * 	=== END =======================================================================",
 * </p>
 *
 *
 * <p class='notes w900'>
 * It should be noted that if you enable request logging detail level {@link DetailLevel#FULL}, response bodies will be cached by default which may introduce
 * a performance penalty.
 *
 * <p class='w900'>
 * Additionally, the following method is also provided for enabling debug mode:
 *
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestClientBuilder}
 * 	<ul>
 * 		<li class='jm'>{@link RestClientBuilder#debug() debug()}
 * 	</ul>
 * </ul>
 *
 * <p class='w900'>
 * Enabling debug mode has the following effects:
 * <ul>
 * 	<li>{@link Context#CONTEXT_debug} is enabled.
 * 	<li>{@link RestClientBuilder#detectLeaks()} is enabled.
 * 	<li>{@link RestClientBuilder#logToConsole()} is called.
 * </ul>
 *
 *
 * <h4 class='topic'>REST Proxies</h4>
 *
 * <p class='w900'>
 * One of the more powerful features of the REST client class is the ability to produce Java interface proxies against
 * arbitrary remote REST resources.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Define a Remote proxy for interacting with a REST interface.</jc>
 * 	<ja>@Remote</ja>(path=<js>"/petstore"</js>)
 * 	<jk>public interface</jk> PetStore {
 *
 * 		<ja>@RemotePost</ja>(<js>"/pets"</js>)
 * 		Pet addPet(
 * 			<ja>@Body</ja> CreatePet <jv>pet</jv>,
 * 			<ja>@Header</ja>(<js>"E-Tag"</js>) UUID <jv>etag</jv>,
 * 			<ja>@Query</ja>(<js>"debug"</js>) <jk>boolean</jk> <jv>debug</jv>
 * 		);
 * 	}
 *
 * 	<jc>// Use a RestClient with default Simple JSON support.</jc>
 * 	RestClient <jv>client</jv> = RestClient.<jsm>create</jsm>().simpleJson().build())
 *
 * 	PetStore <jv>store</jv> = <jv>client</jv>.getRemote(PetStore.<jk>class</jk>, <js>"http://localhost:10000"</js>);
 * 	CreatePet <jv>createPet</jv> = <jk>new</jk> CreatePet(<js>"Fluffy"</js>, 9.99);
 * 	Pet <jv>pet</jv> = <jv>store</jv>.addPet(<jv>createPet</jv>, UUID.<jsm>randomUUID</jsm>(), <jk>true</jk>);
 * </p>
 *
 * <p class='w900'>
 * The methods to retrieve remote interfaces are:
 *
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestClient}
 * 	<ul>
 * 		<li class='jm'><c>{@link RestClient#getRemote(Class) getRemote(Class&lt;T&gt;)} <jk>returns</jk> T</c>
 * 		<li class='jm'><c>{@link RestClient#getRemote(Class,Object) getRemote(Class&lt;T&gt;,Object)} <jk>returns</jk> T</c>
 * 		<li class='jm'><c>{@link RestClient#getRemote(Class,Object,Serializer,Parser) getRemote(Class&lt;T&gt;,Object,Serializer,Parser)} <jk>returns</jk> T</c>
 * 		<li class='jm'><c>{@link RestClient#getRrpcInterface(Class) getRrpcInterface(Class&lt;T&gt;)} <jk>returns</jk> T</c>
 * 		<li class='jm'><c>{@link RestClient#getRrpcInterface(Class,Object) getRrpcInterface(Class&lt;T&gt;,Object)} <jk>returns</jk> T</c>
 * 		<li class='jm'><c>{@link RestClient#getRrpcInterface(Class,Object,Serializer,Parser) getRrpcInterface(Class&lt;T&gt;,Object,Serializer,Parser)} <jk>returns</jk> T</c>
 * 	</ul>
 * </ul>
 *
 * <p class='w900'>
 * Two basic types of remote interfaces are provided:
 *
 * <ul class='spaced-list'>
 * 	<li>{@link Remote @Remote}-annotated interfaces.  These can be defined against arbitrary external REST resources.
 * 	<li>RPC-over-REST interfaces.  These are Java interfaces that allow you to make method calls on server-side POJOs.
 * </ul>
 *
 * <p class='w900'>
 * Refer to the following documentation on both flavors:
 *
 * <ul class='doctree'>
 * 	<li class='link'>{@doc RestcProxies}
 * 	<li class='link'>{@doc RestRpc}
 * </ul>
 *
 * <br>
 * <hr class='w900'>
 * <h4 class='topic'>Customizing Apache HttpClient</h4>
 *
 * <p class='w900'>
 * Several methods are provided for customizing the underlying HTTP client and client builder classes:
 * <ul class='javatree'>
 * 	<li class='jc'>{@link RestClientBuilder}
 * 	<ul>
 * 		<li class='jm'>{@link RestClientBuilder#httpClientBuilder(HttpClientBuilder) httpClientBuilder(HttpClientBuilder)} - Set the client builder yourself.
 * 		<li class='jm'>{@link RestClientBuilder#createHttpClientBuilder() createHttpClientBuilder()} - Override to create the client builder.
 * 		<li class='jm'>{@link RestClientBuilder#createHttpClient() createHttpClient()} - Override to create the client.
 * 		<li class='jm'>{@link RestClientBuilder#createConnectionManager() createConnectionManager()} - Override to create the connection management.
 * 	</ul>
 * </ul>
 *
 * <p class='w900'>
 * Additionally, all methods on the <c>HttpClientBuilder</c> class have been extended with fluent setters.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Create a client with customized HttpClient settings.</jc>
 * 	MyBean <jv>bean</jv> = RestClient
 * 		.<jsm>create</jsm>()
 * 		.disableRedirectHandling()
 * 		.connectionManager(<jv>myConnectionManager</jv>)
 * 		.addInterceptorFirst(<jv>myHttpRequestInterceptor</jv>)
 * 		.build();
 * </p>
 *
 * <p>
 * Refer to the {@link HttpClientBuilder HTTP Client Builder API} for more information.
 *
 *
 * <h4 class='topic'>Extending RestClient</h4>
 *
 * <p class='w900'>
 * The <c>RestClient</c> API has been designed to allow for the ability to be easily extended.
 * The following example that overrides the primary run method shows how this can be done.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jk>public class</jk> MyRestClient <jk>extends</jk> RestClient {
 *
 * 		<jc>// Must provide this constructor!</jc>
 * 		<jk>public</jk> MyRestClient(ContextProperties <jv>properties</jv>) {
 * 			<jk>super</jk>(<jv>properties</jv>);
 * 		}
 *
 * 		<ja>@Override</ja>
 * 		<jk>public</jk> HttpResponse run(HttpHost <jv>target</jv>, HttpRequest <jv>request</jv>, HttpContext <jv>context</jv>) <jk>throws</jk> IOException {
 * 			<jc>// Perform special handling of requests.</jc>
 * 		}
 * 	}
 *
 * 	<jc>// Instantiate your client.</jc>
 * 	MyRestClient <jv>client</jv> = RestClient.<jsm>create</jsm>().json().build(MyRestClient.<jk>class</jk>);
 * </p>
 *
 * <p class='w900'>
 * The {@link RestRequest} and {@link RestResponse} objects can also be extended and integrated by overriding the
 * {@link RestClient#createRequest(URI,String,boolean)} and {@link RestClient#createResponse(RestRequest,HttpResponse,Parser)} methods.
 *
 * <ul class='seealso'>
 * 	<li class='link'>{@doc juneau-rest-client}
 * </ul>
 */
@ConfigurableContext(nocache=true)
public class RestClient extends BeanContextable implements HttpClient, Closeable, RestCallHandler, RestCallInterceptor {

	final HeaderList.Builder headerData;
	final PartList.Builder queryData, formData, pathData;
	final CloseableHttpClient httpClient;

	private final HttpClientConnectionManager connectionManager;
	private final boolean keepHttpClientOpen, detectLeaks, skipEmptyHeaderData, skipEmptyQueryData, skipEmptyFormData;
	private final BeanStore beanStore;
	private final UrlEncodingSerializer urlEncodingSerializer;  // Used for form posts only.
	final HttpPartSerializer partSerializer;
	final HttpPartParser partParser;
	private final RestCallHandler callHandler;
	private final String rootUri;
	private volatile boolean isClosed = false;
	private final StackTraceElement[] creationStack;
	private final Logger logger;
	final DetailLevel logRequests;
	final BiPredicate<RestRequest,RestResponse> logRequestsPredicate;
	final Level logRequestsLevel;
	final boolean ignoreErrors;
	private final boolean logToConsole;
	private final PrintStream console;
	private StackTraceElement[] closedStack;
	private static final ConcurrentHashMap<Class<?>,Context> requestContexts = new ConcurrentHashMap<>();

	// These are read directly by RestCall.
	final SerializerGroup serializers;
	final ParserGroup parsers;
	Predicate<Integer> errorCodes;

	final RestCallInterceptor[] interceptors;

	private final Map<Class<?>, HttpPartParser> partParsers = new ConcurrentHashMap<>();
	private final Map<Class<?>, HttpPartSerializer> partSerializers = new ConcurrentHashMap<>();

	// This is lazy-created.
	private volatile ExecutorService executorService;
	private final boolean executorServiceShutdownOnClose;

	/**
	 * Instantiates a new clean-slate {@link RestClientBuilder} object.
	 *
	 * @return A new {@link RestClientBuilder} object.
	 */
	public static RestClientBuilder create() {
		return new RestClientBuilder();
	}

	@Override /* Context */
	public RestClientBuilder copy() {
		throw new NoSuchMethodError("Not implemented.");
	}

	private static final
		BiPredicate<RestRequest,RestResponse> LOG_REQUESTS_PREDICATE_DEFAULT = (req,res) -> true;

	/**
	 * Constructor.
	 *
	 * @param builder The builder for this client.
	 */
	public RestClient(RestClientBuilder builder) {
		super(builder);

		beanStore = builder.beanStore
			.addBean(RestClient.class, this);

		httpClient = builder.getHttpClient();
		headerData = builder.headerData().build().copy();
		queryData = builder.queryData().build().copy();
		formData = builder.formData().build().copy();
		pathData = builder.pathData().build().copy();
		callHandler = builder.callHandler().run();
		skipEmptyHeaderData = builder.skipEmptyHeaderData;
		skipEmptyQueryData = builder.skipEmptyQueryData;
		skipEmptyFormData = builder.skipEmptyFormData;
		rootUri = builder.rootUri;
		errorCodes = builder.errorCodes;
		connectionManager = builder.connectionManager;
		console = ofNullable(builder.console).orElse(System.err);
		executorService = builder.executorService;
		executorServiceShutdownOnClose = builder.executorServiceShutdownOnClose;
		ignoreErrors = builder.ignoreErrors;
		keepHttpClientOpen = builder.keepHttpClientOpen;
		detectLeaks = builder.detectLeaks;
		logger = ofNullable(builder.logger).orElseGet(()->Logger.getLogger(RestClient.class.getName()));
		logToConsole = builder.logToConsole || isDebug();
		logRequests = ofNullable(builder.logRequests).orElse(isDebug() ? DetailLevel.FULL : DetailLevel.NONE);
		logRequestsLevel = ofNullable(builder.logRequestsLevel).orElse(isDebug() ? Level.WARNING : Level.OFF);
		logRequestsPredicate = ofNullable(builder.logRequestsPredicate).orElse(LOG_REQUESTS_PREDICATE_DEFAULT);
		interceptors = ofNullable(builder.interceptors).map(x -> x.toArray(new RestCallInterceptor[x.size()])).orElse(new RestCallInterceptor[0]);
		serializers = builder.serializers().build();
		parsers = builder.parsers().build();
		partSerializer = builder.partSerializer().create();
		partParser = builder.partParser().create();
		urlEncodingSerializer = builder.urlEncodingSerializer().build();
		creationStack = isDebug() ? Thread.currentThread().getStackTrace() : null;
	}

	/**
	 * Calls {@link CloseableHttpClient#close()} on the underlying {@link CloseableHttpClient}.
	 *
	 * <p>
	 * It's good practice to call this method after the client is no longer used.
	 *
	 * @throws IOException Thrown by underlying stream.
	 */
	@Override
	public void close() throws IOException {
		isClosed = true;
		if (! keepHttpClientOpen)
			httpClient.close();
		if (executorService != null && executorServiceShutdownOnClose)
			executorService.shutdown();
		if (creationStack != null)
			closedStack = Thread.currentThread().getStackTrace();
	}

	/**
	 * Same as {@link #close()}, but ignores any exceptions.
	 */
	public void closeQuietly() {
		isClosed = true;
		try {
			if (! keepHttpClientOpen)
				httpClient.close();
			if (executorService != null && executorServiceShutdownOnClose)
				executorService.shutdown();
		} catch (Throwable t) {}
		if (creationStack != null)
			closedStack = Thread.currentThread().getStackTrace();
	}

	/**
	 * Entrypoint for executing all requests and returning a response.
	 *
	 * <p>
	 * Subclasses can override this method to provide specialized handling.
	 *
	 * <p>
	 * The behavior of this method can also be modified by specifying a different {@link RestCallHandler}.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link RestClientBuilder#callHandler()}
	 * </ul>
	 *
	 * @param target The target host for the request.
	 * 	<br>Implementations may accept <jk>null</jk> if they can still determine a route, for example to a default
	 * 		target or by inspecting the request.
	 * @param request The request to execute.
	 * @param context The context to use for the execution, or <jk>null</jk> to use the default context.
	 * @return
	 * 	The response to the request.
	 * 	<br>This is always a final response, never an intermediate response with an 1xx status code.
	 * 	<br>Whether redirects or authentication challenges will be returned or handled automatically depends on the
	 * 		implementation and configuration of this client.
	 * @throws IOException In case of a problem or the connection was aborted.
	 * @throws ClientProtocolException In case of an http protocol error.
	 */
	@Override /* RestCallHandler */
	public HttpResponse run(HttpHost target, HttpRequest request, HttpContext context) throws ClientProtocolException, IOException {
		return callHandler.run(target, request, context);
	}

	/**
	 * Perform a <c>GET</c> request against the specified URI.
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest get(Object uri) throws RestCallException {
		return request(op("GET", uri, NO_BODY));
	}

	/**
	 * Perform a <c>GET</c> request against the root URI.
	 *
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest get() throws RestCallException {
		return request(op("GET", null, NO_BODY));
	}

	/**
	 * Perform a <c>PUT</c> request against the specified URI.
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @param body
	 * 	The object to serialize and transmit to the URI as the body of the request.
	 * 	Can be of the following types:
	 * 	<ul class='spaced-list'>
	 * 		<li>
	 * 			{@link Reader} - Raw contents of {@code Reader} will be serialized to remote resource.
	 * 		<li>
	 * 			{@link InputStream} - Raw contents of {@code InputStream} will be serialized to remote resource.
	 * 		<li>
	 * 			{@link Object} - POJO to be converted to text using the {@link Serializer} registered with the
	 * 			{@link RestClient}.
	 * 		<li>
	 * 			{@link HttpEntity} / {@link HttpResource} - Bypass Juneau serialization and pass HttpEntity directly to HttpClient.
	 * 		<li>
	 * 			{@link PartList} - Converted to a URL-encoded FORM post.
	 * 		<li>
	 * 			{@link Supplier} - A supplier of anything on this list.
	 * 	</ul>
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request
	 * 	and getting the response as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest put(Object uri, Object body) throws RestCallException {
		return request(op("PUT", uri, body));
	}

	/**
	 * Perform a <c>PUT</c> request against the specified URI using a plain text body bypassing the serializer.
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @param body
	 * 	The object to serialize and transmit to the URI as the body of the request bypassing the serializer.
	 * @param contentType The content type of the request.
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request
	 * 	and getting the response as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest put(Object uri, String body, ContentType contentType) throws RestCallException {
		return request(op("PUT", uri, stringBody(body))).header(contentType);
	}

	/**
	 * Same as {@link #put(Object, Object)} but don't specify the input yet.
	 *
	 * <p>
	 * You must call either {@link RestRequest#body(Object)} or {@link RestRequest#formData(String, Object)}
	 * to set the contents on the result object.
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException REST call failed.
	 */
	public RestRequest put(Object uri) throws RestCallException {
		return request(op("PUT", uri, NO_BODY));
	}

	/**
	 * Perform a <c>POST</c> request against the specified URI.
	 *
	 * <ul class='notes'>
	 * 	<li>Use {@link #formPost(Object, Object)} for <c>application/x-www-form-urlencoded</c> form posts.
	 * </ul>
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @param body
	 * 	The object to serialize and transmit to the URI as the body of the request.
	 * 	Can be of the following types:
	 * 	<ul class='spaced-list'>
	 * 		<li>
	 * 			{@link Reader} - Raw contents of {@code Reader} will be serialized to remote resource.
	 * 		<li>
	 * 			{@link InputStream} - Raw contents of {@code InputStream} will be serialized to remote resource.
	 * 		<li>
	 * 			{@link Object} - POJO to be converted to text using the {@link Serializer} registered with the
	 * 			{@link RestClient}.
	 * 		<li>
	 * 			{@link HttpEntity} / {@link HttpResource} - Bypass Juneau serialization and pass HttpEntity directly to HttpClient.
	 * 		<li>
	 * 			{@link PartList} - Converted to a URL-encoded FORM post.
	 * 		<li>
	 * 			{@link Supplier} - A supplier of anything on this list.
	 * 	</ul>
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest post(Object uri, Object body) throws RestCallException {
		return request(op("POST", uri, body));
	}

	/**
	 * Perform a <c>POST</c> request against the specified URI as a plain text body bypassing the serializer.
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @param body
	 * 	The object to serialize and transmit to the URI as the body of the request bypassing the serializer.
	 * @param contentType
	 * 	The content type of the request.
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest post(Object uri, String body, ContentType contentType) throws RestCallException {
		return request(op("POST", uri, stringBody(body))).header(contentType);
	}

	/**
	 * Same as {@link #post(Object, Object)} but don't specify the input yet.
	 *
	 * <p>
	 * You must call either {@link RestRequest#body(Object)} or {@link RestRequest#formData(String, Object)} to set the
	 * contents on the result object.
	 *
	 * <ul class='notes'>
	 * 	<li>Use {@link #formPost(Object, Object)} for <c>application/x-www-form-urlencoded</c> form posts.
	 * </ul>
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException REST call failed.
	 */
	public RestRequest post(Object uri) throws RestCallException {
		return request(op("POST", uri, NO_BODY));
	}

	/**
	 * Perform a <c>DELETE</c> request against the specified URI.
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest delete(Object uri) throws RestCallException {
		return request(op("DELETE", uri, NO_BODY));
	}

	/**
	 * Perform an <c>OPTIONS</c> request against the specified URI.
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest options(Object uri) throws RestCallException {
		return request(op("OPTIONS", uri, NO_BODY));
	}

	/**
	 * Perform a <c>HEAD</c> request against the specified URI.
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest head(Object uri) throws RestCallException {
		return request(op("HEAD", uri, NO_BODY));
	}

	/**
	 * Perform a <c>POST</c> request with a content type of <c>application/x-www-form-urlencoded</c>
	 * against the specified URI.
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @param body
	 * 	The object to serialize and transmit to the URI as the body of the request.
	 * 	<ul class='spaced-list'>
	 * 		<li>{@link NameValuePair} - URL-encoded as a single name-value pair.
	 * 		<li>{@link NameValuePair} array - URL-encoded as name value pairs.
	 * 		<li>{@link PartList} - URL-encoded as name value pairs.
	 * 		<li>{@link Reader}/{@link InputStream}- Streamed directly and <l>Content-Type</l> set to <js>"application/x-www-form-urlencoded"</js>
	 * 		<li>{@link HttpResource} - Raw contents will be serialized to remote resource.  Additional headers and media type will be set on request.
	 * 		<li>{@link HttpEntity} - Bypass Juneau serialization and pass HttpEntity directly to HttpClient.
	 * 		<li>{@link Object} - Converted to a {@link SerializedEntity} using {@link UrlEncodingSerializer} to serialize.
	 * 		<li>{@link Supplier} - A supplier of anything on this list.
	 * 	</ul>
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest formPost(Object uri, Object body) throws RestCallException {
		RestRequest req = request(op("POST", uri, NO_BODY));
		try {
			if (body instanceof Supplier)
				body = ((Supplier<?>)body).get();
			if (body instanceof NameValuePair)
				return req.body(new UrlEncodedFormEntity(AList.of((NameValuePair)body)));
			if (body instanceof NameValuePair[])
				return req.body(new UrlEncodedFormEntity(Arrays.asList((NameValuePair[])body)));
			if (body instanceof PartList)
				return req.body(new UrlEncodedFormEntity(((PartList)body).asNameValuePairs()));
			if (body instanceof HttpResource)
				((HttpResource)body).getHeaders().forEach(x-> req.header(x));
			if (body instanceof HttpEntity) {
				HttpEntity e = (HttpEntity)body;
				if (e.getContentType() == null)
					req.header(ContentType.APPLICATION_FORM_URLENCODED);
				return req.body(e);
			}
			if (body instanceof Reader || body instanceof InputStream)
				return req.header(ContentType.APPLICATION_FORM_URLENCODED).body(body);
			return req.body(serializedEntity(body, urlEncodingSerializer, null).build());
		} catch (IOException e) {
			throw new RestCallException(null, e, "Could not read form post body.");
		}
	}

	/**
	 * Same as {@link #formPost(Object, Object)} but doesn't specify the input yet.
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest formPost(Object uri) throws RestCallException {
		return request(op("POST", uri, NO_BODY));
	}

	/**
	 * Perform a <c>POST</c> request with a content type of <c>application/x-www-form-urlencoded</c>
	 * against the specified URI.
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @param parameters
	 * 	The parameters of the form post.
	 * 	<br>The parameters represent name/value pairs and must be an even number of arguments.
	 * 	<br>Parameters are converted to {@link BasicPart} objects.
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest formPostPairs(Object uri, Object...parameters) throws RestCallException {
		return formPost(uri, partList(parameters));
	}

	/**
	 * Perform a <c>PATCH</c> request against the specified URI.
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @param body
	 * 	The object to serialize and transmit to the URI as the body of the request.
	 * 	Can be of the following types:
	 * 	<ul class='spaced-list'>
	 * 		<li>
	 * 			{@link Reader} - Raw contents of {@code Reader} will be serialized to remote resource.
	 * 		<li>
	 * 			{@link InputStream} - Raw contents of {@code InputStream} will be serialized to remote resource.
	 * 		<li>
	 * 			{@link HttpResource} - Raw contents will be serialized to remote resource.  Additional headers and media type will be set on request.
	 * 		<li>
	 * 			{@link HttpEntity} - Bypass Juneau serialization and pass HttpEntity directly to HttpClient.
	 * 		<li>
	 * 			{@link Object} - POJO to be converted to text using the {@link Serializer} registered with the
	 * 			{@link RestClient}.
	 * 		<li>
	 * 			{@link PartList} - Converted to a URL-encoded FORM post.
	 * 		<li>
	 * 			{@link Supplier} - A supplier of anything on this list.
	 * 	</ul>
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest patch(Object uri, Object body) throws RestCallException {
		return request(op("PATCH", uri, body));
	}

	/**
	 * Perform a <c>PATCH</c> request against the specified URI as a plain text body bypassing the serializer.
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @param body
	 * 	The object to serialize and transmit to the URI as the body of the request bypassing the serializer.
	 * @param contentType
	 * 	The content type of the request.
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest patch(Object uri, String body, ContentType contentType) throws RestCallException {
		return request(op("PATCH", uri, stringBody(body))).header(contentType);
	}

	/**
	 * Same as {@link #patch(Object, Object)} but don't specify the input yet.
	 *
	 * <p>
	 * You must call {@link RestRequest#body(Object)} to set the contents on the result object.
	 *
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException REST call failed.
	 */
	public RestRequest patch(Object uri) throws RestCallException {
		return request(op("PATCH", uri, NO_BODY));
	}


	/**
	 * Performs a REST call where the entire call is specified in a simple string.
	 *
	 * <p>
	 * This method is useful for performing callbacks when the target of a callback is passed in
	 * on an initial request, for example to signal when a long-running process has completed.
	 *
	 * <p>
	 * The call string can be any of the following formats:
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		<js>"[method] [uri]"</js> - e.g. <js>"GET http://localhost/callback"</js>
	 * 	<li>
	 * 		<js>"[method] [uri] [payload]"</js> - e.g. <js>"POST http://localhost/callback some text payload"</js>
	 * 	<li>
	 * 		<js>"[method] [headers] [uri] [payload]"</js> - e.g. <js>"POST {'Content-Type':'text/json'} http://localhost/callback {'some':'json'}"</js>
	 * </ul>
	 * <p>
	 * The payload will always be sent using a simple {@link StringEntity}.
	 *
	 * @param callString The call string.
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException REST call failed.
	 */
	public RestRequest callback(String callString) throws RestCallException {
		callString = emptyIfNull(callString);

		// S01 - Looking for end of method.
		// S02 - Found end of method, looking for beginning of URI or headers.
		// S03 - Found beginning of headers, looking for end of headers.
		// S04 - Found end of headers, looking for beginning of URI.
		// S05 - Found beginning of URI, looking for end of URI.

		StateMachineState state = S01;

		int mark = 0;
		String method = null, headers = null, uri = null, content = null;
		for (int i = 0; i < callString.length(); i++) {
			char c = callString.charAt(i);
			if (state == S01) {
				if (isWhitespace(c)) {
					method = callString.substring(mark, i);
					state = S02;
				}
			} else if (state == S02) {
				if (! isWhitespace(c)) {
					mark = i;
					if (c == '{')
						state = S03;
					else
						state = S05;
				}
			} else if (state == S03) {
				if (c == '}') {
					headers = callString.substring(mark, i+1);
					state = S04;
				}
			} else if (state == S04) {
				if (! isWhitespace(c)) {
					mark = i;
					state = S05;
				}
			} else /* (state == S05) */ {
				if (isWhitespace(c)) {
					uri = callString.substring(mark, i);
					content = callString.substring(i).trim();
					break;
				}
			}
		}

		if (state != S05)
			throw new RestCallException(null, null, "Invalid format for call string.  State={0}", state);

		try {
			RestRequest req = request(method, uri, isNotEmpty(content));
			if (headers != null)
				for (Map.Entry<String,Object> e : OMap.ofJson(headers).entrySet())
					req.header(stringHeader(e.getKey(), stringify(e.getValue())));
			if (isNotEmpty(content))
				req.bodyString(content);
			return req;
		} catch (ParseException e) {
			throw new RestCallException(null, e, "Invalid format for call string.");
		}
	}

	/**
	 * Perform a generic REST call.
	 *
	 * @param method The HTTP method.
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @param body
	 * 	The HTTP body content.
	 * 	Can be of the following types:
	 * 	<ul class='spaced-list'>
	 * 		<li>
	 * 			{@link Reader} - Raw contents of {@code Reader} will be serialized to remote resource.
	 * 		<li>
	 * 			{@link InputStream} - Raw contents of {@code InputStream} will be serialized to remote resource.
	 * 		<li>
	 * 			{@link HttpResource} - Raw contents will be serialized to remote resource.  Additional headers and media type will be set on request.
	 * 		<li>
	 * 			{@link HttpEntity} - Bypass Juneau serialization and pass HttpEntity directly to HttpClient.
	 * 		<li>
	 * 			{@link Object} - POJO to be converted to text using the {@link Serializer} registered with the
	 * 			{@link RestClient}.
	 * 		<li>
	 * 			{@link PartList} - Converted to a URL-encoded FORM post.
	 * 		<li>
	 * 			{@link Supplier} - A supplier of anything on this list.
	 * 	</ul>
	 * 	This parameter is IGNORED if the method type normally does not have content.
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest request(String method, Object uri, Object body) throws RestCallException {
		return request(op(method, uri, body));
	}

	/**
	 * Perform a generic REST call.
	 *
	 * @param method The HTTP method.
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest request(String method, Object uri) throws RestCallException {
		return request(op(method, uri, NO_BODY));
	}

	/**
	 * Perform a generic REST call.
	 *
	 * <p>
	 * Typically you're going to use {@link #request(String, Object)} or {@link #request(String, Object, Object)},
	 * but this method is provided to allow you to perform non-standard HTTP methods (e.g. HTTP FOO).
	 *
	 * @param method The method name (e.g. <js>"GET"</js>, <js>"OPTIONS"</js>).
	 * @param uri
	 * 	The URI of the remote REST resource.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link URIBuilder}
	 * 		<li>{@link URI}
	 * 		<li>{@link URL}
	 * 		<li>{@link String}
	 * 		<li>{@link Object} - Converted to <c>String</c> using <c>toString()</c>
	 * 	</ul>
	 * @param hasBody Boolean flag indicating if the specified request has content associated with it.
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest request(String method, Object uri, boolean hasBody) throws RestCallException {
		return request(op(method, uri, NO_BODY).hasBody(hasBody));
	}

	/**
	 * Perform an arbitrary request against the specified URI.
	 *
	 * @param op The operation that identifies the HTTP method, URL, and optional payload.
	 * @return
	 * 	A {@link RestRequest} object that can be further tailored before executing the request and getting the response
	 * 	as a parsed object.
	 * @throws RestCallException If any authentication errors occurred.
	 */
	public RestRequest request(RestOperation op) throws RestCallException {
		if (isClosed) {
			Exception e2 = null;
			if (closedStack != null) {
				e2 = new Exception("Creation stack:");
				e2.setStackTrace(closedStack);
				throw new RestCallException(null, e2, "RestClient.close() has already been called.  This client cannot be reused.");
			}
			throw new RestCallException(null, null, "RestClient.close() has already been called.  This client cannot be reused.  Closed location stack trace can be displayed by setting the system property 'org.apache.juneau.rest.client2.RestClient.trackCreation' to true.");
		}

		RestRequest req = createRequest(toURI(op.getUri(), rootUri), op.getMethod(), op.hasBody());

		onInit(req);

		req.body(op.getBody());

		return req;
	}

	/**
	 * Creates a {@link RestRequest} object from the specified {@link HttpRequest} object.
	 *
	 * <p>
	 * Subclasses can override this method to provide their own specialized {@link RestRequest} objects.
	 *
	 * @param uri The target.
	 * @param method The HTTP method (uppercase).
	 * @param hasBody Whether this method has a request entity.
	 * @return A new {@link RestRequest} object.
	 * @throws RestCallException If an exception or non-200 response code occurred during the connection attempt.
	 */
	protected RestRequest createRequest(URI uri, String method, boolean hasBody) throws RestCallException {
		return new RestRequest(this, uri, method, hasBody);
	}

	/**
	 * Creates a {@link RestResponse} object from the specified {@link HttpResponse} object.
	 *
	 * <p>
	 * Subclasses can override this method to provide their own specialized {@link RestResponse} objects.
	 *
	 * @param request The request creating this response.
	 * @param httpResponse The response object to wrap.
	 * @param parser The parser to use to parse the response.
	 *
	 * @return A new {@link RestResponse} object.
	 * @throws RestCallException If an exception or non-200 response code occurred during the connection attempt.
	 */
	protected RestResponse createResponse(RestRequest request, HttpResponse httpResponse, Parser parser) throws RestCallException {
		return new RestResponse(this, request, httpResponse, parser);
	}

	/**
	 * Create a new proxy interface against a 3rd-party REST interface.
	 *
	 * <p>
	 * The URI to the REST interface is based on the following values:
	 * <ul>
	 * 	<li>The {@link Remote#path() @Remote(path)} annotation on the interface (<c>remote-path</c>).
	 * 	<li>The {@link RestClientBuilder#rootUri(Object) rootUri} on the client (<c>root-url</c>).
	 * 	<li>The fully-qualified class name of the interface (<c>class-name</c>).
	 * </ul>
	 *
	 * <p>
	 * The URI calculation is as follows:
	 * <ul>
	 * 	<li><c>remote-path</c> - If remote path is absolute.
	 * 	<li><c>root-uri/remote-path</c> - If remote path is relative and root-uri has been specified.
	 * 	<li><c>root-uri/class-name</c> - If remote path is not specified.
	 * </ul>
	 *
	 * <p>
	 * If the information is not available to resolve to an absolute URI, a {@link RemoteMetadataException} is thrown.
	 *
	 * <h5 class='section'>Examples:</h5>
	 * <p class='bcode w800'>
	 * 	<jk>package</jk> <jk>org.apache.foo</jk>;
	 *
	 * 	<ja>@RemoteResource</ja>(path=<js>"http://hostname/resturi/myinterface1"</js>)
	 * 	<jk>public interface</jk> MyInterface1 { ... }
	 *
	 * 	<ja>@RemoteResource</ja>(path=<js>"/myinterface2"</js>)
	 * 	<jk>public interface</jk> MyInterface2 { ... }
	 *
	 * 	<jk>public interface</jk> MyInterface3 { ... }
	 *
	 * 	<jc>// Resolves to "http://localhost/resturi/myinterface1"</jc>
	 * 	MyInterface1 <jv>i1</jv> = RestClient
	 * 		.<jsm>create</jsm>()
	 * 		.build()
	 * 		.getRemote(MyInterface1.<jk>class</jk>);
	 *
	 * 	<jc>// Resolves to "http://hostname/resturi/myinterface2"</jc>
	 * 	MyInterface2 <jv>i2</jv> = RestClient
	 * 		.<jsm>create</jsm>()
	 * 		.rootUri(<js>"http://hostname/resturi"</js>)
	 * 		.build()
	 * 		.getRemote(MyInterface2.<jk>class</jk>);
	 *
	 * 	<jc>// Resolves to "http://hostname/resturi/org.apache.foo.MyInterface3"</jc>
	 * 	MyInterface3 <jv>i3</jv> = RestClient
	 * 		.<jsm>create</jsm>()
	 * 		.rootUri(<js>"http://hostname/resturi"</js>)
	 * 		.build()
	 * 		.getRemote(MyInterface3.<jk>class</jk>);
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		If you plan on using your proxy in a multi-threaded environment, you'll want to use an underlying
	 * 		pooling client connection manager.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc RestcProxies}
	 * </ul>
	 *
	 * @param interfaceClass The interface to create a proxy for.
	 * @return The new proxy interface.
	 * @throws RemoteMetadataException If the REST URI cannot be determined based on the information given.
	 */
	public <T> T getRemote(Class<T> interfaceClass) {
		return getRemote(interfaceClass, null);
	}

	/**
	 * Same as {@link #getRemote(Class)} except explicitly specifies the URI of the REST interface.
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc RestcProxies}
	 * </ul>
	 *
	 * @param interfaceClass The interface to create a proxy for.
	 * @param rootUri The URI of the REST interface.
	 * @return The new proxy interface.
	 */
	public <T> T getRemote(Class<T> interfaceClass, Object rootUri) {
		return getRemote(interfaceClass, rootUri, null, null);
	}

	/**
	 * Same as {@link #getRemote(Class, Object)} but allows you to override the serializer and parser used.
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc RestcProxies}
	 * </ul>

	 * @param interfaceClass The interface to create a proxy for.
	 * @param rootUri The URI of the REST interface.
	 * @param serializer The serializer used to serialize POJOs to the body of the HTTP request.
	 * @param parser The parser used to parse POJOs from the body of the HTTP response.
	 * @return The new proxy interface.
	 */
	@SuppressWarnings({ "unchecked" })
	public <T> T getRemote(final Class<T> interfaceClass, Object rootUri, final Serializer serializer, final Parser parser) {

		if (rootUri == null)
			rootUri = this.rootUri;

		final String restUrl2 = trimSlashes(emptyIfNull(rootUri));

		return (T)Proxy.newProxyInstance(
			interfaceClass.getClassLoader(),
			new Class[] { interfaceClass },
			new InvocationHandler() {

				final RemoteMeta rm = new RemoteMeta(interfaceClass);

				@Override /* InvocationHandler */
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					RemoteOperationMeta rom = rm.getOperationMeta(method);

					String uri = rom.getFullPath();
					if (uri.indexOf("://") == -1)
						uri = restUrl2 + '/' + uri;
					if (uri.indexOf("://") == -1)
						throw new RemoteMetadataException(interfaceClass, "Root URI has not been specified.  Cannot construct absolute path to remote resource.");

					String httpMethod = rom.getHttpMethod();
					RestRequest rc = request(httpMethod, uri, hasContent(httpMethod));

					rc.serializer(serializer);
					rc.parser(parser);

					rm.getHeaders().forEach(x -> rc.header(x));

					for (RemoteOperationArg a : rom.getPathArgs())
						rc.pathArg(a.getName(), args[a.getIndex()], a.getSchema(), a.getSerializer().orElse(partSerializer));

					for (RemoteOperationArg a : rom.getQueryArgs())
						rc.queryArg(a.getName(), args[a.getIndex()], a.getSchema(), a.getSerializer().orElse(partSerializer), a.isSkipIfEmpty());

					for (RemoteOperationArg a : rom.getFormDataArgs())
						rc.formDataArg(a.getName(), args[a.getIndex()], a.getSchema(), a.getSerializer().orElse(partSerializer), a.isSkipIfEmpty());

					for (RemoteOperationArg a : rom.getHeaderArgs())
						rc.headerArg(a.getName(), args[a.getIndex()], a.getSchema(), a.getSerializer().orElse(partSerializer), a.isSkipIfEmpty());

					RemoteOperationArg ba = rom.getBodyArg();
					if (ba != null)
						rc.body(args[ba.getIndex()], ba.getSchema());

					if (rom.getRequestArgs().length > 0) {
						for (RemoteOperationBeanArg rmba : rom.getRequestArgs()) {
							RequestBeanMeta rbm = rmba.getMeta();
							Object bean = args[rmba.getIndex()];
							if (bean != null) {
								for (RequestBeanPropertyMeta p : rbm.getProperties()) {
									Object val = p.getGetter().invoke(bean);
									HttpPartType pt = p.getPartType();
									String pn = p.getPartName();
									HttpPartSchema schema = p.getSchema();
									if (pt == PATH)
										rc.pathArg(pn, val, schema, p.getSerializer().orElse(partSerializer));
									else if (val != null) {
										if (pt == QUERY)
											rc.queryArg(pn, val, schema, p.getSerializer().orElse(partSerializer), schema.isSkipIfEmpty());
										else if (pt == FORMDATA)
											rc.formDataArg(pn, val, schema, p.getSerializer().orElse(partSerializer), schema.isSkipIfEmpty());
										else if (pt == HEADER)
											rc.headerArg(pn, val, schema, p.getSerializer().orElse(partSerializer), schema.isSkipIfEmpty());
										else /* (pt == HttpPartType.BODY) */
											rc.body(val, schema);
									}
								}
							}
						}
					}

					RemoteOperationReturn ror = rom.getReturns();
					if (ror.isFuture()) {
						return getExecutorService().submit(new Callable<Object>() {
							@Override
							public Object call() throws Exception {
								try {
									return executeRemote(interfaceClass, rc, method, rom);
								} catch (Exception e) {
									throw e;
								} catch (Throwable e) {
									throw runtimeException(e);
								}
							}
						});
					} else if (ror.isCompletableFuture()) {
						CompletableFuture<Object> cf = new CompletableFuture<>();
						getExecutorService().submit(new Callable<Object>() {
							@Override
							public Object call() throws Exception {
								try {
									cf.complete(executeRemote(interfaceClass, rc, method, rom));
								} catch (Throwable e) {
									cf.completeExceptionally(e);
								}
								return null;
							}
						});
						return cf;
					}

					return executeRemote(interfaceClass, rc, method, rom);
				}
		});
	}

	Object executeRemote(Class<?> interfaceClass, RestRequest rc, Method method, RemoteOperationMeta rom) throws Throwable {
		RemoteOperationReturn ror = rom.getReturns();

		try {
			Object ret = null;
			RestResponse res = null;
			rc.rethrow(RuntimeException.class).rethrow(rom.getExceptions());
			if (ror.getReturnValue() == RemoteReturn.NONE) {
				res = rc.complete();
			} else if (ror.getReturnValue() == RemoteReturn.STATUS) {
				res = rc.complete();
				int returnCode = res.getStatusCode();
				Class<?> rt = method.getReturnType();
				if (rt == Integer.class || rt == int.class)
					ret = returnCode;
				else if (rt == Boolean.class || rt == boolean.class)
					ret = returnCode < 400;
				else
					throw new RestCallException(res, null, "Invalid return type on method annotated with @RemoteOp(returns=RemoteReturn.STATUS).  Only integer and booleans types are valid.");
			} else if (ror.getReturnValue() == RemoteReturn.BEAN) {
				rc.ignoreErrors();
				res = rc.run();
				ret = res.as(ror.getResponseBeanMeta());
			} else {
				Class<?> rt = method.getReturnType();
				if (Throwable.class.isAssignableFrom(rt))
					rc.ignoreErrors();
				res = rc.run();
				Object v = res.getBody().asType(ror.getReturnType());
				if (v == null && rt.isPrimitive())
					v = ClassInfo.of(rt).getPrimitiveDefault();
				ret = v;
			}
			return ret;
		} catch (RestCallException e) {
			Throwable t = e.getCause();
			if (t instanceof RuntimeException)
				throw t;
			for (Class<?> t2 : method.getExceptionTypes())
				if (t2.isInstance(t))
					throw t;
			throw runtimeException(e);
		}
	}

	/**
	 * Create a new proxy interface against an RRPC-style service.
	 *
	 * <p>
	 * Remote interfaces are interfaces exposed on the server side using either the <c>RrpcServlet</c>
	 * or <c>RRPC</c> REST methods.
	 *
	 * <p>
	 * The URI to the REST interface is based on the following values:
	 * <ul>
	 * 	<li>The {@link Remote#path() @Remote(path)} annotation on the interface (<c>remote-path</c>).
	 * 	<li>The {@link RestClientBuilder#rootUri(Object) rootUri} on the client (<c>root-url</c>).
	 * 	<li>The fully-qualified class name of the interface (<c>class-name</c>).
	 * </ul>
	 *
	 * <p>
	 * The URI calculation is as follows:
	 * <ul>
	 * 	<li><c>remote-path</c> - If remote path is absolute.
	 * 	<li><c>root-url/remote-path</c> - If remote path is relative and root-url has been specified.
	 * 	<li><c>root-url/class-name</c> - If remote path is not specified.
	 * </ul>
	 *
	 * <p>
	 * If the information is not available to resolve to an absolute URI, a {@link RemoteMetadataException} is thrown.
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		If you plan on using your proxy in a multi-threaded environment, you'll want to use an underlying
	 * 		pooling client connection manager.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc RestRpc}
	 * </ul>
	 *
	 * @param interfaceClass The interface to create a proxy for.
	 * @return The new proxy interface.
	 * @throws RemoteMetadataException If the REST URI cannot be determined based on the information given.
	 */
	public <T> T getRrpcInterface(final Class<T> interfaceClass) {
		return getRrpcInterface(interfaceClass, null);
	}

	/**
	 * Same as {@link #getRrpcInterface(Class)} except explicitly specifies the URI of the REST interface.
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc RestRpc}
	 * </ul>
	 *
	 * @param interfaceClass The interface to create a proxy for.
	 * @param uri The URI of the REST interface.
	 * @return The new proxy interface.
	 */
	public <T> T getRrpcInterface(final Class<T> interfaceClass, final Object uri) {
		return getRrpcInterface(interfaceClass, uri, null, null);
	}

	/**
	 * Same as {@link #getRrpcInterface(Class, Object)} but allows you to override the serializer and parser used.
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc RestRpc}
	 * </ul>
	 *
	 * @param interfaceClass The interface to create a proxy for.
	 * @param uri The URI of the REST interface.
	 * @param serializer The serializer used to serialize POJOs to the body of the HTTP request.
	 * @param parser The parser used to parse POJOs from the body of the HTTP response.
	 * @return The new proxy interface.
	 */
	@SuppressWarnings({ "unchecked" })
	public <T> T getRrpcInterface(final Class<T> interfaceClass, Object uri, final Serializer serializer, final Parser parser) {

		if (uri == null) {
			RrpcInterfaceMeta rm = new RrpcInterfaceMeta(interfaceClass, "");
			String path = rm.getPath();
			if (path.indexOf("://") == -1) {
				if (isEmpty(rootUri))
					throw new RemoteMetadataException(interfaceClass, "Root URI has not been specified.  Cannot construct absolute path to remote interface.");
				path = trimSlashes(rootUri) + '/' + path;
			}
			uri = path;
		}

		final String restUrl2 = stringify(uri);

		return (T)Proxy.newProxyInstance(
			interfaceClass.getClassLoader(),
			new Class[] { interfaceClass },
			new InvocationHandler() {

				final RrpcInterfaceMeta rm = new RrpcInterfaceMeta(interfaceClass, restUrl2);

				@Override /* InvocationHandler */
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					RrpcInterfaceMethodMeta rim = rm.getMethodMeta(method);

					String uri = rim.getUri();
					RestResponse res = null;

					try {
						RestRequest rc = request("POST", uri, true)
							.serializer(serializer)
							.body(args)
							.rethrow(RuntimeException.class)
							.rethrow(method.getExceptionTypes());

						res = rc.run();

						Object v = res.getBody().asType(method.getGenericReturnType());
						if (v == null && method.getReturnType().isPrimitive())
							v = ClassInfo.of(method.getReturnType()).getPrimitiveDefault();
						return v;

					} catch (Throwable e) {
						if (e instanceof RestCallException) {
							Throwable t = e.getCause();
							if (t != null)
								e = t;
						}
						if (e instanceof RuntimeException)
							throw e;
						for (Class<?> t2 : method.getExceptionTypes())
							if (t2.isInstance(e))
								throw e;
						throw runtimeException(e);
					}
				}
		});
	}

	@Override
	protected void finalize() throws Throwable {
		if (detectLeaks && ! isClosed && ! keepHttpClientOpen) {
			StringBuilder sb = new StringBuilder("WARNING:  RestClient garbage collected before it was finalized.");  // NOT DEBUG
			if (creationStack != null) {
				sb.append("\nCreation Stack:");  // NOT DEBUG
				for (StackTraceElement e : creationStack)
					sb.append("\n\t" + e);  // NOT DEBUG
			}
			log(WARNING, sb.toString());
		}
	}

	/**
	 * Logs a message.
	 *
	 * @param level The log level.
	 * @param t Thrown exception.  Can be <jk>null</jk>.
	 * @param msg The message.
	 * @param args Optional message arguments.
	 */
	protected void log(Level level, Throwable t, String msg, Object...args) {
		logger.log(level, t, msg(msg, args));
		if (logToConsole) {
			console.println(msg(msg, args).get());
			if (t != null)
				t.printStackTrace(console);
		}
	}

	/**
	 * Logs a message.
	 *
	 * @param level The log level.
	 * @param msg The message with {@link MessageFormat}-style arguments.
	 * @param args The arguments.
	 */
	protected void log(Level level, String msg, Object...args) {
		logger.log(level, msg(msg, args));
		if (logToConsole)
			console.println(msg(msg, args).get());
	}

	private Supplier<String> msg(String msg, Object...args) {
		return ()->args.length == 0 ? msg : MessageFormat.format(msg, args);
	}

	/**
	 * Returns the part serializer associated with this client.
	 *
	 * @return The part serializer associated with this client.
	 */
	protected HttpPartSerializer getPartSerializer() {
		return partSerializer;
	}

	/**
	 * Returns the part parser associated with this client.
	 *
	 * @return The part parser associated with this client.
	 */
	protected HttpPartParser getPartParser() {
		return partParser;
	}

	/**
	 * Returns the part serializer instance of the specified type.
	 *
	 * @param c The part serializer class.
	 * @return The part serializer.
	 */
	protected HttpPartSerializer getPartSerializer(Class<? extends HttpPartSerializer> c) {
		HttpPartSerializer x = partSerializers.get(c);
		if (x == null) {
			try {
				x = beanStore.creator(c).run();
			} catch (ExecutableException e) {
				throw new RuntimeException(e);
			}
			partSerializers.put(c, x);
		}
		return x;
	}

	/**
	 * Returns the part parser instance of the specified type.
	 *
	 * @param c The part parser class.
	 * @return The part parser.
	 */
	protected HttpPartParser getPartParser(Class<? extends HttpPartParser> c) {
		HttpPartParser x = partParsers.get(c);
		if (x == null) {
			try {
				x = beanStore.creator(c).run();
			} catch (ExecutableException e) {
				throw new RuntimeException(e);
			}
			partParsers.put(c, x);
		}
		return x;
	}

	/**
	 * Returns <jk>true</jk> if empty request header values should be ignored.
	 *
	 * @return <jk>true</jk> if empty request header values should be ignored.
	 */
	protected boolean isSkipEmptyHeaderData() {
		return skipEmptyHeaderData;
	}

	/**
	 * Returns <jk>true</jk> if empty request query parameter values should be ignored.
	 *
	 * @return <jk>true</jk> if empty request query parameter values should be ignored.
	 */
	protected boolean isSkipEmptyQueryData() {
		return skipEmptyQueryData;
	}

	/**
	 * Returns <jk>true</jk> if empty request form-data parameter values should be ignored.
	 *
	 * @return <jk>true</jk> if empty request form-data parameter values should be ignored.
	 */
	protected boolean isSkipEmptyFormData() {
		return skipEmptyFormData;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Part list builders methods
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a mutable copy of the header data defined on this client.
	 *
	 * <p>
	 * Used during the construction of {@link RestRequest} objects.
	 *
	 * <p>
	 * Subclasses can override this method to provide their own builder.
	 *
	 * @return A new builder.
	 */
	public HeaderList.Builder createHeaderDataBuilder() {
		return headerData.copy();
	}

	/**
	 * Creates a mutable copy of the query data defined on this client.
	 *
	 * <p>
	 * Used during the construction of {@link RestRequest} objects.
	 *
	 * <p>
	 * Subclasses can override this method to provide their own builder.
	 *
	 * @return A new builder.
	 */
	public PartList.Builder createQueryDataBuilder() {
		return queryData.copy();
	}

	/**
	 * Creates a mutable copy of the form data defined on this client.
	 *
	 * <p>
	 * Used during the construction of {@link RestRequest} objects.
	 *
	 * <p>
	 * Subclasses can override this method to provide their own builder.
	 *
	 * @return A new builder.
	 */
	public PartList.Builder createFormDataBuilder() {
		return formData.copy();
	}

	/**
	 * Creates a mutable copy of the path data defined on this client.
	 *
	 * <p>
	 * Used during the construction of {@link RestRequest} objects.
	 *
	 * <p>
	 * Subclasses can override this method to provide their own builder.
	 *
	 * @return A new builder.
	 */
	public PartList.Builder createPathDataBuilder() {
		return pathData.copy();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// RestCallInterceptor methods
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Interceptor method called immediately after the RestRequest object is created and all headers/query/form-data has been copied from the client.
	 *
	 * <p>
	 * Subclasses can override this method to intercept the request and perform special modifications.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link RestClientBuilder#interceptors(Object...)}
	 * </ul>
	 *
	 * @param req The HTTP request.
	 * @throws RestCallException If any of the interceptors threw an exception.
	 */
	@Override
	public void onInit(RestRequest req) throws RestCallException {
		try {
			for (RestCallInterceptor rci : interceptors)
				rci.onInit(req);
		} catch (RuntimeException | RestCallException e) {
			throw e;
		} catch (Exception e) {
			throw new RestCallException(null, e, "Interceptor threw an exception on init.");
		}
	}

	/**
	 * Interceptor method called immediately after an HTTP response has been received.
	 *
	 * <p>
	 * Subclasses can override this method to intercept the response and perform special modifications.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link RestClientBuilder#interceptors(Object...)}
	 * </ul>
	 *
	 * @param req The HTTP request.
	 * @param res The HTTP response.
	 * @throws RestCallException If any of the interceptors threw an exception.
	 */
	@Override
	public void onConnect(RestRequest req, RestResponse res) throws RestCallException {
		try {
			for (RestCallInterceptor rci : interceptors)
				rci.onConnect(req, res);
		} catch (RuntimeException | RestCallException e) {
			throw e;
		} catch (Exception e) {
			throw new RestCallException(res, e, "Interceptor threw an exception on connect.");
		}
	}

	/**
	 * Interceptor method called immediately after the RestRequest object is created and all headers/query/form-data has been set on the request from the client.
	 *
	 * <p>
	 * Subclasses can override this method to handle any cleanup operations.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link RestClientBuilder#interceptors(Object...)}
	 * </ul>
	 *
	 * @param req The HTTP request.
	 * @param res The HTTP response.
	 * @throws RestCallException If any of the interceptors threw an exception.
	 */
	@Override
	public void onClose(RestRequest req, RestResponse res) throws RestCallException {
		try {
			for (RestCallInterceptor rci : interceptors)
				rci.onClose(req, res);
		} catch (RuntimeException | RestCallException e) {
			throw e;
		} catch (Exception e) {
			throw new RestCallException(res, e, "Interceptor threw an exception on close.");
		}
	}

	//------------------------------------------------------------------------------------------------
	// Passthrough methods for HttpClient.
	//------------------------------------------------------------------------------------------------

	/**
	 * Obtains the parameters for this client.
	 *
	 * These parameters will become defaults for all requests being executed with this client, and for the parameters of dependent objects in this client.
	 *
	 * @return The default parameters.
	 * @deprecated Use {@link RequestConfig}.
	 */
	@Deprecated
	@Override /* HttpClient */
	public HttpParams getParams() {
		return httpClient.getParams();
	}

	/**
	 * Obtains the connection manager used by this client.
	 *
	 * @return The connection manager.
	 * @deprecated Use {@link HttpClientBuilder}.
	 */
	@Deprecated
	@Override /* HttpClient */
	public ClientConnectionManager getConnectionManager() {
		return httpClient.getConnectionManager();
	}

	/**
	 * Returns the connection manager if one was specified in the client builder.
	 *
	 * @return The connection manager.  May be <jk>null</jk>.
	 */
	public HttpClientConnectionManager getHttpClientConnectionManager() {
		return connectionManager;
	}

	/**
	 * Executes HTTP request using the default context.
	 *
	 * <ul class='notes'>
	 * 	<li>This method gets passed on directly to the underlying {@link HttpClient} class.
	 * </ul>
	 *
	 * @param request The request to execute.
	 * @return
	 * 	The response to the request.
	 * 	<br>This is always a final response, never an intermediate response with an 1xx status code.
	 * 	<br>Whether redirects or authentication challenges will be returned or handled automatically depends on the
	 * 		implementation and configuration of this client.
	 * @throws IOException In case of a problem or the connection was aborted.
	 * @throws ClientProtocolException In case of an http protocol error.
	 */
	@Override /* HttpClient */
	public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
		return httpClient.execute(request);
	}

	/**
	 * Executes HTTP request using the given context.
	 *
	 * <ul class='notes'>
	 * 	<li>This method gets passed on directly to the underlying {@link HttpClient} class.
	 * </ul>
	 *
	 * @param request The request to execute.
	 * @param context The context to use for the execution, or <jk>null</jk> to use the default context.
	 * @return
	 * 	The response to the request.
	 * 	<br>This is always a final response, never an intermediate response with an 1xx status code.
	 * 	<br>Whether redirects or authentication challenges will be returned or handled automatically depends on the
	 * 		implementation and configuration of this client.
	 * @throws IOException In case of a problem or the connection was aborted.
	 * @throws ClientProtocolException In case of an http protocol error.
	 */
	@Override /* HttpClient */
	public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
		return httpClient.execute(request, context);
	}

	/**
	 * Executes HTTP request using the default context.
	 *
	 * <ul class='notes'>
	 * 	<li>This method gets passed on directly to the underlying {@link HttpClient} class.
	 * </ul>
	 *
	 * @param target The target host for the request.
	 * 	<br>Implementations may accept <jk>null</jk> if they can still determine a route, for example to a default
	 * 		target or by inspecting the request.
	 * @param request The request to execute.
	 * @return The response to the request.
	 * 	<br>This is always a final response, never an intermediate response with an 1xx status code.
	 * 	<br>Whether redirects or authentication challenges will be returned or handled automatically depends on the
	 * 		implementation and configuration of this client.
	 * @throws IOException In case of a problem or the connection was aborted.
	 * @throws ClientProtocolException In case of an http protocol error.
	 */
	@Override /* HttpClient */
	public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
		return httpClient.execute(target, request);
	}

	/**
	 * Executes HTTP request using the given context.
	 *
	 * <ul class='notes'>
	 * 	<li>This method gets passed on directly to the underlying {@link HttpClient} class.
	 * 	<li>The {@link #run(HttpHost,HttpRequest,HttpContext)} method has been provided as a wrapper around this method.
	 * 		Subclasses can override these methods for handling requests with and without bodies separately.
	 * 	<li>The {@link RestCallHandler} interface can also be implemented to intercept this method.
	 * </ul>
	 *
	 * @param target The target host for the request.
	 * 	<br>Implementations may accept <jk>null</jk> if they can still determine a route, for example to a default
	 * 		target or by inspecting the request.
	 * @param request The request to execute.
	 * @param context The context to use for the execution, or <jk>null</jk> to use the default context.
	 * @return
	 * 	The response to the request.
	 * 	<br>This is always a final response, never an intermediate response with an 1xx status code.
	 * 	<br>Whether redirects or authentication challenges will be returned or handled automatically depends on the
	 * 		implementation and configuration of this client.
	 * @throws IOException In case of a problem or the connection was aborted.
	 * @throws ClientProtocolException In case of an http protocol error.
	 */
	@Override /* HttpClient */
	public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
		return httpClient.execute(target, request, context);
	}

	/**
	 * Executes HTTP request using the default context and processes the response using the given response handler.
	 *
 	 * <p>
	 * The content entity associated with the response is fully consumed and the underlying connection is released back
	 * to the connection manager automatically in all cases relieving individual {@link ResponseHandler ResponseHandlers}
	 * from having to manage resource deallocation internally.
	 *
	 * <ul class='notes'>
	 * 	<li>This method gets passed on directly to the underlying {@link HttpClient} class.
	 * </ul>
	 *
	 * @param request The request to execute.
	 * @param responseHandler The response handler.
	 * @return Object returned by response handler.
	 * @throws IOException In case of a problem or the connection was aborted.
	 * @throws ClientProtocolException In case of an http protocol error.
	 */
	@Override /* HttpClient */
	public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
		return httpClient.execute(request, responseHandler);
	}

	/**
	 * Executes HTTP request using the given context and processes the response using the given response handler.
	 *
	 * <p>
	 * The content entity associated with the response is fully consumed and the underlying connection is released back
	 * to the connection manager automatically in all cases relieving individual {@link ResponseHandler ResponseHandlers}
	 * from having to manage resource deallocation internally.
	 *
	 * <ul class='notes'>
	 * 	<li>This method gets passed on directly to the underlying {@link HttpClient} class.
	 * </ul>
	 *
	 * @param request The request to execute.
	 * @param responseHandler The response handler.
	 * @param context The context to use for the execution, or <jk>null</jk> to use the default context.
	 * @return The response object as generated by the response handler.
	 * @throws IOException In case of a problem or the connection was aborted.
	 * @throws ClientProtocolException In case of an http protocol error.
	 */
	@Override /* HttpClient */
	public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
		return httpClient.execute(request, responseHandler, context);
	}

	/**
	 * Executes HTTP request to the target using the default context and processes the response using the given response handler.
	 *
	 * <p>
	 * The content entity associated with the response is fully consumed and the underlying connection is released back
	 * to the connection manager automatically in all cases relieving individual {@link ResponseHandler ResponseHandlers}
	 * from having to manage resource deallocation internally.
	 *
	 * <ul class='notes'>
	 * 	<li>This method gets passed on directly to the underlying {@link HttpClient} class.
	 * </ul>
	 *
	 * @param target
	 * 	The target host for the request.
	 * 	<br>Implementations may accept <jk>null</jk> if they can still determine a route, for example to a default target or by inspecting the request.
	 * @param request The request to execute.
	 * @param responseHandler The response handler.
	 * @return The response object as generated by the response handler.
	 * @throws IOException In case of a problem or the connection was aborted.
	 * @throws ClientProtocolException In case of an http protocol error.
	 */
	@Override /* HttpClient */
	public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
		return httpClient.execute(target, request, responseHandler);
	}

	/**
	 * Executes a request using the default context and processes the response using the given response handler.
	 *
	 * <p>
	 * The content entity associated with the response is fully consumed and the underlying connection is released back
	 * to the connection manager automatically in all cases relieving individual {@link ResponseHandler ResponseHandlers}
	 * from having to manage resource deallocation internally.
	 *
	 * <ul class='notes'>
	 * 	<li>This method gets passed on directly to the underlying {@link HttpClient} class.
	 * </ul>
	 *
	 * @param target
	 * 	The target host for the request.
	 * 	<br>Implementations may accept <jk>null</jk> if they can still determine a route, for example to a default target or by inspecting the request.
	 * @param request The request to execute.
	 * @param responseHandler The response handler.
	 * @param context The context to use for the execution, or <jk>null</jk> to use the default context.
	 * @return The response object as generated by the response handler.
	 * @throws IOException In case of a problem or the connection was aborted.
	 * @throws ClientProtocolException In case of an http protocol error.
	 */
	@Override /* HttpClient */
	public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
		return httpClient.execute(target, request, responseHandler, context);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Other methods
	//-----------------------------------------------------------------------------------------------------------------

	private Pattern absUrlPattern = Pattern.compile("^\\w+\\:\\/\\/.*");

	URI toURI(Object x, String rootUri) throws RestCallException {
		try {
			if (x instanceof URI)
				return (URI)x;
			if (x instanceof URL)
				((URL)x).toURI();
			if (x instanceof URIBuilder)
				return ((URIBuilder)x).build();
			String s = x == null ? "" : x.toString();
			if (rootUri != null && ! absUrlPattern.matcher(s).matches()) {
				if (s.isEmpty())
					s = rootUri;
				else {
					StringBuilder sb = new StringBuilder(rootUri);
					if (! s.startsWith("/"))
						sb.append('/');
					sb.append(s);
					s = sb.toString();
				}
			}
			s = fixUrl(s);
			return new URI(s);
		} catch (URISyntaxException e) {
			throw new RestCallException(null, e, "Invalid URI encountered:  {0}", x);  // Shouldn't happen.
		}
	}

	ExecutorService getExecutorService() {
		if (executorService != null)
			return executorService;
		synchronized(this) {
			executorService = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
			return executorService;
		}
	}

	/*
	 * Returns the serializer that best matches the specified content type.
	 * If no match found or the content type is null, returns the serializer in the list if it's a list of one.
	 * Returns null if no serializers are defined.
	 */
	Serializer getMatchingSerializer(String mediaType) {
		if (serializers.isEmpty())
			return null;
		if (mediaType != null) {
			Serializer s = serializers.getSerializer(mediaType);
			if (s != null)
				return s;
		}
		List<Serializer> l = serializers.getSerializers();
		return (l.size() == 1 ? l.get(0) : null);
	}

	boolean hasSerializers() {
		return ! serializers.getSerializers().isEmpty();
	}

	/*
	 * Returns the parser that best matches the specified content type.
	 * If no match found or the content type is null, returns the parser in the list if it's a list of one.
	 * Returns null if no parsers are defined.
	 */
	Parser getMatchingParser(String mediaType) {
		if (parsers.isEmpty())
			return null;
		if (mediaType != null) {
			Parser p = parsers.getParser(mediaType);
			if (p != null)
				return p;
		}
		List<Parser> l = parsers.getParsers();
		return (l.size() == 1 ? l.get(0) : null);
	}

	boolean hasParsers() {
		return ! parsers.getParsers().isEmpty();
	}

	@SuppressWarnings("unchecked")
	<T extends Context> T getInstance(Class<T> c) {
		Context o = requestContexts.get(c);
		if (o == null) {
			if (Serializer.class.isAssignableFrom(c)) {
				o = Serializer.createSerializerBuilder((Class<? extends Serializer>)c).apply(getContextProperties()).build();
			} else if (Parser.class.isAssignableFrom(c)) {
				o = Parser.createParserBuilder((Class<? extends Parser>)c).apply(getContextProperties()).build();
			} else {
				o = ContextCache.INSTANCE.create(c, getContextProperties());
			}
			requestContexts.put(c, o);
		}
		return (T)o;
	}

	private RestOperation op(String method, Object url, Object body) {
		return RestOperation.of(method, url, body);
	}

	private Reader stringBody(String body) {
		return body == null ? null : new StringReader(stringify(body));
	}

	@Override /* Context */
	public OMap toMap() {
		return super.toMap()
			.a(
				"RestClient",
				OMap
					.create()
					.filtered()
					.a("errorCodes", errorCodes)
					.a("executorService", executorService)
					.a("executorServiceShutdownOnClose", executorServiceShutdownOnClose)
					.a("headerData", headerData)
					.a("interceptors", interceptors)
					.a("keepHttpClientOpen", keepHttpClientOpen)
					.a("partParser", partParser)
					.a("partSerializer", partSerializer)
					.a("queryData", queryData)
					.a("rootUri", rootUri)
			);
	}
}
