package dao;

import annotation.SimplyComponent;

@SimplyComponent
public class DaoImp implements IDao{
    @Override
    public double getData() {
        System.out.println("version base de donnees");
        double res = 11;
        return res;
    }
}
