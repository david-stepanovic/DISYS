# DISYS – Gruppe Q
Distributed Systems SS2026

# Energy Community

Projekt für DISYS. Es geht darum, eine Energy Community zu simulieren – also wie viel Strom die Community selbst produziert, verbraucht und wie viel vom Netz kommt.

## Aufbau

Das Projekt besteht aus zwei Teilen:

### `energy-api`
Spring Boot REST API, die die Daten liefert.

- `GET /energy/current` – aktuelle Auslastung der Community (in %) und Netzanteil
- `GET /energy/historical?start=...&end=...` – historische Daten für einen Zeitraum

### `energy-gui`
JavaFX Anwendung, die die Daten vom API holt und anzeigt. Die GUI redet **nicht** direkt mit der Datenbank, sondern nur über die REST API.

Was man sieht:
- aktuelle Community Pool Auslastung und Grid Portion
- Zeitraumfilter (Start/End per Dropdown) für historische Daten
- produzierte, verbrauchte und aus dem Netz bezogene Energie in kWh

## Starten

Zuerst die API starten:
```bash
cd energy-api
./mvnw spring-boot:run
```

Dann die GUI:
```bash
cd energy-gui
./mvnw javafx:run
```

API läuft auf `http://localhost:8080`.

## Projektstruktur
```
DISYS/
├── energy-api/   # Spring Boot Backend
└── energy-gui/   # JavaFX Frontend
```