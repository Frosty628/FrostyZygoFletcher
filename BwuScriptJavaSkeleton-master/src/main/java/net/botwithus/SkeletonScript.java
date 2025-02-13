package net.botwithus;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.api.game.skills.smithing.Smithing;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.cs2.ReturnValue;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.game.vars.VarManager;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;

import java.util.Random;

import static net.botwithus.api.game.skills.smithing.Smithing.get_max_heat;

public class SkeletonScript extends LoopingScript {
    private BotState botState = BotState.IDLE;
    private boolean someBool = true;
    private Random random = new Random();

    enum BotState {
        //define your own states here
        IDLE,
        SKILLING,
        BANKING,
        Heating_Item,
        Smithing_Item,
        //...
    }

    public SkeletonScript(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new SkeletonScriptGraphicsContext(getConsole(), this);
    }

    @Override
    public void onLoop() {
        //Loops every 100ms by default, to change:
        //this.loopDelay = 500;
        LocalPlayer player = Client.getLocalPlayer();
        if (player == null || Client.getGameState() != Client.GameState.LOGGED_IN || botState == BotState.IDLE) {
            //wait some time so we dont immediately start on login.
            Execution.delay(random.nextLong(3000,7000));
            return;
        }
        switch (botState) {
            case IDLE -> {
                //do nothing
                println("We're idle!");
                Execution.delay(random.nextLong(1000,3000));
            }
            case SKILLING -> {
                //do some code that handles your skilling
                Execution.delay(handleSkilling(player));
            }
            case BANKING -> {
                //handle your banking logic, etc
            }
            case Heating_Item -> {
                println("Heating Item");
                Execution.delay(handleSkilling(player));
                //heating smithing items
            }
            case Smithing_Item -> {
                println("Smithing Item");
                Execution.delay(handleSkilling(player));
                //smithing heated items

            }
        }
    }

    private long handleSkilling(LocalPlayer player) {
        if (Interfaces.isOpen(1251)) {
            return random.nextLong(250, 1500);
        }

        println(player.getAnimationId());
        if (player.getAnimationId() != -1) {
            return random.nextLong(100, 1000);
        }

        int itemSlot = findItemSlot("Unfinished smithing item"); // Find first unfinished item
        if (itemSlot != -1) {
            double heatLevel = getHeatLevel(itemSlot); // Get heat level percentage
            println("Item heat level: " + heatLevel + "%");

            if (heatLevel <= 50) {
                // Heat at the forge
                SceneObject forge = SceneObjectQuery.newQuery().name("Forge").option("Heat").results().nearest();
                if (forge != null) {
                    println("Heating item at Forge...");
                    forge.interact("Heat");
                    Execution.delay(random.nextLong(2000, 4000)); // Wait for heating
                    botState = BotState.Smithing_Item; // Move to smithing
                } else {
                    println("No Forge found!");
                }
            } else {
                // Smith at the anvil
                SceneObject anvil = SceneObjectQuery.newQuery().name("Anvil").option("Smith").results().nearest();
                if (anvil != null) {
                    println("Smithing item at Anvil...");
                    anvil.interact("Smith");
                    Execution.delay(random.nextLong(2000, 5000)); // Wait for smithing
                    botState = BotState.Heating_Item; // Restart heating process
                } else {
                    println("No Anvil found!");
                }
            }
        } else {
            println("No Unfinished smithing item found in inventory.");
            botState = BotState.IDLE;
        }

        return random.nextLong(1500, 3000);
    }

    public BotState getBotState() {
        return botState;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    public boolean isSomeBool() {
        return someBool;
    }

    public void setSomeBool(boolean someBool) {
        this.someBool = someBool;
    }
}