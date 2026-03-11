DELIMITER //


DELIMITER //

CREATE OR REPLACE PROCEDURE MoyenneEtudiant(
    IN p_idEtu INT,
    OUT p_moyenne DECIMAL(5,2)
)
BEGIN
    SELECT
        ROUND(
            SUM(p.note * c.coefficient) / SUM(c.coefficient),
            2
        )
    INTO p_moyenne
    FROM Note p
    JOIN Controle c ON p.idControle = c.idControle
    WHERE p.idEtu = p_idEtu;
END//

DELIMITER ;


DELIMITER //


CREATE OR REPLACE FUNCTION MoyenneEtudiantFn(p_idEtu INT)
RETURNS DECIMAL(5,2)
BEGIN
    DECLARE v_moy DECIMAL(5,2);

    SELECT
        ROUND(
            SUM(p.note * c.coefficient) / SUM(c.coefficient),
            2
        )
    INTO v_moy
    FROM Note p
    JOIN Controle c ON p.idControle = c.idControle
    WHERE p.idEtu = p_idEtu;

    RETURN v_moy;
END//

DELIMITER ;


DELIMITER //

CREATE OR REPLACE PROCEDURE AffecterGroupe(
    IN p_idEtu INT,
    IN p_nomGroupe VARCHAR(50)
)
BEGIN
    DECLARE v_idFormation INT;
    DECLARE v_numSemestre INT;

    SELECT idFormation, numSemestre
    INTO v_idFormation, v_numSemestre
    FROM Etudiant
    WHERE idEtu = p_idEtu;

    UPDATE Etudiant
    SET nomGroupe = p_nomGroupe
    WHERE idEtu = p_idEtu
      AND EXISTS (
        SELECT 1
        FROM Groupe g
        WHERE g.idFormation = v_idFormation
          AND g.numSemestre = v_numSemestre
          AND g.nomGroupe = p_nomGroupe
      );
END//

DELIMITER ;


DELIMITER //

CREATE OR REPLACE PROCEDURE ListeRedoublants()
BEGIN
    SELECT
        idEtu,
        nomEtu,
        prenomEtu,
        numSemestre
    FROM Etudiant
    WHERE estRedoublant = b'1';
END//

DELIMITER ;

DELIMITER //

CREATE OR REPLACE FUNCTION note_sur_20(p_note DECIMAL(6,2), p_noteMax DECIMAL(6,2))
RETURNS DECIMAL(6,2)
BEGIN
  IF p_noteMax IS NULL OR p_noteMax = 0 THEN
    RETURN NULL;
  END IF;
  RETURN ROUND((p_note * 20) / p_noteMax, 2);
END //
DELIMITER ;


DELIMITER //

CREATE OR REPLACE FUNCTION moyenne_etudiant_matiere(p_idEtu INT, p_idMatiere INT)
RETURNS DECIMAL(6,2)
READS SQL DATA
BEGIN
  DECLARE v_moy DECIMAL(6,2);

  SELECT
    ROUND(
      SUM(note_sur_20(p.note, c.noteMaximale) * c.coefficient) / NULLIF(SUM(c.coefficient), 0),
    2)
  INTO v_moy
  FROM Note p
  JOIN Controle c ON c.idControle = p.idControle
  WHERE p.idEtu = p_idEtu
    AND c.idMatiere = p_idMatiere;

  RETURN v_moy;
END//
DELIMITER ;


DELIMITER //

DROP PROCEDURE IF EXISTS affecter_etudiant_groupe;
DELIMITER $$

CREATE OR REPLACE PROCEDURE affecter_etudiant_groupe(
  IN p_idEtu INT,
  IN p_idFormation INT,
  IN p_numSemestre INT,
  -- On ajoute COLLATE pour forcer la comparaison correcte
  IN p_nomGroupe VARCHAR(50) COLLATE utf8mb4_unicode_ci 
)
BEGIN
  DECLARE v_exists INT;

  -- 1. Vérifier si l'étudiant existe dans la promo
  SELECT COUNT(*) INTO v_exists
  FROM Etudiant
  WHERE idEtu = p_idEtu
    AND idFormation = p_idFormation
    AND numSemestre = p_numSemestre;

  IF v_exists = 0 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Étudiant introuvable dans cette promotion.';
  END IF;

  -- 2. Vérifier si le groupe existe
  SELECT COUNT(*) INTO v_exists
  FROM Groupe
  WHERE idFormation = p_idFormation
    AND numSemestre = p_numSemestre
    AND nomGroupe = p_nomGroupe; -- La comparaison fonctionnera grâce au COLLATE plus haut

  IF v_exists = 0 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Groupe inexistant pour cette promotion.';
  END IF;

  -- 3. Mise à jour
  UPDATE Etudiant
  SET nomGroupe = p_nomGroupe
  WHERE idEtu = p_idEtu;
  
END$$

DELIMITER ;

DELIMITER //
DROP PROCEDURE IF EXISTS ajouter_note;
DELIMITER $$

