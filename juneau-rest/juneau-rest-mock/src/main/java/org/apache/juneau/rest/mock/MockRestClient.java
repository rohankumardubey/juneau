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
package org.apache.juneau.rest.mock;

import static org.apache.juneau.internal.ExceptionUtils.*;
import static org.apache.juneau.internal.StringUtils.*;
import static org.apache.juneau.rest.util.RestUtils.*;
import static org.apache.juneau.Enablement.*;
import static java.util.Collections.*;
import static java.util.Optional.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.*;

import javax.servlet.http.*;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.conn.*;
import org.apache.http.entity.*;
import org.apache.http.message.*;
import org.apache.juneau.*;
import org.apache.juneau.http.remote.*;
import org.apache.juneau.parser.*;
import org.apache.juneau.rest.*;
import org.apache.juneau.rest.annotation.*;
import org.apache.juneau.rest.client.*;
import org.apache.juneau.rest.client.RestRequest;
import org.apache.juneau.http.header.ContentType;
import org.apache.juneau.rest.logging.*;

/**
 * Mocked {@link RestClient}.
 *
 * <p>
 * 	This class is used for performing serverless unit testing of {@link Rest @Rest}-annotated and {@link Remote @Remote}-annotated classes.
 *
 * <p>
 * 	The class itself extends from {@link RestClient} providing it with the rich feature set of that API and combines
 * 	it with the Apache HttpClient {@link HttpClientConnection} interface for processing requests.
 *  The class converts {@link HttpRequest} objects to instances of {@link MockServletRequest} and {@link MockServletResponse} which are passed directly
 *  to the call handler on the resource class {@link RestContext#execute(Object,HttpServletRequest,HttpServletResponse)}.
 *  In effect, you're fully testing your REST API as if it were running in a live servlet container, yet not
 *  actually having to run in a servlet container.
 *  All aspects of the client and server side code are tested, yet no servlet container is required.  The actual
 *  over-the-wire transmission is the only aspect being bypassed.
 *
 * <p>
 * The following shows a simple example of invoking a PUT method on a simple REST interface and asserting the correct status code and response body:
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jk>public class</jk> MockTest {
 *
 * 		<jc>// A simple bean with one field.</jc>
 * 		<jk>public static class</jk> MyBean {
 * 			<jk>public int</jk> <jf>foo</jf> = 1;
 * 		}
 *
 * 		<jc>// Our REST resource to test.</jc>
 * 		<jc>// Simply echos the response.</jc>
 * 		<ja>@Rest</ja>(
 * 			serializers=SimpleJsonSerializer.<jk>class</jk>,
 * 			parsers=JsonParser.<jk>class</jk>
 * 		)
 * 		<jk>public static class</jk> EchoRest {
 *
 * 			<ja>@RestPut</ja>(
 * 				path=<js>"/echo"</js>
 * 			)
 * 			<jk>public</jk> MyBean echo(<ja>@Body</ja> MyBean <jv>bean</jv>) {
 * 				<jk>return</jk> <jv>bean</jv>;
 * 			}
 * 		}
 *
 * 		<jc>// Our JUnit test.</jc>
 * 		<ja>@Test</ja>
 * 		<jk>public void</jk> testEcho() <jk>throws</jk> Exception {
 *
 * 			MyBean <jv>myBean</jv> = <jk>new</jk> MyBean();
 *
 * 			<jc>// Do a round-trip on the bean through the REST interface</jc>
 * 			<jv>myBean</jv> = MockRestClient
 * 				.<jsm>create</jsm>(EchoRest.<jk>class</jk>)
 * 				.simpleJson()
 * 				.build()
 * 				.put(<js>"/echo"</js>, <jv>myBean</jv>)
 * 				.run()
 * 				.assertStatus().is(200)
 * 				.assertBody().is(<js>"{foo:1}"</js>)
 * 				.getBody().as(MyBean.<jk>class</jk>);
 *
 * 			<jsm>assertEquals</jsm>(1, <jv>myBean</jv>.<jf>foo</jf>);
 * 		}
 * 	}
 * </p>
 * <p>
 * 	Breaking apart the fluent method call above will help you understand how this works.
 *
 * <p class='bcode w800'>
 * 	<ja>@Test</ja>
 * 	<jk>public void</jk> testEcho() <jk>throws</jk> Exception {
 *
 * 		<jc>// Instantiate our mock client.</jc>
 * 		MockRestClient <jv>client</jv> = MockRestClient
 * 			.<jsm>create</jsm>(EchoRest.<jk>class</jk>)
 * 			.simpleJson()
 * 			.build();
 *
 * 		<jc>// Create a request.</jc>
 * 		RestRequest <jv>req</jv> = <jv>client</jv>.put(<js>"/echo"</js>, <jv>myBean</jv>);
 *
 * 		<jc>// Execute it (by calling RestCallHandler.service(...) and then returning the response object).</jc>
 * 		RestResponse <jv>res</jv> = <jv>req</jv>.run();
 *
 * 		<jc>// Run assertion tests on the results.</jc>
 * 		<jv>res</jv>.assertStatus().is(200);
 * 		<jv>res</jv>.assertBody().is(<js>"'foo'"</js>);
 *
 * 		<jc>// Convert the body of the response to a bean.</jc>
 * 		<jv>myBean</jv> = <jv>res</jv>.getBody().as(MyBean.<jk>class</jk>);
 * 	}
 * </p>
 *
 * <p>
 * 	The <c>create(Object)</c> method can take in either <c>Class</c> objects or pre-instantiated beans.
 * 	The latter is particularly useful for testing Spring beans.
 *
 * <p>
 * 	The {@link MockRestRequest} object has convenience methods provided to allow you to set any properties
 * 	directly on the underlying {@link HttpServletRequest} object.  The following example shows how
 * 	this can be used to directly set roles on the request object to perform security testing.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	<ja>@Rest</ja>(roleGuard=<js>"ADMIN"</js>)
 * 	<jk>public class</jk> A {
 * 		<ja>@RestGet</ja>
 * 		<jk>public</jk> String get() {
 * 			<jk>return</jk> <js>"OK"</js>;
 * 		}
 * 	}
 *
 * 	<ja>@Test</ja>
 * 	<jk>public void</jk> mytest() <jk>throws</jk> Exception {
 * 		MockRestClient <jv>client</jv> = MockRestClient.<jsm>build</jsm>(A.<jk>class</jk>);
 *
 * 		<jc>// Admin user should get 200, but anyone else should get 403-Unauthorized.</jc>
 * 		<jv>client</jv>.get().roles(<js>"ADMIN"</js>).run().assertStatus().is(200);
 * 		<jv>client</jv>.get().roles(<js>"USER"</js>).run().assertStatus().is(403);
 * 	}
 * </p>
 *
 * <p>
 * 	Debug mode is provided that will cause your HTTP requests and responses to be sent to the console:
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bcode w800'>
 * 	MockRestClient <jv>client</jv> = MockRestClient
 * 		.<jsm>create</jsm>(MyRest.<jk>class</jk>)
 * 		.debug()
 * 		.simpleJson()
 * 		.build();
 * </p>
 *
 * <p>
 * 	The class can also be used for testing of {@link Remote @Remote}-annotated interfaces against {@link Rest @Rest}-annotated resources.
 *
 * <h5 class='figure'>Example:</h5>
 * <p class='bpcode w800'>
 * 	<jc>// Our remote resource to test.</jc>
 * 	<ja>@Remote</ja>
 * 	<jk>public interface</jk> MyRemoteInterface {
 *
 * 		<ja>@RemoteGet</ja>(<js>"/echoQuery"</js>)
 * 		<jk>public int</jk> echoQuery(<ja>@Query</ja>(name=<js>"id"</js>) <jk>int</jk> <jv>id</jv>);
 * 	}
 *
 * 	<jc>// Our mocked-up REST interface to test against.</jc>
 * 	<ja>@Rest</ja>
 * 	<jk>public class</jk> MyRest {
 *
 * 		<ja>@RestGet</ja>(path=<js>"/echoQuery"</js>)
 * 		<jk>public int</jk> echoQuery(<ja>@Query</ja>(<js>"id"</js>) String <jv>id</jv>) {
 * 			<jk>return</jk> <jv>id</jv>;
 * 		}
 * 	}
 *
 * 	<ja>@Test</ja>
 * 	<jk>public void</jk> testProxy() {
 * 		MyRemoteInterface <jv>client</jv> = MockRestClient
 * 			.create(MyRest.<jk>class</jk>)
 * 			.json()
 * 			.build()
 * 			.getRemote(MyRemoteInterface.<jk>class</jk>);
 *
 * 		<jsm>assertEquals</jsm>(123, <jv>client</jv>.echoQuery(123));
 * 	}
 * </p>
 *
 * <ul class='seealso'>
 * 	<li class='link'>{@doc juneau-rest-mock}
 * </ul>
 */
