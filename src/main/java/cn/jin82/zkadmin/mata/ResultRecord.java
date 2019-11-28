package cn.jin82.zkadmin.mata;

import lombok.*;

/**
 * Created by jin on 2019-11-28.
 */
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class ResultRecord<T> {
    @NonNull
    private final Instance instance;

    private T obj;

}
