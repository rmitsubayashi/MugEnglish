package com.linnca.pelicann.preferences;

import com.linnca.pelicann.mainactivity.ToolbarState;

public interface PreferencesListener {
    void updateTheme();
    void setToolbarState(ToolbarState state);
}