public class MockRestClient extends RestClient implements HttpClientConnection {

	//-------------------------------------------------------------------------------------------------------------------
	// Static
	//-------------------------------------------------------------------------------------------------------------------

	private static Map<Class<?>,RestContext> REST_CONTEXTS = new ConcurrentHashMap<>();

	/**
	 * Creates a new {@link RestClientBuilder} configured with the specified REST implementation bean or bean class.
	 *
	 * @param impl
	 * 	The REST bean or bean class annotated with {@link Rest @Rest}.
	 * 	<br>If a class, it must have a no-arg constructor.
	 * @return A new builder.
	 */
	public static MockRestClientBuilder create(Object impl) {
		return new MockRestClientBuilder().restBean(impl);
	}

	/**
	 * Creates a new {@link RestClientBuilder} configured with the specified REST implementation bean or bean class.
	 *
	 * <p>
	 * Same as {@link #create(Object)} but HTTP 400+ codes don't trigger {@link RestCallException RestCallExceptions}.
	 *
	 * @param impl
	 * 	The REST bean or bean class annotated with {@link Rest @Rest}.
	 * 	<br>If a class, it must have a no-arg constructor.
	 * @return A new builder.
	 */
	public static MockRestClientBuilder createLax(Object impl) {
		return new MockRestClientBuilder().restBean(impl).ignoreErrors().noTrace();
	}

