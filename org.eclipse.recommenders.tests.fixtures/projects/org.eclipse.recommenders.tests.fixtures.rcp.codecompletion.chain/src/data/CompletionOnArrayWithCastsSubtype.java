/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package data;

//call chain 1 ok
public class CompletionOnArrayWithCastsSubtype {
	public Number[][][] findme;
	
	public static void method1() {
		final CompletionOnArrayWithCastsSubtype obj = new CompletionOnArrayWithCastsSubtype();
        final Integer c = <@Ignore^Space>
        /* calling context --> static
         * expected completion --> (Integer) obj.findme[i][j][k]
         */
	}
	
	public static void method2() {
		final CompletionOnArrayWithCastsSubtype obj = new CompletionOnArrayWithCastsSubtype();
        final Integer[] c = <@Ignore^Space>
        /* calling context --> static
         * expected completion --> (Integer[]) obj.findme[i][j]
         */
	}
	
	public static void method3() {
		final CompletionOnArrayWithCastsSubtype obj = new CompletionOnArrayWithCastsSubtype();
        final Integer[][] c = <@Ignore^Space>
        /* calling context --> static
         * expected completion --> (Integer[][]) obj.findme[i]
         */
	}
	
	public static void method4() {
		final CompletionOnArrayWithCastsSubtype obj = new CompletionOnArrayWithCastsSubtype();
        final Integer[][][] c = <@Ignore^Space>
        /* calling context --> static
         * expected completion --> (Integer[][][]) obj.findme
         */
	}
}
