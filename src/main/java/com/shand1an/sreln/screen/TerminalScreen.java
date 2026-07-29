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
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class TerminalScreen extends AbstractContainerScreen<TerminalMenu> {

    private EditBox input;
    private final List<String> outputLines = new ArrayList<>();
    private final List<String> pendingLines = new ArrayList<>();
    private String typingLine;
    private int typingChar;
    private int typingTick;
    private int scrollOffset;
    private int bootTicks;
    private boolean booted;
    private int slideInTicks;
    private boolean powerButtonPlayed;
    private boolean welcomePlayed;
    private int fanHumTimer;
    private boolean shuttingDown;
    private int shutdownLinesDone;
    private int shutdownTotalLines;
    private int shutdownTick;
    private static final int MAX_SHUTDOWN_TICKS = 120;
    private static final int MAX_SLIDE_TICKS = 15;
    private static final int TYPE_SPEED = 1;
    private static final int MAX_BOOT_TICKS = 160;
    private static final int FAN_HUM_INTERVAL = 100;

    private static final int BG_COLOR = 0xE00D1117;
    private static final int HEADER_H = 16;
    private static final int FOOTER_H = 14;
    private static final int INPUT_H = 16;
    private static final int BAR_COLOR = 0xFF5A9AFF;
    private static final int BAR_BG = 0xC01A2240;
    private static final int TEXT_COLOR = 0x96C8FF;
    private static final int PROMPT_COLOR = 0x5A9AFF;
    private static final int ERROR_COLOR = 0xFF6B6B;
    private static final int OK_COLOR = 0x6BFF8E;
    private static final int INPUT_BG = BG_COLOR;
    private static final int SCANLINE_COLOR = 0x08000000;
    private static final String PROMPT = "sre@lab:~$ ";

    public TerminalScreen(TerminalMenu menu, Inventory playerInv, Component title) {
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
                this.minecraft.player.playSound(ModSounds.FAN_HUM.get(), 0.3f, 1.0f);
                fanHumTimer = 0;
                enqueue("SRE Laboratory Terminal v3.7");
                enqueue("Type /help for commands.");
                enqueue("");
            }
        }

        if (booted && !welcomePlayed && typingLine == null && pendingLines.isEmpty()) {
            welcomePlayed = true;
            this.minecraft.player.playSound(ModSounds.WELCOME_VOICE.get(), 0.8f, 1.0f);
        }

        if (booted && !shuttingDown) {
            fanHumTimer++;
            if (fanHumTimer >= FAN_HUM_INTERVAL) {
                fanHumTimer = 0;
                this.minecraft.player.playSound(ModSounds.FAN_HUM.get(), 0.3f, 1.0f);
            }
        }

        if (shuttingDown) {
            shutdownTick++;
        }

        this.input.setVisible(booted && !shuttingDown);

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
        if (!booted || shuttingDown) return true;
        if (this.input.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            String cmd = this.input.getValue().trim();
            if (!cmd.isEmpty()) execute(cmd);
            this.input.setValue("");
            return true;
        }
        if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void startShutdown() {
        shuttingDown = true;
        shutdownLinesDone = 0;
        shutdownTotalLines = 4;
        shutdownTick = 0;
        this.minecraft.getSoundManager().stop(ResourceLocation.fromNamespaceAndPath("sreln_mod", "fan_hum"), SoundSource.BLOCKS);
        this.minecraft.player.playSound(ModSounds.SHUTDOWN_VOICE.get(), 0.8f, 1.0f);
        enqueue("[SYS] Shutdown signal received...");
        enqueue("[SYS] Terminating processes...");
        enqueue("[SYS] Unmounting filesystems...");
        enqueue("[SYS] Halting system...");
        scrollOffset = Math.max(0, outputLines.size() - visibleLines());
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!booted || shuttingDown) return true;
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

    private void execute(String cmd) {
        outputLines.add(PROMPT + cmd);
        String upper = cmd.toUpperCase();
        switch (upper) {
            case "/HELP":
                outputLines.add("  /status     - System status");
                outputLines.add("  /on         - Activate terminal");
                outputLines.add("  /off        - Deactivate terminal");
                outputLines.add("  /clear      - Clear screen");
                outputLines.add("  /about      - About");
                outputLines.add("  /exit       - Shutdown & exit");
                break;
            case "/CLEAR":
                outputLines.clear();
                pendingLines.clear();
                typingLine = null;
                outputLines.add("SRE Laboratory Terminal v3.7");
                return;
            case "/STATUS":
                boolean isActive = this.menu.blockEntity.isActive();
                outputLines.add("  System: ONLINE");
                outputLines.add("  Kernel: SREL-OS 3.7");
                outputLines.add("  Memory: 65536K OK");
                outputLines.add("  Terminal: " + (isActive ? "ACTIVE" : "INACTIVE"));
                break;
            case "/ABOUT":
                outputLines.add("  SRE Laboratory Terminal v3.7");
                outputLines.add("  (c) 2026 SRE Institute");
                break;
            case "/EXIT":
            case "/SHUTDOWN":
                startShutdown();
                return;
            case "/ON":
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                outputLines.add("  [OK] Terminal activated.");
                break;
            case "/OFF":
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
                outputLines.add("  [OK] Terminal deactivated.");
                break;
            default:
                outputLines.add("  Unknown: " + upper);
                break;
        }
        scrollOffset = Math.max(0, outputLines.size() - visibleLines());
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
        g.fill(x + 1, y + 1, x + w - 1, y + HEADER_H, 0xF01A2240);
        g.fill(x + 1, y + h - FOOTER_H, x + w - 1, y + h - 1, 0xF01A2240);

        drawScanlines(g, x, y, w, h);

        if (shuttingDown) {
            renderShutdown(g, x, y, w, h);
            return;
        }

        g.drawString(this.font, "TERMINAL CONTROL", x + 6, y + 5, PROMPT_COLOR);
        String status = booted ? "SYSTEM ONLINE" : "BOOTING...";
        g.drawString(this.font, status, x + w - this.font.width(status) - 6, y + 5, booted ? OK_COLOR : TEXT_COLOR);

        if (!booted) {
            renderBoot(g, x, y, w, h);
            return;
        }

        boolean terminalActive = this.menu.blockEntity.isActive();
        String statusText = terminalActive ? "STATUS: ACTIVE" : "STATUS: INACTIVE";
        int statusColor = terminalActive ? PROMPT_COLOR : ERROR_COLOR;
        g.drawString(this.font, statusText, x + 6, y + h - FOOTER_H + 3, statusColor);

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
            else if (line.contains("INACTIVE") || line.contains("OFFLINE")) c = ERROR_COLOR;
            else if (line.startsWith("  Unknown")) c = ERROR_COLOR;
            else if (line.startsWith("  ")) c = OK_COLOR;
            g.drawString(this.font, line, x + 8, ly, c);
        }
        if (typingLine != null) {
            int ly = oy + (outputLines.size() - start) * 10;
            String partial = typingLine.substring(0, Math.min(typingChar, typingLine.length()));
            int c = partial.startsWith(PROMPT) ? PROMPT_COLOR : (partial.contains("INACTIVE") || partial.contains("OFFLINE")) ? ERROR_COLOR : partial.startsWith("  Unknown") ? ERROR_COLOR : partial.startsWith("  ") ? OK_COLOR : TEXT_COLOR;
            g.drawString(this.font, partial, x + 8, ly, c);
        }
    }

    private void renderShutdown(GuiGraphics g, int x, int y, int w, int h) {
        int cx = x + w / 2;
        int barW = 200, barH = 10;
        int barX = cx - barW / 2, barY = y + h / 2;

        String title = "SYSTEM SHUTDOWN";
        g.drawString(this.font, title, cx - this.font.width(title) / 2, barY - 14, ERROR_COLOR);

        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF1A2240);
        float lineProgress = shutdownTotalLines > 0 ? (float) shutdownLinesDone / shutdownTotalLines : 0f;
        float tickProgress = (float) shutdownTick / MAX_SHUTDOWN_TICKS;
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
        String subtitle = "SRE Laboratory Terminal v3.7";
        g.drawString(this.font, subtitle, cx - this.font.width(subtitle) / 2, barY - 18, OK_COLOR);

        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF1A2240);
        float progress = (float) bootTicks / MAX_BOOT_TICKS;
        int fillW = (int) (barW * progress);
        g.fill(barX, barY, barX + fillW, barY + barH, BAR_COLOR);

        String pct = (int) (progress * 100) + "%";
        g.drawString(this.font, pct, cx - this.font.width(pct) / 2, barY + barH + 6, TEXT_COLOR);

        String[] logs = {
            "Loading kernel",
            "Mounting filesystem",
            "Initializing drivers",
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

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (!booted || shuttingDown) return true;
        int max = visibleLines();
        scrollOffset = (int) Mth.clamp(scrollOffset - sy, 0, Math.max(0, outputLines.size() - max));
        return true;
    }

    @Override
    public void resize(Minecraft mc, int w, int h) {
        String saved = this.input != null ? this.input.getValue() : "";
        super.resize(mc, w, h);
        if (this.input != null) this.input.setValue(saved);
    }
}