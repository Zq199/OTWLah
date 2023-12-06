
# OTA Lah - Real-time Transportation App

## Purpose

OTA Lah is a transformative real-time transportation app designed specifically for the unique climate and commuting challenges of Singapore. Its key features include:

- **Minimizing Outdoor Waiting**: Integrates with the Singapore LTA API and Google Maps API to provide timely bus arrival information, reducing waiting time under harsh weather conditions.
- **Destination Weather Prediction**: Offers weather forecasts at your destination upon your estimated arrival time, helping to prepare for sudden weather changes.
- **Party Mode for Group Travel**: Facilitates synchronized group travel by calculating individualized departure times and providing real-time location and weather updates.

By promoting more efficient and weather-aware bus commutes, OTA Lah encourages a shift from cars to public transport, aiding environmental conservation efforts.

## File Structure

Navigate through the project's structure using the links below:

- [Lab1](./Lab1)
    - [Lab1_BlaBlaBus.pdf](./Lab1/Lab1_BlaBlaBus.pdf)
- [Lab2](./Lab2)
    - [Class Diagram](./Lab2/ClassDiagram)
    - [Refined SRS And Diagrams](./Lab2/Refined_SRS_And_Diagrams_2006_BlaBlaBus.pdf)
    - [Sequence Diagrams](./Lab2/SequenceDiagram)
    - [State Machine Diagrams](./Lab2/StateMachineDiagram)
- [Lab3](./Lab3)
    - [Refined SRS And Diagrams](./Lab3/Refined_SRS_And_Diagrams_2006_BlaBlaBus.pdf)
- [Deploy](./deploy)
    - [Client Application](./deploy/client)
    - [Server Application](./deploy/server)

## Deployment Instructions

### Setting Up the Server

1. Navigate to the server directory: `cd deploy/server`
2. Install necessary dependencies.
3. Start the server by running `server.py`.

### Setting Up the Client

1. Navigate to the client directory: `cd deploy/client`
2. Ensure Android Studio is installed.
3. Open the `SC2006` folder as an Android Studio project.
4. Build and run the application on your device or emulator.

### Components

- **Client**: Handles user interface and interaction, communicates with the server for data.
- **Server**: Manages the backend logic, database interactions, and external API communications.
