/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.calls.rcp.l10n;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.calls.rcp.l10n.messages"; //$NON-NLS-1$

    public static String PROPOSAL_LABEL_USED;
    public static String PROPOSAL_LABEL_PERCENTAGE;
    public static String PROPOSAL_LABEL_PROMILLE;

    public static String PREFPAGE_DESCRIPTION_CALLS;

    public static String FIELD_LABEL_HIGHLIGHT_USED_PROPOSALS;

    public static String PROVIDER_INTRO_NO_RECOMMENDATIONS;
    public static String PROVIDER_INTRO_RECOMMENDATIONS;

    public static String PROVIDER_INFO_LOCAL_VAR_CONTEXT;
    public static String PROVIDER_INFO_UNTRAINED_CONTEXT;

    public static String TABLE_CELL_DEFINITION_UNTRAINED;
    public static String TABLE_CELL_RELATION_CALL;
    public static String TABLE_CELL_RELATION_DEFINED_BY;
    public static String TABLE_CELL_RELATION_OBSERVED;
    public static String TABLE_CELL_SUFFIX_PERCENTAGE;
    public static String TABLE_CELL_SUFFIX_PROMILLE;

    public static String LOG_ERROR_FAILED_TO_CREATE_PROPOSALS;
    public static String LOG_ERROR_FAILED_TO_FIND_ARGUMENTS_FOR_METHODS;
    public static String LOG_ERROR_FAILED_TO_FIND_TYPE;
    public static String LOG_ERROR_FAILED_TO_RESOLVE_SUPER_TYPE;
    public static String LOG_ERROR_FAILED_TO_VALIDATE_TEMPLATE;
    public static String LOG_ERROR_RECEIVER_TYPE_LOOKUP_FAILED;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
