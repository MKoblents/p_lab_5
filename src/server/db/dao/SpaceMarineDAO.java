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
        String sqlMarineInsert = "INSERT INTO collection (name, creation_date, health, astartes_category, weapon, melee_weapon, coordinates_id, chapter_id, owner_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, (SELECT id FROM users WHERE name = ?))";
        try (Connection connection = provider.getConnection()){
            return sendIURequest(connection,spaceMarine, (Map<String, Object>) (new HashMap<>()).put("name", ownerName), sqlMarineInsert, RequestType.INSERTION);
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
                } else {
                    try (PreparedStatement psInsert = connection.prepareStatement(sqlCoordsInsert)) {
                        psInsert.setLong(1, coordinates.getX());
                        psInsert.setLong(2, coordinates.getY());
                        psInsert.executeUpdate();
                        try (ResultSet keys = psInsert.getGeneratedKeys()) {
                            if (keys.next()) {
                                coordId = keys.getLong(1);
                            } else throw new SQLException("Failed to retrieve chapter ID");
                        }
                    }
                }
            }
        } return coordId;
    }
    private long chapterInsertion(Connection connection, Chapter chapter) throws SQLException{
        String sqlChapterInsert = "INSERT INTO Chapters (name, parent_legion, world) VALUES (?, ?, ?)";
        long chapterId;
        try (PreparedStatement ps = connection.prepareStatement(sqlChapterInsert)){
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
    private boolean sendIURequest(Connection connection, SpaceMarine spaceMarine, Map<String, Object> ownerInfo, String sql, RequestType type) throws SQLException {
        connection.setAutoCommit(false);
        connection.setAutoCommit(false);
        long coordId = coordinatesCheck(connection, spaceMarine.getCoordinates());
        Chapter chapter = spaceMarine.getChapter();
        Long chapterId = null;
        if (chapter != null){
            chapterId = chapterInsertion(connection, chapter);
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setSpaceMarineParametrs(spaceMarine,coordId,chapterId,preparedStatement);
            if (type.equals(RequestType.INSERTION)){
                preparedStatement.setString(9, (String) ownerInfo.get("name"));
            } else if (type.equals(RequestType.UPDATE)) {
                preparedStatement.setLong(9, (Long) ownerInfo.get("id"));
            }
            preparedStatement.executeUpdate();
            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()){
                    spaceMarine.setId(keys.getLong(1));
                    connection.commit();
                    return true;
                }
            }
        } catch (SQLException e) {
            connection.rollback();
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
    private Map<String, Object> getOwnerInfoBySpaceMarineId(long spaceMarineId) throws SQLException{
        String sql = "SELECT u.id,u.name FROM Space_marines s join Users u on s.owner = u.id WHERE s.id = ?";
        try (Connection connection = provider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, spaceMarineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()){
                    Map<String, Object> mapa = new HashMap<>();
                    mapa.put("id", rs.getLong("id"));
                    mapa.put("name", rs.getString("owner"));
                    return mapa;
                }
            }
        }
        return null;
    }

    public boolean updateSpaceMarine(long id, SpaceMarine spaceMarine, String owner) throws SQLException {
        Map<String, Object> realOwner = getOwnerInfoBySpaceMarineId(id);
        if (realOwner!= null && !realOwner.isEmpty()){
            if (realOwner.get("name").equals(owner)){
                Long ownerId = getOwnerId(id);
                String sql = "update  collection  set name = ?," +
                        " creation_date = ?," +
                        " health = ?," +
                        " astartes_category = ?," +
                        " weapon = ?," +
                        " melee_weapon = ?," +
                        " coordinates_id = ?," +
                        " chapter_id = ?," +
                        " owner_id = ? ";
                try (Connection connection = provider.getConnection()){
                    return sendIURequest(connection, spaceMarine, realOwner,sql,RequestType.UPDATE);
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
        String sql = "DELETE FROM Space_marines WHERE id = ? AND owner = (SELECT id FROM users WHERE name = ?)";
        try (Connection connection = provider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setLong(1,id);
            ps.setString(2, owner);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()){
                if (keys.next()){
                    return true;
                }
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
                " coordinates_id = ? and" +
                " chapter_id = ? ";
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
        chapter.setName(resultSet.getString("ch_name"));
        chapter.setParentLegion(resultSet.getString("parent_legion"));
        chapter.setWorld(resultSet.getString("world"));
        spaceMarine.setChapter(chapter);
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
        preparedStatement.setLong(8, chapterId);
    }

    enum RequestType {
        INSERTION,
        UPDATE;
    }
}
