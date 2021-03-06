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
package org.apache.juneau.urlencoding;

import java.util.*;
import java.util.concurrent.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.serializer.*;
import org.apache.juneau.uon.*;

/**
 * Serializes POJO models to URL-encoded notation with UON-encoded values (a notation for URL-encoded query paramter values).
 * {@review}
 *
 * <h5 class='section'>Media types:</h5>
 *
 * Handles <c>Accept</c> types:  <bc>application/x-www-form-urlencoded</bc>
 * <p>
 * Produces <c>Content-Type</c> types:  <bc>application/x-www-form-urlencoded</bc>
 *
 * <h5 class='topic'>Description</h5>
 *
 * This serializer provides several serialization options.
 * <br>Typically, one of the predefined DEFAULT serializers will be sufficient.
 * <br>However, custom serializers can be constructed to fine-tune behavior.
 *
 * <p>
 * The following shows a sample object defined in Javascript:
 * <p class='bcode w800'>
 * 	{
 * 		id: 1,
 * 		name: <js>'John Smith'</js>,
 * 		uri: <js>'http://sample/addressBook/person/1'</js>,
 * 		addressBookUri: <js>'http://sample/addressBook'</js>,
 * 		birthDate: <js>'1946-08-12T00:00:00Z'</js>,
 * 		otherIds: <jk>null</jk>,
 * 		addresses: [
 * 			{
 * 				uri: <js>'http://sample/addressBook/address/1'</js>,
 * 				personUri: <js>'http://sample/addressBook/person/1'</js>,
 * 				id: 1,
 * 				street: <js>'100 Main Street'</js>,
 * 				city: <js>'Anywhereville'</js>,
 * 				state: <js>'NY'</js>,
 * 				zip: 12345,
 * 				isCurrent: <jk>true</jk>,
 * 			}
 * 		]
 * 	}
 * </p>
 *
 * <p>
 * Using the "strict" syntax defined in this document, the equivalent URL-encoded notation would be as follows:
 * <p class='bcode w800'>
 * 	<ua>id</ua>=<un>1</un>
 * 	&amp;<ua>name</ua>=<us>'John+Smith'</us>,
 * 	&amp;<ua>uri</ua>=<us>http://sample/addressBook/person/1</us>,
 * 	&amp;<ua>addressBookUri</ua>=<us>http://sample/addressBook</us>,
 * 	&amp;<ua>birthDate</ua>=<us>1946-08-12T00:00:00Z</us>,
 * 	&amp;<ua>otherIds</ua>=<uk>null</uk>,
 * 	&amp;<ua>addresses</ua>=@(
 * 		(
 * 			<ua>uri</ua>=<us>http://sample/addressBook/address/1</us>,
 * 			<ua>personUri</ua>=<us>http://sample/addressBook/person/1</us>,
 * 			<ua>id</ua>=<un>1</un>,
 * 			<ua>street</ua>=<us>'100+Main+Street'</us>,
 * 			<ua>city</ua>=<us>Anywhereville</us>,
 * 			<ua>state</ua>=<us>NY</us>,
 * 			<ua>zip</ua>=<un>12345</un>,
 * 			<ua>isCurrent</ua>=<uk>true</uk>
 * 		)
 * 	)
 * </p>
 *
 * <h5 class='section'>Example:</h5>
 * <p class='bcode w800'>
 * 	<jc>// Serialize a Map</jc>
 * 	Map m = OMap.<jsm>ofJson</jsm>(<js>"{a:'b',c:1,d:false,e:['f',1,false],g:{h:'i'}}"</js>);
 *
 * 	<jc>// Serialize to value equivalent to JSON.</jc>
 * 	<jc>// Produces "a=b&amp;c=1&amp;d=false&amp;e=@(f,1,false)&amp;g=(h=i)"</jc>
 * 	String s = UrlEncodingSerializer.<jsf>DEFAULT</jsf>.serialize(s);
 *
 * 	<jc>// Serialize a bean</jc>
 * 	<jk>public class</jk> Person {
 * 		<jk>public</jk> Person(String s);
 * 		<jk>public</jk> String getName();
 * 		<jk>public int</jk> getAge();
 * 		<jk>public</jk> Address getAddress();
 * 		<jk>public boolean</jk> deceased;
 * 	}
 *
 * 	<jk>public class</jk> Address {
 * 		<jk>public</jk> String getStreet();
 * 		<jk>public</jk> String getCity();
 * 		<jk>public</jk> String getState();
 * 		<jk>public int</jk> getZip();
 * 	}
 *
 * 	Person p = <jk>new</jk> Person(<js>"John Doe"</js>, 23, <js>"123 Main St"</js>, <js>"Anywhere"</js>, <js>"NY"</js>, 12345, <jk>false</jk>);
 *
 * 	<jc>// Produces "name=John+Doe&amp;age=23&amp;address=(street='123+Main+St',city=Anywhere,state=NY,zip=12345)&amp;deceased=false"</jc>
 * 	String s = UrlEncodingSerializer.<jsf>DEFAULT</jsf>.serialize(s);
 * </p>
 */
