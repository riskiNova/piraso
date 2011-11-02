package ard.piraso.server;

import ard.piraso.api.GeneralPreferenceEnum;

/**
 * General preferences evaluator
 */
public class GeneralPreferenceEvaluator {

    protected ContextPreference preference = new PirasoRequestContext();

    private boolean isEnabled(GeneralPreferenceEnum pref) {
        return preference.isEnabled(pref.getPropertyName());
    }

    public boolean isStackTraceEnabled() {
        return isEnabled(GeneralPreferenceEnum.STACK_TRACE_ENABLED);
    }

    public boolean isLoggingScopedEnabled() {
        return isEnabled(GeneralPreferenceEnum.SCOPE_ENABLED);
    }

    public void requestOnScope() {
        preference.requestOnScope();
    }
}
