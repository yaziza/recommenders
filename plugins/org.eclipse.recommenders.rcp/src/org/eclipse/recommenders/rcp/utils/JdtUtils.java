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
package org.eclipse.recommenders.rcp.utils;

import static com.google.common.base.Optional.*;
import static java.util.Objects.requireNonNull;
import static org.eclipse.jdt.internal.corext.util.JdtFlags.*;
import static org.eclipse.jdt.ui.SharedASTProvider.*;
import static org.eclipse.recommenders.internal.rcp.l10n.LogMessages.*;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Logs.log;
import static org.eclipse.recommenders.utils.Throws.*;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.MethodOverrideTester;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.internal.rcp.l10n.Messages;
import org.eclipse.recommenders.utils.Nullable;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.Names;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

@SuppressWarnings({ "restriction", "unchecked" })
public final class JdtUtils {

    private JdtUtils() {
        // Not meant to be instantiated
    }

    private static final Util.BindingsToNodesMap EMPTY_NODE_MAP = new Util.BindingsToNodesMap() {

        @Override
        public org.eclipse.jdt.internal.compiler.ast.ASTNode get(final Binding binding) {
            return null;
        }
    };

    private static final Predicate<IField> STATIC_PUBLIC_FIELDS_ONLY_FILTER = new Predicate<IField>() {

        @Override
        public boolean apply(final IField m) {
            try {
                // filter these:
                return !isStatic(m) || !isPublic(m);
            } catch (Exception e) {
                // filter!
                return true;
            }
        }
    };

    private static final Predicate<IField> PUBLIC_FIELDS_ONLY_FILTER = new Predicate<IField>() {

        @Override
        public boolean apply(final IField m) {
            try {
                // filter these:
                return isStatic(m) || !isPublic(m);
            } catch (Exception e) {
                // filter!
                return true;
            }
        }
    };

    private static final Predicate<IMethod> STATIC_PUBLIC_METHODS_ONLY_FILTER = new Predicate<IMethod>() {

        @Override
        public boolean apply(final IMethod m) {
            try {
                // filter these:
                return !isStatic(m) || !isPublic(m);
            } catch (Exception e) {
                // filter!
                return true;
            }
        }
    };

    private static final Predicate<IMethod> PUBLIC_INSTANCE_METHODS_ONLY_FILTER = new Predicate<IMethod>() {

        @Override
        public boolean apply(final IMethod m) {
            try {
                // filter these:
                return isStatic(m) || !isPublic(m) || m.isConstructor();
            } catch (Exception e) {
                // filter!
                return true;
            }
        }
    };

    private static final Predicate<IMethod> STATIC_NON_VOID_NON_PRIMITIVE_PUBLIC_METHODS_FILTER = new Predicate<IMethod>() {

        @Override
        public boolean apply(final IMethod m) {
            try {
                // filter these:
                return !isStatic(m) || isVoid(m) || !isPublic(m) || hasPrimitiveReturnType(m);
            } catch (Exception e) {
                // filter!
                return true;
            }
        }
    };

    private static String createFieldKey(final IField field) {
        try {
            return field.getElementName() + field.getTypeSignature();
        } catch (Exception e) {
            throw throwUnhandledException(e);
        }
    }

    private static String createMethodKey(final IMethod method) {
        try {
            final String signature = method.getSignature();
            final String signatureWithoutReturnType = StringUtils.substringBeforeLast(signature, ")"); //$NON-NLS-1$
            final String methodName = method.getElementName();
            return methodName + signatureWithoutReturnType;
        } catch (Exception e) {
            throw throwUnhandledException(e);
        }
    }

    public static IRegion createRegion(final ASTNode node) {
        requireNonNull(node);
        return new Region(node.getStartPosition(), node.getLength());
    }

    public static Optional<IField> createUnresolvedField(final FieldBinding compilerBinding) {
        requireNonNull(compilerBinding);
        final IField f = (IField) Util.getUnresolvedJavaElement(compilerBinding, null, EMPTY_NODE_MAP);
        return fromNullable(f);
    }

