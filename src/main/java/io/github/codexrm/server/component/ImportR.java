package io.github.codexrm.server.component;

import io.github.codexrm.EILibrary.controller.EIManager;
import io.github.codexrm.EILibrary.model.*;
import io.github.codexrm.server.model.*;
import org.jbibtex.ParseException;
import org.jbibtex.TokenMgrException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ImportR {

    private final EnumsConverter enumsConverter;
    private final ValidateReference validation;

    public ImportR() {
        this.enumsConverter = new EnumsConverter();
        this.validation = new ValidateReference();
    }

    public ArrayList<Reference> importReferences(String path, String format) throws IOException, TokenMgrException, ParseException {

        EIManager manager = new EIManager();
        ArrayList<Reference> referenceList = new ArrayList<>();

        ArrayList<BaseR> list = manager.importReferences(path, enumsConverter.getFormat(format));

        return list.stream()
                .map(this::createReference)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Reference createReference(BaseR entry) {

        return switch (entry) {
            case ArticleR article ->
                    readArticleReference(article);

            case BookSectionR section ->
                    readBookSectionReference(section);

            case BookR book ->
                    readBookReference(book);

            case ThesisR thesis ->
                    readThesisReference(thesis);

            case ConferenceProceedingsR proceedings ->
                    readConferenceProceedingsReference(proceedings);

            case ConferencePaperR paper ->
                    readConferencePaperReference(paper);

            case WebPageR web ->
                    readWebPageReference(web);

            case BookLetR let ->
                    readBookLetReference(let);

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported reference type: " + entry.getClass().getSimpleName()
                    );
        };
    }

    private void commonField(BaseR entry, Reference reference) {
        reference.setTitle(entry.getTitle());
        reference.setYear(entry.getYear());
        reference.setMonth(enumsConverter.getMonth(entry.getMonth()));
        reference.setNote(entry.getNote());
    }

    private Reference readArticleReference(ArticleR entry) {

        ArticleReference article = new ArticleReference();

        commonField(entry, article);
        article.setAuthor(entry.getAuthor());
        article.setJournal(entry.getJournal());
        article.setVolume(entry.getVolume());
        article.setNumber(entry.getNumber());
        article.setPages(entry.getPages());
        article.setIssn(entry.getIssn());

        validation.validateArticleReference(article);
        return validation.validateRequiredArticle(article);
    }

    private Reference readBookReference(BookR entry) {

        BookReference book = new BookReference();

        commonField(entry, book);
        book.setAuthor(entry.getAuthor());
        book.setEditor(entry.getEditor());
        book.setPublisher(entry.getPublisher());
        book.setVolume(entry.getVolume());
        book.setNumber(entry.getNumber());
        book.setSeries(entry.getSeries());
        book.setAddress(entry.getAddress());
        book.setEdition(entry.getEdition());
        book.setIsbn(entry.getIsbn());

        validation.validateBookReference(book);
        return validation.validateRequiredBook(book);
    }

    private Reference readBookSectionReference(BookSectionR entry) {

        BookSectionReference section = new BookSectionReference();

        commonField(entry, section);
        section.setChapter(entry.getChapter());
        section.setPages(entry.getPages());
        section.setAuthor(entry.getAuthor());
        section.setEditor(entry.getEditor());
        section.setPublisher(entry.getPublisher());
        section.setVolume(entry.getVolume());
        section.setNumber(entry.getNumber());
        section.setSeries(entry.getSeries());
        section.setType(enumsConverter.getBookSectionType(entry.getType()));
        section.setAddress(entry.getAddress());
        section.setEdition(entry.getEdition());
        section.setIsbn(entry.getIsbn());

        validation.validateBookSectionReference(section);
        return validation.validateRequiredBookSection(section);
    }

    private Reference readBookLetReference(BookLetR entry) {

        BookLetReference let = new BookLetReference();

        commonField(entry, let);
        let.setAuthor(entry.getAuthor());
        let.setHowpublished(entry.getHowpublished());
        let.setAddress(entry.getAddress());

        validation.validateBookLetReference(let);
        return validation.validateRequiredBookLet(let);
    }

    private Reference readThesisReference(ThesisR entry) {

        ThesisReference thesis = new ThesisReference();

        commonField(entry, thesis);
        thesis.setAuthor(entry.getAuthor());
        thesis.setSchool(entry.getSchool());
        thesis.setType(enumsConverter.getThesisType(entry.getType()));
        thesis.setAddress(entry.getAddress());

        validation.validateThesisReference(thesis);
        return validation.validateRequiredThesis(thesis);
    }

    private Reference readConferenceProceedingsReference(ConferenceProceedingsR entry) {

        ConferenceProceedingReference proceedings = new ConferenceProceedingReference();

        commonField(entry, proceedings);
        proceedings.setEditor(entry.getEditor());
        proceedings.setVolume(entry.getVolume());
        proceedings.setNumber(entry.getNumber());
        proceedings.setSeries(entry.getSeries());
        proceedings.setAddress(entry.getAddress());
        proceedings.setPublisher(entry.getPublisher());
        proceedings.setOrganization(entry.getOrganization());
        proceedings.setIsbn(entry.getIsbn());

        validation.validateConferenceProceedingsReference(proceedings);
        return validation.validateRequiredConferenceProceedings(proceedings);
    }

    private Reference readConferencePaperReference(ConferencePaperR entry) {

        ConferencePaperReference paper = new ConferencePaperReference();

        commonField(entry, paper);
        paper.setAuthor(entry.getAuthor());
        paper.setBookTitle(entry.getBookTitle());
        paper.setEditor(entry.getEditor());
        paper.setVolume(entry.getVolume());
        paper.setNumber(entry.getNumber());
        paper.setSeries(entry.getSeries());
        paper.setPages(entry.getPages());
        paper.setAddress(entry.getAddress());
        paper.setOrganization(entry.getOrganization());
        paper.setPublisher(entry.getPublisher());

        validation.validateConferencePaperReference(paper);
        return validation.validateRequiredConferencePaper(paper);
    }

    private Reference readWebPageReference(WebPageR entry) {

        WebPageReference webPage = new WebPageReference();

        commonField(entry, webPage);
        webPage.setAuthor(entry.getAuthor());
        webPage.setUrl(entry.getUrl());

        validation.validateWebPageReference(webPage);
        return webPage;
    }
}
