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
public class CollectionManager  implements CollectionService{
    @XmlTransient
    private Set<Long>  updatingSpaceMarines = new HashSet<>();

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
    @Override
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
    @Override
    public boolean addItem(SpaceMarine spaceMarine, String owner){
        return this.spaceMarines.add(spaceMarine);
    }
    /**
     * Inserts a SpaceMarine at the specified position.
     * @param index insertion position
     * @param spaceMarine the element to insert
     */
    @Override
    public boolean addItem(int index, SpaceMarine spaceMarine, String owner){
        if (index>spaceMarines.size()){
            System.err.println("Your index was out of range, so element added to the end of collection.");
            return  addItem(spaceMarine, owner);
        }
        this.spaceMarines.add(index, spaceMarine);
        return true;
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
    @Override
    public ZonedDateTime getCreationData() {
        return creationData;
    }
    /**
     * Removes SpaceMarine by ID.
     * @param id the ID to remove
     */
    @Override
    public boolean remove(long id, String owner){
        return spaceMarines.removeIf(spaceMarine -> spaceMarine.getId() == id);
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
    @Override
    public boolean remove(SpaceMarine spaceMarine, String owner){
    return spaceMarines.remove(spaceMarine);
    }
    /**
     * Clears all elements from the collection.
     */
    @Override
    public void clear(String owner){
        spaceMarines.clear();
    }
    /**
     * Randomly shuffles the collection order.
     */
    @Override
    public void shuffle(){
        Collections.shuffle(spaceMarines);
    }
    /**
     * Calculates sum of all health values (null treated as 0).
     * @return total health sum
     */
    @Override
    public double getSumOfHealth() {
        return spaceMarines.stream()
                .mapToDouble(SpaceMarine::getHealth)
                .sum();
    }
    /**
     * Finds SpaceMarine with minimum melee weapon value.
     * @return marine with min weapon, or null if collection empty
     */
    @Override
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
    private boolean replace(SpaceMarine spaceMarineOld, SpaceMarine spaceMarineNew){
        int index = spaceMarines.indexOf(spaceMarineOld);
        if (index==-1){
            return false;
        }
        spaceMarines.set(index, spaceMarineNew);
        return true;
    }
    /**
     * Removes all elements greater than specified (by health comparison).
     * @param spaceMarine the threshold element
     */
    @Override
    public boolean removeGreater(SpaceMarine spaceMarine, String owner) {
        ArrayList<SpaceMarine> haveToBeRemoved = new ArrayList<>();
        for (SpaceMarine spaceMarineR:spaceMarines){
            if (spaceMarine.compareTo(spaceMarineR)<0){
                haveToBeRemoved.add(spaceMarineR);
            }
        } if(haveToBeRemoved.isEmpty()){
            return false;}
        for (SpaceMarine spaceMarineR: haveToBeRemoved){
                spaceMarines.remove(spaceMarineR);
        } return true;
    }
    /**
     * Returns current collection size.
     * @return number of elements
     */
    @Override
    public int size() {
        return spaceMarines.size();
    }
    @Override
    public boolean isIdInCollection(long id){
        for (SpaceMarine spaceMarine: spaceMarines){
            if (spaceMarine.getId() == id){
                return true;
            }
        }return false;
    }
    @Override
    public boolean update(long id, SpaceMarine spaceMarineInput, String owner) {
        spaceMarineInput.setId(id);
        SpaceMarine spaceMarine = getSpaceMarineById(id);
        return replace(spaceMarine, spaceMarineInput);
//        TODO normal update
    }
    @Override
    public boolean addUpdating(Long id){
        return updatingSpaceMarines.add(id);
    }
    @Override
    public boolean removeUpdating(Long id){
        return updatingSpaceMarines.remove(id);
    }
    @Override
    public Set<Long> getUpdatingSpaceMarines() {
        return updatingSpaceMarines;
    }

    @Override
    public List<SpaceMarine> getAll() {
        return spaceMarines;
    }
}
