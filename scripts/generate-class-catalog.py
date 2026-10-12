"""Generate the human catalogue from shipped class definitions, preserving every node ID."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
ORDER = ['guerreiro', 'mago', 'arqueiro', 'arqueiro_novo', 'necromante', 'xama',
         'aviario', 'ilusionista', 'hidromante', 'piromante', 'vampiresco', 'deus_trovao']
# Runtime traits live outside the presentation datapack; record their actual conditions explicitly.
TRAITS = {
 'guerreiro': ('Corpo a corpo +5%. Talentos compráveis acrescentam vida, roubo de vida com intervalo e resistência a empurrão.',
               'Não usa arco, besta ou tridente; fome adicional. Bastião também desacelera.',
               'Medir a cura por segundo em grupos e limitar o quanto múltiplos alvos sustentam o Vampirismo.'),
 'mago': ('Dano de congelamento recebido -10%; dano mágico contra criaturas congeladas +10%. Talentos acrescentam dano mágico e resistência a empurrão.',
          '-1 coração base; recebe +20% de dano corpo a corpo de criaturas hostis; depende de mana e recargas.',
          'Ter resistência crescente a paralisia nos chefes e uma opção de servidor para impedir Tremor em áreas protegidas.'),
 'arqueiro': ('Treino de adaga: corpo a corpo +5%. Talentos de passos leves e agilidade são compráveis. Furtividade é uma habilidade ativável; não há camuflagem automática ao agachar nesta classe.',
             'Somente armaduras leves: couro e elytra por padrão; projéteis recebidos causam +25% de dano. Outros mods podem incluir peças na tag oxyrpg:light_armor.',
             'Especializar em adagas e ataques pelas costas, dando contrajogo de revelação à furtividade em PvP.'),
 'arqueiro_novo': ('Mantém Mira treinada: flechas +5%. Passos leves desbloqueia camuflagem ao agachar por 2 s e menor detecção; Agilidade concede +3% de velocidade. Flechas também podem destacar criaturas pelo talento inicial.',
                  '-2 corações base; não usa escudo. Rajada e chuva exigem uma flecha por disparo; cobertura sólida bloqueia flechas guiadas.',
                  'Limitar o rastreio contra jogadores por alcance/tempo e manter formas de quebrar a perseguição. Comparar dano por mana da rajada com o arco normal.'),
 'necromante': ('Dano de invocações +5%; talentos ampliam vida dos servos e resistência a empurrão. Servos temporários acompanham ataques e têm regras próprias de alvo.',
                'Dano próprio -20%; movimento -15% durante o dia nas dimensões com céu. Depende de mana e de suas invocações.',
                'Orçamento de servos por jogador e por região, para que grupos de necromantes não sobrecarreguem o servidor.'),
 'xama': ('Raios da habilidade +5%. Talentos ampliam a vida dos animais e resistência a empurrão; aura pode curar e limpar condições.',
          'Não consome carne/peixe; armas convencionais causam -10% de dano. Especialista exige ambas as mãos vazias.',
          'Focar suporte e natureza para diferenciar do Deus do Trovão. O nome antigo Deus Relâmpago pode virar Toque da Tempestade.'),
 'aviario': ('Queda comum causa 70% do dano normal. Peso pena, depois de aprendido, freia a queda ao segurar Agachar, sem pose agachada, e pode reduzir dano recebido durante a descida.',
             '-2 corações base; projéteis causam +20% de dano enquanto está no ar. Voo usa janela de duração, mana nos impulsos e velocidade gradual por nível.',
             'Adicionar resistência/peso das criaturas carregadas e custo contínuo para o transporte. Impedir destruição de construções protegidas pelo Meteoro.'),
 'ilusionista': ('Disfarce e cópias distraem criaturas. Mímico preserva variantes, equipamento e proporção de vida da referência; cópias têm navegação própria.',
                '-2 corações base; ataques diretos -20%. Corpo de Eclipse impede atacar, interagir e usar itens; atravessa apenas categorias de blocos liberadas.',
                'Criar criaturas capazes de revelar ilusões e separar claramente engano visual de imunidade, sobretudo em PvP.'),
 'hidromante': ('Respiração aquática, mineração normal submersa e nado melhorado; chuva aumenta dano em 20%, tempestade pode chegar a 40% com talento.',
               'Sem chuva e fora da água, dano causado -10%; dano de fogo recebido fora da água +15%.',
               'Medir seu desempenho em mapas secos, garantindo fontes de água e objetivos onde a especialização aquática tenha valor.'),
 'piromante': ('Caldeira acumula calor com dano/fogo; calor pode aumentar dano. Fênix desbloqueia imunidade a fogo, mobilidade na lava, saltos, brasas e explosão final.',
              '-1 coração base; frio causa +20% de dano. Na água causa -20% de dano e perde calor; atingir 100 de calor esvazia a caldeira e impõe recuperação de 6 s.',
              'Dar ao adversário informação clara do calor e permitir que controle de frio/água interrompa combinações; proteger terreno de contaminação no servidor.'),
 'vampiresco': ('Visão noturna à noite. Alimentação usa o botão direito em criatura pacífica: recupera sede e tem 35% de chance de causar sangramento. Golpes em hostis causam sangramento. Twilight destaca trilhas de sangue.',
                'Sol causa dano e dia aplica Fraqueza; chuva exposta causa dano leve. Comida comum não sacia. Morcego gasta 1 MP/s e não ataca/interage; Twilight aumenta dano recebido em 25%. Guardas/golens reagem à presença noturna.',
                'Definir regras de alimentação/retaliação para NPCs importantes e janelas de imunidade de chefes à paralisia. Verificar empilhamento de cura.'),
 'deus_trovao': ('Após 3 s de corrida contínua em solo seco ganha +15% de velocidade até parar. Água impede nado e o faz afundar; calor extremo acumula superaquecimento.',
                 'Sem nado normal. Após aproximadamente 10 s no Nether ou bioma de temperatura base ≥1,5, sofre 1 ponto de dano por segundo enquanto o calor persiste. Água esfria, mas continua sendo uma fraqueza de mobilidade.',
                 'Reservar o papel ofensivo elétrico para esta classe. Medir dano da corrente por mana e adicionar resistência ao choque em certos chefes.'),
}
COSTS = {'guerreiro':[12,20,25], 'mago':[20,30,35], 'arqueiro':[12,20,15],
         'arqueiro_novo':[18,28,30], 'necromante':[20,40,35], 'xama':[20,35,20],
         'aviario':[20,0,30], 'ilusionista':[28,24,35], 'hidromante':[8,24,25],
         'piromante':[20,16,35], 'vampiresco':[18,24,30], 'deus_trovao':[20,30,28]}
LEVELS = [1,1,5,10,15,20,25,5,10,15,20,25,30,35,10,15,20,30,35,40,45]
def level(i):
    return LEVELS[i] if i < 21 else LEVELS[((i-21)//10)*7+2] if (i-21)%10 < 2 else 30+((i-21)%10-2)*8
def parents(i):
    if i >= 21:
        branch, step = divmod(i-21, 10)
        return [branch*7+2 if step == 0 else i-1 if step == 1 else branch*7+6 if step < 6 else i-4]
    step = i % 7
    return [] if i == 0 else [21+(i//7)*10+1] if step == 3 else [0] if step == 0 else [i-2] if step == 5 else [i-1,i-2] if step == 6 else [i-1]
def cell(s):
    return str(s).replace('|', '\\|').replace('\n', ' ')
def generate():
    definitions = {key:json.loads((ROOT/'src/generated/resources/data/oxyrpg/rpg/classes'/f'{key}.json').read_text(encoding='utf-8')) for key in ORDER}
    lines = ['# Catálogo de classes — Oxy RPG 0.50.0', '',
      'Estado implementado em 12/10/2026. Minecraft 1.21.10, NeoForge 21.10.64; cliente e servidor usam protocolo 33. Compilação não substitui validação em jogo.', '',
      '## Como funciona a progressão', '',
      'São 12 classes, 3 habilidades por classe e 51 nós por árvore. Os nós 0 e 1 são gratuitos; os outros 49 custam um ponto. O jogador recebe 1 ponto a cada 2 níveis. Habilidades começam em versões iniciais: ramo 1 no nível 1, ramo 2 no nível 10 e ramo 3 no nível 15. As habilidades principais exigem nível 10/20/30 e os caminhos de preparação. As tabelas abaixo registram também cada pré-requisito; nível sozinho não libera uma habilidade.', '',
      'No nível 100 existem 50 pontos de habilidade, suficientes para todos os 49 nós pagos. Atualmente a decisão é a ordem de construção da classe, não uma especialização exclusiva no fim da progressão. A árvore de atributos é separada e dá 1 ponto por nível a partir do nível 2; os trabalhos continuam independentes.', '',
      'Valores apresentados são os padrões: dano, mana, duração e recarga podem mudar por atributos, equipamento, config do servidor e datapack. Os 30 nós avançados por classe acrescentam potência (+3% cada), duração (+5%), eficiência de mana (-3%) e redução de recarga (-3%). Preparação e Domínio também acrescentam +3% de dano. Tudo comprado representa +36% de dano da classe, +30% de duração e -18% de mana/recarga, antes dos demais modificadores.', '',
      'Atalhos padrão: R abre o menu, G/V/B ativam habilidades, Z marca/trava uma criatura, Alt executa esquiva. Todos podem ser personalizados nos Controles do jogo. Peso pena é passiva e usa Agachar. A esquiva geral tem 1 a 3 cargas conforme nível; deslocamento máximo de 3 blocos. A Execução do Assassino é uma habilidade distinta, com alcance próprio de até 6,5 blocos.', '',
      '## Visão geral', '', '| Classe | Habilidade 1 | Habilidade 2 | Habilidade 3 |', '|---|---|---|---|']
    for key,d in definitions.items():
        lines.append('| '+ ' | '.join(cell(v) for v in [d['label'], *(d['nodes'][i]['title'] for i in [3,10,17])])+' |')
    lines += ['', '## Classes, passivas, fraquezas e melhorias', '']
    for key,d in definitions.items():
        lines += [f"### {d['label']}", '', f"**Passiva e talentos automáticos:** {TRAITS[key][0]}", '', f"**Pontos negativos atuais:** {TRAITS[key][1]}", '', '| Habilidade principal | Comportamento | Mana base |', '|---|---|---|']
        for slot,i in enumerate([3,10,17]):
            n=d['nodes'][i]
            lines.append(f"| {cell(n['title'])} | {cell(n['description'])} | {COSTS[key][slot]} MP |")
        if key == 'arqueiro':
            lines += ['', 'Os perfis antigos de Arqueiro viram Assassino preservando nível, XP, atributos, nós e treinamentos. O ID interno ARQUEIRO e o ordinal 3 foram mantidos para compatibilidade. O item vinculado antigo é convertido na Adaga das Sombras. O comando público é `/rpg classe assassino`.']
        elif key == 'arqueiro_novo':
            lines += ['', 'Rajada: 2 flechas na versão inicial; 6 na principal, chegando a 10 com melhorias. Chuva: 4 iniciais ou 10 principais, chegando a 14. A munição é reservada antes da mana; sem munição suficiente, o uso é negado. Flechas especiais do inventário são consumidas como munição da habilidade, que define seus próprios efeitos. Caçada permite esquiva no ar sem cargas; flechas guiadas exigem o desbloqueio principal, criatura visível na mira ou marcada, e continuam colidindo com blocos. Explosões da chuva causam dano em criaturas, sem quebrar terreno.']
        elif key == 'deus_trovao':
            lines += ['', 'Teleporte verifica espaço e chunks já carregados; alcance máximo de 20 blocos com as melhorias diretas. O raio aparece no destino e pode ferir criaturas pacíficas e hostis. Corrente segue a mira e, na principal, atinge um alvo mais 2 saltos; o ápice permite mais 2. Choque dos punhos exige ambas as mãos vazias, acrescenta 2 pontos ao golpe e desacelera. Água bloqueia a pose de nado e a velocidade vertical de subida, com simulação nos dois lados. Criativo/espectador preservam seus controles nativos.']
        elif key == 'aviario':
            lines += ['', 'Primeira ativação lança verticalmente; a segunda entra em planagem e aplica impulso. Ao mergulhar perto de terreno/obstáculo, o pouso controlado põe o corpo ereto, reduz deslocamento horizontal e freia a descida. Durante essa transição, queda/colisão de voo não causam dano; quedas comuns mantêm a redução de 30%. Ao voar, marque uma criatura e passe a até 3,5 blocos para carregá-la abaixo do jogador. Destravar a mira ou pousar a solta; dano de queda passa a seguir a regra da criatura. Chefes Wither/Dragão não são transportáveis.']
        elif key == 'vampiresco':
            lines += ['', 'Alimentação normal restaura 4 de sede (6 com o talento correspondente), usa intervalo de 3 s e causa 2 pontos de dano na mordida bem-sucedida. A dica de botão aparece até três alimentações bem-sucedidas; depois permanece só o ícone de presas.']
        lines += ['', f"**Sugestão, ainda não implementada:** {TRAITS[key][2]}", '']
    lines += ['## Desbloqueios completos', '', 'IDs são estáveis e começam em zero. “Exige” lista os nós que precisam estar comprados antes; os dois nós gratuitos já estão disponíveis. A linha descreve o talento da árvore, enquanto o catálogo acima explica regras adicionais da mecânica. Os nomes e descrições vêm das definições enviadas ao jogo.', '']
    for key,d in definitions.items():
        lines += [f"### Árvore de {d['label']}", '', '| Nó | Nível | Exige | Desbloqueio | Efeito |', '|---:|---:|---|---|---|']
        for n in d['nodes']:
            i=n['id'];req='gratuito' if i<2 else ', '.join(map(str,parents(i)))
            lines.append('| '+ ' | '.join(cell(v) for v in [i,level(i),req,n['title'],n['description']])+' |')
        lines.append('')
    lines += ['## Prioridade sugerida para o servidor', '',
      '1. **Definir PvE/PvP, grupos e proteção de terreno.** As habilidades atualmente miram criaturas; golpes vanilla continuam sujeitos às regras do servidor. Centralizar permissões e aliados antes de incluir combate entre classes. Tremor, Meteoro e magma precisam respeitar construções/claims; chuva explosiva do novo Arqueiro já preserva blocos.', '',
      '2. **Criar escolhas de especialização.** Limitar pontos finais ou acrescentar caminhos mutuamente exclusivos. Minha sugestão inicial é três especializações por classe com um ápice escolhido, mantendo versões iniciais úteis. Isso ainda não foi aplicado.', '',
      '3. **Medir curvas de combate em sobrevivência.** Comparar dano por mana/segundo, cura, mobilidade, tempo de controle e tempo de morte nos níveis 1, 10, 30, 60 e 100. Começar por cura do Guerreiro/Vampiro, mobilidade do Aviário, flechas guiadas e correntes elétricas.', '',
      '4. **Dar resistência a chefes e orçamento por região.** Evitar paralisia permanente e limitar invocações/cópias/efeitos simultâneos por área. Partículas elétricas desta versão usam um pacote por arco e geração visual no cliente, em vez de dezenas de pacotes por segmento.', '',
      '5. **Preparar operação.** Clientes precisam da mesma versão do mod e dependências. Debug é de operador (nível de permissão 2); modo de testes mantém custos normais. Backups, atualização com servidor fechado, documentação de datapacks e uma rodada de testes de multiplayer precedem a abertura pública.', '',
      'Esta lista é uma recomendação de evolução; a 0.50.0 não constitui validação de um servidor público pronto.', '']
    output=ROOT/'docs/CATALOGO_CLASSES_050.md';output.write_text('\n'.join(lines),encoding='utf-8')
    print(f'{len(definitions)} classes, {sum(len(d["nodes"]) for d in definitions.values())} nós -> {output.name}')

if __name__ == '__main__':
    generate()
