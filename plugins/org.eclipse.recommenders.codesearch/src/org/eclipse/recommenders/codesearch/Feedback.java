/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.codesearch;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A feedback is basically a tuple "snippet-id, user-feedback" that allows the
 * server to learn better rankings for codesearch.
 */
public class Feedback {

    public static Feedback newFeedback(final String codeSnippetId, final FeedbackType feedbackEvent) {
        final Feedback res = new Feedback();
        res.snippetId = codeSnippetId;
        res.event = feedbackEvent;
        return res;
    }

    private Feedback() {
        // see #create()
    }

    /**
     * The id of the snippet this feedback is associated with.
     */
    public String snippetId;

    /**
     * The type of feedback which has been observed on the developer's IDE.
     */
    public FeedbackType event;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}