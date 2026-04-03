package component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.codexrm.server.component.FieldValidations;

import static org.junit.jupiter.api.Assertions.*;

public class FieldValidationsTest {

    private FieldValidations validations;

    @BeforeEach
    void setUp() {
        validations = new FieldValidations();
    }
    // ===================== YEAR =====================

    @Test
    void shouldAcceptValidYear() {
        assertFalse(validations.isInvalidateYear("2024"));
    }

    @Test
    void shouldAcceptValidYearRange() {
        assertFalse(validations.isInvalidateYear("2020--2024"));
    }

    @Test
    void shouldRejectInvalidYear() {
        assertTrue(validations.isInvalidateYear("20a4"));
    }

    // ===================== AUTHOR / EDITOR =====================

    @Test
    void shouldAcceptValidAuthor() {
        assertFalse(validations.isInvalidateAuthorOrEditor("Garcia,Juan"));
    }

    @Test
    void shouldAcceptMultipleAuthors() {
        assertFalse(validations.isInvalidateAuthorOrEditor("Garcia,Juan;Perez,Ana"));
    }

    @Test
    void shouldRejectInvalidAuthor() {
        assertTrue(validations.isInvalidateAuthorOrEditor("juan garcia"));
    }

    // ===================== VOLUME / CHAPTER =====================

    @Test
    void shouldAcceptValidVolume() {
        assertFalse(validations.isInvalidateChapterOrVolume("12"));
    }

    @Test
    void shouldRejectInvalidVolume() {
        assertTrue(validations.isInvalidateChapterOrVolume("abc"));
    }

    // ===================== NUMBER =====================

    @Test
    void shouldAcceptValidNumber() {
        assertFalse(validations.isInvalidateNumber("12A"));
    }

    @Test
    void shouldRejectInvalidNumber() {
        assertTrue(validations.isInvalidateNumber("@@@"));
    }

    // ===================== PAGES =====================

    @Test
    void shouldAcceptValidPages() {
        assertFalse(validations.isInvalidatePages("10-20"));
    }

    @Test
    void shouldAcceptRomanPages() {
        assertFalse(validations.isInvalidatePages("X-XX"));
    }

    @Test
    void shouldRejectInvalidPages() {
        assertTrue(validations.isInvalidatePages("pages"));
    }

    // ===================== ISSN =====================

    @Test
    void shouldAcceptValidIssn() {
        assertFalse(validations.isInvalidateIssn("1234-5678"));
    }

    @Test
    void shouldRejectInvalidIssn() {
        assertTrue(validations.isInvalidateIssn("123"));
    }

    // ===================== ISBN =====================

    @Test
    void shouldAcceptValidIsbn() {
        assertFalse(validations.isInvalidateIsbn("978-3-16-148410-0"));
    }

    @Test
    void shouldRejectInvalidIsbn() {
        assertTrue(validations.isInvalidateIsbn("invalid-isbn"));
    }

    // ===================== ADDRESS =====================

    @Test
    void shouldAcceptValidAddress() {
        assertFalse(validations.isInvalidateAddress("Madrid, España"));
    }

    @Test
    void shouldRejectInvalidAddress() {
        assertTrue(validations.isInvalidateAddress("1234"));
    }

    // ===================== SERIES =====================

    @Test
    void shouldAcceptValidSeries() {
        assertFalse(validations.isInvalidateSeries("Spring Series"));
    }

    @Test
    void shouldRejectInvalidSeries() {
        assertTrue(validations.isInvalidateSeries("1234"));
    }

    // ===================== EDITION =====================

    @Test
    void shouldAcceptValidEdition() {
        assertFalse(validations.isInvalidateEdition("Second"));
    }

    @Test
    void shouldRejectInvalidEdition() {
        assertTrue(validations.isInvalidateEdition("@@@"));
    }

    // ===================== URL =====================

    @Test
    void shouldAcceptValidUrl() {
        assertFalse(validations.isInvalidateUrl("https://example.com"));
    }

    @Test
    void shouldRejectInvalidUrl() {
        assertTrue(validations.isInvalidateUrl("htp://bad-url"));
    }
}

