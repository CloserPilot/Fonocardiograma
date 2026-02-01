# 🫀 Fonocardiograma

Sistema completo para **registro, visualización y análisis de fonocardiogramas (PCG)** que combina hardware electrónico, firmware para microcontrolador y aplicación móvil Android.

<img width="734" height="396" alt="image" src="https://github.com/user-attachments/assets/9116db13-cc20-40a9-9e00-cfb209b2e2b5" />
<img width="862" height="1134" alt="image" src="https://github.com/user-attachments/assets/9a63d5f2-67c5-4375-a02c-7de77121796a" />



---

## 📌 Descripción del proyecto

Este proyecto entrega todas las partes necesarias para **reconstruir el dispositivo de prueba del fonocardiograma**, integrando adquisición de señal, transmisión inalámbrica y visualización multiplataforma.

Incluye:

- **Hardware** — diseños electrónicos y PCB para captura de señales acústicas cardiacas.
- **Firmware** — programa embebido para microcontrolador que adquiere y transmite datos.
- **Software móvil** — aplicación Android para comunicación con el hardware, visualización y exposición de datos en red local.
- **Documentación** — manuales y lista de partes.

Cada componente está diseñado para ser **independiente, reutilizable y extensible**.

---

## 📁 Estructura del repositorio

### 📦 APK/
Archivo **.apk instalable** de la aplicación Android, permitiendo su uso sin necesidad de compilación.

---

### 🛠 Circuitos/
Diseños electrónicos desarrollados en **KiCAD**:
- Esquemático del sistema.
- Diseño de PCB y capas correspondientes.

---

### 📲 Codigo AndroidStudio/
Proyecto completo de la aplicación Android desarrollado en **Android Studio**:
- Código fuente en Java.
- Interfaz de visualización de la señal.
- Gestión de conexión Bluetooth y red local.

La aplicación permite:
- Visualizar el fonocardiograma en el dispositivo móvil.
- Exponer la señal mediante **red local (LAN)** para su visualización en cualquier navegador web dentro de la misma red.

---

### 🧠 Codigo Arduino/
Código fuente para el microcontrolador (**ATTiny85 / AVR / Arduino compatible**):
- Configuración de ADC y adquisición de señal.
- Manejo de buffers de datos.
- Comunicación serial hacia el módulo Bluetooth **HC-05**.
- Scripts auxiliares para pruebas y depuración.

Este firmware es responsable de **capturar la señal acústica cardiaca y transmitirla a la aplicación móvil**.

---

### 📄 Documentacion.pdf
Documento técnico que describe:
- Objetivo del proyecto.
- Funcionamiento general del sistema.
- Interconexión entre hardware, firmware y aplicación.

---

### 🧾 Manual de usuario.pdf
Guía práctica para:
- Montaje del hardware.
- Uso del sistema de adquisición.
- Funcionamiento de la aplicación Android.
- Descripción de los módulos que integran la app.

---

### 📊 Lista de partes y costos.xlsx
Listado de componentes con:
- Cantidades requeridas.
- Costos estimados.
- Referencias de proveedores.

Facilita la reproducción del dispositivo.

---

## 📌 Uso del sistema

1. **Montaje de hardware**  
   Ensambla el circuito utilizando los esquemáticos de `Circuitos/` y la lista de partes.

2. **Carga de firmware**  
   Programa el microcontrolador con el código de `Codigo Arduino/`.

3. **Aplicación Android**  
   - Instala la app desde `APK/` o compílala desde `Codigo AndroidStudio/`.
   - Conecta el dispositivo vía Bluetooth.

4. **Visualización**  
   - Visualiza el fonocardiograma directamente en el teléfono.
   - Activa el **modo LAN** para acceder a la señal desde cualquier navegador web dentro de la misma red local (PC, tablet u otro dispositivo).

---

## 🎯 Casos de uso

- Prácticas académicas de bioinstrumentación.
- Análisis y estudio de bioseñales acústicas.
- Prototipado de sistemas biomédicos.
- Demostraciones de integración hardware–software–red.

---

## 🧩 Consideraciones

Este sistema **no sustituye equipos médicos certificados**. Está destinado a fines educativos, experimentales y de prototipado.

---

## 📜 Licencia

Licencia de código abierto (ver archivo `LICENSE` para detalles).

---

## 🤝 Contribuciones

Se aceptan mejoras en:
- Firmware
- Diseño electrónico
- Aplicación Android
- Visualización y análisis de señal

Usa *issues* o *pull requests* para proponer cambios.
