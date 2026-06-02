package io.github.codexrm.server.infrastructure.persistence.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.codexrm.server.api.dto.request.LoginRequest;
import io.github.codexrm.server.api.dto.request.SignupRequest;
import io.github.codexrm.server.api.dto.response.JwtResponse;
import io.github.codexrm.server.domain.model.ArticleReference;
import io.github.codexrm.server.domain.model.Reference;
import io.github.codexrm.server.domain.model.User;
import io.github.codexrm.server.infrastructure.persistence.repository.ReferenceRepository;
import io.github.codexrm.server.infrastructure.persistence.repository.RefreshTokenRepository;
import io.github.codexrm.server.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class ReferenceSynchronizationIntegrationTest extends BaseIntegrationTest  {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReferenceRepository referenceRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private String token;

    private User user;

    @BeforeEach
    void setup() throws Exception {

        refreshTokenRepository.deleteAll();
        referenceRepository.deleteAll();
        userRepository.deleteAll();


        SignupRequest signup = new SignupRequest();
        signup.setUsername("syncUser");
        signup.setPassword("Test@123");
        signup.setEmail("sync@test.com");
        signup.setName("User");
        signup.setLastName("Test");
        signup.setEnabled(true);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)));

        LoginRequest login = new LoginRequest();
        login.setUsername("syncUser");
        login.setPassword("Test@123");

        String response = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JwtResponse jwt = objectMapper.readValue(response, JwtResponse.class);

        token = "Bearer " + jwt.getToken();

        user = userRepository.findByUsername("syncUser").orElseThrow();
    }

    @Test
    void shouldCreateReferencesThroughSync() throws Exception {

        String body = """
                {
                  "newReferencesList": [
                    {
                      "referenceType": "ArticleReferenceDTO",
                      "title": "Distributed Systems",
                      "author": "Perez,Maria",
                      "year": "2024",
                      "journal": "Nature",
                      "month": "jan",
                      "issn": "1234-5678",
                      "number": "3",
                      "pages": "120-135",
                      "volume": "12"
                    }
                  ],
                  "updatedReferencesList": [],
                  "deletedReferencesList": [],
                  "sortReference": "id,desc"
                }
                """;

        mockMvc.perform(post("/api/references/sync")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceDTOList", hasSize(1)))
                .andExpect(jsonPath("$.referenceDTOList[0].title")
                        .value("Distributed Systems"))
                .andExpect(jsonPath("$.referenceDTOList[0].referenceType")
                        .value("ArticleReferenceDTO"))
                .andExpect(jsonPath("$.referenceDTOList[0].author")
                        .value("Perez,Maria"))
                .andExpect(jsonPath("$.pageDTO.totalElement")
                        .value(1));
    }

    @Test
    void shouldUpdateReferenceThroughSync() throws Exception {

        ArticleReference saved = new ArticleReference();
        saved.setTitle("Old Title");
        saved.setAuthor("Martin,Robert");
        saved.setYear("2020");
        saved.setJournal("Journal");
        saved.setMonth("mar");
        saved.setIssn("0740-7459");
        saved.setNumber("2");
        saved.setPages("45-58");
        saved.setVolume("38");
        saved.setUser(user);

        saved = referenceRepository.save(saved);

        String body = """
                {
                  "newReferencesList": [],
                  "updatedReferencesList": [
                    {
                      "referenceType": "ArticleReferenceDTO",
                      "id": %d,
                      "title": "New Title",
                      "author": "Silva,Joao",
                      "year": "2025",
                      "journal": "Journal",
                      "month": "mar",
                      "issn": "1984-1122",
                      "number": "7",
                      "pages": "200-219",
                      "volume": "15"
                    }
                  ],
                  "deletedReferencesList": [],
                  "sortReference": "id,desc"
                }
                """.formatted(saved.getId());


        mockMvc.perform(post("/api/references/sync")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        Reference result = referenceRepository
                .findById(saved.getId())
                .orElseThrow();

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(((ArticleReference) result).getAuthor()).isEqualTo("Silva,Joao");
        assertThat(result.getYear()).isEqualTo("2025");
    }

    @Test
    void shouldDeleteReferenceThroughSync() throws Exception {

        ArticleReference reference = new ArticleReference();
        reference.setTitle("To Delete");
        reference.setAuthor("Fernandez,Lucas");
        reference.setYear("2022");
        reference.setJournal("Journal of Cybersecurity");
        reference.setMonth("oct");
        reference.setIssn("2049-6613");
        reference.setNumber("4");
        reference.setPages("89-101");
        reference.setVolume("9");
        reference.setUser(user);

        reference = referenceRepository.save(reference);

        String body = """
                   {
                   "newReferencesList": [],
                    "updatedReferencesList": [],
                    "deletedReferencesList": [%d],
                    "sortReference": "id,desc"
                    }
                   """.formatted(reference.getId());

        mockMvc.perform(post("/api/references/sync")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }
}