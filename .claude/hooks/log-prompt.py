#!/usr/bin/env python3
import json
import sys
from datetime import datetime
from pathlib import Path

PROMPTS_FILE = Path(__file__).resolve().parents[2] / "PROMPTS.md"

HEADER = (
    "# Registro de Prompts - Uso de IA no Desafio Tecnico\n\n"
    "Este arquivo documenta, em ordem cronologica, os prompts usados durante o "
    "desenvolvimento com IA (Claude Code), conforme exigido no item 6 do desafio "
    "(\"Utilizacao de IA na construcao da solucao\" / \"Compartilhamento do prompt "
    "utilizado e codigo gerado\").\n\n"
    "Entradas do tipo \"Prompt\" sao geradas automaticamente a cada mensagem enviada. "
    "Entradas do tipo \"Pergunta com alternativas\" sao registradas manualmente pela IA "
    "logo apos uma interacao de multipla escolha, incluindo a opcao escolhida ou o "
    "texto livre digitado no lugar.\n\n---\n\n"
)


def main():
    try:
        data = json.load(sys.stdin)
    except json.JSONDecodeError:
        data = {}

    prompt = data.get("prompt", "")
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    if not PROMPTS_FILE.exists():
        PROMPTS_FILE.write_text(HEADER)

    existing = PROMPTS_FILE.read_text()
    separator = "" if existing.endswith("\n\n") else "\n" if existing.endswith("\n") else "\n\n"

    with PROMPTS_FILE.open("a") as f:
        f.write(f"{separator}## [{timestamp}] Prompt\n\n{prompt}\n\n---\n\n")


if __name__ == "__main__":
    main()