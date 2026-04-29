# AI Agent Orchestrator

Serviço de agentes de IA construído com Spring Boot e LangChain4j para geração de **especificações técnicas** no padrão da empresa (IFPUG), com ciclo completo de raciocínio, reflexão, memória e avaliação de confiança.

## Tecnologias

- Java 21
- Spring Boot 3.2.5
- LangChain4j 0.30.0
- OpenAI (gpt-4o-mini por padrão)
- Maven

---

## O que este agente faz

### Fluxo principal (`/api/agents/full-cycle-stream`)

```
Objetivo do analista
        │
        ▼
┌─────────────────────────────────────────┐
│ 1. OrchestratorAgent                    │
│    Consulta RAG + memória episódica     │
│    → planeja seções da especificação    │
└─────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────┐
│ 2. SectionAgent (por seção)             │
│    Gera cada seção individualmente:     │
│    a) CONTEXTO                          │
│    b.N) IMPLEMENTAÇÕES                  │
│    c) PONTOS DE FUNÇÃO (IFPUG)          │
│    → consulta RAG focado por seção      │
│    → verifica especificações similares  │
└─────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────┐
│ 3. CriticAgent (loop de reflexão)       │
│    Revisa a especificação completa      │
│    → max 3 iterações                    │
│    → detecta convergência (≥72%         │
│      similaridade → pede intervenção)   │
│    → regenera só as seções com problemas│
└─────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────┐
│ 4. UncertaintyAgent                     │
│    Avalia confiança por seção:          │
│    ALTA / MÉDIA / BAIXA                 │
│    → aponta o que o analista deve       │
│      revisar com atenção                │
└─────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────┐
│ 5. Memória Episódica                    │
│    Salva especificação aprovada         │
│    → usada como referência em          │
│      especificações futuras similares   │
└─────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────┐
│ 6. CodeAgent (opcional)                 │
│    Gera código a partir da              │
│    especificação (specOnly=false)       │
└─────────────────────────────────────────┘
```

### Agentes

| Agente | Persona | Ferramentas | Responsabilidade |
|--------|---------|-------------|-----------------|
| `OrchestratorAgent` | Analista Sênior | RAG + Memória Episódica | Planeja seções e pesquisa contexto |
| `SectionAgent` | Analista Sênior | RAG + Memória Episódica | Gera uma seção por vez com precisão |
| `DocumentAgent` | Analista Sênior | RAG | Geração monolítica (fallback) |
| `CriticAgent` | Revisor de Qualidade | — | Valida formato IFPUG e completude |
| `UncertaintyAgent` | Avaliador de Risco | — | Confiança por seção (ALTA/MÉDIA/BAIXA) |
| `CodeAgent` | Desenvolvedor | FileSystem | Gera código a partir da especificação |

### Recursos do agente

- **ReAct loop** — LangChain4j executa Reason→Act→Observe automaticamente ao chamar ferramentas (RAG, memória)
- **Reflexão com convergência** — até 3 iterações de revisão; se o revisor apontar os mesmos problemas em iterações consecutivas (≥72% similaridade), o agente para e pede intervenção do analista em vez de ficar em loop infinito
- **Decomposição por seção** — cada seção da especificação é gerada separadamente, permitindo consultas RAG focadas e maior precisão por seção
- **Memória episódica** — especificações aprovadas são salvas em `agent-workspace/memory/` e consultadas em novas especificações similares, permitindo que o agente aprenda com o histórico
- **Incerteza calibrada** — após a aprovação, cada seção recebe um nível de confiança com os pontos que o analista deve revisar com atenção
- **Streaming SSE** — todos os eventos do ciclo (pensamento, ferramentas, reflexão, resultado) são transmitidos em tempo real

### Formato de saída

A especificação segue o template definido em `src/main/resources/doc-template.md`:

```
a) CONTEXTO
   Contexto de negócio, sistemas afetados, motivação

b.1) NOME DA IMPLEMENTAÇÃO
   Parâmetros de entrada (tabela)
   Parâmetros de saída (tabela)
   Lógica passo a passo

b.N) ...

c) PONTOS DE FUNÇÃO SUGERIDOS
   Tabela IFPUG: tipo (AIE/ALI/EE/CE/SE), complexidade, pontos
   Total PF Brutos / Fator de Ajuste / Total PF Ajustados
```

---

## Configuração

### 1. Variável de ambiente

```bash
export OPENAI_API_KEY='sua-chave-aqui'
```

### 2. `application.yml`

```yaml
agent:
  rag:
    base-url: http://localhost:8080        # URL do serviço RAG
    chat-path: /api/v1/chat/query
  workspace:
    root-dir: ./agent-workspace            # arquivos gerados
  memory:
    dir: ./agent-workspace/memory          # memória episódica
  template:
    doc-path: classpath:doc-template.md    # template da especificação
  openai:
    model: gpt-4o-mini
    temperature: 0.2
```

### 3. Template da especificação

Substitua `src/main/resources/doc-template.md` pelo template da sua empresa. O agente seguirá rigorosamente o formato definido nesse arquivo.

---

## Endpoints

### Ciclo completo com streaming (principal)

