package cn.jin82.zkadmin.registry;

import cn.jin82.zkadmin.mata.Instance;
import cn.jin82.zkadmin.mata.Service;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by jin on 2019-11-28.
 */
public interface Registry {

    List<Service> findServices(Predicate<String> filter);

    Set<Instance> findInstances(Service service);

}
