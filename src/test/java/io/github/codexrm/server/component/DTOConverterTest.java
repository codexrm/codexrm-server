package io.github.codexrm.server.component;

import io.github.codexrm.server.api.dto.*;
import io.github.codexrm.server.domain.model.*;
import io.github.codexrm.server.infrastructure.persistence.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@Import({DTOConverter.class})
class DTOConverterTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private DTOConverter dtoConverter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(dtoConverter, "modelMapper", new ModelMapper());
    }

    // USER TESTS
    @Test
    void shouldConvertUserToUserDTO() {
        User user = new User();
        user.setId(1);
        user.setUsername("john");
        user.setEmail("john@test.com");
        user.setName("John");
        user.setLastName("Doe");

        UserDTO dto = dtoConverter.toUserDTO(user);

        assertNotNull(dto);
        assertEquals(1, dto.getId());
        assertEquals("john", dto.getUsername());
        assertEquals("john@test.com", dto.getEmail());
        assertEquals("John", dto.getName());
        assertEquals("Doe", dto.getLastName());
    }

    // REFERENCE TESTS
    @Test
    void shouldConvertArticleReference() {

        ArticleReference articleReference = new ArticleReference();
        articleReference.setId(1);
        articleReference.setTitle("Article Title");
        articleReference.setYear("2020");
        articleReference.setMonth("jan");
        articleReference.setNote("note");
        articleReference.setAuthor("Garcia,Juan");
        articleReference.setJournal("Nature");
        articleReference.setVolume("12");
        articleReference.setNumber("3");
        articleReference.setPages("120-135");
        articleReference.setIssn("1234-5678");

        ArticleReferenceDTO dto = (ArticleReferenceDTO) dtoConverter.toReferenceDTO(articleReference);

        assertNotNull(dto);
        assertTrue(dto instanceof ArticleReferenceDTO);

        assertEquals("Article Title", dto.getTitle());
        assertEquals("2020", dto.getYear());
        assertEquals("Garcia,Juan", dto.getAuthor());
        assertEquals("Nature", dto.getJournal());
        assertEquals("12", dto.getVolume());
        assertEquals("3", dto.getNumber());
        assertEquals("120-135", dto.getPages());
        assertEquals("1234-5678", dto.getIssn());
    }

    @Test
    void shouldConvertBookLetReference() {

        BookLetReference bookLetReference = new BookLetReference();
        bookLetReference.setId(1);
        bookLetReference.setTitle("Booklet Title");
        bookLetReference.setYear("2021");
        bookLetReference.setAuthor("Garcia,Juan");
        bookLetReference.setHowpublished("Online");
        bookLetReference.setAddress("Spain");

        BookLetReferenceDTO dto = (BookLetReferenceDTO) dtoConverter.toReferenceDTO(bookLetReference);

        assertNotNull(dto);
        assertTrue(dto instanceof BookLetReferenceDTO);

        assertEquals("Booklet Title", dto.getTitle());
        assertEquals("2021", dto.getYear());
        assertEquals("Garcia,Juan", dto.getAuthor());
        assertEquals("Online", dto.getHowpublished());
        assertEquals("Spain", dto.getAddress());
    }

    @Test
    void shouldConvertBookReference() {

        BookReference bookReference = new BookReference();
        bookReference.setId(2);
        bookReference.setTitle("Book Title");
        bookReference.setYear("2020");
        bookReference.setAuthor("Garcia,Juan");
        bookReference.setEditor("Editor Name");
        bookReference.setPublisher("Springer");
        bookReference.setVolume("1");
        bookReference.setNumber("2");
        bookReference.setSeries("Series A");
        bookReference.setAddress("USA");
        bookReference.setEdition("2nd");
        bookReference.setIsbn("1234-5678");

        BookReferenceDTO dto = (BookReferenceDTO) dtoConverter.toReferenceDTO(bookReference);

        assertNotNull(dto);
        assertTrue(dto instanceof BookReferenceDTO);

        assertEquals("Book Title", dto.getTitle());
        assertEquals("Garcia,Juan", dto.getAuthor());
        assertEquals("Springer", dto.getPublisher());
        assertEquals("1234-5678", dto.getIsbn());
    }

    @Test
    void shouldConvertBookSectionReference() {

        BookSectionReference bookSectionReference = new BookSectionReference();
        bookSectionReference.setId(3);
        bookSectionReference.setTitle("Section Title");
        bookSectionReference.setYear("2019");
        bookSectionReference.setAuthor("Garcia,Juan");
        bookSectionReference.setPublisher("Springer");
        bookSectionReference.setChapter("5");
        bookSectionReference.setPages("100-120");
        bookSectionReference.setType("Chapter");

        BookSectionReferenceDTO dto =
                (BookSectionReferenceDTO) dtoConverter.toReferenceDTO(bookSectionReference);

        assertNotNull(dto);
        assertTrue(dto instanceof BookSectionReferenceDTO);

        assertEquals("Section Title", dto.getTitle());
        assertEquals("Garcia,Juan", dto.getAuthor());
        assertEquals("Springer", dto.getPublisher());
        assertEquals("5", dto.getChapter());
        assertEquals("100-120", dto.getPages());
        assertEquals("Chapter", dto.getType());
    }

    @Test
    void shouldConvertConferencePaperReference() {

        ConferencePaperReference conferencePaperReference = new ConferencePaperReference();
        conferencePaperReference.setTitle("Paper");
        conferencePaperReference.setAuthor("Garcia,Juan");
        conferencePaperReference.setBookTitle("Conf Book");
        conferencePaperReference.setPages("1-10");

        ConferencePaperReferenceDTO dto =
                (ConferencePaperReferenceDTO) dtoConverter.toReferenceDTO(conferencePaperReference);

        assertNotNull(dto);
        assertEquals("Paper", dto.getTitle());
        assertEquals("Conf Book", dto.getBookTitle());
    }

    @Test
    void shouldConvertConferenceProceedingReference() {

        ConferenceProceedingReference conferenceProceedingReference = new ConferenceProceedingReference();
        conferenceProceedingReference.setTitle("Proceeding");
        conferenceProceedingReference.setEditor("Editor");
        conferenceProceedingReference.setPublisher("IEEE");

        ConferenceProceedingsReferenceDTO dto =
                (ConferenceProceedingsReferenceDTO) dtoConverter.toReferenceDTO(conferenceProceedingReference);

        assertNotNull(dto);
        assertEquals("Proceeding", dto.getTitle());
        assertEquals("Editor", dto.getEditor());
    }

    @Test
    void shouldConvertThesisReference() {

        ThesisReference thesisReference = new ThesisReference();
        thesisReference.setTitle("Thesis");
        thesisReference.setAuthor("Garcia,Juan");
        thesisReference.setSchool("MIT");
        thesisReference.setType("PhD");

        ThesisReferenceDTO dto =
                (ThesisReferenceDTO) dtoConverter.toReferenceDTO(thesisReference);

        assertNotNull(dto);
        assertEquals("Thesis", dto.getTitle());
        assertEquals("MIT", dto.getSchool());
    }

    @Test
    void shouldConvertWebPageReference() {

        WebPageReference webPageReference = new WebPageReference();
        webPageReference.setTitle("Web");
        webPageReference.setAuthor("Garcia,Juan");
        webPageReference.setUrl("https://test.com");

        WebPageReferenceDTO dto =
                (WebPageReferenceDTO) dtoConverter.toReferenceDTO(webPageReference);

        assertNotNull(dto);
        assertEquals("Web", dto.getTitle());
        assertEquals("https://test.com", dto.getUrl());
    }

    @Test
    void shouldThrowExceptionForUnknownReferenceType() {

        Reference unknown = new Reference() {};

        assertThrows(IllegalArgumentException.class, () -> {
            dtoConverter.toReferenceDTO(unknown);
        });
    }
}