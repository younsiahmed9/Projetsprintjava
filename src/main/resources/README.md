# FinTrack

## Base de données (MySQL / XAMPP)

### Schema
- Exécuter `src/main/resources/sql/fintrack_schema.sql` (première installation)

### Migration (si la base existe déjà)
Si vous avez déjà créé la table `users` avant l’ajout de la photo de profil, exécutez:
- `src/main/resources/sql/fintrack_migration_profile_photo.sql`

Ensuite vous pouvez (optionnel) recharger des données de test:
- `src/main/resources/sql/fintrack_seed.sql`

