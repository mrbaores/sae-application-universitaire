DELIMITER //

-- ---------------------------------------------------------
-- 1) Normaliser une note sur 20
-- Objectif : comparer des notes venant de contrôles avec des barèmes différents.
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION note_sur_20(p_note DECIMAL(6,2), p_noteMax DECIMAL(6,2))
RETURNS DECIMAL(6,2)
DETERMINISTIC
BEGIN
  IF p_noteMax IS NULL OR p_noteMax = 0 THEN
    RETURN NULL;
  END IF;
  IF p_note IS NULL THEN
    RETURN NULL;
  END IF;
  RETURN ROUND((p_note * 20) / p_noteMax, 2);
END//

-- ---------------------------------------------------------
-- 2) Moyenne pondérée d’un étudiant sur tous ses contrôles
-- Objectif : “score simple” global basé uniquement sur les contrôles (coef contrôle).
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION moyenne_etudiant_controles(p_idEtu INT)
RETURNS DECIMAL(5,2)
DETERMINISTIC
BEGIN
  DECLARE v_moy DECIMAL(5,2);

  SELECT ROUND(
           SUM(n.note * c.coefficient) / NULLIF(SUM(c.coefficient),0),
           2
         )
    INTO v_moy
  FROM Note n
  JOIN Controle c ON c.idControle = n.idControle
  WHERE n.idEtu = p_idEtu;

  RETURN v_moy;
END//

-- ---------------------------------------------------------
-- 3) Moyenne d’un étudiant dans une matière (pondérée par coef contrôle)
-- Objectif : alimenter “bulletin” et critères de répartition (niveau académique).
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION moyenne_matiere_etudiant(p_idEtu INT, p_idMatiere INT)
RETURNS DECIMAL(5,2)
DETERMINISTIC
BEGIN
  DECLARE v_moy DECIMAL(5,2);

  SELECT ROUND(
           SUM(n.note * c.coefficient) / NULLIF(SUM(c.coefficient),0),
           2
         )
    INTO v_moy
  FROM Note n
  JOIN Controle c ON c.idControle = n.idControle
  WHERE n.idEtu = p_idEtu
    AND c.idMatiere = p_idMatiere;

  RETURN v_moy;
END//

-- ---------------------------------------------------------
-- 4) Score global d’un étudiant (moyennes matières pondérées par coef matière)
-- Objectif : indicateur “niveau global” pour homogénéiser les groupes.
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION score_global_etudiant(p_idEtu INT)
RETURNS DECIMAL(5,2)
DETERMINISTIC
BEGIN
  DECLARE v_score DECIMAL(5,2);

  SELECT ROUND(
           SUM(mm.moy_matiere * m.coefficient) / NULLIF(SUM(m.coefficient),0),
           2
         )
    INTO v_score
  FROM (
      SELECT c.idMatiere,
             SUM(n.note * c.coefficient) / NULLIF(SUM(c.coefficient),0) AS moy_matiere
      FROM Note n
      JOIN Controle c ON c.idControle = n.idControle
      WHERE n.idEtu = p_idEtu
      GROUP BY c.idMatiere
  ) mm
  JOIN Matiere m ON m.idMatiere = mm.idMatiere;

  RETURN v_score;
END//

-- ---------------------------------------------------------
-- 5) Effectif d’un groupe (promo + semestre + nomGroupe)
-- Objectif : contrôle de taille, affichage “qualité des groupes”.
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION effectif_groupe(p_idFormation INT, p_numSemestre INT, p_nomGroupe VARCHAR(50))
RETURNS INT
DETERMINISTIC
BEGIN
  DECLARE v_nb INT;

  SELECT COUNT(*)
    INTO v_nb
  FROM Etudiant e
  WHERE e.idFormation = p_idFormation
    AND e.numSemestre = p_numSemestre
    AND e.nomGroupe = p_nomGroupe;

  RETURN v_nb;
END//

-- ---------------------------------------------------------
-- 6) Nombre d’étudiants ayant un indice de covoiturage donné (dans une promo)
-- Objectif : savoir combien veulent covoiturer ensemble.
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION nb_etudiants_indice_covoit(p_idFormation INT, p_numSemestre INT, p_indice INT)
RETURNS INT
DETERMINISTIC
BEGIN
  DECLARE v_nb INT;

  SELECT COUNT(*)
    INTO v_nb
  FROM Etudiant e
  WHERE e.idFormation = p_idFormation
    AND e.numSemestre = p_numSemestre
    AND e.indiceCovoiturage = p_indice;

  RETURN v_nb;
END//

-- ---------------------------------------------------------
-- 7) Capacité max covoiturage d’une promo (table Semestre)
-- Objectif : récupérer le paramètre “nombreMaxCovoit” fixé par responsable.
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION max_covoit_promo(p_idFormation INT, p_numSemestre INT)
RETURNS INT
DETERMINISTIC
BEGIN
  DECLARE v_max INT;

  SELECT s.nombreMaxCovoit
    INTO v_max
  FROM Semestre s
  WHERE s.idFormation = p_idFormation
    AND s.numSemestre = p_numSemestre;

  RETURN v_max;
END//

-- ---------------------------------------------------------
-- 8) Test logique : un indice covoit est-il “plein” (dépasse la limite) ?
-- Objectif : utilisé par triggers/procédures de contrôle.
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION indice_covoit_est_plein(p_idFormation INT, p_numSemestre INT, p_indice INT)
RETURNS BIT(1)
DETERMINISTIC
BEGIN
  DECLARE v_nb INT;
  DECLARE v_max INT;

  SET v_nb  = nb_etudiants_indice_covoit(p_idFormation, p_numSemestre, p_indice);
  SET v_max = max_covoit_promo(p_idFormation, p_numSemestre);

  IF v_max IS NULL THEN
    -- si pas de config semestre -> on refuse par prudence
    RETURN b'1';
  END IF;

  IF v_nb >= v_max THEN
    RETURN b'1';
  END IF;

  RETURN b'0';
END//

-- ---------------------------------------------------------
-- 9) Nombre de votes sur une réponse de sondage
-- Objectif : statistiques simples pour l’écran “résultats du sondage”.
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION nb_votes_reponse(p_idReponse INT)
RETURNS INT
DETERMINISTIC
BEGIN
  DECLARE v_nb INT;

  SELECT COUNT(*)
    INTO v_nb
  FROM Etudiant_Reponse er
  WHERE er.idReponse = p_idReponse;

  RETURN v_nb;
END//

-- ---------------------------------------------------------
-- 10) Sondage actif ?
-- Objectif : empêcher de voter hors période (utile pour trigger/proc).
-- dateFin NULL = pas de fin => actif.
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION sondage_est_actif(p_idSondage INT)
RETURNS BIT(1)
DETERMINISTIC
BEGIN
  DECLARE v_debut DATETIME;
  DECLARE v_fin   DATETIME;

  SELECT s.dateDebut, s.dateFin
    INTO v_debut, v_fin
  FROM Sondage s
  WHERE s.idSondage = p_idSondage;

  IF v_debut IS NULL THEN
    RETURN b'0';
  END IF;

  IF NOW() < v_debut THEN
    RETURN b'0';
  END IF;

  IF v_fin IS NOT NULL AND NOW() > v_fin THEN
    RETURN b'0';
  END IF;

  RETURN b'1';
END//

DELIMITER ;