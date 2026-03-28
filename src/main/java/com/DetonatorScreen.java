package com;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class DetonatorScreen extends AbstractContainerScreen<DetonatorMenu> {
    private final ArrayList<Button> removeButtons = new ArrayList<>();
    private int scrollOffset = 0;
    private int maxLines = 1;
    private Button detonateAllButton;

    public DetonatorScreen(DetonatorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 120;
        this.inventoryLabelY = this.imageHeight + 6;
    }

    @Override
    protected void init() {
        super.init();
        syncLayout();
        scrollToBottom();
        addDetonateButton();
        rebuildButtons();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        guiGraphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xCC111111);
        guiGraphics.fill(left + 1, top + 1, left + this.imageWidth - 1, top + this.imageHeight - 1, 0xCC222222);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<BlockPos> positions = this.menu.getPositions();
        int maxOffset = Math.max(0, positions.size() - maxLines);
        if (maxOffset <= 0) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

        int next = scrollOffset + (scrollY < 0 ? 1 : -1);
        scrollOffset = Math.max(0, Math.min(maxOffset, next));
        rebuildButtons();
        return true;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);

        List<BlockPos> positions = this.menu.getPositions();
        guiGraphics.drawString(this.font, Component.literal("Stored blocks: " + positions.size()), 8, 18, 0xCFCFCF, false);

        int y = 30;
        int endIndex = Math.min(positions.size(), scrollOffset + maxLines);
        for (int i = scrollOffset; i < endIndex; i++) {
            BlockPos p = positions.get(i);
            String line = (i + 1) + ". " + p.getX() + ", " + p.getY() + ", " + p.getZ();
            guiGraphics.drawString(this.font, line, 8, y, 0xFFFFFF, false);
            y += this.font.lineHeight + 1;
        }
    }

    public void applyPositions(List<BlockPos> positions) {
        this.menu.setPositions(positions);
        syncLayout();
        scrollOffset = Math.min(scrollOffset, Math.max(0, positions.size() - maxLines));
        if (detonateAllButton != null) detonateAllButton.active = !positions.isEmpty();
        rebuildButtons();
    }

    private void syncLayout() {
        int y = 30;
        int lineH = this.font.lineHeight + 1;
        this.maxLines = Math.max(1, (this.imageHeight - y - 8) / lineH);
    }

    private void scrollToBottom() {
        List<BlockPos> positions = this.menu.getPositions();
        this.scrollOffset = Math.max(0, positions.size() - maxLines);
    }

    private void rebuildButtons() {
        for (Button b : removeButtons) {
            this.removeWidget(b);
        }
        removeButtons.clear();

        List<BlockPos> positions = this.menu.getPositions();
        int endIndex = Math.min(positions.size(), scrollOffset + maxLines);

        int y = 30;
        int lineH = this.font.lineHeight + 1;

        int btnX = this.leftPos + this.imageWidth - 16;
        for (int i = scrollOffset; i < endIndex; i++) {
            int idx = i;
            int btnY = this.topPos + y - 1;
            Button b = Button.builder(Component.literal("X"), (btn) -> {
                ModNet.CHANNEL.send(new RemoveDetonatorPosC2SPacket(idx), net.minecraftforge.network.PacketDistributor.SERVER.noArg());
            }).pos(btnX, btnY).size(14, 12).build();
            removeButtons.add(b);
            this.addRenderableWidget(b);
            y += lineH;
        }
    }

    private void addDetonateButton() {
        if (detonateAllButton != null) {
            this.removeWidget(detonateAllButton);
        }
        boolean hasAny = !this.menu.getPositions().isEmpty();
        detonateAllButton = Button.builder(Component.literal("Detonate All"), (btn) -> {
            ModNet.CHANNEL.send(new DetonateAllC2SPacket(), net.minecraftforge.network.PacketDistributor.SERVER.noArg());
        }).pos(this.leftPos + 8, this.topPos + 92).size(90, 16).build();
        detonateAllButton.active = hasAny;
        this.addRenderableWidget(detonateAllButton);
    }
}
