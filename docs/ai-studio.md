# AI Layout Studio (sample only)

Natural language → **OpenAI-compatible chat API** → stable layout JSON → **json-to-view** preview.

This is **not** part of the published `:json-to-view` AAR. The library stays offline and key-free.

## What it does

1. You describe a screen (“product card with image and Shop button”).
2. The sample app (or CLI) calls a chat completions endpoint.
3. Model returns JSON matching [schema.md](schema.md).
4. `JsonTreeParser` validates; `JsonToViewHost` renders Flat or Nested.

## API key

1. Create a key with your LLM provider (OpenAI-compatible `/v1/chat/completions`).
2. Keep it out of git.

### Sample app (Android debug)

In **gitignored** `local.properties` (project root):

```properties
sdk.dir=...
AI_API_KEY=your-key-here
```

Optional overrides (also via BuildConfig defaults in `app/build.gradle.kts`):

- Base URL default: `https://api.x.ai/v1` (change in gradle if you use another host)
- Model default: `grok-4.5`

Rebuild/run **debug** → Launcher → **AI Layout Studio** → **Generate**.

**Release builds** embed an empty key on purpose.

### Security

- Debug key-in-APK can be extracted. **Demo only.**
- Production apps must call **your backend**; keep `AI_API_KEY` on the server.
- Never commit `local.properties` or `.env`.

## CLI

```bash
pip install -r scripts/requirements-ai.txt
export AI_API_KEY=...
# optional:
# export AI_BASE_URL=https://api.x.ai/v1
# export AI_MODEL=grok-4.5
python3 scripts/ai_layout.py "feed of 3 image cards" -o /tmp/feed.json
```

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| “Add AI_API_KEY…” | Set key in `local.properties`, clean rebuild |
| HTTP 401 | Invalid key / credits |
| Parse error | Model returned non-JSON; retry Generate |
| No images | Need network + Glide; placeholder if load fails |
