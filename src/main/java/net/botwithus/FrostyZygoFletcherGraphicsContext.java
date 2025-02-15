package net.botwithus;

import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.ImGuiWindowFlag;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

public class FrostyZygoFletcherGraphicsContext extends ScriptGraphicsContext {

    private FrostyZygoFletcher script;

    public FrostyZygoFletcherGraphicsContext(ScriptConsole scriptConsole, FrostyZygoFletcher script) {
        super(scriptConsole);
        this.script = script;
    }

    @Override
    public void drawScriptConsole() {
        String windowId = "Script Console##ScriptSampleName";

        if (ImGui.Begin(windowId, ImGuiWindowFlag.None.getValue())) {
            if (ImGui.Button("Clear")) {
                script.getConsole().clear();
            }

            ImGui.SameLine();
            script.getConsole().setScrollToBottom(ImGui.Checkbox("Scroll to bottom", script.getConsole().isScrollToBottom()));

            if (ImGui.BeginChild("##console_lines", -1.0F, -1.0F, true, 0)) {
                for (int i = 0; i < 200; ++i) {
                    int lineIndex = (script.getConsole().getLineIndex() + i) % 200;
                    if (script.getConsole().getConsoleLines()[lineIndex] != null) {
                        ImGui.Text("%s", script.getConsole().getConsoleLines()[lineIndex]);
                    }
                }

                if (script.getConsole().isScrollToBottom()) {
                    ImGui.SetScrollHereY(1.0F);
                }

                ImGui.EndChild();
            }
        }

        ImGui.End();
    }

    @Override
    public void drawSettings() {
        if (ImGui.Begin("Frosty Zygo Fletcher", ImGuiWindowFlag.None.getValue())) {
            if (ImGui.BeginTabBar("My bar", ImGuiWindowFlag.None.getValue())) {
                if (ImGui.BeginTabItem("Settings", ImGuiWindowFlag.None.getValue())) {
                    ImGui.Text("Frosty Zygo Fletcher!");
                    ImGui.Text("Instructions - Start near desired activity, with add pathing for each down the line");
                    ImGui.Text("My script's state is: " + script.getBotState());

                    // Start and Stop button
                    ImGui.SameLine();
                    if (ImGui.Button(script.getBotState() == FrostyZygoFletcher.BotState.IDLE ? "Start" : "Stop")) {
                        if (script.getBotState() == FrostyZygoFletcher.BotState.IDLE) {
                            // Change to SKILLING state (Start bot)
                            script.setBotState(FrostyZygoFletcher.BotState.SKILLING);
                        } else {
                            // Stop the bot
                            script.stopBot();  // Call stopBot() to stop the bot immediately
                        }
                    }

                    // Selection dropdown for processing type
                    ImGui.Text("Select Processing Type:");
                    String[] options = { "Fungal Shafts", "Sharp Shells", "Dino Propellant" };

                    // Determine the currently selected option
                    int selectedIndex = -1;
                    if (script.isFungalShaftsSelected()) {
                        selectedIndex = 0;
                    } else if (script.isSharpShellsSelected()) {
                        selectedIndex = 1;
                    } else if (script.isDinoPropellantSelected()) {
                        selectedIndex = 2;
                    }

                    if (ImGui.Button("Select Processing Type")) {
                        ImGui.OpenPopup("ProcessingTypePopup", 0);
                    }

                    if (ImGui.BeginPopup("ProcessingTypePopup", 0)) {
                        for (int i = 0; i < options.length; i++) {
                            boolean isSelected = (selectedIndex == i);

                            if (ImGui.Selectable(options[i], isSelected, 0)) {
                                // Update script state based on selection
                                script.setFungalShaftsSelected(i == 0);
                                script.setSharpShellsSelected(i == 1);
                                script.setDinoPropellantSelected(i == 2);
                            }

                            if (isSelected) {
                                ImGui.SetItemDefaultFocus();
                            }
                        }
                        ImGui.EndPopup();
                    }

                    // Display selected option
                    String selectedText = script.isFungalShaftsSelected() ? "Fungal Shafts"
                            : script.isSharpShellsSelected() ? "Sharp Shells"
                            : script.isDinoPropellantSelected() ? "Dino Propellant"
                            : "None";
                    ImGui.Text("Selected: " + selectedText);

                    ImGui.EndTabItem();
                }
                ImGui.EndTabBar();
            }
            ImGui.End();
        }
    }
}
