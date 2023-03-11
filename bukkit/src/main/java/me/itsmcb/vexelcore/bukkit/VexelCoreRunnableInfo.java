package me.itsmcb.vexelcore.bukkit;

public class VexelCoreRunnableInfo {

    public enum RunnableType {
        ONCE_SYNC,
        ONCE_SYNC_LATER,
        ONCE_ASYNC,
        ONCE_ASYNC_LATER,
        REPEAT_SYNC,
        REPEAT_SYNC_LATER,
        REPEAT_ASYNC,
        REPEAT_ASYNC_LATER,
    }

    private VexelCoreRunnable runnable;

    private RunnableType type;
    private int startDelay = 0;
    private int repeatDelay = 0;

    public VexelCoreRunnableInfo(VexelCoreRunnable runnable, RunnableType type) {
        this.runnable = runnable;
        this.type = type;
    }

    public VexelCoreRunnableInfo(VexelCoreRunnable runnable, RunnableType type, int startDelay, int repeatDelay) {
        this.runnable = runnable;
        this.type = type;
        this.startDelay = startDelay;
        this.repeatDelay = repeatDelay;
    }

    public VexelCoreRunnable getRunnable() {
        return runnable;
    }


    public RunnableType getType() {
        return type;
    }

    public int getStartDelay() {
        return startDelay;
    }

    public int getRepeatDelay() {
        return repeatDelay;
    }
}
