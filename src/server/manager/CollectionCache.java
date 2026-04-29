package server.manager;

import server.db.dao.SpaceMarineDAO;
import shared.models.SpaceMarine;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollectionCache {
    private final List<SpaceMarine> sinchronizedCache;
    private final SpaceMarineDAO spaceMarineDAO;
    public CollectionCache(SpaceMarineDAO spaceMarineDAO) throws SQLException {
        this.spaceMarineDAO = spaceMarineDAO;
        this.sinchronizedCache = (List<SpaceMarine>) Collections.synchronizedCollection(new ArrayList<>(spaceMarineDAO.selectAll()));
    }
    public List<SpaceMarine> getReadOnlySnapshot(){
        return List.copyOf(sinchronizedCache);
    }
    public boolean AddAndSyncToMemory(SpaceMarine spaceMarine, String owner) throws SQLException {
        boolean p = spaceMarineDAO.insertSpaceMarine(spaceMarine, owner);
        if (p){
            sinchronizedCache.add(spaceMarine);
            return true;
        }
        return false;
    }
    public boolean updateInMemory(long id, SpaceMarine spaceMarine, String owner){
//        TODO
        return false;
    }

}