@ConfigurableContext
public class UrlEncodingSerializer extends UonSerializer implements UrlEncodingMetaProvider, UrlEncodingCommon {

	//-------------------------------------------------------------------------------------------------------------------
	// Configurable properties
	//-------------------------------------------------------------------------------------------------------------------

	static final String PREFIX = "UrlEncodingSerializer";

	/**
	 * Configuration property:  Serialize bean property collections/arrays as separate key/value pairs.
	 *
	 * <p>
	 * If <jk>false</jk>, serializing the array <c>[1,2,3]</c> results in <c>?key=$a(1,2,3)</c>.
	 * <br>If <jk>true</jk>, serializing the same array results in <c>?key=1&amp;key=2&amp;key=3</c>.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.urlencoding.UrlEncodingSerializer#URLENC_expandedParams URLENC_expandedParams}
	 * 	<li><b>Name:</b>  <js>"UrlEncodingSerializer.expandedParams.b"</js>
	 * 	<li><b>Data type:</b>  <jk>boolean</jk>
	 * 	<li><b>System property:</b>  <c>UrlEncodingSerializer.expandedParams</c>
	 * 	<li><b>Environment variable:</b>  <c>URLENCODINGSERIALIZER_EXPANDEDPARAMS</c>
	 * 	<li><b>Default:</b>  <jk>false</jk>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.urlencoding.annotation.UrlEncodingConfig#expandedParams()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.urlencoding.UrlEncodingSerializerBuilder#expandedParams()}
	 * 		</ul>
	 * </ul>
	 */
	public static final String URLENC_expandedParams = PREFIX + ".expandedParams.b";


	//-------------------------------------------------------------------------------------------------------------------
	// Predefined instances
	//-------------------------------------------------------------------------------------------------------------------

	/** Reusable instance of {@link UrlEncodingSerializer}, all default settings. */
	public static final UrlEncodingSerializer DEFAULT = new UrlEncodingSerializer(create());

	/** Reusable instance of {@link UrlEncodingSerializer.PlainText}. */
	public static final UrlEncodingSerializer DEFAULT_PLAINTEXT = new PlainText(create());

	/** Reusable instance of {@link UrlEncodingSerializer.Expanded}. */
	public static final UrlEncodingSerializer DEFAULT_EXPANDED = new Expanded(create());

	/** Reusable instance of {@link UrlEncodingSerializer.Readable}. */
	public static final UrlEncodingSerializer DEFAULT_READABLE = new Readable(create());


	//-------------------------------------------------------------------------------------------------------------------
	// Predefined subclasses
	//-------------------------------------------------------------------------------------------------------------------

	/**
	 * Equivalent to <code>UrlEncodingSerializer.<jsm>create</jsm>().expandedParams().build();</code>.
	 */
	public static class Expanded extends UrlEncodingSerializer {

		/**
		 * Constructor.
		 *
		 * @param builder The builder for this object.
		 */
		protected Expanded(UrlEncodingSerializerBuilder builder) {
			super(builder.expandedParams());
		}
	}

	/**
	 * Equivalent to <code>UrlEncodingSerializer.<jsm>create</jsm>().useWhitespace().build();</code>.
	 */
	public static class Readable extends UrlEncodingSerializer {

