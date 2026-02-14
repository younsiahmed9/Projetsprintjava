# FinTrack Project Analysis - Plan

## 1. Information Gathered

### Project Overview
- **Project Name:** FinTrack - Digital Wallet Management Application
- **Technology Stack:** Java 17, JavaFX 17, MySQL/JDBC, Maven
- **Architecture:** MVC pattern with FXML views and Controllers

### Key Files Analyzed

1. **UserDashboardController.java** (lines 318-344)
   - `handleAjouterCarte()` method currently uses hardcoded `Devise.DT`
   - Need to add currency selection dropdown

2. **user_dashboard.fxml** (lines 1-64)
   - Current header has: FinTrack logo, welcomeLabel, Refresh button, Logout button
   - Missing: Navigation bar with menu items

3. **AdminDashboardController.java** (lines 1-492)
   - Handles users, wallets (portefeuilles), and cards management
   - Has refresh methods for each section

4. **admin_dashboard.fxml** (lines 1-81)
   - Uses TabPane with tabs: Utilisateurs, Portefeuilles, Cartes Virtuelles
   - Missing: Navigation bar in header

### Models
- **Devise enum:** DT, USD, EUR
- **TypeCarte enum:** NORMAL, GOLD, SILVER

---

## 2. Plan

### Task 1: Add Currency Selection when Creating Cards
- [ ] **UserDashboardController.java** - Modify `handleAjouterCarte()` method (lines 318-344)
  - Replace simple ChoiceDialog with a custom Dialog
  - Add ComboBox for card type selection (NORMAL, GOLD, SILVER)
  - Add ComboBox for currency selection (DT, USD, EUR)
  - Create card with selected currency instead of hardcoded Devise.DT

### Task 2: Add Navigation Bar to User Dashboard
- [ ] **user_dashboard.fxml** - Add navigation bar in header
  - Add HBox with nav buttons: Dashboard, Transactions
  - Style nav items with hover effects
- [ ] **UserDashboardController.java** - Add navigation methods
  - Add `handleGoToDashboard()` method
  - Add `handleGoToTransactions()` method

### Task 3: Add Navigation Bar to Admin Dashboard
- [ ] **admin_dashboard.fxml** - Add navigation bar in header
  - Add HBox with nav buttons: Dashboard, Users, Portefeuilles, Cartes, Transactions
  - Style nav items with hover effects
- [ ] **AdminDashboardController.java** - Add navigation methods
  - Add methods to navigate between different tabs/views

---

## 3. Dependent Files

- `src/main/java/Models/Devise.java` - Currency enum (already exists)
- `src/main/java/Models/TypeCarte.java` - Card type enum (already exists)
- `src/main/resources/views/transactions_view.fxml` - May need to check if this exists

---

## 4. Followup Steps

1. Apply modifications to UserDashboardController.java for currency selection
2. Apply modifications to user_dashboard.fxml for navigation bar
3. Apply modifications to UserDashboardController.java for navigation methods
4. Apply modifications to admin_dashboard.fxml for navigation bar
5. Apply modifications to AdminDashboardController.java for navigation methods
6. Test the application to verify changes work correctly