    public static ILocalVariable createUnresolvedLocaVariable(final VariableBinding compilerBinding,
            final JavaElement parent) {
        requireNonNull(compilerBinding);
        requireNonNull(parent);

        final String name = new String(compilerBinding.name);
        final String type = new String(compilerBinding.type.signature());
        return new LocalVariable(parent, name, 0, 0, 0, 0, type, null, compilerBinding.modifiers,
                compilerBinding.isParameter());
    }

    public static Optional<IMethod> createUnresolvedMethod(final MethodBinding compilerBinding) {
        requireNonNull(compilerBinding);
        final IMethod m = (IMethod) Util.getUnresolvedJavaElement(compilerBinding, null, EMPTY_NODE_MAP);
        return fromNullable(m);
    }

    public static Optional<IType> createUnresolvedType(final TypeBinding compilerBinding) {
        final JavaElement e = Util.getUnresolvedJavaElement(compilerBinding, null, EMPTY_NODE_MAP);
        if (e instanceof IType) {
            return of((IType) e);
        } else if (e instanceof ITypeParameter) {
            return resolveTypeParameter((ITypeParameter) e);
        }
        return absent();
    }

    private static Optional<IType> resolveTypeParameter(ITypeParameter t) {
        try {
            final IJavaProject project = t.getJavaProject();
            if (project == null) {
                return absent();
            }

            final String[] bounds = t.getBoundsSignatures();
            if (ArrayUtils.isEmpty(bounds)) {
                return Optional.fromNullable(project.findType("java.lang.Object")); //$NON-NLS-1$
            } else {
                final IMember declaringMember = t.getDeclaringMember();
                final Optional<String> typename = resolveUnqualifiedTypeNamesAndStripOffGenericsAndArrayDimension(
                        bounds[0], declaringMember);
                if (typename.isPresent()) {
                    return Optional.fromNullable(project.findType(typename.get()));
                } else {
                    return absent();
                }
            }
        } catch (JavaModelException e) {
            log(WARNING_FAILED_TO_RESOLVE_TYPE_PARAMETER, e, t.getElementName());
            return absent();
        } catch (Exception e) {
            log(ERROR_FAILED_TO_RESOLVE_TYPE_PARAMETER, e, t.getElementName());
            return absent();
        }
    }

    /**
     * Returns a list of all public instance methods and fields declared in the given type or any of its super-types
     */
    public static Collection<IMember> findAllPublicInstanceFieldsAndNonVoidNonPrimitiveInstanceMethods(
            final IType type) {
        final LinkedHashMap<String, IMember> tmp = new LinkedHashMap<String, IMember>();

        final IType[] returnTypeAndSupertypes = findAllSupertypesIncludingArgument(type);
        for (final IType cur : returnTypeAndSupertypes) {
            try {
                for (final IMethod m : cur.getMethods()) {
                    if (isVoid(m) || !isPublic(m) || m.isConstructor() || isStatic(m) || hasPrimitiveReturnType(m)) {
                        continue;
                    }
                    final String key = createMethodKey(m);
                    if (!tmp.containsKey(key)) {
                        tmp.put(key, m);
                    }
                }
            } catch (JavaModelException e) {
                log(WARNING_FAILED_TO_FIND_METHODS_FOR_TYPE, e, cur.getFullyQualifiedName());
            } catch (Exception e) {
                log(ERROR_FAILED_TO_FIND_METHODS_FOR_TYPE, e, cur.getFullyQualifiedName());
            }

            try {
                for (final IField field : cur.getFields()) {
                    if (!isPublic(field) || isStatic(field)) {
                        continue;
                    }
                    final String key = createFieldKey(field);
                    if (!tmp.containsKey(key)) {
                        tmp.put(key, field);
                    }
                }
            } catch (JavaModelException e) {
                log(WARNING_FAILED_TO_FIND_FIELDS_FOR_TYPE, e, cur.getFullyQualifiedName());
            } catch (Exception e) {
                log(ERROR_FAILED_TO_FIND_FIELDS_FOR_TYPE, e, cur.getFullyQualifiedName());
            }
        }

        return tmp.values();
    }

    public static Collection<IMember> findAllPublicInstanceFieldsAndPublicInstanceMethods(final IType type) {
        return findAllRelevantFieldsAndMethods(type, PUBLIC_FIELDS_ONLY_FILTER, PUBLIC_INSTANCE_METHODS_ONLY_FILTER);
    }

