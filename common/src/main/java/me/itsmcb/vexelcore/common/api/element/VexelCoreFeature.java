package me.itsmcb.vexelcore.common.api.element;

public abstract class VexelCoreFeature {

    public enum FeatureStatus {
        ENABLED,
        DISABLED
    }

    public enum ElementState {
        REGISTERING("registered"),
        UNREGISTERING("unregistered");

        private String elementState;

        ElementState(String elementState) {
            this.elementState = elementState;
        }

        public String get() {
            return elementState;
        }

        @Override
        public String toString() {
            return String.valueOf(get());
        }
    }

    public enum ElementType {
        BUKKITLISTENER("Listener"),
        COMMAND("Command");

        private String elementType;

        ElementType(String elementType) {
            this.elementType = elementType;
        }

        public String get() {
            return elementType;
        }

        @Override
        public String toString() {
            return String.valueOf(get());
        }
    }

    private FeatureStatus status = FeatureStatus.DISABLED;
    private String name;
    private String description;
    private boolean debug = false;

    public VexelCoreFeature(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public FeatureStatus getStatus() {
        return status;
    }

    public void setStatus(FeatureStatus status) {
        this.status = status;
    }

    public abstract void enable();

    public void enablePreLoadTriggers() {}

    public void enableTriggers() {}

    public abstract void disable();

    public void disableTriggers() {}

    public void log(ElementType elementType, ElementState elementState, String featureName) {
        if (debug) {
            System.out.println(elementType.toString() + " has " + elementState.toString() + " for feature called " + featureName);
        }
    }
}
