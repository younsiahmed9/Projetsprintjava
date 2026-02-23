package Models;

/**
 * Type concret d'un utilisateur.
 *
 * Dans ce projet, ce type est déduit de l'héritage DB (présence dans admins/clients)
 * et n'est pas stocké dans la table users.
 */
public enum UserType {
    ADMIN,
    CLIENT
}
