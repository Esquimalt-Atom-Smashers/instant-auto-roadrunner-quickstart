package com.example.instantauto.actions;

import java.util.List;

public class UserAction implements MetaAction {
    private final String id;
    private final List<String> subActionLines;
    private final boolean hasError;

    public UserAction(String id, List<String> subActionLines, boolean hasError) {
        this.id = id;
        this.subActionLines = subActionLines;
        this.hasError = hasError;
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public Action create(Object params) {
        if (hasError) {
            return new Action() {
                @Override
                public boolean run() {
                    return false;
                }
            };
        }

        List<Action> actions = new java.util.ArrayList<>();
        for (String line : subActionLines) {
            Action a = UserActionRegistry.createAction(line);
            if (a != null) {
                actions.add(a);
            }
        }

        // Apply merging/fusion to the sub-actions
        final List<Action> mergedActions = UserActionRegistry.applyMerger(actions);

        return new Action() {
            private int currentIndex = 0;

            @Override
            public boolean run() {
                if (currentIndex >= mergedActions.size()) return false;
                
                Action current = mergedActions.get(currentIndex);
                if (current == null || !current.run()) {
                    currentIndex++;
                }
                
                return currentIndex < mergedActions.size();
            }
        };
    }

    @Override
    public Action create(String params) {
        return create((Object) params);
    }
}
