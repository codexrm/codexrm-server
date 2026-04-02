package io.github.codexrm.server.component;

import io.github.codexrm.EILibrary.enums.*;

public class EnumsConverter {

    public EnumsConverter() {}

    // ----------- MONTHS -----------

    public MonthsLibrary getMonthLibrary(String months) {
        try {
            return (months != null) ? MonthsLibrary.valueOf(months) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getMonth(MonthsLibrary months) {
        return (months != null) ? months.name() : null;
    }

    // ----------- FORMAT -----------

    public FormatLibrary getFormat(String format) {
        try {
            return (format != null) ? FormatLibrary.valueOf(format) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ----------- BOOK SECTION TYPE (SE DEJA CON SWITCH PARA NO ROMPER) -----------

    public BookSectionTypeLibrary getBookSectionTypeLibrary(String type) {

        if (type != null) {
            switch (type) {
                case "AUDIOCD":
                    return BookSectionTypeLibrary.AUDIOCD;
                case "CANDTHESIS":
                    return BookSectionTypeLibrary.CANDTHESIS;
                case "DataCD":
                    return BookSectionTypeLibrary.DataCD;
                case "MATHESIS":
                    return BookSectionTypeLibrary.MATHESIS;
                case "PHDTHESIS":
                    return BookSectionTypeLibrary.PHDTHESIS;
                case "RESREPORT":
                    return BookSectionTypeLibrary.RESREPORT;
                case "SOFTWARE":
                    return BookSectionTypeLibrary.SOFTWARE;
                case "TECHREPORT":
                    return BookSectionTypeLibrary.TECHREPORT;
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public String getBookSectionType(BookSectionTypeLibrary type) {

        if (type != null) {
            switch (type) {
                case AUDIOCD:
                    return "AUDIOCD";
                case CANDTHESIS:
                    return "CANDTHESIS";
                case DataCD:
                    return "DataCD";
                case MATHESIS:
                    return "MATHESIS";
                case PHDTHESIS:
                    return "PHDTHESIS";
                case RESREPORT:
                    return "RESREPORT";
                case SOFTWARE:
                    return "SOFTWARE";
                case TECHREPORT:
                    return "TECHREPORT";
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    // ----------- THESIS TYPE -----------

    public ThesisTypeLibrary getThesisTypeLibrary(String type) {
        try {
            return (type != null) ? ThesisTypeLibrary.valueOf(type) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getThesisType(ThesisTypeLibrary type) {
        return (type != null) ? type.name() : null;
    }
}