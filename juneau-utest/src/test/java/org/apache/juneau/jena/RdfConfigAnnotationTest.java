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
package org.apache.juneau.jena;

import static org.junit.Assert.*;
import static org.junit.runners.MethodSorters.*;
import static org.apache.juneau.jena.RdfSerializer.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.apache.juneau.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.jena.annotation.*;
import org.apache.juneau.reflect.*;
import org.apache.juneau.svl.*;
import org.junit.*;

/**
 * Tests the @RdfConfig annotation.
 */
@FixMethodOrder(NAME_ASCENDING)
public class RdfConfigAnnotationTest {

	private static void check(String expected, Object o) {
		assertEquals(expected, TO_STRING.apply(o));
	}

	private static final Function<Object,String> TO_STRING = new Function<Object,String>() {
		@Override
		public String apply(Object t) {
			if (t == null)
				return null;
			if (t instanceof List)
				return ((List<?>)t)
					.stream()
					.map(TO_STRING)
					.collect(Collectors.joining(","));
			if (t.getClass().isArray())
				return apply(ArrayUtils.toList(t, Object.class));
			return t.toString();
		}
	};

	static VarResolverSession sr = VarResolver.create().vars(XVar.class).build().createSession();

	//-----------------------------------------------------------------------------------------------------------------
	// Basic tests
	//-----------------------------------------------------------------------------------------------------------------

	@RdfConfig(
		addBeanTypes="$X{true}",
		addLiteralTypes="$X{true}",
		addRootProperty="$X{true}",
		arp_embedding="$X{true}",
		arp_errorMode="$X{strict}",
		arp_iriRules="$X{strict}",
		disableAutoDetectNamespaces="$X{true}",
		collectionFormat="$X{SEQ}",
		juneauBpNs="$X{foo:http://foo}",
		juneauNs="$X{foo:http://foo}",
		language="$X{N3}",
		looseCollections="$X{true}",
		n3_disableAbbrevBaseUri="$X{true}",
		n3_indentProperty="$X{1}",
		n3_minGap="$X{1}",
		n3_disableObjectLists="$X{true}",
		n3_propertyColumn="$X{1}",
		n3_subjectColumn="$X{1}",
		n3_disableUseDoubles="$X{true}",
		n3_disableUsePropertySymbols="$X{true}",
		n3_disableUseTripleQuotedStrings="$X{true}",
		n3_widePropertyLen="$X{1}",
		namespaces="$X{foo:http://foo}",
		rdfxml_allowBadUris="$X{true}",
		rdfxml_attributeQuoteChar="$X{'}",
		rdfxml_blockRules="$X{foo}",
		rdfxml_longId="$X{true}",
		rdfxml_relativeUris="$X{absolute}",
		rdfxml_disableShowDoctypeDeclaration="$X{true}",
		rdfxml_showXmlDeclaration="$X{true}",
		rdfxml_tab="$X{1}",
		rdfxml_xmlBase="$X{foo}",
		trimWhitespace="$X{true}",
		disableUseXmlNamespaces="$X{true}"
	)
	static class A {}
	static ClassInfo a = ClassInfo.of(A.class);

	@Test
	public void basicSerializer() throws Exception {
		AnnotationWorkList al = a.getAnnotationList().getWork(sr);
		RdfSerializerSession x = RdfSerializer.create().apply(al).build().createSession();
		check("true", x.isAddBeanTypes());
		check("true", x.isAddLiteralTypes());
		check("true", x.isAddRootProp());
		check("false", x.isAutoDetectNamespaces());
		check("SEQ", x.getCollectionFormat());
		check("foo:http://foo", x.getJuneauBpNs());
		check("foo:http://foo", x.getJuneauNs());
		check("N3", x.getLanguage());
		check("true", x.isLooseCollections());
		check("foo:http://foo", x.getNamespaces());
		check("false", x.isUseXmlNamespaces());

		Map<String,Object> jp = x.getJenaProperties();
		check("true", jp.get(RDF_arp_embedding.substring(15)));
		check("strict", jp.get(RDF_arp_errorMode.substring(15)));
		check("strict", jp.get(RDF_arp_iriRules.substring(15)));
		check("true", jp.get(RDF_n3_disableAbbrevBaseUri.substring(15)));
		check("1", jp.get(RDF_n3_indentProperty.substring(15)));
		check("1", jp.get(RDF_n3_minGap.substring(15)));
		check("true", jp.get(RDF_n3_disableObjectLists.substring(15)));
		check("1", jp.get(RDF_n3_propertyColumn.substring(15)));
		check("1", jp.get(RDF_n3_subjectColumn.substring(15)));
		check("true", jp.get(RDF_n3_disableUseDoubles.substring(15)));
		check("true", jp.get(RDF_n3_disableUsePropertySymbols.substring(15)));
		check("true", jp.get(RDF_n3_disableUseTripleQuotedStrings.substring(15)));
		check("1", jp.get(RDF_n3_widePropertyLen.substring(15)));
		check("true", jp.get(RDF_rdfxml_allowBadUris.substring(15)));
		check("'", jp.get(RDF_rdfxml_attributeQuoteChar.substring(15)));
		check("foo", jp.get(RDF_rdfxml_blockRules.substring(15)));
		check("true", jp.get(RDF_rdfxml_longId.substring(15)));
		check("absolute", jp.get(RDF_rdfxml_relativeUris.substring(15)));
		check("true", jp.get(RDF_rdfxml_disableShowDoctypeDeclaration.substring(15)));
		check("true", jp.get(RDF_rdfxml_showXmlDeclaration.substring(15)));
		check("1", jp.get(RDF_rdfxml_tab.substring(15)));
		check("foo", jp.get(RDF_rdfxml_xmlBase.substring(15)));
	}

