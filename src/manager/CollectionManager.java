package manager;
import inputWorkers.Validator;
import inputWorkers.XMLParser;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Collections;

public class CollectionManager {
    private ArrayList<SpaceMarine> spaceMarines;
    private Data creationData;
    private long nextId =1;
    private Validator validator = new Validator(this);
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

    public Data getCreationData() {
        return creationData;
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
}
