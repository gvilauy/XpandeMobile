package org.xpande.mobile.model;

import java.sql.ResultSet;
import java.util.Properties;

/**
 * Modelo de datos de codigos de barra recibidos en lineas de movilidad InOut.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 11/2/18.
 */
public class MZMBInOutUPC extends X_Z_MB_InOutUPC {

    public MZMBInOutUPC(Properties ctx, int Z_MB_InOutUPC_ID, String trxName) {
        super(ctx, Z_MB_InOutUPC_ID, trxName);
    }

    public MZMBInOutUPC(Properties ctx, ResultSet rs, String trxName) {
        super(ctx, rs, trxName);
    }
}