```
GET /api/agents/full-cycle-stream
```

Parâmetros (query string):

| Parâmetro | Obrigatório | Padrão | Descrição |
|-----------|-------------|--------|-----------|
| `objective` | sim | — | Objetivo da especificação |
| `sessionId` | não | UUID gerado | Continuidade de sessão |
| `contextQuestion` | não | — | Contexto adicional |
| `targetStack` | não | `Java Spring Boot` | Stack alvo |
| `saveFiles` | não | `true` | Salvar em `agent-workspace/` |
| `specOnly` | não | `false` | Ignorar geração de código |

Resposta: SSE stream com eventos do tipo `progress`.

**Tipos de evento SSE:**

| Tipo | Significado |
|------|-------------|
| `THOUGHT` | Agente raciocinando |
| `TOOL_START` | Ferramenta sendo chamada (RAG, memória) |
| `TOOL_END` | Ferramenta concluída |
| `STEP_COMPLETE` | Etapa principal finalizada |
| `FILE_CREATED` | Arquivo salvo em workspace |
| `REFLECTION` | Revisor iterando sobre a especificação |
| `NEED_INFO` | Agente pedindo informação ao analista |
| `COMPLETE` | Ciclo concluído (inclui `FullCycleResponse`) |
| `ERROR` | Falha no ciclo |

**Exemplo:**

```bash
curl -N "http://localhost:8090/api/agents/full-cycle-stream?\
objective=Criar+endpoint+de+exportacao+de+relatorio+PDF\
&targetStack=Java+Spring+Boot\
&specOnly=true"
```

**`FullCycleResponse` (evento COMPLETE):**

```json
{
  "document": {
    "content": "# markdown da especificação...",
    "filePath": "doc_20260425_123456.md"
  },
  "code": {
    "notes": "resumo do código gerado",
    "files": []
  },
  "documentIterations": 2,
  "specOnly": false,
  "confidenceReport": {
    "overallConfidence": "ALTA",
    "sectionConfidence": {
      "contexto": "ALTA",
      "b.1": "MÉDIA"
    },
    "attentionPoints": ["Validar nomes das tabelas com o DBA"],
    "rawReport": "..."
  }
}
```

---

### Gerar especificação (sem streaming)

```bash
curl -X POST http://localhost:8090/api/agents/document \
  -H "Content-Type: application/json" \
  -d '{
    "objective": "Criar sistema de login com OAuth2",
    "contextQuestion": "Quais provedores são suportados?",
    "saveToFile": true
  }'
```

### Gerar código a partir de especificação

```bash
curl -X POST http://localhost:8090/api/agents/code \
  -H "Content-Type: application/json" \
  -d '{
    "document": "# Especificação...",
    "targetStack": "Java Spring Boot",
    "outputInstruction": "Use Spring Security e JWT"
  }'
```

### Ciclo completo (sem streaming)

```bash
curl -X POST http://localhost:8090/api/agents/full-cycle \
  -H "Content-Type: application/json" \
  -d '{
    "objective": "Endpoint de exportação de PDF",
    "targetStack": "Java Spring Boot",
    "saveFiles": true,
    "specOnly": true
  }'
```

---

## Estrutura do projeto

```
src/main/java/com/jdeveloperweb/aiagent/
├── agent/
│   ├── OrchestratorAgent.java     # planeja seções, consulta RAG + memória
│   ├── SectionAgent.java          # gera uma seção por vez
│   ├── DocumentAgent.java         # geração monolítica (fallback)
│   ├── CriticAgent.java           # revisão de qualidade IFPUG
│   ├── UncertaintyAgent.java      # confiança por seção
│   └── CodeAgent.java             # geração de código
├── config/
│   ├── AgentConfig.java           # beans LangChain4j (ferramentas por agente)
│   └── WebConfig.java             # CORS
├── controller/
│   └── AgentController.java       # endpoints REST + SSE
├── dto/
│   ├── AgentContext.java          # estado da sessão, monta prompts por seção
│   ├── ConfidenceReport.java      # resultado da avaliação de incerteza
│   ├── SpecMemory.java            # entrada do índice de memória episódica
│   └── ...                        # demais DTOs
├── service/
│   ├── AgentOrchestratorService.java      # orquestra todo o fluxo
│   ├── EpisodicMemoryService.java         # leitura/escrita da memória episódica
│   ├── ReflectionConvergenceDetector.java # detecta loop sem convergência
│   ├── TemplateService.java               # carrega doc-template.md
│   └── AgentIO.java                       # emite eventos SSE
├── tool/
│   ├── RagTool.java               # @Tool consulta ao RAG externo
│   ├── EpisodicMemoryTool.java    # @Tool busca na memória episódica
│   └── FileSystemTool.java        # @Tool salva arquivos
└── exception/
    └── GlobalExceptionHandler.java

src/main/resources/
├── application.yml
└── doc-template.md                # template da especificação (editável)

agent-workspace/                   # gerado em runtime
├── memory/
│   ├── index.json                 # índice da memória episódica
│   └── *.md                       # especificações completas armazenadas
└── *.md                           # especificações geradas
```