	/**
	 * Creates a new {@link RestClient} with no registered serializer or parser.
	 *
	 * <p>
	 * Equivalent to calling:
	 * <p class='bcode w800'>
	 * 	MockRestClient.<jsm>create</jsm>(<jv>impl</jv>).build();
	 * </p>
	 *
	 * @param impl
	 * 	The REST bean or bean class annotated with {@link Rest @Rest}.
	 * 	<br>If a class, it must have a no-arg constructor.
	 * @return A new builder.
	 */
	public static MockRestClient build(Object impl) {
		return create(impl).build();
	}

	/**
	 * Creates a new {@link RestClient} with no registered serializer or parser.
	 *
	 * <p>
	 * Same as {@link #build(Object)} but HTTP 400+ codes don't trigger {@link RestCallException RestCallExceptions}.
	 *
	 * <p>
	 * Equivalent to calling:
	 * <p class='bcode w800'>
	 * 	MockRestClient.<jsm>create</jsm>(<jv>impl</jv>).ignoreErrors().noTrace().build();
	 * </p>
	 *
	 * @param impl
	 * 	The REST bean or bean class annotated with {@link Rest @Rest}.
	 * 	<br>If a class, it must have a no-arg constructor.
	 * @return A new builder.
	 */
	public static MockRestClient buildLax(Object impl) {
		return create(impl).ignoreErrors().noTrace().build();
	}