    /**
     * Returns a list of all public static fields and methods declared in the given class or any of its super-classes.
     */
    public static Collection<IMember> findAllPublicStaticFieldsAndNonVoidNonPrimitiveStaticMethods(final IType type) {

        return findAllRelevantFieldsAndMethods(type, STATIC_PUBLIC_FIELDS_ONLY_FILTER,
                STATIC_NON_VOID_NON_PRIMITIVE_PUBLIC_METHODS_FILTER);
    }

    public static Collection<IMember> findAllRelevantFieldsAndMethods(final IType type,
            final Predicate<IField> fieldFilter, final Predicate<IMethod> methodFilter) {
        final LinkedHashMap<String, IMember> tmp = new LinkedHashMap<String, IMember>();

        for (final IType cur : findAllSupertypesIncludingArgument(type)) {
            try {
                for (final IMethod method : cur.getMethods()) {
                    if (methodFilter.apply(method)) {
                        continue;
                    }
                    final String key = createMethodKey(method);
                    if (!tmp.containsKey(key)) {
                        tmp.put(key, method);
                    }
                }
            } catch (JavaModelException e) {
                log(WARNING_FAILED_TO_FIND_METHODS_FOR_TYPE, e, cur.getFullyQualifiedName());
            } catch (Exception e) {
                log(ERROR_FAILED_TO_FIND_METHODS_FOR_TYPE, e, cur.getFullyQualifiedName());
            }

            try {
                for (final IField field : cur.getFields()) {
                    if (fieldFilter.apply(field)) {
                        continue;
                    }
                    final String key = createFieldKey(field);
                    if (!tmp.containsKey(key)) {
                        tmp.put(key, field);
                    }
                }
            } catch (JavaModelException e) {
                log(WARNING_FAILED_TO_FIND_FIELDS_FOR_TYPE, e, cur.getFullyQualifiedName());
            } catch (Exception e) {
                log(ERROR_FAILED_TO_FIND_FIELDS_FOR_TYPE, e, cur.getFullyQualifiedName());
            }
        }

        return tmp.values();
    }

    public static Collection<IMember> findAllPublicStaticFieldsAndStaticMethods(final IType type) {
        return findAllRelevantFieldsAndMethods(type, STATIC_PUBLIC_FIELDS_ONLY_FILTER,
                STATIC_PUBLIC_METHODS_ONLY_FILTER);
    }

    private static IType[] findAllSupertypesIncludingArgument(final IType returnType) {
        try {
            ITypeHierarchy typeHierarchy;
            typeHierarchy = SuperTypeHierarchyCache.getTypeHierarchy(returnType);
            final IType[] allSupertypes = typeHierarchy.getAllSupertypes(returnType);
            return ArrayUtils.add(allSupertypes, 0, returnType);
        } catch (JavaModelException e) {
            log(WARNING_FAILED_TO_FIND_TYPE_HIERARCHY_OF_TYPE, e, returnType.getFullyQualifiedName());
            return new IType[0];
        } catch (Exception e) {
            log(ERROR_FAILED_TO_FIND_TYPE_HIERARCHY_OF_TYPE, e, returnType.getFullyQualifiedName());
            return new IType[0];
        }
    }

    public static ASTNode findClosestMethodOrTypeDeclarationAroundOffset(final CompilationUnit cuNode,
            final ITextSelection selection) {
        requireNonNull(cuNode);
        requireNonNull(selection);
        ASTNode node = NodeFinder.perform(cuNode, selection.getOffset(), selection.getLength());
        while (node != null) {
            switch (node.getNodeType()) {
            case ASTNode.METHOD_DECLARATION:
            case ASTNode.TYPE_DECLARATION:
                return node;
            }
            node = node.getParent();
        }
        return cuNode;
    }

    public static IMethod findFirstDeclaration(final IMethod method) {
        IMethod res = method;
        while (true) {
            final Optional<IMethod> oFind = findOverriddenMethod(res);
            if (!oFind.isPresent()) {
                break;
            } else {
                res = oFind.get();
            }
        }
        return res;
    }

