package cn.jin82.zkadmin.invoke;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Created by jin on 2019-11-28.
 */
@Getter
@Setter
public class Parameter {
    @NonNull
    private String method;

    private String[] classes;

    private Object[] objects;
}
