package cn.jin82.zkadmin.registry;

import cn.jin82.zkadmin.mata.Instance;
import cn.jin82.zkadmin.mata.Service;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

/**
 * Created by jin on 2019-11-28.
 */
class ZookeeperRegistryTest {

    private ZookeeperRegistry zookeeperRegistry = new ZookeeperRegistry("zookeeper.t.pinming.org", "2181", "dubbo");

    @Test
    void findServices() {
        List<Service> services = zookeeperRegistry.findServices(s -> true);
        services.forEach(System.out::println);
    }

    @Test
    void findInstances() {
        Service service = new Service("cn.pinming.v2.passport.api.service.MemberService");
        service.setGroup("jin");
        Set<Instance> instances = zookeeperRegistry.findInstances(service);
        instances.forEach(System.out::println);
    }
}