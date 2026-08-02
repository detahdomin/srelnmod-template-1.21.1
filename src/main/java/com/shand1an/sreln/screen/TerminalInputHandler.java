package com.shand1an.sreln.screen;

import java.util.List;
import net.minecraft.client.gui.components.EditBox;
import org.lwjgl.glfw.GLFW;

public interface TerminalInputHandler {

    EditBox getInput();
    TerminalTabComplete getTab();
    List<String> getHistory();
    int getHistoryIndex();
    void setHistoryIndex(int idx);
    boolean isHacker();
    boolean isShuttingDown();
    boolean isReady();
    boolean isInventoryKey(int keyCode, int scanCode);
    List<String> buildCompletions();

    default boolean processKey(int keyCode, int scanCode, int modifiers) {
        if (isShuttingDown()) return true;
        if (isInventoryKey(keyCode, scanCode)) return true;
        if (!isReady()) return true;
        if (getInput().keyPressed(keyCode, scanCode, modifiers)) return true;

        if (keyCode == GLFW.GLFW_KEY_TAB) {
            String current = getInput().getValue();
            if (!current.isEmpty()) {
                String completed = getTab().complete(current, buildCompletions());
                if (!completed.equals(current)) {
                    getInput().setValue(completed);
                    getInput().setCursorPosition(completed.length());
                    playTypingSound();
                }
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            String cmd = getInput().getValue().trim();
            if (!cmd.isEmpty()) {
                getHistory().add(cmd);
                setHistoryIndex(-1);
                execute(cmd);
            }
            getInput().setValue("");
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            List<String> history = getHistory();
            if (!history.isEmpty()) {
                int idx = getHistoryIndex();
                if (keyCode == GLFW.GLFW_KEY_UP) {
                    if (idx == -1) idx = history.size() - 1;
                    else if (idx > 0) idx--;
                } else {
                    if (idx < history.size() - 1) idx++;
                    else { setHistoryIndex(-1); getInput().setValue(""); return true; }
                }
                setHistoryIndex(idx);
                getInput().setValue(history.get(idx));
                getInput().setCursorPosition(getInput().getValue().length());
            }
            return true;
        }

        return false;
    }

    void execute(String cmd);

    void playTypingSound();
}