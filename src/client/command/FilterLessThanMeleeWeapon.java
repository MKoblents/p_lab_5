package client.command;

import client.inputWorkers.InputManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.enums.MeleeWeapon;

import java.io.IOException;

public class FilterLessThanMeleeWeapon implements ClientCommand {
    private static final Logger logger = LoggerFactory.getLogger(FilterLessThanMeleeWeapon.class);
    private final InputManager inputManager;

    public FilterLessThanMeleeWeapon(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @Override
    public CommandRequest execute(SideFlag flag) {
        MeleeWeapon weapon;

        if (flag == SideFlag.FORWARDED) {
            try {
                System.out.println("Please, enter melee weapon.");
                weapon = inputManager.getNewEnumType(MeleeWeapon.class);
            } catch (IOException e) {
                logger.error("Failed to read MeleeWeapon from input: {}", e.getMessage());
                System.err.println("Error: Could not read weapon type. Please check your input stream.");
                return null;
            }
        } else {
            weapon = inputManager.getLastInputMeleeWeapon();
        }

        if (weapon == null) {
            System.err.println("Error: A valid MeleeWeapon value is required. Please select from: AXE, SWORD, HAMMER, or CLAWS.");
            logger.debug("FilterLessThanMeleeWeapon command aborted: weapon input is null.");
            return null;
        }

        logger.debug("Preparing filter request with weapon: {}", weapon);
        return RequestsFactory.withMeleeWeapon("filter_less_than_melee_weapon", weapon);
    }
}