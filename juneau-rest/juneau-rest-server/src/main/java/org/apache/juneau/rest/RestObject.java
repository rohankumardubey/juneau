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

import static org.apache.juneau.internal.ClassUtils.*;
import static org.apache.juneau.rest.annotation.HookEvent.*;

import java.text.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.logging.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.juneau.http.response.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.rest.annotation.*;

/**
 * Identical to {@link RestServlet} but doesn't extend from {@link HttpServlet}.
 *
 * <p>
 * This is particularly useful in Spring Boot environments that auto-detect servlets to deploy in servlet containers,
 * but you want this resource to be deployed as a child instead.
 *
 * <ul class='seealso'>
 * 	<li class='link'>{@doc BasicRest}
 * </ul>
 */
public abstract class RestObject {

	private AtomicReference<RestContext> context = new AtomicReference<>();

	//-----------------------------------------------------------------------------------------------------------------
	// Context methods.
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Sets the context object for this servlet.
	 *
	 * @param context Sets the context object on this servlet.
	 * @throws ServletException If error occurred during post-initialiation.
	 */
	protected void setContext(RestContext context) throws ServletException {
		this.context.set(context);
	}

	/**
	 * Returns the read-only context object that contains all the configuration information about this resource.
	 *
	 * @return The context information on this servlet.
	 */
	protected RestContext getContext() {
		if (context.get() == null)
			throw new InternalServerError("RestContext object not set on resource.");
		return context.get();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Convenience logger methods
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Log a message at {@link Level#INFO} level.
	 *
	 * <p>
	 * Subclasses can intercept the handling of these messages by overriding {@link #doLog(Level, Throwable, Supplier)}.
	 *
	 * @param msg The message to log.
	 */
	public void log(String msg) {
		doLog(Level.INFO, null, () -> msg);
	}

	/**
	 * Log a message.
	 *
	 * <p>
	 * Subclasses can intercept the handling of these messages by overriding {@link #doLog(Level, Throwable, Supplier)}.
	 *
	 * @param msg The message to log.
	 * @param cause The cause.
	 */
	public void log(String msg, Throwable cause) {
		doLog(Level.INFO, null, () -> msg);
	}

	/**
	 * Log a message.
	 *
	 * <p>
	 * Subclasses can intercept the handling of these messages by overriding {@link #doLog(Level, Throwable, Supplier)}.
	 *
	 * @param level The log level.
	 * @param msg The message to log.
	 * @param args Optional {@link MessageFormat}-style arguments.
	 */
	public void log(Level level, String msg, Object...args) {
		doLog(level, null, () -> StringUtils.format(msg, args));
	}

	/**
	 * Log a message.
	 *
	 * <p>
	 * Subclasses can intercept the handling of these messages by overriding {@link #doLog(Level, Throwable, Supplier)}.
	 *
	 * @param level The log level.
	 * @param cause The cause.
	 * @param msg The message to log.
	 * @param args Optional {@link MessageFormat}-style arguments.
	 */
	public void log(Level level, Throwable cause, String msg, Object...args) {
		doLog(level, cause, () -> StringUtils.format(msg, args));
	}

	/**
	 * Main logger method.
	 *
	 * <p>
	 * The default behavior logs a message to the Java logger of the class name.
	 *
	 * <p>
	 * Subclasses can override this method to implement their own logger handling.
	 *
	 * @param level The log level.
	 * @param cause Optional throwable.
	 * @param msg The message to log.
	 */
	protected void doLog(Level level, Throwable cause, Supplier<String> msg) {
		RestContext c = context.get();
		Logger logger = c == null ? null : c.getLogger();
		if (logger == null)
			logger = Logger.getLogger(className(this));
		logger.log(level, cause, msg);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Hook events
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Method that gets called during servlet initialization.
	 *
	 * <p>
	 * This method is called from within the {@link Servlet#init(ServletConfig)} method after the {@link RestContextBuilder}
	 * object has been created and initialized with the annotations defined on the class, but before the
	 * {@link RestContext} object has been created.
	 *
	 * <p>
	 * An example of this is the <c>PetStoreResource</c> class that uses an init method to perform initialization
	 * of an internal data structure.
	 *
	 * <h5 class='figure'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<ja>@Rest</ja>(...)
	 * 	<jk>public class</jk> PetStoreResource <jk>extends</jk> ResourceJena {
	 *
	 * 		<jc>// Our database.</jc>
	 * 		<jk>private</jk> Map&lt;Integer,Pet&gt; <jf>petDB</jf>;
	 *
	 * 		<ja>@Override</ja>
	 * 		<jk>public void</jk> onInit(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			<jc>// Load our database from a local JSON file.</jc>
	 * 			<jf>petDB</jf> = JsonParser.<jsf>DEFAULT</jsf>.parse(getClass().getResourceAsStream(<js>"PetStore.json"</js>), LinkedHashMap.<jk>class</jk>, Integer.<jk>class</jk>, Pet.<jk>class</jk>);
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		The default implementation of this method is a no-op.
	 * 	<li>
	 * 		Multiple INIT methods can be defined on a class.
	 * 		<br>INIT methods on parent classes are invoked before INIT methods on child classes.
	 * 		<br>The order of INIT method invocations within a class is alphabetical, then by parameter count, then by parameter types.
	 * 	<li>
	 * 		The method can throw any exception causing initialization of the servlet to fail.
	 * </ul>
	 *
	 * @param builder Context builder which can be used to configure the servlet.
	 * @throws Exception Any exception thrown will cause servlet to fail startup.
	 */
	@RestHook(INIT)
	public void onInit(RestContextBuilder builder) throws Exception {}

	/**
	 * Method that gets called immediately after servlet initialization.
	 *
	 * <p>
	 * This method is called from within the {@link Servlet#init(ServletConfig)} method after the {@link RestContext}
	 * object has been created.
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		The default implementation of this method is a no-op.
	 * 	<li>
	 * 		Multiple POST_INIT methods can be defined on a class.
	 * 		<br>POST_INIT methods on parent classes are invoked before POST_INIT methods on child classes.
	 * 		<br>The order of POST_INIT method invocations within a class is alphabetical, then by parameter count, then by parameter types.
	 * 	<li>
	 * 		The method can throw any exception causing initialization of the servlet to fail.
	 * </ul>
	 *
	 * @param context The initialized context object.
	 * @throws Exception Any exception thrown will cause servlet to fail startup.
	 */
	@RestHook(POST_INIT)
	public void onPostInit(RestContext context) throws Exception {}

	/**
	 * Identical to {@link #onPostInit(RestContext)} except the order of execution is child-resources first.
	 *
	 * <p>
	 * Use this method if you need to perform any kind of initialization on child resources before the parent resource.
	 *
	 * <p>
	 * This method is called from within the {@link Servlet#init(ServletConfig)} method after the {@link RestContext}
	 * object has been created and after the {@link HookEvent#POST_INIT} methods have been called.
	 *
	 * <p>
	 * The only valid parameter type for this method is {@link RestContext} which can be used to retrieve information
	 * about the servlet.
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		The default implementation of this method is a no-op.
	 * 	<li>
	 * 		Multiple POST_INIT_CHILD_FIRST methods can be defined on a class.
	 * 		<br>POST_INIT_CHILD_FIRST methods on parent classes are invoked before POST_INIT_CHILD_FIRST methods on child classes.
	 * 		<br>The order of POST_INIT_CHILD_FIRST method invocations within a class is alphabetical, then by parameter count, then by parameter types.
	 * 	<li>
	 * 		The method can throw any exception causing initialization of the servlet to fail.
	 * </ul>
	 *
	 * @param context The initialized context object.
	 * @throws Exception Any exception thrown will cause servlet to fail startup.
	 */
	@RestHook(POST_INIT_CHILD_FIRST)
	public void onPostInitChildFirst(RestContext context) throws Exception {}

	/**
	 * Method that gets called during servlet destroy.
	 *
	 * <p>
	 * This method is called from within the {@link Servlet#destroy()}.
	 *
	 * <h5 class='figure'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<ja>@Rest</ja>(...)
	 * 	<jk>public class</jk> PetStoreResource <jk>extends</jk> ResourceJena {
	 *
	 * 		<jc>// Our database.</jc>
	 * 		<jk>private</jk> Map&lt;Integer,Pet&gt; <jf>petDB</jf>;
	 *
	 * 		<ja>@Override</ja>
	 * 		<jk>public void</jk> onDestroy(RestContext context) {
	 * 			<jf>petDB</jf> = <jk>null</jk>;
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		The default implementation of this method is a no-op.
	 * 	<li>
	 * 		Multiple DESTROY methods can be defined on a class.
	 * 		<br>DESTROY methods on child classes are invoked before DESTROY methods on parent classes.
	 * 		<br>The order of DESTROY method invocations within a class is alphabetical, then by parameter count, then by parameter types.
	 * 	<li>
	 * 		In general, destroy methods should not throw any exceptions, although if any are thrown, the stack trace will be
	 * 		printed to <c>System.err</c>.
	 * </ul>
	 *
	 * @param context The initialized context object.
	 * @throws Exception Any exception thrown will cause stack trace to be printed to <c>System.err</c>.
	 */
	@RestHook(DESTROY)
	public void onDestroy(RestContext context) throws Exception {}

	/**
	 * A method that is called immediately after the <c>HttpServlet.service(HttpServletRequest, HttpServletResponse)</c>
	 * method is called.
	 *
	 * <p>
	 * Note that you only have access to the raw request and response objects at this point.
	 *
	 * <h5 class='figure'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<ja>@Rest</ja>(...)
	 * 	<jk>public class</jk> MyResource <jk>extends</jk> BasicRestServlet {
	 *
	 * 		<jc>// Add a request attribute to all incoming requests.</jc>
	 * 		<ja>@Override</ja>
	 * 		<jk>public void</jk> onStartCall(HttpServletRequest req, HttpServletResponse res) {
	 * 			req.setAttribute(<js>"foobar"</js>, <jk>new</jk> FooBar());
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		The default implementation of this method is a no-op.
	 * 	<li>
	 * 		Multiple START_CALL methods can be defined on a class.
	 * 		<br>START_CALL methods on parent classes are invoked before START_CALL methods on child classes.
	 * 		<br>The order of START_CALL method invocations within a class is alphabetical, then by parameter count, then by parameter types.
	 * 	<li>
	 * 		The method can throw any exception.
	 * 		<br>{@link BasicHttpException HttpExceptions} can be thrown to cause a particular HTTP error status code.
	 * 		<br>All other exceptions cause an HTTP 500 error status code.
	 * </ul>
	 *
	 * @param req The HTTP servlet request object.
	 * @param res The HTTP servlet response object.
	 * @throws Exception Any exception.
	 */
	@RestHook(START_CALL)
	public void onStartCall(HttpServletRequest req, HttpServletResponse res) throws Exception {}

	/**
	 * Method that gets called immediately before the <ja>@RestOp</ja> annotated method gets called.
	 *
	 * <p>
	 * At this point, the {@link RestRequest} object has been fully initialized, and all {@link RestGuard} and
	 * {@link RestMatcher} objects have been called.
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		The default implementation of this method is a no-op.
	 * 	<li>
	 * 		Multiple PRE_CALL methods can be defined on a class.
	 * 		<br>PRE_CALL methods on parent classes are invoked before PRE_CALL methods on child classes.
	 * 		<br>The order of PRE_CALL method invocations within a class is alphabetical, then by parameter count, then by parameter types.
	 * 	<li>
	 * 		The method can throw any exception.
	 * 		<br>{@link BasicHttpException HttpExceptions} can be thrown to cause a particular HTTP error status code.
	 * 		<br>All other exceptions cause an HTTP 500 error status code.
	 * 	<li>
	 * 		It's advisable not to mess around with the HTTP body itself since you may end up consuming the body
	 * 		before the actual REST method has a chance to use it.
	 * </ul>
	 *
	 * @param req The request object.
	 * @param res The response object.
	 * @throws Exception Any exception.
	 */
	@RestHook(PRE_CALL)
	public void onPreCall(RestRequest req, RestResponse res) throws Exception {}

	/**
	 * Method that gets called immediately after the <ja>@RestOp</ja> annotated method gets called.
	 *
	 * <p>
	 * At this point, the output object returned by the method call has been set on the response, but
	 * {@link RestConverter RestConverters} have not yet been executed and the response has not yet been written.
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		The default implementation of this method is a no-op.
	 * 	<li>
	 * 		Multiple POST_CALL methods can be defined on a class.
	 * 		<br>POST_CALL methods on parent classes are invoked before POST_CALL methods on child classes.
	 * 		<br>The order of POST_CALL method invocations within a class is alphabetical, then by parameter count, then by parameter types.
	 * 	<li>
	 * 		The method can throw any exception, although at this point it is too late to set an HTTP error status code.
	 * </ul>
	 *
	 * @param req The request object.
	 * @param res The response object.
	 * @throws Exception Any exception.
	 */
	@RestHook(POST_CALL)
	public void onPostCall(RestRequest req, RestResponse res) throws Exception {}

	/**
	 * Method that gets called right before we exit the servlet service method.
	 *
	 * <p>
	 * At this point, the output has been written and flushed.
	 *
	 * <p>
	 * The following attributes are set on the {@link HttpServletRequest} object that can be useful for logging purposes:
	 * <ul>
	 * 	<li><js>"Exception"</js> - Any exceptions thrown during the request.
	 * 	<li><js>"ExecTime"</js> - Execution time of the request.
	 * </ul>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		The default implementation of this method is a no-op.
	 * 	<li>
	 * 		Multiple END_CALL methods can be defined on a class.
	 * 		<br>END_CALL methods on parent classes are invoked before END_CALL methods on child classes.
	 * 		<br>The order of END_CALL method invocations within a class is alphabetical, then by parameter count, then by parameter types.
	 * 	<li>
	 * 		The method can throw any exception, although at this point it is too late to set an HTTP error status code.
	 * 	<li>
	 * 		Note that if you override a parent method, you probably need to call <code><jk>super</jk>.parentMethod(...)</code>.
	 * 		<br>The method is still considered part of the parent class for ordering purposes even though it's
	 * 		overridden by the child class.
	 * </ul>
	 *
	 * @param req The HTTP servlet request object.
	 * @param res The HTTP servlet response object.
	 * @throws Exception Any exception.
	 */
	@RestHook(END_CALL)
	public void onEndCall(HttpServletRequest req, HttpServletResponse res) throws Exception {}

	//-----------------------------------------------------------------------------------------------------------------
	// Other methods
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the current HTTP request.
	 *
	 * @return The current HTTP request, or <jk>null</jk> if it wasn't created.
	 */
	public synchronized RestRequest getRequest() {
		return getContext().getRequest();
	}

	/**
	 * Returns the current HTTP response.
	 *
	 * @return The current HTTP response, or <jk>null</jk> if it wasn't created.
	 */
	public synchronized RestResponse getResponse() {
		return getContext().getResponse();
	}
}
