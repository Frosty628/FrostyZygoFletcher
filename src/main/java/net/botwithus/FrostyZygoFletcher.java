package net.botwithus;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Item;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;

import java.util.List;
import java.util.Random;

public class FrostyZygoFletcher extends LoopingScript {

    private BotState botState = BotState.IDLE;
    private Random random = new Random();
    private boolean fungalShaftsSelected = false;
    private boolean sharpShellsSelected = false;
    private boolean dinoPropellantSelected = false;
    private volatile boolean stopped = false;

    private long lastInteractionTime = 0;

    public boolean isFungalShaftsSelected() {
        return fungalShaftsSelected;
    }

    public boolean isSharpShellsSelected() {
        return sharpShellsSelected;
    }

    public boolean isDinoPropellantSelected() {
        return dinoPropellantSelected;
    }

    public void setFungalShaftsSelected(boolean fungalShaftsSelected) {
        this.fungalShaftsSelected = fungalShaftsSelected;
    }

    public void setSharpShellsSelected(boolean sharpShellsSelected) {
        this.sharpShellsSelected = sharpShellsSelected;
    }

    public void setDinoPropellantSelected(boolean dinoPropellantSelected) {
        this.dinoPropellantSelected = dinoPropellantSelected;
    }

    enum BotState {
        IDLE,
        SKILLING,
    }

    public FrostyZygoFletcher(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new FrostyZygoFletcherGraphicsContext(getConsole(), this);
    }

    public void stopBot() {
        stopped = true;
        botState = BotState.IDLE;  // Force the bot state to IDLE
        println("Bot stopped! Returning to idle state.");
    }

    @Override
    public void onLoop() {
        if (stopped) {
            // Immediately cancel everything and return to idle if stopped
            botState = BotState.IDLE;
            return;  // Exit the loop immediately when stopped
        }

        LocalPlayer player = Client.getLocalPlayer();
        if (player == null || Client.getGameState() != Client.GameState.LOGGED_IN || botState == BotState.IDLE) {
            Execution.delay(random.nextLong(3000, 7000));
            return;
        }

        switch (botState) {
            case IDLE:
                println("We're idle!");
                Execution.delay(random.nextLong(1000, 3000));
                break;

            case SKILLING:
                println("We're Skilling!");
                Execution.delay(handleSkilling(player));
                break;
        }
    }

    private long handleSkilling(LocalPlayer player) {
        // Ensure no actions are performed if the bot is stopped
        if (stopped) {
            botState = BotState.IDLE; // Reset bot state to IDLE if stopped
            return 0; // Early return to stop further actions
        }

        long currentTime = System.currentTimeMillis();
        long randomInterval = random.nextLong(7000, 10000);

        // Ensure enough time has passed since the last interaction
        if (currentTime - lastInteractionTime < randomInterval) {
            return random.nextLong(1500, 3000);
        }

        // Check which option is selected
        if (fungalShaftsSelected && !sharpShellsSelected && !dinoPropellantSelected) {
            // === FUNGAL SHAFTS LOGIC ===
            println("Fungal Shafts");
            if (botState == BotState.SKILLING) {
                // Call the method to interact with Scruffy zygomite
                interactWithScruffyZygomite(player);
            }
        } else if (sharpShellsSelected && !fungalShaftsSelected && !dinoPropellantSelected) {
            // === SHARP SHELLS LOGIC ===
            println("Sharp Shells selected");
            interactWithDinosaurEggPile(player);
        } else if (dinoPropellantSelected && !fungalShaftsSelected && !sharpShellsSelected) {
            // === DINO PROPELLANT LOGIC ===
            println("Dino Propellant selected");
            interactWithStormBarn(player);
        }

        return random.nextLong(1500, 3000);
    }

    private void interactWithScruffyZygomite(LocalPlayer player) {
        if (stopped) {
            println("Bot stopped");
            return;  // Immediately return if the bot is stopped
        }
        Npc scruffyZygomite = NpcQuery.newQuery()
                .name("Scruffy zygomite")
                .results()
                .nearest();

        if (scruffyZygomite != null) {
            String[] options = {"Cut and dry", "Cut", "Dry"};
            String chosenOption = null;

            // Fetch the available actions of the NPC (getOptions() returns a List<String>)
            List<String> npcActions = scruffyZygomite.getOptions();  // No need to convert, work directly with the List

            if (npcActions != null) {
                for (String option : options) {
                    for (String action : npcActions) {
                        if (option.equalsIgnoreCase(action)) {
                            chosenOption = option;
                            break;
                        }
                    }
                    if (chosenOption != null) break; // Stop checking once an option is found
                }
            }

            if (chosenOption != null) {
                println("Interacting with Scruffy zygomite using option: " + chosenOption);
                scruffyZygomite.interact(chosenOption);
                lastInteractionTime = System.currentTimeMillis(); // Update last interaction time
                Execution.delay(random.nextLong(1500, 3000)); // Delay after interaction

                // Wait for animation ID to be active for at least 3 seconds
                waitForAnimationStable(player, -1, 3000);  // Adjusted to wait for 3 seconds

            } else {
                println("No valid interaction option found for Scruffy zygomite.");
            }
        } else {
            println("No Scruffy zygomite found nearby.");
        }
    }

