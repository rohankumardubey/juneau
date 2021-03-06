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
package org.apache.juneau.serializer;

import static org.apache.juneau.http.HttpHeaders.*;
import static org.apache.juneau.internal.ExceptionUtils.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.cp.*;
import org.apache.juneau.http.header.*;

/**
 * Represents a group of {@link Serializer Serializers} that can be looked up by media type.
 *
 * <h5 class='topic'>Description</h5>
 *
 * Provides the following features:
 * <ul class='spaced-list'>
 * 	<li>
 * 		Finds serializers based on HTTP <c>Accept</c> header values.
 * 	<li>
 * 		Sets common properties on all serializers in a single method call.
 * 	<li>
 * 		Clones existing groups and all serializers within the group in a single method call.
 * </ul>
 *
 * <h5 class='topic'>Match ordering</h5>
 *
 * Serializers are matched against <c>Accept</c> strings in the order they exist in this group.
 *
 * <p>
 * Adding new entries will cause the entries to be prepended to the group.
 * This allows for previous serializers to be overridden through subsequent calls.
 *
 * <p>
 * For example, calling <code>g.append(S1.<jk>class</jk>,S2.<jk>class</jk>).append(S3.<jk>class</jk>,S4.<jk>class</jk>)</code>
 * will result in the order <c>S3, S4, S1, S2</c>.
 *
 * <h5 class='section'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Construct a new serializer group</jc>
 * 	SerializerGroup g = SerializerGroup.<jsm>create</jsm>();
 * 		.append(JsonSerializer.<jk>class</jk>, XmlSerializer.<jk>class</jk>); <jc>// Add some serializers to it</jc>
 * 		.ws().swaps(TemporalCalendarSwap.IsoLocalDateTime.<jk>class</jk>) <jc>// Change settings for all serializers in the group.</jc>
 * 		.build();
 *
 * 	<jc>// Find the appropriate serializer by Accept type</jc>
 * 	WriterSerializer s = g.getWriterSerializer(<js>"text/foo, text/json;q=0.8, text/*;q:0.6, *\/*;q=0.0"</js>);
 *
 * 	<jc>// Serialize a bean to JSON text </jc>
 * 	AddressBook addressBook = <jk>new</jk> AddressBook();  <jc>// Bean to serialize.</jc>
 * 	String json = s.serialize(addressBook);
 * </p>
 */
@ConfigurableContext(nocache=true)
public final class SerializerGroup {

