package server.outputWorkers;

import client.network.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.manager.CollectionManager;
import shared.models.SpaceMarine;

import javax.xml.bind.*;
import java.io.File;
import java.util.List;

/**
 * Saves CollectionManager state to XML file using JAXB marshalling.
 * Handles serialization with formatted output for readability.
 */
public class CollectionSaver {
    private static final Logger logger = LoggerFactory.getLogger(CollectionSaver.class);
    /**
     * Serializes collection to XML file at specified path.
     *
     * <p>Uses JAXB to marshal the CollectionManager object tree.
     * Output is formatted with indentation for human readability.
     *
     * @param filePath destination path for XML file (will be overwritten if exists)
     * @throws Exception if JAXB context creation, marshaller setup, or file I/O fails
     * @implNote Requires {@code @XmlRootElement} on CollectionManager and nested classes
     */
    public void save(List<SpaceMarine> spaceMarines, String filePath) throws Exception {
        try {
            if (filePath == null){
                filePath = "new.xml";
            }
            JAXBContext context = JAXBContext.newInstance(CollectionManager.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(spaceMarines, new File(filePath));
            System.out.println("Collection saved to " + filePath);
            logger.info("Collection saved successfully: {} elements",spaceMarines.size());
        } catch (Exception e) {
            logger.error("Failed to save collection to {}: {}", filePath, e.getMessage(), e);
            throw e;
        }
        }

}