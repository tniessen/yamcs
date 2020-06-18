package org.yamcs.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.client.base.AbstractSubscription;
import org.yamcs.client.base.WebSocketClient;
import org.yamcs.protobuf.SubscribeParametersData;
import org.yamcs.protobuf.SubscribeParametersRequest;
import org.yamcs.protobuf.SubscribeParametersRequest.Action;
import org.yamcs.protobuf.Yamcs.NamedObjectId;

public class ParameterSubscription extends AbstractSubscription<SubscribeParametersRequest, SubscribeParametersData> {

    protected Set<Listener> listeners = new CopyOnWriteArraySet<>();

    private Map<NamedObjectId, ParameterValue> latestValues = new ConcurrentHashMap<>();

    // Maps server-assigned numeric ids against the request identifiers
    protected Map<Integer, NamedObjectId> mapping = new ConcurrentHashMap<>();

    protected ParameterSubscription(WebSocketClient client) {
        super(client, "parameters", SubscribeParametersData.class);
        addMessageListener(this::processMessage);
    }

    protected void processMessage(SubscribeParametersData message) {
        mapping.putAll(message.getMappingMap());

        for (NamedObjectId id : message.getInvalidList()) {
            listeners.forEach(l -> l.onInvalidIdentification(id));
        }

        List<ParameterValue> values = new ArrayList<>(message.getValuesCount());
        for (ParameterValue incomingValue : message.getValuesList()) {
            NamedObjectId id = mapping.get(incomingValue.getNumericId());
            ParameterValue value = ParameterValue.newBuilder(incomingValue).setId(id).build();
            values.add(value);
            latestValues.put(id, value);
        }

        if (!values.isEmpty()) {
            listeners.forEach(l -> l.onData(values));
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    /**
     * Get the latest value for a specific parameter. This method looks for the value from a local cache. It does not
     * contact Yamcs.
     */
    public ParameterValue get(NamedObjectId id) {
        return latestValues.get(id);
    }

    /**
     * Extends the ongoing subscription with the provided identifiers.
     */
    public void add(List<NamedObjectId> ids) {
        if (!ids.isEmpty()) {
            clientObserver.next(SubscribeParametersRequest.newBuilder()
                    .setAction(Action.ADD)
                    .addAllId(ids)
                    .build());
        }
    }

    /**
     * Shrinks the ongoing subscription by removing the provided identifiers.
     */
    public void remove(List<NamedObjectId> ids) {
        if (!ids.isEmpty()) {
            clientObserver.next(SubscribeParametersRequest.newBuilder()
                    .setAction(Action.REMOVE)
                    .addAllId(ids)
                    .build());
        }
    }

    @FunctionalInterface
    public static interface Listener {

        void onData(List<ParameterValue> values);

        default void onInvalidIdentification(NamedObjectId id) {
        }
    }
}