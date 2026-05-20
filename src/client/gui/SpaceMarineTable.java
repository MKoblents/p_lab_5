package client.gui;

import client.utils.LocaleManager;
import shared.models.SpaceMarine;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpaceMarineTable extends AbstractTableModel {
    private List<SpaceMarine> data;
    private final String[] columnKeys = {
            "table.col.id", "table.col.name", "table.col.x", "table.col.y",
            "table.col.health", "table.col.weapon", "table.col.melee",
            "table.col.category", "table.col.chapter", "table.col.owner"
    };
    public SpaceMarineTable() {
        this.data = new ArrayList<>();
    }
    public void setData(List<SpaceMarine> newData) {
        this.data = newData != null ? new ArrayList<>(newData) : new ArrayList<>();
        fireTableDataChanged();
    }
    @Override
    public int getRowCount() {
        return data.size();
    }
    @Override
    public int getColumnCount() {
        return columnKeys.length;
    }
    @Override
    public String getColumnName(int column) {
        return LocaleManager.get(columnKeys[column]);
    }
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Long.class;
            case 1 -> String.class;
            case 2, 3 -> Long.class;
            case 4 -> Double.class;
            case 5, 6, 7, 8, 9 -> String.class;
            default -> Object.class;
        };
    }
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SpaceMarine marine = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> marine.getId();
            case 1 -> marine.getName();
            case 2 -> marine.getCoordinates() != null ? marine.getCoordinates().getX() : null;
            case 3 -> marine.getCoordinates() != null ? marine.getCoordinates().getY() : null;
            case 4 -> marine.getHealth();
            case 5 -> marine.getWeaponType() != null ? marine.getWeaponType().name() : "";
            case 6 -> marine.getMeleeWeapon() != null ? marine.getMeleeWeapon().name() : "";
            case 7 -> marine.getCategory() != null ? marine.getCategory().name() : "";
            case 8 -> marine.getChapter() != null ? marine.getChapter().getName() : "";
            case 9 -> marine.getOwner();
            default -> null;
        };
    }
    public void sortByColumn(int column) {
        Comparator<SpaceMarine> comparator = switch (column) {
            case 0 -> Comparator.comparingLong(SpaceMarine::getId);
            case 1 -> Comparator.comparing(SpaceMarine::getName, Comparator.nullsLast(Comparator.naturalOrder()));
            case 2 -> Comparator.comparing(m -> m.getCoordinates() != null ? m.getCoordinates().getX() : Long.MAX_VALUE);
            case 3 -> Comparator.comparing(m -> m.getCoordinates() != null ? m.getCoordinates().getY() : Long.MAX_VALUE);
            case 4 -> Comparator.comparingDouble(m -> m.getHealth() != null ? m.getHealth() : Double.MAX_VALUE);
            case 5 -> Comparator.comparing(m -> m.getWeaponType() != null ? m.getWeaponType().name() : "", Comparator.nullsLast(Comparator.naturalOrder()));
            case 6 -> Comparator.comparing(m -> m.getMeleeWeapon() != null ? m.getMeleeWeapon().name() : "", Comparator.nullsLast(Comparator.naturalOrder()));
            case 7 -> Comparator.comparing(m -> m.getCategory() != null ? m.getCategory().name() : "", Comparator.nullsLast(Comparator.naturalOrder()));
            case 8 -> Comparator.comparing(m -> m.getChapter() != null ? m.getChapter().getName() : "", Comparator.nullsLast(Comparator.naturalOrder()));
            case 9 -> Comparator.comparing(SpaceMarine::getOwner, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(SpaceMarine::getId);
        };
        data = data.stream().sorted(comparator).collect(Collectors.toList());
        fireTableDataChanged();
    }
    public void filterByColumn(int column, Predicate<Object> predicate) {
        data = data.stream()
                .filter(marine -> {
                    Object value = getValueAt(data.indexOf(marine), column);
                    return predicate.test(value);
                })
                .collect(Collectors.toList());
        fireTableDataChanged();
    }
    public SpaceMarine getMarineAtRow(int row) {
        if (row >= 0 && row < data.size()) {
            return data.get(row);
        }
        return null;
    }
    public List<SpaceMarine> getAllMarines() {
        return new ArrayList<>(data);
    }
}