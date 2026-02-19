package inputWorkers;

import enums.MeleeWeapon;
import manager.SpaceMarine;
import manager.Coordinates;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class XMLParser {
    private XMLInputFactory factory;
    private XMLStreamReader xmlReader;
    public XMLParser(String filePath) throws FileNotFoundException, XMLStreamException {
        FileInputStream fis = new FileInputStream(filePath);
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(isr);
        this.factory = XMLInputFactory.newInstance();
        this.xmlReader = factory.createXMLStreamReader(reader);
    }

    public List<SpaceMarine> parseSpaceMarines(String filePath) throws Exception {
        ArrayList<SpaceMarine> marines = new ArrayList<>();
        SpaceMarine currentMarine = null;
        String currentTag = "";
        while (xmlReader.hasNext()) {
            xmlReader.next();
            if (xmlReader.isStartElement()) {
                currentTag = xmlReader.getLocalName();
                if ("spacemarine".equalsIgnoreCase(currentTag)) {
                    SpaceMarine spaceMarine = parseSpaseMarine();
                }
                // Подсказка для логики: Как вы обрабатываете вложенные поля, такие как coordinates?
            } else if (xmlReader.isEndElement()) {
                // Подсказка для логики: Когда вы добавляете объект в список?
                if ("spaceMarine".equals(xmlReader.getLocalName()) && currentMarine != null) {
                    marines.add(currentMarine);
                    currentMarine = null;
                }
            } else if (xmlReader.isCharacters()) {
                // Подсказка для логики: Как вы присваиваете текст правильному полю currentMarine?
            }
        }
        xmlReader.close();
        return marines;
    }

    public SpaceMarine parseSpaseMarine() throws XMLStreamException {
        String currentFieldName;
        SpaceMarine spaceMarine = new SpaceMarine();
        while (xmlReader.hasNext()) {
            xmlReader.next();
            if (xmlReader.isEndElement() && "spaceMarine".equalsIgnoreCase(xmlReader.getLocalName())) {
                break;
            }
            if (xmlReader.isStartElement()) {
                currentFieldName = xmlReader.getLocalName();
                if ("coordinates".equalsIgnoreCase(currentFieldName)) {
                    spaceMarine.setCoordinates(parseCoordinates());
                } else if ("meleeWeapon".equalsIgnoreCase(currentFieldName)) {
                    spaceMarine.setMeleeWeapon(parseMeleeWeapon());
                } else if ("name".equalsIgnoreCase(currentFieldName)) {
                    spaceMarine.setName(parseName());
                }else if ("health".equalsIgnoreCase(currentFieldName)){
                    spaceMarine.setHealth(parseHealth());
                } else if ("AstartesCategory".equalsIgnoreCase(currentFieldName)) {

                }
            }
        }
        return spaceMarine;
    }

    public Double parseHealth() {
        return Double.parseDouble(xmlReader.getText());
    }

    public String parseName() {
        return xmlReader.getText();
    }

    public MeleeWeapon parseMeleeWeapon() throws XMLStreamException {
        while (xmlReader.hasNext()) {
            xmlReader.next();
            if (xmlReader.isEndElement() && "meleeWeapon".equalsIgnoreCase(xmlReader.getLocalName())) {
                break;
            }
            if (xmlReader.isCharacters()) {
                String value = xmlReader.getText().trim();
                if (!value.isEmpty()) {
                    return MeleeWeapon.valueOf(value.toUpperCase());
                }
            }
        }
        return null;
    }

    public Coordinates parseCoordinates() throws XMLStreamException {
        Coordinates coordinates = new Coordinates();
        String currentField = "";
        while (xmlReader.hasNext()) {
            xmlReader.next();
            if (xmlReader.isEndElement() && "coordinates".equalsIgnoreCase(xmlReader.getLocalName())) {
                break;
            }
            if (xmlReader.isStartElement()) {
                currentField = xmlReader.getLocalName().toLowerCase().trim();
            }
            if (xmlReader.isCharacters() && !currentField.isEmpty()) {
                String value = xmlReader.getText().trim();
                if (!value.isEmpty()) {
                    switch (currentField) {
                        case "x":
                            coordinates.setX(parseXCoordinate());
                            break;
                        case "y":
                            coordinates.setY(parseYCoordinate());
                            break;
                    }
                }//TODO empty fields
            }
        }
        return coordinates;
    }
    public long parseYCoordinate (){//TODO NumberFormatExceotion
        return  Long.parseLong(xmlReader.getText());
    }

    public long parseXCoordinate () {//TODO NumberFormatExceotion
        return Long.parseLong(xmlReader.getText());
    }
}