CREATE OR REPLACE PROCEDURE ajouter_note(
  IN p_idEtu INT,
  IN p_idControle INT,
  IN p_note DECIMAL(6,2)
)
BEGIN
  DECLARE v_noteMax INT;

  SELECT noteMaximale
    INTO v_noteMax
  FROM Controle
  WHERE idControle = p_idControle;

  IF v_noteMax IS NULL THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Contrôle inexistant.';
  END IF;

  IF p_note < 0 OR p_note > v_noteMax THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Note hors limites.';
  END IF;

  INSERT INTO Note(idEtu, idControle, note)
  VALUES (p_idEtu, p_idControle, p_note);
END$$

DELIMITER ;


DELIMITER $$

CREATE OR REPLACE PROCEDURE ajouter_note(
  IN p_idEtu INT,
  IN p_idControle INT,
  IN p_note DECIMAL(4,2)
)
BEGIN
  DECLARE v_noteMax TINYINT;

  -- 1. Récupération et vérification
  SELECT noteMaximale INTO v_noteMax
  FROM Controle
  WHERE idControle = p_idControle;

  IF v_noteMax IS NULL THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Erreur : Le controle spécifié n''existe pas.';
  END IF;

  -- 2. Vérification de la note
  IF p_note < 0 OR p_note > v_noteMax THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Erreur : Note hors limites pour ce controle.';
  END IF;

  -- 3. Insertion ou mise à jour
  INSERT INTO Note(idEtu, idControle, note)
  VALUES (p_idEtu, p_idControle, p_note)
  ON DUPLICATE KEY UPDATE note = p_note;

END $$

DELIMITER ;


DELIMITER $$

CREATE OR REPLACE PROCEDURE sp_creer_sondage(
  IN p_idEnseignant INT,
  IN p_question VARCHAR(5000),
  IN p_dateFin DATETIME,
  IN p_typeReponse VARCHAR(50),
  IN p_reponses TEXT
)
BEGIN
  DECLARE v_idSondage INT;
  DECLARE v_rest TEXT;
  DECLARE v_pos INT;
  DECLARE v_item VARCHAR(1000);

  INSERT INTO Sondage(questionSondage, dateFin, idEnseignant)
  VALUES (p_question, p_dateFin, p_idEnseignant);

  SET v_idSondage = LAST_INSERT_ID();

  SET v_rest = p_reponses;

  WHILE v_rest IS NOT NULL AND LENGTH(v_rest) > 0 DO
    SET v_pos = LOCATE('|', v_rest);

    IF v_pos = 0 THEN
      SET v_item = TRIM(v_rest);
      SET v_rest = '';
    ELSE
      SET v_item = TRIM(SUBSTRING(v_rest, 1, v_pos - 1));
      SET v_rest = SUBSTRING(v_rest, v_pos + 1);
    END IF;

    IF v_item <> '' THEN
      INSERT INTO Reponse(textReponse, typeReponse, idSondage)
      VALUES (v_item, p_typeReponse, v_idSondage);
    END IF;
  END WHILE;

  SELECT v_idSondage AS idSondage_cree;
END $$

DELIMITER ;

DELIMITER $$

CREATE OR REPLACE PROCEDURE sp_retirer_etudiant_groupe(
  IN p_idEtu INT
)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM Etudiant WHERE idEtu=p_idEtu) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Etudiant introuvable';
  END IF;

  UPDATE Etudiant
  SET nomGroupe = NULL
  WHERE idEtu = p_idEtu;
END $$

DELIMITER ;

DELIMITER //

CREATE OR REPLACE PROCEDURE sp_modifier_etudiant_pedago(
  IN p_idEtu INT,
  IN p_typeBac VARCHAR(50),
  IN p_estRedoublant BIT,
  IN p_estApprenti BIT,
  IN p_estAnglophone BIT,
  IN p_indiceCovoit INT
)
BEGIN
  -- Vérification de l'existence de l'étudiant
  IF NOT EXISTS (SELECT 1 FROM Etudiant WHERE idEtu=p_idEtu) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Etudiant introuvable';
  END IF;

  -- Mise à jour des infos pédagogiques
  UPDATE Etudiant
  SET typeBac = p_typeBac,
      estRedoublant = p_estRedoublant,
      estApprenti = p_estApprenti,
      estAnglophone = p_estAnglophone,
      indiceCovoiturage = p_indiceCovoit
  WHERE idEtu = p_idEtu;
END//

DELIMITER ;

DELIMITER //
CREATE OR REPLACE PROCEDURE sp_supprimer_etudiant(IN p_idEtu INT)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM Etudiant WHERE idEtu = p_idEtu) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Etudiant introuvable';
  END IF;
  -- Suppression des donnees liees pour eviter les erreurs de cle etrangere
  DELETE FROM Etudiant_Reponse WHERE idEtu = p_idEtu;
  DELETE FROM Note WHERE idEtu = p_idEtu;
  DELETE FROM Etudiant WHERE idEtu = p_idEtu;
END//
DELIMITER ;

DELIMITER //

CREATE OR REPLACE FUNCTION NbVotesSondageFn(p_idSondage INT)
RETURNS INT
DETERMINISTIC
BEGIN
  DECLARE v_nb INT;

  -- Compte le nombre total de participations pour un sondage donné
  SELECT COUNT(*)
  INTO v_nb
  FROM Etudiant_Reponse er
  JOIN Reponse r ON r.idReponse = er.idReponse
  WHERE r.idSondage = p_idSondage;

  RETURN v_nb;
END //

DELIMITER ;
