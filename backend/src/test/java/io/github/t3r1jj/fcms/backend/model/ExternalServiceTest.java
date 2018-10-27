package io.github.t3r1jj.fcms.backend.model;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class ExternalServiceTest {

    @Test
    public void testHashCodeShouldBeEqualOnNameAndPrimary() {
        ExternalService s1 = new ExternalService("name", true);
        ExternalService s2 = new ExternalService("name", true);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void testHashCodeShouldNotBeEqualOnNameOnly() {
        ExternalService s1 = new ExternalService("name", true);
        ExternalService s2 = new ExternalService("name", false);
        assertNotEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void testHashCodeShouldNotBeEqualOnPrimaryOnly() {
        ExternalService s1 = new ExternalService("name", true);
        ExternalService s2 = new ExternalService("name2", true);
        assertNotEquals(s1.hashCode(), s2.hashCode());
    }

}