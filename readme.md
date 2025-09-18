# Favourite Links

**Tutorial repository pro výuku KIP/7OPR3 + KIP/7VBAP**

## Popis

Jednoduchý projektu pro správu oblíbených odkazů. Obsahuje základ architektury BE - REST AP => Controller => Service => Repository, práce se entitami přes JPA, databázové migrace, zabezpečení přes JWT, správu chyb, logování, mockování, testování.

## Architektura

**Backend**

Základní stavební bloky:
* Java 21
* SpringBoot 3.5.5
* MariaDB (docker container)
* KeyCloak (docker container)

Další bloky:
* SpringBoot Security
* JWT
* Lombok
* JPA + Flyby migrace
* .env
* SFL4J logging
* Mockito

**Frontend**

Základní stavební bloky:
* //TODO

## Obsah repository

### Backend - významné tagy/commity

 Commit/Tag | Popis 
---|---
[Docker Compose pro Maria DB](https://github.com/Engin1980/favourite-links/commit/e9d553fe54e63102241aeeba5e7469030f092d0b) | Zavedení `.env` souboru. Zavedení konfigurací přes `application.properties`. Vytvoření `docker-compose.yml` pro MariaDB.
[ JPA, Entity, Repo, Service, Controller, Flyby Migrace](https://github.com/Engin1980/favourite-links/commit/2773ae91cc99fb75ca26c89459bb76987b669053) | Přidání závislostí do `pom.xml`. Vytvoření entity, repository, služby (Service) a controlleru. Vytvoření init Flyby DB migrace.
[Bruno, Uložení a vrácení Entity, Object Mapping, Link Mapper](https://github.com/Engin1980/favourite-links/commit/c4d3d772513feecfd5684be0cbac056ed3fd0d91)| Demonstrace vytvoření a načtení entity (uživatelského odkazu) přes celou architekturu REST APi => Controller => Service => Repository. Testování přes Bruno. Mapování entity přes LinkMapper
[Logování + AOP](https://github.com/Engin1980/favourite-links/commit/c94da32e9f58348efdc978d9d106acce0fd828d3) | Základní logování přes SFL4J + Logback. Konfigurace přes `logback-spring.xml`. Defaultní aspekty pro logování přes AOP.
[Správa chyb a výjimek](https://github.com/Engin1980/favourite-links/commit/4d4ee7ea0480c76898bcd7a56488e07821233bad) | Základní správa chyb. Update entity. Global Exception Handler. Vlastní výjimky.
[Keycloak docker + služba; Spring Security](https://github.com/Engin1980/favourite-links/commit/03b99b5c6bfdf681ca08acef546ad074f6789d03)| Keycloak v `docker-compose.yml`. Vytvoření a nastavení realmu (v tutorialech). Služba pro obsloužení Keycloak. Vytvoření uživatele + rolí v Keycloak. Správa přístupu přes Spring Security + JWT. Přihlašování a token refresh přes Keycloak.
[Keycloak - jednoduché role](https://github.com/Engin1980/favourite-links/commit/46c8963b5acf1ef08a494d3e32abbd8a7ad42d6c)|Zjednodušení práce s rolemi, pokud stačí univerzální role napříč SpringBoot <=> Keycloak.
[Swagger](https://github.com/Engin1980/favourite-links/commit/1de28a1afb916383a58b9d1814f426798c3c1a2f) | Podpora exportu API přes swagger (bez dokumentace).
[Keycloak + MariaDB uživatelé](https://github.com/Engin1980/favourite-links/commit/4b3aff8c7b0e6ce6d02e2151d99770ca5d5e4054) | Získávání informací o uživatelích kombinací Keycloak + MariaDB.
[Demo unitové + integrační testy, Mock](https://github.com/Engin1980/favourite-links/commit/948e7a019c7a63321c68f48939f0d37705c9de16) | Demo vlastní unitových a integračních testů pro BE. GlobalCleanupExtensions. Mockito + princip mockování tříd.

### Frontend - významné tagy/commity
//TODO

## Dotazy, nejasnosti
Prosím do issues.

## Kontakt
[Marek Vajgl](mailto:marek.vajgl@osu.cz)