		/**
		 * Constructor.
		 *
		 * @param builder The builder for this object.
		 */
		protected Readable(UrlEncodingSerializerBuilder builder) {
			super(builder.useWhitespace());
		}
	}

	/**
	 * Equivalent to <code>UrlEncodingSerializer.<jsm>create</jsm>().plainTextParts().build();</code>.
	 */
	public static class PlainText extends UrlEncodingSerializer {

		/**
		 * Constructor.
		 *
		 * @param builder The builder for this object.
		 */
		protected PlainText(UrlEncodingSerializerBuilder builder) {
			super(builder.paramFormatPlain());
		}
	}


	//-------------------------------------------------------------------------------------------------------------------
	// Instance
	//-------------------------------------------------------------------------------------------------------------------

	private final boolean
		expandedParams;
	private final Map<ClassMeta<?>,UrlEncodingClassMeta> urlEncodingClassMetas = new ConcurrentHashMap<>();
	private final Map<BeanPropertyMeta,UrlEncodingBeanPropertyMeta> urlEncodingBeanPropertyMetas = new ConcurrentHashMap<>();

	/**
	 * Constructor.
	 *
	 * @param builder The builder for this object.
	 */
	protected UrlEncodingSerializer(UrlEncodingSerializerBuilder builder) {
		super(builder.encoding());
		ContextProperties cp = getContextProperties();
		expandedParams = cp.getBoolean(URLENC_expandedParams).orElse(false);
	}

	@Override /* Context */
	public UrlEncodingSerializerBuilder copy() {
		return new UrlEncodingSerializerBuilder(this);
	}

	/**
	 * Instantiates a new clean-slate {@link UrlEncodingSerializerBuilder} object.
	 *
	 * <p>
	 * This is equivalent to simply calling <code><jk>new</jk> UrlEncodingSerializerBuilder()</code>.
	 *
	 * <p>
	 * Note that this method creates a builder initialized to all default settings, whereas {@link #copy()} copies
	 * the settings of the object called on.
	 *
	 * @return A new {@link UrlEncodingSerializerBuilder} object.
	 */
	public static UrlEncodingSerializerBuilder create() {
		return new UrlEncodingSerializerBuilder();
	}


	//-----------------------------------------------------------------------------------------------------------------
	// Entry point methods
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* Context */
	public UrlEncodingSerializerSession createSession() {
		return createSession(createDefaultSessionArgs());
	}

	@Override /* Serializer */
	public UrlEncodingSerializerSession createSession(SerializerSessionArgs args) {
		return new UrlEncodingSerializerSession(this, null, args);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Extended metadata
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* UrlEncodingMetaProvider */
	public UrlEncodingClassMeta getUrlEncodingClassMeta(ClassMeta<?> cm) {
		UrlEncodingClassMeta m = urlEncodingClassMetas.get(cm);
		if (m == null) {
			m = new UrlEncodingClassMeta(cm, this);
			urlEncodingClassMetas.put(cm, m);
		}
		return m;
	}

	@Override /* UrlEncodingMetaProvider */
	public UrlEncodingBeanPropertyMeta getUrlEncodingBeanPropertyMeta(BeanPropertyMeta bpm) {
		if (bpm == null)
			return UrlEncodingBeanPropertyMeta.DEFAULT;
		UrlEncodingBeanPropertyMeta m = urlEncodingBeanPropertyMetas.get(bpm);
		if (m == null) {
			m = new UrlEncodingBeanPropertyMeta(bpm.getDelegateFor(), this);
			urlEncodingBeanPropertyMetas.put(bpm, m);
		}
		return m;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Properties
	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Serialize bean property collections/arrays as separate key/value pairs.
	 *
	 * @see #URLENC_expandedParams
	 * @return
	 * 	<jk>false</jk> if serializing the array <c>[1,2,3]</c> results in <c>?key=$a(1,2,3)</c>.
	 * 	<br><jk>true</jk> if serializing the same array results in <c>?key=1&amp;key=2&amp;key=3</c>.
	 */
	protected final boolean isExpandedParams() {
		return expandedParams;
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Other methods
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* Context */
	public OMap toMap() {
		return super.toMap()
			.a(
				"UrlEncodingSerializer",
				OMap
					.create()
					.filtered()
					.a("expandedParams", expandedParams)
			);
	}
}
