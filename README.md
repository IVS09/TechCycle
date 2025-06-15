# Informe de Pruebas – TechCycle

**Fecha:** 15 de junio de 2025  
**Proyecto:** Aplicación de compraventa TechCycle  
**Plataforma:** Android (Kotlin, Firebase)

---

## 1. Entorno de pruebas

- **Emulador:** Pixel 5 – Android 13 (API 33)  
- **IDE:** Android Studio Iguana  
- **Back-end:** Firebase Authentication & Realtime Database  
- **Frameworks:** Espresso, Lint, Accessibility Scanner

---

## 2. Pruebas funcionales manuales (Smoke Test)

| Paso                             | Esperado                                  | Resultado |
|----------------------------------|--------------------------------------------|-----------|
| Login con credenciales válidas  | Acceso a pantalla principal                | ✅ OK      |
| Login con email mal formado     | Muestra error y no navega                  | ✅ OK      |
| Publicar anuncio sin imagen     | Error al guardar                           | ✅ OK      |
| Añadir favorito                 | Icono se marca, persiste tras reinicio    | ✅ OK      |
| Solicitar reserva desde otro usuario | El vendedor puede aceptarla           | ✅ OK      |

---

## 3. Pruebas automatizadas (Espresso UI)

### LoginTest.kt
- Verifica validación de formato y muestra de errores en login.
- Comprueba que con credenciales incorrectas no se permite el acceso.

### ProductDetailTest.kt
- Accede al primer producto del listado.
- Verifica la visibilidad del título, botón de favoritos y estado del botón de reserva.

### ReserveAdTest.kt
- Simula la reserva de un producto desde otro usuario.
- Comprueba la aparición del mensaje Toast al reservar correctamente.

### MyAdsListTest.kt
- Accede al apartado “Mis anuncios”.
- Verifica que al menos un anuncio del usuario se muestra en el RecyclerView.

---

## 4. Evidencia generada

- Informes HTML:  
  - `index.html`  
  - `com.mrlapidus.techcycle.MyAdsListTest.html`  
  - `com.mrlapidus.techcycle.html`

- Capturas:
  - `smoke_test.png`  
  - Capturas de los tests disponibles en carpeta `/screenshots`

- Código de test incluido en carpeta `/androidTest/`:
  - LoginTest.kt  
  - ProductDetailTest.kt  
  - ReserveAdTest.kt  
  - MyAdsListTest.kt

---

## 5. Valoración final

El sistema ha sido validado mediante pruebas de caja negra, test instrumentados y verificación manual completa de sus funcionalidades clave: login, favoritos, reserva, listado de anuncios y flujo de navegación.

El comportamiento es estable y el código ha sido verificado con herramientas de análisis. La app está lista para su entrega y despliegue.
