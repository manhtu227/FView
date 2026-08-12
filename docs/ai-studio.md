# AI Layout Studio (sample only)

Natural language → **xAI (SpaceXAI)** → stable layout JSON → **json-to-view** preview.

This is **not** part of the published `:json-to-view` AAR. The library stays offline and key-free.

## What it does

1. You describe a screen (“product card with image and Shop button”).
2. The sample app (or CLI) calls xAI chat completions.
3. Model returns JSON matching [schema.md](schema.md).
4. `JsonTreeParser` validates; `JsonToViewHost` renders Flat or Nested.

## Get an API key

1. https://accounts.x.ai  
2. Create a key: https://console.x.ai  
3. Add credits as required by xAI.

## Sample app (Android)

1. In **gitignored** `local.properties` (project root), add:

```properties
sdk.dir=...
XAI_API_KEY=xai-your-key-here
```

2. Rebuild/run the **debug** sample app.

3. Launcher → **AI Layout Studio** → edit prompt → **Generate**.

4. Toggle **Flat / Nested** without calling the API again.

**Release builds** embed an empty key on purpose.

### Security

- Debug key-in-APK can be extracted. **Demo only.**
- Production apps must call **your backend**; keep `XAI_API_KEY` on the server.
- Never commit `local.properties` or `.env`.

## CLI

```bash
pip install -r scripts/requirements-ai.txt
export XAI_API_KEY=...
python3 scripts/ai_layout.py "feed of 3 image cards" -o /tmp/feed.json
```

Optional: `XAI_MODEL=grok-4.5` (default).

## Models & endpoint

- Base URL: `https://api.x.ai/v1`
- Default model: `grok-4.5` (override via `BuildConfig` / `XAI_MODEL`)
- Docs: https://docs.x.ai

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| “Add XAI_API_KEY…” | Set key in `local.properties`, clean rebuild |
| HTTP 401 | Invalid key / credits |
| Parse error | Model returned non-JSON; retry Generate |
| No images | Need network + Glide; placeholder if load fails |
