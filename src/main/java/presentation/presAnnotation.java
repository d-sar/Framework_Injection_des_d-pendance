package presentation;

import core.ApplicationContext;
import metier.IMetier;
import metier.MetierImp;

public class presAnnotation {
    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = new ApplicationContext("config.xml");
        IMetier metier = (MetierImp) applicationContext.getBean("metier");
        System.out.println(metier.calcule());
    }
}
