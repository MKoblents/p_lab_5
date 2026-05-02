package server.db.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.db.provider.DbProvider;
import shared.enums.AstartesCategory;
import shared.enums.MeleeWeapon;
import shared.enums.Weapon;
import shared.models.Chapter;
import shared.models.Coordinates;
import shared.models.SpaceMarine;

import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpaceMarineDAO {
    private static final Logger logger = LoggerFactory.getLogger(SpaceMarineDAO.class);
    private final DbProvider provider;
    public SpaceMarineDAO(DbProvider provider){
        this.provider = provider;
    }
    public List<SpaceMarine> selectAll() throws SQLException {
        List<SpaceMarine> res = new ArrayList<>();
        String sql = "select * from Space_marines s left join Coordinates coor on s.coordinates = coor.id " +
                "left join Chapters ch on s.chapter = ch.id;";
        try (Connection connection = provider.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()){
                while (resultSet.next()){
                    res.add(mapRow(resultSet));
                }
        }
        return res;
    }
    public boolean insertSpaceMarine(SpaceMarine spaceMarine, String ownerName)throws SQLException{
        String sqlMarineInsert = "INSERT INTO space_marines (name, creation_date, health, astartes_category, weapon, melee_weapon, coordinates, chapter, owner) " +
                "VALUES (?, ?, ?, CAST(? AS astartes_category), CAST(? AS weapon), CAST(? AS melee_weapon), ?, ?, ?)";
        long ownerId =getUserId(ownerName);
        try (Connection connection = provider.getConnection()){
            return sendIURequest(connection,spaceMarine,0, ownerId, sqlMarineInsert, RequestType.INSERTION);
        }
    }

    private long getUserId(String ownerName)  throws  SQLException{
        String sql = "select  id from users where name = ?";
        try (Connection connection = provider.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, ownerName);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    return rs.getLong("id");
                } return 0;
            }

        }
    }

    private long coordinatesCheck(Connection connection, Coordinates coordinates)throws SQLException{
        String sqlCoordsCheck = "SELECT id FROM coordinates WHERE x = ? AND y = ?";
        String sqlCoordsInsert = "INSERT INTO coordinates (x, y) VALUES (?, ?)";
        long coordId;
        try (PreparedStatement psCheck = connection.prepareStatement(sqlCoordsCheck)) {
            psCheck.setLong(1, coordinates.getX());
            psCheck.setLong(2, coordinates.getY());
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) {
                    coordId = rs.getLong("id");
                    return coordId;
                }
            }
            try (PreparedStatement psInsert = connection.prepareStatement(sqlCoordsInsert, Statement.RETURN_GENERATED_KEYS)) {
                psInsert.setLong(1, coordinates.getX());
                psInsert.setLong(2, coordinates.getY());
                psInsert.executeUpdate();
                try (ResultSet keys = psInsert.getGeneratedKeys()) {
                    if (keys.next()) {
                        coordId = keys.getLong(1);
                    } else throw new SQLException("Failed to retrieve chapter ID");
                }
            }
        } return coordId;
    }
    private long chapterInsertion(Connection connection, Chapter chapter) throws SQLException{
        String sqlChapterInsert = "INSERT INTO Chapters (name, parent_legion, world) VALUES (?, ?, ?)";
        long chapterId;
        try (PreparedStatement ps = connection.prepareStatement(sqlChapterInsert, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, chapter.getName());
            ps.setString(2, chapter.getParentLegion());
            ps.setString(3, chapter.getWorld());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()){
                if (keys.next()) chapterId = keys.getLong(1);
                else throw new SQLException("Failed to retrieve chapter ID");
            }
        }
        return chapterId;

    }
    private boolean sendIURequest(Connection connection, SpaceMarine spaceMarine,long spaceMarineId, long ownerId, String sql, RequestType type) throws SQLException {
        connection.setAutoCommit(false);
        connection.setAutoCommit(false);
        long coordId = coordinatesCheck(connection, spaceMarine.getCoordinates());
        Chapter chapter = spaceMarine.getChapter();
        Long chapterId = null;
        if (chapter != null){
            chapterId = chapterInsertion(connection, chapter);
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setSpaceMarineParametrs(spaceMarine,coordId,chapterId,preparedStatement);
            if (type.equals(RequestType.INSERTION)){
                preparedStatement.setLong(9, ownerId);
            } else if (type.equals(RequestType.UPDATE)) {
                preparedStatement.setLong(9, spaceMarineId);
                preparedStatement.setLong(10, ownerId);
            }
            preparedStatement.executeUpdate();
            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()){
                    spaceMarine.setId(keys.getLong(1));
                    spaceMarine.setOwner(ownerId);
                    connection.commit();
                    return true;
                }
            }
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
        return false;
    }

    private Long getOwnerId(long spaceMarineId) throws SQLException{
        String sql = "SELECT owner FROM Space_marines WHERE id = ?";
        try (Connection connection = provider.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, spaceMarineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()){
                    return rs.getLong("owner");
                }
            }
        }
        return null;
    }
    public Map<String, Object> getOwnerInfoBySpaceMarineId(long spaceMarineId) throws SQLException{
        String sql = "SELECT u.id,u.name FROM Space_marines s join Users u on s.owner = u.id WHERE s.id = ?";
        try (Connection connection = provider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, spaceMarineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()){
                    Map<String, Object> mapa = new HashMap<>();
                    mapa.put("id", rs.getLong("id"));
                    mapa.put("name", rs.getString("name"));
                    return mapa;
                }
            }
        }
        return null;
    }

    public boolean updateSpaceMarine(long id, SpaceMarine spaceMarine, String owner) throws SQLException {
        Map<String, Object> realOwner = getOwnerInfoBySpaceMarineId(id);
        if (realOwner!= null && !realOwner.isEmpty()){
            if (isAncestorOrSelf(owner, realOwner.get("name").toString())){
                Long ownerId = getOwnerId(id);
                String sql = "update  space_marines  set name = ?," +
                        " creation_date = ?," +
                        " health = ?," +
                        " astartes_category = CAST(? AS astartes_category)," +
                        " weapon = CAST(? AS weapon)," +
                        " melee_weapon = CAST(? AS melee_weapon)," +
                        " coordinates = ?," +
                        " chapter = ?" +
                        " WHERE id = ? AND owner = ?";
                try (Connection connection = provider.getConnection()){
                    return sendIURequest(connection, spaceMarine,id, ownerId,sql,RequestType.UPDATE);
                }
            }
        }
        logger.info("{} is not the owner of {}", owner,spaceMarine);
        return false;

    }
    public boolean deleteSpaceMarine(SpaceMarine spaceMarine, String owner) throws SQLException{
        Long id = getSpaceMarineId(spaceMarine);
        return deleteSpaceMarineById(id, owner);
    }
    public boolean deleteSpaceMarineById(long id, String owner) throws SQLException{
        Map<String, Object> ro =getOwnerInfoBySpaceMarineId(id);
        String realOwner = ro.get("name").toString();
        if (isAncestorOrSelf(owner, realOwner)) {
            String sql = "DELETE FROM Space_marines WHERE id = ? AND owner = ?";
            try (Connection connection = provider.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.setLong(2, (long) ro.get("id"));
                int affected = ps.executeUpdate();
                return affected > 0;
            }
        }
        return false;

    }
    private Long getSpaceMarineId(SpaceMarine spaceMarine) throws  SQLException{
        String sql = "select id from Space_marines where name = ? and"+
                " creation_date = ? and" +
                " health = ? and" +
                " astartes_category = ? and" +
                " weapon = ? and" +
                " melee_weapon = ? and" +
                " coordinates = ? and" +
                " chapter = ? ";
        try (Connection connection = provider.getConnection()){
            long coordId = coordinatesCheck(connection, spaceMarine.getCoordinates());
            Chapter chapter = spaceMarine.getChapter();
            Long chapterId = null;
            if (chapter != null){
                chapterId = chapterInsertion(connection, chapter);
            }
            try (PreparedStatement ps = connection.prepareStatement(sql)){
                setSpaceMarineParametrs(spaceMarine,coordId,chapterId,ps);
                try (ResultSet rs = ps.executeQuery()){
                    if (rs.next()){
                        return rs.getLong("id");
                    } return null;

                }
            }

        }

    }

    private SpaceMarine mapRow(ResultSet resultSet) throws SQLException {
        SpaceMarine spaceMarine = new SpaceMarine();
        spaceMarine.setId(resultSet.getLong("id"));
        spaceMarine.setName(resultSet.getString("name"));
        Timestamp ts = resultSet.getTimestamp("creation_date");
        if (ts != null) spaceMarine.setCreationDate(ts.toLocalDateTime().atZone(ZoneId.systemDefault()));
        spaceMarine.setHealth(resultSet.getDouble("health"));
        String cat = resultSet.getString("astartes_category");
        if (cat != null) spaceMarine.setCategory(AstartesCategory.valueOf(cat));
        String weapon = resultSet.getString("weapon");
        if (weapon != null) spaceMarine.setWeaponType(Weapon.valueOf(weapon));
        String melee = resultSet.getString("melee_weapon");
        if (melee != null) spaceMarine.setMeleeWeapon(MeleeWeapon.valueOf(melee));
        Coordinates coords = new Coordinates();
        coords.setX(resultSet.getLong("x"));
        coords.setY(resultSet.getLong("y"));
        spaceMarine.setCoordinates(coords);
        Chapter chapter = new Chapter();
        chapter.setName(resultSet.getString("name"));
        chapter.setParentLegion(resultSet.getString("parent_legion"));
        chapter.setWorld(resultSet.getString("world"));
        spaceMarine.setChapter(chapter);
        spaceMarine.setOwner(resultSet.getLong("owner"));
        return spaceMarine;

    }

    public boolean isIdInCollection(long id)  throws SQLException{
        String sql = "select * from Space_marines where id = ?";
        try (Connection connection = provider.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    return true;
                } return false;
            }
        }
    }
    private void setSpaceMarineParametrs(SpaceMarine spaceMarine, long coordId, Long chapterId,PreparedStatement preparedStatement) throws SQLException{
        preparedStatement.setString(1, spaceMarine.getName());
        preparedStatement.setTimestamp(2, Timestamp.valueOf(spaceMarine.getCreationDate().toLocalDateTime()));
        preparedStatement.setDouble(3, spaceMarine.getHealth());
        preparedStatement.setString(4, spaceMarine.getCategory() != null ? spaceMarine.getCategory().name() : null);
        preparedStatement.setString(5, spaceMarine.getWeaponType() != null ? spaceMarine.getWeaponType().name() : null);
        preparedStatement.setString(6, spaceMarine.getMeleeWeapon() != null ? spaceMarine.getMeleeWeapon().name() : null);
        preparedStatement.setLong(7, coordId);
        preparedStatement.setObject(8, chapterId);
    }

    public void clear(String ownerUsername) throws SQLException{
        String sql = "delete from space_marines where owner = (select id from users where name = ?)";
        try (Connection connection = provider.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, ownerUsername);
            ps.executeUpdate();
        }
    }

    private String getParentName(String username) throws SQLException {
        String sql = "SELECT p.name FROM Users u JOIN Users p ON u.parent = p.id WHERE u.name = ?";
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("name") : null;
            }
        }
    }

    /**
     * Checks if potentialAncestor is an ancestor (parent, grandparent, etc.)
     * or the same user as descendant.
     */
    public boolean isAncestorOrSelf(String potentialAncestor, String descendant) throws SQLException {
        if (potentialAncestor == null || descendant == null) return false;
        if (potentialAncestor.equals(descendant)) return true;

        String current = descendant;
        while (current != null) {
            current = getParentName(current);
            if (potentialAncestor.equals(current)) return true;
        }
        return false;
    }

    enum RequestType {
        INSERTION,
        UPDATE;
    }
}
