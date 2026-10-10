package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.RpgClass;
import dev.emersonoxy.rpg.RpgNetwork;
import dev.emersonoxy.rpg.SkillTree;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.List;

/** Advancement-style connections and vanilla frames; each node has its own item icon. */
public final class SkillTreeScreen extends Screen {
    private final java.util.List<SkillButton> skillButtons = new java.util.ArrayList<>();
    private int scroll;
    private final Screen parent;
    private RpgNetwork.State state;
    private RpgClass viewed;
    private int selected;
    private int left, top, panelWidth, panelHeight;
    private int ticks, waitingTicks;
    private boolean waiting;
    private boolean feedback;
    private String notice = "Cada melhoria custa 1 ponto. Você ganha 1 ponto a cada 5 níveis.";
    private Button learn;
    private List<SkillTree.Node> nodes;

    public SkillTreeScreen(Screen parent, RpgNetwork.State state) {
        super(Component.literal("Árvore de skills"));
        this.parent = parent;
        this.state = state;
        viewed = state.rpgClass() == 0 ? RpgClass.GUERREIRO : RpgClass.values()[state.rpgClass()];
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override protected void init() {
        panelWidth = Math.min(520, width - 16);
        left = (width - panelWidth) / 2;
        panelHeight = Math.min(400, height - 8);
        top = Math.max(4, (height - panelHeight) / 2);
        nodes = SkillTree.nodes(viewed);
        int classCount=RpgClass.values().length-1;
        int tabWidth = (panelWidth - 24 - (classCount-1)*4) / classCount;
        for (int i = 0; i < classCount; i++) {
            final RpgClass tab = RpgClass.values()[i + 1];
            var button = addRenderableWidget(Button.builder(Component.literal(tab.label()), b -> {
                viewed = tab;
                selected = 0; scroll = 0;
                rebuildWidgets();
            }).bounds(left + 12 + i * (tabWidth + 4), top + 27, tabWidth, 20).build());
            button.active = tab != viewed;
        }
        skillButtons.clear();
        for (var node : nodes) skillButtons.add(addRenderableWidget(new SkillButton(node, nodeX(node.id()), nodeY(node.id()))));
        positionNodes();
        learn = addRenderableWidget(Button.builder(Component.literal("Aprender skill"), b -> {
            if (canLearn(selected) && !waiting && minecraft.player != null) {
                waiting = true;
                waitingTicks = 0;
                notice = "Aguardando confirmação do servidor...";
                minecraft.player.connection.send(new RpgNetwork.Request(100 + selected));
                updateButton();
            }
        }).bounds(left + 12, top + panelHeight - 28, (panelWidth - 28) / 2, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"), b -> onClose())
            .bounds(left + 16 + (panelWidth - 28) / 2, top + panelHeight - 28, (panelWidth - 28) / 2, 20).build());
        updateButton();
        if (minecraft.player != null) minecraft.player.connection.send(new RpgNetwork.Request(0));
    }
    private boolean ownTree() { return state.rpgClass() == viewed.ordinal(); }
    private boolean unlocked(int id) { return ownTree() && (id < 2 || (state.skills() & (1 << id)) != 0); }
    private int points() { return Math.max(0, state.level() / 5 - Integer.bitCount(state.skills())); }
    private boolean canLearn(int id) {
        return ownTree() && id >= 2 && !unlocked(id) && (state.debug() || (points() > 0 && state.level() >= SkillTree.requiredLevel(id)))
            && ((state.skills() | SkillTree.FREE_MASK) & SkillTree.parents(id)) == SkillTree.parents(id);
    }
    private int nodeX(int id) {
        int branch = id / 7, step = id % 7;
        int center = left + 12 + (int)((branch + .5) * (panelWidth - 24) / 3);
        return center - 13 + (step == 4 ? -24 : step == 5 ? 24 : 0);
    }
    private int nodeY(int id) { return top + 58 + new int[] {6,5,4,3,2,2,0}[id % 7] * 36 - scroll; }
    private void positionNodes() {
        int available = panelHeight - 146;
        scroll = Math.max(0, Math.min(scroll, Math.max(0, 250 - available)));
        for (var button : skillButtons) {
            button.setPosition(nodeX(button.node.id()), nodeY(button.node.id()));
            button.visible = button.getY() >= top + 52 && button.getY() + 28 <= top + panelHeight - 88;
        }
    }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (x >= left && x <= left + panelWidth && y >= top + 52 && y < top + panelHeight - 88) {
            scroll -= (int)(vertical * 24); positionNodes(); return true;
        }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }    private String status(int id) {
        if (!ownTree()) return "Prévia: escolha esta classe para usar esta árvore.";
        if (unlocked(id)) return id < 2 ? "Habilidade inicial — já disponível." : "Skill aprendida.";
        if (canLearn(id)) return state.debug() ? "DEBUG: aprender temporariamente, sem gastar pontos." : "Disponível: custa 1 ponto de skill.";
        if (!state.debug() && state.level() < SkillTree.requiredLevel(id)) return "Requer nível " + SkillTree.requiredLevel(id) + ".";
        if (((state.skills() | SkillTree.FREE_MASK) & SkillTree.parents(id)) != SkillTree.parents(id)) return "Requer todas as skills conectadas anteriores.";
        return "Sem pontos. Ganhe 1 ponto a cada 5 níveis.";
    }
    private void updateButton() {
        if (learn != null) { learn.active = !waiting && canLearn(selected); learn.setMessage(Component.literal(waiting ? "Aguardando..." : state.debug() ? "Aprender (DEBUG)" : "Aprender skill (1 ponto)")); }
    }
    public void update(RpgNetwork.State value) {
        state = value;
        waiting = false;
        if (!value.notice().isEmpty()) { notice = value.notice(); feedback = true; }
        updateButton();
    }
    @Override public void tick() {
        if (minecraft.player == null || minecraft.level == null) { minecraft.setScreen(null); return; }
        if (waiting && ++waitingTicks >= 100) { waiting = false; notice = "Resposta atrasada. Atualizando..."; }
        if (++ticks % 40 == 0 && !waiting) minecraft.player.connection.send(new RpgNetwork.Request(0));
        updateButton();
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        VanillaTheme.panel(g,left,top,panelWidth,panelHeight);

        String budget = state.debug() ? "DEBUG • pontos livres" : points() + (points() == 1 ? " ponto" : " pontos");
        g.drawCenteredString(font, title.getString() + " • " + budget, width / 2, top + 10, 0xFFFFFFFF);
        int canvasBottom = top + panelHeight - 88;
        g.enableScissor(left + 8, top + 52, left + panelWidth - 8, canvasBottom);
        var stone = ResourceLocation.withDefaultNamespace("textures/block/stone.png");
        for (int x = left + 8; x < left + panelWidth - 8; x += 16)
            for (int y = top + 52; y < canvasBottom; y += 16)
                g.blit(RenderPipelines.GUI_TEXTURED, stone, x, y, 0, 0, 16, 16, 16, 16);
        g.fill(left + 8, top + 52, left + panelWidth - 8, canvasBottom, 0x70000000);
        for (var node : nodes) {
            for (int parentId = 0; parentId < SkillTree.COUNT; parentId++) {
                if ((node.parents() & (1 << parentId)) == 0) continue;
                int x1 = nodeX(parentId) + 13, y1 = nodeY(parentId) + 13;
                int x2 = nodeX(node.id()) + 13, y2 = nodeY(node.id()) + 13;
                int color = unlocked(node.id()) ? 0xFFFFCC55 : 0xFFAAAAAA;
                curve(g, x1, y1, x2, y2, 0xFF111111, 3);
                curve(g, x1, y1, x2, y2, color, 1);
            }
        }
        g.disableScissor();
        var node = nodes.get(selected);
        g.drawString(font, node.title() + " • Nv. " + node.level(), left + 12, top + panelHeight - 82, 0xFFFFCC79);
        var lines = font.split(Component.literal(node.description()), panelWidth - 24);
        for (int i = 0; i < Math.min(2, lines.size()); i++) g.drawString(font, lines.get(i), left + 12, top + panelHeight - 69 + i * 10, 0xFFE1EAF0);
        String text = waiting || feedback ? notice : status(selected);
        g.drawString(font, font.plainSubstrByWidth(text, panelWidth - 24), left + 12, top + panelHeight - 42, 0xFFB7C8D7);
        super.render(g, mouseX, mouseY, partialTick);
    }
    private static void curve(GuiGraphics g, int x1, int y1, int x2, int y2, int color, int thickness) {
        double bend = Math.min(26, Math.abs(y2 - y1) / 2.0);
        int previousX = x1, previousY = y1;
        for (int i = 1; i <= 64; i++) {
            double t = i / 64.0, u = 1 - t;
            int x = (int) Math.round(u * u * u * x1 + 3 * u * u * t * x1 + 3 * u * t * t * x2 + t * t * t * x2);
            int y = (int) Math.round(u * u * u * y1 + 3 * u * u * t * (y1 - bend) + 3 * u * t * t * (y2 + bend) + t * t * t * y2);
            int steps = Math.max(1, Math.max(Math.abs(x - previousX), Math.abs(y - previousY)));
            for (int step = 0; step <= steps; step++) {
                int px = previousX + (x - previousX) * step / steps, py = previousY + (y - previousY) * step / steps;
                g.fill(px - thickness / 2, py - thickness / 2, px - thickness / 2 + thickness, py - thickness / 2 + thickness, color);
            }
            previousX = x; previousY = y;
        }
    }
    private final class SkillButton extends Button {
        private final SkillTree.Node node;
        private final ItemStack icon;
        private String tooltipText = "";
        SkillButton(SkillTree.Node node, int x, int y) {
            super(x, y, 26, 26, Component.literal(node.title()), button -> {
                selected = node.id();
                feedback = false;
                notice = status(selected);
                updateButton();
            }, DEFAULT_NARRATION);
            this.node = node;
            var item = BuiltInRegistries.ITEM.getValue(ResourceLocation.withDefaultNamespace(node.icon()));
            icon = new ItemStack(item == null ? Items.BOOK : item);
        }
        @Override protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            String kind = node.id() % 7 == 6 ? "challenge" : node.id() % 7 == 3 ? "goal" : "task";
            var frame = ResourceLocation.withDefaultNamespace("advancements/" + kind + "_frame_" + (unlocked(node.id()) ? "obtained" : "unobtained"));
            int tint = selected == node.id() || isHoveredOrFocused() ? 0xFFFFE8B5 : 0xFFFFFFFF;
            g.blitSprite(RenderPipelines.GUI_TEXTURED, frame, getX(), getY(), 26, 26, tint);
            g.renderFakeItem(icon, getX() + 5, getY() + 5);
            if (selected == node.id()) g.fill(getX() + 7, getY() + 27, getX() + 19, getY() + 28, 0xFFFFCC79);
            String text = node.title() + "\n" + node.description() + "\n" + status(node.id());
            if (!text.equals(tooltipText)) {
                tooltipText = text;
                setTooltip(Tooltip.create(Component.literal(text)));
            }
        }
    }
}




