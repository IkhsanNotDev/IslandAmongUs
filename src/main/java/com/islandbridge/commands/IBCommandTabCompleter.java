// ============================================================
// FILE: IBCommandTabCompleter.java (UPDATE - termasuk autofarmland)
// ============================================================
package com.islandbridge.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IBCommandTabCompleter implements TabCompleter {

    private final JavaPlugin plugin;

    public IBCommandTabCompleter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("ib")) {
            return null;
        }

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList(
                    "setlobby", "setmap", "setmeeting", "setpulau1", "setpulau2",
                    "setjembatankiri", "setjembatankanan", "setnpcjembatan", "setnpcname",
                    "resetbridge", "setlamp", "autolamp", "autochest", "autooak", "autofarmland",
                    "setgenerator", "setskin", "testing", "role", "start",
                    "sabotage", "faketask", "reload", "setting", "setvoid", "ceknpc",
                    "export", "import"
            );
            List<String> result = new ArrayList<>();
            for (String sub : subCommands) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(sub);
                }
            }
            return result;
        }

        if (args.length == 2) {
            String firstArg = args[0].toLowerCase();

            if (firstArg.equals("export") || firstArg.equals("import")) {
                return getExportFiles();
            }

            if (firstArg.equals("setvoid") || firstArg.equals("setskin")) {
                return null; // return null agar server mengembalikan daftar player online
            }

            if (firstArg.equals("setjembatankiri") || firstArg.equals("setjembatankanan") ||
                    firstArg.equals("autolamp") || firstArg.equals("autochest") ||
                    firstArg.equals("autooak") || firstArg.equals("autofarmland") ||
                    firstArg.equals("testing")) {
                return new ArrayList<>();
            }

            if (firstArg.equals("setting")) {
                return Arrays.asList(
                        "cdkill", "minplayers", "countdown", "meetingtime",
                        "joker", "impostorcount", "skinwalkerblind",
                        "autodetectlamp", "autodetecttask"
                );
            }

            if (firstArg.equals("import")) {
                return getExportFiles();
            }

            return new ArrayList<>();
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("setting")) {
            String key = args[1].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            switch (key) {
                case "cdkill":
                case "minplayers":
                case "countdown":
                case "meetingtime":
                case "impostorcount":
                case "autodetectlamp":
                case "autodetecttask":
                    suggestions.add("<angka>");
                    suggestions.add("10");
                    suggestions.add("20");
                    suggestions.add("30");
                    suggestions.add("60");
                    suggestions.add("90");
                    suggestions.add("120");
                    break;
                case "joker":
                    suggestions.add("true");
                    suggestions.add("false");
                    break;
                case "skinwalkerblind":
                    suggestions.add("4s");
                    suggestions.add("5s");
                    suggestions.add("10s");
                    suggestions.add("15s");
                    break;
                default:
                    suggestions.add("<value>");
            }
            List<String> result = new ArrayList<>();
            for (String s : suggestions) {
                if (s.toLowerCase().startsWith(args[2].toLowerCase())) {
                    result.add(s);
                }
            }
            return result;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("setting")) {
            String key = args[1].toLowerCase();
            if (key.equals("skinwalkerblind") || key.equals("cdkill")) {
                List<String> examples = Arrays.asList("4s", "5s", "10s", "15s", "20s", "30s", "45s", "60s", "90s", "120s", "5m", "10m");
                List<String> result = new ArrayList<>();
                for (String ex : examples) {
                    if (ex.toLowerCase().startsWith(args[3].toLowerCase())) {
                        result.add(ex);
                    }
                }
                return result;
            }
            return new ArrayList<>();
        }

        return new ArrayList<>();
    }

    private List<String> getExportFiles() {
        File exportsDir = new File(plugin.getDataFolder(), "exports");
        if (!exportsDir.exists() || !exportsDir.isDirectory()) {
            return new ArrayList<>();
        }
        File[] files = exportsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (File f : files) {
            result.add(f.getName());
        }
        return result;
    }
}