	/**
	 * Creates a new {@link RestClient} with JSON marshalling support.
	 *
	 * <p>
	 * Equivalent to calling:
	 * <p class='bcode w800'>
	 * 	MockRestClient.<jsm>create</jsm>(<jv>impl</jv>).json().build();
	 * </p>
	 *
	 * @param impl
	 * 	The REST bean or bean class annotated with {@link Rest @Rest}.
	 * 	<br>If a class, it must have a no-arg constructor.
	 * @return A new builder.
	 */
	public static MockRestClient buildJson(Object impl) {
		return create(impl).json().build();
	}

	/**
	 * Creates a new {@link RestClient} with JSON marshalling support.
	 *
	 * <p>
	 * Same as {@link #buildJson(Object)} but HTTP 400+ codes don't trigger {@link RestCallException RestCallExceptions}.
	 *
	 * <p>
	 * Equivalent to calling:
	 * <p class='bcode w800'>
	 * 	MockRestClient.<jsm>create</jsm>(<jv>impl</jv>).json().ignoreErrors().build();
	 * </p>
	 *
	 * @param impl
	 * 	The REST bean or bean class annotated with {@link Rest @Rest}.
	 * 	<br>If a class, it must have a no-arg constructor.
	 * @return A new builder.
	 */
	public static MockRestClient buildJsonLax(Object impl) {
		return create(impl).json().ignoreErrors().noTrace().build();
	}

	/**
	 * Creates a new {@link RestClient} with Simplified-JSON marshalling support.
	 *
	 * <p>
	 * Equivalent to calling:
	 * <p class='bcode w800'>
	 * 	MockRestClient.<jsm>create</jsm>(<jv>impl</jv>).json().build();
	 * </p>
	 *
	 * @param impl
	 * 	The REST bean or bean class annotated with {@link Rest @Rest}.
	 * 	<br>If a class, it must have a no-arg constructor.
	 * @return A new builder.
	 */
	public static MockRestClient buildSimpleJson(Object impl) {
		return create(impl).simpleJson().build();
	}

