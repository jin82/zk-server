package cn.jin82.zkadmin.mata;

import lombok.*;

import java.util.Objects;

/**
 * Created by jin on 2019-11-28.
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class Instance {

    private String ip;

    private String port;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instance instance = (Instance) o;
        return Objects.equals(ip, instance.ip) &&
                Objects.equals(port, instance.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
