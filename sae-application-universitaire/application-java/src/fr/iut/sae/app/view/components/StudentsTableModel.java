package fr.iut.sae.app.view.components;

import fr.iut.sae.app.model.dto.EtudiantDTO;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class StudentsTableModel extends AbstractTableModel {

    private final String[] cols = {"ID", "Nom", "Prénom", "Bac", "Genre", "Redoublant", "Covoit", "Groupe"};
    private final List<EtudiantDTO> data = new ArrayList<>();

    public void setData(List<EtudiantDTO> etudiants) {
        data.clear();
        if (etudiants != null) data.addAll(etudiants);
        fireTableDataChanged();
    }

    public EtudiantDTO getAt(int row) {
        if (row < 0 || row >= data.size()) return null;
        return data.get(row);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return cols.length;
    }

    @Override
    public String getColumnName(int column) {
        return cols[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        EtudiantDTO e = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> e.getIdEtu();
            case 1 -> e.getNomEtu();
            case 2 -> e.getPrenomEtu();
            case 3 -> e.getTypeBac();
            case 4 -> e.getGenreEtu();
            case 5 -> e.getEstRedoublant() == 1 ? "Oui" : "Non";
            case 6 -> e.getIndiceCovoiturage();
            case 7 -> (e.getNomGroupe() == null || e.getNomGroupe().isBlank()) ? "-" : e.getNomGroupe();
            default -> "";
        };
    }
}
