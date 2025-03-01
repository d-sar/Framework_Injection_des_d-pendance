package presentation;

import core.ApplicationContextAnnotation;
import metier.IMetier;
import metier.MetierImp;


public class presAnnotation {
    public static void main(String[] args) throws Exception {
        ApplicationContextAnnotation applicationContext = new ApplicationContextAnnotation(MetierImp.class, "ext");
        IMetier metier = applicationContext.getBean(IMetier.class);
        System.out.println(metier.calcule());
    }
}

