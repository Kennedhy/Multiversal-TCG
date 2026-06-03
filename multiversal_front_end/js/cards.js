/* não utilizado */
const C={
  charmander:{id:'charmander',n:'Charmander',cat:'monster',icon:'🦎',type:'CHAMA',atk:55,def:35,uni:'Pokémon'},
  pikachu:{id:'pikachu',n:'Pikachu',cat:'monster',icon:'⚡',type:'RELAMPAGO',atk:65,def:25,uni:'Pokémon'},
  squirtle:{id:'squirtle',n:'Squirtle',cat:'monster',icon:'🐢',type:'ABISMO',atk:45,def:65,uni:'Pokémon'},
  bulbasaur:{id:'bulbasaur',n:'Bulbasaur',cat:'monster',icon:'🌿',type:'NATUREZA',atk:50,def:55,uni:'Pokémon'},
  agumon:{id:'agumon',n:'Agumon',cat:'monster',icon:'🦕',type:'CHAMA',atk:65,def:50,uni:'Digimon'},
  garurumon:{id:'garurumon',n:'Garurumon',cat:'monster',icon:'🐺',type:'ABISMO',atk:90,def:70,uni:'Digimon'},
  magonegro:{id:'magonegro',n:'Mago Negro',cat:'monster',icon:'🔮',type:'SOMBRA',atk:100,def:40,uni:'Yu-Gi-Oh!'},
  zagueiro:{id:'zagueiro',n:'Zagueiro Muro',cat:'monster',icon:'🛡',type:'ABISMO',atk:40,def:90,uni:'Futebol'},
  pontaveloz:{id:'pontaveloz',n:'Ponta Veloz',cat:'monster',icon:'🏃',type:'RELAMPAGO',atk:75,def:25,uni:'Futebol'},
  dragao:{id:'dragao',n:'Dragão Azul',cat:'monster',icon:'🐉',type:'ETER',atk:120,def:30,uni:'Yu-Gi-Oh!'},
  orei:{id:'orei',n:'O Rei',cat:'monster',icon:'👑',type:'CHAMA',atk:70,def:60,uni:'Figuras BR'},
  pocao:{id:'pocao',n:'Poção',cat:'item',icon:'💊',effect:'heal1',desc:'Remove 1 Pressão do monstro ativo.'},
  pocaomax:{id:'pocaomax',n:'Poção Máxima',cat:'item',icon:'💉',effect:'healall',desc:'Remove toda a Pressão do monstro ativo.'},
  elixir:{id:'elixir',n:'Elixir de Força',cat:'item',icon:'⚗️',effect:'atkbuff',value:25,duration:2,desc:'ATK +25 por 2 turnos.'},
  escudo:{id:'escudo',n:'Escudo Arcano',cat:'item',icon:'🔰',effect:'defbuff',value:25,duration:2,desc:'DEF +25 por 2 turnos.'},
  antidoto:{id:'antidoto',n:'Antídoto',cat:'item',icon:'🧪',effect:'cleanse',desc:'Remove todos os status negativos do ativo.'},
  contraataque:{id:'contraataque',n:'Contra-Ataque',cat:'trap',icon:'🪤',trigger:'enemy_attack',desc:'Quando inimigo atacar: +1 Pressão no atacante.'},
  explosiva:{id:'explosiva',n:'Armadilha Explosiva',cat:'trap',icon:'💣',trigger:'enemy_invoke',desc:'Quando inimigo invocar: novo monstro entra com 1 Pressão.'},
  barreira:{id:'barreira',n:'Barreira de Tipo',cat:'trap',icon:'⛓️',trigger:'clash',desc:'No choque: anula a vantagem de tipo do inimigo.'},
  chamas:{id:'chamas',n:'Chamas',cat:'effect',status:'burn',duration:2,icon:'🔥',desc:'Aplica Queimado no inimigo: +1 Pressão por turno (2 turnos).'},
  veneno:{id:'veneno',n:'Veneno',cat:'effect',status:'poison',duration:3,icon:'☠️',desc:'Aplica Veneno: +1 Pressão a cada 2 turnos (3 turnos).'},
  blizzard:{id:'blizzard',n:'Blizzard',cat:'effect',status:'freeze',duration:1,icon:'❄️',desc:'Aplica Congelado: próxima postura inimiga forçada a Equilíbrio.'},
  caos:{id:'caos',n:'Caos',cat:'effect',status:'confused',duration:2,icon:'😵',desc:'Aplica Confusão: postura inimiga é aleatória por 2 turnos.'},
};

const ADV={CHAMA:['NATUREZA','SOMBRA'],ABISMO:['CHAMA','RELAMPAGO'],NATUREZA:['ABISMO','ETER'],RELAMPAGO:['CHAMA','SOMBRA'],SOMBRA:['NATUREZA','ETER'],ETER:['ABISMO','RELAMPAGO']};
const TCOLOR={CHAMA:'#e8633a',ABISMO:'#378add',NATUREZA:'#1d9e75',RELAMPAGO:'#f5a623',SOMBRA:'#7f77dd',ETER:'#c9a84c'};
const TEMOJI={CHAMA:'🔥',ABISMO:'🌊',NATUREZA:'🌿',RELAMPAGO:'⚡',SOMBRA:'🌑',ETER:'✨'};

const PLAYER_DECK=['charmander','charmander','pikachu','agumon','garurumon','magonegro','zagueiro','pontaveloz','pocao','pocao','pocaomax','elixir','escudo','antidoto','contraataque','explosiva','barreira','chamas','veneno','blizzard'];
const ENEMY_DECK=['squirtle','squirtle','bulbasaur','dragao','garurumon','pikachu','agumon','orei','pocao','pocao','pocaomax','elixir','escudo','antidoto','contraataque','explosiva','barreira','chamas','veneno','caos'];

const LEADERS=[
  {id:'ash',name:'Ash Ketchum',icon:'A',pass:'Monstros em Modo Farm geram +2 Aura extra.'},
  {id:'tai',name:'Tai Kamiya',icon:'T',pass:'Bloqueios em Modo Defesa causam 8 HP ao Lider inimigo, e armadilhas de Pressao ficam mais fortes.'},
  {id:'pele',name:'Pele',icon:'P',pass:'Modo Defesa concede +40 DEF em vez de +25, e inimigos em Farm recebem +1 Pressao.'},
  {id:'canarinho',name:'Canarinho Pistola',icon:'C',pass:'Se os 3 monstros atacarem no mesmo turno, todos recebem +15 ATK nesse combate.'},
  {id:'yugi',name:'Yugi Muto',icon:'Y',pass:'Cada choque vencido aplica +1 Pressao extra no defensor.'},
];

const STANCES={assault:{n:'Assalto',icon:'⚔️',atk:20,def:-15},guard:{n:'Guarda',icon:'🛡️',atk:-15,def:20},balance:{n:'Equilíbrio',icon:'⚖️',atk:0,def:0}};
