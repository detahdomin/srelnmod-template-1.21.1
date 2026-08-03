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

public class InfoTerminalScreen extends AbstractContainerScreen<InfoTerminalMenu> implements TerminalInputHandler {

    private EditBox input;
    private final List<String> outputLines = new ArrayList<>();
    private final List<String> pendingLines = new ArrayList<>();
    private String typingLine;
    private final List<String> outputQueue = new ArrayList<>();
    private int outputQueueTick;
    private int typingChar;
    private int typingTick;
    private int scrollOffset;
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

    private boolean songActive;
    private int songTick;
    private int songLyricIndex;
    private int songTypewriterTick;
    private int songTypewriterChar;
    private String songCurrentLine;
    private int songCurrentAscii;
    private final List<String> songLyricHistory = new ArrayList<>();
    private boolean songCreditsActive;
    private int songCreditsPos;
    private float songCreditsAccum;
    private final List<String> songCreditsDisplayLines = new ArrayList<>();
    private static final int MAX_SONG_CREDITS_LINES = 8;

    private boolean vimActive;
    private boolean vimInsertMode;
    private String vimFile;
    private String vimContent;
    private boolean vimModified;
    private int vimCursorTick;

    private TerminalFileSystem fs;
    private String ip;
    private String prompt;

    private static final java.util.concurrent.ConcurrentHashMap<String, InfoTerminalScreen> NETWORK =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static final int BG_COLOR = 0xE0000000;
    private static final int HEADER_H = 12;
    private static final int INPUT_H = 14;
    private static final int BAR_COLOR = 0xFF5A9AFF;
    private static final int TEXT_COLOR = 0xFF5A9AFF;
    private static final int PROMPT_COLOR = 0xFF5A9AFF;
    private static final int ERROR_COLOR = 0xFF6B6B;
    private static final int OK_COLOR = 0x6BFF8E;
    private int currentTextColor = TEXT_COLOR;
    private static final int SCANLINE_COLOR = 0x08000000;

    private static final int LINE_HEIGHT = 9;
    private static final int MAX_CHARS_PER_LINE = 70;

    private static final int SONG_BG = 0xFF000000;
    private static final int SONG_BAR = 0xFF1A1A1A;
    private static final int SONG_TEXT = 0xFFFFCC00;
    private static final int SONG_MUTED = 0xFFCC9900;

    public InfoTerminalScreen(InfoTerminalMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 500;
        this.imageHeight = 300;
        var pos = menu.blockEntity.getBlockPos();
        this.ip = String.format("10.0.%d.%d", Math.abs(pos.getX()) % 256, Math.abs(pos.getZ()) % 256);
        this.fs = new TerminalFileSystem(this.ip);
        String name = playerInv.player.getGameProfile().getName();
        this.prompt = (name != null && isAscii(name)) ? name + "@sre:~$ " : "null@sre:~$ ";
    }

