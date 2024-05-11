package me.itsmcb.vexelcore.common.api.command;

import java.util.HashMap;
import java.util.List;

public class CMDHelper {

    private String[] args;
    private HashMap<HashMap<Integer, String>, List<String>> tabCompletions = new HashMap<>();

    // Construction

    public CMDHelper() { }

    public CMDHelper(String[] args) {
        setArgs(args);
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    // Methods

    public boolean argEquals(int argIndex, String... inputs) {
        if (hasArgIndex(argIndex)) {
            boolean isFound = false;
            for (String input : inputs) {
                if (args[argIndex].equalsIgnoreCase(input)) {
                    isFound = true;
                    break;
                }
            }
            return isFound;
        }
        return false;
    }

    public boolean isCalling(String... inputs) {
        for (String input : inputs) {
            if (isCalling(input)) {
                return true;
            }
        }
        return false;
    }

    public String getFlag(String flag) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase(flag) && argExists(i+1)) {
                return args[i+1];
            }
        }
        return null;
    }

    public boolean flagExists(String flag) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase(flag) && argExists(i+1)) {
                return true;
            }
        }
        return false;
    }

    public boolean emptyFlagExists(String flag) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase(flag)) {
                return true;
            }
        }
        return false;
    }

    public boolean argExists(int argIndex) {
        return hasArgIndex(argIndex);
    }

    public boolean argNotExists(int argIndex) {
        return !hasArgIndex(argIndex);
    }

    private boolean hasArgIndex(int argIndex) {
        return argIndex <= this.args.length-1;
    }

    // Tab Completion Assistant

    public void addTabCompletion(HashMap<Integer, String> argPointCheck, List<String> completions) {
        tabCompletions.put(argPointCheck, completions);
    }

    public List<String> generateTabComplete() {
        for (int i = 0; i < tabCompletions.size(); i++) {
            int tcArgIndex = tabCompletions.entrySet().stream().toList().get(i).getKey().entrySet().stream().toList().get(0).getKey();
            String tcArgValue = tabCompletions.entrySet().stream().toList().get(i).getKey().entrySet().stream().toList().get(0).getValue();
            if (args.length == tcArgIndex+1) {
                if (tcArgIndex == 0) {
                    return tabCompletions.entrySet().stream().toList().get(i).getValue();
                }
                if (args[tcArgIndex-1].equalsIgnoreCase(tcArgValue)) {
                    return tabCompletions.entrySet().stream().toList().get(i).getValue();
                }
            }
            if (args.length == 0 && tcArgIndex == 0) {
                return tabCompletions.entrySet().stream().toList().get(i).getValue();
            }
        }
        return List.of("");
    }

    public HashMap<Integer, String> getMap(int argIndex, String valueToCheck) {
        HashMap<Integer, String> hashMap = new HashMap<>();
        hashMap.put(argIndex, valueToCheck);
        return hashMap;
    }

    // Internal methods

    private boolean isCalling(String input) {
        if (hasArgIndex(0)) {
            return this.args[0].equalsIgnoreCase(input);
        }
        return false;
    }

    public String getStringOfArgsAfterIndex(int argIndex) {
        return getStringOfArgsAfterIndex(argIndex, args.length);
    }

    public String getStringOfArgsAfterIndex(int argIndex, int endArgIndex) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = argIndex+1; i < endArgIndex; i++) {
            stringBuilder.append(args[i]);
            if (i+1 < endArgIndex) {
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

    public boolean isInt(int argIndex) {
        if (hasArgIndex(argIndex)) {
            try {
                Integer.parseInt(args[argIndex]);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    public boolean isDouble(int argIndex) {
        if (hasArgIndex(argIndex)) {
            try {
                Double.parseDouble(args[argIndex]);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    public boolean isFloat(int argIndex) {
        if (hasArgIndex(argIndex)) {
            try {
                Float.parseFloat(args[argIndex]);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    public boolean isLong(int argIndex) {
        if (hasArgIndex(argIndex)) {
            try {
                Long.parseLong(args[argIndex]);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

}
