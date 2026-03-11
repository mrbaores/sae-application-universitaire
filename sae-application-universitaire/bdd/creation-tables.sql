SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS Enseignant_Matiere;
DROP TABLE IF EXISTS ResponsableSemestre;
DROP TABLE IF EXISTS Etudiant_Reponse;
DROP TABLE IF EXISTS Note;

DROP TABLE IF EXISTS Etudiant;
DROP TABLE IF EXISTS Groupe;
DROP TABLE IF EXISTS Semestre;

DROP TABLE IF EXISTS Reponse;
DROP TABLE IF EXISTS Sondage;

DROP TABLE IF EXISTS Controle;
DROP TABLE IF EXISTS Matiere;

DROP TABLE IF EXISTS Enseignant;
DROP TABLE IF EXISTS Utilisateur;

DROP TABLE IF EXISTS Formation;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- Formation
-- =========================
CREATE TABLE Formation(
   idFormation INT AUTO_INCREMENT,
   nomFormation VARCHAR(100) NOT NULL,
   PRIMARY KEY(idFormation),
   UNIQUE(nomFormation)
);

-- =========================
-- Utilisateur (auth + rôles)
-- =========================
CREATE TABLE Utilisateur(
   idUser INT AUTO_INCREMENT,
   login VARCHAR(80) NOT NULL,
   passwordHash VARCHAR(255) NOT NULL,
   role ENUM('ETUDIANT','ENSEIGNANT','RESP_SEMESTRE','RESP_FORMATION') NOT NULL,
   actif BIT(1) NOT NULL DEFAULT b'1',
   PRIMARY KEY(idUser),
   UNIQUE(login)
);

-- =========================
-- Enseignant
-- =========================
CREATE TABLE Enseignant(
   idEnseignant INT AUTO_INCREMENT,
   nomEns VARCHAR(50) NOT NULL,
   prenomEns VARCHAR(50) NOT NULL,

   -- 1 compte (Utilisateur) max par enseignant
   idUser INT,

   estResponsableAnnee BIT(1) NOT NULL DEFAULT b'0',
   responsableFormation INT,

   PRIMARY KEY(idEnseignant),

   FOREIGN KEY(idUser) REFERENCES Utilisateur(idUser),
   FOREIGN KEY(responsableFormation) REFERENCES Formation(idFormation)
);

-- =========================
-- Matière
-- =========================
CREATE TABLE Matiere(
   idMatiere INT AUTO_INCREMENT,
   nomMatiere VARCHAR(100) NOT NULL,
   ue SMALLINT NOT NULL CHECK (ue BETWEEN 101 AND 606),
   coefficient TINYINT NOT NULL CHECK (coefficient > 0),
   profResponsable INT NOT NULL,

   PRIMARY KEY(idMatiere),

   FOREIGN KEY(profResponsable) REFERENCES Enseignant(idEnseignant)
);

-- =========================
-- Contrôle
-- =========================
CREATE TABLE Controle(
   idControle INT AUTO_INCREMENT,
   nomExamen VARCHAR(100) NOT NULL,
   noteMaximale TINYINT NOT NULL DEFAULT 20 CHECK (noteMaximale BETWEEN 1 AND 20),
   coefficient TINYINT NOT NULL CHECK (coefficient > 0 AND coefficient < 100),
   idMatiere INT NOT NULL,

   PRIMARY KEY(idControle),

   FOREIGN KEY(idMatiere) REFERENCES Matiere(idMatiere)
);

-- =========================
-- Sondage / Réponse
-- =========================
CREATE TABLE Sondage(
   idSondage INT AUTO_INCREMENT,
   questionSondage VARCHAR(5000) NOT NULL,
   dateDebut DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
   dateFin DATETIME,
   idEnseignant INT NOT NULL,

   PRIMARY KEY(idSondage),

   FOREIGN KEY(idEnseignant) REFERENCES Enseignant(idEnseignant)
);

CREATE TABLE Reponse(
   idReponse INT AUTO_INCREMENT,
   textReponse VARCHAR(1000) NOT NULL,
   typeReponse VARCHAR(50) NOT NULL,
   idSondage INT NOT NULL,

   PRIMARY KEY(idReponse),

   FOREIGN KEY(idSondage) REFERENCES Sondage(idSondage)
);

