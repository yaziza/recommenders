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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.commons.client.ResultObject;
import org.eclipse.recommenders.commons.client.ServerErrorException;
import org.eclipse.recommenders.commons.client.ServerUnreachableException;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.rcp.extdoc.preferences.PreferenceConstants;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.GenericType;

public final class Server {

    @Inject
    @Named(PreferenceConstants.NAME_EXTDOC_WEBSERVICE_CONFIGURATION)
    private static ClientConfiguration clientConfig;
    private static WebServiceClient lazyClient;

    @Inject
    private static JavaElementResolver resolver;

    private static final String QUOTE = encode("\"");
    private static final String BRACEOPEN = encode("{");
    private static final String BRACECLOSE = encode("}");

    private Server() {
    }

    public static <T> T get(final String path, final Class<T> resultType) {
        return getClient().doGetRequest(path, resultType);
    }

    public static void post(final Object object) {
        getClient().doPostRequest("", object);
    }

    public static <T> T getProviderContent(final String view, final String providerId, final String key,
            final String value, final Class<T> resultType) {
        final String path = buildPath(view, providerId, key, value);
        return get(path, resultType);
    }

    public static <T> T getProviderContent(final String providerId, final String key, final String value,
            final GenericType<GenericResultObjectView<T>> resultType) {
        final String path = buildPath("providers", providerId, key, value);
        try {
            final List<ResultObject<T>> rows = getClient().doGetRequest(path, resultType).rows;
            return rows.isEmpty() ? null : rows.get(0).value;
        } catch (final ServerErrorException e) {
            return null;
        } catch (final ServerUnreachableException e) {
            return null;
        }
    }

    public static String createKey(final IMethod method) {
        final IMethodName methodName = resolver.toRecMethod(method);
        return methodName == null ? null : methodName.getIdentifier();
    }

    public static String createKey(final IType type) {
        final ITypeName typeName = resolver.toRecType(type);
        return typeName == null ? null : typeName.getIdentifier();
    }

    private static String buildPath(final String view, final String providerId, final String key, final String value) {
        Checks.ensureIsNotNull(value);
        return String.format("_design/providers/_view/%s?key=%s%sproviderId%s:%s%s%s,%s%s%s:%s%s%s%s?stale=ok", view,
                BRACEOPEN, QUOTE, QUOTE, QUOTE, providerId, QUOTE, QUOTE, key, QUOTE, QUOTE, encode(value), QUOTE,
                BRACECLOSE);
    }

    private static String encode(final String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static WebServiceClient getClient() {
        if (lazyClient == null) {
            Checks.ensureIsNotNull(clientConfig,
                    "ClientConfiguration was not injected. Check your guice configuration.");
            lazyClient = new WebServiceClient(clientConfig);
        }
        return lazyClient;
    }
}
