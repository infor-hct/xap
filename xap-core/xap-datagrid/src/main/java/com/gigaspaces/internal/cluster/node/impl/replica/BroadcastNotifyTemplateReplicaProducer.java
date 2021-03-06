/*
 * Copyright (c) 2008-2016, GigaSpaces Technologies, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gigaspaces.internal.cluster.node.impl.replica;

import com.gigaspaces.internal.server.space.SpaceEngine;
import com.gigaspaces.internal.server.storage.NotifyTemplateHolder;

import java.util.logging.Level;


@com.gigaspaces.api.InternalApi
public class BroadcastNotifyTemplateReplicaProducer
        extends NotifyTemplateReplicaProducer {

    public BroadcastNotifyTemplateReplicaProducer(SpaceEngine engine, Object requestContext) {
        super(engine, requestContext);
    }

    protected boolean isRelevant(NotifyTemplateHolder notifyTemplate) {
        if (!notifyTemplate.isBroadcast()) {
            if (_logger.isLoggable(Level.FINEST))
                _logger.finest(getLogPrefix() + "filtered notify template " + notifyTemplate.getClassName() + " Uid=" + notifyTemplate.getUID() + " since it is not a broadcast template");
            return false;
        }

        if (notifyTemplate.getXidOriginatedTransaction() != null) {
            if (_logger.isLoggable(Level.FINEST))
                _logger.finest(getLogPrefix() + "filtered notify template " + notifyTemplate.getClassName() + " Uid=" + notifyTemplate.getUID() + " since it is under transaction");
            return false;
        }

        return true;
    }

    @Override
    protected String getDumpName() {
        return "Broadcast notify template replica";
    }

    @Override
    public String getName() {
        return "BroadcastNotifyTemplateReplicaProducer";
    }

}
