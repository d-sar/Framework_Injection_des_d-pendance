package ext;

import annotation.SimplyComponent;
import dao.IDao;

@SimplyComponent
public class dao2 implements IDao {
    @Override
    public double getData() {
        System.out.println("dao2");
        return 1;
    }
}
