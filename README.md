# Serasa — Desafio Técnico Backend

Solução para o desafio técnico de backend: ingestão, estabilização e armazenamento das leituras de peso de balanças de uma empresa de transporte de grãos, com cálculo de custo/margem e relatórios administrativos.

## Cenário

Uma empresa de transporte de grãos tem um parque de balanças (ESP32 + câmera LPR) que enviam leituras de peso via HTTP a cada 100ms enquanto há um caminhão presente, sem aguardar resposta (*fire-and-forget*) e sem estabilização própria no hardware. A aplicação precisa receber essas leituras, identificar automaticamente quando o peso estabilizou, persistir a pesagem e calcular custo/margem de venda de cada tipo de grão.

## Stack

- Java 25
- Spring Boot 4.1.0
- PostgreSQL 16 + Flyway
- Docker / Docker Compose

## O que foi implementado

- **Cadastros**: Caminhão, Tipo de Grão, Filial, Balança, Transação de Transporte (CRUD completo).
- **Endpoint de ingestão** (`POST /api/scale-readings`): recebe leituras concorrentes de múltiplas balanças simultaneamente, responde `202 Accepted` sem bloquear (fire-and-forget).
- **Estabilização automática**: janela deslizante em memória por balança, com reset por hiato de leituras (não por queda de peso) — estratégia detalhada em [`DECISIONS.md`](DECISIONS.md).
- **Custo e margem**: preço de venda calculado com margem de 5%–20% inversamente proporcional ao estoque disponível de cada grão.
- **Relatórios**: por tipo de grão, por filial, por balança (eficiência de estabilização) e uma visão geral — `GET /api/reports/*`.
- **Diferenciais**: arquitetura documentada, decisões de concorrência (`@Version` + update atômico), e um **simulador autônomo de balanças** (`scale-simulator/`) — serviço Spring Boot independente que descobre os cadastros existentes, abre transações e simula múltiplos ESP32 mandando leituras concorrentes com uma curva de peso realista.

## Como rodar

```bash
docker compose --profile simulator up --build
```

Sobe Postgres + API + simulador de balanças, já com dados de demonstração (seed via Flyway). Passo a passo completo, exemplos de chamadas e collection do Postman em [`INSTRUCTIONS.md`](INSTRUCTIONS.md).

## Documentação

- [`INSTRUCTIONS.md`](INSTRUCTIONS.md) — como rodar e testar (Docker Compose, IDE, Postman)
- [`DECISIONS.md`](DECISIONS.md) — decisões de arquitetura, estratégia de estabilização e melhorias futuras
- [`PROMPTS.md`](PROMPTS.md) — registro de uso de IA na construção da solução
- [`postman/serasa.postman_collection.json`](postman/serasa.postman_collection.json) — collection pronta para testes manuais