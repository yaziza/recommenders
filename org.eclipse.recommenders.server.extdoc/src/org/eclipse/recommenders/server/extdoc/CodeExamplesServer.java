/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.server.extdoc;

import com.sun.jersey.api.client.GenericType;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.internal.server.extdoc.AbstractRatingsServer;
import org.eclipse.recommenders.internal.server.extdoc.Server;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.server.extdoc.types.CodeExamples;

public final class CodeExamplesServer extends AbstractRatingsServer {

    private static final String PROVIDERID = CodeExamples.class.getSimpleName();

    public CodeExamples getOverridenMethodCodeExamples(final IMethod method) {
        final String key = JavaElementResolver.INSTANCE.toRecMethod(method).getIdentifier();
        final CodeExamples result = Server.getProviderContent(PROVIDERID, "method", key,
                new GenericType<GenericResultObjectView<CodeExamples>>() {
                });
        return result;
    }

    public CodeExamples getTypeCodeExamples(final IType type) {
        final String key = JavaElementResolver.INSTANCE.toRecType(type).getIdentifier();
        final CodeExamples result = Server.getProviderContent(PROVIDERID, "type", key,
                new GenericType<GenericResultObjectView<CodeExamples>>() {
                });
        return result;
    }

    @Override
    protected String getDocumentId(final Object object) {
        // TODO Auto-generated method stub
        return null;
    }

}
