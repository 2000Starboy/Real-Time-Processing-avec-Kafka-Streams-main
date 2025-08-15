# Projet de Traitement de Données en Temps Réel avec Kafka Streams

Ce projet démontre l'utilisation d'Apache Kafka et de la bibliothèque Kafka Streams pour construire des pipelines de traitement de données en temps réel. Il est divisé en deux exercices principaux basés sur le TP de "Big Data Processing".

## Technologies Utilisées

* Java 21
* Spring Boot 3
* Apache Kafka
* Kafka Streams
* Maven
* Docker

---

## Exercice 1 : Analyse de Données Météorologiques

### Objectif

Développer une application Kafka Streams qui consomme des données météorologiques, filtre les températures élevées (> 30°C), convertit les températures de Celsius en Fahrenheit, et calcule la température et l'humidité moyennes par station.

### Résultats

#### Environnement Docker et Code
L'environnement Kafka a été déployé via Docker, et la logique de traitement a été implémentée en Java.

<img width="1512" height="982" alt="ex1_docker_dashboard" src="https://github.com/user-attachments/assets/4bc85292-6d82-4a03-a732-6e52ff992027" />

*Figure 1 : Environnement Kafka et Zookeeper fonctionnant sur Docker.*

<img width="1007" height="883" alt="ex1_code_weather_analysis" src="https://github.com/user-attachments/assets/5fedfd58-ac33-4cbd-a3c6-1809f12031c0" />

*Figure 2 : Logique principale au sein de l'application `WeatherAnalysis.java`.*

---

#### Résultats Finaux
L'application publie avec succès les données agrégées dans le topic `station-averages`.

<img width="1512" height="467" alt="ex1_final_results" src="https://github.com/user-attachments/assets/cbbda980-ac6c-477f-b932-65d5b271d03f" />

*Figure 3 : Le `kafka-console-consumer` affichant les moyennes finales par station.*

---

## Exercice 2 : Analyse de Clics en Temps Réel

### Objectif

Construire une application web full-stack avec Spring Boot pour suivre les clics des utilisateurs en temps réel. Le système se compose d'un producteur web, d'un processeur Kafka Streams pour l'agrégation, et d'une API REST pour afficher le décompte final.

### Résultats

#### Interface Utilisateur et Production d'Événements
L'interface web permet aux utilisateurs de générer des événements de clic qui sont envoyés au topic Kafka `clicks`.

<img width="1624" height="985" alt="ex2 - Click button page" src="https://github.com/user-attachments/assets/599b7784-de4b-4368-8865-b9cfb76f36ae" />

*Figure 4 : L'interface utilisateur pour générer des événements.*

<img width="1512" height="949" alt="ex2 - checking click" src="https://github.com/user-attachments/assets/53f0fc6c-6d64-4fd3-a27d-bdc2ea7f7ae2" />

*Figure 5 : Preuve que les messages de clics sont bien reçus dans le topic `clicks`.*

---

#### Traitement de Flux et Agrégation
L'application Kafka Streams traite les clics et publie le décompte agrégé dans le topic `click-counts`.

<img width="1624" height="985" alt="ex2 - Count page" src="https://github.com/user-attachments/assets/188a2cd3-1e6f-4352-91d5-7c75101c6802" />

*Figure 6 : Le décompte agrégé en temps réel dans le topic `click-counts`.*

---

#### Résultat Final de l'API
L'API REST expose le décompte final, complétant ainsi le pipeline de données de bout en bout.

*Figure 7 <img width="1512" height="949" alt="ex2 - checking count" src="https://github.com/user-attachments/assets/0f8daeb2-48dd-4daa-989f-d31476c57a8f" />

: Le décompte total des clics affiché par le point de terminaison de l'API REST.*

---

## Instructions de Lancement

### Prérequis

* Java 21+
* Maven 3.8+
* Docker et Docker Compose

### Étapes

1.  **Démarrer l'environnement Kafka :**
    *Assurez-vous que vos conteneurs Docker Kafka et Zookeeper
