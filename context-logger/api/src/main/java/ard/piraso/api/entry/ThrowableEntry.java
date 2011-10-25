package ard.piraso.api.entry;

/**
 * Defines an exception entry.
 */
public class ThrowableEntry extends Entry {
    private String message;

    private ThrowableEntry cause;

    private StackTraceElementEntry[] stackTrace;

    public ThrowableEntry() {}

    public ThrowableEntry(Throwable e) {
        message = e.getMessage();

        StackTraceElement[] elements = e.getStackTrace();
        stackTrace = new StackTraceElementEntry[elements.length];

        for(int i = 0; i < elements.length; i++) {
            stackTrace[i] = new StackTraceElementEntry(elements[i]);
        }

        if(e.getCause() != null) {
            cause = new ThrowableEntry(e.getCause());
        }
    }

    public StackTraceElementEntry[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTraceElementEntry[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ThrowableEntry getCause() {
        return cause;
    }

    public void setCause(ThrowableEntry cause) {
        this.cause = cause;
    }
}
