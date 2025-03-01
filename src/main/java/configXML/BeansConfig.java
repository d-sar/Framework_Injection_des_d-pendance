package configXML;



import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "beans")
public class BeansConfig {
    private List<BeanConfig> beans;

    @XmlElement(name = "bean")
    public List<BeanConfig> getBeans() {
        return beans;
    }

    public void setBeans(List<BeanConfig> beans) {
        this.beans = beans;
    }
}