    private static boolean isAscii(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 127) return false;
        }
        return true;
    }

    @Override
    protected void init() {
        super.init();
        this.fs.seedDefaults();
        int inputBgY = this.topPos + this.imageHeight - INPUT_H - 2;
        int x = this.leftPos;
        int w = this.imageWidth;

        this.input = new EditBox(this.font, x + 1 + this.font.width(prompt), inputBgY + 1,
                w - 5 - this.font.width(prompt), 14, Component.empty());
        this.input.setMaxLength(256);
        this.input.setBordered(false);
        this.input.setTextColor(currentTextColor);
        this.input.setTextColorUneditable(currentTextColor);
        this.input.setFocused(true);
        this.input.setResponder(t -> {});
        this.input.setVisible(false);
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
                if (!shuttingDown) {
                    this.minecraft.player.playSound(ModSounds.FAN_HUM.get(), 0.3f, 1.0f);
                    enqueue("SRE Info Terminal v2.0");
                    enqueue("Type 'help' for commands.");
                    enqueue("");
                }
            }
        }

        if (booted && !welcomePlayed && !shuttingDown && typingLine == null && pendingLines.isEmpty()) {
            welcomePlayed = true;
            ready = true;
            NETWORK.put(this.ip, this);
            this.minecraft.player.playSound(ModSounds.WELCOME_VOICE.get(), 0.8f, 1.0f);
        }

        if (booted) {
            fanHumTimer++;
            if (fanHumTimer >= FAN_HUM_INTERVAL) {
                fanHumTimer = 0;
                this.minecraft.player.playSound(ModSounds.FAN_HUM.get(), 0.3f, 1.0f);
            }
        }

        if (songActive) {
            songTick++;
            processSongTick();
        }
        if (songCreditsActive) {
            processCredits();
        }

        if (vimInsertMode) vimCursorTick++;

        if (shuttingDown) {
            shutdownTick++;
        }

        this.input.setVisible(ready && !shuttingDown && !songActive && !songCreditsActive);

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

        if (typingLine == null && !outputQueue.isEmpty()) {
            outputQueueTick++;
            if (outputQueueTick >= 3) {
                outputQueueTick = 0;
                addWrapped(outputQueue.remove(0));
            }
        }
    }

    private void processSongTick() {
        int timeMs = songTick * 50;

        while (songLyricIndex < SongData.LYRICS.length) {
            SongLyric lyric = SongData.LYRICS[songLyricIndex];
            if (lyric.timeMs() * 10L > timeMs) break;
            songLyricIndex++;

            switch (lyric.mode()) {
                case 0:
                case 1:
                    if (songCurrentLine != null) {
                        songLyricHistory.add(songCurrentLine);
                    }
                    songCurrentLine = lyric.words().replace("\0", "");
                    if (songCurrentLine.isEmpty()) {
                        songCurrentLine = null;
                        break;
                    }
                    songTypewriterChar = 0;
                    songTypewriterTick = 0;
                    break;
                case 2:
                    if (songCurrentLine != null) {
                        songLyricHistory.add(songCurrentLine);
                        songCurrentLine = null;
                    }
                    try {
                        songCurrentAscii = Integer.parseInt(lyric.words());
                    } catch (NumberFormatException e) {
                        songCurrentAscii = -1;
                    }
                    break;
                case 3:
                    songLyricHistory.clear();
                    songCurrentLine = null;
                    songTypewriterChar = 0;
                    break;
                case 4:
                    this.minecraft.player.playSound(ModSounds.STILL_ALIVE.get(), 0.8f, 1.0f);
                    break;
                case 5:
                    songCreditsActive = true;
                    songCreditsPos = 0;
                    songCreditsAccum = 0;
                    songCreditsDisplayLines.clear();
                    break;
                case 9:
                    songActive = false;
                    stopSongSounds();
                    addWrapped("  [SYS] Playback complete.");
                    return;
            }
        }

        if (songCurrentLine != null) {
            processSongTypewriter();
        }
    }

    private void processSongTypewriter() {
        if (songTypewriterChar >= songCurrentLine.length()) return;

        double intervalSec;
        SongLyric prev = SongData.LYRICS[songLyricIndex - 1];
        if (prev.interval() > 0) {
            intervalSec = prev.interval() / Math.max(1, songCurrentLine.length());
        } else {
            int nextTime = songLyricIndex < SongData.LYRICS.length
                    ? SongData.LYRICS[songLyricIndex].timeMs() : prev.timeMs() + 5000;
            intervalSec = (nextTime - prev.timeMs()) / 100.0 / Math.max(1, songCurrentLine.length());
        }
        int intervalTicks = Math.max(2, (int) (intervalSec * 20));

        songTypewriterTick++;
        if (songTypewriterTick >= intervalTicks) {
            songTypewriterTick = 0;
            songTypewriterChar++;
            if (songTypewriterChar >= songCurrentLine.length()) {
                songLyricHistory.add(songCurrentLine);
                songCurrentLine = null;
            }
        }
    }

    private void processCredits() {
        String credits = SongData.CREDITS;
        if (songCreditsPos >= credits.length()) return;

        int creditsLen = credits.length();
        double totalSec = 174.0;
        double charsPerTick = creditsLen / (totalSec * 20.0);

        songCreditsAccum += (float) charsPerTick;
        while (songCreditsAccum >= 1.0f && songCreditsPos < credits.length()) {
            songCreditsAccum -= 1.0f;

            char ch = credits.charAt(songCreditsPos);
            songCreditsPos++;

            if (ch == '\n') {
                songCreditsDisplayLines.add("");
                if (songCreditsDisplayLines.size() > MAX_SONG_CREDITS_LINES) {
                    songCreditsDisplayLines.remove(0);
                }
            } else {
                if (songCreditsDisplayLines.isEmpty()) {
                    songCreditsDisplayLines.add("");
                }
                int last = songCreditsDisplayLines.size() - 1;
                songCreditsDisplayLines.set(last, songCreditsDisplayLines.get(last) + ch);
            }
        }

        if (songCreditsPos >= creditsLen) {
            songCreditsActive = false;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (vimActive) {
            if (keyCode == 256) {
                if (vimInsertMode) {
                    vimInsertMode = false;
                    return true;
                }
                return true;
            }
            if (keyCode == 257 || keyCode == 335) {
                if (vimInsertMode) {
                    vimContent = (vimContent != null ? vimContent : "") + "\n";
                    vimModified = true;
                    return true;
                }
                String cmd = this.input.getValue().trim();
                if (!cmd.isEmpty()) {
                    processVimCommand(":" + cmd);
                }
                return true;
            }
            if (vimInsertMode) {
                if (keyCode == 259 && vimContent != null && !vimContent.isEmpty()) {
                    vimContent = vimContent.substring(0, vimContent.length() - 1);
                    return true;
                }
                return true;
            }
            if (!vimInsertMode && keyCode == GLFW.GLFW_KEY_I) {
                vimInsertMode = true;
                vimCursorTick = 0;
                return true;
            }
            this.input.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        if (songActive || songCreditsActive) {
            if (keyCode == 256) {
                stopSong();
                return true;
            }
            return true;
        }

        boolean ctrl = (modifiers & 2) != 0;

        if (ctrl && keyCode == 76) {
            outputLines.clear();
            pendingLines.clear();
            typingLine = null;
            addWrapped("SRE Info Terminal v2.0");
            scrollOffset = 0;
            return true;
        }
        if (ctrl && keyCode == 67) {
            this.input.setValue("");
            return true;
        }
        if (ctrl && keyCode == 65) {
            this.input.setCursorPosition(0);
            return true;
        }
        if (ctrl && keyCode == 69) {
            this.input.setCursorPosition(this.input.getValue().length());
            return true;
        }
        if (keyCode == 268) {
            this.input.setCursorPosition(0);
            return true;
        }
        if (keyCode == 269) {
            this.input.setCursorPosition(this.input.getValue().length());
            return true;
        }

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
        stopSongSounds();
        this.fs.saveUserData();
        this.fs.resetLocal();
        NETWORK.remove(this.ip);
        this.minecraft.getSoundManager().stop(
                ResourceLocation.fromNamespaceAndPath("sreln_mod", "fan_hum"), SoundSource.PLAYERS);
        this.minecraft.getSoundManager().stop(
                ResourceLocation.fromNamespaceAndPath("sreln_mod", "welcome_voice"), SoundSource.PLAYERS);
    }

    private void stopSong() {
        songActive = false;
        songCreditsActive = false;
        stopSongSounds();
        addWrapped("  [SYS] Playback stopped.");
        scrollOffset = Math.max(0, outputLines.size() - visibleLines());
    }

    private void stopSongSounds() {
        this.minecraft.getSoundManager().stop(
                ResourceLocation.fromNamespaceAndPath("sreln_mod", "still_alive"), SoundSource.PLAYERS);
    }

    private void startShutdown() {
        shuttingDown = true;
        shutdownLinesDone = 0;
        shutdownTotalLines = 4;
        shutdownTick = 0;
        stopSongSounds();
        this.minecraft.getSoundManager().stop(
                ResourceLocation.fromNamespaceAndPath("sreln_mod", "welcome_voice"), SoundSource.PLAYERS);
        this.minecraft.player.playSound(ModSounds.SHUTDOWN_VOICE.get(), 0.8f, 1.0f);
        enqueue("[SYS] Shutdown signal received...");
        enqueue("[SYS] Terminating processes...");
        enqueue("[SYS] Unmounting filesystems...");
        enqueue("[SYS] Halting system...");
        scrollOffset = Math.max(0, outputLines.size() - visibleLines());
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (vimActive) {
            if (vimInsertMode) {
                if (codePoint >= 32) {
                    vimContent = (vimContent != null ? vimContent : "") + codePoint;
                    vimModified = true;
                }
                return true;
            }
            if (this.input.charTyped(codePoint, modifiers)) {
                this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.15f, 1.6f);
                return true;
            }
            return true;
        }
        if (!ready || shuttingDown || songActive) return true;
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

    private void queueOutput(String line) {
        outputQueue.add(line);
    }

    private void addWrapped(String line) {
        for (String part : line.split("\n", -1)) {
            addWrappedSingle(part);
        }
    }

    private void addWrappedSingle(String line) {
        if (line.length() <= MAX_CHARS_PER_LINE) {
            outputLines.add(line);
            return;
        }
        int i = 0;
        while (i < line.length()) {
            int end = Math.min(i + MAX_CHARS_PER_LINE, line.length());
            outputLines.add(line.substring(i, end));
            i = end;
        }
    }

    public void execute(String cmd) {
        addWrapped(this.prompt + cmd);
        String upper = cmd.toUpperCase();
        String[] parts = cmd.split("\\s+", 2);
        String first = parts[0].toLowerCase();

        switch (first) {
            case "help":
                queueOutput("  status           - 系统状态");
                queueOutput("  clear, cls       - 清屏");
                queueOutput("  about            - 关于");
                queueOutput("  exit, shutdown   - 关机退出");
                queueOutput("  ls [dir]         - 列出文件");
                queueOutput("  cat <file>       - 查看文件内容");
                queueOutput("  touch <file>     - 创建文件");
                queueOutput("  rm <file>        - 删除文件");
                queueOutput("  mkdir <dir>      - 创建文件夹");
                queueOutput("  vim <file>       - 编辑文件");
                queueOutput("  cd <dir>         - 切换目录");
                queueOutput("  pwd              - 当前目录");
                queueOutput("  ./<file>         - 执行文件");
                queueOutput("  neofetch         - 系统信息");
                queueOutput("  whoami           - 当前用户");
                queueOutput("  date             - 系统时间");
                queueOutput("  ping <host>      - 网络测试");
                queueOutput("  ipconfig         - 查看本机IP");
                queueOutput("  msg <IP> <消息>   - 发送消息");
                queueOutput("  sudo <cmd>       - 超级用户");
                queueOutput("  color <name>     - 更改字体颜色");
                break;
            case "clear":
            case "cls":
                outputLines.clear();
                pendingLines.clear();
                typingLine = null;
                addWrapped("SRE Info Terminal v2.0");
                return;
            case "status":
                queueOutput("  System: 在线");
                queueOutput("  Kernel: SREL-OS 3.7 (info)");
                queueOutput("  Memory: 65536K OK");
                queueOutput("  Storage: 2 files, 1 executable");
                break;
            case "about":
                queueOutput("  SRE Info Terminal v2.0");
                queueOutput("  (c) 2026 SRE Institute");
                queueOutput("  File system module active");
                break;
            case "exit":
            case "shutdown":
                startShutdown();
                return;
            case "pwd":
                addWrapped(this.fs.pwd());
                break;
            case "ls":
                if (parts.length > 1) {
                    addWrapped(this.fs.ls(parts[1]));
                } else {
                    addWrapped(this.fs.ls());
                }
                break;
            case "cd":
                if (parts.length > 1) {
                    String err = this.fs.cd(parts[1]);
                    if (!err.isEmpty()) addWrapped(err);
                } else {
                    addWrapped("  用法: cd <目录>");
                }
                break;
            case "touch":
                if (parts.length > 1) {
                    String err = this.fs.touch(parts[1]);
                    if (!err.isEmpty()) addWrapped(err);
                } else {
                    addWrapped("  用法: touch <文件名>");
                }
                break;
            case "mkdir":
                if (parts.length > 1) {
                    addWrapped(this.fs.mkdir(parts[1]));
                } else {
                    addWrapped("  用法: mkdir <目录名>");
                }
                break;
            case "vim":
                if (parts.length > 1) {
                    startVim(parts[1]);
                } else {
                    addWrapped("  用法: vim <文件名>");
                }
                break;
            case "cat":
                if (parts.length > 1) {
                    addWrapped(this.fs.cat(parts[1]));
                } else {
                    addWrapped("  用法: cat <文件名>");
                }
                break;
            case "rm":
                if (parts.length > 1) {
                    addWrapped(this.fs.rm(parts[1]));
                } else {
                    addWrapped("  用法: rm <文件名>");
                }
                break;
            case "neofetch":
                queueOutput("      .--.");
                queueOutput("     |o_o |     SRE 研究所");
                queueOutput("     |:_/ |     ----------");
                queueOutput("    //   \\ \\    OS: SREL-OS 3.7");
                queueOutput("   (|     | )   内核: sreln-kernel");
                queueOutput("  /'\\_   _/`\\   终端: 信息终端 v2.0");
                queueOutput("  \\___)=(___/   内存: 65536K");
                queueOutput("                用户: researcher");
                break;
            case "whoami":
                addWrapped("  researcher@sre-lab");
                break;
            case "date":
                addWrapped("  " + getGameDate());
                break;
            case "ping":
                if (parts.length > 1) {
                    String target = parts[1];
                    if ("127.0.0.1".equals(target) || "localhost".equals(target)) {
                        queueOutput("  PING 127.0.0.1: 56 data bytes");
                        queueOutput("  64 bytes from 127.0.0.1: icmp_seq=0 ttl=64 time=0.042 ms");
                        queueOutput("  64 bytes from 127.0.0.1: icmp_seq=1 ttl=64 time=0.038 ms");
                        queueOutput("  64 bytes from 127.0.0.1: icmp_seq=2 ttl=64 time=0.041 ms");
                        queueOutput("  --- 127.0.0.1 ping statistics ---");
                        queueOutput("  3 packets transmitted, 3 received, 0% loss");
                    } else if (NETWORK.containsKey(target)) {
                        queueOutput("  PING " + target + ": 56 data bytes");
                        queueOutput("  64 bytes from " + target + ": icmp_seq=0 ttl=64 time=0.042 ms");
                        queueOutput("  64 bytes from " + target + ": icmp_seq=1 ttl=64 time=0.038 ms");
                        queueOutput("  64 bytes from " + target + ": icmp_seq=2 ttl=64 time=0.041 ms");
                        queueOutput("  --- " + target + " ping statistics ---");
                        queueOutput("  3 packets transmitted, 3 received, 0% loss");
                    } else {
                        addWrapped("  ping: " + target + ": 目标主机不可达");
                    }
                } else {
                    addWrapped("  用法: ping <host|IP>");
                }
                break;
            case "ipconfig":
                queueOutput("  IP: " + this.ip);
                queueOutput("  子网掩码: 255.255.255.0");
                queueOutput("  网关: 10.0.0.1");
                break;
            case "msg":
                if (parts.length < 2) {
                    addWrapped("  用法: msg <IP> <消息>");
                } else {
                    String[] msgParts = parts[1].split("\\s+", 2);
                    if (msgParts.length < 2) {
                        addWrapped("  用法: msg <IP> <消息>");
                    } else {
                        InfoTerminalScreen target = NETWORK.get(msgParts[0]);
                        if (target != null) {
                            target.receiveMsg(this.ip, msgParts[1]);
                            addWrapped("  [MSG] 已发送至 " + msgParts[0]);
                        } else {
                            addWrapped("  [ERR] 目标终端不在线: " + msgParts[0]);
                        }
                    }
                }
                break;
            case "sudo":
                addWrapped("  [ERR] 你不是 root。但 root 也不存在。");
                addWrapped("  [ERR] 这里没有超级用户，只有超级研究员。");
                break;
            case "color":
                if (parts.length > 1) {
                    String colorName = parts[1].toLowerCase();
                    switch (colorName) {
                        case "blue", "蓝" -> currentTextColor = 0xFF5A9AFF;
                        case "yellow", "黄" -> currentTextColor = 0xFFFFCC00;
                        case "green", "绿" -> currentTextColor = 0xFF6BFF8E;
                        case "red", "红" -> currentTextColor = 0xFFFF6B6B;
                        case "white", "白" -> currentTextColor = 0xFFFFFFFF;
                        case "cyan", "青" -> currentTextColor = 0xFF00FFFF;
                        case "magenta", "紫" -> currentTextColor = 0xFFFF00FF;
                        default -> {
                            addWrapped("  可用颜色: blue/yellow/green/red/white/cyan/magenta");
                            addWrapped("  或: 蓝/黄/绿/红/白/青/紫");
                            scrollOffset = Math.max(0, outputLines.size() - visibleLines());
                            return;
                        }
                    }
                    addWrapped("  字体颜色已切换为: " + colorName);
                    this.input.setTextColor(currentTextColor);
                    this.input.setTextColorUneditable(currentTextColor);
                } else {
                    addWrapped("  用法: color <颜色名>");
                    addWrapped("  可用: blue/yellow/green/red/white/cyan/magenta");
                }
                break;
            default:
                if (upper.startsWith("./")) {
                    String exe = cmd.substring(2).trim();
                    if (this.fs.isExecutable(exe)) {
                        startSong(exe);
                    } else if (this.fs.exists(exe)) {
                        addWrapped("  [ERR] " + exe + " 不可执行");
                    } else {
                        addWrapped("  [ERR] 文件不存在: " + exe);
                    }
                } else {
                    addWrapped("  未知命令: " + first);
                }
                break;
        }
        scrollOffset = Math.max(0, outputLines.size() - visibleLines());
    }

    public void receiveMsg(String fromIp, String message) {
        addWrapped("[MSG] 来自 " + fromIp + ": " + message);
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.5f, 1.2f);
        }
    }

    private void startSong(String exe) {
        songActive = true;
        songTick = 0;
        songLyricIndex = 0;
        songTypewriterTick = 0;
        songTypewriterChar = 0;
        songCurrentLine = null;
        songCurrentAscii = -1;
        songLyricHistory.clear();
        songCreditsActive = false;
        songCreditsPos = 0;
        songCreditsAccum = 0;
        songCreditsDisplayLines.clear();
        outputLines.clear();
        pendingLines.clear();
        typingLine = null;
    }

    private void startVim(String path) {
        vimActive = true;
        vimInsertMode = false;
        vimFile = path;
        vimModified = false;
        String content = this.fs.getVimContent(path);
        if (content == null) {
            addWrapped("  [ERR] " + path + " 是目录");
            vimActive = false;
            vimFile = null;
            scrollOffset = Math.max(0, outputLines.size() - visibleLines());
            return;
        }
        vimContent = content;
        this.input.setValue("");
        this.input.setSuggestion(":w 保存  :q 退出  :wq 保存退出  :q! 强制退出  i 插入模式");
        this.input.setVisible(true);
        this.setFocused(this.input);
    }

    private void stopVim() {
        vimActive = false;
        vimInsertMode = false;
        vimFile = null;
        vimContent = null;
        vimModified = false;
        this.input.setValue("");
        this.input.setSuggestion(null);
        this.input.setX(this.leftPos + 1 + this.font.width(this.prompt));
        this.input.setWidth(this.imageWidth - 5 - this.font.width(this.prompt));
        this.input.setVisible(ready && !shuttingDown && !songActive && !songCreditsActive && !vimActive);
        this.setFocused(this.input);
    }

    private void processVimCommand(String cmd) {
        switch (cmd) {
            case ":w" -> {
                vimContent = vimContent != null ? vimContent : "";
                this.fs.vimSave(vimFile, vimContent);
                vimModified = false;
                addWrapped("\"" + vimFile + "\" written");
                this.input.setValue("");
            }
            case ":q" -> {
                if (vimModified) {
                    addWrapped("  [ERR] 未保存修改! 使用 :q! 强制退出");
                    this.input.setValue("");
                } else {
                    stopVim();
                    return;
                }
            }
            case ":wq" -> {
                vimContent = vimContent != null ? vimContent : "";
                this.fs.vimSave(vimFile, vimContent);
                stopVim();
                return;
            }
            case ":q!" -> {
                stopVim();
                return;
            }
            case "i" -> {
                vimInsertMode = true;
                this.input.setValue("");
                this.input.setSuggestion("-- INSERT --  ESC 退出插入模式");
            }
            default -> {
                if (cmd.startsWith(":")) {
                    addWrapped("  [ERR] 未知vim命令: " + cmd);
                }
                this.input.setValue("");
            }
        }
        scrollOffset = Math.max(0, outputLines.size() - visibleLines());
    }

    @Override
    public boolean isHacker() { return false; }

    @Override
    public List<String> buildCompletions() {
        List<String> completions = new ArrayList<>();
        completions.addAll(List.of("help", "status", "clear", "about", "exit", "shutdown",
                "ls", "cat ", "pwd", "cd ", "cls", "neofetch", "whoami", "date",
                "ping ", "ipconfig", "msg ", "sudo ", "color ", "touch ", "rm ", "mkdir ", "vim ",
                "network/".equals(this.fs.getCwdName()) ? "./still_alive.exe" : "./network/still_alive.exe"));
        for (String f : this.fs.getReadableFiles()) {
            completions.add("cat " + f);
            completions.add("vim " + f);
            completions.add("rm " + f);
            completions.add(f);
        }
        for (String d : this.fs.getDirectories()) {
            completions.add("cd " + d);
            completions.add("ls " + d);
            completions.add(d);
        }
        completions.add("cd ..");
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
    public boolean isReady() { return ready && !songActive && !songCreditsActive; }

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

    private int visibleLines() {
        return (this.imageHeight - HEADER_H - INPUT_H - 4) / LINE_HEIGHT;
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
        boolean songMode = songActive || songCreditsActive;

        g.fill(x, y, x + w, y + h, songMode ? SONG_BG : BG_COLOR);
        g.fill(x + 1, y + 1, x + w - 1, y + HEADER_H, songMode ? SONG_BAR : 0xFF1A1A1A);

        drawScanlines(g, x, y, w, h);

        if (shuttingDown) {
            renderShutdown(g, x, y, w, h);
            return;
        }

        if (songMode) {
            renderSong(g, x, y, w, h);
            return;
        }

        g.drawString(this.font, "TERMINAL", x + 6, y + 2, PROMPT_COLOR);
        String status = booted ? "ONLINE" : "BOOTING";
        g.drawString(this.font, status, x + w - this.font.width(status) - 6, y + 2, TEXT_COLOR);

        if (!booted) {
            renderBoot(g, x, y, w, h);
            return;
        }

        if (vimActive) {
            renderVim(g, x, y, w, h);
            return;
        }

        int outputY = y + HEADER_H + 4;
        int visibleOutput = Math.min(outputLines.size() + (typingLine != null ? 1 : 0), visibleLines());
        int inputBgY = outputY + visibleOutput * LINE_HEIGHT + 2;
        this.input.setY(inputBgY + 1);

        g.drawString(this.font, this.prompt, x + 5, inputBgY + 1, PROMPT_COLOR);

        renderOutputLines(g, x, y);
    }

    private void renderOutputLines(GuiGraphics g, int x, int y) {
        int oy = y + HEADER_H + 4;
        int max = visibleLines();
        int totalLines = outputLines.size() + (typingLine != null ? 1 : 0);
        int start = Math.max(0, totalLines - max);
        for (int i = start; i < outputLines.size(); i++) {
            String line = outputLines.get(i);
            int ly = oy + (i - start) * LINE_HEIGHT;
            int c = currentTextColor;
            if (line.startsWith(this.prompt)) c = PROMPT_COLOR;
            else if (line.contains("[ERR]") || line.contains("Unknown")) c = ERROR_COLOR;
            else if (line.startsWith("  ")) c = OK_COLOR;
            g.drawString(this.font, line, x + 4, ly, c);
        }
        if (typingLine != null) {
            int ly = oy + (outputLines.size() - start) * LINE_HEIGHT;
            String partial = typingLine.substring(0, Math.min(typingChar, typingLine.length()));
            int c = partial.startsWith(this.prompt) ? PROMPT_COLOR
                    : partial.contains("Unknown") ? ERROR_COLOR
                    : partial.startsWith("  ") ? OK_COLOR : currentTextColor;
            g.drawString(this.font, partial, x + 4, ly, c);
        }
    }

    private void renderVim(GuiGraphics g, int x, int y, int w, int h) {
        int oy = y + HEADER_H + 4;
        int maxLines = visibleLines() - 2;

        g.drawString(this.font, "VIM - " + vimFile, x + 6, oy, PROMPT_COLOR);
        oy += LINE_HEIGHT + 2;

        String content = vimContent != null ? vimContent : "";
        String[] lines = content.split("\n", -1);
        int start = Math.max(0, lines.length - maxLines);
        for (int i = start; i < lines.length; i++) {
            int ly = oy + (i - start) * LINE_HEIGHT;
            String prefix = (i < lines.length - 1 || !content.isEmpty()) ? "  " : "";
            g.drawString(this.font, prefix + lines[i], x + 4, ly, TEXT_COLOR);
        }

        String modeStr = vimInsertMode ? "-- INSERT --" : (vimModified ? "[+]" : "");
        g.drawString(this.font, modeStr, x + w - this.font.width(modeStr) - 6, oy + maxLines * LINE_HEIGHT, TEXT_COLOR);

        if (vimInsertMode) {
            String lastLine = lines.length > 0 ? lines[lines.length - 1] : "";
            int cursorX = x + 4 + this.font.width("  " + lastLine);
            int cursorY = oy + Math.min(lines.length - start, maxLines) * LINE_HEIGHT;
            if ((vimCursorTick / 20) % 2 == 0) {
                g.fill(cursorX, cursorY, cursorX + 1, cursorY + LINE_HEIGHT, 0xFFFFFFFF);
            }
            this.input.setVisible(false);
        } else {
            int inputY = oy + maxLines * LINE_HEIGHT + 2;
            g.drawString(this.font, ":", x + 5, inputY + 1, PROMPT_COLOR);
            this.input.setX(x + 1 + this.font.width(":"));
            this.input.setY(inputY + 1);
            this.input.setWidth(w - 5 - this.font.width(":"));
            this.input.setVisible(true);
        }
    }

    private void renderSong(GuiGraphics g, int x, int y, int w, int h) {
        int timeMs = songTick * 50;
        int sec = timeMs / 1000;
        int min = sec / 60;
        sec = sec % 60;
        String timeStr = String.format("%02d:%02d", min, sec);

        g.drawString(this.font, "NOW PLAYING: Still Alive", x + 6, y + 4, SONG_TEXT);
        g.drawString(this.font, timeStr, x + w - this.font.width(timeStr) - 6, y + 4, SONG_MUTED);

        int contentX = x + 2;
        int contentY = y + HEADER_H + 2;
        int contentW = w - 4;
        int contentH = h - HEADER_H - 2 - 20;
        int pipeW = this.font.width("|");
        int mx = contentX + contentW / 2;
        int rightPipeX = contentX + contentW - pipeW;

        int rightX = mx + pipeW;

        int borderBottom = contentY + (contentH / LINE_HEIGHT) * LINE_HEIGHT + LINE_HEIGHT - 2;
        int bottomLineY = borderBottom + 5;
        g.fill(x, y, x + w, y + h, 0xFF000000);
        drawSongBorder(g, contentX, contentY, contentW, contentH);

        int lyricX = contentX + pipeW + 2;
        int lyricY = contentY + LINE_HEIGHT + 2; // start at row 1 (below top dashes)
        int lyricAreaH = contentH - LINE_HEIGHT; // minus top border row
        int maxLyricLines = lyricAreaH / LINE_HEIGHT;

        int totalLyricLines = songLyricHistory.size() + (songCurrentLine != null ? 1 : 0);
        int startLyric = Math.max(0, totalLyricLines - maxLyricLines);

        int drawn = 0;
        for (int i = startLyric; i < songLyricHistory.size() && drawn < maxLyricLines; i++) {
            g.drawString(this.font, songLyricHistory.get(i), lyricX, lyricY + drawn * LINE_HEIGHT, SONG_TEXT);
            drawn++;
        }
        if (songCurrentLine != null && drawn < maxLyricLines) {
            String partial = songCurrentLine.substring(0, Math.min(songTypewriterChar, songCurrentLine.length()));
            g.drawString(this.font, partial, lyricX, lyricY + drawn * LINE_HEIGHT, SONG_TEXT);
        }

        int asciiTop = contentY + 3 * LINE_HEIGHT + 15;
        int maxAsciiH = contentY + contentH - asciiTop - 2;
        if (songCurrentAscii >= 0 && songCurrentAscii < SongData.ASCII_ART.length && maxAsciiH > 0) {
            String[] art = SongData.ASCII_ART[songCurrentAscii];
            int rightPanelW = rightPipeX - rightX;
            int maxArtChars = 0;
            for (String line : art) {
                if (line.length() > maxArtChars) maxArtChars = line.length();
            }

            float baseCellW = this.font.width(" ") * 1.5f;
            float baseCellH = LINE_HEIGHT;
            float scaleW = (float)rightPanelW / (maxArtChars * baseCellW);
            float scaleH = (float)maxAsciiH / (art.length * baseCellH);
            float scale = Math.min(scaleW, scaleH);

            int cellW = Math.max(1, (int)(baseCellW * scale));
            int cellH = Math.max(1, (int)(baseCellH * scale));

            int artPixelW = maxArtChars * cellW;
            int artPixelH = art.length * cellH;
            int asciiX = rightX + (rightPanelW - artPixelW) / 2;
            int asciiY = asciiTop + (maxAsciiH - artPixelH) / 2;

            for (int i = 0; i < art.length; i++) {
                String line = art[i];
                for (int j = 0; j < line.length(); j++) {
                    char c = line.charAt(j);
                    if (c != ' ') {
                        g.drawString(this.font, String.valueOf(c), asciiX + j * cellW, asciiY + i * cellH, SONG_TEXT);
                    }
                }
            }
        }

        int maxCreditLines = 2;
        if (maxCreditLines > 0 && !songCreditsDisplayLines.isEmpty()) {
            for (int i = 0; i < songCreditsDisplayLines.size() && i < maxCreditLines; i++) {
                String line = songCreditsDisplayLines.get(songCreditsDisplayLines.size() - 1 - i);
                g.drawString(this.font, line, rightX, contentY + LINE_HEIGHT + 4 + i * LINE_HEIGHT, SONG_TEXT);
            }
        }

        g.fill(contentX + pipeW, bottomLineY, contentX + contentW - pipeW, bottomLineY + 1, SONG_TEXT);
        g.drawString(this.font, "ESC to stop", x + 6, bottomLineY + 4, SONG_MUTED);
    }

    private void drawSongBorder(GuiGraphics g, int bx, int by, int bw, int h) {
        int lineH = LINE_HEIGHT;
        int creditsLines = 2;
        int totalRows = h / lineH;
        int color = SONG_TEXT;

        int pipeW = this.font.width("|");
        int dashW = this.font.width("-");
        int mx = bx + bw / 2; // center single | (shared by both panels)
        int rightPipeX = bx + bw - pipeW;

        int topDashY = by + lineH - 2; // dashes at bottom of row 0, touching row 1 |

        // Row 0: top dashes only (no |) - matching Python ' ' + '-'*N + '  ' + '-'*N + ' '
        drawDashedH(g, bx + pipeW, topDashY, mx, color, dashW);
        drawDashedH(g, mx + pipeW, topDashY, rightPipeX, color, dashW);

        // Rows 1-2: |    |    |  (credits rows, single | in middle, both sides 2 pipes)
        for (int i = 1; i <= creditsLines; i++) {
            int rowY = by + i * lineH;
            g.drawString(this.font, "|", bx, rowY, color);
            g.drawString(this.font, "|", mx, rowY, color);
            g.drawString(this.font, "|", rightPipeX, rowY, color);
        }

        // Row 3: |    |----|  (separator)
        int sepY = by + (creditsLines + 1) * lineH;
        int sepDashY = sepY + lineH - 2;
        g.drawString(this.font, "|", bx, sepY, color);
        g.drawString(this.font, "|", mx, sepY, color);
        drawDashedH(g, mx + pipeW, sepDashY, rightPipeX, color, dashW);
        g.drawString(this.font, "|", rightPipeX, sepY, color);

        // Rows 4+: |    |   (left panel only, no right |)
        for (int i = creditsLines + 2; i <= totalRows; i++) {
            int rowY = by + i * lineH;
            g.drawString(this.font, "|", bx, rowY, color);
            g.drawString(this.font, "|", mx, rowY, color);
        }

        // Bottom: |----|   (left panel only)
        int botY = by + totalRows * lineH;
        int botDashY = botY + lineH - 2;
        g.drawString(this.font, "|", bx, botY, color);
        drawDashedH(g, bx + pipeW, botDashY, mx, color, dashW);
        g.drawString(this.font, "|", mx, botY, color);
    }

    private void drawDashedH(GuiGraphics g, int x1, int y, int x2, int color, int dashW) {
        int gap = 1;
        int step = dashW + gap;
        for (int x = x1; x + dashW <= x2; x += step) {
            g.fill(x, y, x + dashW, y + 1, color);
        }
    }

    private void renderBoot(GuiGraphics g, int x, int y, int w, int h) {
        int cx = x + w / 2;
        int barW = 200, barH = 10;
        int barX = cx - barW / 2, barY = y + h / 2 - 10;

        String title = "SREL-OS v3.7";
        g.drawString(this.font, title, cx - this.font.width(title) / 2, barY - 28, PROMPT_COLOR);
        String subtitle = "SRE Info Terminal v2.0";
        g.drawString(this.font, subtitle, cx - this.font.width(subtitle) / 2, barY - 18, OK_COLOR);

        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF1A1A1A);
        float progress = (float) bootTicks / MAX_BOOT_TICKS;
        int fillW = (int) (barW * progress);
        g.fill(barX, barY, barX + fillW, barY + barH, BAR_COLOR);

        String pct = (int) (progress * 100) + "%";
        g.drawString(this.font, pct, cx - this.font.width(pct) / 2, barY + barH + 6, TEXT_COLOR);

        String[] logs = {
            "Loading kernel",
            "Mounting filesystem",
            "Initializing info module",
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

    private void renderShutdown(GuiGraphics g, int x, int y, int w, int h) {
        int cx = x + w / 2;
        int barW = 200, barH = 10;
        int barX = cx - barW / 2, barY = y + h / 2;

        String title = "SYSTEM SHUTDOWN";
        g.drawString(this.font, title, cx - this.font.width(title) / 2, barY - 14, ERROR_COLOR);

        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF1A1A1A);
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
                g.drawString(this.font, line, x + 4, lineY + drawn * 10, ERROR_COLOR);
                drawn++;
            }
        }
        if (typingLine != null) {
            String partial = typingLine.substring(0, Math.min(typingChar, typingLine.length()));
            g.drawString(this.font, partial, x + 4, lineY + drawn * 10, ERROR_COLOR);
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
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        scrollOffset = Mth.clamp(scrollOffset - (int) scrollY, 0, Math.max(0, outputLines.size() - visibleLines()));
        return true;
    }

    private String getGameDate() {
        java.time.LocalDateTime base = java.time.LocalDateTime.of(2335, 8, 1, 0, 0, 0);
        if (this.minecraft == null || this.minecraft.level == null) {
            return base.toString().replace("T", " ");
        }
        long days = this.minecraft.level.getGameTime() / 24000;
        java.time.LocalDateTime gameDate = base.plusDays(days);
        return gameDate.toString().replace("T", " ");
    }
}