package server.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.db.dao.SpaceMarineDAO;
import shared.models.SpaceMarine;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollectionCache {
    private static final Logger logger = LoggerFactory.getLogger(CollectionCache.class);
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
    public boolean updateInMemory(long id, SpaceMarine spaceMarine, String owner) throws SQLException {
        boolean updated =  spaceMarineDAO.updateSpaceMarine(id,spaceMarine,owner);
        if (updated){
            sinchronizedCache.removeIf(m->m.getId() == id);
            sinchronizedCache.add(spaceMarine);
            logger.info("SpaceMarine {} successfully updated", id);
        }else {
            logger.info("SpaceMarine {} isn't updated, something went wrong", id);
        }
        return updated;
    }
    public boolean removeFromMemory(SpaceMarine spaceMarine, String owner) throws SQLException {
        boolean removed = spaceMarineDAO.deleteSpaceMarine(spaceMarine, owner);
        if (removed){
            sinchronizedCache.remove(spaceMarine);
            logger.info("SpaceMarine {} successfully removed", spaceMarine);
        }else {
            logger.info("SpaceMarine {} isn't removed. Something went wrong", spaceMarine);
        }
        return removed;
    }

}
