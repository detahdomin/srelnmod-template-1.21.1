package com.shand1an.sreln.screen;

import java.util.ArrayList;
import java.util.List;
import com.shand1an.sreln.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;

public class FacilityTerminalScreen extends AbstractContainerScreen<FacilityTerminalMenu> implements TerminalInputHandler {

    private EditBox input;
    private final List<String> outputLines = new ArrayList<>();
    private final List<String> pendingLines = new ArrayList<>();
    private String typingLine;
    private int typingChar;
    private int typingTick;
    private int bootTicks;
    private boolean booted;
    private boolean ready;
    private int slideInTicks;
    private boolean powerButtonPlayed;
    private boolean welcomePlayed;
    private int fanHumTimer;
    private boolean shuttingDown;
    private int shutdownLinesDone;
    private int shutdownTotalLines;
    private int shutdownTick;
    private final TerminalTabComplete tab = new TerminalTabComplete();
    private final List<String> history = new ArrayList<>();
    private int historyIndex = -1;
    private static final int MAX_SHUTDOWN_TICKS = 120;
    private static final int MAX_SLIDE_TICKS = 15;
    private static final int TYPE_SPEED = 1;
    private static final int MAX_BOOT_TICKS = 160;
    private static final int FAN_HUM_INTERVAL = 100;
    private static final int PROGRESS_MAX_TICKS = 80;

    private String progressFacility;
    private String progressAction;
    private int progressTicks;
    private int progressFacilityIdx;

    private static final int BG_COLOR = 0xE00E0D0A;
    private static final int HEADER_BG = 0xF02A1A0A;
    private static final int HEADER_H = 16;
    private static final int FOOTER_H = 14;
    private static final int INPUT_H = 16;
    private static final int BAR_COLOR = 0xFFCC7700;
    private static final int TEXT_COLOR = 0xFFCC88;
    private static final int PROMPT_COLOR = 0xCC8800;
    private static final int ERROR_COLOR = 0xFF4444;
    private static final int OK_COLOR = 0x44FF44;
    private static final int HACKER_COLOR = 0xFF4444;
    private static final int INPUT_BG = BG_COLOR;
    private static final int SCANLINE_COLOR = 0x08000000;
    private static final String PROMPT = "fac@ctl:~$ ";

    public FacilityTerminalScreen(FacilityTerminalMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 320;
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();
        int inputBgY = this.topPos + this.imageHeight - FOOTER_H - INPUT_H;
        int x = this.leftPos;
        int w = this.imageWidth;

        this.input = new EditBox(this.font, x + 8 + this.font.width(PROMPT) + 2, inputBgY + 1, w - 16 - this.font.width(PROMPT), 14, Component.empty());
        this.input.setMaxLength(256);
        this.input.setBordered(false);
        this.input.setTextColor(TEXT_COLOR);
        this.input.setFocused(true);
        this.input.setResponder(t -> {});
        this.addRenderableWidget(this.input);
        this.setInitialFocus(this.input);
    }

    @Override
    public boolean isHacker() {
        return this.menu.isHacker;
    }

    @Override
    public List<String> buildCompletions() {
        List<String> completions = new ArrayList<>();
        completions.addAll(List.of("/help", "/list", "/modify", "/clear", "/exit", "/shutdown"));
        if (isHacker()) {
            completions.add("/reset all");
            completions.add("!exit");
        }
        for (var f : this.menu.blockEntity.getFacilities()) {
            completions.add("/modify \"" + f.name() + "\" on");
            completions.add("/modify \"" + f.name() + "\" off");
        }
        return completions;
    }

    @Override
    public TerminalTabComplete getTab() { return tab; }

    @Override
    public List<String> getHistory() { return history; }

    @Override
    public int getHistoryIndex() { return historyIndex; }

    @Override
    public void setHistoryIndex(int idx) { this.historyIndex = idx; }

    @Override
    public boolean isShuttingDown() { return shuttingDown; }

    @Override
    public boolean isReady() { return ready && progressFacility == null; }

    @Override
    public boolean isInventoryKey(int keyCode, int scanCode) {
        return this.minecraft.options.keyInventory.matches(keyCode, scanCode);
    }

    @Override
    public EditBox getInput() { return input; }

    @Override
    public void playTypingSound() {
        this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.15f, 2.0f);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (slideInTicks < MAX_SLIDE_TICKS) slideInTicks++;

        if (!powerButtonPlayed && bootTicks == 1) {
            powerButtonPlayed = true;
            this.minecraft.player.playSound(ModSounds.POWER_BUTTON.get(), 0.8f, 1.0f);
        }

