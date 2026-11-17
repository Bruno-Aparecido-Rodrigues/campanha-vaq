# campanha-vaq — Muuv Vaquinhas

CRUD das vaquinhas. Porta **8082**, banco `vaquinha-campanha`.

Segue a modularização do professor: módulo **`domain`** (Java puro: entidades, interfaces dos repositórios e regras) e módulo **`spring`** (controller, repositório Mongo, configuração e mensageria).

## Endpoints

Os headers `X-User-*` são colocados pelo gateway. Para testar direto na 8082, envie-os à mão.

| Método | Rota | Headers |
|---|---|---|
| POST | `/campanha/save` | `X-User-Id`, `X-User-Nome` |
| PUT | `/campanha/update` | `X-User-Id` (só o criador) |
| DELETE | `/campanha/delete/{id}` (409 se já recebeu doação) | `X-User-Id` |
| GET | `/campanha/all` | — |
| GET | `/campanha/minhas` | `X-User-Id` |
| GET | `/campanha/{id}` | — |

## Mensageria (exchange `vaquinha.eventos`)

- **Publica** `campanha.*`, que é consumido pelo doacao-vaq e pelo relatorio-vaq.
- **Ouve** `doacao.realizada` na fila `campanha.doacao-realizada` e soma o valor arrecadado.

## Como rodar

Pré-requisitos: Java 17+, Maven e MongoDB local em replica set (`rs0`).

```bash
mvn clean install
mvn -pl spring spring-boot:run
```

Ou, no IntelliJ, rode a classe `*Application` do módulo `spring`.

### Variável obrigatória

A senha do CloudAMQP não fica no repositório. Antes de rodar, defina:

```
RABBITMQ_PASSWORD=<senha do CloudAMQP>
```

No IntelliJ: *Run → Edit Configurations → Environment variables*. Também dá para sobrescrever `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_VHOST` e `RABBITMQ_SSL` (os padrões já apontam para o CloudAMQP do grupo).

## Outros repositórios do projeto

`front-vaq` · `gateway-vaq` · `login-vaq` · `campanha-vaq` · `doacao-vaq` · `relatorio-vaq`
