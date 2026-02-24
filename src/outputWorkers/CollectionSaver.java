package outputWorkers;

import manager.CollectionManager;
import javax.xml.bind.*;
import java.io.File;
/**
 * Saves CollectionManager state to XML file using JAXB marshalling.
 * Handles serialization with formatted output for readability.
 */
public class CollectionSaver {
    /**
     * Serializes collection to XML file at specified path.
     *
     * <p>Uses JAXB to marshal the CollectionManager object tree.
     * Output is formatted with indentation for human readability.
     *
     * @param collectionManager the manager instance containing data to save
     * @param filePath destination path for XML file (will be overwritten if exists)
     * @throws Exception if JAXB context creation, marshaller setup, or file I/O fails
     * @implNote Requires {@code @XmlRootElement} on CollectionManager and nested classes
     */
    public void save(CollectionManager collectionManager, String filePath) throws Exception {
        JAXBContext context = JAXBContext.newInstance(CollectionManager.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(collectionManager, new File(filePath));
        System.out.println("Collection saved to " + filePath);
    }
}