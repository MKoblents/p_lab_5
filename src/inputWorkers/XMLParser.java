package inputWorkers;

import enums.AstartesCategory;
import enums.MeleeWeapon;
import enums.Weapon;
import manager.Chapter;
import manager.SpaceMarine;
import manager.Coordinates;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
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
                    currentMarine = parseSpaceMarine();
                    marines.add(currentMarine);
                    currentMarine = null;
                }
            }
        }
        xmlReader.close();
        return marines;
    }

    public SpaceMarine parseSpaceMarine() throws XMLStreamException {
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
                    spaceMarine.setCategory(parseAstartesCategory());
                } else if ("Weapon".equalsIgnoreCase(currentFieldName)) {
                    spaceMarine.setWeaponType(parseWeapon());
                } else if ("Chapter".equalsIgnoreCase(currentFieldName)){
                    spaceMarine.setChapter(parseChapter());
                }
            }
        }
        return spaceMarine;
    }

    public Chapter parseChapter() throws XMLStreamException {
        Chapter chapter = new Chapter();
        String currentField = "";
        while (xmlReader.hasNext()) {
            xmlReader.next();
            if (xmlReader.isEndElement() && "Chapter".equalsIgnoreCase(xmlReader.getLocalName())) {
                break;
            }
            if (xmlReader.isStartElement()) {
                currentField = xmlReader.getLocalName().toLowerCase();
            }
            if (xmlReader.isCharacters() && !currentField.isEmpty()) {
                String value = getTrimmedText();
                if (!value.isEmpty()) {
                    switch (currentField) {
                        case "name":
                            chapter.setName(value);
                            break;
                        case "parentlegion":
                            chapter.setParentLegion(value);
                            break;
                        case "world":
                            chapter.setWorld(value);
                    }
                }//TODO empty fields
            }
        }return chapter;
    }
    private <T extends Enum<T>> T parseEnum(String tagName, Class<T> enumType) throws XMLStreamException {
        while (xmlReader.hasNext()) {
            xmlReader.next();
            if (xmlReader.isEndElement() && tagName.equalsIgnoreCase(xmlReader.getLocalName())) {
                break;
            }
            if (xmlReader.isCharacters()) {
                String value = getTrimmedText();
                if (value != null && !value.isEmpty()) {
                    try {
                        return Enum.valueOf(enumType, value.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public Weapon parseWeapon() throws XMLStreamException {
        return parseEnum("weapon", Weapon.class);
    }

    public AstartesCategory parseAstartesCategory() throws XMLStreamException {
        return parseEnum("AstartesCategory", AstartesCategory.class);
    }

    public Double parseHealth() throws XMLStreamException {
        xmlReader.next();
        return xmlReader.getText().isEmpty() ? null : Double.parseDouble(xmlReader.getText());
    }

    public String parseName() throws XMLStreamException {

        while (xmlReader.hasNext()) {
            int eventType = xmlReader.next();
            if (eventType == XMLStreamConstants.CHARACTERS) {
                String text = getTrimmedText();
                if (!text.isEmpty()) {
                    return text;
                }
            }
            if (eventType == XMLStreamConstants.END_ELEMENT) {
                break;
            }
        }
        return null;
    }

    public MeleeWeapon parseMeleeWeapon() throws XMLStreamException {
        return parseEnum("MeleeWeapon", MeleeWeapon.class);
    }

    public Coordinates parseCoordinates() throws XMLStreamException {
        Coordinates coordinates = new Coordinates();
        String currentField = null;
        while (xmlReader.hasNext()) {
            xmlReader.next();
            if (xmlReader.isEndElement() && "coordinates".equalsIgnoreCase(xmlReader.getLocalName())) {
                break;
            }
            if (xmlReader.isStartElement()) {
                currentField = xmlReader.getLocalName().toLowerCase().trim();
            }
            if (xmlReader.isCharacters() && currentField != null) {
                parseCoordinateField(coordinates, currentField);
            }
        }
        return coordinates;
    }
    private void parseCoordinateField(Coordinates coordinates, String fieldName) {
        String value = getTrimmedText();
        if (value == null || value.isEmpty()) {
            return;
        }
        try {
            long coordinateValue = Long.parseLong(value);

            switch (fieldName) {
                case "x" -> coordinates.setX(coordinateValue);
                case "y" -> coordinates.setY(coordinateValue);
            }
        } catch (NumberFormatException e) {
            System.err.println("Warning: Invalid coordinate value for '" + fieldName + "': '" + value + "'");
        }
    }

    public long parseLongField(){
        try {
            return Long.parseLong(getTrimmedText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    private String getTrimmedText(){
        return xmlReader.getText().trim();
    }

}