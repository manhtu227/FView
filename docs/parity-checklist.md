# Flat vs Nested parity checklist

Automated tests cover **parser/model** equality for fixtures. Visual parity is verified manually:

| Check | Flat | Nested |
|-------|------|--------|
| Same JSON → same `FNode` | unit tests | unit tests |
| padding / margin / gap | visual | visual |
| text content / color | visual | visual |
| imageUrl without loader → placeholder | yes | yes |
| imageUrl with app loader → image | yes | yes |
| action tap fires handler | yes | yes |
| LIST scrolls | RV | RV |

Run sample app: **hello / card / feed-page** in Flat, then set Nested in a debug build if comparing.
