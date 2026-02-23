package manager;
import enums.MeleeWeapon;
import inputWorkers.Validator;
import inputWorkers.XMLParser;

import javax.xml.bind.annotation.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement(name = "spaceMarines")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionManager {
    @XmlElement(name = "spaceMarine")
    private ArrayList<SpaceMarine> spaceMarines;
    @XmlTransient
    private ZonedDateTime creationData;
    @XmlTransient
    private long nextId =1;
    @XmlTransient
    private Validator validator = new Validator(this);
    public CollectionManager(){
        this.creationData = ZonedDateTime.now();
    }
    public SpaceMarine getSpaceMarineById(long id) {
        return spaceMarines.stream()
                .filter(marine -> marine.getId() == id)
                .findFirst()
                .orElse(null);
    }
    public SpaceMarine getNewSpaceMarine(){
       SpaceMarine spaceMarine = new SpaceMarine();
       spaceMarine.setId(generateId());
       return spaceMarine;
    }
    public SpaceMarine getNewSpaceMarine(String name, Coordinates coordinates, MeleeWeapon meleeWeapon){
        SpaceMarine spaceMarine = new SpaceMarine(name, coordinates, meleeWeapon);
        spaceMarine.setId(generateId());
        return spaceMarine;
    }
    public boolean addItem(SpaceMarine spaceMarine){
        return this.spaceMarines.add(spaceMarine);
    }
    public void addItem(int index, SpaceMarine spaceMarine){
        this.spaceMarines.add(index, spaceMarine);
    }
    public void loadFromFile(String filePath) throws Exception {
        XMLParser parser = new XMLParser(filePath, this);
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
    public List<SpaceMarine> filterLessThanMeleeWeapon(MeleeWeapon weapon) {
        return spaceMarines.stream()
                .filter(marine -> marine.getMeleeWeapon().compareTo(weapon) < 0)
                .collect(Collectors.toList());
    }
    public void remove(SpaceMarine spaceMarine){
    spaceMarines.remove(spaceMarine);
    }
    public void clear(){
        spaceMarines.clear();
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
    public void replace(SpaceMarine spaceMarineOld, SpaceMarine spaceMarineNew){
        int index = spaceMarines.indexOf(spaceMarineOld);
        spaceMarines.set(index, spaceMarineNew);
    }

    public void removeGreater(SpaceMarine spaceMarine) {
        ArrayList<SpaceMarine> haveToBeRemoved = new ArrayList<>();
        for (SpaceMarine spaceMarineR:spaceMarines){
            if (spaceMarine.compareTo(spaceMarineR)>0){
                haveToBeRemoved.add(spaceMarineR);
            }
        }for (SpaceMarine spaceMarineR: haveToBeRemoved){
            spaceMarines.remove(spaceMarineR);
        }
    }

    public int size() {
        return spaceMarines.size();
    }
}
