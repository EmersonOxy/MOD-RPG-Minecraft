package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.QuestCatalog;
import dev.emersonoxy.rpg.RpgNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class MissionScreen extends Screen {
    private final Screen parent;
    private RpgNetwork.State state;
    private int left, top, panelWidth, ticks;
    public MissionScreen(Screen parent, RpgNetwork.State state) {
        super(Component.literal("Diário de missões"));
        this.parent = parent;
        this.state = state;
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { minecraft.setScreen(parent); }
    public void update(RpgNetwork.State state) { this.state = state; }
    @Override protected void init() {
        panelWidth = Math.min(400, width - 16);
        left = (width - panelWidth) / 2;
        top = Math.max(4, (height - 226) / 2);
        addRenderableWidget(Button.builder(Component.literal("Voltar"), b -> onClose())
            .bounds(left + 12, top + 198, panelWidth - 24, 20).build());
        if (minecraft.player != null) minecraft.player.connection.send(new RpgNetwork.Request(0));
    }
    @Override public void tick() {
        if (minecraft.player == null || minecraft.level == null) { minecraft.setScreen(null); return; }
        if (++ticks % 40 == 0) minecraft.player.connection.send(new RpgNetwork.Request(0));
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        var quest = QuestCatalog.quest(state.questStage());
        VanillaTheme.panel(g,left,top,panelWidth,226);

        g.drawCenteredString(font, title, width / 2, top + 10, 0xFFFFFFFF);
        g.drawCenteredString(font, quest.title(), width / 2, top + 29, 0xFFFFCC79);
        for (int i = 0; i < quest.targets().size(); i++) {
            var target = quest.targets().get(i);
            int progress = i == 0 ? state.questFirst() : state.questSecond();
            int y = top + 52 + i * 42;
            var item = BuiltInRegistries.ITEM.getValue(ResourceLocation.withDefaultNamespace(target.icon()));
            g.renderFakeItem(new ItemStack(item == null ? Items.PAPER : item), left + 14, y);
            g.drawString(font, target.label() + ": " + progress + "/" + target.amount(), left + 40, y + 2, 0xFFE1EAF0);
            g.fill(left + 40, y + 16, left + panelWidth - 14, y + 21, 0xFF313B4B);
            int filled = (panelWidth - 54) * progress / target.amount();
            g.fill(left + 40, y + 16, left + 40 + filled, y + 21, progress == target.amount() ? 0xFFFFCC79 : 0xFF53D5BF);
        }
        g.drawString(font, "Recompensa: +" + quest.reward() + " XP RPG", left + 12, top + 137, 0xFFFFCC79);
        g.drawString(font, "Caçada geral: " + state.kills() % 10 + "/10 • +100 XP", left + 12, top + 151, 0xFFE1EAF0);
        g.drawString(font, state.rpgClass() == 0 ? "Escolha uma classe para começar." : "Todos os alvos devem ser concluídos.", left + 12, top + 166, 0xFFB7C8D7);
        String next = "Próxima: " + QuestCatalog.quest(state.questStage() + 1).title();
        g.drawString(font, font.plainSubstrByWidth(next, panelWidth - 24), left + 12, top + 180, 0xFFB7C8D7);
        super.render(g, mouseX, mouseY, partialTick);
    }
}