    private void interactWithDinosaurEggPile(LocalPlayer player) {
        if (stopped) {
            println("Bot stopped");
            return;  // Immediately return if the bot is stopped
        }
        SceneObject dinosaurEggPile = SceneObjectQuery.newQuery()
                .name("Pile of dinosaur eggs")
                .option("Collect")
                .results()
                .nearest();

        if (dinosaurEggPile != null) {
            println("Interacting with Pile of dinosaur eggs: " + dinosaurEggPile.interact("Collect"));
            lastInteractionTime = System.currentTimeMillis();

            Execution.delay(random.nextLong(50, 3000));
            waitForSpecificAnimation(player, 12739);
            waitForAnimationStable(player, -1, 3000);  // Changed to wait for animation stability for 3 seconds

            interactWithFastIncubator(player);
        } else {
            println("No Pile of dinosaur eggs found nearby.");
        }
    }

    private void interactWithFastIncubator(LocalPlayer player) {
        if (stopped) {
            println("Bot stopped");
            return;  // Immediately return if the bot is stopped
        }
        SceneObject fastIncubator = SceneObjectQuery.newQuery()
                .name("Fast incubator")
                .option("Incubate")
                .results()
                .nearest();

        if (fastIncubator != null) {
            println("Interacting with Fast incubator: " + fastIncubator.interact("Incubate"));
            lastInteractionTime = System.currentTimeMillis();

            Execution.delay(random.nextLong(1500, 3000));
            waitForSpecificAnimation(player, 24908);
            waitForAnimationStable(player, -1, 3000);  // Changed to wait for animation stability for 3 seconds

            interactWithCompostBin(player);
        } else {
            println("No Fast incubator found nearby.");
        }
    }

    private void interactWithCompostBin(LocalPlayer player) {
        if (stopped) {
            println("Bot stopped");
            return;  // Immediately return if the bot is stopped
        }
        SceneObject compostBin = SceneObjectQuery.newQuery()
                .name("Fast compost bin")
                .option("Compost")
                .results()
                .nearest();

        if (compostBin != null) {
            println("Interacting with Fast compost bin: " + compostBin.interact("Compost"));
            lastInteractionTime = System.currentTimeMillis();

            Execution.delay(random.nextLong(1500, 3000));
            waitForSpecificAnimation(player, 34786);
            waitForAnimationStable(player, -1, 3000);  // Changed to wait for animation stability for 3 seconds
        } else {
            println("No Fast compost bin found nearby.");
        }
    }

