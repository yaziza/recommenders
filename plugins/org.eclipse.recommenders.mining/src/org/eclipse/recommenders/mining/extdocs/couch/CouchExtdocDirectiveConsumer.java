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
package org.eclipse.recommenders.mining.extdocs.couch;

import org.eclipse.recommenders.extdoc.ClassOverrideDirectives;
import org.eclipse.recommenders.extdoc.ClassOverridePatterns;
import org.eclipse.recommenders.extdoc.MethodSelfcallDirectives;
import org.eclipse.recommenders.mining.extdocs.IExtdocDirectiveConsumer;
import org.eclipse.recommenders.mining.extdocs.couch.CouchGuiceModule.Output;

import com.google.inject.Inject;

public class CouchExtdocDirectiveConsumer implements IExtdocDirectiveConsumer {

    private final CouchDbDataAccess db;

    @Inject
    public CouchExtdocDirectiveConsumer(@Output final CouchDbDataAccess db) {
        this.db = db;
    }

    @Override
    public void consume(final ClassOverrideDirectives directives) {
        db.saveOrUpdate(directives);
    }

    @Override
    public void consume(final ClassOverridePatterns patterns) {
        db.saveOrUpdate(patterns);
    }

    @Override
    public void consume(final MethodSelfcallDirectives methodSelfcallDirectives) {
        db.saveOrUpdate(methodSelfcallDirectives);
    }

}
