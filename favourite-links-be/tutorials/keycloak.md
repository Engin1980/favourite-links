# Zprovoznění Keycloak

## Instalace z Dockeru
Instalace z Dockeru, defaultně na port 8080 + nastavení administrátorského jména a hesla.
Data se ukládají do databáze, takže potřebuje být na stejné síti jako DB (mariaDb) a znát
přihlašovací údaje.

## Nastavení Realmu
1. Přihlásit se na `http://localhost:8080` admin jménem a heslem. Pokud se na úvodní stránce nezobrazí
formulář pro přihlášení, při vytváření kontejneru nebyl správně nastaven `KEYCLOAK_USER` a `KEYCLOAK_PASSWORD`.

2. Vytvoření nového realmu: 
   - Kliknout na `Select realm` vlevo nahoře
   - Kliknout na `Create realm`
   - Vyplnit `Name` (např. `favourite-links-realm`)
   - Kliknout na `Create`

3. Přepnout se na vytvořený realm.

4. Vytvoření klienta pro správu uživatelů (alternativně lze použít `admin-cli`, 
ale musí se mu zapnout "Client Authentication"):
   - Kliknout na `Clients` v levém menu
   - Kliknout na `Create client`
   - Vyplnit `Client ID` (např. `favourite-links-admin`)
   - V nastavení klienta zvolit "Client authentication" a "Service account roles", zbytek odškrtnout
   - Uložit
   - Dále v detailu klienta:
     - V zálžoce `Service Account Roles` přidat roli `manage-users` z `realm-management` (pozor, je možná až na druhé stránce seznamu rolí)
     - Zkopírovat si hodnotu `Secret` z detailu klienta (v záložce `Credentials`)

5. Vytvoření klienta pro přihlašování a práci s tokeny:
   - Kliknout na `Clients` v levém menu
   - Kliknout na `Create client`
   - Vyplnit `Client ID` (např. `favourite-links-frontend`)
   - V nastavení klienta zvolit "Direct Access Grants Enabled"
   - Uložit

6. Uživatelům nastavit, že se mají přihlašovat e-mailem a že není jméno/příjmení povinné:
   - Kliknout na `Realm Settings` v levém menu
   - V záložce `Login` zapnout `Login with email` a `Email as username`
   - V záložce `User Profile` u položek `firstName` a `lastName` v detailu vypnout `Required field`. (Možná by pomohlo je i smazat, nevyzkoušeno.)

> `manage-users` role umožňuje vytvářet, mazat a upravovat uživatele uvnitř daného realmu v Keycloak

> `Direct Access Grants Enabled` zajistí, že lze autentizovat uživatele pomocí uživatelského jména a hesla (Resource Owner Password Credentials Grant) uživatele v keycloak namísto autentizace klientského id.

7. Vytvoření rolí
    - Kliknout na `Client Scopes`
    - Vybrat a zobrazit detail od `roles`
    - Vybrat záložku `Mapers`
    - Vytvořit nový mapper "by configuration":
      - Vyplnit jméno mapperu a správné `client-id`
      - Zadat `Token Claim Name` (např. `roles`)
      - Zbytek dle potřeby
      - Uložit

## Úprava nastavení projektu
* Hesla a citlivé informace dávat do `.env` souboru.
* Keycloak lze použít s pomocí knihovny `keycloak-admin`. 
    - Knihovna však nepodporuje rozumnou implementaci refresh tokenů.
    - Závislost knihovny v pom.xml generuje warningy ohledně referencí a bezpečnosti.
    - Takže se v projektu použil původní čistý HTTP-request based způsob.

## Refresh Token Rotation in Keycloak
http://localhost:8080/admin/master/console/#/favourite-links-realm/realm-settings/tokens