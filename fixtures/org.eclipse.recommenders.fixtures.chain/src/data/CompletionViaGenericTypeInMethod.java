/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Kaluza, Marko Martin, Marcel Bruch - chain completion test scenario definitions 
 */
package data;

import java.util.Arrays;
import java.util.Iterator;

public class CompletionViaGenericTypeInMethod {

    public static void method() {
		final Iterator<CompletionViaGenericTypeInMethod> useMe = Arrays.asList(
				new CompletionViaGenericTypeInMethod()).iterator();
		final CompletionViaGenericTypeInMethod c = <@Ignore^Space>
		/*
		 * calling context --> static
		 * expected type --> CompletionViaGenericTypeInMethod
		 * expected completion --> useMe.next()
		 * variable name --> c
		 */
	}
}