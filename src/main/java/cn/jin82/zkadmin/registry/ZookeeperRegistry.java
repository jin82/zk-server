package cn.jin82.zkadmin.registry;

import cn.jin82.zkadmin.mata.Instance;
import cn.jin82.zkadmin.mata.Service;
import com.alibaba.dubbo.common.URL;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by jin on 2019-11-28.
 */
@Slf4j
public class ZookeeperRegistry implements Registry{

    private final String ARG_INTERFACE = "interface";
    private final String ARG_GROUP = "default.group";
    private final String ARG_VERSION = "default.version";

    @NonNull
    private String ip;
    @NonNull
    private String port;
    @NonNull
    private String namespace;

    private CuratorFramework curator;

    private final Object refreshLock = new Object();

    private List<String> paths = Collections.emptyList();

    public ZookeeperRegistry(@NonNull String ip, @NonNull String port, @NonNull String namespace) {
        this.ip = ip;
        this.port = port;
        this.namespace = namespace;

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(ip + ":" + port, 60 * 1000, 5000, retryPolicy);
        client.getUnhandledErrorListenable().addListener(new UnhandledErrorListener() {
            @Override
            public void unhandledError(String message, Throwable e) {
                if (log.isErrorEnabled()) {
                    log.error(message, e);
                }
            }
        });
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework c, ConnectionState newState) {
                if (log.isDebugEnabled()) {
                    log.debug("state={}", newState);
                }
            }
        });
        client.start();
        this.curator = client.usingNamespace(namespace);
    }

    @Override
    public List<Service> findServices(Predicate<String> filter) {
        this.refresh();
        try {
            Stream<String> pathsStreram = paths.stream();
            if (filter != null) {
                pathsStreram = pathsStreram.filter(filter);
            }
            return pathsStreram.flatMap(this.findProviders)
                    .filter(Objects::nonNull)
                    .map(URL::valueOf)
                    .map(u -> {
                        String interfaceName = u.getParameterAndDecoded(ARG_INTERFACE);
                        String group = u.getParameterAndDecoded(ARG_GROUP);
                        String version = u.getParameterAndDecoded(ARG_VERSION);
                        Service s = new Service(interfaceName);
                        s.setGroup(group);
                        s.setVersion(version);
                        return s;
                    }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public Set<Instance> findInstances(Service service) {
        if (service.hasInstances()) {
            return service.getInstances();
        } else {
            Set<Instance> instances = doFindServiceInstances(service);
            service.setInstances(instances);
            return instances;
        }
    }

    private Set<Instance> doFindServiceInstances(Service queryServiceExample) {
        String interfaceName = queryServiceExample.getName();
        return Stream.of(interfaceName)
                .flatMap(this.findProviders)
                .map(URL::valueOf)
                .filter(u -> this.filterUrl(u, ARG_GROUP, queryServiceExample.getGroup()))
                .filter(u -> this.filterUrl(u, ARG_VERSION, queryServiceExample.getVersion()))
                .map(u -> {
                    String ip = u.getIp();
                    int port = u.getPort();
                    return new Instance(ip, String.valueOf(port));
                }).collect(Collectors.toSet());
    }

    private boolean filterUrl(URL url, String key, String value) {
        if (value == null) {
            return true;
        }
        String parameterAndDecoded = url.getParameterAndDecoded(key);
        if (!StringUtils.isEmpty(parameterAndDecoded)) {
            return parameterAndDecoded.equals(value);
        } else {
            return false;
        }
    }

    private Function<String, Stream<String>> findProviders = s -> {
        try {
            return curator.getChildren().forPath("/" + s + "/providers").stream().map(this::decode).filter(Optional::isPresent).map(Optional::get);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Stream.empty();
    };

    private Optional<String> decode(String s) {
        try {
            return Optional.of(URLDecoder.decode(s, "UTF-8"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    private void refresh() {
        synchronized (refreshLock) {
            try {
                paths = curator.getChildren().forPath("/");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
