package io.github.ibuildthecloud.dstack.engine.eventing.impl;

import io.github.ibuildthecloud.dstack.async.utils.TimeoutException;
import io.github.ibuildthecloud.dstack.engine.eventing.ProcessEventListener;
import io.github.ibuildthecloud.dstack.engine.manager.ProcessManager;
import io.github.ibuildthecloud.dstack.engine.manager.ProcessNotFoundException;
import io.github.ibuildthecloud.dstack.engine.process.ExitReason;
import io.github.ibuildthecloud.dstack.engine.process.ProcessInstanceException;
import io.github.ibuildthecloud.dstack.engine.server.ProcessServer;
import io.github.ibuildthecloud.dstack.eventing.model.Event;
import io.github.ibuildthecloud.dstack.metrics.util.MetricsUtil;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;

public class ProcessEventListenerImpl implements ProcessEventListener {

    private static final Logger log = LoggerFactory.getLogger(ProcessEventListenerImpl.class);

    private static Counter EVENT = MetricsUtil.getRegistry().counter("process_execution.event");
    private static Counter DONE = MetricsUtil.getRegistry().counter("process_execution.done");
    private static Counter NOT_FOUND = MetricsUtil.getRegistry().counter("process_execution.not_found");
    private static Counter EXCEPTION = MetricsUtil.getRegistry().counter("process_execution.unknown_exception");
    private static Counter TIMEOUT = MetricsUtil.getRegistry().counter("process_execution.timeout");

    ProcessManager processManager;
    ProcessServer processServer;
    Map<ExitReason,Counter> counters = new HashMap<ExitReason, Counter>();

    @Override
    public void processExecute(Event event) {
        if ( event.getResourceId() == null )
            return;

        EVENT.inc();

        long processId = new Long(event.getResourceId());
        boolean runRemaining = false;
        try {
            processManager.loadProcess(processId).execute();
            runRemaining = true;
            DONE.inc();
        } catch ( ProcessNotFoundException e ) {
            NOT_FOUND.inc();
            log.debug("Failed to find process for id [{}]", event.getResourceId());
        } catch ( ProcessInstanceException e ) {
            counters.get(e.getExitReason()).inc();
            switch (e.getExitReason()) {
            case PROCESS_ALREADY_IN_PROGRESS:
            case RESOURCE_BUSY:
                break;
            default:
                log.error("Process [{}] failed, exit [{}] : {}", event.getResourceId(), e.getExitReason(), e.getMessage());
            }
        } catch ( TimeoutException e ) {
            TIMEOUT.inc();
            log.info("Communication timeout on process [{}] : {}", event.getResourceId(), e.getMessage());
        } catch ( RuntimeException e ) {
            EXCEPTION.inc();
            log.error("Unknown exception running process [{}]", event.getResourceId(), e);
        }

        if ( runRemaining ) {
            processServer.runRemainingTasks(processId);
        }
    }

    @PostConstruct
    public void init() {
        for ( ExitReason e : ExitReason.values() ) {
            switch (e) {
            case DONE:
            case ALREADY_DONE:
                break;
            default:
                counters.put(e, MetricsUtil.getRegistry().counter("process_execution." + e.toString().toLowerCase()));
            }
        }
    }

    public ProcessManager getProcessManager() {
        return processManager;
    }

    @Inject
    public void setProcessManager(ProcessManager processManager) {
        this.processManager = processManager;
    }

    public ProcessServer getProcessServer() {
        return processServer;
    }

    @Inject
    public void setProcessServer(ProcessServer processServer) {
        this.processServer = processServer;
    }

}
