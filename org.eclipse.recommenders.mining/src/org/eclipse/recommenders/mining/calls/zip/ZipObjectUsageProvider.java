/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.mining.calls.zip;

import static org.eclipse.recommenders.commons.utils.GenericEnumerationUtils.iterable;
import static org.eclipse.recommenders.commons.utils.Option.wrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.commons.udc.ObjectUsage;
import org.eclipse.recommenders.commons.utils.Option;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ReceiverCallSite;
import org.eclipse.recommenders.mining.calls.AlgorithmParameters;
import org.eclipse.recommenders.mining.calls.IObjectUsageProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class ZipObjectUsageProvider implements IObjectUsageProvider {
    private final ZipFile zip;
    private List<ObjectUsage> usages;

    @Inject
    public ZipObjectUsageProvider(final AlgorithmParameters config) {
        zip = config.getZip();
    }

    @Override
    public Iterable<ObjectUsage> findObjectUsages(final ModelSpecification spec) {
        usages = Lists.newLinkedList();

        for (final ZipEntry entry : iterable(zip.entries())) {
            final Option<CompilationUnit> option = loadCompilationUnit(entry);
            if (option.hasValue()) {
                collectObjectUsages(option.get());
            }
        }
        return usages;
    }

    private Option<CompilationUnit> loadCompilationUnit(final ZipEntry entry) {
        CompilationUnit res = null;
        try {
            if (entry.getName().endsWith(".json")) {
                final InputStream inputStream = zip.getInputStream(entry);
                res = GsonUtil.deserialize(inputStream, CompilationUnit.class);
            }
        } catch (final IOException e) {
            // log.warn(String.format("Failed to parse '%s' from zip '%s'",
            // entry, zip.getName()), e);
        }
        return wrap(res);
    }

    private void collectObjectUsages(final CompilationUnit unit) {
        for (final MethodDeclaration method : unit.primaryType.methods) {
            for (final ObjectInstanceKey instanceKey : method.objects) {
                final ObjectUsage usage = createObjectUsage(method, instanceKey);
                usages.add(usage);
            }
        }
    }

    private static ObjectUsage createObjectUsage(final MethodDeclaration method, final ObjectInstanceKey instanceKey) {
        final ObjectUsage objectUsage = new ObjectUsage();
        objectUsage.type = instanceKey.type;
        objectUsage.contextFirst = method.firstDeclaration;
        objectUsage.contextSuper = method.superDeclaration;
        objectUsage.calls = Sets.newHashSet();
        for (final ReceiverCallSite call : instanceKey.receiverCallSites) {
            objectUsage.calls.add(call.targetMethod);
        }
        return objectUsage;
    }
}