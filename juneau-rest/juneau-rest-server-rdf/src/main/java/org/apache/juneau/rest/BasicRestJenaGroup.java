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

import javax.servlet.http.HttpServlet;

import org.apache.juneau.jena.*;
import org.apache.juneau.rest.annotation.*;

/**
 * Identical to {@link BasicRestServletJenaGroup} but doesn't extend from {@link HttpServlet}
 */
@SuppressWarnings("serial")
@Rest(
	serializers={
		RdfXmlSerializer.class,
		RdfXmlAbbrevSerializer.class,
		TurtleSerializer.class,
		NTripleSerializer.class,
		N3Serializer.class
	},
	parsers={
		Inherit.class,
		RdfXmlParser.class,
		TurtleParser.class,
		NTripleParser.class,
		N3Parser.class
	}
)
public abstract class BasicRestJenaGroup extends BasicRestServletGroup {}
