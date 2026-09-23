package com.itemsucker.gui;

import com.itemsucker.ItemSuckerConfig;
import com.itemsucker.ItemSuckerMod;
import com.itemsucker.BaritoneBridge;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple grid of every item in the game so the player can click to add /
 * remove items from the active list (whitelist or blacklist, depending on
 * ItemSuckerConfig#mode). Reachable with "/itemsucker gui".
 */
public class ItemSelectionScreen extends Screen {
    private static final int CELL = 18;
    private static final int GRID_TOP = 96;
    private static final int WHITELIST_HIGHLIGHT = 0xFF35B86B;
    private static final int BLACKLIST_HIGHLIGHT = 0xFFD04A4A;
    private static final int HIGHLIGHT_INNER = 0xFF17202A;

    private final Screen parent;
    private final ItemSuckerConfig config;

    private EditBox searchBox;
    private Button modeButton;
    private Button enabledButton;
    private Button returnButton;
    private Button notifyButton;
    private List<Item> filteredItems = new ArrayList<>();
    private int columns = 9;
    private int scrollRows = 0;

    public ItemSelectionScreen(Screen parent) {
        super(Component.literal("ItemSucker - Item Selection"));
        this.parent = parent;
        this.config = ItemSuckerMod.getConfig();
    }

    @Override
    protected void init() {
        columns = Math.max(6, (this.width - 40) / CELL);

        // Row 1: filter mode, clear list, movement mode.
        modeButton = Button.builder(modeLabel(), b -> {
            config.mode = config.mode == ItemSuckerConfig.Mode.WHITELIST
                    ? ItemSuckerConfig.Mode.BLACKLIST
                    : ItemSuckerConfig.Mode.WHITELIST;
            b.setMessage(modeLabel());
        }).bounds(this.width / 2 - 150, 6, 100, 18).build();
        addRenderableWidget(modeButton);

        addRenderableWidget(Button.builder(Component.literal("Clear List"), b -> {
            (config.mode == ItemSuckerConfig.Mode.WHITELIST ? config.whitelist : config.blacklist).clear();
        }).bounds(this.width / 2 - 46, 6, 92, 18).build());

        addRenderableWidget(Button.builder(moveModeLabel(), b -> {
            if (config.moveMode == ItemSuckerConfig.MoveMode.TELEPORT) {
                if (com.itemsucker.BaritoneBridge.IS_AVAILABLE) {
                    config.moveMode = ItemSuckerConfig.MoveMode.BARITONE;
                }
            } else {
                config.moveMode = ItemSuckerConfig.MoveMode.TELEPORT;
            }
            b.setMessage(moveModeLabel());
            if (returnButton != null) returnButton.setMessage(returnLabel());
        }).bounds(this.width / 2 + 50, 6, 100, 18).build());

        enabledButton = Button.builder(enabledLabel(), b -> {
            ItemSuckerMod.setEnabled(!config.enabled);
            b.setMessage(enabledLabel());
        }).bounds(this.width / 2 - 150, 28, 100, 18).build();
        addRenderableWidget(enabledButton);

        returnButton = Button.builder(returnLabel(), b -> {
            config.setReturnToOrigin(!config.isReturnToOrigin());
            b.setMessage(returnLabel());
        }).bounds(this.width / 2 + 50, 28, 100, 18).build();
        addRenderableWidget(returnButton);

        notifyButton = Button.builder(notifyLabel(), b -> {
            config.showPickupNotifications = !config.showPickupNotifications;
            b.setMessage(notifyLabel());
        }).bounds(this.width / 2 - 46, 28, 92, 18).build();
        addRenderableWidget(notifyButton);

        searchBox = new EditBox(this.font, this.width / 2 - 100, 50, 200, 18, Component.literal("Search"));
        searchBox.setResponder(text -> refreshFilter());
        addRenderableWidget(searchBox);

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            config.save();
            onClose();
        }).bounds(this.width / 2 - 60, this.height - 28, 120, 20).build());

        refreshFilter();
    }

    private Component modeLabel() {
        return Component.literal("Filter: " + (config.mode == ItemSuckerConfig.Mode.WHITELIST ? "Whitelist" : "Blacklist"));
    }

    private Component moveModeLabel() {
        String label = config.moveMode == ItemSuckerConfig.MoveMode.BARITONE ? "Baritone" : "Teleport";
        return Component.literal("Movement: " + label);
    }

    private Component enabledLabel() {
        return Component.literal(config.enabled ? "Enabled: §aON" : "Enabled: §cOFF");
    }

    private Component returnLabel() {
        return Component.literal(config.isReturnToOrigin() ? "Return: §aON" : "Return: §cOFF");
    }

    private Component notifyLabel() {
        return Component.literal(config.showPickupNotifications ? "Notifications: §aON" : "Notifications: §cOFF");
    }

    private void refreshFilter() {
        String query = searchBox == null ? "" : searchBox.getValue().toLowerCase();
        filteredItems = BuiltInRegistries.ITEM.stream()
                .filter(item -> query.isEmpty() || item.getName(item.getDefaultInstance()).getString().toLowerCase().contains(query)
                        || BuiltInRegistries.ITEM.getKey(item).getPath().contains(query))
                .collect(Collectors.toList());
        scrollRows = 0;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.centeredText(this.font, this.title, this.width / 2, 74, 0xFFFFFF);
        graphics.centeredText(this.font,
                Component.literal("§7Reopen with M (rebindable in Options > Controls)"),
                this.width / 2, 86, 0xFFA6A6A6);

        int gridWidth = columns * CELL;
        int startX = this.width / 2 - gridWidth / 2;
        int maxRows = (this.height - GRID_TOP - 40) / CELL;

        int index = scrollRows * columns;
        for (int row = 0; row < maxRows && index < filteredItems.size(); row++) {
            for (int col = 0; col < columns && index < filteredItems.size(); col++, index++) {
                Item item = filteredItems.get(index);
                int x = startX + col * CELL;
                int y = GRID_TOP + row * CELL;

                boolean selected = config.isSelected(item);
                if (selected) {
                    int highlight = config.mode == ItemSuckerConfig.Mode.WHITELIST
                            ? WHITELIST_HIGHLIGHT
                            : BLACKLIST_HIGHLIGHT;
                    graphics.fill(x, y, x + CELL, y + CELL, highlight);
                    graphics.fill(x + 2, y + 2, x + CELL - 2, y + CELL - 2, HIGHLIGHT_INNER);
                }

                ItemStack stack = new ItemStack(item);
                graphics.item(stack, x + 1, y + 1);

                if (mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL) {
                    graphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int gridWidth = columns * CELL;
        int startX = this.width / 2 - gridWidth / 2;
        int maxRows = (this.height - GRID_TOP - 40) / CELL;

        if (mouseY >= GRID_TOP && mouseY < GRID_TOP + maxRows * CELL && mouseX >= startX && mouseX < startX + gridWidth) {
            int col = (int) ((mouseX - startX) / CELL);
            int row = (int) ((mouseY - GRID_TOP) / CELL);
            int index = (scrollRows + row) * columns + col;

            if (index >= 0 && index < filteredItems.size()) {
                config.toggleItem(filteredItems.get(index));
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxRows = (this.height - GRID_TOP - 40) / CELL;
        int totalRows = (int) Math.ceil(filteredItems.size() / (double) columns);
        int maxScroll = Math.max(0, totalRows - maxRows);

        scrollRows = Math.max(0, Math.min(maxScroll, scrollRows - (int) Math.signum(verticalAmount)));
        return true;
    }

    @Override
    public void onClose() {
        config.save();
        this.minecraft.setScreenAndShow(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
