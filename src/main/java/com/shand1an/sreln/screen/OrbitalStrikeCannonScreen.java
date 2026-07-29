package com.shand1an.sreln.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class OrbitalStrikeCannonScreen extends AbstractContainerScreen<OrbitalStrikeCannonMenu> {

    public OrbitalStrikeCannonScreen(OrbitalStrikeCannonMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
        this.titleLabelY = 8;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.sreln_mod.scan"), btn -> {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }).pos(this.leftPos + 10, this.topPos + 40).size(50, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.sreln_mod.fire"), btn -> {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
        }).pos(this.leftPos + 116, this.topPos + 40).size(50, 20).build());
    }

    @Override
    public void containerTick() {
        super.containerTick();
        for (var widget : this.renderables) {
            if (widget instanceof Button button && button.getMessage().getString()
                    .equals(Component.translatable("gui.sreln_mod.fire").getString())) {
                button.active = !this.menu.blockEntity.isExhausted() && !this.menu.blockEntity.isFiring();
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xC0101010);
        guiGraphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + this.imageHeight - 1, 0xC0202020);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = this.leftPos + 10;
        int y = this.topPos + 60;
        BlockPos target = this.menu.blockEntity.getTargetPos();
        String targetText = target != null
                ? Component.translatable("gui.sreln_mod.target", target.getX(), target.getY(), target.getZ()).getString()
                : Component.translatable("gui.sreln_mod.no_target").getString();
        guiGraphics.drawString(this.font, targetText, x, y, 0xFFFFFF);

        String usesText = Component.translatable("gui.sreln_mod.uses", this.menu.blockEntity.getUseCount()).getString();
        guiGraphics.drawString(this.font, usesText, x, y + 15, this.menu.blockEntity.getUseCount() > 0 ? 0x55FF55 : 0xFF5555);

        String statusText = this.menu.blockEntity.isFiring()
                ? Component.translatable("gui.sreln_mod.firing").getString()
                : this.menu.blockEntity.isExhausted()
                ? Component.translatable("gui.sreln_mod.exhausted").getString()
                : Component.translatable("gui.sreln_mod.ready").getString();
        int statusColor = this.menu.blockEntity.isFiring() ? 0xFF5555
                : this.menu.blockEntity.isExhausted() ? 0xFF5555 : 0x55FF55;
        guiGraphics.drawString(this.font, statusText, x, y + 30, statusColor);
    }
}