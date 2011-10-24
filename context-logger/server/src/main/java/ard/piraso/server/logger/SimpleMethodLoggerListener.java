package ard.piraso.server.logger;

import ard.piraso.api.entry.ElapseTimeEntry;
import ard.piraso.api.entry.MessageEntry;
import ard.piraso.server.dispatcher.ContextLogDispatcher;
import ard.piraso.server.proxy.RegexMethodInterceptorEvent;
import ard.piraso.server.proxy.RegexMethodInterceptorListener;

/**
 * simple method call logger listener.
 */
public class SimpleMethodLoggerListener<T> implements RegexMethodInterceptorListener<T> {

    private TraceableID id;

    private ElapseTimeEntry elapseTime;

    private MessageEntry entry;

    private String preferenceProperty;

    public SimpleMethodLoggerListener(String preferenceProperty, TraceableID id) {
        this(preferenceProperty, id, null);
    }

    public SimpleMethodLoggerListener(String preferenceProperty, TraceableID id, ElapseTimeEntry elapseTime) {
        this.id = id;
        this.preferenceProperty = preferenceProperty;

        if(elapseTime == null) {
            this.elapseTime = new ElapseTimeEntry();
        } else {
            this.elapseTime = elapseTime;
        }
    }

    public void beforeCall(RegexMethodInterceptorEvent<T> evt) {
        entry = new MessageEntry(evt.getInvocation().getMethod().getName(), elapseTime);
        entry.getElapseTime().start();
    }

    public void afterCall(RegexMethodInterceptorEvent<T> evt) {
        assert entry != null;

        entry.getElapseTime().stop();
        ContextLogDispatcher.forward(preferenceProperty, id, entry);
    }

    public void exceptionCall(RegexMethodInterceptorEvent<T> evt) {
        assert entry != null;

        entry.getElapseTime().stop();
        entry.setMessage(evt.getInvocation().getMethod().getName() + ":" + evt.getException().getClass().getName());

        ContextLogDispatcher.forward(preferenceProperty, id, entry);
    }
}
