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

package com.gigaspaces.internal.client.spaceproxy.actioninfo;

import com.gigaspaces.internal.client.spaceproxy.IDirectSpaceProxy;
import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.gigaspaces.internal.client.spaceproxy.metadata.ObjectType;
import com.gigaspaces.internal.transport.IEntryPacket;
import com.j_spaces.core.LeaseContext;
import com.j_spaces.core.client.Modifiers;
import com.j_spaces.core.client.UpdateModifiers;
import com.j_spaces.kernel.SystemProperties;

import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;

@com.gigaspaces.api.InternalApi
public class WriteMultipleProxyActionInfo extends CommonProxyActionInfo {
    public final Object[] entries;
    public final IEntryPacket[] entryPackets;
    public final long lease;
    public final long[] leases;
    public final long timeout;

    @SuppressWarnings("deprecation")
    private final static boolean oneWaySystemProperty = Boolean.getBoolean(SystemProperties.ONE_WAY_WRITE);

    public WriteMultipleProxyActionInfo(ISpaceProxy spaceProxy, Object[] entries, Transaction txn, long lease, long[] leases, long timeout, int modifiers) {
        super(txn, spaceProxy.initWriteModifiers(modifiers));

        if (entries == null || entries.length == 0)
            throw new IllegalArgumentException("Cannot write a null or empty entries array.");
        if (leases != null && leases.length != entries.length)
            throw new IllegalArgumentException("Leases array size must match entries array size.");
        if ((lease < 0L) && (lease != Lease.ANY) && (lease != Long.MIN_VALUE)) {
            throw new IllegalArgumentException("Lease cannot be less than zero: " + lease);
        }


        if (oneWaySystemProperty)
            modifiers |= Modifiers.ONE_WAY;

        final boolean oneWay = Modifiers.contains(modifiers, Modifiers.ONE_WAY);
        if (oneWay) {
            if (txn != null || spaceProxy.getContextTransaction() != null)
                throw new UnsupportedOperationException("Oneway write is not supported when the write is being done under a transaction.");
            if (UpdateModifiers.isReturnPrevOnUpdate(modifiers))
                throw new UnsupportedOperationException("Oneway write is not supported with return previous value on update.");
        }

        this.entries = entries;
        this.lease = lease;
        this.leases = leases;
        this.timeout = timeout;

        entryPackets = new IEntryPacket[entries.length];
        for (int i = 0; i < entries.length; i++) {
            if (entries[i] == null)
                throw new IllegalArgumentException("entry number " + i + " is null");
            ObjectType objectType = ObjectType.fromObject(entries[i]);

            entryPackets[i] = toEntryPacket(spaceProxy, entries[i], objectType);

            if (entryPackets[i].getTypeName() == null)
                throw new IllegalArgumentException("Cannot write null-class Entry- entry number " + i);
            if (UpdateModifiers.isPartialUpdate(this.modifiers) && entryPackets[i].getDynamicProperties() != null)
                throw new UnsupportedOperationException("Partial update is not supported for dynamic properties.");
        }
    }

    public LeaseContext<?>[] convertWriteResults(IDirectSpaceProxy spaceProxy, LeaseContext<?>[] result) {
        //handle the case when result is null - no return value modifier+local cache
        if (result == null) {
            for (int i = 0; i < entries.length; i++)
                spaceProxy.getTypeManager().convertWriteResult(entries[i], entryPackets[i], null);
            return null;
        }

        // fill the user object with the UID but return null.
        for (int i = 0; i < result.length; i++) {
            result[i] = spaceProxy.getTypeManager().convertWriteOrUpdateResult(result[i], entries[i],
                    entryPackets[i], modifiers);
        }

        if (UpdateModifiers.isNoReturnValue(modifiers))
            result = null;

        return result;
    }
}
