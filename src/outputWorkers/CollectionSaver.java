package outputWorkers;

import manager.CollectionManager;
import javax.xml.bind.*;
import java.io.File;

public class CollectionSaver {
    public void save(CollectionManager collectionManager, String filePath) throws Exception {
        JAXBContext context = JAXBContext.newInstance(CollectionManager.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(collectionManager, new File(filePath));
        System.out.println("Collection saved to " + filePath);
    }
}