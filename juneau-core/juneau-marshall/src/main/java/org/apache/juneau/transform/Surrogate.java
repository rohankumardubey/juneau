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
package org.apache.juneau.transform;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;

/**
 * Identifies a class as being a surrogate class.
 * 
 * <p>
 * Surrogate classes are used in place of other classes during serialization.
 * <br>For example, you may want to use a surrogate class to change the names or order of bean properties on a bean.
 * 
 * <p>
 * This interface has no methods to implement.
 * <br>It's simply used by the framework to identify the class as a surrogate class when specified as a swap.
 * 
 * <p>
 * The following is an example of a surrogate class change changes a property name:
 * <p class='bcode'>
 * 	<jk>public class</jk> MySurrogate <jk>implements</jk> Surrogate {
 * 
 * 		<jc>// Public constructor that wraps the normal object during serialization.</jc>
 * 		<jk>public</jk> MySurrogate(NormalClass o) {...}
 * 	
 * 		<jc>// Public no-arg constructor using during parsing.</jc>
 * 		<jc>// Not required if only used during serializing.</jc>
 * 		<jk>public</jk> MySurrogate() {...}
 * 
 * 		<jc>// Public method that converts surrogate back into normal object during parsing.</jc>
 * 		<jc>// The method name can be anything (e.g. "build", "create", etc...).</jc>
 * 		<jc>// Not required if only used during serializing.</jc>
 * 		<jk>public</jk> NormalClass unswap() {...}
 * 	}
 * </p>
 * 
 * <p>
 * Surrogate classes are associated with serializers and parsers using the {@link BeanContextBuilder#pojoSwaps(Class...)}
 * method.
 * <p class='bcode'>
 * 	JsonSerializer s = JsonSerializer
 * 		.<jsm>create</jsm>()
 * 		.pojoSwaps(MySurrogate.<jk>class</jk>)
 * 		.build();
 * 	
 * 	JsonParser p = JsonParser
 * 		.<jsm>create</jsm>()
 * 		.pojoSwaps(MySurrogate.<jk>class</jk>)
 * 		.build();
 * </p>
 * 
 * Surrogates can also be associated using the {@link Swap @Swap} annotation.
 * <p class='bcode'>
 * 	<ja>@Swap</ja>(MySurrogate.<jk>class</jk>)
 * 	<jk>public class</jk> NormalClass {...}
 * </p>
 * 
 * <p>
 * On a side note, a surrogate class is functionally equivalent to the following {@link PojoSwap}
 * implementation:
 * <p class='bcode'>
 * 	<jk>public class</jk> MySurrogate <jk>extends</jk> PojoSwap&lt;NormalClass,MySurrogate&gt; {
 * 		<jk>public</jk> MySurrogate swap(NormalClass o) <jk>throws</jk> SerializeException {
 * 			<jk>return new</jk> MySurrogate(o);
 * 		}
 * 		<jk>public</jk> NormalClass unswap(MySurrogate o, ClassMeta&lt;?&gt; hint) <jk>throws</jk> ParseException {
 * 			<jk>return</jk> o.unswap();
 * 		}
 * 	}
 * </p>
 * 
 * <h5 class='section'>See Also:</h5>
 * <ul>
 * 	<li class='link'><a class="doclink" href="../../../../overview-summary.html#juneau-marshall.SurrogateClasses">Overview &gt; SurrogateClasses</a>
 * </ul>
 */
public interface Surrogate {}
