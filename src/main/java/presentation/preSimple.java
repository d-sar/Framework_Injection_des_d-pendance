package presentation;

import dao.DaoImp;
import metier.MetierImp;

public class preSimple {
    public static void main(String[] args) {
        DaoImp dao = new DaoImp();
        MetierImp metier = new MetierImp(dao);
        System.out.println("resultat : "+metier.calcule());
    }
}
