package server.manager;

import shared.enums.MeleeWeapon;
import shared.models.SpaceMarine;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public interface CollectionService {

    List<SpaceMarine> getAll();
    double getSumOfHealth();
    SpaceMarine getMinByMeleeWeapon();
    ZonedDateTime getCreationData();
    List<SpaceMarine> filterLessThanMeleeWeapon(MeleeWeapon weapon);
    int size();
    boolean isIdInCollection(long id);
    void remove(SpaceMarine spaceMarine, String owner);
    void shuffle();

    SpaceMarine getSpaceMarineById(long id);

    boolean addItem(SpaceMarine marine, String ownerUsername) throws SQLException;
    boolean addItem(int index, SpaceMarine marine, String ownerUsername) throws SQLException;
    boolean update(long id, SpaceMarine newMarine, String ownerUsername) throws SQLException;
    boolean remove(long id, String ownerUsername) throws SQLException;
    boolean removeGreater(SpaceMarine threshold, String ownerUsername) throws SQLException;
    void clear(String ownerUsername) throws SQLException;
    boolean addUpdating(Long id);
    boolean removeUpdating(Long id);
    ArrayList<Long> getUpdatingSpaceMarines();
}