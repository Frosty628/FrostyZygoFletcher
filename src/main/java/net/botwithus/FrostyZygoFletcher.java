package net.botwithus;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;

import java.util.Random;

public class FrostyZygoFletcher extends LoopingScript {

    private BotState botState = BotState.IDLE;
    private Random random = new Random();
    private boolean fungalShaftsSelected = false;
    private boolean sharpShellsSelected = false;

    // Getter and setter for Fungal Shafts selection
    public boolean isFungalShaftsSelected() {
        return fungalShaftsSelected;
    }

    public void setFungalShaftsSelected(boolean selected) {
        this.fungalShaftsSelected = selected;
    }

    // Getter and setter for Sharp Shells selection
    public boolean isSharpShellsSelected() {
        return sharpShellsSelected;
    }

    public void setSharpShellsSelected(boolean selected) {
        this.sharpShellsSelected = selected;
    }

    private void waitForSpecificAnimation(LocalPlayer player, int expectedAnimationId) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 5000) { // Max wait time: 5 sec
            if (player.getAnimationId() == expectedAnimationId) {
                println("Detected expected animation ID: " + expectedAnimationId);
                break; // Stop waiting as soon as the correct animation is seen
            }
            Execution.delay(500);
        }
    }

    private void waitForAnimation(LocalPlayer player, int animationId, int waitTime) {
        long animationStartTime = 0;
        boolean animationStarted = false;

        while (true) {
            if (player.getAnimationId() == animationId) {
                if (!animationStarted) {
                    animationStartTime = System.currentTimeMillis();
                    animationStarted = true;
                }

                if (System.currentTimeMillis() - animationStartTime >= waitTime) {
                    println("Animation ID " + animationId + " has been active for " + waitTime / 1000 + " seconds.");
                    break;
                }
            } else {
                animationStarted = false; // Reset if animation changes
            }

            Execution.delay(500); // Prevent excessive CPU usage
        }
    }

    // Track the time of the last interaction
    private long lastInteractionTime = 0;

    enum BotState {
        IDLE,
        SKILLING,
    }

    public FrostyZygoFletcher(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new FrostyZygoFletcherGraphicsContext(getConsole(), this);
    }

    @Override
    public void onLoop() {
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
        long currentTime = System.currentTimeMillis();
        long randomInterval = random.nextLong(7000, 10000);

        // Ensure enough time has passed since the last interaction
        if (currentTime - lastInteractionTime < randomInterval) {
            return random.nextLong(1500, 3000);
        }

        // Check which option is selected
        if (fungalShaftsSelected && !sharpShellsSelected) {
            // === ORIGINAL CODE FOR FUNGAL SHAFTS (UNCHANGED) ===
            println("Fungal Shafts");
            if (botState == BotState.SKILLING) {
                Npc scruffyZygomite = NpcQuery.newQuery()
                        .name("Scruffy zygomite")
                        .option("Cut and dry")
                        .results()
                        .nearest();

                if (scruffyZygomite != null) {
                    println("Interacting with Scruffy zygomite: " + scruffyZygomite.interact("Cut and dry"));
                    lastInteractionTime = currentTime; // Update last interaction time
                    Execution.delay(random.nextLong(1500, 3000)); // Delay after interaction

                    // Wait for animation ID -1 to be active for at least 5 seconds
                    long animationWaitStartTime = System.currentTimeMillis();
                    long animationStartTime = 0;
                    boolean animationStarted = false;

                    while (true) {
                        if (player.getAnimationId() == -1) {
                            if (!animationStarted) {
                                animationStartTime = System.currentTimeMillis();
                                animationStarted = true;
                            }

                            if (System.currentTimeMillis() - animationStartTime >= 5000) {
                                println("Animation ID -1 has been active for 5 seconds.");
                                break;
                            }
                        } else {
                            animationStarted = false;
                        }

                        Execution.delay(500); // Prevent excessive CPU usage
                    }

                    return random.nextLong(1500, 3000);
                }
            }
        }

        else if (sharpShellsSelected && !fungalShaftsSelected) {
            // === SHARP SHELLS LOGIC ===
            println("Sharp Shells selected - Attempting to collect dinosaur eggs");

            // Locate and interact with "Pile of dinosaur eggs"
            SceneObject dinosaurEggPile = SceneObjectQuery.newQuery()
                    .name("Pile of dinosaur eggs")
                    .option("Collect")
                    .results()
                    .nearest();

            if (dinosaurEggPile != null) {
                println("Interacting with Pile of dinosaur eggs: " + dinosaurEggPile.interact("Collect"));
                lastInteractionTime = currentTime;

                Execution.delay(random.nextLong(1500, 3000));

                // === Check for Animation ID 12739 Once ===
                waitForSpecificAnimation(player, 12739);

                // === Wait for Animation ID -1 for 5 seconds ===
                waitForAnimation(player, -1, 5000);

                // === SECOND INTERACTION: Fast incubator ===
                println("Attempting to use Fast incubator...");

                SceneObject fastIncubator = SceneObjectQuery.newQuery()
                        .name("Fast incubator")
                        .option("Incubate")
                        .results()
                        .nearest();

                if (fastIncubator != null) {
                    println("Interacting with Fast incubator: " + fastIncubator.interact("Incubate"));
                    lastInteractionTime = System.currentTimeMillis();

                    Execution.delay(random.nextLong(1500, 3000));

                    // === Check for Animation ID 24908 Once ===
                    waitForSpecificAnimation(player, 24908);

                    // === Wait for Animation ID -1 for another 5 seconds ===
                    waitForAnimation(player, -1, 5000);

                    // === THIRD INTERACTION: Fast compost bin ===
                    println("Attempting to use Fast compost bin...");

                    SceneObject compostBin = SceneObjectQuery.newQuery()
                            .name("Fast compost bin")
                            .option("Compost")
                            .results()
                            .nearest();

                    if (compostBin != null) {
                        println("Interacting with Fast compost bin: " + compostBin.interact("Compost"));
                        lastInteractionTime = System.currentTimeMillis();

                        Execution.delay(random.nextLong(1500, 3000));

                        // === Check for Animation ID 34786 Once ===
                        waitForSpecificAnimation(player, 34786);

                        // === Wait for Animation ID -1 for 5 seconds ===
                        waitForAnimation(player, -1, 5000);
                    } else {
                        println("No Fast compost bin found nearby.");
                    }
                } else {
                    println("No Fast incubator found nearby.");
                }
            } else {
                println("No Pile of dinosaur eggs found nearby.");
            }

        } else {
            println("No valid processing option selected.");
        }

        return random.nextLong(1500, 3000); // Default delay
    }

    // Waits for animation ID -1 for at least `durationMillis` milliseconds
    private long waitForAnimation(LocalPlayer player, int animationId, long durationMillis) {
        long animationStartTime = 0;
        boolean animationStarted = false;

        while (true) {
            if (player.getAnimationId() == animationId) {
                if (!animationStarted) {
                    animationStartTime = System.currentTimeMillis();
                    animationStarted = true;
                }

                if (System.currentTimeMillis() - animationStartTime >= 5000) {
                    println("Animation ID -1 has been active for 5 seconds.");
                    break;
                }
            } else {
                animationStarted = false;
            }

            Execution.delay(500); // Prevent excessive CPU usage
        }

        // Default return delay if no interaction occurs
        return random.nextLong(1500, 3000);

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

