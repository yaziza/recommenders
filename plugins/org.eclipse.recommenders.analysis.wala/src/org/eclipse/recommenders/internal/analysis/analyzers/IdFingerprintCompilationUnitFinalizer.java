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
package org.eclipse.recommenders.internal.analysis.analyzers;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.internal.analysis.utils.ClassUtils;
import org.eclipse.recommenders.internal.analysis.utils.WalaNameUtils;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.TypeReference;

public class IdFingerprintCompilationUnitFinalizer implements ICompilationUnitFinalizer {
    private final Map<IClass, String/* fingerprint */> map = new MapMaker().softKeys().maximumSize(50)
            .concurrencyLevel(2).makeMap();
    private final IDependencyFingerprintComputer fingerprintComputer;

    @Inject
    public IdFingerprintCompilationUnitFinalizer(final IDependencyFingerprintComputer fingerprintComputer) {
        this.fingerprintComputer = fingerprintComputer;
    }

    @Override
    public void finalizeClass(final CompilationUnit compilationUnit, final IClass exampleClass,
            final IProgressMonitor monitor) {
        setCompilationUnitFingerprint(compilationUnit, exampleClass);
        //
        final IClassHierarchy cha = exampleClass.getClassHierarchy();
        for (final ITypeName recType : findAllUsedTypes(compilationUnit)) {
            final TypeReference walaType = WalaNameUtils.rec2walaType(recType);
            final IClass clazz = ClassUtils.findClass(walaType, cha);
            if (clazz != null) {

                addTypeToUsedTypes(compilationUnit, recType, clazz);
            }
        }
    }

    private void setCompilationUnitFingerprint(final CompilationUnit compilationUnit, final IClass exampleClass) {
        compilationUnit.id = fingerprint(exampleClass);
    }

    private Set<ITypeName> findAllUsedTypes(final CompilationUnit compilationUnit) {
        final Set<ITypeName> res = Sets.newHashSet();
        visitType(compilationUnit.primaryType, res);
        return res;
    }

    private void visitType(final TypeDeclaration type, final Set<ITypeName> res) {
        res.addAll(type.interfaces);
        res.add(type.superclass);
        res.addAll(type.fields);
        res.addAll(type.fields);
        for (final TypeDeclaration nestedType : type.memberTypes) {
            visitType(nestedType, res);
        }
        for (final MethodDeclaration method : type.methods) {
            visitMethod(method, res);
        }
    }

    private void visitMethod(final MethodDeclaration method, final Set<ITypeName> res) {
        for (final ObjectInstanceKey value : method.objects) {
            res.add(value.type);
        }
    }

    private void addTypeToUsedTypes(final CompilationUnit compilationUnit, final ITypeName recType, final IClass clazz) {
        final String fingerprint = fingerprintComputer.computeContainerFingerprint(clazz);
        final org.eclipse.recommenders.internal.analysis.codeelements.TypeReference recTypeRef = org.eclipse.recommenders.internal.analysis.codeelements.TypeReference
                .create(recType, fingerprint);
        compilationUnit.imports.add(recTypeRef);
    }

    private final Lock lock = new ReentrantLock();

    private String fingerprint(final IClass clazz) {
        ensureIsNotNull(clazz);
        String fingerprint = map.get(clazz);
        if (fingerprint == null) {
            lock.lock();
            fingerprint = ClassUtils.fingerprint(clazz);
            map.put(clazz, fingerprint);
            lock.unlock();
        }
        return fingerprint;
    }
}