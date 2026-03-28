-- =========================
-- ROLES
-- =========================
CREATE TABLE rol (
                     id SERIAL PRIMARY KEY,
                     name VARCHAR(20) NOT NULL UNIQUE
);

-- =========================
-- USERS (RENAMED)
-- =========================
CREATE TABLE app_user (
                          id SERIAL PRIMARY KEY,
                          username VARCHAR(255) NOT NULL UNIQUE,
                          name VARCHAR(255) NOT NULL,
                          lastname VARCHAR(255) NOT NULL,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          password VARCHAR(100) NOT NULL,
                          enabled BOOLEAN NOT NULL
);

-- =========================
-- USER ROLES (ManyToMany)
-- =========================
CREATE TABLE user_roles (
                            user_id INTEGER NOT NULL,
                            role_id INTEGER NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES rol(id) ON DELETE CASCADE
);

-- =========================
-- REFERENCE (BASE)
-- =========================
CREATE TABLE reference (
                           id SERIAL PRIMARY KEY,
                           title VARCHAR(255),
                           year VARCHAR(255),
                           month VARCHAR(255),
                           note VARCHAR(255),
                           usercodex INTEGER,
                           FOREIGN KEY (usercodex) REFERENCES app_user(id) ON DELETE SET NULL
);

-- =========================
-- ARTICLE REFERENCE
-- =========================
CREATE TABLE articlereference (
                                  id INTEGER PRIMARY KEY,
                                  author VARCHAR(255),
                                  journal VARCHAR(255),
                                  volume VARCHAR(255),
                                  numbera VARCHAR(255),
                                  pages VARCHAR(255),
                                  issn VARCHAR(255),
                                  FOREIGN KEY (id) REFERENCES reference(id) ON DELETE CASCADE
);

-- =========================
-- BOOK REFERENCE
-- =========================
CREATE TABLE bookreference (
                               id INTEGER PRIMARY KEY,
                               author VARCHAR(255),
                               editor VARCHAR(255),
                               publisher VARCHAR(255),
                               volume VARCHAR(255),
                               numbera VARCHAR(255),
                               series VARCHAR(255),
                               address VARCHAR(255),
                               edition VARCHAR(255),
                               isbn VARCHAR(255),
                               FOREIGN KEY (id) REFERENCES reference(id) ON DELETE CASCADE
);

-- =========================
-- BOOKLET REFERENCE
-- =========================
CREATE TABLE bookletreference (
                                  id INTEGER PRIMARY KEY,
                                  author VARCHAR(255),
                                  howpublished VARCHAR(255),
                                  address VARCHAR(255),
                                  FOREIGN KEY (id) REFERENCES reference(id) ON DELETE CASCADE
);

-- =========================
-- BOOK SECTION (HEREDA DE BOOK)
-- =========================
CREATE TABLE booksectionreference (
                                      id INTEGER PRIMARY KEY,
                                      chapter VARCHAR(255),
                                      pages VARCHAR(255),
                                      type VARCHAR(255),
                                      FOREIGN KEY (id) REFERENCES bookreference(id) ON DELETE CASCADE
);

-- =========================
-- CONFERENCE PAPER
-- =========================
CREATE TABLE conferencepaperreference (
                                          id INTEGER PRIMARY KEY,
                                          author VARCHAR(255),
                                          booktitle VARCHAR(255),
                                          editor VARCHAR(255),
                                          volume VARCHAR(255),
                                          numbera VARCHAR(255),
                                          series VARCHAR(255),
                                          pages VARCHAR(255),
                                          address VARCHAR(255),
                                          organization VARCHAR(255),
                                          publisher VARCHAR(255),
                                          FOREIGN KEY (id) REFERENCES reference(id) ON DELETE CASCADE
);

-- =========================
-- CONFERENCE PROCEEDINGS
-- =========================
CREATE TABLE conferenceproceedingsreference (
                                                id INTEGER PRIMARY KEY,
                                                editor VARCHAR(255),
                                                volume VARCHAR(255),
                                                numbera VARCHAR(255),
                                                series VARCHAR(255),
                                                address VARCHAR(255),
                                                publisher VARCHAR(255),
                                                organization VARCHAR(255),
                                                isbn VARCHAR(255),
                                                FOREIGN KEY (id) REFERENCES reference(id) ON DELETE CASCADE
);

-- =========================
-- THESIS
-- =========================
CREATE TABLE thesisreference (
                                 id INTEGER PRIMARY KEY,
                                 author VARCHAR(255),
                                 school VARCHAR(255),
                                 type VARCHAR(255),
                                 address VARCHAR(255),
                                 FOREIGN KEY (id) REFERENCES reference(id) ON DELETE CASCADE
);

-- =========================
-- WEBPAGE
-- =========================
CREATE TABLE webpagereference (
                                  id INTEGER PRIMARY KEY,
                                  author VARCHAR(255),
                                  url VARCHAR(255),
                                  FOREIGN KEY (id) REFERENCES reference(id) ON DELETE CASCADE
);

-- =========================
-- REFRESH TOKEN (OneToOne)
-- =========================
CREATE TABLE refreshtoken (
                              id SERIAL PRIMARY KEY,
                              token VARCHAR(255) NOT NULL UNIQUE,
                              expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
                              user_id INTEGER UNIQUE,
                              FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);