package cn.jin82.zkadmin.mata;

import lombok.*;

import java.util.Objects;
import java.util.Set;

/**
 * Created by jin on 2019-11-28.
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class Service {

    @NonNull
    private String name;

    private String group;

    private String version;

    private Set<Instance> instances;

    public boolean hasInstances() {
        return instances != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return Objects.equals(name, service.name) &&
                Objects.equals(group, service.group) &&
                Objects.equals(version, service.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, group, version);
    }
}
