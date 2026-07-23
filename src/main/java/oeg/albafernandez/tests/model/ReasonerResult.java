package oeg.albafernandez.tests.model;

public enum ReasonerResult {
    CONSISTENT("consistent"),
    INCONSISTENT("inconsistent"),
    UNSATISFIABLE("unsatisfiable");

    private final String value;

    ReasonerResult(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ReasonerResult fromString(String text) {
        if (text == null) {
            return null;
        }
        for (ReasonerResult r : ReasonerResult.values()) {
            if (r.name().equalsIgnoreCase(text) || r.value.equalsIgnoreCase(text)) {
                return r;
            }
        }
        return null;
    }
}
