/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.Expression;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class ExpressionPrinterTest {

    @Test
    public final void testPatternNamer() throws JavaModelException {
        final ExpressionFormatter printer = getExpressionPrinterMock();
        final Expression expression = AllTests.getDefaultExpression();
        Assert.assertEquals("someVariable = new null;", printer.format(expression));
    }

    public static final ExpressionFormatter getExpressionPrinterMock() throws JavaModelException {
        final MethodFormatter methodFormatter = Mockito.mock(MethodFormatter.class);
        Mockito.doReturn("...").when(methodFormatter).getParametersString(Matchers.any(IMethodName.class));
        return new ExpressionFormatter(methodFormatter);
    }
}
