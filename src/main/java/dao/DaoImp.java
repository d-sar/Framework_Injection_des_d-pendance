package dao;

import annotation.SimplyComponent;

@SimplyComponent
public class DaoImp implements IDao {
    @Override
    public double getData() {
        System.out.println("Version base de données");
        return 11;
    }
}