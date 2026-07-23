package oeg.albafernandez.tests.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MapperTest {

    @Test
    @DisplayName("getPrecTerm should return term enclosed in angle brackets when present")
    public void testGetPrecTermFound() {
        String query = "Class(<http://example.org/Sensor>)";
        String term = Mapper.getPrecTerm(query);
        assertEquals("<http://example.org/Sensor>", term);
    }

    @Test
    @DisplayName("getPrecTerm should return null without throwing exception when no angle brackets present")
    public void testGetPrecTermNotFound() {
        String query = "Class(Sensor)";
        String term = Mapper.getPrecTerm(query);
        assertNull(term, "Should return null when no match is found");
    }

    @Test
    @DisplayName("getPrecTerm should handle null input gracefully")
    public void testGetPrecTermNull() {
        String term = Mapper.getPrecTerm(null);
        assertNull(term, "Should return null for null input");
    }
}
