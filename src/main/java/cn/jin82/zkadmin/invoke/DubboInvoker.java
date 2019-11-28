package cn.jin82.zkadmin.invoke;

import cn.jin82.zkadmin.mata.Instance;
import cn.jin82.zkadmin.mata.ResultRecord;
import cn.jin82.zkadmin.mata.Service;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

/**
 * Created by jin on 2019-11-28.
 */
public class DubboInvoker {

    private Map<ServiceInstanceKey, GenericService> services = new ConcurrentHashMap<>(1024);

    private Service service;


    public List<ResultRecord<?>> invoke(Parameter parameter, Set<Instance> instances) {

        return instances.parallelStream()
                .map(instance -> {
                    GenericService genericService = services.computeIfAbsent(new ServiceInstanceKey(service, instance), k -> {
                        return createService(this.service, instance);
                    });
                    return new ResultRecord<>(instance, genericService);
                })
                .map(invokerWrapper -> {
                    GenericService invoker = invokerWrapper.getObj();
                    Object result = invoker.$invoke(parameter.getMethod(), parameter.getClasses(), parameter.getObjects());
                    return new ResultRecord<>(invokerWrapper.getInstance(),result);
                }).collect(toList());

    }

    private GenericService createService(Service service,Instance instance) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setInterface(service.getName());
        reference.setGroup(service.getGroup());
        reference.setVersion(service.getVersion());
        reference.setGeneric(true);
        reference.setUrl("dubbo://" + instance.getIp() + ":" + instance.getPort());
        return reference.get();
    }



    @RequiredArgsConstructor
    private class ServiceInstanceKey{
        @NonNull
        private Service service;
        @NonNull
        private Instance instance;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ServiceInstanceKey that = (ServiceInstanceKey) o;
            return Objects.equals(service, that.service) &&
                    Objects.equals(instance, that.instance);
        }

        @Override
        public int hashCode() {
            return Objects.hash(service, instance);
        }
    }
}
