package server.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.db.dao.SMDAO;
import server.db.dao.UserDAO;
import shared.enums.MeleeWeapon;
import shared.models.SpaceMarine;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class CollectionCache implements CollectionService {
    private static final Logger logger = LoggerFactory.getLogger(CollectionCache.class);
    private final List<SpaceMarine> spaceMarines;
    private final Set<Long> updatingSpaceMarines = Collections.synchronizedSet(new HashSet<>());
    private final SMDAO smdao;
    private ZonedDateTime creationData;
    private final UserDAO userDAO;
    public CollectionCache(UserDAO userDAO, SMDAO smdao) throws SQLException {
        this.smdao = smdao;
        this.userDAO = userDAO;
        this.spaceMarines = Collections.synchronizedList(new ArrayList<>(smdao.selectAll()));
        creationData = ZonedDateTime.now();
    }
    @Override
    public List<SpaceMarine> getSpaceMarines(){
        return List.copyOf(spaceMarines);
    }
    @Override
    public boolean addItem(SpaceMarine spaceMarine, String owner) throws SQLException {
        boolean p = smdao.insertSpaceMarine(spaceMarine, owner);
        if (p){
            spaceMarines.add(spaceMarine);
            return true;
        }
        return false;
    }

    @Override
    public void reload() throws SQLException {
        spaceMarines.clear();
        java.util.List<SpaceMarine> sm = smdao.selectAll();
        for (SpaceMarine m : sm) {
            spaceMarines.add(m);
        }
    }

    @Override
    public boolean update(long id, SpaceMarine spaceMarine, String owner) throws SQLException {
        String realOwner = smdao.getOwnerInfoBySpaceMarineId(id).get("name").toString();
        boolean isAns = userDAO.isAncestorOrSelf(owner, realOwner);
        if (isAns){
            boolean updated =  smdao.updateSpaceMarine(id,spaceMarine,realOwner);
            if (updated){
                spaceMarines.removeIf(m->m.getId() == id);
                spaceMarines.add(spaceMarine);
                logger.info("SpaceMarine {} successfully updated", id);
            }else {
                logger.info("SpaceMarine {} isn't updated, something went wrong", id);
            }
            return updated;
        }
        return false;
    }
    @Override
    public boolean remove(SpaceMarine spaceMarine, String owner) throws SQLException {
        long id = smdao.getSpaceMarineId(spaceMarine);
        String realOwner = smdao.getOwnerInfoBySpaceMarineId(id).get("name").toString();
        boolean isAns = userDAO.isAncestorOrSelf(owner, realOwner);
        if (isAns) {
            boolean removed = smdao.deleteSpaceMarine(spaceMarine, owner);
            if (removed) {
                spaceMarines.remove(spaceMarine);
                logger.info("SpaceMarine {} successfully removed", spaceMarine);
            } else {
                logger.info("SpaceMarine {} isn't removed. Something went wrong", spaceMarine);
            }
            return removed;
        } return false;
    }
    @Override
    public int size(){
        return spaceMarines.size();
    }
    @Override
    public double getSumOfHealth() {
        double sum = 0.0;
        for (SpaceMarine marine : spaceMarines) {
            sum += marine.getHealth() != null ? marine.getHealth() : 0.0;
        }
        return sum;
    }
    @Override
    public SpaceMarine getMinByMeleeWeapon() {
        return spaceMarines.stream()
                .filter(m -> m.getMeleeWeapon() != null)
                .min((m1, m2) -> m1.getMeleeWeapon().compareTo(m2.getMeleeWeapon()))
                .orElse(null);
    }

    @Override
    public boolean isIdInCollection(long id) throws SQLException {
        return smdao.isIdInCollection(id);
    }

    @Override
    public boolean addItem(int index, SpaceMarine spaceMarine, String owner) throws SQLException {
        boolean p = smdao.insertSpaceMarine(spaceMarine, owner);
        if (p){
            spaceMarines.add(index,spaceMarine);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(long id, String ownerUsername) throws SQLException {
        String realOwner = smdao.getOwnerInfoBySpaceMarineId(id).get("name").toString();
        boolean isAns = userDAO.isAncestorOrSelf(ownerUsername, realOwner);
        if (isAns) {
            if ( smdao.deleteSpaceMarineById(id, realOwner));
            {
                reload();
                return true;
            }

        }
        return false;
    }

    @Override
    public boolean addUpdating(Long id) {
        return updatingSpaceMarines.add(id);
    }

    @Override
    public boolean removeUpdating(Long id) {
        return updatingSpaceMarines.remove(id);
    }

    @Override
    public boolean removeGreater(SpaceMarine spaceMarine, String ownerUsername) throws SQLException {
        ArrayList<SpaceMarine> haveToBeRemoved = new ArrayList<>();
        for (SpaceMarine spaceMarineR:spaceMarines){
            if (spaceMarine.compareTo(spaceMarineR)<0){
                haveToBeRemoved.add(spaceMarineR);
            }
        } if(haveToBeRemoved.isEmpty()){
            return false;}
        for (SpaceMarine spaceMarineR: haveToBeRemoved){
            remove(spaceMarineR, ownerUsername);
        } return true;
    }

    @Override
    public void clear(String ownerUsername) throws SQLException {
        smdao.clear(ownerUsername);
//        spaceMarines.removeAll(spaceMarines);
        reload();
    }

    @Override
    public void shuffle() {
        Collections.shuffle(spaceMarines);
    }

    @Override
    public List<SpaceMarine> filterLessThanMeleeWeapon(MeleeWeapon weapon) {
        return spaceMarines.stream()
                .filter(marine -> marine.getMeleeWeapon().compareTo(weapon) < 0)
                .collect(Collectors.toList());
    }

    @Override
    public ZonedDateTime getCreationData() {
        return creationData;
    }

    @Override
    public SpaceMarine getSpaceMarineById(long id) {
        return spaceMarines.stream()
                .filter(marine -> marine.getId() == id)
                .findFirst()
                .orElse(null);
    }


    @Override
    public Set<Long> getUpdatingSpaceMarines() {
        return updatingSpaceMarines;
    }

    @Override
    public String getOwnerName(long spaceMarineId) throws SQLException {
        return smdao.getOwnerInfoBySpaceMarineId(spaceMarineId).get("name").toString();
    }
    public boolean isAncestorOrSelf(String potentialAncestor, String descendant) throws SQLException{
        return userDAO.isAncestorOrSelf(potentialAncestor, descendant);
    }
}
