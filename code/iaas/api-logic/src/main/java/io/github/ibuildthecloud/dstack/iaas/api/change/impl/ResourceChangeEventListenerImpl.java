package io.github.ibuildthecloud.dstack.iaas.api.change.impl;

import io.github.ibuildthecloud.dstack.eventing.EventService;
import io.github.ibuildthecloud.dstack.eventing.model.Event;
import io.github.ibuildthecloud.dstack.eventing.model.EventVO;
import io.github.ibuildthecloud.dstack.iaas.api.change.ResourceChangeEventListener;
import io.github.ibuildthecloud.dstack.iaas.event.IaasEvents;
import io.github.ibuildthecloud.dstack.lock.LockDelegator;
import io.github.ibuildthecloud.dstack.task.Task;
import io.github.ibuildthecloud.dstack.task.TaskOptions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class ResourceChangeEventListenerImpl implements ResourceChangeEventListener, Task, TaskOptions {

    volatile Map<Pair<String, String>,Object> changed = new ConcurrentHashMap<Pair<String,String>, Object>();
    LockDelegator lockDelegator;
    EventService eventService;

    @Override
    public void stateChange(Event event) {
        add(event);
    }

    @Override
    public void apiChange(Event event) {
        add(event);
    }

    protected void add(Event event) {
        String id = event.getResourceId();
        String type = event.getResourceType();

        if ( type != null && id != null ) {
            changed.put(new ImmutablePair<String, String>(type, id), Boolean.TRUE);
        }
    }

    @Override
    public void run() {
        if ( ! lockDelegator.tryLock(new ResourceChangePublishLock()) ) {
            changed.clear();
            return;
        }

        Map<Pair<String, String>,Object> changed = this.changed;
        this.changed = new ConcurrentHashMap<Pair<String,String>, Object>();

        for ( Pair<String, String> pair: changed.keySet() ) {
            eventService.publish(EventVO.newEvent(IaasEvents.RESOURCE_CHANGE)
                    .withResourceType(pair.getLeft())
                    .withResourceId(pair.getRight()));
        }
    }

    @Override
    public String getName() {
        return "resource.change.publish";
    }

    @Override
    public boolean isShouldRecord() {
        return false;
    }

    @Override
    public boolean isShouldLock() {
        return false;
    }

    public EventService getEventService() {
        return eventService;
    }

    @Inject
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public LockDelegator getLockDelegator() {
        return lockDelegator;
    }

    @Inject
    public void setLockDelegator(LockDelegator lockDelegator) {
        this.lockDelegator = lockDelegator;
    }

}
