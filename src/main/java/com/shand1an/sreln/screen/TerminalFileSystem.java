package com.shand1an.sreln.screen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TerminalFileSystem {

    static class FileNode {
        String name;
        boolean isDirectory;
        String content = "";
        boolean isExecutable;
        boolean isNetwork;
        FileNode parent;
        Map<String, FileNode> children = new LinkedHashMap<>();

        FileNode(String name, boolean isDirectory) {
            this.name = name;
            this.isDirectory = isDirectory;
        }
    }

    private static final Map<String, Map<String, String>> USER_DATA = new ConcurrentHashMap<>();

    private static final FileNode CLOUD;
    static {
        CLOUD = new FileNode("network/", true);
        CLOUD.isNetwork = true;
        FileNode exe = new FileNode("still_alive.exe", false);
        exe.isExecutable = true;
        exe.isNetwork = true;
        CLOUD.children.put("still_alive.exe", exe);
        exe.parent = CLOUD;
    }

    private static final String RESEARCH_LOG_1 = "research_log_01.txt";
    private static final String RESEARCH_LOG_2 = "research_log_02.txt";
    private static final String R1_CONTENT = """
            ========================================
             SRE LABORATORY - RESEARCH LOG #01
            ========================================
            Date: 2025-11-17
            Author: Dr. [REDACTED]
            Clearance: LEVEL 4

            Subject 47 continues to show remarkable
            adaptation to the neuro-interface. We've
            successfully mapped 83% of the synaptic
            pathways. The subject reports vivid dreams
            of geometric patterns and a persistent
            humming sound at approximately 440Hz.

            Note: Subject 47's vitals spiked during
            the resonant frequency test. Recommend
            reducing exposure by 30% for next session.
            """;
    private static final String R2_CONTENT = """
            ========================================
             SRE LABORATORY - RESEARCH LOG #02
            ========================================
            Date: 2025-12-03
            Author: Dr. [REDACTED]
            Clearance: LEVEL 5

            The resonance cascade experiment exceeded
            all projections. The boundary between our
            dimension and [REDACTED] has thinned to
            approximately 0.03 Planck lengths.

            Side effects include: temporal anomalies
            in Sector 7, unexplained shadows in the
            lower levels, and the persistent feeling
            of being watched. All personnel have been
            advised to avoid Sector 7 until further
            notice.

            The test must continue. The board has
            authorized Phase 3.
            """;

    private final FileNode root;
    private FileNode cwd;
    final String ip;

    public TerminalFileSystem(String ip) {
        this.ip = ip;
        this.root = new FileNode("/", true);
        this.cwd = root;
        root.children.put("network/", CLOUD);
        CLOUD.parent = root;
        seedDefaultFile(RESEARCH_LOG_1, R1_CONTENT);
        seedDefaultFile(RESEARCH_LOG_2, R2_CONTENT);
        loadUserData();
    }

    private void loadUserData() {
        Map<String, String> files = USER_DATA.get(ip);
        if (files != null) {
            for (Map.Entry<String, String> e : files.entrySet()) {
                FileNode f = new FileNode(e.getKey(), false);
                f.content = e.getValue();
                f.parent = root;
                root.children.put(e.getKey(), f);
            }
        }
    }

    public void saveUserData() {
        Map<String, String> files = new LinkedHashMap<>();
        for (Map.Entry<String, FileNode> e : root.children.entrySet()) {
            if (!e.getValue().isNetwork && !e.getValue().isDirectory) {
                files.put(e.getKey(), e.getValue().content);
            }
        }
        if (!files.isEmpty()) {
            USER_DATA.put(ip, files);
        } else {
            USER_DATA.remove(ip);
        }
    }

    public static void clearUserData(String ip) {
        USER_DATA.remove(ip);
    }

    public void seedDefaults() {
        seedDefaultFile(RESEARCH_LOG_1, R1_CONTENT);
        seedDefaultFile(RESEARCH_LOG_2, R2_CONTENT);
    }

    private void seedDefaultFile(String name, String content) {
        if (!root.children.containsKey(name)) {
            FileNode f = new FileNode(name, false);
            f.content = content;
            root.children.put(name, f);
            f.parent = root;
        }
    }

    private FileNode resolve(String path) {
        if (path.startsWith("/")) return resolveAbsolute(path);
        return resolveRelative(path);
    }

    private FileNode resolveAbsolute(String path) {
        FileNode current = root;
        String[] parts = path.substring(1).split("/");
        for (String part : parts) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) {
                if (current.parent != null) current = current.parent;
                continue;
            }
            if (current.isDirectory && current.children.containsKey(part)) {
                current = current.children.get(part);
            } else if (current.isDirectory && current.children.containsKey(part + "/")) {
                current = current.children.get(part + "/");
            } else {
                return null;
            }
        }
        return current;
    }

    private FileNode resolveRelative(String path) {
        FileNode current = cwd;
        String[] parts = path.split("/");
        for (String part : parts) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) {
                if (current.parent != null) current = current.parent;
                continue;
            }
            if (current.isDirectory && current.children.containsKey(part)) {
                current = current.children.get(part);
            } else if (current.isDirectory && current.children.containsKey(part + "/")) {
                current = current.children.get(part + "/");
            } else {
                return null;
            }
        }
        return current;
    }

    public String touch(String name) {
        if (name.contains("/")) return "  [ERR] touch: 仅支持在当前目录创建文件";
        if (cwd.children.containsKey(name)) {
            FileNode existing = cwd.children.get(name);
            if (existing.isDirectory) return "  [ERR] touch: " + name + " 是目录";
            return "";
        }
        FileNode file = new FileNode(name, false);
        file.content = "";
        file.parent = cwd;
        cwd.children.put(name, file);
        return "";
    }

    public String mkdir(String name) {
        if (name.contains("/")) return "  [ERR] mkdir: 目录名不能包含 '/'";
        if (cwd != root) return "  [ERR] mkdir: 仅允许在根目录创建文件夹";
        int dirCount = 0;
        for (FileNode child : root.children.values()) {
            if (child.isDirectory && !child.isNetwork) dirCount++;
        }
        if (dirCount >= 3) return "  [ERR] mkdir: 已达到最大文件夹数量 (3)";
        String dirName = name.endsWith("/") ? name : name + "/";
        if (root.children.containsKey(dirName)) return "  [ERR] mkdir: " + name + " 已存在";
        FileNode dir = new FileNode(dirName, true);
        dir.parent = root;
        root.children.put(dirName, dir);
        return "  已创建目录: " + dirName;
    }

    public String ls() {
        return lsNode(cwd);
    }

    public String ls(String dir) {
        FileNode target = cwd;
        if (dir != null && !dir.isEmpty()) {
            target = resolve(dir);
            if (target == null) return "  [ERR] ls: 目录不存在: " + dir;
            if (!target.isDirectory) return "  [ERR] ls: " + dir + " 不是目录";
        }
        return lsNode(target);
    }

    private String lsNode(FileNode target) {
        StringBuilder sb = new StringBuilder();
        for (FileNode child : target.children.values()) {
            if (child.isDirectory) {
                sb.append("  ").append(child.name).append("\n");
            } else if (child.isExecutable) {
                sb.append("  ").append(child.name).append("*\n");
            } else {
                sb.append("  ").append(child.name).append("\n");
            }
        }
        if (sb.isEmpty()) sb.append("  (empty)");
        return sb.toString().stripTrailing();
    }

    public String cat(String path) {
        FileNode node = resolve(path);
        if (node == null) return "  [ERR] cat: 文件不存在: " + path;
        if (node.isDirectory) return "  [ERR] cat: " + path + " 是目录";
        if (node.isExecutable) return "  Binary file. Use ./" + path + " to execute.";
        return node.content;
    }

    public String rm(String path) {
        FileNode target = resolve(path);
        if (target == null) return "  [ERR] rm: 文件不存在: " + path;
        if (target.isNetwork) return "  [ERR] rm: " + path + " 是云文件，不可删除";
        if (target.isDirectory && !target.children.isEmpty()) return "  [ERR] rm: 目录不为空";
        target.parent.children.values().remove(target);
        return "  已删除: " + path;
    }

    public String cd(String dir) {
        FileNode target = resolve(dir);
        if (target == null) return "  [ERR] cd: 目录不存在: " + dir;
        if (!target.isDirectory) return "  [ERR] cd: " + dir + " 不是目录";
        cwd = target;
        return "";
    }

    public String getCwdName() { return cwd.name; }

    public String pwd() {
        if (cwd == root) return "  /";
        StringBuilder sb = new StringBuilder();
        FileNode current = cwd;
        while (current != root) {
            sb.insert(0, current.name);
            current = current.parent;
        }
        sb.insert(0, "/");
        return "  " + sb.toString();
    }

    public boolean isExecutable(String path) {
        FileNode node = resolve(path);
        return node != null && node.isExecutable;
    }

    public boolean exists(String path) {
        return resolve(path) != null;
    }

    public List<String> getFileNames() {
        List<String> names = new ArrayList<>();
        for (String key : cwd.children.keySet()) {
            names.add(key);
        }
        return names;
    }

    public List<String> getReadableFiles() {
        List<String> files = new ArrayList<>();
        collectReadable(root, "", files);
        return files;
    }

    public List<String> getDirectories() {
        List<String> dirs = new ArrayList<>();
        collectDirs(cwd, "", dirs);
        return dirs;
    }

    private void collectDirs(FileNode dir, String prefix, List<String> out) {
        for (FileNode child : dir.children.values()) {
            if (child.isDirectory) {
                String name = child.name.endsWith("/") ? child.name.substring(0, child.name.length() - 1) : child.name;
                out.add(prefix + name);
                collectDirs(child, prefix + child.name, out);
            }
        }
    }

    private void collectReadable(FileNode dir, String prefix, List<String> out) {
        for (FileNode child : dir.children.values()) {
            if (child.isDirectory) {
                collectReadable(child, prefix + child.name, out);
            } else if (!child.isExecutable) {
                out.add(prefix + child.name);
            }
        }
    }

    public String getVimContent(String path) {
        FileNode node = resolve(path);
        if (node == null) return "";
        if (node.isDirectory) return null;
        return node.content;
    }

    public String vimSave(String path, String content) {
        if (path.contains("/")) {
            String[] parts = path.split("/");
            FileNode parent = resolveParent(path);
            if (parent == null) return "  [ERR] 目录不存在";
            String name = parts[parts.length - 1];
            FileNode existing = parent.children.get(name);
            if (existing != null && existing.isDirectory) return "  [ERR] 目标是一个目录";
            if (existing == null) {
                existing = new FileNode(name, false);
                existing.parent = parent;
                parent.children.put(name, existing);
            }
            existing.content = content;
        } else {
            FileNode existing = cwd.children.get(path);
            if (existing != null && existing.isDirectory) return "  [ERR] 目标是一个目录";
            if (existing == null) {
                existing = new FileNode(path, false);
                existing.parent = cwd;
                cwd.children.put(path, existing);
            }
            existing.content = content;
        }
        return "";
    }

    private FileNode resolveParent(String path) {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash <= 0) return root;
        return resolve(path.substring(0, lastSlash));
    }

    public void resetLocal() {
        cwd = root;
        pruneLocal(root);
    }

    private void pruneLocal(FileNode dir) {
        var it = dir.children.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            FileNode child = entry.getValue();
            if (child.isDirectory) {
                pruneLocal(child);
                if (child.children.isEmpty() && !child.isNetwork) {
                    it.remove();
                }
            } else if (!child.isNetwork) {
                it.remove();
            }
        }
    }
}