	/**
	 * Creates a new {@link RestClient} with Simplified-JSON marshalling support.
	 *
	 * <p>
	 * Same as {@link #buildSimpleJson(Object)} but HTTP 400+ codes don't trigger {@link RestCallException RestCallExceptions}.
	 *
	 * <p>
	 * Equivalent to calling:
	 * <p class='bcode w800'>
	 * 	MockRestClient.<jsm>create</jsm>(<jv>impl</jv>).json().ignoreErrors().build();
	 * </p>
	 *
	 * @param impl
	 * 	The REST bean or bean class annotated with {@link Rest @Rest}.
	 * 	<br>If a class, it must have a no-arg constructor.
	 * @return A new builder.
	 */
	public static MockRestClient buildSimpleJsonLax(Object impl) {
		return create(impl).simpleJson().ignoreErrors().noTrace().build();
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Instance
	//-------------------------------------------------------------------------------------------------------------------

	private final RestContext restContext;
	private final Object restObject;
	private final String contextPath, servletPath;
	private final Map<String,String> pathVars;

	private final ThreadLocal<HttpRequest> rreq = new ThreadLocal<>();
	private final ThreadLocal<MockRestResponse> rres = new ThreadLocal<>();
	private final ThreadLocal<MockServletRequest> sreq = new ThreadLocal<>();
	private final ThreadLocal<MockServletResponse> sres = new ThreadLocal<>();

	/**
	 * Constructor.
	 *
	 * @param builder
	 * 	The builder for this object.
	 */
	public MockRestClient(MockRestClientBuilder builder) {
		super(preInit(builder));
		restContext = builder.restContext;
		contextPath = ofNullable(builder.contextPath).orElse("");
		servletPath = ofNullable(builder.servletPath).orElse("");
		pathVars = ofNullable(builder.pathVars).orElse(emptyMap());
		restObject = restContext.getResource();

		HttpClientConnectionManager ccm = getHttpClientConnectionManager();
		if (ccm instanceof MockHttpClientConnectionManager)
			((MockHttpClientConnectionManager)ccm).init(this);
	}

	private static MockRestClientBuilder preInit(MockRestClientBuilder builder) {
		try {
			Object restBean = builder.restBean;
			String contextPath = builder.contextPath;
			String servletPath = builder.servletPath;
			String rootUrl = ofNullable(builder.getRootUri()).orElse("http://localhost");

			Class<?> c = restBean instanceof Class ? (Class<?>)restBean : restBean.getClass();
			if (! REST_CONTEXTS.containsKey(c)) {
				boolean isClass = restBean instanceof Class;
				Object o = isClass ? ((Class<?>)restBean).newInstance() : restBean;
				RestContext rc = RestContext
					.create(o.getClass(), null, null)
					.defaultClasses(BasicTestRestLogger.class)
					.debugDefault(CONDITIONAL)
					.init(()->o)
					.build()
					.postInit()
					.postInitChildFirst();
				REST_CONTEXTS.put(c, rc);
			}
			RestContext restBeanCtx = REST_CONTEXTS.get(c);
			builder.restContext(restBeanCtx);

			if (servletPath == null)
				servletPath = toValidContextPath(restBeanCtx.getFullPath());

			rootUrl = rootUrl + emptyIfNull(contextPath) + emptyIfNull(servletPath);

			builder.servletPath = servletPath;
			builder.rootUri(rootUrl);
			return builder;
		} catch (Exception e) {
			throw new ConfigException(e, "Could not initialize MockRestClient");
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	// Entry point methods.
	//------------------------------------------------------------------------------------------------------------------

	@Override /* RestClient */
	public MockRestRequest request(RestOperation op) throws RestCallException {
		return (MockRestRequest)super.request(op);
	}

	@Override /* RestClient */
	public MockRestRequest get(Object url) throws RestCallException {
		return (MockRestRequest)super.get(url);
	}

	@Override /* RestClient */
	public MockRestRequest get() throws RestCallException {
		return (MockRestRequest)super.get();
	}

	@Override /* RestClient */
	public MockRestRequest put(Object url, Object body) throws RestCallException {
		return (MockRestRequest)super.put(url, body);
	}

	@Override /* RestClient */
	public MockRestRequest put(Object url, String body, ContentType contentType) throws RestCallException {
		return (MockRestRequest)super.put(url, body, contentType);
	}

	@Override /* RestClient */
	public MockRestRequest put(Object url) throws RestCallException {
		return (MockRestRequest)super.put(url);
	}

	@Override /* RestClient */
	public MockRestRequest post(Object url, Object body) throws RestCallException {
		return (MockRestRequest)super.post(url, body);
	}

	@Override /* RestClient */
	public MockRestRequest post(Object url, String body, ContentType contentType) throws RestCallException {
		return (MockRestRequest)super.post(url, body, contentType);
	}

	@Override /* RestClient */
	public MockRestRequest post(Object url) throws RestCallException {
		return (MockRestRequest)super.post(url);
	}

	@Override /* RestClient */
	public MockRestRequest delete(Object url) throws RestCallException {
		return (MockRestRequest)super.delete(url);
	}

	@Override /* RestClient */
	public MockRestRequest options(Object url) throws RestCallException {
		return (MockRestRequest)super.options(url);
	}

	@Override /* RestClient */
	public MockRestRequest head(Object url) throws RestCallException {
		return (MockRestRequest)super.head(url);
	}

	@Override /* RestClient */
	public MockRestRequest formPost(Object url, Object body) throws RestCallException {
		return (MockRestRequest)super.formPost(url, body);
	}

	@Override /* RestClient */
	public MockRestRequest formPost(Object url) throws RestCallException {
		return (MockRestRequest)super.formPost(url);
	}

	@Override /* RestClient */
	public MockRestRequest formPostPairs(Object url, Object...parameters) throws RestCallException {
		return (MockRestRequest)super.formPostPairs(url, parameters);
	}

	@Override /* RestClient */
	public MockRestRequest patch(Object url, Object body) throws RestCallException {
		return (MockRestRequest)super.patch(url, body);
	}

	@Override /* RestClient */
	public MockRestRequest patch(Object url, String body, ContentType contentType) throws RestCallException {
		return (MockRestRequest)super.patch(url, body, contentType);
	}

	@Override /* RestClient */
	public MockRestRequest patch(Object url) throws RestCallException {
		return (MockRestRequest)super.patch(url);
	}

	@Override /* RestClient */
	public MockRestRequest callback(String callString) throws RestCallException {
		return (MockRestRequest)super.callback(callString);
	}

	@Override /* RestClient */
	public MockRestRequest request(String method, Object url, Object body) throws RestCallException {
		return (MockRestRequest)super.request(method, url, body);
	}

	@Override /* RestClient */
	public MockRestRequest request(String method, Object url) throws RestCallException {
		return (MockRestRequest)super.request(method, url);
	}

	@Override /* RestClient */
	public MockRestRequest request(String method, Object url, boolean hasBody) throws RestCallException {
		return (MockRestRequest)super.request(method, url, hasBody);
	}

	//------------------------------------------------------------------------------------------------------------------
	// Getters and setters.
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the current client-side REST request.
	 *
	 * <p>
	 * Note that this uses a {@link ThreadLocal} object for storage and so will not work on requests executed in
	 * separate threads such as when using {@link Future Futures}.
	 *
	 * @return The current client-side REST request, or <jk>null</jk> if not set.
	 */
	public HttpRequest getCurrentClientRequest() {
		return rreq.get();
	}

	/**
	 * Returns the current client-side REST response.
	 *
	 * <p>
	 * Note that this uses a {@link ThreadLocal} object for storage and so will not work on requests executed in
	 * separate threads such as when using {@link Future Futures}.
	 *
	 * @return The current client-side REST response, or <jk>null</jk> if not set.
	 */
	public MockRestResponse getCurrentClientResponse() {
		return rres.get();
	}

	/**
	 * Returns the current server-side REST request.
	 *
	 * <p>
	 * Note that this uses a {@link ThreadLocal} object for storage and so will not work on requests executed in
	 * separate threads such as when using {@link Future Futures}.
	 *
	 * @return The current server-side REST request, or <jk>null</jk> if not set.
	 */
	public MockServletRequest getCurrentServerRequest() {
		return sreq.get();
	}

	/**
	 * Returns the current server-side REST response.
	 *
	 * <p>
	 * Note that this uses a {@link ThreadLocal} object for storage and so will not work on requests executed in
	 * separate threads such as when using {@link Future Futures}.
	 *
	 * @return The current server-side REST response, or <jk>null</jk> if not set.
	 */
	public MockServletResponse getCurrentServerResponse() {
		return sres.get();
	}

	MockRestClient currentResponse(MockRestResponse value) {
		rres.set(value);
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------
	// RestClient methods.
	//------------------------------------------------------------------------------------------------------------------

	@Override /* RestClient */
	protected MockRestRequest createRequest(URI uri, String method, boolean hasBody) throws RestCallException {
		return new MockRestRequest(this, uri, method, hasBody);
	}

	@Override /* RestClient */
	protected MockRestResponse createResponse(RestRequest req, HttpResponse httpResponse, Parser parser) throws RestCallException {
		return new MockRestResponse(this, req, httpResponse, parser);
	}

	//------------------------------------------------------------------------------------------------------------------
	// HttpClientConnection methods.
	//------------------------------------------------------------------------------------------------------------------

	@Override /* HttpClientConnection */
	public void close() throws IOException {
		// Don't call super.close() because it will close the client.
		rreq.remove();
		rres.remove();
		sreq.remove();
		sres.remove();
	}

	@Override /* HttpClientConnection */
	public boolean isOpen() {
		return true;
	}

	@Override /* HttpClientConnection */
	public boolean isStale() {
		return false;
	}

	@Override /* HttpClientConnection */
	public void setSocketTimeout(int timeout) {}

	@Override /* HttpClientConnection */
	public int getSocketTimeout() {
		return Integer.MAX_VALUE;
	}

	@Override /* HttpClientConnection */
	public void shutdown() throws IOException {}

	@Override /* HttpClientConnection */
	public HttpConnectionMetrics getMetrics() {
		return null;
	}

	@Override /* HttpClientConnection */
	public boolean isResponseAvailable(int timeout) throws IOException {
		return true;
	}

	@Override /* HttpClientConnection */
	public void sendRequestHeader(HttpRequest request) throws HttpException, IOException {
		try {
			RequestLine rl = request.getRequestLine();
			String path = rl.getUri();
			String target = findTarget(request);

			HttpRequest req = findRestRequest(request);
			rreq.set(req);
			rres.remove();
			sreq.remove();
			sres.remove();

			path = target + path;

			MockPathResolver pr = new MockPathResolver(target, contextPath, servletPath, path, null);
			if (pr.getError() != null)
				throw runtimeException(pr.getError());

			MockServletRequest r = MockServletRequest
				.create(request.getRequestLine().getMethod(), pr.getURI())
				.contextPath(pr.getContextPath())
				.servletPath(pr.getServletPath())
				.pathVars(pathVars)
				.debug(isDebug());

			for (Header h : request.getAllHeaders())
				r.header(h.getName(), h.getValue());

			sreq.set(r);
			sreq.get().applyOverrides(req);
		} catch (Exception e) {
			throw new HttpException(e.getMessage(), e);
		}
	}

	/**
	 * Attempts to unwrap the request to find the underlying RestRequest object.
	 * Returns the same object if one of the low-level client methods are used (e.g. execute(HttpUriRequest)).
	 */
	private HttpRequest findRestRequest(HttpRequest req) {
		if (req instanceof RestRequestCreated)
			return ((RestRequestCreated)req).getRestRequest();
		if (req instanceof HttpRequestWrapper)
			return findRestRequest(((HttpRequestWrapper) req).getOriginal());
		return req;
	}

	private String findTarget(HttpRequest req) {
		if (req instanceof HttpRequestWrapper) {
			HttpHost httpHost = ((HttpRequestWrapper)req).getTarget();
			if (httpHost != null)
				return httpHost.toURI();
		}
		return "http://localhost";
	}

	@Override /* HttpClientConnection */
	public void sendRequestEntity(HttpEntityEnclosingRequest request) throws HttpException, IOException {
		byte[] body = new byte[0];
		HttpEntity entity = request.getEntity();
		if (entity != null) {
			long length = entity.getContentLength();
			if (length < 0)
				length = 1024;
			ByteArrayOutputStream baos = new ByteArrayOutputStream((int)Math.min(length, 1024));
			entity.writeTo(baos);
			baos.flush();
			body = baos.toByteArray();
		}
		sreq.get().body(body);
	}

	@Override /* HttpClientConnection */
	public HttpResponse receiveResponseHeader() throws HttpException, IOException {
		try {
			MockServletResponse res = MockServletResponse.create();
			restContext.execute(restObject, sreq.get(), res);

			// If the status isn't set, something's broken.
			if (res.getStatus() == 0)
				throw runtimeException("Response status was 0.");

			// A bug in HttpClient causes an infinite loop if the response is less than 200.
			// As a workaround, just add 1000 to the status code (which is better than an infinite loop).
			if (res.getStatus() < 200)
				res.setStatus(1000 + res.getStatus());

			sres.set(res);

			HttpResponse response = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, res.getStatus(), res.getMessage()));
			for (Map.Entry<String,String[]> e : res.getHeaders().entrySet())
				for (String hv : e.getValue())
					response.addHeader(e.getKey(), hv);

			return response;
		} catch (Exception e) {
			throw new HttpException(emptyIfNull(e.getMessage()), e);
		}
	}

	@Override /* HttpClientConnection */
	public void receiveResponseEntity(HttpResponse response) throws HttpException, IOException {
		InputStream is = new ByteArrayInputStream(sres.get().getBody());
		Header contentEncoding = response.getLastHeader("Content-Encoding");
		if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip"))
			is = new GZIPInputStream(is);
		response.setEntity(new InputStreamEntity(is));
	}

	@Override /* HttpClientConnection */
	public void flush() throws IOException {}
}
