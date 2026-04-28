package server.db.dao;

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
import java.util.List;

public class SpaceMarineDAO {
    private final DbProvider provider;
    public SpaceMarineDAO(DbProvider provider){
        this.provider = provider;
    }
    public List<SpaceMarine> selectAll() throws SQLException {
        List<SpaceMarine> res = new ArrayList<>();
        String sql = "select * from Space_marines s left join Coordinates coor on s.coordinates = coor.id" +
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
            connection.setAutoCommit(false);
            connection.setAutoCommit(false);
            long coordId = coordinatesCheck(connection, spaceMarine.getCoordinates());
            Chapter chapter = spaceMarine.getChapter();
            Long chapterId = null;
            if (chapter != null){
                chapterId = chapterInsertion(connection, chapter);
            }
             try (PreparedStatement preparedStatement = connection.prepareStatement(sqlMarineInsert)) {
                 preparedStatement.setString(1, spaceMarine.getName());
                 preparedStatement.setTimestamp(2, Timestamp.valueOf(spaceMarine.getCreationDate().toLocalDateTime()));
                 preparedStatement.setDouble(3, spaceMarine.getHealth());
                 preparedStatement.setString(4, spaceMarine.getCategory() != null ? spaceMarine.getCategory().name() : null);
                 preparedStatement.setString(5, spaceMarine.getWeaponType() != null ? spaceMarine.getWeaponType().name() : null);
                 preparedStatement.setString(6, spaceMarine.getMeleeWeapon() != null ? spaceMarine.getMeleeWeapon().name() : null);
                 preparedStatement.setLong(7, coordId);
                 preparedStatement.setLong(8, chapterId);
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
                 return false;
             } finally {
                 connection.setAutoCommit(true);
             }
        }
      return false;
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
                        try (ResultSet keys = psCheck.getGeneratedKeys()) {
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
}
