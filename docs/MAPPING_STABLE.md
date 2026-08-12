# Stable JSON mapping v1

Canonical schema for backends. See also [schema.md](schema.md).

## Node

```json
{
  "type": "row|column|stack|box|text|list",
  "props": { },
  "children": []
}
```

| type | Renders as |
|------|------------|
| `row` | horizontal linear |
| `column` | vertical linear |
| `stack` | layered |
| `box` | rect / image |
| `text` | text |
| `list` | vertical RecyclerView of children |

## props (stable keys)

`id`, `width`, `height`, `padding`, `margin`, `gap`, `backgroundColor`, `cornerRadius`,  
`text`, `textSizeSp`, `textColor`, `imageUrl`, `contentDescription`, `action`

### action

```json
{ "type": "string", "payload": "string|null" }
```

### width / height

```json
{ "value": number, "unit": "dp|px|percent" }
```

`-1` match, `-2` wrap.

## Deprecated (supported in 1.x)

- Root/group `viewType` (1/2/3), `props.orientation`, `props.layoutType`, `props.drawable`
- Dimension `unit` as int `1|2|3`

New backends **must** use `type` + string units.
