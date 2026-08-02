# Como rodar a aplicação

## Stack

- Java 25
- Spring Boot 4.1.0
- PostgreSQL 16 + Flyway

## Pré-requisitos

- Docker e Docker Compose
- Para rodar pela IDE (Opção 2): JDK 25 instalado

## Opção 1: Docker Compose (recomendado)

### Só a API principal (Postgres + app)

```bash
docker compose up --build
```

### API principal + simulador de balanças

O simulador (`scale-simulator`) fica atrás de um profile, para não gerar tráfego de demonstração toda vez que alguém só quer subir a API:

```bash
docker compose --profile simulator up --build
```

Ordem de subida garantida pelos healthchecks do compose: `postgres` fica saudável → `app` fica saudável (Flyway já aplicou as migrations, Tomcat aceitando conexões) → só então `scale-simulator` sobe.

- API: `http://localhost:8080`
- Postgres: `localhost:5432` (banco/usuário/senha: `serasa`/`serasa`/`serasa`)

### Dados já cadastrados (seed)

A migration `V3__seed_demo_data.sql` roda automaticamente no primeiro startup e já cadastra:

- 10 filiais, 10 tipos de grão, 10 caminhões, 3 balanças (`SCALE-01`, `SCALE-02`, `SCALE-03`)

Ou seja, dá para testar o fluxo de pesagem sem cadastrar nada manualmente.

### Testando o endpoint de leitura das balanças manualmente

Antes de mandar leituras, é preciso abrir uma transação de transporte para o caminhão:

```bash
curl -X POST http://localhost:8080/api/transport-transactions \
  -H "Content-Type: application/json" \
  -d '{"truckId":1,"grainTypeId":1,"originBranchId":1}'
```

(os ids `1` correspondem aos primeiros registros do seed; confira via `GET /api/trucks`, `/api/grain-types`, `/api/branches` se preferir outro caminhão/grão/filial)

Uma única chamada já mostra o endpoint funcionando (responde `202 Accepted`):

```bash
curl -X POST http://localhost:8080/api/scale-readings \
  -H "Content-Type: application/json" \
  -d '{"id":"SCALE-01","plate":"ABC1D23","weight":22000}'
```

**Importante**: essa chamada isolada não é suficiente para ver a pesagem persistida. Com os parâmetros de estabilização de produção (`min-samples: 10`, `window-duration: 1000ms`), é preciso ~10 leituras dentro de 1 segundo para o peso ser considerado estabilizado — clicar "Send" manualmente uma vez (ou mesmo várias vezes espaçadas por segundos) não alcança esse ritmo, porque a janela deslizante descarta amostras com mais de 1s. Duas formas de ver o fluxo completo (leitura → estabilização → transação `COMPLETED`):

1. **Recomendado**: suba com `--profile simulator` (acima) — ele já descobre as balanças/caminhões cadastrados, abre as transações sozinho e manda leituras a cada 100ms continuamente.
2. **Ou, um loop de `curl`** simulando o ESP32 manualmente:
   ```bash
   for i in $(seq 1 15); do
     curl -s -X POST http://localhost:8080/api/scale-readings \
       -H "Content-Type: application/json" \
       -d '{"id":"SCALE-01","plate":"ABC1D23","weight":22000}' > /dev/null
     sleep 0.1
   done
   ```
   (15 chamadas a cada 100ms — depois de ~1s já há amostras suficientes na janela). No Postman, o equivalente é usar o **Collection Runner** na request `scale-readings` com várias iterações e delay baixo, não clicar "Send" manualmente.
3. **Ou relaxar os parâmetros de estabilização** em `src/main/resources/application.yaml` antes de subir a aplicação, se preferir testar clicando manualmente sem loop nenhum:
   ```yaml
   scale:
     stabilization:
       window-duration: 30000ms  # janela de 30s em vez de 1s
       min-samples: 2            # 2 leituras bastam em vez de 10
   ```
   Com isso, dois ou três cliques no Postman (mesmo espaçados por vários segundos) já estabilizam o peso. É só um ajuste de conveniência para teste manual — não reflete o comportamento de produção (documentado em `DECISIONS.md`), então vale reverter depois se for reaproveitar o `application.yaml`.

### Parando e limpando

```bash
docker compose --profile simulator down -v
```

O `-v` remove também o volume do Postgres (apaga os dados; a próxima subida reaplica o seed do zero).

## Opção 2: Rodando localmente pela IDE

1. Suba só o Postgres: `docker compose up -d postgres`
2. Abra o projeto na IDE (Java 25) e rode `SerasaApplication` — o `application.yaml` já aponta para `localhost:5432` por padrão.
3. (Opcional) Para rodar o simulador também: importe `scale-simulator/pom.xml` como um projeto Maven separado na IDE (ele é um módulo independente, não faz parte do build da app principal) e rode `ScaleSimulatorApplication`. Sem nenhuma variável de ambiente configurada, ele já aponta para `http://localhost:8080` por padrão.

## Endpoints principais

- Cadastros (CRUD): `/api/branches`, `/api/grain-types`, `/api/trucks`, `/api/scales`
- Transações de transporte: `POST /api/transport-transactions` (abre), `GET /api/transport-transactions` (lista/filtra por status)
- Leituras das balanças: `POST /api/scale-readings`
- Relatórios: `/api/reports/grain-types`, `/api/reports/branches`, `/api/reports/scales`, `/api/reports/overview`

## Collection do Postman

Em `postman/serasa.postman_collection.json` tem uma collection pronta com os principais cadastros, a abertura de transação e o envio de leituras. Para usar:

1. Importe o arquivo no Postman (File → Import).
2. A collection usa a variável `{{localhost}}` para a URL base — edite a variável da collection e defina o valor como `http://localhost:8080`.

## Documentação adicional

- `DECISIONS.md` — decisões de arquitetura, estratégia de estabilização, melhorias futuras
- `PROMPTS.md` — registro de uso de IA no desenvolvimento (item 6 do desafio)
