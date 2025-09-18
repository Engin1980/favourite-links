# Zprovoznění Keycloak

## Instalace z Dockeru
Instalace z Dockeru, defaultně na port 8080 + nastavení administrátorského jména a hesla.
Data se ukládají do databáze, takže potřebuje být na stejné síti jako DB (mariaDb) a znát
přihlašovací údaje.

## Nastavení Realmu

Přihlásit se na `http://localhost:8080` admin jménem a heslem. Pokud se na úvodní stránce nezobrazí
formulář pro přihlášení, při vytváření kontejneru nebyl správně nastaven `KEYCLOAK_USER` a `KEYCLOAK_PASSWORD`.

### 1. Vytvoření nového realmu: 
   1. Kliknout na `Select realm` vlevo nahoře
   2. Kliknout na `Create realm`
   3. Vyplnit `Name` (např. `favourite-links-realm`)
   4. Kliknout na `Create`

Přepnout se na vytvořený realm (vlevo nahoře).

### 2. Vytvoření admin klienta pro správu uživatelů

Alternativně lze použít `admin-cli`, ale musí se mu zapnout "Client Authentication"):
1. Kliknout na `Clients` v levém menu
2. Kliknout na `Create client`
3. Vyplnit `Client ID` (např. `favourite-links-admin`)
4. V nastavení klienta zvolit "Client authentication" a "Service account roles", zbytek odškrtnout
5. Uložit
   
Dále v detailu klienta:
6. V záložce `Service Account Roles` přidat roli `manage-users` z `realm-management` (pozor, je možná až na druhé stránce seznamu rolí)
7. V záložce `Service Account Roles` přidat roli `view-realm` z `realm-management` (pozor, je možná až na druhé stránce seznamu rolí)
7. Zkopírovat si hodnotu `Secret` z detailu klienta (v záložce `Credentials`)

> `manage-users` role umožňuje vytvářet, mazat a upravovat uživatele uvnitř daného realmu v Keycloak

### 3. Vytvoření klienta pro přihlašování a práci s tokeny
1. Kliknout na `Clients` v levém menu
2. Kliknout na `Create client`
3. Vyplnit `Client ID` (např. `springboot-client`)
4. V nastavení klienta zvolit "Direct Access Grants Enabled"
5. Uložit

> `Direct Access Grants Enabled` zajistí, že lze autentizovat uživatele pomocí uživatelského jména a hesla (Resource Owner Password Credentials Grant) uživatele v keycloak namísto autentizace klientského id.

### 4. Upravit nastavení uživatelů
Cílem je nastavit uživatelům, že se mají přihlašovat e-mailem a že není jméno/příjmení povinné:
1. Kliknout na `Realm Settings` v levém menu
2. V záložce `Login` zapnout `Login with email` a `Email as username`
3. V záložce `User Profile` u položek `firstName` a `lastName` v detailu vypnout `Required field`. (Možná by pomohlo je i smazat, nevyzkoušeno.)

### 5. Vytvoření rolí
1. Kliknout na `Realm roles`
2. Vytvořit role pro uživatele a admina (předpokládáme `KC_ROLE_USER` a `KC_ROLE_ADMIN`)

### 6. Vynucení rolí v JWT tokenu

**Vytvoření "mapperu", který bude role do JWT přidávat**

1. Kliknout na `Client Scopes`
2. Vybrat a zobrazit detail od `roles`
3. Vybrat záložku `Mapers`
4. Vytvořit nový mapper "by configuration" - nebo upravit existující `roles`:
    - vybrat mapper podle konfigurace `User Realm Role`
    - Vyplnit jméno mapperu
    - vložit token claim name (např. `roles`) - pod tímhle klíčem bude v tokenu
    - nastavit `Multivalued` na ON - těch rolí je několik a naše je jen jedna z nich
    - Zaškrtnout `Add to ID token` a `Add to access token`, aby byly v tokenu
    - Uložit

**Nastavení klienta, aby client scope/mapper používal**
1. Přejít na `Clients`, vybrat klienta pro ověřování uživatelů (`springboot-client`), záložka `Client Scopes`
2. V ní přidat do `Client Scopes` scope `roles` jako `default`

### 7. Refresh Token Rotation in Keycloak
Pokud není zapnutá rotace, refresh tokeny se dají použít opakovaně, což je bezpečnostní riziko.
Pokud je zapnuto, nový refresh token expiruje předchozí tokeny.
Nastavit lze na (upravit realm name):
http://localhost:8080/admin/master/console/#/favourite-links-realm/realm-settings/tokens

## Další info
* Realm je třeba duplikovat pro testovací prostředí.
* Hesla a citlivé informace dávat do `.env` souboru.
* Keycloak lze použít s pomocí knihovny `keycloak-admin`. 
    - Knihovna však nepodporuje rozumnou implementaci refresh tokenů.
    - Závislost knihovny v pom.xml generuje warningy ohledně referencí a bezpečnosti.
    - Takže se v projektu použil původní čistý HTTP-request based způsob.
* Keycloak někdy od verze 17/18 přešel výraznou změnou. 
Pozor, že spousta tutorialů (i rad od LLM) je neaktuálních.
