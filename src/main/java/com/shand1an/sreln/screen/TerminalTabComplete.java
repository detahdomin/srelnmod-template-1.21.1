package com.shand1an.sreln.screen;

import java.util.ArrayList;
import java.util.List;

public class TerminalTabComplete {
    private int tabIndex = -1;
    private String prefix = "";
    private String lastReturned = "";

    public String complete(String input, List<String> completions) {
        String lower = input.toLowerCase();

        // ponytail: reset prefix only when user typed something new (not TAB fill)
        if (!lower.equals(lastReturned)) {
            prefix = lower;
            tabIndex = -1;
        }

        List<String> matches = new ArrayList<>();
        for (String c : completions) {
            if (c.toLowerCase().startsWith(prefix)) {
                matches.add(c);
            }
        }
        if (matches.isEmpty()) {
            prefix = "";
            lastReturned = "";
            return input;
        }

        tabIndex = (tabIndex + 1) % matches.size();
        lastReturned = matches.get(tabIndex);
        return lastReturned;
    }
}