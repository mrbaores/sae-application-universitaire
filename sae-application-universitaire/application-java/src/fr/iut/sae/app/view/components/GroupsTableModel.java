package fr.iut.sae.app.view.components;

import fr.iut.sae.app.model.dto.GroupeDTO;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class GroupsTableModel extends AbstractTableModel {

    private final String[] cols = {"Groupe", "Effectif"};
    private List<GroupeDTO> data = new ArrayList<>();

    public void setData(List<GroupeDTO> groups) {
        this.data = (groups == null) ? new ArrayList<>() : new ArrayList<>(groups);
        fireTableDataChanged();
    }

    public GroupeDTO getAt(int row) {
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
        GroupeDTO g = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> g.getNomGroupe();
            case 1 -> g.getEffectif();
            default -> "";
        };
    }
}