    public static Optional<IMethod> findOverriddenMethod(final IMethod jdtMethod) {
        IType jdtDeclaringType = null;
        try {
            jdtDeclaringType = jdtMethod.getDeclaringType();
            final MethodOverrideTester methodOverrideTester = SuperTypeHierarchyCache
                    .getMethodOverrideTester(jdtDeclaringType);
            final IMethod overriddenMethod = methodOverrideTester.findOverriddenMethod(jdtMethod, false);
            return fromNullable(overriddenMethod);
        } catch (JavaModelException e) {
            log(WARNING_FAILED_TO_FIND_OVERRIDDEN_METHOD_OF_METHOD, e,
                    jdtDeclaringType == null ? Messages.UNKNOWN_TYPE : jdtDeclaringType.getFullyQualifiedName(),
                    jdtMethod.getElementName());
            return absent();
        } catch (Exception e) {
            log(ERROR_FAILED_TO_FIND_OVERRIDDEN_METHOD_OF_METHOD, e,
                    jdtDeclaringType == null ? Messages.UNKNOWN_TYPE : jdtDeclaringType.getFullyQualifiedName(),
                    jdtMethod.getElementName());
            return absent();
        }
    }

    public static Optional<ITypeName> findSuperclassName(final IType type) {
        try {
            final String superclassName = type.getSuperclassTypeSignature();
            if (superclassName == null) {
                return absent();
            }
            final Optional<String> opt = resolveUnqualifiedTypeNamesAndStripOffGenericsAndArrayDimension(superclassName,
                    type);
            if (!opt.isPresent()) {
                return absent();
            }
            final String vmSuperclassName = toVMTypeDescriptor(opt.get());
            final ITypeName vmTypeName = VmTypeName.get(vmSuperclassName);
            return of(vmTypeName);
        } catch (JavaModelException e) {
            log(WARNING_FAILED_TO_FIND_SUPERTYPE_NAME_OF_TYPE, e, type.getFullyQualifiedName());
            return absent();
        } catch (Exception e) {
            log(ERROR_FAILED_TO_FIND_SUPERTYPE_NAME_OF_TYPE, e, type.getFullyQualifiedName());
            return absent();
        }
    }

    public static Optional<ITypeName> resolveUnqualifiedJDTType(String qName, final IJavaElement parent) {
        IType type = null;
        try {
            qName = Signature.getTypeErasure(qName);
            qName = StringUtils.removeEnd(qName, ";"); //$NON-NLS-1$
            final int dimensions = Signature.getArrayCount(qName);
            if (dimensions > 0) {
                qName = Signature.getElementType(qName);
            }

            if (isPrimitiveTypeSignature(qName)) {
                final ITypeName t = VmTypeName.get(StringUtils.repeat('[', dimensions) + qName);
                return of(t);
            }

            type = findClosestTypeOrThis(parent);
            if (type == null) {
                return absent();
            }

            if (qName.charAt(0) == Signature.C_TYPE_VARIABLE) {
                String literal = StringUtils.repeat('[', dimensions) + VmTypeName.OBJECT.getIdentifier();
                ITypeName name = VmTypeName.get(literal);
                return of(name);
            }
            if (qName.charAt(0) == Signature.C_UNRESOLVED) {

                final String[][] resolvedNames = type.resolveType(qName.substring(1));
                if (resolvedNames == null || resolvedNames.length == 0) {
                    return of((ITypeName) VmTypeName.OBJECT);
                }
                final String pkg = resolvedNames[0][0];
                final String name = resolvedNames[0][1].replace('.', '$');
                qName = StringUtils.repeat('[', dimensions) + 'L' + pkg + '.' + name;
            }
            qName = qName.replace('.', '/');
            final ITypeName typeName = VmTypeName.get(qName);
            return of(typeName);
        } catch (JavaModelException e) {
            log(WARNING_FAILED_TO_RESOLVE_UNQUALIFIED_JDT_TYPE, e, type.getFullyQualifiedName());
            return absent();
        } catch (Exception e) {
            log(ERROR_FAILED_TO_RESOLVE_UNQUALIFIED_JDT_TYPE, e, parent.getElementName(), qName);
            return absent();
        }
    }

