# Multiversal-TCG

## Rotas do backend

Base local: `http://localhost:8080`

Rotas protegidas usam:

```http
Authorization: Bearer <token>
```

| Metodo | Rota | Protegida | O que faz |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | Nao | Cadastra um novo usuario. |
| POST | `/api/auth/login` | Nao | Faz login e retorna o token. |
| GET | `/api/auth/me` | Sim | Retorna o usuario logado. |
| POST | `/api/game/iniciar` | Opcional | Inicia uma partida. |
| GET | `/api/game/estado/{roomId}` | Nao | Busca o estado da partida. |
| POST | `/api/game/turno/{roomId}` | Nao | Envia uma jogada do turno. |
| POST | `/api/game/especial/{roomId}` | Nao | Ativa a habilidade especial. |
| GET | `/api/cards` | Nao | Lista as cartas. |
| GET | `/api/cards/options` | Nao | Lista opcoes para criar/editar cartas. |
| GET | `/api/cards/{id}` | Nao | Busca uma carta pelo ID. |
| POST | `/api/cards` | Nao | Cria uma carta. |
| PUT | `/api/cards/{id}` | Nao | Edita uma carta. |
| DELETE | `/api/cards/{id}` | Nao | Exclui uma carta. |
| POST | `/api/cards/{id}/image?rarity={rarity}` | Nao | Envia imagem para uma carta; `rarity` e opcional. |
| GET | `/api/packs` | Nao | Lista os pacotes cadastrados. |
| GET | `/api/packs/{id}` | Nao | Busca um pacote pelo ID. |
| POST | `/api/packs` | Nao | Cria um pacote. |
| PUT | `/api/packs/{id}` | Nao | Edita um pacote. |
| DELETE | `/api/packs/{id}` | Nao | Exclui um pacote. |
| GET | `/api/deck/default` | Nao | Busca o deck padrao. |
| PUT | `/api/deck/default` | Nao | Atualiza o deck padrao. |
| GET | `/api/ranking` | Nao | Lista o ranking geral dos jogadores. Aceita `?limit=50`. |
| GET | `/api/players/{playerId}/profile` | Sim | Busca o perfil do jogador. |
| GET | `/api/players/{playerId}/matches` | Sim | Lista o historico de partidas PvP do jogador. Aceita `?limit=30`. |
| GET | `/api/players/{playerId}/collection` | Sim | Lista a colecao do jogador. |
| GET | `/api/players/{playerId}/shop` | Sim | Mostra a loja do jogador. |
| POST | `/api/players/{playerId}/packs/{packId}/buy` | Sim | Compra e abre um pacote especifico. |
| GET | `/api/players/{playerId}/packs/history` | Sim | Lista pacotes ja abertos. |
| GET | `/api/players/{playerId}/decks` | Sim | Lista os decks do jogador. |
| GET | `/api/players/{playerId}/decks/{deckId}` | Sim | Busca um deck especifico. |
| POST | `/api/players/{playerId}/decks` | Sim | Cria um deck. |
| POST | `/api/players/{playerId}/decks/copy-default` | Sim | Copia o deck padrao. |
| PUT | `/api/players/{playerId}/decks/{deckId}` | Sim | Edita um deck. |
| DELETE | `/api/players/{playerId}/decks/{deckId}` | Sim | Exclui um deck. |
| POST | `/api/pvp/rooms` | Sim | Cria uma sala PvP. |
| POST | `/api/pvp/rooms/{code}/join` | Sim | Entra em uma sala PvP. |
| GET | `/api/pvp/rooms/{code}` | Sim | Busca o estado da sala PvP. |
| POST | `/api/pvp/rooms/{code}/turn` | Sim | Envia uma jogada PvP. |
| POST | `/api/pvp/rooms/{code}/special` | Sim | Usa a habilidade especial no PvP. |

Obs: nas rotas de jogador, `{playerId}` precisa ser o mesmo usuario do token.

## Ranking e historico

Todo jogador comeca com `0` pontos de ranking.

- Vitoria PvP: `+30` pontos.
- Derrota PvP: `-15` pontos, sem ficar abaixo de `0`.
- Empate PvP: `+5` pontos.

O historico e gravado automaticamente quando uma partida PvP termina.
