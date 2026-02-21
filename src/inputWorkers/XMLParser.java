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

    public ArrayList<SpaceMarine> parseSpaceMarines() throws Exception {
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
                currentFieldName = xmlReader.getLocalName().toLowerCase();
                parseSpaseNarineField(spaceMarine, currentFieldName);
            }
        }
        return spaceMarine;
    }
    private void parseSpaseNarineField(SpaceMarine marine, String fieldName){
        try {
            switch (fieldName) {
                case "name" -> marine.setName(parseStringField());
                case "id" -> marine.setId(parseLongField());
                case "health" -> marine.setHealth(parseDoubleField());
                case "coordinates" -> marine.setCoordinates(parseCoordinates());
                case "astartescategory" -> marine.setCategory(parseAstartesCategory());
                case "weapon" -> marine.setWeaponType(parseWeapon());
                case "meleeweapon" -> marine.setMeleeWeapon(parseMeleeWeapon());
                case "chapter" -> marine.setChapter(parseChapter());
                default -> {}
            }
        } catch (XMLStreamException e) {
            System.err.println("Warning: Failed to parse field '" + fieldName + "': " + e.getMessage());
        }
    }

    public Chapter parseChapter() throws XMLStreamException {
        Chapter chapter = new Chapter();
        String currentField = null;
        while (xmlReader.hasNext()) {
            xmlReader.next();
            if (xmlReader.isEndElement() && "Chapter".equalsIgnoreCase(xmlReader.getLocalName())) {
                break;
            }
            if (xmlReader.isStartElement()) {
                currentField = xmlReader.getLocalName().toLowerCase();
            }
            if (xmlReader.isCharacters() && currentField != null) {
                processChapterField(chapter, currentField);
            }
        }
        return chapter;
    }

    private void processChapterField(Chapter chapter, String fieldName) {
        String value = getTrimmedText();
        if (value == null || value.isEmpty()) {
            return;
        }
        switch (fieldName) {
            case "name" -> chapter.setName(value);
            case "parentlegion" -> chapter.setParentLegion(value);
            case "world" -> chapter.setWorld(value);
            default -> {}
        }
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

    public double parseDoubleField() throws XMLStreamException {
        while (xmlReader.hasNext()) {
            int eventType = xmlReader.next();
            if (eventType == XMLStreamConstants.CHARACTERS) {
                String text = getTrimmedText();
                if (text != null && !text.isEmpty()) {
                    try {
                        return Double.parseDouble(text);
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Invalid health value: '" + text + "'");
                        return 0;
                    }
                }
            }
            if (eventType == XMLStreamConstants.END_ELEMENT) {
                return 0;
            }
        }
        return 0;
    }

    public String parseStringField() throws XMLStreamException {
        while (xmlReader.hasNext()) {
            int eventType = xmlReader.next();
            if (eventType == XMLStreamConstants.CHARACTERS) {
                return getTrimmedText();
            }
            if (eventType == XMLStreamConstants.END_ELEMENT) {
                return null;
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
    private void parseCoordinateField(Coordinates coordinates, String fieldName) throws XMLStreamException {
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
        }
        catch (NumberFormatException e){
            return;
        }

    }

    public long parseLongField() throws XMLStreamException {
        while (xmlReader.hasNext()) {
            int eventType = xmlReader.next();
            if (eventType == XMLStreamConstants.CHARACTERS) {
                String text = getTrimmedText();
                if (text != null && !text.isEmpty()) {
                    try {
                        return Long.parseLong(text);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
            }
            if (eventType == XMLStreamConstants.END_ELEMENT) {
                return 0;
            }
        }
        return 0;
    }
    private String getTrimmedText(){
        return xmlReader.getText().trim();
    }

}