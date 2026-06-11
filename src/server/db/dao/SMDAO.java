package server.db.dao;

import shared.models.SpaceMarine;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface SMDAO {//SpaceMarineDataAccessObject
    boolean insertSpaceMarine(SpaceMarine spaceMarine, String owner)  throws SQLException;
    boolean updateSpaceMarine(long id, SpaceMarine spaceMarine, String owner)  throws SQLException;

    boolean isIdInCollection(long id) throws SQLException;

    boolean deleteSpaceMarineById(long id, String ownerUsername)  throws SQLException;

    List<SpaceMarine> selectAll() throws SQLException;

    void clear(String ownerUsername)throws SQLException;

    Map<String, Object> getOwnerInfoBySpaceMarineId(long spaceMarineId)   throws SQLException;

    boolean isAncestorOrSelf(String potentialAncestor, String descendant)  throws SQLException;

    boolean deleteSpaceMarine(SpaceMarine spaceMarine, String owner)  throws SQLException;

    long getSpaceMarineId(SpaceMarine spaceMarine) throws SQLException;
}
