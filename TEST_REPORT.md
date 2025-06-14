# TechCycle – Informe de Pruebas (14-jun-2025)

## 1 · Smoke manual

| Paso | Resultado |
|------|-----------|
| Login – email/contraseña válidos | ✅ |
| Login – email formato erróneo   | ✅ |
| Login – Google recordado        | ✅ |
| Publicar anuncio – campos vacíos| ✅ |
| Publicar anuncio – añadir 3 imágenes | ✅ |
| Publicar anuncio – publicar     | ✅ |
| Favorito – pulsar corazón       | ✅ |
| Reserva flujo A→B               | ✅ |


*Hoja completa*: [Google Sheet – Smoke Tests](https://docs.google.com/spreadsheets/d/1GdQw0OkBhT5LdV-U-7_6ULGr0sAEZVLfxTJBU8V-X2M/edit?usp=drive_link)

---

## 2 · Automatizadas

| Tipo | Estado | Evidencia |
|------|--------|-----------|
| Espresso UI – `ProductDetailTest` | ✅ sin fallos | `app/build/reports/androidTests/connected/index.html` |
| Lint/Analyze | 0 errores, 2 warnings | (adjunto en repo) |

## 3 · Conclusión

La versión `master` es estable.  
Próximos pasos propuestos:  
1. Migrar `Toast` → `Snackbar`.  
2. Mostrar errores con `TextInputLayout.setError`.  

---

_Generado el 14-jun-2025_
