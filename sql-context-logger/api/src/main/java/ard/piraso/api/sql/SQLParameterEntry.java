package ard.piraso.api.sql;

import ard.piraso.api.entry.EntryUtils;
import ard.piraso.api.entry.MethodCallEntry;
import ard.piraso.api.entry.ObjectEntry;

import java.lang.reflect.Method;

/**
 * SQL parameter entry
 */
public class SQLParameterEntry extends MethodCallEntry {
    private String name;

    private Integer index;

    public SQLParameterEntry() {}

    public SQLParameterEntry(String name, Method method, Object[] arguments) {
        this(name, method, arguments, null);
    }

    public SQLParameterEntry(String name, Method method, Object[] arguments, Object returnValue) {
        super(method);
        this.name = name;
        setArguments(EntryUtils.toEntry(arguments));
        setReturnedValue(new ObjectEntry(returnValue));
    }

    public SQLParameterEntry(Integer index, Method method, Object[] arguments) {
        this(index, method, arguments, null);
    }

    public SQLParameterEntry(Integer index, Method method, Object[] arguments, Object returnValue) {
        super(method);
        this.index = index;
        setArguments(EntryUtils.toEntry(arguments));
        setReturnedValue(new ObjectEntry(returnValue));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}