    private static String toVMTypeDescriptor(final String fqjdtName) {
        return fqjdtName == null ? "Ljava/lang/Object" : 'L' + fqjdtName.replace('.', '/'); //$NON-NLS-1$
    }

    public static Optional<IType> findSuperclass(final IType type) {
        requireNonNull(type);
        try {
            final String superclassTypeSignature = type.getSuperclassTypeSignature();
            if (superclassTypeSignature == null) {
                return absent();
            }
            return findTypeFromSignature(superclassTypeSignature, type);
        } catch (JavaModelException e) {
            log(WARNING_FAILED_TO_FIND_SUPERTYPE_OF_TYPE, e, type.getFullyQualifiedName());
            return absent();
        } catch (Exception e) {
            log(ERROR_FAILED_TO_FIND_SUPERTYPE_OF_TYPE, e, type.getFullyQualifiedName());
            return absent();
        }
    }

    public static Optional<IType> findTypeFromSignature(final String typeSignature, final IJavaElement parent) {
        requireNonNull(typeSignature);
        requireNonNull(parent);
        final String bareTypeName = resolveUnqualifiedTypeNamesAndStripOffGenericsAndArrayDimension(typeSignature,
                parent).orNull();
        if (bareTypeName == null) {
            return absent();
        }

        try {
            final IType res = parent.getJavaProject().findType(bareTypeName);
            return Optional.fromNullable(res);
        } catch (JavaModelException e) {
            log(WARNING_FAILED_TO_FIND_TYPE_FROM_SIGNATURE, e, bareTypeName);
            return absent();
        } catch (Exception e) {
            log(ERROR_FAILED_TO_FIND_TYPE_FROM_SIGNATURE, e, bareTypeName);
            return absent();
        }
    }

    public static Optional<IType> findTypeOfField(final IField field) {
        requireNonNull(field);
        try {
            String fieldSignature = field.getTypeSignature();
            return findTypeFromSignature(fieldSignature, field);
        } catch (JavaModelException e) {
            IType declaringType = field.getDeclaringType();
            log(WARNING_FAILED_TO_FIND_TYPE_OF_FIELD, e,
                    declaringType == null ? Messages.UNKNOWN_TYPE : declaringType.getFullyQualifiedName(),
                    field.getElementName());
            return absent();
        } catch (Exception e) {
            IType declaringType = field.getDeclaringType();
            log(ERROR_FAILED_TO_FIND_TYPE_OF_FIELD, e,
                    declaringType == null ? Messages.UNKNOWN_TYPE : declaringType.getFullyQualifiedName(),
                    field.getElementName());
            return absent();
        }
    }

    public static Optional<ITypeRoot> findTypeRoot(final IEditorPart editor) {
        final ITypeRoot root = EditorUtility.getEditorInputJavaElement(editor, true);
        return fromNullable(root);
    }

    public static Optional<JavaEditor> getActiveJavaEditor() {
        final Optional<IWorkbenchPage> page = RCPUtils.getActiveWorkbenchPage();
        if (page.isPresent()) {
            final IEditorPart editor = page.get().getActiveEditor();
            if (editor instanceof JavaEditor) {
                return of((JavaEditor) editor);
            }
        }
        return absent();
    }

    public static boolean hasPrimitiveReturnType(final IMethod method) {
        try {
            return !method.getReturnType().endsWith(";"); //$NON-NLS-1$
        } catch (Exception e) {
            throw throwUnhandledException(e);
        }
    }

