package server.manager;
import shared.enums.MeleeWeapon;
import client.utils.Validator;
import shared.models.SpaceMarine;
import shared.utils.XMLParser;
import javax.xml.bind.annotation.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
/**
 * Manages a collection of SpaceMarine entities.
 * Handles CRUD operations, sorting, filtering, and XML persistence.
 */
@XmlRootElement(name = "spaceMarines")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionManager {
    private ArrayList<Long>  updatingSpaceMarines = new ArrayList<>();

    /** Collection of SpaceMarine objects. */
    @XmlElement(name = "spaceMarine")
    private ArrayList<SpaceMarine> spaceMarines;
    /** Timestamp when the collection was created. */
    @XmlTransient
    private ZonedDateTime creationData;
    /** Counter for auto-generating unique IDs. */
    @XmlTransient
    private Validator validator = new Validator();
    /**
     * Initializes empty collection with current timestamp.
     */
    public CollectionManager(){
        this.creationData = ZonedDateTime.now();
        spaceMarines = new ArrayList<>();
    }
    /**
     * Finds a SpaceMarine by its unique ID.
     * @param id the ID to search for
     * @return matching SpaceMarine, or null if not found
     */
    public SpaceMarine getSpaceMarineById(long id) {
        return spaceMarines.stream()
                .filter(marine -> marine.getId() == id)
                .findFirst()
                .orElse(null);
    }
    /**
     * Adds a SpaceMarine to the end of the collection.
     * @param spaceMarine the element to add
     * @return true if added successfully
     */
    public boolean addItem(SpaceMarine spaceMarine){
        return this.spaceMarines.add(spaceMarine);
    }
    /**
     * Inserts a SpaceMarine at the specified position.
     * @param index insertion position
     * @param spaceMarine the element to insert
     */
    public void addItem(int index, SpaceMarine spaceMarine){
        if (index>spaceMarines.size()){
            addItem(spaceMarine);
            System.err.println("Your index was out of range, so element added to the end of collection.");
            return;
        }
        this.spaceMarines.add(index, spaceMarine);
    }
    /**
     * Loads collection from XML file with validation.
     * @param filePath path to the XML file
     */
    public void loadFromFile(String filePath){
        try {
            XMLParser parser = new XMLParser(filePath);
            this.spaceMarines = parser.parseSpaceMarines();
            validator.spaceMarinesValidate(this.spaceMarines);
        }catch (Exception e){
            System.err.println("Can't read from file: "+filePath);
        }
    }
    /**
     * Sorts the collection by natural order (health).
     * @see SpaceMarine#compareTo(SpaceMarine)
     */
    public void sort(){
        Collections.sort(spaceMarines);
    }
    /**
     * Returns the collection (note: mutable reference).
     * @return list of SpaceMarine objects
     */
    public ArrayList<SpaceMarine> getSpaceMarines() {
        return spaceMarines;
    }
    /**
     * Gets the collection creation timestamp.
     * @return creation date
     */
    public ZonedDateTime getCreationData() {
        return creationData;
    }
    /**
     * Removes SpaceMarine by ID.
     * @param id the ID to remove
     */
    public void remove(long id){
        spaceMarines.removeIf(spaceMarine -> spaceMarine.getId() == id);
    }
    /**
     * Filters marines with melee weapon less than specified.
     * @param weapon the comparison threshold
     * @return list of matching SpaceMarines
     */
    public List<SpaceMarine> filterLessThanMeleeWeapon(MeleeWeapon weapon) {
        return spaceMarines.stream()
                .filter(marine -> marine.getMeleeWeapon().compareTo(weapon) < 0)
                .collect(Collectors.toList());
    }
    /**
     * Removes specific SpaceMarine instance from collection.
     * @param spaceMarine the element to remove
     */
    public void remove(SpaceMarine spaceMarine){
    spaceMarines.remove(spaceMarine);
    }
    /**
     * Clears all elements from the collection.
     */
    public void clear(){
        spaceMarines.clear();
    }
    /**
     * Randomly shuffles the collection order.
     */
    public void shuffle(){
        Collections.shuffle(spaceMarines);
    }
    /**
     * Calculates sum of all health values (null treated as 0).
     * @return total health sum
     */
    public double getSumOfHealth() {
        return spaceMarines.stream()
                .mapToDouble(SpaceMarine::getHealth)
                .sum();
    }
    /**
     * Finds SpaceMarine with minimum melee weapon value.
     * @return marine with min weapon, or null if collection empty
     */
    public SpaceMarine getMinByMeleeWeapon() {
        if (spaceMarines.isEmpty()) {
            return null;
        }
        return spaceMarines.stream()
                .min(Comparator.comparing(SpaceMarine::getMeleeWeapon))
                .orElse(null);
    }
    /**
     * Replaces old element with new one at same position.
     * @param spaceMarineOld element to replace
     * @param spaceMarineNew replacement element
     */
    public void replace(SpaceMarine spaceMarineOld, SpaceMarine spaceMarineNew){
        int index = spaceMarines.indexOf(spaceMarineOld);
        if (index==-1){
            return;
        }
        spaceMarines.set(index, spaceMarineNew);
    }
    /**
     * Removes all elements greater than specified (by health comparison).
     * @param spaceMarine the threshold element
     */
    public void removeGreater(SpaceMarine spaceMarine) {
        ArrayList<SpaceMarine> haveToBeRemoved = new ArrayList<>();
        for (SpaceMarine spaceMarineR:spaceMarines){
            if (spaceMarine.compareTo(spaceMarineR)<0){
                haveToBeRemoved.add(spaceMarineR);
            }
        }for (SpaceMarine spaceMarineR: haveToBeRemoved){
            spaceMarines.remove(spaceMarineR);
        }
    }
    /**
     * Returns current collection size.
     * @return number of elements
     */
    public int size() {
        return spaceMarines.size();
    }
    public boolean isIdInCollection(long id){
        for (SpaceMarine spaceMarine: spaceMarines){
            if (spaceMarine.getId() == id){
                return true;
            }
        }return false;
    }

    public void update(Long id, SpaceMarine spaceMarineInput) {
        spaceMarineInput.setId(id);
        SpaceMarine spaceMarine = getSpaceMarineById(id);
        replace(spaceMarine, spaceMarineInput);
//        TODO normal update
    }
    public boolean addUpdating(Long id){
        return updatingSpaceMarines.add(id);
    }
    public boolean removeUpdating(Long id){
        return updatingSpaceMarines.remove(id);
    }

    public ArrayList<Long> getUpdatingSpaceMarines() {
        return updatingSpaceMarines;
    }
}
