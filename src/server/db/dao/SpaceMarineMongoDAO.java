package server.db.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import server.db.provider.MongoProvider;
import shared.models.*;
import shared.enums.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class SpaceMarineMongoDAO {
    private final MongoCollection<Document> collection;
    private static final AtomicLong idCounter = new AtomicLong(System.currentTimeMillis());

    public SpaceMarineMongoDAO(MongoProvider provider) {
        this.collection = provider.getDb().getCollection("space_marines");
    }

    public List<SpaceMarine> findAll() {
        List<SpaceMarine> result = new ArrayList<>();
        for (Document doc : collection.find()) {
            result.add(fromDocument(doc));
        }
        return result;
    }

    public boolean insert(SpaceMarine marine, String owner) {
        if (marine.getId() <= 0) {
            marine.setId(idCounter.incrementAndGet());
        }
        collection.insertOne(toDocument(marine, owner));
        return true;
    }

    public boolean update(long id, SpaceMarine marine, String owner) {
        UpdateResult result = collection.replaceOne(
                Filters.and(Filters.eq("id", id), Filters.eq("owner", owner)),
                toDocument(marine, owner)
        );
        return result.getModifiedCount() > 0;
    }

    public boolean deleteById(long id, String owner) {
        DeleteResult result = collection.deleteOne(
                Filters.and(Filters.eq("id", id), Filters.eq("owner", owner))
        );
        return result.getDeletedCount() > 0;
    }

    public void clearByOwner(String owner) {
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
        Object ownerObj = doc.get("owner");
        if (ownerObj != null) {
            try { spaceMarine.setOwner(Long.parseLong(ownerObj.toString())); }
            catch (NumberFormatException ignored) { spaceMarine.setOwner(0L); }
        }

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
}