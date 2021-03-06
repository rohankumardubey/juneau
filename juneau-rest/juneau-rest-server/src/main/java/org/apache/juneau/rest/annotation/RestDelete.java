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

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;
import java.nio.charset.*;

import org.apache.juneau.annotation.*;
import org.apache.juneau.rest.*;
import org.apache.juneau.dto.swagger.*;
import org.apache.juneau.encoders.*;

/**
 * Identifies a REST DELETE operation Java method on a {@link RestServlet} implementation class.
 *
 * <p>
 * This is a specialized subtype of <c><ja>{@link RestOp @RestOp}(method=<jsf>DELETE</jsf>)</c>.
 *
 * <ul class='seealso'>
 * 	<li class='link'>{@doc RestMethod}
 * </ul>
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
@Inherited
@ContextApply(RestDeleteAnnotation.RestOpContextApply.class)
@AnnotationGroup(RestOp.class)
public @interface RestDelete {

	/**
	 * Specifies whether this method can be called based on the client version.
	 *
	 * <p>
	 * The client version is identified via the HTTP request header identified by
	 * {@link Rest#clientVersionHeader() @Rest(clientVersionHeader)} which by default is <js>"Client-Version"</js>.
	 *
	 * <p>
	 * This is a specialized kind of {@link RestMatcher} that allows you to invoke different Java methods for the same
	 * method/path based on the client version.
	 *
	 * <p>
	 * The format of the client version range is similar to that of OSGi versions.
	 *
	 * <p>
	 * In the following example, the Java methods are mapped to the same HTTP method and URL <js>"/foobar"</js>.
	 * <p class='bcode w800'>
	 * 	<jc>// Call this method if Client-Version is at least 2.0.
	 * 	// Note that this also matches 2.0.1.</jc>
	 * 	<ja>@RestDelete</ja>(path=<js>"/foobar"</js>, clientVersion=<js>"2.0"</js>)
	 * 	<jk>public</jk> Object method1()  {...}
	 *
	 * 	<jc>// Call this method if Client-Version is at least 1.1, but less than 2.0.</jc>
	 * 	<ja>@RestDelete</ja>(path=<js>"/foobar"</js>, clientVersion=<js>"[1.1,2.0)"</js>)
	 * 	<jk>public</jk> Object method2()  {...}
	 *
	 * 	<jc>// Call this method if Client-Version is less than 1.1.</jc>
	 * 	<ja>@RestDelete</ja>(path=<js>"/foobar"</js>, clientVersion=<js>"[0,1.1)"</js>)
	 * 	<jk>public</jk> Object method3()  {...}
	 * </p>
	 *
	 * <p>
	 * It's common to combine the client version with transforms that will convert new POJOs into older POJOs for
	 * backwards compatibility.
	 * <p class='bcode w800'>
	 * 	<jc>// Call this method if Client-Version is at least 2.0.</jc>
	 * 	<ja>@RestDelete</ja>(path=<js>"/foobar"</js>, clientVersion=<js>"2.0"</js>)
	 * 	<jk>public void</jk> newMethod()  {...}
	 *
	 * 	<jc>// Call this method if Client-Version is at least 1.1, but less than 2.0.</jc>
	 * 	<ja>@RestDelete</ja>(path=<js>"/foobar"</js>, clientVersion=<js>"[1.1,2.0)"</js>})
	 * 	<jk>public void</jk> oldMethod() {...}
	 *
	 * <p>
	 * Note that in the previous example, we're returning the exact same POJO, but using a transform to convert it into
	 * an older form.
	 * The old method could also just return back a completely different object.
	 * The range can be any of the following:
	 * <ul>
	 * 	<li><js>"[0,1.0)"</js> = Less than 1.0.  1.0 and 1.0.0 does not match.
	 * 	<li><js>"[0,1.0]"</js> = Less than or equal to 1.0.  Note that 1.0.1 will match.
	 * 	<li><js>"1.0"</js> = At least 1.0.  1.0 and 2.0 will match.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link RestContextBuilder#clientVersionHeader(String)}
	 * </ul>
	 */
	String clientVersion() default "";

	/**
	 * Allows you to extend the {@link RestOpContext} class to modify how any of the methods are implemented.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link RestOpContextBuilder#type(Class)}
	 * </ul>
	 */
	Class<? extends RestOpContext> contextClass() default RestOpContext.Null.class;

	/**
	 * Enable debug mode.
	 *
	 * <p>
	 * Enables the following:
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		HTTP request/response bodies are cached in memory for logging purposes.
	 * 	<li>
	 * 		Request/response messages are automatically logged.
	 * </ul>
	 *
	 * <p>
	 * Possible values (case insensitive):
	 * <ul>
	 * 	<li><js>"true"</js> - Debug is enabled for all requests.
	 * 	<li><js>"false"</js> - Debug is disabled for all requests.
	 * 	<li><js>"conditional"</js> - Debug is enabled only for requests that have a <c class='snippet'>X-Debug: true</c> header.
	 * 	<li><js>""</js> (or anything else) - Debug mode is inherited from class.
	 * </ul>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Supports {@doc RestSvlVariables}
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link RestContextBuilder#debugEnablement()}
	 * </ul>
	 */
	String debug() default "";

	/**
	 * Default <c>Accept</c> header.
	 *
	 * <p>
	 * The default value for the <c>Accept</c> header if not specified on a request.
	 *
	 * <p>
	 * This is a shortcut for using {@link #defaultRequestHeaders()} for just this specific header.
	 */
	String defaultAccept() default "";

	/**
	 * Default character encoding.
	 *
	 * <p>
	 * The default character encoding for the request and response if not specified on the request.
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Supports {@doc RestSvlVariables}
	 * 		(e.g. <js>"$S{mySystemProperty}"</js>).
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link RestContextBuilder#defaultCharset(Charset)}
	 * 	<li class='jm'>{@link RestOpContextBuilder#defaultCharset(Charset)}
	 * 	<li class='ja'>{@link Rest#defaultCharset}
	 * </ul>
	 */
	String defaultCharset() default "";

	/**
	 * Specifies default values for query parameters.
	 *
	 * <p>
	 * Strings are of the format <js>"name=value"</js>.
	 *
	 * <p>
	 * Affects values returned by {@link RestRequest#getQueryParam(String)} when the parameter is not present on the request.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<ja>@RestDelete</ja>(path=<js>"/*"</js>, defaultRequestQueryData={<js>"foo=bar"</js>})
	 * 	<jk>public</jk> String doDelete(<ja>@Query</ja>(<js>"foo"</js>) String foo)  {...}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		You can use either <js>':'</js> or <js>'='</js> as the key/value delimiter.
	 * 	<li>
	 * 		Key and value is trimmed of whitespace.
	 * 	<li>
	 * 		Supports {@doc RestSvlVariables}
	 * 		(e.g. <js>"$S{mySystemProperty}"</js>).
	 * </ul>
	 */
	String[] defaultRequestQueryData() default {};

	/**
	 * Default request attributes.
	 *
	 * <p>
	 * Specifies default values for request attributes if they're not already set on the request.
	 *
	 * <p>
	 * Affects values returned by the following methods:
	 * 	<ul>
	 * 		<li class='jm'>{@link RestRequest#getAttribute(String)}.
	 * 		<li class='jm'>{@link RestRequest#getAttributes()}.
	 * 	</ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Defined via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(defaultRequestAttributes={<js>"Foo=bar"</js>, <js>"Baz: $C{REST/myAttributeValue}"</js>})
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Override at the method level.</jc>
	 * 		<ja>@RestGet</ja>(defaultRequestAttributes={<js>"Foo: bar"</js>})
	 * 		<jk>public</jk> Object myMethod() {...}
	 * 	}
	 * </p>
	 *
	 * </ul>
	 * <ul class='notes'>
	 * 	<li>
	 * 		Supports {@doc RestSvlVariables}
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link RestContextBuilder#defaultRequestAttributes(NamedAttribute...)}
	 * 	<li class='ja'>{@link Rest#defaultRequestAttributes()}
	 * </ul>
	 */
	String[] defaultRequestAttributes() default {};

	/**
	 * Default request headers.
	 *
	 * <p>
	 * Specifies default values for request headers if they're not passed in through the request.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Assume "text/json" Accept value when Accept not specified</jc>
	 * 	<ja>@RestDelete</ja>(path=<js>"/*"</js>, defaultRequestHeaders={<js>"Accept: text/json"</js>})
	 * 	<jk>public</jk> String doDelete()  {...}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Supports {@doc RestSvlVariables}
	 * 		(e.g. <js>"$S{mySystemProperty}"</js>).
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link RestContextBuilder#defaultRequestHeaders(org.apache.http.Header...)}
	 * </ul>
	 */
	String[] defaultRequestHeaders() default {};

	/**
	 * Default response headers.
	 *
	 * <p>
	 * Specifies default values for response headers if they're not overwritten during the request.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Assume "text/json" Accept value when Accept not specified</jc>
	 * 	<ja>@RestDelete</ja>(path=<js>"/*"</js>, defaultResponseHeaders={<js>"Content-Type: text/json"</js>})
	 * 	<jk>public</jk> String doDelete()  {...}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Supports {@doc RestSvlVariables}
	 * 		(e.g. <js>"$S{mySystemProperty}"</js>).
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link RestContextBuilder#defaultResponseHeaders(org.apache.http.Header...)}
	 * </ul>
	 */
	String[] defaultResponseHeaders() default {};

	/**
	 * Optional description for the exposed API.
	 *
	 * <p>
	 * This description is used in the following locations:
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The value returned by {@link Operation#getDescription()} in the auto-generated swagger.
	 * 	<li>
	 * 		The <js>"$RS{operationDescription}"</js> variable.
	 * 	<li>
	 * 		The description of the method in the Swagger page.
	 * </ul>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Corresponds to the swagger field <c>/paths/{path}/{method}/description</c>.
	 * 	<li>
	 * 		Supports {@doc RestSvlVariables}
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String[] description() default {};

	/**
	 * Specifies the compression encoders for this method.
	 *
	 * <p>
	 * Encoders are used to enable various kinds of compression (e.g. <js>"gzip"</js>) on requests and responses.
	 *
	 * <p>
	 * This value overrides encoders specified at the class level using {@link Rest#encoders()}.
	 * The {@link org.apache.juneau.encoders.EncoderGroup.Inherit} class can be used to include values from the parent class.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Define a REST resource that handles GZIP compression.</jc>
	 * 	<ja>@Rest</ja>(
	 * 		encoders={
	 * 			GzipEncoder.<jk>class</jk>
	 * 		}
	 * 	)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Define a REST method that can also use a custom encoder.</jc>
	 * 		<ja>@RestDelete</ja>(
	 * 			encoders={
	 * 				EncoderGroup.Inherit.<jk>class</jk>, MyEncoder.<jk>class</jk>
	 * 			}
	 * 		)
	 * 		<jk>public</jk> String doDelete() {
	 * 			...
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <p>
	 * The programmatic equivalent to this annotation is:
	 * <p class='bcode w800'>
	 * 	RestOpContextBuilder <jv>builder</jv> = RestOpContextBuilder.<jsm>create</jsm>(<jv>method</jv>,<jv>restContext</jv>);
	 * 	<jv>builder</jv>.getEncoders().set(<jv>classes</jv>);
	 * </p>
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc RestEncoders}
	 * </ul>
	 */
	Class<? extends Encoder>[] encoders() default {};

	/**
	 * Method-level guards.
	 *
	 * <p>
	 * Associates one or more {@link RestGuard RestGuards} with this method.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link RestOpContextBuilder#guards()}
	 * </ul>
	 */
	Class<? extends RestGuard>[] guards() default {};

	/**
	 * Method matchers.
	 *
	 * <p>
	 * Associates one more more {@link RestMatcher RestMatchers} with this method.
	 *
	 * <p>
	 * Matchers are used to allow multiple Java methods to handle requests assigned to the same URL path pattern, but
	 * differing based on some request attribute, such as a specific header value.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jac'>{@link RestMatcher}
	 * </ul>
	 */
	Class<? extends RestMatcher>[] matchers() default {};

	/**
	 * Dynamically apply this annotation to the specified methods.
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc DynamicallyAppliedAnnotations}
	 * </ul>
	 */
	String[] on() default {};

	/**
	 * Optional path pattern for the specified method.
	 *
	 * <p>
	 * Appending <js>"/*"</js> to the end of the path pattern will make it match any remainder too.
	 * <br>Not appending <js>"/*"</js> to the end of the pattern will cause a 404 (Not found) error to occur if the exact
	 * pattern is not found.
	 *
	 * <p>
	 * The path can contain variables that get resolved to {@link org.apache.juneau.http.annotation.Path @Path} parameters.
	 *
	 * <h5 class='figure'>Examples:</h5>
	 * <p class='bcode w800'>
	 * 	<ja>@RestDelete</ja>(path=<js>"/myurl/{foo}/{bar}/{baz}/*"</js>)
	 * </p>
	 * <p class='bcode w800'>
	 * 	<ja>@RestDelete</ja>(path=<js>"/myurl/{0}/{1}/{2}/*"</js>)
	 * </p>
	 *
	 * <p>
	 * Note that you can also use {@link #value()} to specify the path.
	 *
	 * <ul class='seealso'>
	 * 	<li class='ja'>{@link org.apache.juneau.http.annotation.Path}
	 * </ul>
	 */
	String[] path() default {};

	/**
	 * Role guard.
	 *
	 * <p>
	 * An expression defining if a user with the specified roles are allowed to access this method.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jk>public class</jk> MyResource <jk>extends</jk> RestServlet {
	 *
	 * 		<ja>@RestDelete</ja>(
	 * 			path=<js>"/foo"</js>,
	 * 			roleGuard=<js>"ROLE_ADMIN || (ROLE_READ_WRITE &amp;&amp; ROLE_SPECIAL)"</js>
	 * 		)
	 * 		<jk>public</jk> Object doDelete() {
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Supports any of the following expression constructs:
	 * 		<ul>
	 * 			<li><js>"foo"</js> - Single arguments.
	 * 			<li><js>"foo,bar,baz"</js> - Multiple OR'ed arguments.
	 * 			<li><js>"foo | bar | baz"</js> - Multiple OR'ed arguments, pipe syntax.
	 * 			<li><js>"foo || bar || baz"</js> - Multiple OR'ed arguments, Java-OR syntax.
	 * 			<li><js>"fo*"</js> - Patterns including <js>'*'</js> and <js>'?'</js>.
	 * 			<li><js>"fo* &amp; *oo"</js> - Multiple AND'ed arguments, ampersand syntax.
	 * 			<li><js>"fo* &amp;&amp; *oo"</js> - Multiple AND'ed arguments, Java-AND syntax.
	 * 			<li><js>"fo* || (*oo || bar)"</js> - Parenthesis.
	 * 		</ul>
	 * 	<li>
	 * 		AND operations take precedence over OR operations (as expected).
	 * 	<li>
	 * 		Whitespace is ignored.
	 * 	<li>
	 * 		<jk>null</jk> or empty expressions always match as <jk>false</jk>.
	 * 	<li>
	 * 		If patterns are used, you must specify the list of declared roles using {@link #rolesDeclared()} or {@link RestOpContextBuilder#rolesDeclared(String...)}.
	 * 	<li>
	 * 		Supports {@doc RestSvlVariables}
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * 	<li>
	 * 		When defined on parent/child classes and methods, ALL guards within the hierarchy must pass.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link RestOpContextBuilder#roleGuard(String)}
	 * </ul>
	 */
	String roleGuard() default "";

	/**
	 * Declared roles.
	 *
	 * <p>
	 * A comma-delimited list of all possible user roles.
	 *
	 * <p>
	 * Used in conjunction with {@link #roleGuard()} is used with patterns.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jk>public class</jk> MyResource <jk>extends</jk> RestServlet {
	 *
	 * 		<ja>@RestDelete</ja>(
	 * 			path=<js>"/foo"</js>,
	 * 			rolesDeclared=<js>"ROLE_ADMIN,ROLE_READ_WRITE,ROLE_READ_ONLY,ROLE_SPECIAL"</js>,
	 * 			roleGuard=<js>"ROLE_ADMIN || (ROLE_READ_WRITE &amp;&amp; ROLE_SPECIAL)"</js>
	 * 		)
	 * 		<jk>public</jk> Object doGet() {
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link RestOpContextBuilder#rolesDeclared(String...)}
	 * </ul>
	 */
	String rolesDeclared() default "";

	/**
	 * Optional summary for the exposed API.
	 *
	 * <p>
	 * This summary is used in the following locations:
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The value returned by {@link Operation#getSummary()} in the auto-generated swagger.
	 * 	<li>
	 * 		The <js>"$RS{operationSummary}"</js> variable.
	 * 	<li>
	 * 		The summary of the method in the Swagger page.
	 * </ul>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Corresponds to the swagger field <c>/paths/{path}/{method}/summary</c>.
	 * 	<li>
	 * 		Supports {@doc RestSvlVariables}
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String summary() default "";

	/**
	 * Provides swagger-specific metadata on this method.
	 *
	 * <p>
	 * Used to populate the auto-generated OPTIONS swagger documentation.
	 *
	 * <p>
	 * The format of this annotation is JSON when all individual parts are concatenated.
	 * <br>The starting and ending <js>'{'</js>/<js>'}'</js> characters around the entire value are optional.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<ja>@RestDelete</ja>(
	 * 		path=<js>"/{propertyName}"</js>,
	 *
	 * 		<jc>// Swagger info.</jc>
	 * 		swagger={
	 * 			<js>"parameters:["</js>,
	 * 				<js>"{name:'propertyName',in:'path',description:'The system property name.'},"</js>,
	 * 				<js>"{in:'body',description:'The new system property value.'}"</js>,
	 * 			<js>"],"</js>,
	 * 			<js>"responses:{"</js>,
	 * 				<js>"302: {headers:{Location:{description:'The root URL of this resource.'}}},"</js>,
	 * 				<js>"403: {description:'User is not an admin.'}"</js>,
	 * 			<js>"}"</js>
	 * 		}
	 * 	)
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		The format is {@doc SimplifiedJson}.
	 * 		<br>Multiple lines are concatenated with newlines.
	 * 	<li>
	 * 		The starting and ending <js>'{'</js>/<js>'}'</js> characters around the entire value are optional.
	 * 	<li>
	 * 		These values are superimposed on top of any Swagger JSON file present for the resource in the classpath.
	 * 	<li>
	 * 		Supports {@doc RestSvlVariables}
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='ja'>{@link OpSwagger}
	 * 	<li class='jc'>{@link SwaggerProvider}
	 * </ul>
	 */
	OpSwagger swagger() default @OpSwagger;

	/**
	 * REST method path.
	 *
	 * <p>
	 * Can be used to provide a shortened form for the {@link #path()} value.
	 *
	 * <p>
	 * The following examples are considered equivalent.
	 * <p class='bcode w800'>
	 * 	<jc>// Normal form</jc>
	 * 	<ja>@RestDelete</ja>(path=<js>"/{propertyName}"</js>)
	 *
	 * 	<jc>// Shortened form</jc>
	 * 	<ja>@RestDelete</ja>(<js>"/{propertyName}"</js>)
	 * </p>
	 */
	String value() default "";
}
