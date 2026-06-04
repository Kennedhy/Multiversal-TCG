# Multiversal TCG

Backend Spring Boot do Multiversal TCG, com partidas PvE, salas PvP, colecao de cartas, decks, loja de pacotes, ranking e administracao de catalogo.

## Stack

- Java 17
- Spring Boot 3.2
- Spring Security com JWT
- Spring Data JPA
- SQLite
- WebSocket/STOMP para eventos PvP
- Swagger/OpenAPI via springdoc

## Como Rodar

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

A API sobe por padrao em:

```text
http://localhost:8080
```

## Documentacao Da API

As rotas ficam documentadas no Swagger:

```text
http://localhost:8080/swagger-ui.html
```

O contrato OpenAPI em JSON fica em:

```text
http://localhost:8080/v3/api-docs
```

Rotas protegidas usam JWT:

```http
Authorization: Bearer <token>
```

## Banco De Dados

O projeto usa SQLite local:

```properties
spring.datasource.url=jdbc:sqlite:multiverso.db
```

O schema e atualizado automaticamente pelo Hibernate durante o desenvolvimento.

## Testes

```bash
./mvnw test
```

No Windows:

```powershell
.\mvnw.cmd test
```

## Ranking

Todo jogador comeca com `0` pontos.

- Vitoria PvP: `+30`
- Derrota PvP: `-15`, sem ficar abaixo de `0`
- Empate PvP: `+5`

O historico e gravado automaticamente quando uma partida PvP termina.

## Arquivos Enviados

Uploads ficam em:

```text
data/uploads
```

Esses arquivos sao servidos pela aplicacao para cartas e emotes cadastrados no admin.
