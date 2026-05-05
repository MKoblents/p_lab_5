package server.db.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import server.db.provider.MongoProvider;
import shared.models.*;
import shared.enums.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SpaceMarineMongoDAO implements SMDAO {
    private final MongoCollection<Document> collection;
    private static final AtomicLong idCounter = new AtomicLong(System.currentTimeMillis());

    public SpaceMarineMongoDAO(MongoProvider provider) {
        this.collection = provider.getDb().getCollection("space_marines");
    }

    @Override
    public boolean isAncestorOrSelf(String potentialAncestor, String descendant) throws SQLException {
        return false;
    }

    @Override
    public boolean deleteSpaceMarine(SpaceMarine spaceMarine, String owner) throws SQLException {
        return false;
    }

    @Override
    public List<SpaceMarine> selectAll() {
        List<SpaceMarine> result = new ArrayList<>();
        for (Document doc : collection.find()) {
            result.add(fromDocument(doc));
        }
        return result;
    }

    @Override
    public boolean insertSpaceMarine(SpaceMarine marine, String owner) {
        if (marine.getId() <= 0) {
            marine.setId(idCounter.incrementAndGet());
        }
        collection.insertOne(toDocument(marine, owner));
        marine.setOwner(owner);
        return true;
    }
    @Override
    public boolean updateSpaceMarine(long id, SpaceMarine marine, String owner) {
        UpdateResult result = collection.replaceOne(
                Filters.and(Filters.eq("id", id), Filters.eq("owner", owner)),
                toDocument(marine, owner)
        );
        return result.getModifiedCount() > 0;
    }

    public boolean deleteSpaceMarineById(long id, String owner) {
        DeleteResult result = collection.deleteOne(
                Filters.and(Filters.eq("id", id), Filters.eq("owner", owner))
        );
        return result.getDeletedCount() > 0;
    }

    @Override
    public void clear(String owner) {
        collection.deleteMany(Filters.eq("owner", owner));
    }

    public boolean exists(long id) {
        return collection.countDocuments(Filters.eq("id", id)) > 0;
    }


    private Document toDocument(SpaceMarine m, String owner) {
        Document doc = new Document();
        doc.put("id", m.getId());
        doc.put("name", m.getName());
        doc.put("health", m.getHealth());
        doc.put("owner", owner);
        if (m.getCreationDate() != null) {
            doc.put("creation_date", Date.from(m.getCreationDate().toInstant()));
        }
        if (m.getMeleeWeapon() != null) doc.put("melee_weapon", m.getMeleeWeapon().name());
        if (m.getWeaponType() != null) doc.put("weapon_type", m.getWeaponType().name());
        if (m.getCategory() != null) doc.put("category", m.getCategory().name());

        if (m.getCoordinates() != null) {
            Document coords = new Document();
            coords.put("x", m.getCoordinates().getX());
            coords.put("y", m.getCoordinates().getY());
            doc.put("coordinates", coords);
        }

        if (m.getChapter() != null) {
            Document ch = new Document();
            ch.put("name", m.getChapter().getName());
            ch.put("parent_legion", m.getChapter().getParentLegion());
            ch.put("world", m.getChapter().getWorld());
            doc.put("chapter", ch);
        }
        return doc;
    }

    private SpaceMarine fromDocument(Document doc) {
        SpaceMarine spaceMarine = new SpaceMarine();
        spaceMarine.setId(doc.getLong("id"));
        spaceMarine.setName(doc.getString("name"));
        spaceMarine.setHealth(doc.getDouble("health"));

        Date date = doc.getDate("creation_date");
        if (date != null) spaceMarine.setCreationDate(date.toInstant().atZone(java.time.ZoneId.systemDefault()));

        String mw = doc.getString("melee_weapon");
        if (mw != null) spaceMarine.setMeleeWeapon(MeleeWeapon.valueOf(mw));
        String wt = doc.getString("weapon_type");
        if (wt != null) spaceMarine.setWeaponType(Weapon.valueOf(wt));
        String cat = doc.getString("category");
        if (cat != null) spaceMarine.setCategory(AstartesCategory.valueOf(cat));
        spaceMarine.setOwner(doc.getString("owner"));

        Document coords = (Document) doc.get("coordinates");
        if (coords != null) {
            Coordinates c = new Coordinates();
            c.setX(coords.getLong("x"));
            c.setY(coords.getLong("y"));
            spaceMarine.setCoordinates(c);
        }

        Document ch = (Document) doc.get("chapter");
        if (ch != null) {
            Chapter chapter = new Chapter();
            chapter.setName(ch.getString("name"));
            chapter.setParentLegion(ch.getString("parent_legion"));
            chapter.setWorld(ch.getString("world"));
            spaceMarine.setChapter(chapter);
        }
        return spaceMarine;
    }

    @Override
    public boolean isIdInCollection(long id) throws SQLException {
        return collection.countDocuments(Filters.eq("id", id)) > 0;
    }

    @Override
    public Map<String, Object> getOwnerInfoBySpaceMarineId(long spaceMarineId) throws SQLException {
        return Map.of();
    }
    @Override
    public long getSpaceMarineId(SpaceMarine spaceMarine) throws SQLException {
        List<Bson> filters = new ArrayList<>();
        if (spaceMarine.getName() != null) filters.add(Filters.eq("name", spaceMarine.getName()));
        if (spaceMarine.getHealth() != null) filters.add(Filters.eq("health", spaceMarine.getHealth()));
        if (spaceMarine.getCategory() != null) filters.add(Filters.eq("category", spaceMarine.getCategory().name()));
        if (spaceMarine.getWeaponType() != null) filters.add(Filters.eq("weapon_type", spaceMarine.getWeaponType().name()));
        if (spaceMarine.getMeleeWeapon() != null) filters.add(Filters.eq("melee_weapon", spaceMarine.getMeleeWeapon().name()));
        if (spaceMarine.getCreationDate() != null) {
            filters.add(Filters.eq("creation_date", Date.from(spaceMarine.getCreationDate().toInstant())));
        }
        if (spaceMarine.getCoordinates() != null) {
            filters.add(Filters.eq("coordinates.x", spaceMarine.getCoordinates().getX()));
            filters.add(Filters.eq("coordinates.y", spaceMarine.getCoordinates().getY()));
        }
        if (spaceMarine.getChapter() != null) {
            filters.add(Filters.eq("chapter.name", spaceMarine.getChapter().getName()));
            filters.add(Filters.eq("chapter.parent_legion", spaceMarine.getChapter().getParentLegion()));
            filters.add(Filters.eq("chapter.world", spaceMarine.getChapter().getWorld()));
        }

        if (filters.isEmpty()) return 0;
        Document found = collection.find(Filters.and(filters)).first();
        return found != null ? found.getLong("id") : 0;
    }
}