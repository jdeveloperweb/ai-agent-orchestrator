# [NÚMERO DA OS] - [TÍTULO DA ESPECIFICAÇÃO]

## a) CONTEXTO:

[Descreva o contexto geral desta solicitação: qual problema de negócio resolve, quais sistemas
são afetados, motivação da mudança e observações sobre escopo, limitações ou protótipos.
Mínimo 3 parágrafos. Evite texto genérico — explique o problema específico.]

---

## b) IMPLEMENTAÇÕES:

<!--
REGRA FUNDAMENTAL: cada b.N) representa UMA funcionalidade distinta a ser implementada.
Exemplos de granularidade correta:
  b.1) Endpoint de consulta de saldo         ← uma função
  b.2) Rotina de atualização de status       ← outra função
  b.3) Relatório de movimentações            ← outra função

NÃO agrupe tudo em um único b.1). Se o objetivo tiver 3 funcionalidades, crie b.1), b.2) e b.3).
Os passos numerados dentro de cada b.N) são a lógica INTERNA daquela função — não são funções separadas.
-->

### b.1) [Título da primeira funcionalidade]

**Parâmetros de entrada:**
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| campo_a   | varchar(50) | Código do documento a ser processado |
| campo_b   | date | Data de referência para o filtro |

**Parâmetros de saída:**
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| success   | bool | true se a operação foi realizada com sucesso |
| message   | text | Mensagem de erro descritiva, caso success=false |
| id_gerado | bigint | ID do registro criado, quando aplicável |

**Lógica:**
1. Validar que `campo_a` não está vazio e possui formato válido (ex: regex `^[A-Z0-9]{8}$`).
2. Verificar na tabela `TB_DOCUMENTOS` (campo `CD_DOCUMENTO`) se o registro já existe.
   Se existir com status `PROCESSADO`, retornar erro: "Documento já processado."
3. Inserir na tabela `TB_LOG_PROCESSAMENTO` com: `CD_DOCUMENTO`, `DT_PROCESSAMENTO = NOW()`,
   `CD_USUARIO = usuário da sessão`, `ST_STATUS = 'INICIADO'`.
4. Chamar o serviço externo `OcrService.processar(campo_a)` via REST POST em `/ocr/processar`.
   Timeout: 30s. Em caso de timeout, atualizar `ST_STATUS = 'ERRO_TIMEOUT'` e retornar erro.
5. Atualizar `TB_LOG_PROCESSAMENTO` com `ST_STATUS = 'CONCLUIDO'` e `DS_RESULTADO = resposta do OCR`.
6. Retornar `success = true` e `id_gerado = ID do log inserido`.

---

### b.2) [Título da segunda funcionalidade]

**Parâmetros de entrada:**
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|

**Parâmetros de saída:**
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|

**Lógica:**
[Passos numerados com tabelas, campos, validações e regras de negócio específicas.]

---

*(repita o padrão b.N) para cada funcionalidade necessária)*

---

## c) PONTOS DE FUNÇÃO SUGERIDOS:

> Sugestão baseada na metodologia IFPUG. Deve ser revisada e validada pelo especialista antes de registrar no sistema.

<!--
REGRA FUNDAMENTAL: a tabela abaixo deve ter EXATAMENTE UMA LINHA POR SEÇÃO b.N).
Se o documento tiver b.1), b.2) e b.3) → a tabela deve ter 3 linhas.
NÃO crie linhas para passos internos de um b.N). Cada b.N) é UMA função, independente
de quantos passos de lógica ela tenha internamente.
-->

| # | Descrição da Função | Tipo | Complexidade | Pontos |
|---|---------------------|------|--------------|--------|
| 1 | b.1) [título da primeira funcionalidade] | [AIE/ALI/EE/CE/SE] | [Simples/Média/Complexa] | [pts] |
| 2 | b.2) [título da segunda funcionalidade]  | [AIE/ALI/EE/CE/SE] | [Simples/Média/Complexa] | [pts] |

**Total PF Brutos:** [X]
**Fator de Ajuste:** 1.0 *(a confirmar com o especialista)*
**Total PF Ajustados:** [X]

### Referência IFPUG — pontos por tipo e complexidade:
| Tipo | Sigla | Simples | Média | Complexa |
|------|-------|---------|-------|----------|
| Arquivo Lógico Interno    | ALI | 7  | 10 | 15 |
| Arquivo de Interface Ext. | AIE | 5  | 7  | 10 |
| Entrada Externa           | EE  | 3  | 4  | 6  |
| Saída Externa             | SE  | 4  | 5  | 7  |
| Consulta Externa          | CE  | 3  | 4  | 6  |

### Critério de complexidade:
- **Simples** — lógica direta, poucos dados referenciados, sem regras de decisão complexas
- **Média** — lógica moderada, dados de múltiplas tabelas, algumas regras de negócio
- **Complexa** — lógica elaborada, múltiplas integrações ou regras de negócio complexas
