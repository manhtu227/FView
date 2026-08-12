#!/usr/bin/env python3
"""Generate json-to-view layout JSON from a natural-language prompt via xAI.

Usage:
  export XAI_API_KEY=...
  python3 scripts/ai_layout.py "card with image and title" -o /tmp/ui.json

Requires: pip install -r scripts/requirements-ai.txt  (or: pip install openai)
"""

from __future__ import annotations

import argparse
import json
import os
import re
import sys

SYSTEM_PROMPT = """You generate Android server-driven UI JSON for the json-to-view library.
Respond with ONLY one JSON object. No markdown fences, no commentary.
Root: { "type": string, "props": object, "children": array }
type must be one of: row, column, stack, box, text, list
Dimensions: "width"/"height": { "value": number, "unit": "dp" } (-1 match, -2 wrap).
Optional props: id, padding, margin, gap, backgroundColor, cornerRadius,
text, textSizeSp, textColor, imageUrl, contentDescription,
action: { "type": string, "payload": string or null }.
Prefer shallow mobile trees. Use https://picsum.photos/… for images when needed.
"""


def extract_json_object(raw: str) -> str:
    text = raw.strip()
    fence = re.search(r"```(?:json)?\s*([\s\S]*?)```", text, re.I)
    candidate = fence.group(1).strip() if fence else text
    start = candidate.find("{")
    if start < 0:
        raise ValueError("No JSON object in model response")
    depth = 0
    in_string = False
    escape = False
    for i, c in enumerate(candidate[start:], start):
        if in_string:
            if escape:
                escape = False
            elif c == "\\":
                escape = True
            elif c == '"':
                in_string = False
            continue
        if c == '"':
            in_string = True
        elif c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                return candidate[start : i + 1]
    raise ValueError("Unbalanced JSON braces")


def main() -> int:
    parser = argparse.ArgumentParser(description="AI layout JSON generator (xAI)")
    parser.add_argument("prompt", help="Natural language screen description")
    parser.add_argument("-o", "--output", help="Write JSON to file (default: stdout)")
    parser.add_argument(
        "--model",
        default=os.environ.get("XAI_MODEL", "grok-4.5"),
        help="Model id (default grok-4.5)",
    )
    args = parser.parse_args()

    api_key = os.environ.get("XAI_API_KEY", "").strip()
    if not api_key:
        print("XAI_API_KEY is not set. Export it or see docs/ai-studio.md", file=sys.stderr)
        return 1

    try:
        from openai import OpenAI
    except ImportError:
        print("Install openai: pip install openai", file=sys.stderr)
        return 1

    client = OpenAI(api_key=api_key, base_url="https://api.x.ai/v1")
    completion = client.chat.completions.create(
        model=args.model,
        messages=[
            {"role": "system", "content": SYSTEM_PROMPT},
            {
                "role": "user",
                "content": f"Create a mobile screen layout for:\n{args.prompt}\n\nReturn only the JSON object.",
            },
        ],
    )
    content = completion.choices[0].message.content or ""
    json_text = extract_json_object(content)
    # Validate JSON syntax
    json.loads(json_text)

    if args.output:
        with open(args.output, "w", encoding="utf-8") as f:
            f.write(json_text)
            if not json_text.endswith("\n"):
                f.write("\n")
        print(f"Wrote {args.output}", file=sys.stderr)
    else:
        print(json_text)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
