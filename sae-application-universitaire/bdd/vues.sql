DELIMITER //
CREATE OR REPLACE VIEW vue_stats_sondage AS
SELECT 
    s.idSondage,
    s.questionSondage,
    r.textReponse,
    COUNT(er.idEtu) AS nb_votes
FROM Sondage s
JOIN Reponse r ON s.idSondage = r.idSondage
LEFT JOIN Etudiant_Reponse er ON r.idReponse = er.idReponse
GROUP BY r.idReponse;
DELIMITER ;



CREATE OR REPLACE VIEW Vue_Bulletin_Notes AS
SELECT
    e.idEtu,
    e.nomEtu,
    e.prenomEtu,
    m.idMatiere,
    m.nomMatiere,

    -- Moyenne pondérée sur les contrôles de la matière
    ROUND(
      SUM(p.note * c.coefficient) / NULLIF(SUM(c.coefficient), 0),
      2
    ) AS moyenne_matiere

FROM Etudiant e
JOIN Note  p ON p.idEtu = e.idEtu
JOIN Controle c ON c.idControle = p.idControle
JOIN Matiere  m ON m.idMatiere = c.idMatiere

GROUP BY
    e.idEtu, e.nomEtu, e.prenomEtu,
    m.idMatiere, m.nomMatiere;



CREATE OR REPLACE VIEW v_liste_promo_publique AS
SELECT
  e.idFormation, e.numSemestre,
  e.nomEtu, e.prenomEtu, e.genreEtu, e.typeBac,
  e.emailUniEtu,
  e.nomGroupe
FROM Etudiant e;


CREATE OR REPLACE VIEW v_note_matiere_etudiant AS
SELECT
  p.idEtu,
  c.idMatiere,
  SUM(p.note * c.coefficient) / SUM(c.coefficient) AS noteMatiere
FROM Note p
JOIN Controle c ON c.idControle = p.idControle
GROUP BY p.idEtu, c.idMatiere;


CREATE OR REPLACE VIEW v_score_global_etudiant AS
SELECT
  v.idEtu,
  SUM(v.noteMatiere * m.coefficient) / SUM(m.coefficient) AS scoreGlobal
FROM v_note_matiere_etudiant v
JOIN Matiere m ON m.idMatiere = v.idMatiere
GROUP BY v.idEtu;

CREATE OR REPLACE VIEW v_qualite_groupes AS
SELECT
  e.idFormation, e.numSemestre, e.nomGroupe,
  COUNT(*) AS effectif,
  AVG(s.scoreGlobal) AS moyenneGroupe,
  STDDEV_POP(s.scoreGlobal) AS ecartTypeGroupe
FROM Etudiant e
JOIN v_score_global_etudiant s ON s.idEtu = e.idEtu
WHERE e.nomGroupe IS NOT NULL
GROUP BY e.idFormation, e.numSemestre, e.nomGroupe;

CREATE OR REPLACE VIEW Vue_Dossier_Pedagogique AS
SELECT
    e.idEtu,
    e.nomEtu,
    e.prenomEtu,

    m.nomMatiere,

    c.nomExamen,
    p.note,
    c.noteMaximale,
    c.coefficient AS Coeff_Examen,

    ens.nomEns AS Enseignant_Responsable

FROM Etudiant e
JOIN Note p
    ON p.idEtu = e.idEtu
JOIN Controle c
    ON c.idControle = p.idControle
JOIN Matiere m
    ON m.idMatiere = c.idMatiere
JOIN Enseignant ens
    ON ens.idEnseignant = m.profResponsable;

