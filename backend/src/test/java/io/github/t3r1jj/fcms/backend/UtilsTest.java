package io.github.t3r1jj.fcms.backend;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.function.Predicate;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class UtilsTest {

    @Test
    public void testNot() {
        Predicate predicateMock = Mockito.mock(Predicate.class);
        Predicate negatedPredicateMock = Mockito.mock(Predicate.class);
        when(predicateMock.negate()).thenReturn(negatedPredicateMock);
        Predicate negatedPredicate = Utils.not(predicateMock);
        verify(predicateMock.negate());
        assertEquals(negatedPredicate, negatedPredicateMock);
    }

    @Test
    public void testNotIfTrue() {
        Predicate predicateMock = Mockito.mock(Predicate.class);
        Predicate negatedPredicateMock = Mockito.mock(Predicate.class);
        when(predicateMock.negate()).thenReturn(negatedPredicateMock);
        Predicate negatedPredicate = Utils.notIf(predicateMock, true);
        verify(predicateMock.negate());
        assertEquals(negatedPredicate, negatedPredicateMock);
    }

    @Test
    public void testNotIfFalse() {
        Predicate predicateMock = Mockito.mock(Predicate.class);
        Predicate negatedPredicateMock = Mockito.mock(Predicate.class);
        when(predicateMock.negate()).thenReturn(negatedPredicateMock);
        Predicate notNegatedPredicate = Utils.notIf(predicateMock, false);
        verify(predicateMock, times(0)).negate();
        assertEquals(notNegatedPredicate, predicateMock);
    }

}