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
package org.eclipse.recommenders.rcp.extdoc.feedback;

import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.feedback.CommunityFeedback;
import org.eclipse.recommenders.rcp.extdoc.feedback.IUserFeedbackServer;
import org.eclipse.recommenders.rcp.extdoc.feedback.StarsRatingComposite;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.commons.extdoc.ServerUtils;
import org.eclipse.recommenders.tests.commons.extdoc.TestTypeUtils;
import org.eclipse.swt.widgets.Shell;

import org.junit.Test;

public final class StarsRatingCompositeTest {

    @Test
    public void testCreate() {
        final Shell shell = ExtDocUtils.getShell();
        final IProvider provider = ExtDocUtils.getTestProvider();

        provider.createComposite(shell, null);

        final IUserFeedbackServer server = ServerUtils.getGenericServer();
        for (final IName name : TestTypeUtils.getDefaultNames()) {
            final StarsRatingComposite composite = CommunityFeedback.create(name, "test", provider, server)
                    .loadStarsRatingComposite(shell);
            // composite.addRating(4, summary);
        }
    }
}