package manager;
import javax.xml.crypto.Data;
import java.util.ArrayList;

public class CollectionManager {
    private ArrayList<SpaceMarine> spaceMarines;
    private Data creationData;
    public boolean addItem(SpaceMarine spaceMarine){
        return this.spaceMarines.add(spaceMarine);
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
}
