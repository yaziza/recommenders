/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.extdoc;

import java.util.Collection;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.rcp.extdoc.features.IRatingSummary;
import org.eclipse.recommenders.server.extdoc.GenericServer;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.commons.extdoc.ServerUtils;
import org.eclipse.recommenders.tests.commons.extdoc.TestUtils;
import org.junit.Test;

public final class GenericServerTest {

    private final GenericServer server = ServerUtils.getGenericServer();
    private final IProvider provider = ExtDocUtils.getTestProvider();

    private final IJavaElement element = TestUtils.getDefaultMethod();

    @Test
    public void testComments() {
        final IComment comment = server.addComment("Test text", element, provider);
        final Collection<? extends IComment> comments = server.getUserFeedback(element, provider).getComments();

        // Assert.assertFalse(comments.isEmpty());
        // Assert.assertTrue(comments.contains(comment));
    }

    @Test
    public void testRatings() {
        server.addRating(4, element, provider);
        final IRatingSummary summary = server.getUserFeedback(element, provider).getRatingSummary();

        // Assert.assertTrue(summary.getAverage() > 0);
        // Assert.assertNotNull(summary.getUserRating());
    }
}
