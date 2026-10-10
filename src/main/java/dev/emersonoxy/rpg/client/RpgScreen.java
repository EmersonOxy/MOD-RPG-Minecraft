package dev.emersonoxy.rpg.client;

import dev.emersonoxy.rpg.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.KeyEvent;

public final class RpgScreen extends Screen {
    private RpgNetwork.State state;
    private final long[] readyAt = new long[3];
    private final Button[] abilities = new Button[3], classes = new Button[RpgClass.values().length-1];
    private String notice = "Carregando perfil...";
    private RpgClass selected;
    private boolean pending;
    private int ticks, pendingTicks, left, top, panelWidth, panelHeight;
    private Button confirm, tree, missions, attributes;
    public RpgScreen() { super(Component.literal("Oxy RPG")); }
    @Override public boolean isPauseScreen() { return false; }
    @Override public boolean keyPressed(KeyEvent event) {
        if (OxyRpgClient.OPEN_MENU.matches(event)) { onClose(); return true; }
        return super.keyPressed(event);
    }
    @Override protected void init() {
        panelWidth = Math.min(420, width - 16);
        left = (width - panelWidth) / 2;
        panelHeight = Math.min(274, height - 8);
        top = Math.max(4, (height - panelHeight) / 2);
        int buttonWidth = (panelWidth - 24 - (classes.length-1)*4) / classes.length;
        for (int i = 0; i < classes.length; i++) {
            final RpgClass choice = RpgClass.values()[i + 1];
            classes[i] = addRenderableWidget(Button.builder(Component.literal(choice.label()), b -> {
                selected = choice;
                notice = "Confirme " + choice.label() + ". O reset por comando volta ao nível 1.";
                updateButtons();
            }).tooltip(Tooltip.create(Component.literal(ClassRules.description(choice))))
                .bounds(left + 12 + i * (buttonWidth + 4), top + row(123), buttonWidth, 20).build());
        }
        confirm = addRenderableWidget(Button.builder(Component.literal("Confirmar classe"), b -> {
            if (selected != null) request(selected==RpgClass.NECROMANTE?7:selected==RpgClass.XAMA?10:selected.ordinal()+1);
        }).bounds(left + 12, top + row(147), panelWidth - 24, 20).build());
        missions = addRenderableWidget(Button.builder(Component.literal("Diário de missões"), b -> {
            if (state != null) minecraft.setScreen(new MissionScreen(this, state));
        }).bounds(left + 12, top + row(67), (panelWidth - 28)/2, 16).build());
        addRenderableWidget(Button.builder(Component.literal("Profissões e contratos"),b->minecraft.setScreen(new ProfessionsScreen(this,OxyRpgClient.professions())))
            .bounds(left+16+(panelWidth-28)/2,top+row(67),(panelWidth-28)/2,16).build());
        tree = addRenderableWidget(Button.builder(Component.literal("Árvore de skills"), b -> {
            if (state != null) minecraft.setScreen(new SkillTreeScreen(this, state));
        }).bounds(left + 12, top + row(87), (panelWidth - 28)/2, 16).build());
        attributes=addRenderableWidget(Button.builder(Component.literal("Atributos"),b->minecraft.setScreen(new AttributesScreen(this,OxyRpgClient.resources())))
            .bounds(left+16+(panelWidth-28)/2,top+row(87),(panelWidth-28)/2,16).build());
        for (int slot = 0; slot < 3; slot++) {
            final int index = slot;
            abilities[slot] = addRenderableWidget(Button.builder(Component.literal("Habilidade " + (slot + 1)), b -> {
                pending = true; pendingTicks = 0; notice = "Aguardando resposta...";
                OxyRpgClient.cast(index); updateButtons();
            }).bounds(left + 12, top + row(173 + slot * 24), panelWidth - 24, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Fechar"), b -> onClose())
            .bounds(left + 12, top + row(247), (panelWidth - 28)/2, 20).build()); addRenderableWidget(Button.builder(Component.literal("Câmera"),b->minecraft.setScreen(new CameraSettingsScreen(this))).bounds(left+16+(panelWidth-28)/2,top+row(247),(panelWidth-28)/2,20).build());
        updateButtons(); request(0);
    }
    private int row(int y) { return y * panelHeight / 274; }
    private long gameTime() { return minecraft.level == null ? 0 : minecraft.level.getGameTime(); }
    private void request(int action) {
        if (minecraft.player == null) return;
        if (action != 0) { pending = true; pendingTicks = 0; notice = "Aguardando resposta..."; updateButtons(); }
        minecraft.player.connection.send(new RpgNetwork.Request(action));
    }
    public void update(RpgNetwork.State value) {
        state = value;
        for(int slot=0;slot<3;slot++)readyAt[slot]=gameTime()+value.cooldown(slot);
        pending = false;
        if (!value.notice().isEmpty()) notice = value.notice();
        else if (notice.equals("Carregando perfil...")) notice = "Três ramos, três habilidades. Abra a árvore para evoluir.";
        updateButtons();
    }
    private void updateButtons() {
        boolean choosing = state != null && state.rpgClass() == 0 && !pending;
        for (Button b : classes) if (b != null) b.active = choosing;
        if (confirm != null) {confirm.active = choosing && selected != null;confirm.setMessage(Component.literal(selected == null ? "Confirmar classe" : "Confirmar " + selected.label()));}
        for(int slot=0;slot<3;slot++)if(abilities[slot]!=null){
            long remaining=Math.max(0,readyAt[slot]-gameTime());
            boolean usable=state!=null&&state.canUse(slot);
            abilities[slot].active=usable&&!pending&&(remaining==0||(slot==2&&state.rpgClass()==RpgClass.ARQUEIRO.ordinal()&&state.hasSkill(20)&&remaining>740));
            String name=state==null||state.rpgClass()==0?"Habilidade "+(slot+1):SkillTree.abilityName(RpgClass.values()[state.rpgClass()],slot);
            int cost=state==null||state.rpgClass()==0?0:RpgResources.cost(new RpgProfile(RpgClass.values()[state.rpgClass()],100,0,0,0,state.skills()),slot,slot==2&&state.rpgClass()==RpgClass.ARQUEIRO.ordinal()&&state.hasSkill(20)&&remaining>740);
            if(state!=null&&minecraft.player!=null)cost=ClassEquipment.discount(minecraft.player,RpgClass.values()[state.rpgClass()],cost);
            boolean free=state!=null&&state.debug();
            if(free)cost=0;
            var resource=OxyRpgClient.resources();boolean enough=resource==null||resource.mana()>=cost;
            abilities[slot].active=abilities[slot].active&&enough;
            String status=!usable?"bloqueada":!enough?"sem mana":remaining>0?((remaining+19)/20)+" s":"usar";
            abilities[slot].setMessage(Component.literal((slot+1)+" • "+name+" • "+cost+" MP • "+status));
        }
        if(tree!=null){tree.active=state!=null&&!pending;int points=state==null?0:Math.max(0,state.level()/5-Integer.bitCount(state.skills()));tree.setMessage(Component.literal("Árvore de skills • "+(state!=null&&state.debug()?"DEBUG":points+" pontos")));}
        if(attributes!=null){attributes.active=state!=null&&state.rpgClass()!=0&&!pending;var resource=OxyRpgClient.resources();attributes.setMessage(Component.literal("Atributos"+(resource==null?"":" • "+resource.points())));}
        if(missions!=null)missions.active=state!=null&&!pending;
    }
    @Override public void tick() {
        if(minecraft.player==null||minecraft.level==null){onClose();return;}
        if(pending&&++pendingTicks>=100){pending=false;notice="Resposta atrasada. Atualizando...";request(0);}
        updateButtons();if(++ticks%40==0&&!pending)request(0);
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partialTick){
        VanillaTheme.panel(g,left,top,panelWidth,panelHeight);
        g.drawCenteredString(font,title,width/2,top+row(10),0xFF53D5BF);
        String status=state==null?"Conectando...":RpgClass.values()[state.rpgClass()].label()+" • Nível "+state.level();
        g.drawCenteredString(font,status,width/2,top+row(28),0xFFFFFFFF);
        if(state!=null){int next=state.level()==100?0:state.level()*100;g.drawString(font,next==0?"Nível máximo!":"XP "+state.xp()+" / "+next,left+12,top+row(45),0xFFE1EAF0);
            VanillaTheme.progress(g,left+12,top+row(57),panelWidth-24,next==0?1:state.xp(),next==0?1:next);}
        g.drawString(font,font.plainSubstrByWidth(notice,panelWidth-24),left+12,top+row(109),0xFFB7C8D7);
        super.render(g,mouseX,mouseY,partialTick);
    }
}










