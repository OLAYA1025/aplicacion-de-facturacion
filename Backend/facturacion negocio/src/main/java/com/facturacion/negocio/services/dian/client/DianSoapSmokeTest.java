package com.facturacion.negocio.services.dian.client;

import colombia.dian.wcf.IWcfDianCustomerServices;
import colombia.dian.wcf.WcfDianCustomerServices;
import org.springframework.stereotype.Service;

@Service
public class DianSoapSmokeTest {
    public void probar() {
        WcfDianCustomerServices service = new WcfDianCustomerServices();
        IWcfDianCustomerServices port = service.getWSHttpBindingIWcfDianCustomerServices();

        System.out.println(port.getClass().getName());

    }
}
