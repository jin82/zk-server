package cn.jin82.zkadmin.invoke;

import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import org.junit.jupiter.api.Test;

import java.util.Properties;

/**
 * Created by jin on 2019-11-28.
 */
class DubboInvokerTest {

    @Test
    void testInvokerViaUrl() {
        Properties properties = new Properties();
        properties.setProperty("dubbo.application.name", "testADmin");
        properties.setProperty("dubbo.consumer.timeout", "30000");
        ConfigUtils.addProperties(properties);
        ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
        reference.setInterface("cn.pinming.v2.passport.api.service.MemberService");
        reference.setGroup("jin");
        reference.setGeneric(true);
        reference.setUrl("dubbo://172.16.10.30:20890");
        GenericService genericService = reference.get();
        Object result = genericService.$invoke("findMember", new String[]{"java.lang.String"}, new Object[]{"297e2aeb60973dec0160973dec3e0000"});
        System.out.println(result);
    }

}