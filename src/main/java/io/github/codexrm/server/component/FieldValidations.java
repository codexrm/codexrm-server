package io.github.codexrm.server.component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldValidations {

    public boolean isInvalidateAuthorOrEditor(String author) {
        if (author == null)
            return true;

        final Pattern pat = Pattern.compile("^[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+[;(?=[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+)[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+]*");
        final Matcher mat = pat.matcher(author);
        return !mat.matches();
    }

    public boolean isInvalidateAddress(String address) {
        if (address == null || address.isBlank()) {
            return true;
        }

        Pattern pat = Pattern.compile(
                "^[\\p{Lu}][\\p{L}\\s]*,\\s[\\p{Lu}][\\p{L}\\s]*$"
        );

        Matcher mat = pat.matcher(address.trim());
        return !mat.matches();
    }

    public boolean isInvalidateChapterOrVolume(final String number) {
        if (number == null)
            return true;

        final Pattern pat = Pattern.compile("^$|[\\d]*");
        final Matcher mat = pat.matcher(number);
        return !mat.matches();
    }

    public boolean isInvalidateEdition(String edition) {
        if (edition == null)
            return true;

        final Pattern pat = Pattern.compile("[A-ZÁÉÍÓÚÜÑa-záéíóúüñ0]+|\\d+\\.");
        final Matcher mat = pat.matcher(edition);
        return !mat.matches();
    }

    public boolean isInvalidateIssn(String issn) {
        if (issn == null)
            return true;

        final Pattern pat = Pattern.compile("\\d{4}-\\d{4}|\\d{4}-\\d{3}X");
        final Matcher mat = pat.matcher(issn);
        return !mat.matches();
    }

    public boolean isInvalidateIsbn(String isbn) {
        if (isbn == null)
            return true;

        final Pattern pat = Pattern.compile("^(?=(?:\\D*\\d){10}(?:(?:\\D*\\d){3})?$)[\\d-]+$");
        final Matcher mat = pat.matcher(isbn);
        return !mat.matches();
    }

    public boolean isInvalidateNumber(String number) {
        if (number == null)
            return true;

        final Pattern pat = Pattern.compile("[A-ZÁÉÍÓÚÜÑa-záéíóúüñ0-9\\s-]+");
        final Matcher mat = pat.matcher(number);
        return !mat.matches();
    }

    public boolean isInvalidatePages(String pages) {
        if (pages == null)
            return true;

        final Pattern pat = Pattern.compile("[IVXMLCD]+|[IVXMLCD]+,[IVXMLCD]+|[IVXMLCD]+-[IVXMLCD]+|[0-9]+|[0-9]+,[0-9]+|[0-9]+-[0-9]+");
        final Matcher mat = pat.matcher(pages);
        return !mat.matches();
    }

    public boolean isInvalidateSeries(String serie) {
        if (serie == null)
            return true;

        final Pattern pat = Pattern.compile("[A-ZÁÉÍÓÚÜÑa-záéíóúüñ\\s]+");
        final Matcher mat = pat.matcher(serie);
        return !mat.matches();
    }

    public boolean isInvalidateUrl(String url) {
        if (url == null)
            return true;

        final Pattern pat = Pattern.compile("^https://.*");
        final Matcher mat = pat.matcher(url);
        return !mat.matches();
    }

    public boolean isInvalidateYear(String year) {
        if (year == null)
            return true;

        final Pattern pat = Pattern.compile("\\d{4}|\\d{4}--\\d{4}");
        final Matcher mat = pat.matcher(year);
        return !mat.matches();
    }
}