    public static boolean isAssignable(final IType lhsType, final IType rhsType) {
        requireNonNull(lhsType);
        requireNonNull(rhsType);
        final IType[] supertypes = findAllSupertypesIncludingArgument(rhsType);
        for (final IType supertype : supertypes) {
            if (supertype.equals(lhsType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isVoid(final IMethod method) {
        try {
            return Signature.SIG_VOID.equals(method.getReturnType());
        } catch (Exception e) {
            throw throwUnhandledException(e);
        }
    }

    public static Optional<IMethod> resolveMethod(@Nullable final MethodDeclaration node) {
        if (node == null) {
            return absent();
        }
        final IMethodBinding b = node.resolveBinding();
        if (b == null) {
            return absent();
        }
        final IMethod method = cast(b.getJavaElement());
        return Optional.fromNullable(method);
    }

    public static <T extends IJavaElement> T resolveJavaElementProxy(final IJavaElement element) {
        return (T) element.getPrimaryElement();
    }

    /**
     * @param parent
     *            must be an {@link IType} or something that has an {@link IType} as parent.
     */
    public static Optional<String> resolveUnqualifiedTypeNamesAndStripOffGenericsAndArrayDimension(String typeSignature,
            final IJavaElement parent) {
        requireNonNull(typeSignature);
        requireNonNull(parent);

        // remove generics information if available:
        typeSignature = Signature.getTypeErasure(typeSignature);

        if (isPrimitiveTypeSignature(typeSignature)) {
            return of(Names.vm2srcTypeName(typeSignature));
        }

        IType type = null;

        try {
            typeSignature = typeSignature.replace('/', '.');
            type = findClosestTypeOrThis(parent);
            if (type == null) {
                throwIllegalArgumentException("parent could not be resolved to an IType: %s", parent); //$NON-NLS-1$
            }
            final String resolvedTypeSignature = JavaModelUtil.getResolvedTypeName(typeSignature, type);
            if (resolvedTypeSignature == null) {
                // return fall-back. This happens for instance when giving <T>
                // or QT; respectively.
                return of("java.lang.Object"); //$NON-NLS-1$
            } else {
                return of(resolvedTypeSignature);
            }
        } catch (JavaModelException e) {
            log(WARNING_FAILED_TO_RESOLVE_UNQUALIFIED_TYPE_NAME, e, typeSignature, type);
            return absent();
        } catch (Exception e) {
            log(ERROR_FAILED_TO_RESOLVE_UNQUALIFIED_TYPE_NAME, e, typeSignature, type);
            return absent();
        }
    }

    private static IType findClosestTypeOrThis(final IJavaElement parent) {
        return (IType) (parent instanceof IType ? parent : parent.getAncestor(IJavaElement.TYPE));
    }

    private static boolean isPrimitiveTypeSignature(final String typeSignature) {
        return typeSignature.length() == 1;
    }

    public static Optional<ASTNode> findAstNodeFromEditorSelection(final JavaEditor editor,
            final ITextSelection textSelection) {
        final Optional<ITypeRoot> root = findTypeRoot(editor);
        if (!root.isPresent()) {
            return absent();
        }
        final CompilationUnit astRoot = getAST(root.get(), WAIT_YES, null);
        if (astRoot == null) {
            return absent();
        }
        final ASTNode node = org.eclipse.jdt.core.dom.NodeFinder.perform(astRoot, textSelection.getOffset(), 0);
        return Optional.fromNullable(node);
    }

    /**
     * Returns the absolute location of the given java project or {@link Optional#absent} if the project is
     * <code>null</code> or could not be mapped to any existing location on disk.
     */
    public static Optional<File> getLocation(@Nullable final IJavaProject javaProject) {
        if (javaProject == null) {
            return absent();
        }
        IProject project = javaProject.getProject();
        IPath location = project.getLocation();
        if (location == null) {
            return absent();
        }
        File file = location.toFile().getAbsoluteFile();
        if (file.exists()) {
            return of(file);
        } else {
            return absent();
        }
    }

    public static Optional<File> getLocation(@Nullable final IPackageFragmentRoot packageRoot) {
        if (packageRoot == null) {
            return absent();
        }
        File res = null;
        final IResource resource = packageRoot.getResource();
        if (resource != null) {
            if (resource.getLocation() == null) {
                res = resource.getRawLocation().toFile().getAbsoluteFile();
            } else {
                res = resource.getLocation().toFile().getAbsoluteFile();
            }
        }
        if (packageRoot.isExternal()) {
            res = packageRoot.getPath().toFile().getAbsoluteFile();
        }

        // if the file (for whatever reasons) does not exist, skip it.
        if (res != null && !res.exists()) {
            res = null;
        }
        return Optional.fromNullable(res);
    }

    public static boolean isInitializer(final IMethod m) {
        return m.getElementName().equals("<clinit>"); //$NON-NLS-1$
    }
}