	//-----------------------------------------------------------------------------------------------------------------
	// Static
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * An identifier that the previous entries in this group should be inherited.
	 * <p>
	 * Used by {@link Builder#set(Class...)}
	 */
	@SuppressWarnings("javadoc")
	public static abstract class Inherit extends Serializer {
		protected Inherit(SerializerBuilder builder) {
			super(builder);
		}
	}

	/**
	 * An identifier that the previous entries in this group should not be inherited.
	 * <p>
	 * Used by {@link Builder#add(Class...)}
	 */
	@SuppressWarnings("javadoc")
	public static abstract class NoInherit extends Serializer {
		protected NoInherit(SerializerBuilder builder) {
			super(builder);
		}
	}

	/**
	 * Instantiates a new clean-slate {@link Builder} object.
	 *
	 * @return A new {@link Builder} object.
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
	public static class Builder extends BeanBuilder<SerializerGroup> {

		List<Object> entries;
		private BeanContextBuilder bcBuilder;

		/**
		 * Create an empty serializer group builder.
		 */
		protected Builder() {
			super(SerializerGroup.class);
			this.entries = AList.create();
		}

		/**
		 * Clone an existing serializer group.
		 *
		 * @param copyFrom The serializer group that we're copying settings and serializers from.
		 */
		protected Builder(SerializerGroup copyFrom) {
			super(copyFrom.getClass());
			this.entries = AList.create().append(asList(copyFrom.entries));
		}

		/**
		 * Clone an existing serializer group builder.
		 *
		 * <p>
		 * Serializer builders will be cloned during this process.
		 *
		 * @param copyFrom The serializer group that we're copying settings and serializers from.
		 */
		protected Builder(Builder copyFrom) {
			super(copyFrom);
			bcBuilder = copyFrom.bcBuilder == null ? null : copyFrom.bcBuilder.copy();
			entries = AList.create();
			copyFrom.entries.stream().map(x -> copyBuilder(x)).forEach(x -> entries.add(x));
		}

		private Object copyBuilder(Object o) {
			if (o instanceof SerializerBuilder) {
				SerializerBuilder x = (SerializerBuilder)o;
				x = x.copy();
				if (bcBuilder != null)
					x.beanContext(bcBuilder);
				return x;
			}
			return o;
		}

		@Override /* BeanBuilder */
		protected SerializerGroup buildDefault() {
			return new SerializerGroup(this);
		}

		@Override /* BeanBuilder */
		public Builder copy() {
			return new Builder(this);
		}

		//-------------------------------------------------------------------------------------------------------------
		// Properties
		//-------------------------------------------------------------------------------------------------------------

		/**
		 * Associates an existing bean context builder with all serializer builders in this group.
		 *
		 * @param value The bean contest builder to associate.
		 * @return This object (for method chaining).
		 */
		public Builder beanContext(BeanContextBuilder value) {
			bcBuilder = value;
			forEach(x -> x.beanContext(value));
			return this;
		}

		/**
		 * Applies an operation to the bean context builder.
		 *
		 * @param operation The operation to apply.
		 * @return This object.
		 */
		public final Builder beanContext(Consumer<BeanContextBuilder> operation) {
			if (bcBuilder != null)
				operation.accept(bcBuilder);
			return this;
		}

		/**
		 * Adds the specified serializers to this group.
		 *
		 * <p>
		 * Entries are added in-order to the beginning of the list in the group.
		 *
		 * <p>
		 * The {@link NoInherit} class can be used to clear out the existing list of serializers before adding the new entries.
		 *
		 * <h5 class='section'>Example:</h5>
		 * <p class='bcode w800'>
		 * 	SerializerGroup.Builder <jv>builder</jv> = SerializerGroup.<jsm>create</jsm>();  <jc>// Create an empty builder.</jc>
		 *
		 * 	<jv>builder</jv>.add(FooSerializer.<jk>class</jk>);  <jc>// Now contains:  [FooSerializer]</jc>
		 *
		 * 	<jv>builder</jv>.add(BarSerializer.<jk>class</jk>, BazSerializer.<jk>class</jk>);  <jc>// Now contains:  [BarParser,BazSerializer,FooSerializer]</jc>
		 *
		 * 	<jv>builder</jv>.add(NoInherit.<jk>class</jk>, QuxSerializer.<jk>class</jk>);  <jc>// Now contains:  [QuxSerializer]</jc>
		 * </p>
		 *
		 * @param values The serializers to add to this group.
		 * @return This object (for method chaining).
		 * @throws IllegalArgumentException If one or more values do not extend from {@link Serializer}.
		 */
		public Builder add(Class<?>...values) {
			List<Object> l = new ArrayList<>();
			for (Class<?> e : values) {
				if (Serializer.class.isAssignableFrom(e)) {
					l.add(createBuilder(e));
				} else {
					throw runtimeException("Invalid type passed to SerializeGroup.Builder.add(): " + e.getName());
				}
			}
			entries.addAll(0, l);
			return this;
		}

		/**
		 * Sets the specified serializers for this group.
		 *
		 * <p>
		 * Existing values are overwritten.
		 *
		 * <p>
		 * The {@link Inherit} class can be used to insert existing entries in this group into the position specified.
		 *
		 * <h5 class='section'>Example:</h5>
		 * <p class='bcode w800'>
		 * 	SerializerGroup.Builder <jv>builder</jv> = SerializerGroup.<jsm>create</jsm>();  <jc>// Create an empty builder.</jc>
		 *
		 * 	<jv>builder</jv>.set(FooSerializer.<jk>class</jk>);  <jc>// Now contains:  [FooSerializer]</jc>
		 *
		 * 	<jv>builder</jv>.set(BarSerializer.<jk>class</jk>, BazSerializer.<jk>class</jk>);  <jc>// Now contains:  [BarParser,BazSerializer]</jc>
		 *
		 * 	<jv>builder</jv>.set(Inherit.<jk>class</jk>, QuxSerializer.<jk>class</jk>);  <jc>// Now contains:  [BarParser,BazSerializer,QuxSerializer]</jc>
		 * </p>
		 *
		 * @param values The serializers to set in this group.
		 * @return This object (for method chaining).
		 * @throws IllegalArgumentException If one or more values do not extend from {@link Serializer} or named <js>"Inherit"</js>.
		 */
		public Builder set(Class<?>...values) {
			List<Object> l = new ArrayList<>();
			for (Class<?> e : values) {
				if (e.getSimpleName().equals("Inherit")) {
					l.addAll(entries);
				} else if (Serializer.class.isAssignableFrom(e)) {
					l.add(createBuilder(e));
				} else {
					throw runtimeException("Invalid type passed to SerializeGroup.Builder.set(): " + e.getName());
				}
			}
			entries = l;
			return this;
		}

		private Object createBuilder(Object o) {
			if (o instanceof Class) {
				@SuppressWarnings("unchecked")
				SerializerBuilder b = Serializer.createSerializerBuilder((Class<? extends Serializer>)o);
				if (bcBuilder != null)
					b.beanContext(bcBuilder);
				o = b;
			}
			return o;
		}

		/**
		 * Registers the specified serializers with this group.
		 *
		 * <p>
		 * When passing in pre-instantiated serializers to this group, applying properties and transforms to the group
		 * do not affect them.
		 *
		 * @param s The serializers to append to this group.
		 * @return This object (for method chaining).
		 */
		public Builder add(Serializer...s) {
			entries.addAll(0, asList(s));
			return this;
		}

		/**
		 * Clears out any existing serializers in this group.
		 *
		 * @return This object (for method chaining).
		 */
		public Builder clear() {
			entries.clear();
			return this;
		}

		/**
		 * Returns <jk>true</jk> if at least one of the specified annotations can be applied to at least one serializer builder in this group.
		 *
		 * @param work The work to check.
		 * @return <jk>true</jk> if at least one of the specified annotations can be applied to at least one serializer builder in this group.
		 */
		public boolean canApply(AnnotationWorkList work) {
			for (Object o : entries)
				if (o instanceof SerializerBuilder)
					if (((SerializerBuilder)o).canApply(work))
						return true;
			return false;
		}

		/**
		 * Applies the specified annotations to all applicable serializer builders in this group.
		 *
		 * @param work The annotations to apply.
		 * @return This object (for method chaining).
		 */
		public Builder apply(AnnotationWorkList work) {
			return forEach(x -> x.apply(work));
		}

		/**
		 * Performs an action on all serializer builders in this group.
		 *
		 * @param action The action to perform.
		 * @return This object (for method chaining).
		 */
		public Builder forEach(Consumer<SerializerBuilder> action) {
			builders(SerializerBuilder.class).forEach(action);
			return this;
		}

		/**
		 * Performs an action on all writer serializer builders in this group.
		 *
		 * @param action The action to perform.
		 * @return This object (for method chaining).
		 */
		public Builder forEachWS(Consumer<WriterSerializerBuilder> action) {
			return forEach(WriterSerializerBuilder.class, action);
		}

		/**
		 * Performs an action on all output stream serializer builders in this group.
		 *
		 * @param action The action to perform.
		 * @return This object (for method chaining).
		 */
		public Builder forEachOSS(Consumer<OutputStreamSerializerBuilder> action) {
			return forEach(OutputStreamSerializerBuilder.class, action);
		}

		/**
		 * Performs an action on all serializer builders of the specified type in this group.
		 *
		 * @param type The serializer builder type.
		 * @param action The action to perform.
		 * @return This object (for method chaining).
		 */
		public <T extends SerializerBuilder> Builder forEach(Class<T> type, Consumer<T> action) {
			builders(type).forEach(action);
			return this;
		}

		/**
		 * Returns direct access to the {@link Serializer} and {@link SerializerBuilder} objects in this builder.
		 *
		 * <p>
		 * Provided to allow for any extraneous modifications to the list not accomplishable via other methods on this builder such
		 * as re-ordering/adding/removing entries.
		 *
		 * <p>
		 * Note that it is up to the user to ensure that the list only contains {@link Serializer} and {@link SerializerBuilder} objects.
		 *
		 * @return The inner list of entries in this builder.
		 */
		public List<Object> inner() {
			return entries;
		}

		@SuppressWarnings("unchecked")
		private <T extends SerializerBuilder> Stream<T> builders(Class<T> type) {
			return entries.stream().filter(x -> type.isInstance(x)).map(x -> (T)x);
		}

		// <FluentSetters>

		@Override /* BeanBuilder */
		public Builder type(Class<? extends SerializerGroup> value) {
			super.type(value);
			return this;
		}

		@Override /* BeanBuilder */
		public Builder impl(SerializerGroup value) {
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

		//-------------------------------------------------------------------------------------------------------------
		// Other methods
		//-------------------------------------------------------------------------------------------------------------

		@Override /* Object */
		public String toString() {
			return entries.stream().map(x -> toString(x)).collect(joining(",","[","]"));
		}

		private String toString(Object o) {
			if (o == null)
				return "null";
			if (o instanceof SerializerBuilder)
				return "builder:" + o.getClass().getName();
			return "serializer:" + o.getClass().getName();
		}
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Instance
	//-----------------------------------------------------------------------------------------------------------------

	// Maps Accept headers to matching serializers.
	private final ConcurrentHashMap<String,SerializerMatch> cache = new ConcurrentHashMap<>();

	private final List<MediaRange> mediaRanges;
	private final List<Serializer> mediaTypeRangeSerializers;

	private final List<MediaType> mediaTypesList;
	final Serializer[] entries;

	/**
	 * Constructor.
	 *
	 * @param builder The builder for this bean.
	 */
	protected SerializerGroup(Builder builder) {

		this.entries = builder.entries.stream().map(x -> build(x)).toArray(Serializer[]::new);

		AList<MediaRange> lmtr = AList.create();
		ASet<MediaType> lmt = ASet.of();
		AList<Serializer> l = AList.create();
		for (Serializer e : entries) {
			for (MediaRange m: e.getMediaTypeRanges().getRanges()) {
				lmtr.add(m);
				l.add(e);
			}
			for (MediaType mt : e.getAcceptMediaTypes())
				lmt.add(mt);
		}

		this.mediaRanges = lmtr.unmodifiable();
		this.mediaTypesList = AList.of(lmt).unmodifiable();
		this.mediaTypeRangeSerializers = l.unmodifiable();
	}

	private Serializer build(Object o) {
		if (o instanceof Serializer)
			return (Serializer)o;
		return ((SerializerBuilder)o).build();
	}

	/**
	 * Creates a copy of this serializer group.
	 *
	 * @return A new copy of this serializer group.
	 */
	public Builder copy() {
		return new Builder(this);
	}

	/**
	 * Searches the group for a serializer that can handle the specified <c>Accept</c> value.
	 *
	 * <p>
	 * The <c>accept</c> value complies with the syntax described in RFC2616, Section 14.1, as described below:
	 * <p class='bcode w800'>
	 * 	Accept         = "Accept" ":"
	 * 	                  #( media-range [ accept-params ] )
	 *
	 * 	media-range    = ( "*\/*"
	 * 	                  | ( type "/" "*" )
	 * 	                  | ( type "/" subtype )
	 * 	                  ) *( ";" parameter )
	 * 	accept-params  = ";" "q" "=" qvalue *( accept-extension )
	 * 	accept-extension = ";" token [ "=" ( token | quoted-string ) ]
	 * </p>
	 *
	 * <p>
	 * The returned object includes both the serializer and media type that matched.
	 *
	 * @param acceptHeader The HTTP <l>Accept</l> header string.
	 * @return The serializer and media type that matched the accept header, or <jk>null</jk> if no match was made.
	 */
	public SerializerMatch getSerializerMatch(String acceptHeader) {
		if (acceptHeader == null)
			return null;
		SerializerMatch sm = cache.get(acceptHeader);
		if (sm != null)
			return sm;

		Accept a = accept(acceptHeader);
		int match = a.match(mediaRanges);
		if (match >= 0) {
			sm = new SerializerMatch(mediaRanges.get(match), mediaTypeRangeSerializers.get(match));
			cache.putIfAbsent(acceptHeader, sm);
		}

		return cache.get(acceptHeader);
	}

	/**
	 * Same as {@link #getSerializerMatch(String)} but matches using a {@link MediaType} instance.
	 *
	 * @param mediaType The HTTP media type.
	 * @return The serializer and media type that matched the media type, or <jk>null</jk> if no match was made.
	 */
	public SerializerMatch getSerializerMatch(MediaType mediaType) {
		return getSerializerMatch(mediaType.toString());
	}

	/**
	 * Same as {@link #getSerializerMatch(String)} but returns just the matched serializer.
	 *
	 * @param acceptHeader The HTTP <l>Accept</l> header string.
	 * @return The serializer that matched the accept header, or <jk>null</jk> if no match was made.
	 */
	public Serializer getSerializer(String acceptHeader) {
		SerializerMatch sm = getSerializerMatch(acceptHeader);
		return sm == null ? null : sm.getSerializer();
	}

	/**
	 * Same as {@link #getSerializerMatch(MediaType)} but returns just the matched serializer.
	 *
	 * @param mediaType The HTTP media type.
	 * @return The serializer that matched the accept header, or <jk>null</jk> if no match was made.
	 */
	public Serializer getSerializer(MediaType mediaType) {
		if (mediaType == null)
			return null;
		return getSerializer(mediaType.toString());
	}

	/**
	 * Same as {@link #getSerializer(String)}, but casts it to a {@link WriterSerializer}.
	 *
	 * @param acceptHeader The HTTP <l>Accept</l> header string.
	 * @return The serializer that matched the accept header, or <jk>null</jk> if no match was made.
	 */
	public WriterSerializer getWriterSerializer(String acceptHeader) {
		return (WriterSerializer)getSerializer(acceptHeader);
	}

	/**
	 * Same as {@link #getSerializer(MediaType)}, but casts it to a {@link WriterSerializer}.
	 *
	 * @param mediaType The HTTP media type.
	 * @return The serializer that matched the accept header, or <jk>null</jk> if no match was made.
	 */
	public WriterSerializer getWriterSerializer(MediaType mediaType) {
		return (WriterSerializer)getSerializer(mediaType);
	}

	/**
	 * Same as {@link #getSerializer(String)}, but casts it to an {@link OutputStreamSerializer}.
	 *
	 * @param acceptHeader The HTTP <l>Accept</l> header string.
	 * @return The serializer that matched the accept header, or <jk>null</jk> if no match was made.
	 */
	public OutputStreamSerializer getStreamSerializer(String acceptHeader) {
		return (OutputStreamSerializer)getSerializer(acceptHeader);
	}

	/**
	 * Same as {@link #getSerializer(MediaType)}, but casts it to a {@link OutputStreamSerializer}.
	 *
	 * @param mediaType The HTTP media type.
	 * @return The serializer that matched the accept header, or <jk>null</jk> if no match was made.
	 */
	public OutputStreamSerializer getStreamSerializer(MediaType mediaType) {
		return (OutputStreamSerializer)getSerializer(mediaType);
	}

	/**
	 * Returns the media types that all serializers in this group can handle.
	 *
	 * <p>
	 * Entries are ordered in the same order as the serializers in the group.
	 *
	 * @return An unmodifiable list of media types.
	 */
	public List<MediaType> getSupportedMediaTypes() {
		return mediaTypesList;
	}

	/**
	 * Returns a copy of the serializers in this group.
	 *
	 * @return An unmodifiable list of serializers in this group.
	 */
	public List<Serializer> getSerializers() {
		return unmodifiableList(asList(entries));
	}

	/**
	 * Returns <jk>true</jk> if this group contains no serializers.
	 *
	 * @return <jk>true</jk> if this group contains no serializers.
	 */
	public boolean isEmpty() {
		return entries.length == 0;
	}
}
