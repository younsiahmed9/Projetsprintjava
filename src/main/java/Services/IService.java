package Services;

import java.util.List;

public interface IService<T> {
    T ajouter(T t);
    T modifier(T t);
    void supprimer(int id);
    List<T> afficherTous();
    T afficherParId(int id);
}