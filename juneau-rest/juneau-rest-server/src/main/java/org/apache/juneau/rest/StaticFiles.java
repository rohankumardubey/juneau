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

import java.nio.file.*;
import java.util.*;

import javax.activation.*;

import org.apache.http.*;
import org.apache.juneau.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.cp.*;
import org.apache.juneau.http.resource.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.utils.*;

/**
 * API for retrieving localized static files from either the classpath or file system.
 */
public interface StaticFiles extends FileFinder {

	//-----------------------------------------------------------------------------------------------------------------
	// Static
	//-----------------------------------------------------------------------------------------------------------------

	/** Represents no static files */
	public abstract class Null implements StaticFiles {}

	/**
	 * Creates a new builder for this object.
	 *
	 * @return A new builder for this object.
	 */
	public static Builder create() {
		return new Builder();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Builder
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Builder class.
	 */
	@FluentSetters
	public static class Builder extends BeanBuilder<StaticFiles> {

		List<Header> headers;
		MimetypesFileTypeMap mimeTypes;
		FileFinder.Builder fileFinder;

		/**
		 * Constructor.
		 */
		protected Builder() {
			super(BasicStaticFiles.class);
			headers = AList.create();
			fileFinder = FileFinder.create();
			mimeTypes = new ExtendedMimetypesFileTypeMap();
		}

		/**
		 * Copy constructor.
		 *
		 * @param copyFrom The builder being copied.
		 */
		protected Builder(Builder copyFrom) {
			super(copyFrom);
			headers = AList.of(copyFrom.headers);
			mimeTypes = copyFrom.mimeTypes;
		}

		@Override /* BeanBuilder */
		protected StaticFiles buildDefault() {
			return new BasicStaticFiles(this);
		}

		@Override /* BeanBuilder */
		public Builder copy() {
			return new Builder(this);
		}

		//-------------------------------------------------------------------------------------------------------------
		// Properties
		//-------------------------------------------------------------------------------------------------------------

		/**
		 * Appends headers to add to HTTP responses.
		 *
		 * <p>
		 * Can be called multiple times to add multiple headers.
		 *
		 * @param headers The headers to add.
		 * @return This object (for method chaining).
		 */
		@FluentSetter
		public Builder headers(Header...headers) {
			this.headers.addAll(Arrays.asList(headers));
			return this;
		}

		/**
		 * Prepend the MIME type values to the MIME types registry.
		 *
		 * @param mimeTypes A .mime.types formatted string of entries.  See {@link MimetypesFileTypeMap#addMimeTypes(String)}.
		 * @return This object (for method chaining).
		 */
		@FluentSetter
		public Builder addMimeTypes(String mimeTypes) {
			this.mimeTypes.addMimeTypes(mimeTypes);
			return this;
		}

		/**
		 * Replaces the MIME types registry used for determining content types.
		 *
		 * @param mimeTypes The new MIME types registry.
		 * @return This object (for method chaining).
		 */
		@FluentSetter
		public Builder mimeTypes(MimetypesFileTypeMap mimeTypes) {
			this.mimeTypes = mimeTypes;
			return this;
		}

		/**
		 * Enables in-memory caching of files for quicker retrieval.
		 *
		 * @param cachingLimit The maximum file size in bytes.
		 * @return This object.
		 */
		@FluentSetter
		public Builder caching(long cachingLimit) {
			fileFinder.caching(cachingLimit);
			return this;
		}

		/**
		 * Adds a class subpackage to the lookup paths.
		 *
		 * @param c The class whose package will be added to the lookup paths.  Must not be <jk>null</jk>.
		 * @param path The absolute or relative subpath.
		 * @param recursive If <jk>true</jk>, also recursively adds all the paths of the parent classes as well.
		 * @return This object.
		 */
		@FluentSetter
		public Builder cp(Class<?> c, String path, boolean recursive) {
			fileFinder.cp(c, path, recursive);
			return this;
		}

		/**
		 * Adds a file system directory to the lookup paths.
		 *
		 * @param path The path relative to the working directory.  Must not be <jk>null</jk>
		 * @return This object.
		 */
		@FluentSetter
		public Builder dir(String path) {
			fileFinder.dir(path);
			return this;
		}

		/**
		 * Specifies the regular expression file name pattern to use to exclude files from being retrieved from the file source.
		 *
		 * @param patterns
		 * 	The regular expression exclude patterns.
		 * 	<br>If none are specified, no files will be excluded.
		 * @return This object.
		 */
		@FluentSetter
		public Builder exclude(String...patterns) {
			fileFinder.exclude(patterns);
			return this;
		}

		/**
		 * Specifies the regular expression file name patterns to use to include files being retrieved from the file source.
		 *
		 * @param patterns
		 * 	The regular expression include patterns.
		 * 	<br>The default is <js>".*"</js>.
		 * @return This object.
		 */
		@FluentSetter
		public Builder include(String...patterns) {
			fileFinder.include(patterns);
			return this;
		}

		/**
		 * Adds a file system directory to the lookup paths.
		 *
		 * @param path The directory path.
		 * @return This object.
		 */
		@FluentSetter
		public Builder path(Path path) {
			fileFinder.path(path);
			return this;
		}

		// <FluentSetters>

		@Override /* BeanBuilder */
		public Builder type(Class<? extends StaticFiles> value) {
			super.type(value);
			return this;
		}

		@Override /* BeanBuilder */
		public Builder impl(StaticFiles value) {
			super.impl(value);
			return this;
		}

		@Override /* BeanBuilder */
		public Builder outer(Object value) {
			super.outer(value);
			return this;
		}

		@Override /* BeanBuilder */
		public Builder beanStore(BeanStore value) {
			super.beanStore(value);
			return this;
		}

		// </FluentSetters>
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Instance
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Resolve the specified path.
	 *
	 * @param path The path to resolve to a static file.
	 * @param locale Optional locale.
	 * @return The resource, or <jk>null</jk> if not found.
	 */
	public Optional<HttpResource> resolve(String path, Locale locale);
}
