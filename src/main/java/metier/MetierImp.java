package metier;

import annotation.Inject;
import annotation.SimplyAutoWired;
import annotation.SimplyComponent;
import dao.IDao;

@SimplyComponent
public class MetierImp implements IMetier {
    @Inject
    private  IDao dao;

    public MetierImp() {
    }

    @SimplyAutoWired // Constructor injection
    public MetierImp(IDao dao) {
        this.dao = dao;
    }

    @Override
    public double calcule() {
        double t = dao.getData();
        return t * 23;
    }
    //@Inject
    public void setDao(IDao dao) {
        this.dao = dao;
    }

}


