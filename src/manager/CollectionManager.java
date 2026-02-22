package manager;
import inputWorkers.Validator;
import inputWorkers.XMLParser;

import javax.xml.crypto.Data;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CollectionManager {
    private ArrayList<SpaceMarine> spaceMarines;
    private ZonedDateTime creationData;
    private long nextId =1;
    private Validator validator = new Validator(this);
    public CollectionManager(){
        this.creationData = ZonedDateTime.now();
    }
    public boolean addItem(SpaceMarine spaceMarine){
        spaceMarine.setId(generateId());
        return this.spaceMarines.add(spaceMarine);
    }
    public void loadFromFile(String filePath) throws Exception {
        XMLParser parser = new XMLParser(filePath);
        this.spaceMarines = parser.parseSpaceMarines();
        validator.spaceMarinesValidate(this.spaceMarines);
    }
    public void sort(){
        Collections.sort(spaceMarines);
    }

    public ArrayList<SpaceMarine> getSpaceMarines() {
        return spaceMarines;
    }

    public ZonedDateTime getCreationData() {
        return creationData;
    }
    public void remove(long id){
        spaceMarines.removeIf(spaceMarine -> spaceMarine.getId() == id);
    }
    public void remove(SpaceMarine spaceMarine){
    spaceMarines.remove(spaceMarine);
    }
    public void clear(){
        spaceMarines.clear();
    }
    public long getCorrectId(SpaceMarine spaceMarine){
        return (long) spaceMarines.indexOf(spaceMarine)+1;
    }
    public long generateId() {
        return nextId++;
    }
    public void shuffle(){
        Collections.shuffle(spaceMarines);
    }
    public double getSumOfHealth() {
        return spaceMarines.stream()
                .mapToDouble(SpaceMarine::getHealth)
                .sum();
    }
    public SpaceMarine getMinByMeleeWeapon() {
        if (spaceMarines.isEmpty()) {
            return null;
        }
        return spaceMarines.stream()
                .min(Comparator.comparing(SpaceMarine::getMeleeWeapon))
                .orElse(null);
    }
}
