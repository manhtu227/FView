# json-to-view schema (0.2+)

Backend describes a screen as a **tree of nodes**. The client parses JSON into `FNode` and renders with **Flat** or **Nested**.

## Node shape (stable)

```json
{
  "type": "column",
  "props": { },
  "children": [ ]
}
```

| Field | Required | Description |
|-------|----------|-------------|
| `type` | yes (stable) | `row` \| `column` \| `stack` \| `box` \| `text` \| `list` |
| `props` | no | layout + content + action |
| `children` | no | nested nodes (array) |

### Legacy (still supported)

Older trees use `viewType` (1=list, 2=group, 3=box), `props.orientation`, `props.drawable`. Prefer stable `type` for new backends.

## Dimensions

```json
"width": { "value": -1, "unit": "dp" },
"height": { "value": 160, "unit": "dp" }
```

| value | meaning |
|------:|---------|
| `-1` | match parent |
| `-2` | wrap content |
| `>0` | fixed |

`unit`: `"dp"` \| `"px"` \| `"percent"` (or legacy ints `1`/`2`/`3`).

## Props

| Prop | Type | Notes |
|------|------|--------|
| `id` | string | optional id for debugging / analytics |
| `padding` / `margin` | `{left,top,right,bottom}` ints (dp) | |
| `gap` | int (dp) | between children in row/column |
| `backgroundColor` | `#RRGGBB` / `#AARRGGBB` | or legacy `background.color` |
| `cornerRadius` | float (dp) | |
| `text` | string | text nodes |
| `textSizeSp` | number | |
| `textColor` | color string | |
| `imageUrl` | string | loaded via app `ImageLoader` |
| `contentDescription` | string | a11y |
| `action` | object | see below |

## Actions

```json
"action": {
  "type": "open_url",
  "payload": "https://example.com"
}
```

| type (convention) | payload example |
|-------------------|-----------------|
| `open_url` | URL string |
| `navigate` | route id |
| `custom` | free-form |

The library **does not** interpret actions. Your app’s `NodeActionHandler` receives `(FNode, NodeAction)`.

## Images

- Backend sets `imageUrl`.
- App injects `ImageLoader` (e.g. Glide) in `RenderConfig`.
- Without a loader (benchmark default): solid **placeholder** color.

## Samples

| File | Purpose |
|------|---------|
| [schema/hello.json](schema/hello.json) | column + tappable text |
| [schema/card.json](schema/card.json) | image + title + card action |
| [schema/feed-page.json](schema/feed-page.json) | short feed of cards |

Also shipped under `app/src/main/assets/samples/` for the sample app.
