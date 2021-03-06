/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import java.util.Arrays;
import java.util.Map;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.mylyn.commons.notifications.ui.NotificationsUi;
import org.eclipse.recommenders.internal.news.rcp.notifications.NewMessageNotification;
import org.eclipse.recommenders.news.rcp.INotificationFacade;
import org.eclipse.recommenders.news.rcp.IPollingResult;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
@Creatable
@Singleton
public class NotificationFacade implements INotificationFacade {

    @Override
    public void displayNotification(final Map<FeedDescriptor, IPollingResult> messages, final EventBus eventBus) {
        NotificationsUi.getService().notify(Arrays.asList(new NewMessageNotification(eventBus, messages)));
    }

}
