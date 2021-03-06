package io.github.t3r1jj.fcms.backend.model;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class ExternalServiceTest {

    @Test
    public void testHashCodeShouldBeEqualOnNameAndApiKeys() {
        ExternalService s1 = new ExternalService("name", true, true,
                new ExternalService.ApiKey("label123", "key123"));
        ExternalService s2 = new ExternalService("name", true, false,
                new ExternalService.ApiKey("label123", "key123"));
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void testHashCodeShouldNotBeEqualOnNameOnly() {
        ExternalService s1 = new ExternalService("name", true, true,
                new ExternalService.ApiKey("label123", "key123"));
        ExternalService s2 = new ExternalService("name", false, false,
                new ExternalService.ApiKey("different", "key123"));
        assertNotEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void testHashCodeShouldNotBeEqualOnPrimaryOnly() {
        ExternalService s1 = new ExternalService("name", true, true,
                new ExternalService.ApiKey("label123", "key123"));
        ExternalService s2 = new ExternalService("name2", true, false,
                new ExternalService.ApiKey("label123", "key123"));
        assertNotEquals(s1.hashCode(), s2.hashCode());
    }

}