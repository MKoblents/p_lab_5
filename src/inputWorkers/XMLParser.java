package inputWorkers;

import enums.AstartesCategory;
import enums.MeleeWeapon;
import enums.Weapon;
import manager.Chapter;
import manager.CollectionManager;
import manager.SpaceMarine;
import manager.Coordinates;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
/**
 * StAX-based XML parser for SpaceMarine collection deserialization.
 * Reads XML files or strings and converts them to managed SpaceMarine objects.
 */
public class XMLParser {
    /** Factory for creating XML stream readers (reused for efficiency). */
    private XMLInputFactory factory;
    /** Active XML stream reader for parsing operations. */
    private XMLStreamReader xmlReader;
    /** Manager delegate for creating new SpaceMarine instances with auto-ID. */
    private CollectionManager collectionManager;
    /**
     * Initializes parser with file source and collection manager.
     * @param filePath path to XML file to parse
     * @param collectionManager manager for ID generation and validation
     * @throws FileNotFoundException if file doesn't exist
     * @throws XMLStreamException if XML is malformed
     */
    public XMLParser(String filePath, CollectionManager collectionManager) throws FileNotFoundException, XMLStreamException {
        FileInputStream fis = new FileInputStream(filePath);
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(isr);
        this.factory = XMLInputFactory.newInstance();
        this.xmlReader = factory.createXMLStreamReader(reader);
        this.collectionManager = collectionManager;
    }
    /**
     * Parses entire XML file and returns list of SpaceMarine objects.
     * @return list of parsed marines (may be empty)
     * @throws Exception if parsing fails at any point
     */
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
    /**
     * Parses a single <spaceMarine></spaceMarine> element from current stream position.
     * @return fully populated SpaceMarine instance
     * @throws XMLStreamException if XML structure is invalid
     */
    public SpaceMarine parseSpaceMarine() throws XMLStreamException {
        String currentFieldName;
        SpaceMarine spaceMarine = collectionManager.getNewSpaceMarine();
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
    /**
     * Parses SpaceMarine from raw XML string (for command arguments).
     * @param xmlString XML fragment containing single spaceMarine element
     * @return parsed SpaceMarine instance
     * @throws XMLStreamException if XML is malformed
     * @throws IOException if UTF-8 encoding fails
     */
    public SpaceMarine parseSpaceMarineFromString(String xmlString) throws XMLStreamException, IOException {
        byte[] bytes = xmlString.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        factory = XMLInputFactory.newInstance();
        xmlReader = factory.createXMLStreamReader(inputStream);
        SpaceMarine spaceMarine = parseSpaceMarine();
        xmlReader.close();
        inputStream.close();
        return spaceMarine;
    }
    /**
     * Dispatches field parsing based on XML tag name.
     * @param marine target object to populate
     * @param fieldName lowercase XML tag name
     * @implNote Catches XMLStreamException internally to avoid aborting entire parse
     */
    private void parseSpaseNarineField(SpaceMarine marine, String fieldName){
        try {
            switch (fieldName) {
                case "name" -> marine.setName(parseStringField());
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
    /**
     * Parses &lt;Chapter&gt; nested element with its subfields.
     * @return populated Chapter instance (fields may be null if missing)
     * @throws XMLStreamException if XML structure is invalid
     */
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
    /**
     * Sets Chapter field value based on tag name.
     * @param chapter target object
     * @param fieldName lowercase tag name (name/parentLegion/world)
     */
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
    /**
     * Generic enum parser: reads text content and matches to enum constant.
     * @param tagName expected closing tag name
     * @param enumType enum class to parse into
     * @return matched enum constant, or null if not found/invalid
     * @param <T> enum type parameter
     * @throws XMLStreamException if stream read fails
     */
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
    /** @return parsed Weapon enum or null */
    public Weapon parseWeapon() throws XMLStreamException {
        return parseEnum("weapon", Weapon.class);
    }
    /** @return parsed AstartesCategory enum or null */
    public AstartesCategory parseAstartesCategory() throws XMLStreamException {
        return parseEnum("AstartesCategory", AstartesCategory.class);
    }
    /**
     * Parses double value with fallback on error.
     * @return parsed double, or 0.0 if invalid/missing
     * @throws XMLStreamException if stream read fails
     */
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
    /**
     * Parses string value from current element.
     * @return trimmed text content, or null if empty/missing
     * @throws XMLStreamException if stream read fails
     */
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
    /** @return parsed MeleeWeapon enum or null */
    public MeleeWeapon parseMeleeWeapon() throws XMLStreamException {
        return parseEnum("MeleeWeapon", MeleeWeapon.class);
    }
    /**
     * Parses &lt;coordinates&gt; element with x/y subfields.
     * @return Coordinates object (defaults to 0,0 if missing)
     * @throws XMLStreamException if XML structure is invalid
     */
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
    /**
     * Sets coordinate value (x or y) based on field name.
     * @param coordinates target object
     * @param fieldName "x" or "y"
     * @throws XMLStreamException if stream read fails (unused but declared)
     */
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
    /**
     * Helper: gets current XML text content trimmed.
     * @return trimmed text string
     */
    private String getTrimmedText(){
        return xmlReader.getText().trim();
    }

}