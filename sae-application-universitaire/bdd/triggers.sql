#Verifie si le role d’un etudiant est bien etudiant

DELIMITER $$

CREATE OR REPLACE TRIGGER trg_verif_role_etudiant
BEFORE INSERT ON Etudiant
FOR EACH ROW
BEGIN
   IF NEW.idUser IS NOT NULL THEN
      IF (SELECT role FROM Utilisateur WHERE idUser = NEW.idUser) <> 'ETUDIANT' THEN
         SIGNAL SQLSTATE '45000'
         SET MESSAGE_TEXT = 'Le compte utilisateur doit avoir le rôle ETUDIANT';
      END IF;
   END IF;
END$$


CREATE OR REPLACE TRIGGER trg_verif_role_enseignant
BEFORE INSERT ON Enseignant
FOR EACH ROW
BEGIN
   IF NEW.idUser IS NOT NULL THEN
      IF (SELECT role FROM Utilisateur WHERE idUser = NEW.idUser) <> 'ENSEIGNANT' THEN
         SIGNAL SQLSTATE '45001'
         SET MESSAGE_TEXT = 'Le compte utilisateur doit avoir le rôle ENSEIGNANT';
      END IF;
   END IF;
END$$

CREATE OR REPLACE TRIGGER trg_verif_note_max
BEFORE INSERT ON Note
FOR EACH ROW
BEGIN
   DECLARE maxNote INT;

   SELECT noteMaximale
   INTO maxNote
   FROM Controle
   WHERE idControle = NEW.idControle;

   IF NEW.note > maxNote THEN
      SIGNAL SQLSTATE '45002'
      SET MESSAGE_TEXT = 'La note dépasse la note maximale du contrôle';
   END IF;
END$$

# Empeche qu’un étudiant reponde deux fois aux même sondages

CREATE OR REPLACE TRIGGER trg_pas_double_reponse_sondage
BEFORE INSERT ON Etudiant_Reponse
FOR EACH ROW
BEGIN
   IF EXISTS (
      SELECT 1
      FROM Etudiant_Reponse er
      JOIN Reponse r ON er.idReponse = r.idReponse
      WHERE er.idEtu = NEW.idEtu
        AND r.idSondage = (SELECT idSondage FROM Reponse WHERE idReponse = NEW.idReponse)
        AND typeReponse = 'bouton'
   ) THEN
      SIGNAL SQLSTATE '45003'
      SET MESSAGE_TEXT = 'L’étudiant a déjà répondu à ce sondage';
   END IF;
END$$

# Interdit les réponses apres la fermeture du sondage 

CREATE OR REPLACE TRIGGER trg_verif_date_sondage
BEFORE INSERT ON Etudiant_Reponse
FOR EACH ROW
BEGIN
   DECLARE dDebut DATETIME;
   DECLARE dFin DATETIME;

   SELECT s.dateDebut, s.dateFin
   INTO dDebut, dFin
   FROM Sondage s
   JOIN Reponse r ON s.idSondage = r.idSondage
   WHERE r.idReponse = NEW.idReponse;

   IF NOW() < dDebut OR (dFin IS NOT NULL AND NOW() > dFin) THEN
      SIGNAL SQLSTATE '45004'
      SET MESSAGE_TEXT = 'Le sondage n’est pas actif';
   END IF;
END$$

CREATE OR REPLACE TRIGGER trg_etudiant_reponse_bi_check
BEFORE INSERT ON Etudiant_Reponse
FOR EACH ROW
BEGIN
  DECLARE v_idSondage INT;
  DECLARE v_type VARCHAR(50);
  DECLARE v_debut DATETIME;
  DECLARE v_fin DATETIME;
  DECLARE v_cnt INT;

  SELECT r.idSondage, r.typeReponse, s.dateDebut, s.dateFin
  INTO v_idSondage, v_type, v_debut, v_fin
  FROM Reponse r
  JOIN Sondage s ON s.idSondage = r.idSondage
  WHERE r.idReponse = NEW.idReponse;

  IF v_idSondage IS NULL THEN
    SIGNAL SQLSTATE '45005' SET MESSAGE_TEXT = 'Reponse/sondage inexistant';
  END IF;

  IF NOW() < v_debut THEN
    SIGNAL SQLSTATE '45006' SET MESSAGE_TEXT = 'Sondage pas encore ouvert';
  END IF;

  IF v_fin IS NOT NULL AND NOW() > v_fin THEN
    SIGNAL SQLSTATE '45007' SET MESSAGE_TEXT = 'Sondage termine';
  END IF;

  IF v_type = 'bouton' THEN
    SELECT COUNT(*)
    INTO v_cnt
    FROM Etudiant_Reponse er
    JOIN Reponse r2 ON r2.idReponse = er.idReponse
    WHERE er.idEtu = NEW.idEtu
      AND r2.idSondage = v_idSondage;

    IF v_cnt > 0 THEN
      SIGNAL SQLSTATE '45008' SET MESSAGE_TEXT = 'Choix unique : deja repondu a ce sondage';
    END IF;
  END IF;
END$$

CREATE OR REPLACE TRIGGER trg_verif_type_reponse
BEFORE INSERT ON Reponse
FOR EACH ROW
BEGIN

   IF NEW.typeReponse != 'bouton' OR NEW.typeReponse != 'texte' OR NEW.typeReponse != 'case'  THEN
      SIGNAL SQLSTATE '45009'
      SET MESSAGE_TEXT = 'Le type de sondage n''est pas correct';
   END IF;
END$$