        if (!booted) {
            bootTicks++;
            if (bootTicks >= MAX_BOOT_TICKS) {
                booted = true;
                if (!shuttingDown) {
                    this.minecraft.player.playSound(ModSounds.FAN_HUM.get(), 0.3f, 1.0f);
                    enqueue("SRE Facility Control Terminal v1.0");
                    enqueue("Type /help for commands.");
                    enqueue("");
                }
            }
        }

        if (booted && !welcomePlayed && !shuttingDown && typingLine == null && pendingLines.isEmpty()) {
            welcomePlayed = true;
            ready = true;
            this.minecraft.player.playSound(ModSounds.WELCOME_VOICE.get(), 0.8f, 1.0f);
        }

        if (booted) {
            fanHumTimer++;
            if (fanHumTimer >= FAN_HUM_INTERVAL) {
                fanHumTimer = 0;
                this.minecraft.player.playSound(ModSounds.FAN_HUM.get(), 0.3f, 1.0f);
            }
        }

        if (progressFacility != null) {
            progressTicks++;
            float pct = Math.min(1f, (float) progressTicks / PROGRESS_MAX_TICKS);
            int barLen = 20;
            int filled = (int) (barLen * pct);
            StringBuilder bar = new StringBuilder("  [");
            for (int i = 0; i < barLen; i++) {
                bar.append(i < filled ? '#' : '-');
            }
            bar.append("] ").append(String.format("%3d%%", (int)(pct * 100)));
            if (!outputLines.isEmpty()) {
                outputLines.set(outputLines.size() - 1, bar.toString());
            }
            if (progressTicks >= PROGRESS_MAX_TICKS) {
                if (progressAction.equals("ON")) {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, progressFacilityIdx);
                } else {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 100 + progressFacilityIdx);
                }
                this.minecraft.player.playSound(ModSounds.MISSION_COMPLETE.get(), 0.8f, 1.0f);
                outputLines.set(outputLines.size() - 1, "  [OK] " + progressFacility + " " + (progressAction.equals("ON") ? "activated" : "deactivated"));
                progressFacility = null;
                progressAction = null;
                progressTicks = 0;
            }
        }

        if (shuttingDown) {
            shutdownTick++;
        }

        this.input.setVisible(ready && !shuttingDown);

        if (typingLine != null) {
            typingTick++;
            if (typingTick >= TYPE_SPEED) {
                typingTick = 0;
                typingChar++;
                if (typingChar >= typingLine.length()) {
                    outputLines.add(typingLine);
                    if (shuttingDown) shutdownLinesDone++;
                    if (!pendingLines.isEmpty()) {
                        typingLine = pendingLines.remove(0);
                        typingChar = 0;
                    } else {
                        typingLine = null;
                        if (shuttingDown && shutdownLinesDone >= shutdownTotalLines) {
                            this.onClose();
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (processKey(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        this.minecraft.getSoundManager().stop(ResourceLocation.fromNamespaceAndPath("sreln_mod", "fan_hum"), SoundSource.PLAYERS);
        this.minecraft.getSoundManager().stop(ResourceLocation.fromNamespaceAndPath("sreln_mod", "welcome_voice"), SoundSource.PLAYERS);
    }

    private void startShutdown() {
        shuttingDown = true;
        shutdownLinesDone = 0;
        shutdownTotalLines = 4;
        shutdownTick = 0;
        this.minecraft.getSoundManager().stop(ResourceLocation.fromNamespaceAndPath("sreln_mod", "welcome_voice"), SoundSource.PLAYERS);
        this.minecraft.player.playSound(ModSounds.SHUTDOWN_VOICE.get(), 0.8f, 1.0f);
        enqueue("[SYS] Shutdown signal received...");
        enqueue("[SYS] Terminating processes...");
        enqueue("[SYS] Unmounting filesystems...");
        enqueue("[SYS] Halting system...");
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!ready || shuttingDown) return true;
        if (this.input.charTyped(codePoint, modifiers)) {
            this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.15f, 1.6f);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void enqueue(String line) {
        if (typingLine == null) {
            typingLine = line;
            typingChar = 0;
            typingTick = 0;
        } else {
            pendingLines.add(line);
        }
    }

    public void execute(String cmd) {
        outputLines.add(PROMPT + cmd);
        String upper = cmd.toUpperCase();

        if (upper.equals("!EXIT")) {
            if (isHacker()) {
                this.onClose();
                return;
            } else {
                outputLines.add("  [ERR] Access denied: hacker tag required");
                return;
            }
        }

        if (upper.startsWith("!EXIT")) {
            outputLines.add("  [ERR] Access denied: hacker tag required");
            return;
        }

        switch (upper) {
            case "/EXIT":
            case "/SHUTDOWN":
                startShutdown();
                return;
            case "/HELP":
                outputLines.add("  /list              - List all facilities");
                outputLines.add("  /modify \"name\" on  - Activate facility");
                outputLines.add("  /modify \"name\" off - Deactivate facility");
                outputLines.add("  /clear             - Clear screen");
                outputLines.add("  /exit              - Shutdown & exit");
                if (isHacker()) {
                    outputLines.add("  [HACKER] /reset all         - Reset all facilities");
                    outputLines.add("  [HACKER] !exit              - Force exit");
                }
                break;
            case "/CLEAR":
                outputLines.clear();
                pendingLines.clear();
                typingLine = null;
                outputLines.add("SRE Facility Control Terminal v1.0");
                return;
            case "/LIST":
                var facilities = this.menu.blockEntity.getFacilities();
                if (facilities.isEmpty()) {
                    outputLines.add("  No facilities registered.");
                } else {
                    for (var entry : facilities) {
                        boolean on = this.menu.blockEntity.isLeverOn(entry.name());
                        String state = on ? "ON" : "OFF";
                        int color = on ? OK_COLOR : ERROR_COLOR;
                        outputLines.add("  " + entry.name() + "  [" + state + "]");
                    }
                }
                break;
            case "/RESET ALL":
                if (!isHacker()) break;
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 200);
                outputLines.add("  [RESET] All facilities deactivated.");
                break;
            default:
                if (upper.startsWith("/MODIFY ")) {
                    String rest = cmd.substring(8).trim();
                    if (rest.startsWith("\"") && rest.contains("\" ")) {
                        int endQuote = rest.indexOf("\"", 1);
                        if (endQuote > 0) {
                            String name = rest.substring(1, endQuote);
                            String action = rest.substring(endQuote + 1).trim().toUpperCase();
                            var entry = this.menu.blockEntity.findFacility(name);
                            if (entry == null) {
                                outputLines.add("  [ERR] Facility '" + name + "' not found");
                            } else if (action.equals("ON")) {
                                int idx = this.menu.blockEntity.getFacilities().indexOf(entry);
                                if (idx >= 0) {
                                    progressFacility = name;
                                    progressAction = "ON";
                                    progressTicks = 0;
                                    progressFacilityIdx = idx;
                                    outputLines.add("  [RUN] activating " + name + "...");
                                    outputLines.add("  [--------------------]   0%");
                                }
                            } else if (action.equals("OFF")) {
                                int idx = this.menu.blockEntity.getFacilities().indexOf(entry);
                                if (idx >= 0) {
                                    progressFacility = name;
                                    progressAction = "OFF";
                                    progressTicks = 0;
                                    progressFacilityIdx = idx;
                                    outputLines.add("  [RUN] deactivating " + name + "...");
                                    outputLines.add("  [--------------------]   0%");
                                }
                            } else {
                                outputLines.add("  Usage: /modify \"name\" on|off");
                            }
                            break;
                        }
                    }
                    outputLines.add("  Usage: /modify \"name\" on|off");
                    break;
                }
                outputLines.add("  Unknown: " + upper);
                break;
        }
    }

    private int visibleLines() {
        return (this.imageHeight - HEADER_H - FOOTER_H - INPUT_H - 4) / 10;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        if (slideInTicks < MAX_SLIDE_TICKS) {
            float progress = Math.min(1f, (slideInTicks + partialTick) / MAX_SLIDE_TICKS);
            int alpha = (int) (0xE0 * (1f - progress));
            g.fill(0, 0, this.width, this.height, alpha << 24);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mx, int my) {
        int x = this.leftPos, y = this.topPos, w = this.imageWidth, h = this.imageHeight;

        g.fill(x, y, x + w, y + h, BG_COLOR);
        g.fill(x + 1, y + 1, x + w - 1, y + HEADER_H, HEADER_BG);
        g.fill(x + 1, y + h - FOOTER_H, x + w - 1, y + h - 1, HEADER_BG);

        drawScanlines(g, x, y, w, h);

        if (shuttingDown) {
            renderShutdown(g, x, y, w, h);
            return;
        }

        g.drawString(this.font, "FACILITY CONTROL", x + 6, y + 5, PROMPT_COLOR);
        String status = booted ? "SYSTEM ONLINE" : "BOOTING...";
        g.drawString(this.font, status, x + w - this.font.width(status) - 6, y + 5, TEXT_COLOR);

        if (!booted) {
            renderBoot(g, x, y, w, h);
            return;
        }

        int facilityCount = this.menu.blockEntity.getFacilityCount();
        String info = "F: " + facilityCount;
        g.drawString(this.font, info, x + w - this.font.width(info) - 6, y + 5, TEXT_COLOR);

        int inputBgY = y + h - FOOTER_H - INPUT_H;
        g.fill(x + 6, inputBgY, x + w - 6, inputBgY + INPUT_H, INPUT_BG);
        g.drawString(this.font, PROMPT, x + 8, inputBgY + 1, PROMPT_COLOR);

        int oy = y + HEADER_H + 4;
        int max = visibleLines();
        int totalLines = outputLines.size() + (typingLine != null ? 1 : 0);
        int start = Math.max(0, totalLines - max);
        for (int i = start; i < outputLines.size(); i++) {
            String line = outputLines.get(i);
            int ly = oy + (i - start) * 10;
            int c = TEXT_COLOR;
            if (line.startsWith(PROMPT)) c = PROMPT_COLOR;
            else if (line.contains("[ERR]") || line.contains("Unknown") || line.contains("[OFF]")) c = ERROR_COLOR;
            else if (line.contains("[RESET]") || line.contains("[HACKER]")) c = HACKER_COLOR;
            else if (line.startsWith("  ")) c = OK_COLOR;
            g.drawString(this.font, line, x + 8, ly, c);
        }
        if (typingLine != null) {
            int ly = oy + (outputLines.size() - start) * 10;
            String partial = typingLine.substring(0, Math.min(typingChar, typingLine.length()));
            int c = TEXT_COLOR;
            if (partial.contains("[ERR]") || partial.contains("Unknown")) c = ERROR_COLOR;
            else if (partial.contains("[RESET]") || partial.contains("[HACKER]")) c = HACKER_COLOR;
            else if (partial.startsWith("  ")) c = OK_COLOR;
            g.drawString(this.font, partial, x + 8, ly, c);
        }
    }

    private void renderShutdown(GuiGraphics g, int x, int y, int w, int h) {
        int cx = x + w / 2;
        int barW = 200, barH = 10;
        int barX = cx - barW / 2, barY = y + h / 2;

        String title = "SYSTEM SHUTDOWN";
        g.drawString(this.font, title, cx - this.font.width(title) / 2, barY - 14, ERROR_COLOR);

        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, HEADER_BG);
        float lineProgress = shutdownTotalLines > 0 ? (float) shutdownLinesDone / shutdownTotalLines : 0f;
        float tickProgress = Math.min(1f, (float) shutdownTick / MAX_SHUTDOWN_TICKS);
        float progress = Math.max(lineProgress, tickProgress);
        float remaining = 1f - progress;
        int fillW = (int) (barW * remaining);
        g.fill(barX, barY, barX + fillW, barY + barH, BAR_COLOR);

        String pct = (int) (remaining * 100) + "%";
        g.drawString(this.font, pct, cx - this.font.width(pct) / 2, barY + barH + 6, TEXT_COLOR);

        int lineY = y + HEADER_H + 4;
        int drawn = 0;
        for (int i = outputLines.size() - 1; i >= 0 && drawn < 5; i--) {
            String line = outputLines.get(i);
            if (line.startsWith("[SYS]")) {
                g.drawString(this.font, line, x + 8, lineY + drawn * 10, ERROR_COLOR);
                drawn++;
            }
        }
        if (typingLine != null) {
            String partial = typingLine.substring(0, Math.min(typingChar, typingLine.length()));
            g.drawString(this.font, partial, x + 8, lineY + drawn * 10, ERROR_COLOR);
        }
    }

    private void renderBoot(GuiGraphics g, int x, int y, int w, int h) {
        int cx = x + w / 2;
        int barW = 200, barH = 10;
        int barX = cx - barW / 2, barY = y + h / 2 - 10;

        String title = "SREL-OS v3.7";
        g.drawString(this.font, title, cx - this.font.width(title) / 2, barY - 28, PROMPT_COLOR);
        String subtitle = "Facility Control Subsystem";
        g.drawString(this.font, subtitle, cx - this.font.width(subtitle) / 2, barY - 18, OK_COLOR);

        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, HEADER_BG);
        float progress = (float) bootTicks / MAX_BOOT_TICKS;
        int fillW = (int) (barW * progress);
        g.fill(barX, barY, barX + fillW, barY + barH, BAR_COLOR);

        String pct = (int) (progress * 100) + "%";
        g.drawString(this.font, pct, cx - this.font.width(pct) / 2, barY + barH + 6, TEXT_COLOR);

        String[] logs = {
            "Loading kernel",
            "Mounting filesystem",
            "Initializing facility drivers",
            "Starting terminal services"
        };
        int logY = barY + barH + 20;
        int idx = (int) (progress * (logs.length + 1));
        for (int i = 0; i < Math.min(idx, logs.length); i++) {
            String pre = i < idx - 1 ? "  " : "> ";
            int col = i < idx - 1 ? OK_COLOR : TEXT_COLOR;
            g.drawString(this.font, pre + logs[i], barX, logY + i * 10, col);
        }
    }

    private void drawScanlines(GuiGraphics g, int x, int y, int w, int h) {
        for (int sy = y; sy < y + h; sy += 3) {
            g.fill(x, sy, x + w, sy + 1, SCANLINE_COLOR);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {}
}