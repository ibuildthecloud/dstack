package io.cattle.platform.engine.process.impl;

import io.cattle.platform.engine.process.ExitReason;

public class ProcessExecutionExitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    ExitReason exitReason;

    public ProcessExecutionExitException(ExitReason exitReason) {
        this(exitReason, null);
    }

    public ProcessExecutionExitException(String message, ExitReason exitReason) {
        this(message, exitReason, null);
    }

    public ProcessExecutionExitException(ExitReason exitReason, Throwable t) {
        this(null, exitReason, t);
    }

    public ProcessExecutionExitException(String message, ExitReason exitReason, Throwable t) {
        super(message == null ? exitReason.toString() : message, t);
        this.exitReason = exitReason;
    }

    public ExitReason getExitReason() {
        return exitReason;
    }

}
