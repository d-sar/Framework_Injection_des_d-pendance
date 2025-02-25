package metier;


import annotation.Inject;
import annotation.SimplyAutoWired;
import annotation.SimplyComponent;
import dao.IDao;

@SimplyComponent
public class MetierImp implements IMetier{
    @Inject
    private IDao dao;

    public MetierImp() {
        this.dao = dao;
    }
    @SimplyAutoWired
    public MetierImp(IDao dao) {
        this.dao = dao;
    }

    @Override
    public double calcule() {
        double t = dao.getData();
        double res = t*23;
        return res;
    }
    @Inject
    public void setDao(IDao dao) {
        this.dao = dao;
    }
}
