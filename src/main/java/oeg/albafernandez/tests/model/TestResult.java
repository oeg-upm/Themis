package oeg.albafernandez.tests.model;

public enum TestResult {
    PASSED("Passed"),
    UNDEFINED("Undefined"),
    INCORRECT("Incorrect"),
    ABSENT("Absent"),
    CONFLICT("Conflict"),
    NOT_PASSED("not passed");

    private final String label;

    TestResult(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static TestResult fromString(String text) {
        if (text == null) {
            return null;
        }
        for (TestResult r : TestResult.values()) {
            if (r.name().equalsIgnoreCase(text) || r.label.equalsIgnoreCase(text)) {
                return r;
            }
        }
        if ("not passed".equalsIgnoreCase(text)) {
            return NOT_PASSED;
        }
        return null;
    }
}