    private void waitForAnimationStable(LocalPlayer player, int animationId, int minDuration) {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < minDuration) {
            if (player.getAnimationId() != animationId) {
                startTime = System.currentTimeMillis(); // Reset timer if animation changes
            }
            Execution.delay(100); // Fixed delay method call with a single argument
        }
    }

    private void interactWithStormBarn(LocalPlayer player) {
        if (stopped) {
            println("Bot stopped");
            return;  // Immediately return if the bot is stopped
        }
        SceneObject stormBarn = SceneObjectQuery.newQuery()
                .name("Storm barn")
                .option("Collect")
                .results()
                .nearest();

        if (stormBarn != null) {
            println("Interacting with Storm barn: " + stormBarn.interact("Collect"));
            lastInteractionTime = System.currentTimeMillis();

            Execution.delay(random.nextLong(1500, 3000));

            waitForAnimation(player, 25654, 7000);
            waitForAnimationStable(player, -1, 5000); // Ensure animation -1 occurs for at least 5s

            interactWithBeanyMushTrough(player);
        } else {
            println("No Storm barn found nearby.");
        }
    }

    private void interactWithBeanyMushTrough(LocalPlayer player) {
        if (stopped) {
            println("Bot stopped");
            return;  // Immediately return if the bot is stopped
        }
        SceneObject beanyMushTrough = SceneObjectQuery.newQuery()
                .name("Beany mush trough")
                .option("Feed")
                .results()
                .nearest();

        if (beanyMushTrough != null) {
            println("Interacting with Beany mush trough: " + beanyMushTrough.interact("Feed"));
            lastInteractionTime = System.currentTimeMillis();

            Execution.delay(random.nextLong(1500, 3000));

            waitForAnimation(player, 34784, 7000);
            waitForAnimationStable(player, -1, 5000); // Ensure animation -1 occurs for at least 5s

            feedBerryMushTrough(player);
        } else {
            println("No Beany mush trough found nearby.");
        }
    }

    private void feedBerryMushTrough(LocalPlayer player) {
        if (stopped) {
            println("Bot stopped");
            return;  // Immediately return if the bot is stopped
        }
        println("Attempting to feed Berry mush trough");

        SceneObject berryMushTrough = SceneObjectQuery.newQuery()
                .name("Berry mush trough")
                .option("Feed")
                .results()
                .nearest();

        if (berryMushTrough != null) {
            println("Interacting with Berry mush trough: " + berryMushTrough.interact("Feed"));
            lastInteractionTime = System.currentTimeMillis();

            Execution.delay(random.nextLong(1500, 3000));

            waitForAnimation(player, 34784, 7000);
            waitForAnimationStable(player, -1, 5000); // Ensure animation -1 occurs for at least 5s

            feedCerealyMushTrough(player);
        } else {
            println("No Berry mush trough found nearby.");
        }
    }

    private void feedCerealyMushTrough(LocalPlayer player) {
        if (stopped) {
            println("Bot stopped");
            return;  // Immediately return if the bot is stopped
        }
        println("Attempting to feed Cerealy mush trough");

        SceneObject cerealyMushTrough = SceneObjectQuery.newQuery()
                .name("Cerealy mush trough")
                .option("Feed")
                .results()
                .nearest();

        if (cerealyMushTrough != null) {
            println("Interacting with Cerealy mush trough: " + cerealyMushTrough.interact("Feed"));
            lastInteractionTime = System.currentTimeMillis();

            Execution.delay(random.nextLong(1500, 3000));

            waitForAnimation(player, 34784, 7000);
            waitForAnimationStable(player, -1, 5000); // Ensure animation -1 occurs for at least 5s

            feedRootyMushTrough(player);
        } else {
            println("No Cerealy mush trough found nearby.");
        }
    }

    private void feedRootyMushTrough(LocalPlayer player) {
        if (stopped) {
            println("Bot stopped");
            return;  // Immediately return if the bot is stopped
        }
        println("Attempting to feed Rooty mush trough");

        SceneObject rootyMushTrough = SceneObjectQuery.newQuery()
                .name("Rooty mush trough")
                .option("Feed")
                .results()
                .nearest();

        if (rootyMushTrough != null) {
            println("Interacting with Rooty mush trough: " + rootyMushTrough.interact("Feed"));
            lastInteractionTime = System.currentTimeMillis();

            Execution.delay(random.nextLong(1500, 3000));

            waitForAnimation(player, 34784, 7000);
            waitForAnimationStable(player, -1, 5000); // Ensure animation -1 occurs for at least 5s

            ignitePotteringtonBlend(player);
        } else {
            println("No Rooty mush trough found nearby.");
        }
    }

    private long ignitePotteringtonBlend(LocalPlayer player) {
        println("Attempting to Ignite Potterington Blend");

        Item potteringtonBlendItem = Backpack.getItem("Potterington Blend #102 Fertiliser");

        if (potteringtonBlendItem != null) {
            // Interact with the item in the backpack
            int itemSlot = Backpack.getItems().indexOf(potteringtonBlendItem);
            if (itemSlot != -1) {
                println("Interacting with Potterington Blend: " + Backpack.interact(itemSlot, "Ignite"));
                lastInteractionTime = System.currentTimeMillis();

                Execution.delay(random.nextLong(1500, 3000));

                // Wait for specific animation ID (25600) for Potterington Blend
                waitForSpecificAnimation(player, 25600);

                // Wait for Animation ID -1 for at least 5 seconds
                waitForAnimationStable(player, -1, 5000); // Ensure animation -1 occurs for at least 5s

                return random.nextLong(1500, 3000); // Return delay after performing the action
            }
        } else {
            println("No Potterington Blend #102 Fertiliser found in backpack.");
            return random.nextLong(1500, 3000); // Return delay if no item found
        }
        return random.nextLong(1500, 3000); // Return delay if no item found
    }




    private void waitForAnimation(LocalPlayer player, int animationId, int timeout) {
        // Set timeout to 30 seconds if animation ID is 34784
        if (animationId == 34784) {
            timeout = 30000;
        }

        println("Waiting for animation: " + animationId);
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeout) {
            if (player.getAnimationId() == animationId) {
                println("Animation started: " + animationId);
                while (player.getAnimationId() == animationId) {
                    Execution.delay(100);
                    if (System.currentTimeMillis() - startTime > timeout) {
                        println("Timeout reached while waiting for animation: " + animationId);
                        return;
                    }
                }
                println("Animation finished: " + animationId);
                return;
            }
            Execution.delay(100);
        }
        println("Timeout: Animation " + animationId + " did not start.");
    }

    private void waitForSpecificAnimation(LocalPlayer player, int animationId) {
        waitForAnimation(player, animationId, 40000); // Default to wait for 40 seconds for a specific animation
    }

    public BotState getBotState() {
        return botState;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    public boolean isSomeBool() {
        return true;
    }

    public void setSomeBool(boolean someBool) {
        // This function isn't used but can be implemented if needed
    }
}

