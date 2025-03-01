package metier;

import annotation.SimplyAutoWired;
import annotation.SimplyComponent;
import dao.IDao;

@SimplyComponent
public class MetierImp implements IMetier {
    private final IDao dao;

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

}