	@Test
	public void basicParser() throws Exception {
		AnnotationWorkList al = a.getAnnotationList().getWork(sr);
		RdfParserSession x = RdfParser.create().apply(al).build().createSession();
		check("SEQ", x.getCollectionFormat());
		check("foo:http://foo", x.getJuneauBpNs());
		check("foo:http://foo", x.getJuneauNs());
		check("N3", x.getLanguage());
		check("true", x.isLooseCollections());
		check("true", x.isTrimWhitespace());

		Map<String,Object> jp = x.getJenaProperties();
		check("true", jp.get(RDF_arp_embedding.substring(15)));
		check("strict", jp.get(RDF_arp_errorMode.substring(15)));
		check("strict", jp.get(RDF_arp_iriRules.substring(15)));
		check("true", jp.get(RDF_n3_disableAbbrevBaseUri.substring(15)));
		check("1", jp.get(RDF_n3_indentProperty.substring(15)));
		check("1", jp.get(RDF_n3_minGap.substring(15)));
		check("true", jp.get(RDF_n3_disableObjectLists.substring(15)));
		check("1", jp.get(RDF_n3_propertyColumn.substring(15)));
		check("1", jp.get(RDF_n3_subjectColumn.substring(15)));
		check("true", jp.get(RDF_n3_disableUseDoubles.substring(15)));
		check("true", jp.get(RDF_n3_disableUsePropertySymbols.substring(15)));
		check("true", jp.get(RDF_n3_disableUseTripleQuotedStrings.substring(15)));
		check("1", jp.get(RDF_n3_widePropertyLen.substring(15)));
		check("true", jp.get(RDF_rdfxml_allowBadUris.substring(15)));
		check("'", jp.get(RDF_rdfxml_attributeQuoteChar.substring(15)));
		check("foo", jp.get(RDF_rdfxml_blockRules.substring(15)));
		check("true", jp.get(RDF_rdfxml_longId.substring(15)));
		check("absolute", jp.get(RDF_rdfxml_relativeUris.substring(15)));
		check("true", jp.get(RDF_rdfxml_disableShowDoctypeDeclaration.substring(15)));
		check("true", jp.get(RDF_rdfxml_showXmlDeclaration.substring(15)));
		check("1", jp.get(RDF_rdfxml_tab.substring(15)));
		check("foo", jp.get(RDF_rdfxml_xmlBase.substring(15)));
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Annotation with no values.
	//-----------------------------------------------------------------------------------------------------------------

	@RdfConfig()
	static class B {}
	static ClassInfo b = ClassInfo.of(B.class);

	@Test
	public void noValuesSerializer() throws Exception {
		AnnotationWorkList al = b.getAnnotationList().getWork(sr);
		RdfSerializerSession x = RdfSerializer.create().apply(al).build().createSession();
		check("false", x.isAddBeanTypes());
		check("false", x.isAddLiteralTypes());
		check("false", x.isAddRootProp());
		check("true", x.isAutoDetectNamespaces());
		check("DEFAULT", x.getCollectionFormat());
		check("jp:http://www.apache.org/juneaubp/", x.getJuneauBpNs());
		check("j:http://www.apache.org/juneau/", x.getJuneauNs());
		check("RDF/XML-ABBREV", x.getLanguage());
		check("false", x.isLooseCollections());
		check("", x.getNamespaces());
		check("true", x.isUseXmlNamespaces());

		Map<String,Object> jp = x.getJenaProperties();
		check(null, jp.get(RDF_arp_embedding.substring(15)));
		check(null, jp.get(RDF_arp_errorMode.substring(15)));
		check(null, jp.get(RDF_arp_iriRules.substring(15)));
		check(null, jp.get(RDF_n3_disableAbbrevBaseUri.substring(15)));
		check(null, jp.get(RDF_n3_indentProperty.substring(15)));
		check(null, jp.get(RDF_n3_minGap.substring(15)));
		check(null, jp.get(RDF_n3_disableObjectLists.substring(15)));
		check(null, jp.get(RDF_n3_propertyColumn.substring(15)));
		check(null, jp.get(RDF_n3_subjectColumn.substring(15)));
		check(null, jp.get(RDF_n3_disableUseDoubles.substring(15)));
		check(null, jp.get(RDF_n3_disableUsePropertySymbols.substring(15)));
		check(null, jp.get(RDF_n3_disableUseTripleQuotedStrings.substring(15)));
		check(null, jp.get(RDF_n3_widePropertyLen.substring(15)));
		check(null, jp.get(RDF_rdfxml_allowBadUris.substring(15)));
		check(null, jp.get(RDF_rdfxml_attributeQuoteChar.substring(15)));
		check(null, jp.get(RDF_rdfxml_blockRules.substring(15)));
		check(null, jp.get(RDF_rdfxml_longId.substring(15)));
		check(null, jp.get(RDF_rdfxml_relativeUris.substring(15)));
		check(null, jp.get(RDF_rdfxml_disableShowDoctypeDeclaration.substring(15)));
		check(null, jp.get(RDF_rdfxml_showXmlDeclaration.substring(15)));
		check(null, jp.get(RDF_rdfxml_tab.substring(15)));
		check(null, jp.get(RDF_rdfxml_xmlBase.substring(15)));
	}

	@Test
	public void noValuesParser() throws Exception {
		AnnotationWorkList al = b.getAnnotationList().getWork(sr);
		RdfParserSession x = RdfParser.create().apply(al).build().createSession();
		check("DEFAULT", x.getCollectionFormat());
		check("jp:http://www.apache.org/juneaubp/", x.getJuneauBpNs());
		check("j:http://www.apache.org/juneau/", x.getJuneauNs());
		check("RDF/XML-ABBREV", x.getLanguage());
		check("false", x.isLooseCollections());
		check("false", x.isTrimWhitespace());

		Map<String,Object> jp = x.getJenaProperties();
		check(null, jp.get(RDF_arp_embedding.substring(15)));
		check(null, jp.get(RDF_arp_errorMode.substring(15)));
		check(null, jp.get(RDF_arp_iriRules.substring(15)));
		check(null, jp.get(RDF_n3_disableAbbrevBaseUri.substring(15)));
		check(null, jp.get(RDF_n3_indentProperty.substring(15)));
		check(null, jp.get(RDF_n3_minGap.substring(15)));
		check(null, jp.get(RDF_n3_disableObjectLists.substring(15)));
		check(null, jp.get(RDF_n3_propertyColumn.substring(15)));
		check(null, jp.get(RDF_n3_subjectColumn.substring(15)));
		check(null, jp.get(RDF_n3_disableUseDoubles.substring(15)));
		check(null, jp.get(RDF_n3_disableUsePropertySymbols.substring(15)));
		check(null, jp.get(RDF_n3_disableUseTripleQuotedStrings.substring(15)));
		check(null, jp.get(RDF_n3_widePropertyLen.substring(15)));
		check(null, jp.get(RDF_rdfxml_allowBadUris.substring(15)));
		check(null, jp.get(RDF_rdfxml_attributeQuoteChar.substring(15)));
		check(null, jp.get(RDF_rdfxml_blockRules.substring(15)));
		check(null, jp.get(RDF_rdfxml_longId.substring(15)));
		check(null, jp.get(RDF_rdfxml_relativeUris.substring(15)));
		check(null, jp.get(RDF_rdfxml_disableShowDoctypeDeclaration.substring(15)));
		check(null, jp.get(RDF_rdfxml_showXmlDeclaration.substring(15)));
		check(null, jp.get(RDF_rdfxml_tab.substring(15)));
		check(null, jp.get(RDF_rdfxml_xmlBase.substring(15)));
	}

	//-----------------------------------------------------------------------------------------------------------------
	// No annotation.
	//-----------------------------------------------------------------------------------------------------------------

	static class C {}
	static ClassInfo c = ClassInfo.of(C.class);

	@Test
	public void noAnnotationSerializer() throws Exception {
		AnnotationWorkList al = c.getAnnotationList().getWork(sr);
		RdfSerializerSession x = RdfSerializer.create().apply(al).build().createSession();
		check("false", x.isAddBeanTypes());
		check("false", x.isAddLiteralTypes());
		check("false", x.isAddRootProp());
		check("true", x.isAutoDetectNamespaces());
		check("DEFAULT", x.getCollectionFormat());
		check("jp:http://www.apache.org/juneaubp/", x.getJuneauBpNs());
		check("j:http://www.apache.org/juneau/", x.getJuneauNs());
		check("RDF/XML-ABBREV", x.getLanguage());
		check("false", x.isLooseCollections());
		check("", x.getNamespaces());
		check("true", x.isUseXmlNamespaces());

		Map<String,Object> jp = x.getJenaProperties();
		check(null, jp.get(RDF_arp_embedding.substring(15)));
		check(null, jp.get(RDF_arp_errorMode.substring(15)));
		check(null, jp.get(RDF_arp_iriRules.substring(15)));
		check(null, jp.get(RDF_n3_disableAbbrevBaseUri.substring(15)));
		check(null, jp.get(RDF_n3_indentProperty.substring(15)));
		check(null, jp.get(RDF_n3_minGap.substring(15)));
		check(null, jp.get(RDF_n3_disableObjectLists.substring(15)));
		check(null, jp.get(RDF_n3_propertyColumn.substring(15)));
		check(null, jp.get(RDF_n3_subjectColumn.substring(15)));
		check(null, jp.get(RDF_n3_disableUseDoubles.substring(15)));
		check(null, jp.get(RDF_n3_disableUsePropertySymbols.substring(15)));
		check(null, jp.get(RDF_n3_disableUseTripleQuotedStrings.substring(15)));
		check(null, jp.get(RDF_n3_widePropertyLen.substring(15)));
		check(null, jp.get(RDF_rdfxml_allowBadUris.substring(15)));
		check(null, jp.get(RDF_rdfxml_attributeQuoteChar.substring(15)));
		check(null, jp.get(RDF_rdfxml_blockRules.substring(15)));
		check(null, jp.get(RDF_rdfxml_longId.substring(15)));
		check(null, jp.get(RDF_rdfxml_relativeUris.substring(15)));
		check(null, jp.get(RDF_rdfxml_disableShowDoctypeDeclaration.substring(15)));
		check(null, jp.get(RDF_rdfxml_showXmlDeclaration.substring(15)));
		check(null, jp.get(RDF_rdfxml_tab.substring(15)));
		check(null, jp.get(RDF_rdfxml_xmlBase.substring(15)));
	}

	@Test
	public void noAnnotationParser() throws Exception {
		AnnotationWorkList al = c.getAnnotationList().getWork(sr);
		RdfParserSession x = RdfParser.create().apply(al).build().createSession();
		check("DEFAULT", x.getCollectionFormat());
		check("jp:http://www.apache.org/juneaubp/", x.getJuneauBpNs());
		check("j:http://www.apache.org/juneau/", x.getJuneauNs());
		check("RDF/XML-ABBREV", x.getLanguage());
		check("false", x.isLooseCollections());
		check("false", x.isTrimWhitespace());

		Map<String,Object> jp = x.getJenaProperties();
		check(null, jp.get(RDF_arp_embedding.substring(15)));
		check(null, jp.get(RDF_arp_errorMode.substring(15)));
		check(null, jp.get(RDF_arp_iriRules.substring(15)));
		check(null, jp.get(RDF_n3_disableAbbrevBaseUri.substring(15)));
		check(null, jp.get(RDF_n3_indentProperty.substring(15)));
		check(null, jp.get(RDF_n3_minGap.substring(15)));
		check(null, jp.get(RDF_n3_disableObjectLists.substring(15)));
		check(null, jp.get(RDF_n3_propertyColumn.substring(15)));
		check(null, jp.get(RDF_n3_subjectColumn.substring(15)));
		check(null, jp.get(RDF_n3_disableUseDoubles.substring(15)));
		check(null, jp.get(RDF_n3_disableUsePropertySymbols.substring(15)));
		check(null, jp.get(RDF_n3_disableUseTripleQuotedStrings.substring(15)));
		check(null, jp.get(RDF_n3_widePropertyLen.substring(15)));
		check(null, jp.get(RDF_rdfxml_allowBadUris.substring(15)));
		check(null, jp.get(RDF_rdfxml_attributeQuoteChar.substring(15)));
		check(null, jp.get(RDF_rdfxml_blockRules.substring(15)));
		check(null, jp.get(RDF_rdfxml_longId.substring(15)));
		check(null, jp.get(RDF_rdfxml_relativeUris.substring(15)));
		check(null, jp.get(RDF_rdfxml_disableShowDoctypeDeclaration.substring(15)));
		check(null, jp.get(RDF_rdfxml_showXmlDeclaration.substring(15)));
		check(null, jp.get(RDF_rdfxml_tab.substring(15)));
		check(null, jp.get(RDF_rdfxml_xmlBase.substring(15)));
	}
}
