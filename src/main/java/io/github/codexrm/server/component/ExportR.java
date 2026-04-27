package io.github.codexrm.server.component;

import io.github.codexrm.EILibrary.controller.EIManager;
import io.github.codexrm.EILibrary.model.*;
import io.github.codexrm.server.domain.model.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ExportR {

    private final EnumsConverter enumsConverter;

    public ExportR() {
        this.enumsConverter = new EnumsConverter();
    }

    public void exportReferenceList(File file, ArrayList<Reference> referenceList, String format) throws IOException {

        EIManager manager = new EIManager();

        ArrayList<BaseR> list = new ArrayList<>();
        for (Reference reference : referenceList) {
            BaseR entry = identifyType(reference);
            if (entry != null) {
                list.add(entry);
            }
        }

        manager.exportReferenceList(file.toPath(), list, enumsConverter.getFormat(format));
    }

    private BaseR identifyType(Reference reference) {

        BaseR entry;

        if (reference instanceof ArticleReference) {
            ArticleReference article = (ArticleReference) reference;
            entry = new ArticleR(article.getTitle(), article.getYear(), enumsConverter.getMonthLibrary(article.getMonth()), article.getNote(), article.getId(), article.getAuthor(), article.getJournal(), article.getVolume(), article.getNumber(), article.getPages(), article.getIssn());

        } else {
            if (reference instanceof BookSectionReference) {
                BookSectionReference section = (BookSectionReference) reference;
                entry = new BookSectionR(section.getTitle(), section.getYear(), enumsConverter.getMonthLibrary(section.getMonth()), section.getNote(), section.getId(), section.getAuthor(), section.getEditor(), section.getPublisher(), section.getVolume(), section.getNumber(),
                        section.getSeries(), section.getAddress(), section.getEdition(), section.getIsbn(), section.getChapter(), section.getPages(), enumsConverter.getBookSectionTypeLibrary(section.getType()));

            } else {
                if (reference instanceof BookReference) {
                    BookReference book = (BookReference) reference;
                    entry = new BookR(book.getTitle(), book.getYear(), enumsConverter.getMonthLibrary(book.getMonth()), book.getNote(), book.getId(), book.getAuthor(), book.getEditor(), book.getPublisher(), book.getVolume(), book.getNumber(), book.getSeries(), book.getAddress(),
                            book.getEdition(), book.getIsbn());

                } else {
                    if (reference instanceof ThesisReference) {
                        ThesisReference thesis = (ThesisReference) reference;
                        entry = new ThesisR(thesis.getTitle(), thesis.getYear(), enumsConverter.getMonthLibrary(thesis.getMonth()), thesis.getNote(), thesis.getId(), thesis.getAuthor(), thesis.getSchool(), enumsConverter.getThesisTypeLibrary(thesis.getType()), thesis.getAddress());

                    } else {
                        if (reference instanceof ConferenceProceedingReference) {
                            ConferenceProceedingReference proceedings = (ConferenceProceedingReference) reference;
                            entry = new ConferenceProceedingsR(proceedings.getTitle(), proceedings.getYear(), enumsConverter.getMonthLibrary(proceedings.getMonth()), proceedings.getNote(), proceedings.getId(), proceedings.getEditor(), proceedings.getVolume(), proceedings.getNumber(),
                                    proceedings.getSeries(), proceedings.getAddress(), proceedings.getPublisher(), proceedings.getOrganization(), proceedings.getIsbn());

                        } else {
                            if (reference instanceof ConferencePaperReference) {
                                ConferencePaperReference paper = (ConferencePaperReference) reference;
                                entry = new ConferencePaperR(paper.getTitle(), paper.getYear(), enumsConverter.getMonthLibrary(paper.getMonth()), paper.getNote(), paper.getId(), paper.getAuthor(), paper.getBookTitle(), paper.getEditor(), paper.getVolume(), paper.getNumber(),
                                        paper.getSeries(), paper.getPages(), paper.getAddress(), paper.getOrganization(), paper.getPublisher());

                            } else {
                                if (reference instanceof WebPageReference) {
                                    WebPageReference webPage = (WebPageReference) reference;
                                    entry = new WebPageR(webPage.getTitle(), webPage.getYear(), enumsConverter.getMonthLibrary(webPage.getMonth()), webPage.getNote(), webPage.getId(), webPage.getAuthor(), webPage.getUrl());

                                } else {
                                    if (reference instanceof BookLetReference) {
                                        BookLetReference let = (BookLetReference) reference;
                                        entry = new BookLetR(let.getTitle(), let.getYear(), enumsConverter.getMonthLibrary(let.getMonth()), let.getNote(), let.getId(), let.getAuthor(), let.getHowpublished(), let.getAddress());

                                    } else {
                                        entry = null;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return entry;
    }
}
