/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package tracing;

import acme.Button;

public class Tracing__Calls_To_Local_With_Throws_In_Delegate {

    public void __test() {
        final Button s = new Button();
        throwsException();
        s.foo1();
    }

    private void throwsException() {
        throw new IllegalStateException();
    }
}