-- =========================
-- Semestre / Groupe
-- =========================
CREATE TABLE Semestre(
   idFormation INT NOT NULL,
   numSemestre INT NOT NULL CHECK (numSemestre BETWEEN 1 AND 6),
   nombreMaxCovoit TINYINT NOT NULL CHECK (nombreMaxCovoit BETWEEN 2 AND 4),
   estApprentissage BIT(1) NOT NULL DEFAULT b'0',

   PRIMARY KEY(idFormation, numSemestre),

   FOREIGN KEY(idFormation) REFERENCES Formation(idFormation)
);

CREATE TABLE Groupe(
   idFormation INT NOT NULL,
   numSemestre INT NOT NULL,
   nomGroupe VARCHAR(50) NOT NULL,

   PRIMARY KEY(idFormation, numSemestre, nomGroupe),

   FOREIGN KEY(idFormation, numSemestre) REFERENCES Semestre(idFormation, numSemestre)
);

-- =========================
-- Étudiant
-- =========================
CREATE TABLE Etudiant(
   idEtu INT AUTO_INCREMENT,

   nomEtu VARCHAR(100) NOT NULL,
   prenomEtu VARCHAR(100) NOT NULL,
   dateNaissance DATE NOT NULL,
   tel VARCHAR(16) NOT NULL,
   typeBac VARCHAR(50) NOT NULL,
   addresseEtu VARCHAR(250) NOT NULL,
   emailPersoEtu VARCHAR(150) NOT NULL,
   emailUniEtu VARCHAR(300) NOT NULL,

   genreEtu BIT(1),
   indiceCovoiturage INT,
   estAnglophone BIT(1) NOT NULL DEFAULT b'0',

   -- Critères utiles pour constitution des groupes
   estRedoublant BIT(1) NOT NULL DEFAULT b'0',
   estApprenti  BIT(1) NOT NULL DEFAULT b'0',

   -- NULL = pas encore affecté à un groupe
   nomGroupe VARCHAR(50),
   idFormation INT,
   numSemestre INT,

   -- 1 compte (Utilisateur) max par étudiant
   idUser INT,

   PRIMARY KEY(idEtu),
   UNIQUE(emailUniEtu),

   FOREIGN KEY(idUser) REFERENCES Utilisateur(idUser),

   FOREIGN KEY(idFormation, numSemestre, nomGroupe) REFERENCES Groupe(idFormation, numSemestre, nomGroupe)
);

-- =========================
-- Notes : Possède
-- =========================
CREATE TABLE Note(
   idEtu INT NOT NULL,
   idControle INT NOT NULL,
   note DECIMAL(4,2) NOT NULL CHECK (note >= 0),

   PRIMARY KEY(idEtu, idControle),

   FOREIGN KEY(idEtu) REFERENCES Etudiant(idEtu),
   FOREIGN KEY(idControle) REFERENCES Controle(idControle)
);

-- =========================
-- Réponses étudiants aux sondages
-- =========================
CREATE TABLE Etudiant_Reponse(
   idEtu INT NOT NULL,
   idReponse INT NOT NULL,
   dateReponse DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

   PRIMARY KEY(idEtu, idReponse),

   FOREIGN KEY(idEtu) REFERENCES Etudiant(idEtu),
   FOREIGN KEY(idReponse) REFERENCES Reponse(idReponse)
);

-- =========================
-- Responsable Semestre
-- =========================
CREATE TABLE ResponsableSemestre(
   idEnseignant INT NOT NULL,
   idFormation INT NOT NULL,
   numSemestre INT NOT NULL,

   PRIMARY KEY(idEnseignant, idFormation, numSemestre),

   FOREIGN KEY(idEnseignant) REFERENCES Enseignant(idEnseignant),
   FOREIGN KEY(idFormation, numSemestre) REFERENCES Semestre(idFormation, numSemestre)
);

-- =========================
-- Enseignant <-> Matière
-- =========================
CREATE TABLE Enseignant_Matiere(
   idEnseignant INT NOT NULL,
   idMatiere INT NOT NULL,

   PRIMARY KEY(idEnseignant, idMatiere),

   FOREIGN KEY(idEnseignant) REFERENCES Enseignant(idEnseignant),
   FOREIGN KEY(idMatiere) REFERENCES Matiere(idMatiere)
);
