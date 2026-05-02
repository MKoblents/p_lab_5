package server.manager;

import shared.enums.MeleeWeapon;
import shared.models.SpaceMarine;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

public interface CollectionService {

    List<SpaceMarine> getSpaceMarines();
    double getSumOfHealth();
    SpaceMarine getMinByMeleeWeapon();
    ZonedDateTime getCreationData();
    List<SpaceMarine> filterLessThanMeleeWeapon(MeleeWeapon weapon);
    int size();
    boolean isIdInCollection(long id)  throws SQLException;
    boolean remove(SpaceMarine spaceMarine, String owner) throws SQLException;
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
    Set<Long> getUpdatingSpaceMarines();
    String getOwnerName(long spaceMarineId) throws SQLException;
    boolean isAncestorOrSelf(String potentialAncestor, String descendant) throws SQLException;
}