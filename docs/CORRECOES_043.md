# Ajustes de Vampiresco, ambiente e interface 0.43.0

## Evidência

Inspecionados os 12 prints mais recentes da instância, de 11/10/2026 entre 20:46:39 e 21:10:51: vegetação com cor/forma divergente, aba de trocas desativada no diálogo, sangue/números vermelhos, morcego pequeno e gotas azuis de sede. Imagens de outras regiões não demonstram por si só outro defeito. Dois adendos do usuário incluídos: textos sobrepostos pelas skills e acesso a trocas.

## Vampiresco

- Alimentação usa o atalho nativo de interação (botão direito por padrão), com sede abaixo de 20, alvo pacífico vivo, alcance de três blocos e linha de visão. Invocações/proxies ficam excluídos. Botão direito agachado reserva a interação comum e abre o diálogo de aldeão durante o dia em vez de morder; bandagem tem precedência. Forma de morcego não pode alimentar-se.
- Mordida causa dois pontos de dano, recupera quatro pontos de sede (seis com Paladar aguçado), recarga de três segundos e 35% de chance de sangramento. Recursos só são recuperados se a mordida causar dano. Punhos/armas deixam de recuperar sede; ataques contra hostis continuam causando sangramento conforme a passiva. Sede de Sangue conserva o vínculo por golpes.
- Indicador de gota vermelha e tecla personalizada aparecem ao mirar uma presa ao alcance. Sede na HUD usa vermelho; mana permanece azul.
- Morcego da transformação sobe para 1,15 bloco acima dos pés e ganha escala visual 2,2. Apenas o proxy da forma é ampliado; escolta e morcegos naturais preservados. Não altera o hitbox do jogador.
- Marcas durante Twilight recebem vermelho vivo e luz completa (bloco e céu); fora da habilidade mantêm sangue escuro. Destaque não atravessa paredes.
- Alvos com feridas atribuídas a Vampiresco fogem ou, se hostis, recebem o vampiro como alvo. Aldeões até oito blocos durante a noite acordam e recebem intenção de fuga pela IA/caminho nativos. Diálogos não congelam aldeões assustados; não negociam com vampiros à noite. Obstáculos e casas fechadas continuam limitando caminhos.
- Um subconjunto determinístico (1/3 por UUID) de aldeões, comerciantes ambulantes, illagers e piglins dispõe de duas bandagens ao longo da vida. Após seis segundos de ferida e dois de preparação, sem o alvo de ataque em alcance próximo, estanca e recupera dois pontos de vida. Aviso e corações indicam a autocura. Animais não usam bandagens.

## Interface e trocas

Números de dano recebidos pelo jogador são filtrados no servidor e no cliente; textos de crítico e erro continuam. Cards das skills têm 28×28, tecla e tempo fora do pictograma, progresso fino e sombreamento de recarga. Foram elevados para reservar espaço à barra de ação nativa. Feedback de combate é desenhado após os cards, preservando textos que cruzem essa região.

A aba Trocas pode ser aberta mesmo sem ofertas para explicar a ausência de profissão/ofertas; não inventa negócios para aldeões simplórios. Para moradores com ofertas, abre o MerchantScreen nativo. Alternar tela renova a conversa após validação de entidade, distância e visão, evitando abandono silencioso quando a janela anterior expira. Print com aba desativada não permite concluir sozinho se todo morador falhava; confirmar manualmente com profissional e sem profissão.

## Vegetação e água

Grama/samambaias são pressionadas por corpos vivos, inclusive parados. A pressão fica mantida; recupera suavemente por 1,5 s após a saída. Base permanece ancorada, altura se comprime a 18%, inclinação reduzida. Plantas altas compartilham o pivô. Modelos usam a semente da posição, cor do bioma, offset e luz nativos pelo ModelBlockRenderer, substituindo o modelo isolado sem contexto. O mixin conserva o descritor exato corrigido na 0.42.1.

Ondas sob o voo detectam superfície de água até sete blocos abaixo, duram quatro segundos, expandem até cerca de três blocos de raio e produzem ondas laterais. Conservam recorte na margem, partículas mínimas/desativação e limite de 64 ondas. Referência visual: [Effective, projeto original](https://github.com/Ladysnake/Effective), efeitos de ondulações/respingos. Código próprio, sem portar ou instalar o mod de outra versão.

## Animações externas

[Fresh Animations: Player Extension](https://modrinth.com/resourcepack/fa-player-extension), de FreshLX, versão oficial 1.1, publicada em 14/06/2026 e listada pelo autor para 1.21.10. Download original intacto via Modrinth/CDN. Hash SHA512 e versão em ANIMACOES_PACOTE_043.json. Exige EMF/ETF, já presentes na instância. Melhora movimento/repouso/corrida/nado/voo/interação e inclui animações das mãos em primeira pessoa. Não é biblioteca de novas skills e não altera combate. Pacote externo separado; assets do autor não entram no JAR ou Git. Prioridade acima do Fresh Animations dos mobs, mantendo outros pacotes.

## Verificação e limites

Compilação via build-windows.bat assemble, sem suíte automatizada ou abertura do jogo conforme AGENTS.md. JSON, integridade CRC de JAR/ZIP, SHA512 do pacote externo e descritor do mixin revisados estaticamente. Roteiro manual pendente: alimentação/mãos/armas/sede cheia, bandagem, profissional sem ofertas/com ofertas, aldeão dormindo, fuga/retaliação, morcego em F5, Twilight em terreno claro/escuro, planta baixa/alta parada e andando, água a 3–7 blocos e animações com armadura/Barrel Roll. Compatibilidade visual em jogo ainda depende desse uso manual.
